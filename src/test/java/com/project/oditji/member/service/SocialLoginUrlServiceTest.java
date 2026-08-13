package com.project.oditji.member.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;
import org.springframework.test.util.ReflectionTestUtils;

import com.project.oditji.member.dao.MemberDAO;
import com.project.oditji.member.dao.MemberSocialDAO;

/** Google·Kakao·Naver 로그인 URL 생성과 인증 입력 검증을 확인합니다. */
@ExtendWith(MockitoExtension.class)
class SocialLoginUrlServiceTest {

    @Mock
    private MemberDAO memberDAO;

    @Mock
    private MemberSocialDAO memberSocialDAO;

    @Mock
    private Environment environment;

    private GoogleLoginServiceImpl googleService;
    private KakaoLoginServiceImpl kakaoService;
    private NaverLoginServiceImpl naverService;

    @BeforeEach
    void setUp() {
        googleService = new GoogleLoginServiceImpl(
                memberDAO,
                memberSocialDAO,
                environment);
        kakaoService = new KakaoLoginServiceImpl(
                memberDAO,
                memberSocialDAO);
        naverService = new NaverLoginServiceImpl(
                memberDAO,
                memberSocialDAO);

        ReflectionTestUtils.setField(
                kakaoService,
                "kakaoClientId",
                "kakao-client");
        ReflectionTestUtils.setField(
                kakaoService,
                "kakaoRedirectUri",
                "http://localhost/kakao/callback");
        ReflectionTestUtils.setField(
                kakaoService,
                "kakaoClientSecret",
                "secret");

        ReflectionTestUtils.setField(
                naverService,
                "naverClientId",
                "naver-client");
        ReflectionTestUtils.setField(
                naverService,
                "naverClientSecret",
                "secret");
        ReflectionTestUtils.setField(
                naverService,
                "naverRedirectUri",
                "http://localhost/naver/callback");
    }

    @Test
    void googleLoginUrlShouldUseConfiguredOrFallbackProperties() {
        when(environment.getProperty("google.client-id"))
                .thenReturn(" google-client ");
        /* 구현체가 OAuth2 표준 속성도 함께 조회하므로 null 반환을 명시합니다. */
        when(environment.getProperty(
                "spring.security.oauth2.client.registration.google.client-id"))
                .thenReturn(null);
        when(environment.getProperty("google.redirect-uri"))
                .thenReturn("http://localhost/google/callback");

        String url = googleService.getGoogleLoginUrl("state-value");

        assertTrue(url.startsWith(
                "https://accounts.google.com/o/oauth2/v2/auth"));
        assertTrue(url.contains("client_id=google-client"));
        assertTrue(url.contains("state=state-value"));
        assertTrue(url.contains("scope=openid%20profile"));

        assertThrows(
                IllegalStateException.class,
                () -> googleService.getGoogleLoginUrl(" "));
        assertThrows(
                IllegalStateException.class,
                () -> googleService.googleLogin(null));
    }

    @Test
    void googleLoginUrlShouldUseSpringRegistrationPropertyAndDefaultRedirect() {
        when(environment.getProperty("google.client-id"))
                .thenReturn(" ");
        when(environment.getProperty(
                "spring.security.oauth2.client.registration.google.client-id"))
                .thenReturn("spring-google-client");
        when(environment.getProperty("google.redirect-uri"))
                .thenReturn(null);

        String url = googleService.getGoogleLoginUrl("state");

        assertTrue(url.contains("client_id=spring-google-client"));
        String decodedUrl = URLDecoder.decode(
                url,
                StandardCharsets.UTF_8);
        assertTrue(decodedUrl.contains(
                "redirect_uri=http://localhost:8080/oditji/member/google/callback"));
    }

    @Test
    void googleLoginUrlShouldFailWhenClientIdIsMissing() {
        when(environment.getProperty("google.client-id")).thenReturn(null);
        when(environment.getProperty(
                "spring.security.oauth2.client.registration.google.client-id"))
                .thenReturn(null);

        assertEquals(
                "Google Client ID가 설정되지 않았습니다.",
                assertThrows(
                        IllegalStateException.class,
                        () -> googleService.getGoogleLoginUrl("state"))
                        .getMessage());
    }

    @Test
    void kakaoLoginUrlShouldContainClientAndRedirectValues() {
        String url = kakaoService.getKakaoLoginUrl();

        assertTrue(url.startsWith(
                "https://kauth.kakao.com/oauth/authorize"));
        assertTrue(url.contains("client_id=kakao-client"));
        assertTrue(url.contains("response_type=code"));
        assertTrue(url.contains("kakao/callback"));
    }

    @Test
    void naverStateAndLoginUrlShouldBeUniqueAndValidateInput() {
        String firstState = naverService.createState();
        String secondState = naverService.createState();

        assertEquals(43, firstState.length());
        assertEquals(43, secondState.length());
        assertNotEquals(firstState, secondState);

        String url = naverService.getNaverLoginUrl(firstState);
        assertTrue(url.startsWith(
                "https://nid.naver.com/oauth2.0/authorize"));
        assertTrue(url.contains("client_id=naver-client"));
        assertTrue(url.contains("state=" + firstState));

        assertThrows(
                IllegalArgumentException.class,
                () -> naverService.getNaverLoginUrl(null));
        assertThrows(
                IllegalArgumentException.class,
                () -> naverService.naverLogin(null, firstState));
        assertThrows(
                IllegalArgumentException.class,
                () -> naverService.naverLogin("code", " "));
    }
}
