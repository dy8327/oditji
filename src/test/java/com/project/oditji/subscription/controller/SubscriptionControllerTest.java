package com.project.oditji.subscription.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;

import com.project.oditji.event.dao.OttDiscountDAO;
import com.project.oditji.subscription.service.SubscriptionCalculatorService;
import com.project.oditji.subscription.vo.SubscriptionCalculationResultVO;

/** 구독 계산기 화면 라우팅 및 필터 선택지(모델) 조회를 검증합니다. */
@ExtendWith(MockitoExtension.class)
class SubscriptionControllerTest {

    @Mock
    private OttDiscountDAO ottDiscountDAO;

    @Mock
    private SubscriptionCalculatorService subscriptionCalculatorService;

    private SubscriptionController controller;

    @BeforeEach
    void setUp() {
        controller = new SubscriptionController(ottDiscountDAO, subscriptionCalculatorService);
    }

    @Test
    void calculatorShouldReturnCalculatorViewWithFilterOptions() {
        when(ottDiscountDAO.selectDiscountProviderNames("TELECOM"))
                .thenReturn(List.of("SKT", "KT", "LGU+"));
        when(ottDiscountDAO.selectDiscountProviderNames("CARD"))
                .thenReturn(List.of("현대카드", "KB국민카드"));
        when(ottDiscountDAO.selectDiscountProviderNames("MEMBERSHIP"))
                .thenReturn(List.of("네이버", "쿠팡 와우"));

        Model model = new ExtendedModelMap();

        String viewName = controller.calculator(model);

        assertEquals("subscription/calculator", viewName);
        assertEquals(List.of("SKT", "KT", "LGU+"), model.getAttribute("telecomList"));
        assertEquals(List.of("현대카드", "KB국민카드"), model.getAttribute("cardList"));
        assertEquals(List.of("네이버", "쿠팡 와우"), model.getAttribute("membershipList"));
    }

    @Test
    void resultShouldReturnResultViewWithRestoredResult() {
        SubscriptionCalculationResultVO restored = new SubscriptionCalculationResultVO();
        when(subscriptionCalculatorService.restoreResult("SUBS_1"))
                .thenReturn(restored);

        Model model = new ExtendedModelMap();

        String viewName = controller.result("SUBS_1", model);

        assertEquals("subscription/result", viewName);
        assertEquals(restored, model.getAttribute("result"));
        assertEquals("SUBS_1", model.getAttribute("resultId"));
        assertNull(model.getAttribute("resultNotFound"));
    }

    @Test
    void resultShouldReturnResultViewWithNotFoundFlagWhenMissing() {
        when(subscriptionCalculatorService.restoreResult("SUBS_UNKNOWN"))
                .thenReturn(null);

        Model model = new ExtendedModelMap();

        String viewName = controller.result("SUBS_UNKNOWN", model);

        assertEquals("subscription/result", viewName);
        assertNull(model.getAttribute("result"));
        assertEquals(Boolean.TRUE, model.getAttribute("resultNotFound"));
    }
}