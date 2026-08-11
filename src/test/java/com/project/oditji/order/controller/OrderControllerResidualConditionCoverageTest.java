package com.project.oditji.order.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.web.server.ResponseStatusException;

import com.project.oditji.member.vo.MemberVO;
import com.project.oditji.order.service.OrderService;
import com.project.oditji.order.vo.OrderPaymentCancelRequestVO;
import com.project.oditji.order.vo.OrderPaymentCompleteRequestVO;
import com.project.oditji.order.vo.OrderSheetItemVO;
import com.project.oditji.order.vo.OrderSubmitRequestVO;
import com.project.oditji.refund.service.OrderCancelRefundService;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import jakarta.servlet.http.HttpSession;

/** 주문 컨트롤러의 null/blank/empty 단락 조건과 logger guard false 분기를 보완합니다. */
class OrderControllerResidualConditionCoverageTest {

    private OrderService orderService;
    private OrderController controller;

    @BeforeEach
    void setUp() {
        orderService = mock(OrderService.class);
        OrderCancelRefundService cancelRefundService = mock(OrderCancelRefundService.class);
        controller = new OrderController(orderService, cancelRefundService);
        ReflectionTestUtils.setField(controller, "storeId", "store");
        ReflectionTestUtils.setField(controller, "paymentChannelKey", "channel");
        ReflectionTestUtils.setField(controller, "portOneTestMode", true);
    }

    @Test
    void orderSheetAndPreparePaymentShouldCoverNonNullEmptyLists() {
        HttpSession session = loginSession(1L);
        when(session.getAttribute("orderSheet")).thenReturn(List.of());

        assertEquals(
                "redirect:/cart",
                controller.orderSheet(session, new ExtendedModelMap()));

        OrderSubmitRequestVO submit = new OrderSubmitRequestVO("홍길동", "010", "서울");
        Map<String, Object> response = controller.preparePayment(submit, session);
        assertEquals(Boolean.FALSE, response.get("success"));
    }

    @Test
    void completePaymentShouldCoverNonNullRequestWithNullAndBlankPaymentIds() {
        HttpSession session = loginSession(2L);

        OrderPaymentCompleteRequestVO nullId = new OrderPaymentCompleteRequestVO();
        Map<String, Object> nullResult = controller.completePayment(nullId, session);
        assertEquals(Boolean.FALSE, nullResult.get("success"));

        OrderPaymentCompleteRequestVO blankId = new OrderPaymentCompleteRequestVO();
        blankId.setPaymentId("   ");
        Map<String, Object> blankResult = controller.completePayment(blankId, session);
        assertEquals(Boolean.FALSE, blankResult.get("success"));
    }

    @Test
    void cancelEndpointsShouldCoverSecondAndThirdOrOperands() {
        HttpSession session = loginSession(3L);

        OrderPaymentCancelRequestVO itemRequest = new OrderPaymentCancelRequestVO();
        Map<String, Object> itemResult = controller.cancelOrderItem(itemRequest, session);
        assertEquals(Boolean.FALSE, itemResult.get("success"));

        OrderPaymentCancelRequestVO nullItems = new OrderPaymentCancelRequestVO();
        Map<String, Object> nullItemsResult = controller.cancelOrderItems(nullItems, session);
        assertEquals(Boolean.FALSE, nullItemsResult.get("success"));

        OrderPaymentCancelRequestVO emptyItems = new OrderPaymentCancelRequestVO();
        emptyItems.setOrderItemNos(List.of());
        Map<String, Object> emptyItemsResult = controller.cancelOrderItems(emptyItems, session);
        assertEquals(Boolean.FALSE, emptyItemsResult.get("success"));
    }

    @Test
    void loggerDisabledShouldCoverPreparePaymentAndDeliveryErrorGuards() {
        HttpSession session = loginSession(4L);
        OrderSheetItemVO item = new OrderSheetItemVO();
        item.setProductNo(10);
        item.setQuantity(1);
        when(session.getAttribute("orderSheet")).thenReturn(List.of(item));

        OrderSubmitRequestVO submit = new OrderSubmitRequestVO("홍길동", "010", "서울");
        when(orderService.preparePayment(4L, List.of(item), "홍길동", "010", "서울"))
                .thenThrow(new RuntimeException("payment"));
        when(orderService.getDeliveryDetail(4L, 10L))
                .thenThrow(new RuntimeException("delivery"));

        Logger logger = (Logger) LoggerFactory.getLogger(OrderController.class);
        Level original = logger.getLevel();
        try {
            logger.setLevel(Level.OFF);
            Map<String, Object> paymentResult = controller.preparePayment(submit, session);
            assertEquals(Boolean.FALSE, paymentResult.get("success"));

            Map<String, Object> deliveryResult = controller.orderDeliveryDetail(10L, session);
            assertEquals(Boolean.FALSE, deliveryResult.get("success"));
        } finally {
            logger.setLevel(original);
        }
    }

    @Test
    void loggerDisabledShouldCoverOrderCompleteWarnGuard() {
        HttpSession session = loginSession(5L);
        when(orderService.getOrderDetail(5L, 99L))
                .thenThrow(new IllegalArgumentException("missing"));

        Logger logger = (Logger) LoggerFactory.getLogger(OrderController.class);
        Level original = logger.getLevel();
        try {
            logger.setLevel(Level.OFF);
            assertThrows(
                    ResponseStatusException.class,
                    () -> controller.orderComplete(99L, session, new ExtendedModelMap()));
        } finally {
            logger.setLevel(original);
        }
    }

    private HttpSession loginSession(Long memberNo) {
        HttpSession session = mock(HttpSession.class);
        MemberVO member = new MemberVO();
        member.setMemberNo(memberNo);
        when(session.getAttribute("loginMember")).thenReturn(member);
        return session;
    }
}
