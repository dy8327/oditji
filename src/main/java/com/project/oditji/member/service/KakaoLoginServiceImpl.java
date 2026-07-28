package com.project.oditji.member.service;

import java.net.URI;

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
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.client.RestClientResponseException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.project.oditji.member.dao.MemberDAO;
import com.project.oditji.member.dao.MemberSocialDAO;
import com.project.oditji.member.exception.MemberBlockedException;
import com.project.oditji.member.exception.MemberWithdrawnException;
import com.project.oditji.member.vo.KakaoLoginResultVO;
import com.project.oditji.member.vo.KakaoTokenVO;
import com.project.oditji.member.vo.KakaoUserInfoVO;
import com.project.oditji.member.vo.MemberSocialJoinVO;
import com.project.oditji.member.vo.MemberSocialVO;
import com.project.oditji.member.vo.MemberVO;

@Service
public class KakaoLoginServiceImpl implements KakaoLoginService {

    private static final String PROVIDER_KAKAO = "KAKAO";

    private final MemberDAO memberDAO;
    private final MemberSocialDAO memberSocialDAO;
    private final RestTemplate restTemplate;
    private static final Logger log = LoggerFactory.getLogger(KakaoLoginServiceImpl.class);

    @Value("${kakao.client-id}")
    private String kakaoClientId;

    @Value("${kakao.redirect-uri}")
    private String kakaoRedirectUri;

    @Value("${kakao.client-secret:}")
    private String kakaoClientSecret;

    public KakaoLoginServiceImpl(MemberDAO memberDAO,
                                 MemberSocialDAO memberSocialDAO) {
        this.memberDAO = memberDAO;
        this.memberSocialDAO = memberSocialDAO;
        this.restTemplate = new RestTemplate();
    }

    @Override
    public String getKakaoLoginUrl() {
        return UriComponentsBuilder
                .fromUriString("https://kauth.kakao.com/oauth/authorize")
                .queryParam("response_type", "code")
                .queryParam("client_id", kakaoClientId)
                .queryParam("redirect_uri", kakaoRedirectUri)
                .build()
                .toUriString();
    }

    @Override
    @Transactional
    public KakaoLoginResultVO kakaoLogin(String code) {
        KakaoTokenVO token = requestToken(code);
        KakaoUserInfoVO kakaoUser = requestUserInfo(token.getAccessToken());

        String providerUserId = String.valueOf(kakaoUser.getId());

        MemberSocialJoinVO existingMember =
                memberSocialDAO.selectMemberBySocial(PROVIDER_KAKAO, providerUserId);

        // 기존 회원인 경우
        if (existingMember != null) {

            /*
             * 카카오 토큰 발급 + 사용자 정보 조회에 성공했다는 것 자체가
             * "카카오 계정 소유자 본인"임을 증명하므로,
             * 정지/탈퇴 여부만 STATUS로 분기한다.
             */
            if ("BLOCKED".equals(existingMember.getStatus())) {
                throw new MemberBlockedException("정지된 계정입니다. 고객센터로 문의해주세요.");
            }

            if ("WITHDRAWN".equals(existingMember.getStatus())) {
                throw new MemberWithdrawnException(
                        "탈퇴한 계정입니다.",
                        existingMember.getMemberNo(),
                        existingMember.getWithdrawnAt());
            }

            return new KakaoLoginResultVO(false, existingMember);
        }

        // 신규 회원 가입
        MemberVO newMember = createKakaoMember(kakaoUser);
        memberDAO.insertKakaoMember(newMember);

        MemberSocialVO memberSocialVO = new MemberSocialVO();
        memberSocialVO.setMemberNo(newMember.getMemberNo());
        memberSocialVO.setProvider(PROVIDER_KAKAO);
        memberSocialVO.setProviderUserId(providerUserId);

        memberSocialDAO.insertMemberSocial(memberSocialVO);

        MemberSocialJoinVO joinedMember =
                memberSocialDAO.selectMemberBySocial(PROVIDER_KAKAO, providerUserId);

        return new KakaoLoginResultVO(true, joinedMember);
    }

    private KakaoTokenVO requestToken(String code) {
        String tokenUrl = "https://kauth.kakao.com/oauth/token";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", kakaoClientId);
        params.add("redirect_uri", kakaoRedirectUri);
        params.add("code", code);

        if (kakaoClientSecret != null && !kakaoClientSecret.isBlank()) {
            params.add("client_secret", kakaoClientSecret);
        }

        HttpEntity<MultiValueMap<String, String>> request =
                new HttpEntity<>(params, headers);

        ResponseEntity<KakaoTokenVO> response;

            try {
                response = restTemplate.exchange(tokenUrl, HttpMethod.POST, request, KakaoTokenVO.class);
            } catch (RestClientResponseException e) {
                if (log.isErrorEnabled()) {
                    log.error("카카오 토큰 발급 실패 - HTTP 상태: {}, 응답: {}", e.getStatusCode(), e.getResponseBodyAsString(), e);
                }
                throw new IllegalStateException("카카오 로그인 인증에 실패했습니다.", e);
            }

        KakaoTokenVO token = response.getBody();
        if (token == null || token.getAccessToken() == null) {
            throw new IllegalStateException("카카오 access_token 발급에 실패했습니다.");
        }

        return token;
    }

    private KakaoUserInfoVO requestUserInfo(String accessToken) {
        String userInfoUrl = "https://kapi.kakao.com/v2/user/me";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        RequestEntity<Void> request = RequestEntity
                .get(URI.create(userInfoUrl))
                .headers(headers)
                .build();

        ResponseEntity<KakaoUserInfoVO> response;

            try {
                response = restTemplate.exchange(request, KakaoUserInfoVO.class);
            } catch (RestClientResponseException e) {
                if (log.isErrorEnabled()) {
                    log.error("카카오 사용자 정보 조회 실패 - HTTP 상태: {}, 응답: {}", e.getStatusCode(), e.getResponseBodyAsString(), e);
                }
                throw new IllegalStateException("카카오 사용자 정보 조회에 실패했습니다.", e);
            }

        KakaoUserInfoVO kakaoUser = response.getBody();

        if (kakaoUser == null || kakaoUser.getId() == null) {
            throw new IllegalStateException("카카오 사용자 정보 조회에 실패했습니다.");
        }

        return kakaoUser;
    }

    private MemberVO createKakaoMember(KakaoUserInfoVO kakaoUser) {
        String kakaoId = String.valueOf(kakaoUser.getId());

        String nickname = kakaoUser.getNickname();

        MemberVO memberVO = new MemberVO();

        memberVO.setMemberId("kakao_" + kakaoId);
        memberVO.setMemberPw(null);

        // 팀 회의 결과 이름 NULL 허용
        memberVO.setMemberName(null);

        // 카카오에서 받은 닉네임은 NICKNAME에 저장
        memberVO.setNickname(nickname);

        // 팀 회의 결과 이메일 NULL 허용
        memberVO.setEmail(null);

        memberVO.setPhone(null);
        memberVO.setProfileImage(kakaoUser.getProfileImageUrl());
        memberVO.setRole("USER");
        memberVO.setStatus("ACTIVE");
        memberVO.setAdultVerified("N");

        return memberVO;
    }
}
