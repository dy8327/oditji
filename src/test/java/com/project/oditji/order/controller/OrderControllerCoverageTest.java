package com.project.oditji.order.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.Month;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.web.server.ResponseStatusException;

import com.project.oditji.common.vo.PageVO;
import com.project.oditji.member.vo.MemberVO;
import com.project.oditji.order.service.OrderService;
import com.project.oditji.order.vo.DeliveryVO;
import com.project.oditji.order.vo.OrderCheckoutRequestVO;
import com.project.oditji.order.vo.OrderDirectRequestVO;
import com.project.oditji.order.vo.OrderPaymentCancelRequestVO;
import com.project.oditji.order.vo.OrderPaymentCompleteRequestVO;
import com.project.oditji.order.vo.OrderPaymentPrepareVO;
import com.project.oditji.order.vo.OrderSheetItemVO;
import com.project.oditji.order.vo.OrderSubmitRequestVO;
import com.project.oditji.order.vo.OrderVO;
import com.project.oditji.refund.service.OrderCancelRefundService;
import com.project.oditji.refund.vo.OrderCancelRefundVO;

import jakarta.servlet.http.HttpSession;

/** 주문 화면과 결제·취소·배송 API의 성공 및 예외 분기를 검증합니다. */
@ExtendWith(MockitoExtension.class)
class OrderControllerCoverageTest {

    @Mock
    private OrderService orderService;

    @Mock
    private OrderCancelRefundService cancelRefundService;

    @Mock
    private HttpSession session;

    private final Map<String, Object> sessionValues = new HashMap<String, Object>();

    private OrderController controller;

    @BeforeEach
    void setUp() {
        controller = new OrderController(orderService, cancelRefundService);
        ReflectionTestUtils.setField(controller, "storeId", "store-test");
        ReflectionTestUtils.setField(controller, "paymentChannelKey", "channel-test");
        ReflectionTestUtils.setField(controller, "portOneTestMode", true);
        when(session.getAttribute(anyString()))
                .thenAnswer(invocation -> sessionValues.get(invocation.getArgument(0)));
    }

    @Test
    void anonymousOrInvalidMemberShouldReceiveLoginResponses() {
        OrderCheckoutRequestVO checkout = new OrderCheckoutRequestVO();
        checkout.setCartItemNos(List.of(1L));
        OrderDirectRequestVO direct = directRequest();
        OrderSubmitRequestVO submit = new OrderSubmitRequestVO("홍길동", "010", "서울");
        OrderPaymentCompleteRequestVO complete = completeRequest("pay-1");
        OrderPaymentCancelRequestVO cancel = cancelRequest();

        assertEquals(Boolean.TRUE, controller.checkoutFromCart(checkout, session).get("loginRequired"));
        assertEquals(Boolean.TRUE, controller.directOrder(direct, session).get("loginRequired"));
        assertEquals("redirect:/member/login?redirect=/order", controller.orderSheet(session, new ExtendedModelMap()));
        assertEquals(Boolean.TRUE, controller.preparePayment(submit, session).get("loginRequired"));
        assertEquals(Boolean.TRUE, controller.completePayment(complete, session).get("loginRequired"));
        assertEquals(Boolean.TRUE, controller.cancelPayment(cancel, session).get("loginRequired"));
        assertEquals(Boolean.TRUE, controller.cancelOrderItem(cancel, session).get("loginRequired"));
        assertEquals(Boolean.TRUE, controller.cancelOrderItems(cancel, session).get("loginRequired"));
        assertEquals("redirect:/member/login?redirect=/order/list", controller.orderComplete(1L, session, new ExtendedModelMap()));
        assertEquals(
                "redirect:/member/login?redirect=/order/list",
                controller.orderList(1, "ALL", "ALL", null, null, "order", session, new ExtendedModelMap()));
        assertEquals(Boolean.TRUE, controller.orderDeliveryDetail(1L, session).get("loginRequired"));

        MemberVO invalid = new MemberVO();
        invalid.setMemberNo(0L);
        sessionValues.put("loginMember", invalid);
        assertEquals(Boolean.TRUE, controller.checkoutFromCart(checkout, session).get("loginRequired"));
    }

