package com.project.oditji.member.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.util.ReflectionTestUtils;

import com.project.oditji.business.service.BusinessService;
import com.project.oditji.business.service.NtsBusinessService;
import com.project.oditji.business.vo.BusinessVO;
import com.project.oditji.favorite.service.FavoriteService;
import com.project.oditji.mail.service.MailService;
import com.project.oditji.member.service.MemberPlatformService;
import com.project.oditji.member.service.MemberService;
import com.project.oditji.order.service.OrderService;
import com.project.oditji.review.service.ReviewService;
import com.project.oditji.wish.service.WishService;

/** 미승인 사업자 로그인 성공 후 제한된 사업자 홈으로 이동하는 분기를 보완합니다. */
class MemberControllerBusinessRejectReasonNullCoverageTest {

    @Test
    void rejectedBusinessShouldRedirectToBusinessMainAndClearSavedRedirect() {
        MemberController controller = new MemberController(
                mock(MemberService.class),
                mock(MemberPlatformService.class),
                mock(BusinessService.class),
                mock(NtsBusinessService.class),
                mock(MailService.class),
                mock(FavoriteService.class),
                mock(WishService.class),
                mock(OrderService.class),
                mock(ReviewService.class),
                "C:/test/profile",
                "C:/test/business-license");

        BusinessVO business = new BusinessVO();
        business.setStatus("REJECTED");
        business.setRejectReason(null);

        MockHttpSession session = new MockHttpSession();
        session.setAttribute("redirectAfterLogin", "/business/settlement/main");

        String redirect = ReflectionTestUtils.invokeMethod(
                controller,
                "getLoginSuccessRedirect",
                session,
                business);

        assertEquals("redirect:/business/main", redirect);
        assertNull(session.getAttribute("redirectAfterLogin"));
    }
}
