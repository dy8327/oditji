package com.project.oditji.order.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.project.oditji.admin.service.AdminService;
import com.project.oditji.cart.dao.CartDAO;
import com.project.oditji.notification.service.NotificationService;
import com.project.oditji.order.dao.OrderDAO;
import com.project.oditji.order.vo.OrderItemVO;
import com.project.oditji.order.vo.OrderPaymentPrepareVO;
import com.project.oditji.order.vo.OrderSheetItemVO;
import com.project.oditji.order.vo.OrderVO;
import com.project.oditji.payment.dao.PaymentDAO;
import com.project.oditji.payment.service.PaymentService;
import com.project.oditji.payment.vo.PaymentVO;

/** 결제 준비·완료와 사용자 전액 취소의 트랜잭션 흐름을 검증합니다. */
@ExtendWith(MockitoExtension.class)
class OrderServicePaymentFlowCoverageTest {

    @Mock
    private OrderDAO orderDAO;
    @Mock
    private CartDAO cartDAO;
    @Mock
    private PaymentDAO paymentDAO;
    @Mock
    private PaymentService paymentService;
    @Mock
    private NotificationService notificationService;
    @Mock
    private AdminService adminService;

    private OrderServiceImpl orderService;

    @BeforeEach
    void setUp() {
        orderService = new OrderServiceImpl(
                orderDAO,
                cartDAO,
                paymentDAO,
                paymentService,
                notificationService,
                adminService);
    }

    @Test
    void preparePaymentShouldRejectMissingItemsAndInvalidEntries() {
        assertThrows(
                IllegalArgumentException.class,
                () -> orderService.preparePayment(
                        1L, null, "홍길동", "010", "서울"));
        assertThrows(
                IllegalArgumentException.class,
                () -> orderService.preparePayment(
                        1L, List.of(), "홍길동", "010", "서울"));

        List<OrderSheetItemVO> invalidItems = Arrays.asList((OrderSheetItemVO) null);
        assertThrows(
                IllegalArgumentException.class,
                () -> orderService.preparePayment(
                        1L, invalidItems, "홍길동", "010", "서울"));
    }

    @Test
    void preparePaymentShouldReloadProductsCalculateTotalAndTrimAddress() {
        OrderSheetItemVO requested = item(10, null, "상품A", 2, 10, 10000, 10, 5L);
        OrderSheetItemVO current = item(10, null, "상품A", 1, 10, 10000, 10, null);
        when(orderDAO.selectProductForOrder(10, null)).thenReturn(current);

        OrderPaymentPrepareVO result = orderService.preparePayment(
                1L,
                List.of(requested),
                " 홍길동 ",
                " 010-1234-5678 ",
                " 서울시 강남구 ");

        assertTrue(result.getPaymentId().startsWith("ODT_"));
        assertEquals(36, result.getPaymentId().length());
        assertEquals("상품A", result.getOrderName());
        assertEquals(18000L, result.getTotalAmount());
        assertEquals("홍길동", result.getReceiverName());
        assertEquals("010-1234-5678", result.getReceiverPhone());
        assertEquals("서울시 강남구", result.getAddress());
        assertSame(current, result.getItems().get(0));
        assertEquals(2, current.getQuantity());
        assertEquals(5L, current.getCartItemNo());
    }

    @Test
    void preparePaymentShouldRejectMissingReloadedProductAndZeroAmount() {
        OrderSheetItemVO requested = item(10, null, "없음", 1, 1, 1000, 0, null);
        List<OrderSheetItemVO> requestedItems = List.of(requested);
        when(orderDAO.selectProductForOrder(10, null)).thenReturn(null);
        assertThrows(
                IllegalArgumentException.class,
                () -> orderService.preparePayment(
                        1L, requestedItems, "홍길동", "010", "서울"));

        OrderSheetItemVO freeRequested = item(11, null, "무료", 1, 1, 0, 0, null);
        List<OrderSheetItemVO> freeRequestedItems = List.of(freeRequested);
        OrderSheetItemVO freeCurrent = item(11, null, "무료", 1, 1, 0, 0, null);
        when(orderDAO.selectProductForOrder(11, null)).thenReturn(freeCurrent);
        assertThrows(
                IllegalArgumentException.class,
                () -> orderService.preparePayment(
                        1L, freeRequestedItems, "홍길동", "010", "서울"));
    }