    @Test
    void checkoutAndDirectShouldStoreOrderSheetAndMapFailures() {
        login(1L);
        OrderCheckoutRequestVO checkout = new OrderCheckoutRequestVO();
        checkout.setCartItemNos(List.of(1L, 2L));
        List<OrderSheetItemVO> items = List.of(item(1000, 0, 2));
        when(orderService.prepareCheckoutFromCart(1L, List.of(1L, 2L))).thenReturn(items);

        Map<String, Object> checkoutResponse = controller.checkoutFromCart(checkout, session);
        assertEquals(Boolean.TRUE, checkoutResponse.get("success"));
        assertEquals("/order", checkoutResponse.get("redirectUrl"));
        verify(session).setAttribute("orderSheet", items);
        verify(session).removeAttribute("orderPaymentPrepare");

        OrderDirectRequestVO direct = directRequest();
        when(orderService.prepareDirectOrder(1L, 20, 30L, 2)).thenReturn(items);
        Map<String, Object> directResponse = controller.directOrder(direct, session);
        assertEquals(Boolean.TRUE, directResponse.get("success"));

        when(orderService.prepareCheckoutFromCart(1L, List.of(9L)))
                .thenThrow(new IllegalArgumentException("선택 상품 오류"));
        checkout.setCartItemNos(List.of(9L));
        assertEquals("선택 상품 오류", controller.checkoutFromCart(checkout, session).get("message"));

        when(orderService.prepareDirectOrder(1L, 20, 30L, 2))
                .thenThrow(new RuntimeException("db"));
        assertEquals("주문서 작성 중 오류가 발생했습니다.", controller.directOrder(direct, session).get("message"));
    }

    @Test
    void orderSheetShouldRedirectExpiredSheetAndCalculateTotal() {
        login(2L);
        assertEquals("redirect:/cart", controller.orderSheet(session, new ExtendedModelMap()));

        List<OrderSheetItemVO> items = List.of(item(1000, 10, 2), item(500, 0, 3));
        sessionValues.put("orderSheet", items);
        ExtendedModelMap model = new ExtendedModelMap();

        assertEquals("order/order", controller.orderSheet(session, model));
        assertSame(items, model.get("orderItems"));
        assertEquals(3300L, model.get("totalPrice"));
        assertEquals("회원2", model.get("defaultReceiverName"));
        assertEquals("010-0000-0002", model.get("defaultReceiverPhone"));
        assertEquals("member2@test.com", model.get("defaultReceiverEmail"));
    }

    @Test
    void preparePaymentShouldValidateSheetAndExposePortOneValues() {
        login(3L);
        OrderSubmitRequestVO request = new OrderSubmitRequestVO("수령인", "010", "서울");
        assertEquals(
                "주문서 정보가 만료되었습니다. 다시 주문해주세요.",
                controller.preparePayment(request, session).get("message"));

        List<OrderSheetItemVO> items = List.of(item(1000, 0, 1));
        sessionValues.put("orderSheet", items);
        OrderPaymentPrepareVO prepare = new OrderPaymentPrepareVO();
        prepare.setPaymentId("payment-1");
        prepare.setOrderName("상품 외 1건");
        prepare.setTotalAmount(1000L);
        when(orderService.preparePayment(3L, items, "수령인", "010", "서울")).thenReturn(prepare);

        Map<String, Object> response = controller.preparePayment(request, session);
        assertEquals(Boolean.TRUE, response.get("success"));
        assertEquals("store-test", response.get("storeId"));
        assertEquals("channel-test", response.get("channelKey"));
        assertEquals("payment-1", response.get("paymentId"));
        assertEquals(1000L, response.get("totalAmount"));
        verify(session).setAttribute("orderPaymentPrepare", prepare);

        when(orderService.preparePayment(3L, items, "잘못", "010", "서울"))
                .thenThrow(new IllegalArgumentException("배송지 오류"));
        assertEquals(
                "배송지 오류",
                controller.preparePayment(new OrderSubmitRequestVO("잘못", "010", "서울"), session).get("message"));

        when(orderService.preparePayment(3L, items, "예외", "010", "서울"))
                .thenThrow(new RuntimeException("db"));
        assertEquals(
                "결제 준비 중 오류가 발생했습니다.",
                controller.preparePayment(new OrderSubmitRequestVO("예외", "010", "서울"), session).get("message"));
    }

    @Test
    void completePaymentShouldValidateAndCompleteOrder() {
        login(4L);
        assertEquals("결제 ID가 없습니다.", controller.completePayment(null, session).get("message"));
        assertEquals("결제 ID가 없습니다.", controller.completePayment(completeRequest(" "), session).get("message"));
        assertEquals(
                "결제 준비 정보가 만료되었습니다. 주문서를 다시 작성해주세요.",
                controller.completePayment(completeRequest("pay-1"), session).get("message"));

        OrderPaymentPrepareVO prepare = new OrderPaymentPrepareVO();
        sessionValues.put("orderPaymentPrepare", prepare);
        when(orderService.completePaidOrder(4L, prepare, "pay-1")).thenReturn(99L);

        Map<String, Object> success = controller.completePayment(completeRequest("pay-1"), session);
        assertEquals(Boolean.TRUE, success.get("success"));
        assertEquals(99L, success.get("orderNo"));
        assertEquals("/order/complete/99", success.get("redirectUrl"));
        verify(session).removeAttribute("orderSheet");
        verify(session).removeAttribute("orderPaymentPrepare");

        when(orderService.completePaidOrder(4L, prepare, "pay-2"))
                .thenThrow(new IllegalArgumentException("검증 실패"));
        assertEquals("검증 실패", controller.completePayment(completeRequest("pay-2"), session).get("message"));

        when(orderService.completePaidOrder(4L, prepare, "pay-3"))
                .thenThrow(new RuntimeException("db"));
        assertEquals(
                "결제 검증 또는 주문 처리 중 오류가 발생했습니다.",
                controller.completePayment(completeRequest("pay-3"), session).get("message"));
    }

