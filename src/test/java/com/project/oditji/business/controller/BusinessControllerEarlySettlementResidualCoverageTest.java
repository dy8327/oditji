package com.project.oditji.business.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import com.project.oditji.business.service.BusinessService;
import com.project.oditji.business.vo.BusinessVO;
import com.project.oditji.refund.service.OrderCancelRefundService;

/** 사전 정산 요청 컨트롤러의 로그인/성공/업무 예외 잔여 분기를 보완합니다. */
@ExtendWith(MockitoExtension.class)
class BusinessControllerEarlySettlementResidualCoverageTest {

    @Mock
    private BusinessService businessService;

    @Mock
    private OrderCancelRefundService orderCancelRefundService;

    private BusinessController controller;

    @BeforeEach
    void setUp() {
        controller = new BusinessController(
                businessService,
                orderCancelRefundService);
    }

    @Test
    void earlySettlementShouldRedirectGuestToLogin() {
        RedirectAttributesModelMap redirectAttributes =
                new RedirectAttributesModelMap();

        assertEquals(
                "redirect:/member/login",
                controller.requestEarlySettlement(
                        new MockHttpSession(),
                        redirectAttributes));
        assertEquals(
                "로그인 회원 정보를 확인할 수 없습니다. 다시 로그인해주세요.",
                redirectAttributes.getFlashAttributes().get("errorMessage"));
    }

    @Test
    void earlySettlementShouldRequestForLoggedInBusiness() {
        BusinessVO business = business(200L);
        when(businessService.getBusinessByMemberNo(100L))
                .thenReturn(business);

        RedirectAttributesModelMap redirectAttributes =
                new RedirectAttributesModelMap();

        assertEquals(
                "redirect:/business/settlement/main?cycle=next",
                controller.requestEarlySettlement(
                        session(100L),
                        redirectAttributes));
        verify(businessService).requestEarlySettlement(200L);
        assertEquals(
                "사전 정산 요청이 완료되었습니다. 이번 달에 추가되는 정산 대상 매출도 자동으로 반영됩니다.",
                redirectAttributes.getFlashAttributes().get("successMessage"));
    }

    @Test
    void earlySettlementShouldExposeBusinessValidationMessage() {
        BusinessVO business = business(201L);
        when(businessService.getBusinessByMemberNo(101L))
                .thenReturn(business);
        doThrow(new IllegalStateException("이미 사전 정산 요청이 존재합니다."))
                .when(businessService)
                .requestEarlySettlement(201L);

        RedirectAttributesModelMap redirectAttributes =
                new RedirectAttributesModelMap();

        assertEquals(
                "redirect:/business/settlement/main?cycle=next",
                controller.requestEarlySettlement(
                        session(101L),
                        redirectAttributes));
        assertEquals(
                "이미 사전 정산 요청이 존재합니다.",
                redirectAttributes.getFlashAttributes().get("errorMessage"));
    }

    private BusinessVO business(Long businessNo) {
        BusinessVO business = new BusinessVO();
        business.setBusinessNo(businessNo);
        business.setStatus("APPROVED");
        return business;
    }

    private MockHttpSession session(long memberNo) {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("loginMemberNo", memberNo);
        return session;
    }
}
