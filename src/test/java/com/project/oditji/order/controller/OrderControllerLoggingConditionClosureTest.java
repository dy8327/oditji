package com.project.oditji.order.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.mock.web.MockHttpSession;

import com.project.oditji.member.vo.MemberVO;
import com.project.oditji.order.service.OrderService;
import com.project.oditji.order.vo.OrderCheckoutRequestVO;
import com.project.oditji.order.vo.OrderPaymentCompleteRequestVO;
import com.project.oditji.order.vo.OrderPaymentPrepareVO;
import com.project.oditji.refund.service.OrderCancelRefundService;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

/** 주문 컨트롤러 예외 처리의 logger guard false 분기를 추가로 보완합니다. */
class OrderControllerLoggingConditionClosureTest {

    private OrderService orderService;
    private OrderController controller;

    @BeforeEach
    void setUp() {
        orderService = mock(OrderService.class);
        controller = new OrderController(
                orderService,
                mock(OrderCancelRefundService.class));
    }

    @Test
    void checkoutFailureShouldRemainSafeWhenErrorLoggingIsDisabled() {
        MockHttpSession session = loginSession(1L);
        OrderCheckoutRequestVO request = new OrderCheckoutRequestVO();
        request.setCartItemNos(List.of(10L));

        when(orderService.prepareCheckoutFromCart(1L, List.of(10L)))
                .thenThrow(new RuntimeException("checkout"));

        Logger logger = (Logger) LoggerFactory.getLogger(OrderController.class);
        Level original = logger.getLevel();
        try {
            logger.setLevel(Level.OFF);

            Map<String, Object> response = controller.checkoutFromCart(request, session);

            assertEquals(Boolean.FALSE, response.get("success"));
            assertEquals("주문서 작성 중 오류가 발생했습니다.", response.get("message"));
        } finally {
            logger.setLevel(original);
        }
    }

    @Test
    void paymentCompletionFailureShouldRemainSafeWhenErrorLoggingIsDisabled() {
        MockHttpSession session = loginSession(2L);
        OrderPaymentPrepareVO prepareVO = new OrderPaymentPrepareVO();
        session.setAttribute("orderPaymentPrepare", prepareVO);

        OrderPaymentCompleteRequestVO request = new OrderPaymentCompleteRequestVO();
        request.setPaymentId("payment-1");

        when(orderService.completePaidOrder(2L, prepareVO, "payment-1"))
                .thenThrow(new RuntimeException("complete"));

        Logger logger = (Logger) LoggerFactory.getLogger(OrderController.class);
        Level original = logger.getLevel();
        try {
            logger.setLevel(Level.OFF);

            Map<String, Object> response = controller.completePayment(request, session);

            assertEquals(Boolean.FALSE, response.get("success"));
            assertEquals(
                    "결제 검증 또는 주문 처리 중 오류가 발생했습니다.",
                    response.get("message"));
        } finally {
            logger.setLevel(original);
        }
    }

    private MockHttpSession loginSession(Long memberNo) {
        MockHttpSession session = new MockHttpSession();
        MemberVO member = new MemberVO();
        member.setMemberNo(memberNo);
        session.setAttribute("loginMember", member);
        return session;
    }
}
