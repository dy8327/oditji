package com.project.oditji.member.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import com.project.oditji.member.dao.MemberDAO;
import com.project.oditji.member.dao.MemberSocialDAO;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

/**
 * Naver API 오류 처리의 ERROR/WARN 로그 가드 false 분기를 검증합니다.
 */
class NaverLoginServiceImplLoggingGuardCoverageTest {

    private NaverLoginServiceImpl service;
    private MockRestServiceServer server;
    private Logger serviceLogger;
    private Level previousLevel;

    @BeforeEach
    void setUp() {
        service = new NaverLoginServiceImpl(
                mock(MemberDAO.class),
                mock(MemberSocialDAO.class));

        ReflectionTestUtils.setField(service, "naverClientId", "client");
        ReflectionTestUtils.setField(service, "naverClientSecret", "secret");
        ReflectionTestUtils.setField(service, "naverRedirectUri", "http://localhost/callback");

        RestTemplate restTemplate = (RestTemplate) ReflectionTestUtils.getField(service, "restTemplate");
        server = MockRestServiceServer.createServer(restTemplate);

        serviceLogger = (Logger) LoggerFactory.getLogger(NaverLoginServiceImpl.class);
        previousLevel = serviceLogger.getLevel();
        serviceLogger.setLevel(Level.OFF);
    }

    @AfterEach
    void tearDown() {
        serviceLogger.setLevel(previousLevel);
    }

    @Test
    void tokenHttpFailureShouldSkipErrorLogWhenDisabled() {
        server.expect(requestTo("https://nid.naver.com/oauth2.0/token"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withStatus(HttpStatus.BAD_REQUEST));

        assertThrows(
                IllegalStateException.class,
                () -> ReflectionTestUtils.invokeMethod(
                        service,
                        "requestToken",
                        "code",
                        "state"));

        server.verify();
    }

    @Test
    void profileHttpFailureShouldSkipErrorLogWhenDisabled() {
        server.expect(requestTo("https://openapi.naver.com/v1/nid/me"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.BAD_REQUEST));

        assertThrows(
                IllegalStateException.class,
                () -> ReflectionTestUtils.invokeMethod(
                        service,
                        "requestUserInfo",
                        "access-token"));

        server.verify();
    }

    @Test
    void providerDetailShouldReachWarnGuardFalseBranch() {
        String message = ReflectionTestUtils.invokeMethod(
                service,
                "buildNaverErrorMessage",
                "기본 오류",
                "provider detail");

        assertEquals("기본 오류", message);
    }
}
