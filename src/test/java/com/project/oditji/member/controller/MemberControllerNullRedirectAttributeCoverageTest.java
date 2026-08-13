package com.project.oditji.member.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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

/** 로그인 성공 redirect의 세션 속성 자체가 없는 첫 번째 short-circuit 분기를 보완합니다. */
class MemberControllerNullRedirectAttributeCoverageTest {

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
    void loginSuccessRedirectShouldUseHomeWhenSessionHasNoSavedRedirect() {
        MockHttpSession session = new MockHttpSession();

        String result = ReflectionTestUtils.invokeMethod(
                controller,
                "getLoginSuccessRedirect",
                session);

        assertEquals("redirect:/", result);
        assertNull(session.getAttribute("redirectAfterLogin"));
    }
}
