package com.project.oditji.member.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Test;
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

/** 사업자 로그인 거절 메시지에서 rejectReason null 단축평가 분기를 보완합니다. */
class MemberControllerBusinessRejectReasonNullCoverageTest {

    @Test
    void rejectedBusinessWithoutRejectReasonShouldReturnBaseMessageOnly() {
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

        String message = ReflectionTestUtils.invokeMethod(
                controller,
                "getBusinessLoginMessage",
                business);

        assertEquals("사업자 승인이 거절되었습니다.", message);
    }
}
