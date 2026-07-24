package com.project.oditji.member.service;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.project.oditji.member.dao.MemberDAO;
import com.project.oditji.member.dao.MemberSocialDAO;
import com.project.oditji.member.exception.MemberBlockedException;
import com.project.oditji.member.exception.MemberWithdrawnException;
import com.project.oditji.member.vo.MemberSocialJoinVO;
import com.project.oditji.member.vo.MemberSocialVO;
import com.project.oditji.member.vo.MemberVO;
import com.project.oditji.member.vo.NaverLoginResultVO;
import com.project.oditji.member.vo.NaverTokenVO;
import com.project.oditji.member.vo.NaverUserInfoVO;

/**
 * 네이버 OAuth 로그인 서비스 구현체입니다.
 *
 * 네이버 접근 토큰 발급, 프로필 조회, MEMBER 및 MEMBER_SOCIAL 등록,
 * 기존 회원 상태 확인을 한 곳에서 처리합니다.
 */
@Service
public class NaverLoginServiceImpl implements NaverLoginService {

    private static final String PROVIDER_NAVER = "NAVER";
    private static final String NAVER_AUTHORIZE_URL =
            "https://nid.naver.com/oauth2.0/authorize";
    private static final String NAVER_TOKEN_URL =
            "https://nid.naver.com/oauth2.0/token";
    private static final String NAVER_PROFILE_URL =
            "https://openapi.naver.com/v1/nid/me";
    private static final int STATE_BYTE_LENGTH = 32;
    private static final int MEMBER_ID_HASH_LENGTH = 40;

    private final MemberDAO memberDAO;
    private final MemberSocialDAO memberSocialDAO;
    private final RestTemplate restTemplate;
    private final SecureRandom secureRandom;

    @Value("${naver.client-id}")
    private String naverClientId;

    @Value("${naver.client-secret}")
    private String naverClientSecret;

    @Value("${naver.redirect-uri}")
    private String naverRedirectUri;

    public NaverLoginServiceImpl(MemberDAO memberDAO,
                                 MemberSocialDAO memberSocialDAO) {
        this.memberDAO = memberDAO;
        this.memberSocialDAO = memberSocialDAO;
        this.restTemplate = new RestTemplate();
        this.secureRandom = new SecureRandom();
    }

    /**
     * 로그인 요청마다 새로운 state 값을 만들어 CSRF 공격을 방지합니다.
     */
    @Override
    public String createState() {
        byte[] randomBytes = new byte[STATE_BYTE_LENGTH];
        secureRandom.nextBytes(randomBytes);

        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(randomBytes);
    }

    /**
     * 네이버 인증 요청 URL을 생성합니다.
     *
     * redirect_uri와 state는 URL 인코딩된 상태로 전달됩니다.
     */
    @Override
    public String getNaverLoginUrl(String state) {
        if (state == null || state.isBlank()) {
            throw new IllegalArgumentException("네이버 로그인 state 값이 없습니다.");
        }

        return UriComponentsBuilder
                .fromUriString(NAVER_AUTHORIZE_URL)
                .queryParam("response_type", "code")
                .queryParam("client_id", naverClientId)
                .queryParam("redirect_uri", naverRedirectUri)
                .queryParam("state", state)
                .build()
                .encode(StandardCharsets.UTF_8)
                .toUriString();
    }

