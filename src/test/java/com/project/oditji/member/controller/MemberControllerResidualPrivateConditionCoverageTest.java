package com.project.oditji.member.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import java.net.URI;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.util.ReflectionTestUtils;

import com.project.oditji.business.service.BusinessService;
import com.project.oditji.business.service.NtsBusinessService;
import com.project.oditji.favorite.service.FavoriteService;
import com.project.oditji.mail.service.MailService;
import com.project.oditji.member.service.MemberPlatformService;
import com.project.oditji.member.service.MemberService;
import com.project.oditji.order.service.OrderService;
import com.project.oditji.review.service.ReviewService;
import com.project.oditji.subscription.service.SubscriptionCalculatorService;
import com.project.oditji.wish.service.WishService;

/** MemberController의 비밀번호 재설정과 내부 redirect helper의 short-circuit 분기를 보완합니다. */
class MemberControllerResidualPrivateConditionCoverageTest {

    private MemberController controller;

    @BeforeEach
    void setUp() {
        controller = new MemberController(
                mock(MemberService.class),
                mock(MemberPlatformService.class),
                mock(BusinessService.class),
                mock(NtsBusinessService.class),
                mock(MailService.class),
                mock(FavoriteService.class),
                mock(WishService.class),
                mock(OrderService.class),
                mock(ReviewService.class),
                mock(SubscriptionCalculatorService.class),
                "uploads/profile",
                "uploads/business");
    }

    @Test
    void passwordResetAvailabilityShouldCoverEveryShortCircuitPosition() {
        MockHttpSession session = new MockHttpSession();
        assertFalse(invokeBoolean("isPwResetAvailable", session));

        session.setAttribute("pwResetMemberNo", 1L);
        assertFalse(invokeBoolean("isPwResetAvailable", session));

        session.setAttribute("pwResetVerified", true);
        assertFalse(invokeBoolean("isPwResetAvailable", session));

        session.setAttribute("pwResetExpiresAt", System.currentTimeMillis() - 1L);
        assertFalse(invokeBoolean("isPwResetAvailable", session));

        session.setAttribute("pwResetExpiresAt", System.currentTimeMillis() + 60_000L);
        assertTrue(invokeBoolean("isPwResetAvailable", session));
    }

    @Test
    void maskEmailShouldCoverInvalidShortAndLongIdentifiers() {
        assertEquals("", invokeString("maskEmail", (Object) null));
        assertEquals("", invokeString("maskEmail", "invalid"));
        assertEquals("a*@mail.com", invokeString("maskEmail", "a@mail.com"));
        assertEquals("a*@mail.com", invokeString("maskEmail", "ab@mail.com"));
        assertEquals("ab**@mail.com", invokeString("maskEmail", "abcd@mail.com"));
    }

    @Test
    void usableRedirectShouldCoverEveryBlockedPathQueryAndRelativePath() {
        assertFalse(invokeBoolean("isUsableRedirectUrl", (Object) null));
        assertFalse(invokeBoolean("isUsableRedirectUrl", "   "));
        assertFalse(invokeBoolean("isUsableRedirectUrl", "/member/login"));
        assertFalse(invokeBoolean("isUsableRedirectUrl", "/member/logout?next=/"));
        assertFalse(invokeBoolean("isUsableRedirectUrl", "/member/join"));
        assertFalse(invokeBoolean("isUsableRedirectUrl", "/member/findId"));
        assertFalse(invokeBoolean("isUsableRedirectUrl", "/member/findPw"));
        assertFalse(invokeBoolean("isUsableRedirectUrl", "relative/path"));
        assertTrue(invokeBoolean("isUsableRedirectUrl", "/content/list?page=1"));
    }

    @Test
    void sameOriginShouldCoverNullHostHostMismatchDefaultPortsAndExplicitPorts() {
        MockHttpServletRequest request = request("example.com", 80, "/oditji");

        assertFalse(invokeBoolean("isSameOrigin", URI.create("/relative"), request));
        assertFalse(invokeBoolean(
                "isSameOrigin", URI.create("http://other.example.com/path"), request));
        assertTrue(invokeBoolean(
                "isSameOrigin", URI.create("http://example.com/path"), request));
        assertFalse(invokeBoolean(
                "isSameOrigin", URI.create("http://example.com:81/path"), request));

        MockHttpServletRequest httpsRequest = request("example.com", 443, "/oditji");
        assertTrue(invokeBoolean(
                "isSameOrigin", URI.create("https://example.com/path"), httpsRequest));
    }

    @Test
    void normalizeRedirectShouldCoverNullBlankExternalContextQueryPrefixAndExceptionBranches() {
        MockHttpServletRequest request = request("localhost", 8080, "/oditji");

        assertNull(invokeString("normalizeRedirectUrl", null, request));
        assertNull(invokeString("normalizeRedirectUrl", "   ", request));
        assertNull(invokeString(
                "normalizeRedirectUrl", "http://external.example/path", request));
        assertEquals("/content/list?page=2", invokeString(
                "normalizeRedirectUrl",
                "http://localhost:8080/oditji/content/list?page=2",
                request));
        assertEquals("/", invokeString("normalizeRedirectUrl", "/oditji", request));
        assertEquals("/member/mypage", invokeString(
                "normalizeRedirectUrl", "member/mypage", request));
        assertEquals("/content/list", invokeString(
                "normalizeRedirectUrl", "/content/list?", request));
        assertNull(invokeString("normalizeRedirectUrl", "http://[", request));
    }

    @Test
    void extractPreviousUrlShouldDelegateRefererNormalization() {
        MockHttpServletRequest request = request("localhost", 8080, "/oditji");
        request.addHeader("Referer", "http://localhost:8080/oditji/recommend?tab=all");

        assertEquals("/recommend?tab=all", invokeString("extractPreviousUrl", request));
    }

    private MockHttpServletRequest request(String serverName, int port, String contextPath) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setServerName(serverName);
        request.setServerPort(port);
        request.setContextPath(contextPath);
        return request;
    }

    private boolean invokeBoolean(String methodName, Object... arguments) {
        Boolean result = ReflectionTestUtils.invokeMethod(controller, methodName, arguments);
        return Boolean.TRUE.equals(result);
    }

    private String invokeString(String methodName, Object... arguments) {
        return ReflectionTestUtils.invokeMethod(controller, methodName, arguments);
    }
}
