package com.project.oditji.business.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import com.project.oditji.business.service.BusinessService;
import com.project.oditji.business.vo.BusinessVO;
import com.project.oditji.business.vo.SettlementManageVO;
import com.project.oditji.common.vo.SettlementRequestVO;
import com.project.oditji.refund.service.OrderCancelRefundService;

/** BusinessController 정산 완료/계좌 화면과 세션 번호 변환 분기를 보완합니다. */
@ExtendWith(MockitoExtension.class)
class BusinessControllerSettlementCoverageTest {

    private static final String REDIRECT_LOGIN = "redirect:/member/login";

    @Mock
    private BusinessService businessService;

    @Mock
    private OrderCancelRefundService orderCancelRefundService;

    private BusinessController controller;

    @BeforeEach
    void setUp() {
        controller = new BusinessController(businessService, orderCancelRefundService);
    }

    @Test
    void settlementCompleteShouldRedirectAnonymousAndRenderLoggedInBusiness() {
        assertEquals(
                REDIRECT_LOGIN,
                controller.settlementComplete(
                        new MockHttpSession(),
                        new ExtendedModelMap(),
                        new RedirectAttributesModelMap()));

        BusinessVO business = business(31L);
        List<SettlementRequestVO> history = List.of(new SettlementRequestVO());
        when(businessService.getBusinessByMemberNo(11L)).thenReturn(business);
        when(businessService.getSettlementPaymentHistory(31L)).thenReturn(history);
        ExtendedModelMap model = new ExtendedModelMap();

        String view = controller.settlementComplete(
                session("loginMemberNo", 11L),
                model,
                new RedirectAttributesModelMap());

        assertEquals("business/settlement/settlementComplete", view);
        assertSame(business, model.get("business"));
        assertSame(history, model.get("settlementHistory"));
        assertEquals("settlement", model.get("activeMenu"));
    }

    @Test
    void settlementAccountShouldSupportLegacyNumericStringMemberNo() {
        BusinessVO business = business(41L);
        SettlementManageVO account = new SettlementManageVO();
        when(businessService.getBusinessByMemberNo(12L)).thenReturn(business);
        when(businessService.getSettlementAccount(41L)).thenReturn(account);
        MockHttpSession session = session("memberNo", " 12 ");
        ExtendedModelMap model = new ExtendedModelMap();

        String view = controller.settlementAccount(
                session,
                model,
                new RedirectAttributesModelMap());

        assertEquals("business/settlement/settlementAccount", view);
        assertEquals(12L, session.getAttribute("loginMemberNo"));
        assertSame(account, model.get("settlementAccount"));
    }

    @Test
    void invalidLegacyMemberNumbersShouldStillRedirectToLogin() {
        assertEquals(
                REDIRECT_LOGIN,
                controller.settlementAccount(
                        session("memberNo", "not-a-number"),
                        new ExtendedModelMap(),
                        new RedirectAttributesModelMap()));

        assertEquals(
                REDIRECT_LOGIN,
                controller.settlementAccount(
                        session("loginMemberNo", -1),
                        new ExtendedModelMap(),
                        new RedirectAttributesModelMap()));
    }

    private MockHttpSession session(String key, Object value) {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(key, value);
        return session;
    }

    private BusinessVO business(long businessNo) {
        BusinessVO business = new BusinessVO();
        business.setBusinessNo(businessNo);
        business.setStatus("APPROVED");
        return business;
    }
}
