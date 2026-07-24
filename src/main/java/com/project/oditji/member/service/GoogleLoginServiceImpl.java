package com.project.oditji.member.service;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.springframework.core.env.Environment;
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
import com.project.oditji.member.vo.GoogleLoginResultVO;
import com.project.oditji.member.vo.GoogleTokenVO;
import com.project.oditji.member.vo.GoogleUserInfoVO;
import com.project.oditji.member.vo.MemberSocialJoinVO;
import com.project.oditji.member.vo.MemberSocialVO;
import com.project.oditji.member.vo.MemberVO;

/**
 * Google OAuth 2.0 / OpenID Connect 로그인 구현체입니다.
 *
 * 기존 ODITJI의 카카오·네이버 로그인과 동일하게 RestTemplate을 사용해
 * 인증 코드 발급 → 토큰 교환 → 사용자정보 조회 → MEMBER 저장 순서로 처리합니다.
 *
 * 요청 범위는 openid profile만 사용합니다.
 * 따라서 이메일은 요청하거나 MEMBER.EMAIL에 저장하지 않습니다.
 */
@Service
public class GoogleLoginServiceImpl implements GoogleLoginService {

    private static final String PROVIDER_GOOGLE = "GOOGLE";

    private static final String GOOGLE_AUTHORIZATION_URL =
            "https://accounts.google.com/o/oauth2/v2/auth";

    private static final String GOOGLE_TOKEN_URL =
            "https://oauth2.googleapis.com/token";

    private static final String GOOGLE_USER_INFO_URL =
            "https://openidconnect.googleapis.com/v1/userinfo";

    private static final String DEFAULT_REDIRECT_URI =
            "http://localhost:8080/oditji/member/google/callback";

    private static final int MAX_NICKNAME_LENGTH = 30;

    private final MemberDAO memberDAO;
    private final MemberSocialDAO memberSocialDAO;
    private final Environment environment;
    private final RestTemplate restTemplate;

    public GoogleLoginServiceImpl(
            MemberDAO memberDAO,
            MemberSocialDAO memberSocialDAO,
            Environment environment) {

        this.memberDAO = memberDAO;
        this.memberSocialDAO = memberSocialDAO;
        this.environment = environment;
        this.restTemplate = new RestTemplate();
    }

    /**
     * Google 로그인 동의 화면 URL을 생성합니다.
     *
     * scope에 email을 넣지 않으므로 ODITJI가 요청하는 개인정보는
     * Google 표시 이름과 프로필 이미지뿐입니다.
     */
    @Override
    public String getGoogleLoginUrl(String state) {

        String clientId = getGoogleClientId();
        String redirectUri = getGoogleRedirectUri();

        if (state == null || state.isBlank()) {
            throw new IllegalStateException(
                    "Google 로그인 상태값 생성에 실패했습니다.");
        }

        return UriComponentsBuilder
                .fromUriString(GOOGLE_AUTHORIZATION_URL)
                .queryParam("client_id", clientId)
                .queryParam("redirect_uri", redirectUri)
                .queryParam("response_type", "code")
                .queryParam("scope", "openid profile")
                .queryParam("state", state)
                .queryParam("prompt", "select_account")
                .build()
                .encode()
                .toUriString();
    }

    /**
     * 인증 코드를 토큰으로 교환하고 Google 프로필을 조회한 뒤
     * 기존 회원이면 조회 결과를 반환하고 신규 회원이면 DB에 저장합니다.
     */
    @Override
    @Transactional
    public GoogleLoginResultVO googleLogin(String code) {

        if (code == null || code.isBlank()) {
            throw new IllegalStateException(
                    "Google 인증 코드가 전달되지 않았습니다.");
        }

        GoogleTokenVO token = requestToken(code);
        GoogleUserInfoVO googleUser =
                requestUserInfo(token.getAccessToken());

        String providerUserId = googleUser.getSub();

        if (providerUserId == null || providerUserId.isBlank()) {
            throw new IllegalStateException(
                    "Google 사용자 고유 식별자를 확인할 수 없습니다.");
        }

        MemberSocialJoinVO existingMember =
                memberSocialDAO.selectMemberBySocial(
                        PROVIDER_GOOGLE,
                        providerUserId);

        if (existingMember != null) {
            return new GoogleLoginResultVO(
                    false,
                    existingMember);
        }

        MemberVO newMember = createGoogleMember(
                googleUser,
                providerUserId);

        /*
         * 현재 memberMapper의 insertKakaoMember SQL은 이름과 달리
         * provider에 종속되지 않은 공통 SNS 회원 INSERT입니다.
         * 기존 DB/Mapper를 불필요하게 변경하지 않기 위해 그대로 재사용합니다.
         */
        memberDAO.insertKakaoMember(newMember);

        if (newMember.getMemberNo() == null) {
            throw new IllegalStateException(
                    "Google 회원번호 생성에 실패했습니다.");
        }

        MemberSocialVO memberSocialVO = new MemberSocialVO();
        memberSocialVO.setMemberNo(newMember.getMemberNo());
        memberSocialVO.setProvider(PROVIDER_GOOGLE);
        memberSocialVO.setProviderUserId(providerUserId);

        memberSocialDAO.insertMemberSocial(memberSocialVO);

        MemberSocialJoinVO joinedMember =
                memberSocialDAO.selectMemberBySocial(
                        PROVIDER_GOOGLE,
                        providerUserId);

        if (joinedMember == null) {
            throw new IllegalStateException(
                    "Google 회원정보 저장 후 조회에 실패했습니다.");
        }

        return new GoogleLoginResultVO(
                true,
                joinedMember);
    }

