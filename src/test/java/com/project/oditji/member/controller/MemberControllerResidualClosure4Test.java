package com.project.oditji.member.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.URI;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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

/** 회원 컨트롤러의 파일 확장자 OR 체인과 context-path 단락 조건을 추가 보완합니다. */
class MemberControllerResidualClosure4Test {

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
    void businessLicenseExtensionShouldCoverJpgAndJpegOperands() {
        assertTrue(invokeBoolean("isAllowedBusinessLicenseExtension", ".jpg"));
        assertTrue(invokeBoolean("isAllowedBusinessLicenseExtension", ".jpeg"));
        assertFalse(invokeBoolean("isAllowedBusinessLicenseExtension", (Object) null));
    }

    @Test
    void redirectNormalizerShouldCoverNullBlankAndNonMatchingContextPathOperands() {
        HttpServletRequest nullContext = request(null);
        HttpServletRequest blankContext = request("   ");
        HttpServletRequest otherContext = request("/oditji");

        assertEquals(
                "/content/list",
                invokeString("normalizeRedirectUrl", "/content/list", nullContext));
        assertEquals(
                "/content/list",
                invokeString("normalizeRedirectUrl", "/content/list", blankContext));
        assertEquals(
                "/other/content/list",
                invokeString("normalizeRedirectUrl", "/other/content/list", otherContext));
    }

    @Test
    void sameOriginShouldCoverHttpDefaultAndExplicitPortPaths() {
        HttpServletRequest defaultPort = request("/oditji");
        when(defaultPort.getServerPort()).thenReturn(80);

        assertTrue(invokeBoolean(
                "isSameOrigin",
                URI.create("http://localhost/path"),
                defaultPort));

        HttpServletRequest explicitPort = request("/oditji");
        when(explicitPort.getServerPort()).thenReturn(8080);

        assertTrue(invokeBoolean(
                "isSameOrigin",
                URI.create("http://localhost:8080/path"),
                explicitPort));
    }

    private HttpServletRequest request(String contextPath) {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getContextPath()).thenReturn(contextPath);
        when(request.getServerName()).thenReturn("localhost");
        return request;
    }

    private boolean invokeBoolean(String methodName, Object... arguments) {
        Boolean result = ReflectionTestUtils.invokeMethod(
                controller,
                methodName,
                arguments);
        return Boolean.TRUE.equals(result);
    }

    private String invokeString(String methodName, Object... arguments) {
        return ReflectionTestUtils.invokeMethod(
                controller,
                methodName,
                arguments);
    }
}
