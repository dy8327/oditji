package com.project.oditji.member.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withNoContent;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import com.project.oditji.member.dao.MemberDAO;
import com.project.oditji.member.dao.MemberSocialDAO;

/**
 * Google 로그인 서비스의 null HTTP 본문, Client ID fallback과 닉네임 반복 충돌 성공 분기를 보완합니다.
 */
class GoogleLoginServiceImplAdditionalRemainingCoverageTest {

    private MemberDAO memberDAO;
    private Environment environment;
    private GoogleLoginServiceImpl service;
    private MockRestServiceServer server;

    @BeforeEach
    void setUp() {
        memberDAO = mock(MemberDAO.class);
        environment = mock(Environment.class);

        service =
                new GoogleLoginServiceImpl(
                        memberDAO,
                        mock(MemberSocialDAO.class),
                        environment);

        RestTemplate restTemplate =
                (RestTemplate)
                        ReflectionTestUtils.getField(
                                service,
                                "restTemplate");

        server =
                MockRestServiceServer.createServer(
                        restTemplate);
    }

    @Test
    void loginUrlShouldRejectNullAndBlankStateAfterConfigurationResolution() {
        stubConfig();

        assertThrows(
                IllegalStateException.class,
                () -> service.getGoogleLoginUrl(
                        null));

        assertThrows(
                IllegalStateException.class,
                () -> service.getGoogleLoginUrl(
                        "   "));
    }

    @Test
    void clientIdShouldUseSpringFallbackAndRejectMissingConfiguration() {
        stubProperties(
                Map.of(
                        "spring.security.oauth2.client.registration.google.client-id",
                        " fallback-client "));

        String clientId =
                ReflectionTestUtils.invokeMethod(
                        service,
                        "getGoogleClientId");

        assertEquals(
                "fallback-client",
                clientId);

        stubProperties(Map.of());

        assertThrows(
                IllegalStateException.class,
                () -> ReflectionTestUtils.invokeMethod(
                        service,
                        "getGoogleClientId"));
    }

    @Test
    void nullTokenHttpBodyShouldUseTokenNullBranch() {
        stubConfig();

        server.expect(
                requestTo(
                        "https://oauth2.googleapis.com/token"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withNoContent());

        IllegalStateException exception =
                assertThrows(
                        IllegalStateException.class,
                        () -> service.googleLogin(
                                "code"));

        assertTrue(
                exception.getMessage()
                        .contains("access_token"));

        server.verify();
    }

    @Test
    void nullUserInfoHttpBodyShouldUseUserNullBranch() {
        stubConfig();

        server.expect(
                requestTo(
                        "https://oauth2.googleapis.com/token"))
                .andRespond(
                        withSuccess(
                                "{\"access_token\":\"token\"}",
                                MediaType.APPLICATION_JSON));

        server.expect(
                requestTo(
                        "https://openidconnect.googleapis.com/v1/userinfo"))
                .andRespond(withNoContent());

        IllegalStateException exception =
                assertThrows(
                        IllegalStateException.class,
                        () -> service.googleLogin(
                                "code"));

        assertTrue(
                exception.getMessage()
                        .contains("사용자정보 조회"));

        server.verify();
    }

    @Test
    void nicknameCollisionLoopShouldReturnFirstAvailableIndexedCandidate() {
        when(memberDAO.countByNickname(anyString()))
                .thenReturn(
                        1,
                        1,
                        0);

        String nickname =
                ReflectionTestUtils.invokeMethod(
                        service,
                        "createAvailableNickname",
                        "Google User",
                        "provider-id");

        assertTrue(
                nickname.endsWith("_2"));
        assertTrue(
                nickname.length() <= 30);
    }

    private void stubConfig() {
        stubProperties(
                Map.of(
                        "google.client-id",
                        "client-id",
                        "google.client-secret",
                        "client-secret",
                        "google.redirect-uri",
                        "http://localhost/callback"));
    }

    private void stubProperties(
            Map<String, String> properties) {

        when(environment.getProperty(anyString()))
                .thenAnswer(invocation ->
                        properties.get(
                                invocation.getArgument(0)));
    }
}
