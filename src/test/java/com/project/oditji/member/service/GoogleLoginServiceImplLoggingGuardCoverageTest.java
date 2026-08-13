package com.project.oditji.member.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import com.project.oditji.member.dao.MemberDAO;
import com.project.oditji.member.dao.MemberSocialDAO;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

/** Google API 오류 로그 가드와 null/blank 토큰 식별자 조건을 보완합니다. */
class GoogleLoginServiceImplLoggingGuardCoverageTest {

    private Environment environment;
    private GoogleLoginServiceImpl service;
    private MockRestServiceServer server;

    @BeforeEach
    void setUp() {
        environment = mock(Environment.class);
        service = new GoogleLoginServiceImpl(
                mock(MemberDAO.class),
                mock(MemberSocialDAO.class),
                environment);

        RestTemplate restTemplate = (RestTemplate) ReflectionTestUtils.getField(
                service,
                "restTemplate");
        server = MockRestServiceServer.createServer(restTemplate);
    }

    @Test
    void tokenApiFailureShouldCoverDisabledErrorLogGuard() {
        stubConfig();
        server.expect(requestTo("https://oauth2.googleapis.com/token"))
                .andRespond(withStatus(HttpStatus.BAD_REQUEST)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("{\"error\":\"invalid_grant\"}"));

        withErrorLoggingDisabled(() -> assertThrows(
                IllegalStateException.class,
                () -> service.googleLogin("bad-code")));

        server.verify();
    }

    @Test
    void userInfoApiFailureShouldCoverDisabledErrorLogGuard() {
        stubConfig();
        server.expect(requestTo("https://oauth2.googleapis.com/token"))
                .andRespond(withSuccess(
                        "{\"access_token\":\"token\"}",
                        MediaType.APPLICATION_JSON));
        server.expect(requestTo("https://openidconnect.googleapis.com/v1/userinfo"))
                .andRespond(withStatus(HttpStatus.UNAUTHORIZED)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("{\"error\":\"unauthorized\"}"));

        withErrorLoggingDisabled(() -> assertThrows(
                IllegalStateException.class,
                () -> service.googleLogin("bad-user")));

        server.verify();
    }

    @Test
    void tokenValidationShouldCoverNullAndBlankAccessTokenOperands() {
        stubConfig();
        server.expect(requestTo("https://oauth2.googleapis.com/token"))
                .andRespond(withSuccess(
                        "{\"access_token\":null}",
                        MediaType.APPLICATION_JSON));

        assertThrows(
                IllegalStateException.class,
                () -> ReflectionTestUtils.invokeMethod(service, "requestToken", "null-token"));
        server.verify();

        resetServer();
        server.expect(requestTo("https://oauth2.googleapis.com/token"))
                .andRespond(withSuccess(
                        "{\"access_token\":\"   \"}",
                        MediaType.APPLICATION_JSON));

        assertThrows(
                IllegalStateException.class,
                () -> ReflectionTestUtils.invokeMethod(service, "requestToken", "blank-token"));
        server.verify();
    }

    @Test
    void userInfoValidationShouldCoverNullAndBlankSubOperands() {
        server.expect(requestTo("https://openidconnect.googleapis.com/v1/userinfo"))
                .andRespond(withSuccess(
                        "{\"sub\":null}",
                        MediaType.APPLICATION_JSON));

        assertThrows(
                IllegalStateException.class,
                () -> ReflectionTestUtils.invokeMethod(service, "requestUserInfo", "token-a"));
        server.verify();

        resetServer();
        server.expect(requestTo("https://openidconnect.googleapis.com/v1/userinfo"))
                .andRespond(withSuccess(
                        "{\"sub\":\"   \"}",
                        MediaType.APPLICATION_JSON));

        assertThrows(
                IllegalStateException.class,
                () -> ReflectionTestUtils.invokeMethod(service, "requestUserInfo", "token-b"));
        server.verify();
    }

    private void stubConfig() {
        Map<String, String> properties = Map.of(
                "google.client-id", "client-id",
                "google.client-secret", "client-secret",
                "google.redirect-uri", "http://localhost/callback");

        when(environment.getProperty(anyString()))
                .thenAnswer(invocation -> properties.get(invocation.getArgument(0)));
    }

    private void resetServer() {
        RestTemplate restTemplate = (RestTemplate) ReflectionTestUtils.getField(
                service,
                "restTemplate");
        server = MockRestServiceServer.createServer(restTemplate);
    }

    private void withErrorLoggingDisabled(Runnable assertion) {
        Logger logger = (Logger) LoggerFactory.getLogger(GoogleLoginServiceImpl.class);
        Level originalLevel = logger.getLevel();

        try {
            logger.setLevel(Level.OFF);
            assertion.run();
        } finally {
            logger.setLevel(originalLevel);
        }
    }
}
