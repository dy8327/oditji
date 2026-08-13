package com.project.oditji.member.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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

import jakarta.servlet.http.HttpServletRequest;

/** MemberController의 비밀번호 재설정과 내부 redirect 검증 조건을 세분화합니다. */
class MemberControllerDeepRedirectConditionCoverageTest {

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
                "build/test-profile",
                "build/test-license");
    }

    @Test
    void pwResetAvailabilityShouldCoverEveryShortCircuitPosition() {
        MockHttpSession session = new MockHttpSession();
        assertFalse(invokeBoolean("isPwResetAvailable", session));

        session.setAttribute("pwResetMemberNo", 1L);
        session.setAttribute("pwResetVerified", false);
        assertFalse(invokeBoolean("isPwResetAvailable", session));

        session.setAttribute("pwResetVerified", true);
        assertFalse(invokeBoolean("isPwResetAvailable", session));

        session.setAttribute("pwResetExpiresAt", System.currentTimeMillis() - 1_000L);
        assertFalse(invokeBoolean("isPwResetAvailable", session));

        session.setAttribute("pwResetExpiresAt", System.currentTimeMillis() + 60_000L);
        assertTrue(invokeBoolean("isPwResetAvailable", session));
    }

    @Test
    void normalizeRedirectShouldCoverBlankInvalidOtherOriginRootQueryAndRelativePath() {
        MockHttpServletRequest request = request();

        assertNull(invoke("normalizeRedirectUrl", null, request));
        assertNull(invoke("normalizeRedirectUrl", "   ", request));
        assertNull(invoke("normalizeRedirectUrl", "http://[invalid", request));
        assertNull(invoke("normalizeRedirectUrl", "http://evil.example/oditji/home", request));
        assertEquals("/", invoke("normalizeRedirectUrl", "http://localhost:8080/oditji", request));
        assertEquals("/recommend?tab=all",
                invoke("normalizeRedirectUrl", "http://localhost:8080/oditji/recommend?tab=all", request));
        assertEquals("/recommend", invoke("normalizeRedirectUrl", "recommend", request));
    }

    @Test
    void sameOriginShouldCoverMissingHostHostMismatchDefaultPortsAndExplicitPorts() {
        MockHttpServletRequest http = request();
        assertFalse(invokeBoolean("isSameOrigin", URI.create("/local"), http));
        assertFalse(invokeBoolean("isSameOrigin", URI.create("http://other:8080/a"), http));
        assertTrue(invokeBoolean("isSameOrigin", URI.create("http://localhost:8080/a"), http));
        assertFalse(invokeBoolean("isSameOrigin", URI.create("http://localhost/a"), http));

        MockHttpServletRequest https = new MockHttpServletRequest();
        https.setServerName("localhost");
        https.setServerPort(443);
        assertTrue(invokeBoolean("isSameOrigin", URI.create("https://localhost/a"), https));
    }

    @Test
    void usableRedirectShouldCoverAllBlockedPathsAndValidPath() {
        assertFalse(invokeBoolean("isUsableRedirectUrl", (Object) null));
        assertFalse(invokeBoolean("isUsableRedirectUrl", "   "));
        assertFalse(invokeBoolean("isUsableRedirectUrl", "/member/login?next=/home"));
        assertFalse(invokeBoolean("isUsableRedirectUrl", "/member/logout"));
        assertFalse(invokeBoolean("isUsableRedirectUrl", "/member/join"));
        assertFalse(invokeBoolean("isUsableRedirectUrl", "/member/findId"));
        assertFalse(invokeBoolean("isUsableRedirectUrl", "/member/findPw"));
        assertFalse(invokeBoolean("isUsableRedirectUrl", "home"));
        assertTrue(invokeBoolean("isUsableRedirectUrl", "/home?tab=1"));
    }

    @Test
    void extractPreviousUrlShouldReturnNormalizedInternalReferer() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("Referer")).thenReturn("http://localhost:8080/oditji/content/list");
        when(request.getServerName()).thenReturn("localhost");
        when(request.getServerPort()).thenReturn(8080);
        when(request.getContextPath()).thenReturn("/oditji");

        assertEquals("/content/list", invoke("extractPreviousUrl", request));
    }

    private MockHttpServletRequest request() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setServerName("localhost");
        request.setServerPort(8080);
        request.setContextPath("/oditji");
        return request;
    }

    private Object invoke(String name, Object... args) {
        return ReflectionTestUtils.invokeMethod(controller, name, args);
    }

    private boolean invokeBoolean(String name, Object... args) {
        return Boolean.TRUE.equals(invoke(name, args));
    }
}