    /**
     * 인증 코드로 토큰과 프로필을 조회한 뒤 기존 회원 로그인 또는 신규 가입을 처리합니다.
     */
    @Override
    @Transactional
    public NaverLoginResultVO naverLogin(String code, String state) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("네이버 인증 코드가 없습니다.");
        }

        if (state == null || state.isBlank()) {
            throw new IllegalArgumentException("네이버 로그인 state 값이 없습니다.");
        }

        NaverTokenVO token = requestToken(code, state);
        NaverUserInfoVO naverUser = requestUserInfo(token.getAccessToken());
        String providerUserId = naverUser.getProviderUserId();

        MemberSocialJoinVO existingMember =
                memberSocialDAO.selectMemberBySocial(
                        PROVIDER_NAVER,
                        providerUserId);

        if (existingMember != null) {
            validateMemberStatus(existingMember);
            return new NaverLoginResultVO(false, existingMember);
        }

        MemberVO newMember = createNaverMember(naverUser, providerUserId);

        /*
         * 기존 공통 MEMBER INSERT를 재사용하므로
         * MemberDAO와 memberMapper.xml은 수정하지 않습니다.
         */
        memberDAO.insertMember(newMember);

        MemberSocialVO memberSocialVO = new MemberSocialVO();
        memberSocialVO.setMemberNo(newMember.getMemberNo());
        memberSocialVO.setProvider(PROVIDER_NAVER);
        memberSocialVO.setProviderUserId(providerUserId);

        memberSocialDAO.insertMemberSocial(memberSocialVO);

        MemberSocialJoinVO joinedMember =
                memberSocialDAO.selectMemberBySocial(
                        PROVIDER_NAVER,
                        providerUserId);

        if (joinedMember == null) {
            throw new IllegalStateException("네이버 회원 연동 정보 생성에 실패했습니다.");
        }

        return new NaverLoginResultVO(true, joinedMember);
    }

    /**
     * 네이버 인증 코드로 접근 토큰을 발급받습니다.
     */
    private NaverTokenVO requestToken(String code, String state) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", naverClientId);
        params.add("client_secret", naverClientSecret);
        params.add("code", code);
        params.add("state", state);

        HttpEntity<MultiValueMap<String, String>> request =
                new HttpEntity<>(params, headers);

        try {
            ResponseEntity<NaverTokenVO> response = restTemplate.exchange(
                    NAVER_TOKEN_URL,
                    HttpMethod.POST,
                    request,
                    NaverTokenVO.class);

            NaverTokenVO token = response.getBody();

            if (token == null
                    || token.getAccessToken() == null
                    || token.getAccessToken().isBlank()) {

                String errorDescription = token == null
                        ? null
                        : token.getErrorDescription();

                throw new IllegalStateException(
                        buildNaverErrorMessage(
                                "네이버 접근 토큰 발급에 실패했습니다.",
                                errorDescription));
            }

            return token;

        } catch (RestClientResponseException e) {
            throw new IllegalStateException(
                    "네이버 접근 토큰 발급 요청에 실패했습니다.",
                    e);
        }
    }

    /**
     * 발급받은 접근 토큰으로 네이버 회원 프로필을 조회합니다.
     */
    private NaverUserInfoVO requestUserInfo(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        RequestEntity<Void> request = RequestEntity
                .get(URI.create(NAVER_PROFILE_URL))
                .headers(headers)
                .build();

        try {
            ResponseEntity<NaverUserInfoVO> response =
                    restTemplate.exchange(request, NaverUserInfoVO.class);

            NaverUserInfoVO naverUser = response.getBody();

            if (naverUser == null
                    || naverUser.getProviderUserId() == null
                    || naverUser.getProviderUserId().isBlank()) {

                throw new IllegalStateException(
                        "네이버 사용자 정보 조회에 실패했습니다.");
            }

            if (naverUser.getResultcode() != null
                    && !"00".equals(naverUser.getResultcode())) {

                throw new IllegalStateException(
                        buildNaverErrorMessage(
                                "네이버 사용자 정보 조회에 실패했습니다.",
                                naverUser.getMessage()));
            }

            return naverUser;

        } catch (RestClientResponseException e) {
            throw new IllegalStateException(
                    "네이버 사용자 정보 조회 요청에 실패했습니다.",
                    e);
        }
    }

    /**
     * 기존 연동 회원의 정지 및 탈퇴 상태를 확인합니다.
     */
    private void validateMemberStatus(MemberSocialJoinVO member) {
        if ("BLOCKED".equals(member.getStatus())) {
            throw new MemberBlockedException(
                    "정지된 계정입니다. 고객센터로 문의해주세요.");
        }

        if ("WITHDRAWN".equals(member.getStatus())) {
            throw new MemberWithdrawnException(
                    "탈퇴한 계정입니다.",
                    member.getMemberNo(),
                    member.getWithdrawnAt());
        }
    }

    /**
     * 네이버 프로필을 ODITJI MEMBER 형식으로 변환합니다.
     *
     * 네이버 이용자 식별자는 최대 64자이므로 MEMBER_ID의 50자 제한을 넘을 수 있습니다.
     * 따라서 원본 식별자는 MEMBER_SOCIAL에 저장하고, MEMBER_ID에는 SHA-256 해시 일부를 사용합니다.
     */
    private MemberVO createNaverMember(
            NaverUserInfoVO naverUser,
            String providerUserId) {

        MemberVO memberVO = new MemberVO();

        memberVO.setMemberId(buildNaverMemberId(providerUserId));
        memberVO.setMemberPw(null);
        memberVO.setMemberName(naverUser.getName());
        memberVO.setNickname(naverUser.getSafeNickname());

        /*
         * 기존 카카오 자동가입과 동일하게 이메일은 최초 OTT 선택 화면에서 입력받습니다.
         * 네이버 이메일이 기존 일반회원 이메일과 중복되어 자동가입이 실패하는 상황을 막습니다.
         */
        memberVO.setEmail(null);
        memberVO.setPhone(null);
        memberVO.setProfileImage(naverUser.getProfileImage());
        memberVO.setRole("USER");
        memberVO.setStatus("ACTIVE");
        memberVO.setAdultVerified("N");

        return memberVO;
    }

    /**
     * 네이버 식별자를 MEMBER_ID 길이에 맞는 고정 문자열로 변환합니다.
     */
    private String buildNaverMemberId(String providerUserId) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            byte[] digest = messageDigest.digest(
                    providerUserId.getBytes(StandardCharsets.UTF_8));
            String hash = HexFormat.of().formatHex(digest);

            return "naver_" + hash.substring(0, MEMBER_ID_HASH_LENGTH);

        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(
                    "네이버 회원 아이디 생성에 실패했습니다.",
                    e);
        }
    }

    /**
     * 네이버가 전달한 오류 설명이 있을 때 사용자 안내 문구 뒤에 덧붙입니다.
     */
    private String buildNaverErrorMessage(
            String defaultMessage,
            String errorDescription) {

        if (errorDescription == null || errorDescription.isBlank()) {
            return defaultMessage;
        }

        return defaultMessage + " " + errorDescription;
    }
}