    @Test
    void cancelApisShouldValidateRequestsAndMapSuccess() {
        login(5L);
        assertEquals("결제 취소 요청 정보가 없습니다.", controller.cancelPayment(null, session).get("message"));
        assertEquals("부분 취소 요청 정보가 없습니다.", controller.cancelOrderItem(new OrderPaymentCancelRequestVO(), session).get("message"));
        assertEquals("선택한 주문상품이 없습니다.", controller.cancelOrderItems(new OrderPaymentCancelRequestVO(), session).get("message"));

        OrderPaymentCancelRequestVO request = cancelRequest();
        assertEquals(Boolean.TRUE, controller.cancelPayment(request, session).get("success"));
        assertEquals(Boolean.TRUE, controller.cancelOrderItem(request, session).get("success"));
        assertEquals(Boolean.TRUE, controller.cancelOrderItems(request, session).get("success"));
        verify(cancelRefundService).requestOrderCancel(5L, 10L, "단순 변심");
        verify(cancelRefundService).requestOrderItemCancel(5L, 20L, "단순 변심");
        verify(cancelRefundService).requestOrderItemsCancel(5L, List.of(20L, 21L), "단순 변심");
    }

    @Test
    void cancelApisShouldMapIllegalAndUnexpectedFailures() {
        login(6L);
        OrderPaymentCancelRequestVO request = cancelRequest();
        doThrow(new IllegalArgumentException("전체 취소 불가"))
                .when(cancelRefundService).requestOrderCancel(6L, 10L, "단순 변심");
        assertEquals("전체 취소 불가", controller.cancelPayment(request, session).get("message"));

        doThrow(new IllegalArgumentException("부분 취소 불가"))
                .when(cancelRefundService).requestOrderItemCancel(6L, 20L, "단순 변심");
        assertEquals("부분 취소 불가", controller.cancelOrderItem(request, session).get("message"));

        doThrow(new IllegalArgumentException("일괄 취소 불가"))
                .when(cancelRefundService).requestOrderItemsCancel(6L, List.of(20L, 21L), "단순 변심");
        assertEquals("일괄 취소 불가", controller.cancelOrderItems(request, session).get("message"));

        OrderPaymentCancelRequestVO runtimeRequest = cancelRequest();
        runtimeRequest.setOrderNo(11L);
        runtimeRequest.setOrderItemNo(22L);
        runtimeRequest.setOrderItemNos(List.of(22L));
        doThrow(new RuntimeException("db"))
                .when(cancelRefundService).requestOrderCancel(6L, 11L, "단순 변심");
        doThrow(new RuntimeException("db"))
                .when(cancelRefundService).requestOrderItemCancel(6L, 22L, "단순 변심");
        doThrow(new RuntimeException("db"))
                .when(cancelRefundService).requestOrderItemsCancel(6L, List.of(22L), "단순 변심");
        assertEquals("주문 취소 요청 처리 중 오류가 발생했습니다.", controller.cancelPayment(runtimeRequest, session).get("message"));
        assertEquals("상품 부분 취소 요청 처리 중 오류가 발생했습니다.", controller.cancelOrderItem(runtimeRequest, session).get("message"));
        assertEquals("선택 상품 취소/환불 요청 처리 중 오류가 발생했습니다.", controller.cancelOrderItems(runtimeRequest, session).get("message"));
    }