    @Test
    void completePaidOrderShouldValidatePreparedRequestAndDuplicatePayment() {
        assertThrows(
                IllegalArgumentException.class,
                () -> orderService.completePaidOrder(1L, null, "pay-1"));

        OrderPaymentPrepareVO prepare = prepare("pay-1", List.of(item(
                10, null, "상품", 1, 10, 1000, 0, null)), 1000L);
        assertThrows(
                IllegalArgumentException.class,
                () -> orderService.completePaidOrder(1L, prepare, " "));
        assertThrows(
                IllegalArgumentException.class,
                () -> orderService.completePaidOrder(1L, prepare, "other"));

        OrderPaymentPrepareVO empty = prepare("pay-2", List.of(), 1000L);
        assertThrows(
                IllegalArgumentException.class,
                () -> orderService.completePaidOrder(1L, empty, "pay-2"));

        when(paymentDAO.selectPaymentByPaymentId("pay-1")).thenReturn(new PaymentVO());
        assertThrows(
                IllegalArgumentException.class,
                () -> orderService.completePaidOrder(1L, prepare, "pay-1"));
        verify(paymentService, never()).verifyPaidPayment(any(), any(), any());
    }

    @Test
    void completePaidOrderShouldPersistOrderItemsPaymentSettlementsAndNotification() {
        OrderSheetItemVO first = item(10, null, "상품A", 2, 10, 1000, 0, 5L);
        first.setBusinessNo(20);
        OrderSheetItemVO second = item(11, 9L, "상품B", 1, 5, 2000, 10, null);
        second.setBusinessNo(21);
        OrderPaymentPrepareVO prepare = prepare(
                "pay-success",
                List.of(first, second),
                3800L);
        prepare.setOrderName("상품A 외 1건");

        when(orderDAO.selectProductForOrder(10, null))
                .thenReturn(item(10, null, "상품A", 1, 10, 1000, 0, null));
        when(orderDAO.selectProductForOrder(11, 9L))
                .thenReturn(item(11, 9L, "상품B", 1, 5, 2000, 10, null));
        PaymentVO verified = new PaymentVO();
        when(paymentService.verifyPaidPayment(
                "pay-success", 3800L, "상품A 외 1건"))
                .thenReturn(verified);
        when(orderDAO.insertOrder(any(OrderVO.class))).thenAnswer(invocation -> {
            OrderVO order = invocation.getArgument(0);
            order.setOrderNo(100L);
            return 1;
        });
        AtomicLong orderItemSequence = new AtomicLong(200L);
        when(orderDAO.insertOrderItem(any(OrderItemVO.class))).thenAnswer(invocation -> {
            OrderItemVO item = invocation.getArgument(0);
            item.setOrderItemNo(orderItemSequence.getAndIncrement());
            return 1;
        });
        when(orderDAO.decreaseProductStock(10, 2)).thenReturn(1);
        when(orderDAO.decreaseProductOptionStock(9L, 1)).thenReturn(1);
        when(orderDAO.decreaseProductStock(11, 1)).thenReturn(1);
        when(paymentDAO.insertPayment(verified)).thenAnswer(invocation -> {
            verified.setPaymentNo(300L);
            return 1;
        });
        when(orderDAO.insertWaitingSettlement(200L)).thenReturn(1);
        when(orderDAO.insertWaitingSettlement(201L)).thenReturn(1);

        Long orderNo = orderService.completePaidOrder(
                1L,
                prepare,
                "pay-success");

        assertEquals(100L, orderNo);
        assertEquals(100L, verified.getOrderNo());
        assertEquals("상품A 외 1건", verified.getOrderName());
        verify(adminService).updateBusinessGradesBySales();
        verify(cartDAO).deleteSelectedCartItems(1L, List.of(5L));
        verify(notificationService).createForOrderBusinesses(
                100L,
                "NEW_ORDER",
                "새로운 주문 접수",
                "새로운 결제 완료 주문이 접수되었습니다.",
                "/business/order/list?openOrderNo=100",
                "ORDER",
                100L);
    }

    @Test
    void completePaidOrderShouldRejectUnavailablePreparedProduct() {
        OrderSheetItemVO preparedItem = item(10, null, "사라진 상품", 1, 1, 1000, 0, null);
        OrderPaymentPrepareVO prepare = prepare("pay-missing", List.of(preparedItem), 1000L);
        when(orderDAO.selectProductForOrder(10, null)).thenReturn(null);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> orderService.completePaidOrder(1L, prepare, "pay-missing"));

