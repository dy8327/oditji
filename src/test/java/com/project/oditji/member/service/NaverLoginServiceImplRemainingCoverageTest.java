package com.project.oditji.member.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withNoContent;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import com.project.oditji.member.dao.MemberDAO;
import com.project.oditji.member.dao.MemberSocialDAO;
import com.project.oditji.member.vo.MemberSocialJoinVO;
import com.project.oditji.member.vo.SocialLoginResultVO;

/**
 * 네이버 로그인 서비스의 null 토큰 본문, 오류 상세와 resultcode null 조건을 보완합니다.
 */
@ExtendWith(MockitoExtension.class)
class NaverLoginServiceImplRemainingCoverageTest {

    @Mock
    private MemberDAO memberDAO;

    @Mock
    private MemberSocialDAO memberSocialDAO;

    private NaverLoginServiceImpl service;
    private MockRestServiceServer server;

    @BeforeEach
    void setUp() {
        service = new NaverLoginServiceImpl(
                memberDAO,
                memberSocialDAO);

        ReflectionTestUtils.setField(
                service,
                "naverClientId",
                "naver-client");
        ReflectionTestUtils.setField(
                service,
                "naverClientSecret",
                "naver-secret");
        ReflectionTestUtils.setField(
                service,
                "naverRedirectUri",
                "http://localhost/callback");

        resetServer();
    }

    @Test
    void tokenResponseWithNoBodyShouldUseNullTokenBranch() {
        server.expect(
                requestTo(
                        "https://nid.naver.com/oauth2.0/token"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withNoContent());

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> service.naverLogin(
                        "code",
                        "state"));

        assertTrue(exception.getMessage().contains("접근 토큰"));
        server.verify();
    }

    @Test
    void tokenErrorDescriptionShouldStillReturnSafeDefaultMessage() {
        server.expect(
                requestTo(
                        "https://nid.naver.com/oauth2.0/token"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess(
                        "{\"error\":\"invalid_request\","
                        + "\"error_description\":\"detail\"}",
                        MediaType.APPLICATION_JSON));

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> service.naverLogin(
                        "code",
                        "state"));

        assertEquals(
                "네이버 접근 토큰 발급에 실패했습니다.",
                exception.getMessage());
        server.verify();
    }

    @Test
    void profileWithNullResultCodeAndValidIdShouldBeAccepted() {
        expectToken();

        server.expect(
                requestTo(
                        "https://openapi.naver.com/v1/nid/me"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(
                        "{\"response\":{"
                        + "\"id\":\"valid-id\","
                        + "\"nickname\":\"닉네임\"}}",
                        MediaType.APPLICATION_JSON));

        MemberSocialJoinVO existing =
                new MemberSocialJoinVO();
        existing.setMemberNo(10L);
        existing.setStatus("ACTIVE");

        when(memberSocialDAO.selectMemberBySocial(
                "NAVER",
                "valid-id"))
                .thenReturn(existing);

        SocialLoginResultVO result =
                service.naverLogin(
                        "code",
                        "state");

        assertFalse(result.isNewMember());
        assertEquals(existing, result.getMember());
        server.verify();
    }

    @Test
    void blankProviderUserIdShouldFailBeforeMemberLookup() {
        expectToken();

        server.expect(
                requestTo(
                        "https://openapi.naver.com/v1/nid/me"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(
                        "{\"resultcode\":\"00\","
                        + "\"response\":{\"id\":\"   \"}}",
                        MediaType.APPLICATION_JSON));

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> service.naverLogin(
                        "code",
                        "state"));

        assertTrue(exception.getMessage().contains("사용자 정보 조회"));
        server.verify();
    }

    @Test
    void activeOrUnknownMemberStatusShouldReturnNormallyFromValidator() {
        MemberSocialJoinVO active =
                new MemberSocialJoinVO();
        active.setStatus("ACTIVE");

        MemberSocialJoinVO unknown =
                new MemberSocialJoinVO();
        unknown.setStatus(null);

        assertDoesNotThrow(
                () -> ReflectionTestUtils.invokeMethod(
                        service,
                        "validateMemberStatus",
                        active));

        assertDoesNotThrow(
                () -> ReflectionTestUtils.invokeMethod(
                        service,
                        "validateMemberStatus",
                        unknown));
    }

    @Test
    void errorMessageHelperShouldIgnoreNullBlankAndDetailedDescriptions() {
        assertEquals(
                "기본",
                ReflectionTestUtils.invokeMethod(
                        service,
                        "buildNaverErrorMessage",
                        "기본",
                        null));

        assertEquals(
                "기본",
                ReflectionTestUtils.invokeMethod(
                        service,
                        "buildNaverErrorMessage",
                        "기본",
                        "   "));

        assertEquals(
                "기본",
                ReflectionTestUtils.invokeMethod(
                        service,
                        "buildNaverErrorMessage",
                        "기본",
                        "상세 오류"));
    }

    private void expectToken() {
        server.expect(
                requestTo(
                        "https://nid.naver.com/oauth2.0/token"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess(
                        "{\"access_token\":\"token\"}",
                        MediaType.APPLICATION_JSON));
    }

    private void resetServer() {
        RestTemplate restTemplate =
                (RestTemplate) ReflectionTestUtils.getField(
                        service,
                        "restTemplate");

        server = MockRestServiceServer.createServer(
                restTemplate);
    }
}