    /**
     * Google 토큰 엔드포인트에 인증 코드를 전달해 access_token을 발급받습니다.
     */
    private GoogleTokenVO requestToken(String code) {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(
                MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params =
                new LinkedMultiValueMap<String, String>();

        params.add("grant_type", "authorization_code");
        params.add("code", code);
        params.add("client_id", getGoogleClientId());
        params.add("client_secret", getGoogleClientSecret());
        params.add("redirect_uri", getGoogleRedirectUri());

        HttpEntity<MultiValueMap<String, String>> request =
                new HttpEntity<MultiValueMap<String, String>>(
                        params,
                        headers);

        try {

            ResponseEntity<GoogleTokenVO> response =
                    restTemplate.exchange(
                            GOOGLE_TOKEN_URL,
                            HttpMethod.POST,
                            request,
                            GoogleTokenVO.class);

            GoogleTokenVO token = response.getBody();

            if (token == null
                    || token.getAccessToken() == null
                    || token.getAccessToken().isBlank()) {

                throw new IllegalStateException(
                        "Google access_token 발급에 실패했습니다.");
            }

            return token;

        } catch (RestClientResponseException e) {

            throw new IllegalStateException(
                    "Google 토큰 발급에 실패했습니다. "
                    + "Client ID, Client Secret, 콜백 주소를 확인해주세요.",
                    e);
        }
    }

    /**
     * access_token으로 OpenID Connect UserInfo를 조회합니다.
     */
    private GoogleUserInfoVO requestUserInfo(
            String accessToken) {

        if (accessToken == null || accessToken.isBlank()) {
            throw new IllegalStateException(
                    "Google 사용자정보 조회용 토큰이 없습니다.");
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        RequestEntity<Void> request =
                RequestEntity
                        .get(URI.create(GOOGLE_USER_INFO_URL))
                        .headers(headers)
                        .build();

        try {

            ResponseEntity<GoogleUserInfoVO> response =
                    restTemplate.exchange(
                            request,
                            GoogleUserInfoVO.class);

            GoogleUserInfoVO googleUser = response.getBody();

            if (googleUser == null
                    || googleUser.getSub() == null
                    || googleUser.getSub().isBlank()) {

                throw new IllegalStateException(
                        "Google 사용자정보 조회에 실패했습니다.");
            }

            return googleUser;

        } catch (RestClientResponseException e) {

            throw new IllegalStateException(
                    "Google 프로필 정보 조회에 실패했습니다.",
                    e);
        }
    }

    /**
     * Google 프로필을 ODITJI MEMBER 구조로 변환합니다.
     *
     * - MEMBER_ID: 사용자가 입력하는 아이디가 아니므로 Google sub의 해시 사용
     * - MEMBER_NAME: 팀 정책에 따라 NULL
     * - NICKNAME: Google name을 사용하며 중복이면 짧은 식별값 추가
     * - EMAIL: 요청하지 않으므로 NULL
     * - PROFILE_IMAGE: Google picture URL
     */
    private MemberVO createGoogleMember(
            GoogleUserInfoVO googleUser,
            String providerUserId) {

        MemberVO memberVO = new MemberVO();

        memberVO.setMemberId(
                "google_" + sha256(providerUserId).substring(0, 32));

        memberVO.setMemberPw(null);
        memberVO.setMemberName(null);

        memberVO.setNickname(
                createAvailableNickname(
                        googleUser.getNickname(),
                        providerUserId));

        memberVO.setEmail(null);
        memberVO.setPhone(null);

        memberVO.setProfileImage(
                googleUser.getProfileImageUrl());

        memberVO.setRole("USER");
        memberVO.setStatus("ACTIVE");
        memberVO.setAdultVerified("N");

        return memberVO;
    }

    /**
     * MEMBER.NICKNAME UNIQUE 제약조건을 지키기 위해
     * Google 표시 이름이 이미 사용 중일 때만 식별값을 덧붙입니다.
     */
    private String createAvailableNickname(
            String googleName,
            String providerUserId) {

        String baseNickname = normalizeNickname(googleName);

        if (memberDAO.countByNickname(baseNickname) == 0) {
            return baseNickname;
        }

        String shortId = sha256(providerUserId).substring(0, 6);

        String candidate = appendNicknameSuffix(
                baseNickname,
                "_" + shortId);

        if (memberDAO.countByNickname(candidate) == 0) {
            return candidate;
        }

        for (int index = 2; index <= 99; index++) {

            candidate = appendNicknameSuffix(
                    baseNickname,
                    "_" + shortId + "_" + index);

            if (memberDAO.countByNickname(candidate) == 0) {
                return candidate;
            }
        }

        return "구글회원_"
                + sha256(providerUserId).substring(0, 12);
    }

    private String normalizeNickname(String value) {

        String nickname = value;

        if (nickname == null || nickname.isBlank()) {
            nickname = "구글회원";
        }

        nickname = nickname.trim()
                .replaceAll("\\s+", " ");

        return limitLength(
                nickname,
                MAX_NICKNAME_LENGTH);
    }

    private String appendNicknameSuffix(
            String baseNickname,
            String suffix) {

        int baseMaxLength =
                MAX_NICKNAME_LENGTH - suffix.length();

        String safeBase = limitLength(
                baseNickname,
                Math.max(baseMaxLength, 1));

        return safeBase + suffix;
    }

    private String limitLength(
            String value,
            int maxLength) {

        if (value == null) {
            return null;
        }

        if (value.length() <= maxLength) {
            return value;
        }

        return value.substring(0, maxLength);
    }

    /**
     * Google sub 원문을 MEMBER_ID에 직접 노출하지 않기 위해
     * SHA-256 해시값을 내부 아이디 생성에 사용합니다.
     */
    private String sha256(String value) {

        try {

            MessageDigest digest =
                    MessageDigest.getInstance("SHA-256");

            byte[] hash = digest.digest(
                    value.getBytes(StandardCharsets.UTF_8));

            StringBuilder builder =
                    new StringBuilder(hash.length * 2);

            for (byte oneByte : hash) {
                builder.append(
                        String.format("%02x", oneByte & 0xff));
            }

            return builder.toString();

        } catch (NoSuchAlgorithmException e) {

            throw new IllegalStateException(
                    "SHA-256 알고리즘을 사용할 수 없습니다.",
                    e);
        }
    }

    /**
     * 사용자가 이미 application.properties에 spring.security.oauth2 형식으로
     * 키를 넣었을 가능성까지 고려해 두 가지 속성명을 모두 지원합니다.
     */
    private String getGoogleClientId() {

        String clientId = firstNonBlank(
                environment.getProperty("google.client-id"),
                environment.getProperty(
                        "spring.security.oauth2.client.registration.google.client-id"));

        if (clientId == null) {
            throw new IllegalStateException(
                    "Google Client ID가 설정되지 않았습니다.");
        }

        return clientId;
    }

    private String getGoogleClientSecret() {

        String clientSecret = firstNonBlank(
                environment.getProperty("google.client-secret"),
                environment.getProperty(
                        "spring.security.oauth2.client.registration.google.client-secret"));

        if (clientSecret == null) {
            throw new IllegalStateException(
                    "Google Client Secret이 설정되지 않았습니다.");
        }

        return clientSecret;
    }

    private String getGoogleRedirectUri() {

        String redirectUri = firstNonBlank(
                environment.getProperty("google.redirect-uri"),
                DEFAULT_REDIRECT_URI);

        return redirectUri;
    }

    private String firstNonBlank(String... values) {

        if (values == null) {
            return null;
        }

        for (String value : values) {

            if (value != null && !value.isBlank()) {
                return value.trim();
            }
        }

        return null;
    }
}
