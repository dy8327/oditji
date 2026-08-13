package com.project.oditji.member.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import com.project.oditji.member.dao.MemberDAO;
import com.project.oditji.member.dao.MemberSocialDAO;

/**
 * 네이버 토큰/사용자정보 OR 조건의 null과 blank 피연산자를 각각 보완합니다.
 */
class NaverLoginServiceImplMoreConditionCoverageTest {

    private NaverLoginServiceImpl service;
    private MockRestServiceServer server;

    @BeforeEach
    void setUp() {
        service = new NaverLoginServiceImpl(
                mock(MemberDAO.class),
                mock(MemberSocialDAO.class));

        ReflectionTestUtils.setField(
                service,
                "naverClientId",
                "client");
        ReflectionTestUtils.setField(
                service,
                "naverClientSecret",
                "secret");
        ReflectionTestUtils.setField(
                service,
                "naverRedirectUri",
                "http://localhost/callback");

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
    void tokenWithNullAccessTokenShouldFail() {
        tokenResponse(
                "{\"error_description\":\"detail\"}");

        IllegalStateException exception =
                assertThrows(
                        IllegalStateException.class,
                        () -> ReflectionTestUtils.invokeMethod(
                                service,
                                "requestToken",
                                "code",
                                "state"));

        assertTrue(
                exception.getMessage()
                        .contains("접근 토큰"));
        server.verify();
    }

    @Test
    void tokenWithBlankAccessTokenShouldFail() {
        tokenResponse(
                "{\"access_token\":\"   \","
                        + "\"error_description\":\"detail\"}");

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
    void userInfoWithNullAndBlankProviderIdsShouldFailSeparately() {
        profileResponse(
                "{\"resultcode\":\"00\","
                        + "\"response\":{\"nickname\":\"nick\"}}");

        assertThrows(
                IllegalStateException.class,
                () -> ReflectionTestUtils.invokeMethod(
                        service,
                        "requestUserInfo",
                        "token"));

        server.verify();

        resetServer();

        profileResponse(
                "{\"resultcode\":\"00\","
                        + "\"response\":{\"id\":\"   \"}}");

        assertThrows(
                IllegalStateException.class,
                () -> ReflectionTestUtils.invokeMethod(
                        service,
                        "requestUserInfo",
                        "token"));

        server.verify();
    }

    private void tokenResponse(String json) {
        server.expect(
                requestTo(
                        "https://nid.naver.com/oauth2.0/token"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(
                        withSuccess(
                                json,
                                MediaType.APPLICATION_JSON));
    }

    private void profileResponse(String json) {
        server.expect(
                requestTo(
                        "https://openapi.naver.com/v1/nid/me"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(
                        withSuccess(
                                json,
                                MediaType.APPLICATION_JSON));
    }

    private void resetServer() {
        RestTemplate restTemplate =
                (RestTemplate)
                        ReflectionTestUtils.getField(
                                service,
                                "restTemplate");

        server =
                MockRestServiceServer.createServer(
                        restTemplate);
    }
}