    @Test
    void orderCompleteShouldExposeOrderAndTranslateMissingOrder() {
        login(7L);
        OrderVO order = new OrderVO();
        when(orderService.getOrderDetail(7L, 100L)).thenReturn(order);
        ExtendedModelMap model = new ExtendedModelMap();

        assertEquals("order/orderComplete", controller.orderComplete(100L, session, model));
        assertSame(order, model.get("order"));

        when(orderService.getOrderDetail(7L, 101L)).thenThrow(new IllegalArgumentException("없음"));
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> controller.orderComplete(101L, session, new ExtendedModelMap()));
        assertEquals(404, exception.getStatusCode().value());
    }

    @Test
    void orderListShouldClampPageAndExposeHistoryConditions() {
        login(8L);
        List<OrderVO> orders = List.of(new OrderVO());
        List<OrderCancelRefundVO> history = List.of(new OrderCancelRefundVO());
        when(orderService.getOrderCount(8L)).thenReturn(8);
        when(orderService.getOrderList(8L, 7, 9)).thenReturn(orders);
        LocalDate startDate = LocalDate.of(2026, Month.JULY, 1);
        LocalDate endDate = LocalDate.of(2026, Month.AUGUST, 1);
        when(cancelRefundService.getMemberCancelRefundHistory(8L, "REFUND", "WAITING", startDate, endDate))
                .thenReturn(history);
        ExtendedModelMap model = new ExtendedModelMap();

        assertEquals(
                "order/orderList",
                controller.orderList(99, "REFUND", "WAITING", startDate, endDate, "history", session, model));
        assertSame(orders, model.get("orderList"));
        assertSame(history, model.get("cancelRefundHistory"));
        assertEquals(Boolean.TRUE, model.get("portOneTestMode"));
        assertEquals("history", model.get("activeTab"));
        PageVO page = (PageVO) model.get("pageVO");
        assertEquals(3, page.getCurrentPage());
        assertEquals(3, page.getTotalPage());
        assertFalse(page.isPrev());
        assertFalse(page.isNext());
    }

    @Test
    void orderListShouldLeaveHistoryPeriodEmptyAndUseOrderTabWhenValuesAreMissing() {
        login(9L);
        when(orderService.getOrderCount(9L)).thenReturn(0);
        when(orderService.getOrderList(9L, 1, 3)).thenReturn(List.of());
        when(cancelRefundService.getMemberCancelRefundHistory(
                org.mockito.ArgumentMatchers.eq(9L),
                org.mockito.ArgumentMatchers.eq("ALL"),
                org.mockito.ArgumentMatchers.eq("ALL"),
                org.mockito.ArgumentMatchers.isNull(),
                org.mockito.ArgumentMatchers.isNull()))
                .thenReturn(List.of());
        ExtendedModelMap model = new ExtendedModelMap();

        controller.orderList(-5, "ALL", "ALL", null, null, "unknown", session, model);

        assertEquals("order", model.get("activeTab"));
        assertNull(model.get("historyStartDate"));
        assertNull(model.get("historyEndDate"));
        PageVO page = (PageVO) model.get("pageVO");
        assertEquals(1, page.getCurrentPage());
        assertEquals(1, page.getTotalPage());
    }

    @Test
    void deliveryShouldValidateReturnDataAndMapFailures() {
        login(10L);
        assertEquals("조회할 주문상품 번호가 없습니다.", controller.orderDeliveryDetail(null, session).get("message"));

        DeliveryVO delivery = new DeliveryVO();
        when(orderService.getDeliveryDetail(10L, 30L)).thenReturn(delivery);
        Map<String, Object> success = controller.orderDeliveryDetail(30L, session);
        assertEquals(Boolean.TRUE, success.get("success"));
        assertSame(delivery, success.get("delivery"));

        when(orderService.getDeliveryDetail(10L, 31L)).thenThrow(new IllegalArgumentException("배송 없음"));
        assertEquals("배송 없음", controller.orderDeliveryDetail(31L, session).get("message"));

        when(orderService.getDeliveryDetail(10L, 32L)).thenThrow(new RuntimeException("db"));
        assertEquals("배송 정보 조회 중 오류가 발생했습니다.", controller.orderDeliveryDetail(32L, session).get("message"));
    }

    private void login(Long memberNo) {
        MemberVO member = new MemberVO();
        member.setMemberNo(memberNo);
        member.setMemberName("회원" + memberNo);
        member.setPhone("010-0000-000" + memberNo);
        member.setEmail("member" + memberNo + "@test.com");
        sessionValues.put("loginMember", member);
    }

    private OrderDirectRequestVO directRequest() {
        OrderDirectRequestVO request = new OrderDirectRequestVO();
        request.setProductNo(20);
        request.setOptionNo(30L);
        request.setQuantity(2);
        return request;
    }

    private OrderPaymentCompleteRequestVO completeRequest(String paymentId) {
        OrderPaymentCompleteRequestVO request = new OrderPaymentCompleteRequestVO();
        request.setPaymentId(paymentId);
        return request;
    }

    private OrderPaymentCancelRequestVO cancelRequest() {
        OrderPaymentCancelRequestVO request = new OrderPaymentCancelRequestVO();
        request.setOrderNo(10L);
        request.setOrderItemNo(20L);
        request.setOrderItemNos(List.of(20L, 21L));
        request.setReason("단순 변심");
        return request;
    }

    private OrderSheetItemVO item(int price, int discountRate, int quantity) {
        OrderSheetItemVO item = new OrderSheetItemVO();
        item.setPrice(price);
        item.setDiscountRate(discountRate);
        item.setQuantity(quantity);
        return item;
    }
}
