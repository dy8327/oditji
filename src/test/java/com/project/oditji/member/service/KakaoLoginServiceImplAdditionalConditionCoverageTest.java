package com.project.oditji.member.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withNoContent;
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
import com.project.oditji.member.vo.KakaoTokenVO;

/**
 * 카카오 로그인 HTTP helper의 client-secret 단축평가와 null 응답 body 분기를 보완합니다.
 */
class KakaoLoginServiceImplAdditionalConditionCoverageTest {

    private KakaoLoginServiceImpl service;
    private MockRestServiceServer server;

    @BeforeEach
    void setUp() {
        service = new KakaoLoginServiceImpl(
                mock(MemberDAO.class),
                mock(MemberSocialDAO.class));

        ReflectionTestUtils.setField(
                service,
                "kakaoClientId",
                "client-id");
        ReflectionTestUtils.setField(
                service,
                "kakaoRedirectUri",
                "http://localhost/callback");

        resetServer();
    }

    @Test
    void nullClientSecretShouldSkipSecretParameterAndStillReadToken() {
        ReflectionTestUtils.setField(
                service,
                "kakaoClientSecret",
                null);

        expectToken("null-secret-token");

        KakaoTokenVO token = ReflectionTestUtils.invokeMethod(
                service,
                "requestToken",
                "code");

        assertEquals(
                "null-secret-token",
                token.getAccessToken());

        server.verify();
    }

    @Test
    void blankClientSecretShouldCoverSecondAndConditionAsFalse() {
        ReflectionTestUtils.setField(
                service,
                "kakaoClientSecret",
                "   ");

        expectToken("blank-secret-token");

        KakaoTokenVO token = ReflectionTestUtils.invokeMethod(
                service,
                "requestToken",
                "code");

        assertEquals(
                "blank-secret-token",
                token.getAccessToken());

        server.verify();
    }

    @Test
    void nullTokenResponseBodyShouldFailBeforeAccessTokenRead() {
        server.expect(
                requestTo(
                        "https://kauth.kakao.com/oauth/token"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withNoContent());

        IllegalStateException exception =
                assertThrows(
                        IllegalStateException.class,
                        () -> ReflectionTestUtils.invokeMethod(
                                service,
                                "requestToken",
                                "code"));

        assertTrue(
                exception.getMessage()
                        .contains("access_token"));

        server.verify();
    }

    @Test
    void nullUserInfoResponseBodyShouldFailBeforeIdRead() {
        server.expect(
                requestTo(
                        "https://kapi.kakao.com/v2/user/me"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withNoContent());

        IllegalStateException exception =
                assertThrows(
                        IllegalStateException.class,
                        () -> ReflectionTestUtils.invokeMethod(
                                service,
                                "requestUserInfo",
                                "access-token"));

        assertTrue(
                exception.getMessage()
                        .contains("사용자 정보 조회"));

        server.verify();
    }

    private void expectToken(String token) {
        server.expect(
                requestTo(
                        "https://kauth.kakao.com/oauth/token"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(
                        withSuccess(
                                "{\"access_token\":\""
                                        + token
                                        + "\"}",
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
