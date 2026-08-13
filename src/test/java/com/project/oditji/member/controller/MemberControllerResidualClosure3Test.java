package com.project.oditji.member.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.URI;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.project.oditji.business.service.BusinessService;
import com.project.oditji.business.service.NtsBusinessService;
import com.project.oditji.business.vo.BusinessVO;
import com.project.oditji.favorite.service.FavoriteService;
import com.project.oditji.mail.service.MailService;
import com.project.oditji.member.service.MemberPlatformService;
import com.project.oditji.member.service.MemberService;
import com.project.oditji.member.vo.MemberVO;
import com.project.oditji.order.service.OrderService;
import com.project.oditji.review.service.ReviewService;
import com.project.oditji.subscription.service.SubscriptionCalculatorService;
import com.project.oditji.wish.service.WishService;

import jakarta.servlet.http.HttpServletRequest;

/** 회원 컨트롤러의 표시명과 내부 리다이렉트 helper 잔여 피연산자를 보완합니다. */
class MemberControllerResidualClosure3Test {

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
    void loginDisplayNameShouldCoverBlankBusinessNameAndMemberNicknameFallback() {
        MemberVO member = new MemberVO();
        member.setMemberName(" ");
        member.setNickname("닉네임");

        assertEquals("닉네임", invoke("getLoginDisplayName", member, null));

        BusinessVO business = new BusinessVO();
        business.setBusinessName(" ");
        assertEquals("회원", invoke("getLoginDisplayName", member, business));
    }

    @Test
    void redirectNormalizerShouldCoverContextMismatchBlankQueryAndRelativePrefixing() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getContextPath()).thenReturn("/oditji");
        when(request.getServerName()).thenReturn("localhost");
        when(request.getServerPort()).thenReturn(8080);

        assertEquals(
                "/other/path",
                invoke("normalizeRedirectUrl", "/other/path", request));
        assertEquals(
                "/content/list",
                invoke("normalizeRedirectUrl", "/oditji/content/list", request));
        assertEquals(
                "/content/list",
                invoke("normalizeRedirectUrl", "content/list", request));
        assertNull(invoke("normalizeRedirectUrl", "http://example.com/other", request));
    }

    @Test
    void sameOriginAndLicenseExtensionShouldCoverLateConditions() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getServerName()).thenReturn("localhost");
        when(request.getServerPort()).thenReturn(443);

        Boolean sameHttps = invoke(
                "isSameOrigin",
                URI.create("https://localhost/path"),
                request);
        assertEquals(Boolean.TRUE, sameHttps);

        when(request.getServerPort()).thenReturn(80);
        Boolean differentPort = invoke(
                "isSameOrigin",
                URI.create("https://localhost/path"),
                request);
        assertNotEquals(Boolean.TRUE, differentPort);

        // [SonarQube] boolean 비교식을 assertTrue/assertFalse로 감싸지 않고 실제값을 직접 검증합니다.
        Boolean pdfAllowed = invoke("isAllowedBusinessLicenseExtension", ".pdf");
        Boolean pngAllowed = invoke("isAllowedBusinessLicenseExtension", ".png");
        Boolean gifAllowed = invoke("isAllowedBusinessLicenseExtension", ".gif");
        assertEquals(Boolean.TRUE, pdfAllowed);
        assertEquals(Boolean.TRUE, pngAllowed);
        assertNotEquals(Boolean.TRUE, gifAllowed);
    }

    @SuppressWarnings("unchecked")
    private <T> T invoke(String method, Object... arguments) {
        return (T) ReflectionTestUtils.invokeMethod(controller, method, arguments);
    }
}
