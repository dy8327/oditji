package com.project.oditji.member.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import com.project.oditji.member.dao.MemberDAO;
import com.project.oditji.member.dao.MemberSocialDAO;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

/** 카카오 OAuth 오류 처리의 비활성 로그 가드 분기를 보완합니다. */
class KakaoLoginServiceImplLoggingGuardCoverageTest {

    private KakaoLoginServiceImpl service;
    private MockRestServiceServer server;

    @BeforeEach
    void setUp() {
        service = new KakaoLoginServiceImpl(
                mock(MemberDAO.class),
                mock(MemberSocialDAO.class));

        ReflectionTestUtils.setField(service, "kakaoClientId", "client-id");
        ReflectionTestUtils.setField(service, "kakaoRedirectUri", "http://localhost/callback");
        ReflectionTestUtils.setField(service, "kakaoClientSecret", "client-secret");

        resetServer();
    }

    @Test
    void tokenApiFailureShouldCoverDisabledErrorLogGuard() {
        server.expect(requestTo("https://kauth.kakao.com/oauth/token"))
                .andRespond(withStatus(HttpStatus.BAD_REQUEST)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("{\"error\":\"invalid_grant\"}"));

        withErrorLoggingDisabled(() -> assertThrows(
                IllegalStateException.class,
                () -> service.kakaoLogin("bad-code")));

        server.verify();
    }

    @Test
    void userInfoApiFailureShouldCoverDisabledErrorLogGuard() {
        server.expect(requestTo("https://kauth.kakao.com/oauth/token"))
                .andRespond(withSuccess(
                        "{\"access_token\":\"access-token\"}",
                        MediaType.APPLICATION_JSON));
        server.expect(requestTo("https://kapi.kakao.com/v2/user/me"))
                .andRespond(withStatus(HttpStatus.UNAUTHORIZED)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("{\"msg\":\"unauthorized\"}"));

        withErrorLoggingDisabled(() -> assertThrows(
                IllegalStateException.class,
                () -> service.kakaoLogin("bad-user")));

        server.verify();
    }

    private void resetServer() {
        RestTemplate restTemplate = (RestTemplate) ReflectionTestUtils.getField(
                service,
                "restTemplate");
        server = MockRestServiceServer.createServer(restTemplate);
    }

    private void withErrorLoggingDisabled(Runnable assertion) {
        Logger logger = (Logger) LoggerFactory.getLogger(KakaoLoginServiceImpl.class);
        Level originalLevel = logger.getLevel();

        try {
            logger.setLevel(Level.OFF);
            assertion.run();
        } finally {
            logger.setLevel(originalLevel);
        }
    }
}