        assertTrue(exception.getMessage().contains("사라진 상품"));
        verify(paymentService, never()).verifyPaidPayment(any(), any(), any());
    }

    @Test
    void cancelPaidOrderShouldRejectOwnershipStateDeliveryAndPaymentProblems() {
        assertThrows(
                IllegalArgumentException.class,
                () -> orderService.cancelPaidOrder(1L, 0L, "사유"));

        when(orderDAO.selectOrderByMember(1L, 10L)).thenReturn(null);
        assertThrows(
                IllegalArgumentException.class,
                () -> orderService.cancelPaidOrder(1L, 10L, "사유"));

        OrderVO canceled = order(11L, "CANCELED");
        when(orderDAO.selectOrderByMember(1L, 11L)).thenReturn(canceled);
        assertThrows(
                IllegalArgumentException.class,
                () -> orderService.cancelPaidOrder(1L, 11L, "사유"));

        OrderVO ready = order(12L, "READY");
        when(orderDAO.selectOrderByMember(1L, 12L)).thenReturn(ready);
        assertThrows(
                IllegalArgumentException.class,
                () -> orderService.cancelPaidOrder(1L, 12L, "사유"));

        OrderVO paidShipping = order(13L, "PAID");
        when(orderDAO.selectOrderByMember(1L, 13L)).thenReturn(paidShipping);
        when(orderDAO.countStartedDeliveryByOrderNo(13L)).thenReturn(1);
        assertThrows(
                IllegalArgumentException.class,
                () -> orderService.cancelPaidOrder(1L, 13L, "사유"));

        OrderVO paidNoPayment = order(14L, "PAID");
        when(orderDAO.selectOrderByMember(1L, 14L)).thenReturn(paidNoPayment);
        when(paymentDAO.selectPaymentByOrderNo(14L)).thenReturn(null);
        assertThrows(
                IllegalArgumentException.class,
                () -> orderService.cancelPaidOrder(1L, 14L, "사유"));
    }

    @Test
    void cancelPaidOrderShouldRestoreStockAndPersistCanceledStates() {
        OrderVO order = order(20L, "PAID");
        PaymentVO payment = payment("pay-cancel", "PAID");
        PaymentVO canceledPayment = payment("pay-cancel", "CANCELED");
        when(orderDAO.selectOrderByMember(1L, 20L)).thenReturn(order);
        when(orderDAO.countStartedDeliveryByOrderNo(20L)).thenReturn(0);
        when(paymentDAO.selectPaymentByOrderNo(20L)).thenReturn(payment);
        when(paymentService.cancelPaidPayment(payment, "단순 변심"))
                .thenReturn(canceledPayment);
        when(orderDAO.restoreProductStockByOrderNo(20L)).thenReturn(2);
        when(orderDAO.updateOrderItemsCanceled(20L)).thenReturn(2);
        when(orderDAO.updateOrderCanceled(1L, 20L)).thenReturn(1);
        when(paymentDAO.updatePaymentCanceled(canceledPayment)).thenReturn(1);

        orderService.cancelPaidOrder(1L, 20L, "  단순 변심  ");

        verify(orderDAO).restoreProductOptionStockByOrderNo(20L);
        verify(orderDAO).rejectSettlementsByOrderNo(20L);
        verify(paymentDAO).updatePaymentCanceled(canceledPayment);
    }

    @Test
    void cancelPaidOrderShouldFailWhenStockRestoreDoesNotChangeRows() {
        OrderVO order = order(21L, "PAID");
        PaymentVO payment = payment("pay-restore", "PAID");
        when(orderDAO.selectOrderByMember(1L, 21L)).thenReturn(order);
        when(paymentDAO.selectPaymentByOrderNo(21L)).thenReturn(payment);
        when(paymentService.cancelPaidPayment(payment, "사유")).thenReturn(payment);
        when(orderDAO.restoreProductStockByOrderNo(21L)).thenReturn(0);

        assertThrows(
                IllegalStateException.class,
                () -> orderService.cancelPaidOrder(1L, 21L, "사유"));
        verify(orderDAO, never()).updateOrderItemsCanceled(21L);
    }

    private OrderSheetItemVO item(
            int productNo,
            Long optionNo,
            String name,
            int quantity,
            int stock,
            int price,
            int discountRate,
            Long cartItemNo) {
        OrderSheetItemVO item = new OrderSheetItemVO();
        item.setProductNo(productNo);
        item.setOptionNo(optionNo);
        item.setProductName(name);
        item.setProductType("GOODS");
        item.setQuantity(quantity);
        item.setStock(stock);
        item.setPrice(price);
        item.setDiscountRate(discountRate);
        item.setStatus("APPROVED");
        item.setCartItemNo(cartItemNo);
        return item;
    }

    private OrderPaymentPrepareVO prepare(
            String paymentId,
            List<OrderSheetItemVO> items,
            Long totalAmount) {
        OrderPaymentPrepareVO prepare = new OrderPaymentPrepareVO();
        prepare.setPaymentId(paymentId);
        prepare.setOrderName("주문 상품");
        prepare.setTotalAmount(totalAmount);
        prepare.setReceiverName("홍길동");
        prepare.setReceiverPhone("010-1234-5678");
        prepare.setAddress("서울시");
        prepare.setItems(items);
        return prepare;
    }

    private OrderVO order(Long orderNo, String status) {
        OrderVO order = new OrderVO();
        order.setOrderNo(orderNo);
        order.setOrderStatus(status);
        return order;
    }

    private PaymentVO payment(String paymentId, String status) {
        PaymentVO payment = new PaymentVO();
        payment.setPaymentId(paymentId);
        payment.setPaymentStatus(status);
        payment.setPaymentAmount(1000L);
        return payment;
    }
}
