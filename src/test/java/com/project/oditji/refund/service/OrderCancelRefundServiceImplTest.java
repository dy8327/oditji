package com.project.oditji.refund.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.Month;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.project.oditji.business.dao.BusinessDAO;
import com.project.oditji.business.vo.BusinessVO;
import com.project.oditji.notification.service.NotificationService;
import com.project.oditji.order.vo.OrderItemVO;
import com.project.oditji.payment.dao.PaymentDAO;
import com.project.oditji.payment.service.PaymentService;
import com.project.oditji.payment.vo.PaymentVO;
import com.project.oditji.refund.dao.OrderCancelRefundDAO;
import com.project.oditji.refund.vo.OrderCancelRefundVO;

/** 주문 전체·부분 취소, 사업자 승인·반려와 조회 검증을 수행합니다. */
@ExtendWith(MockitoExtension.class)
class OrderCancelRefundServiceImplTest {

    @Mock
    private OrderCancelRefundDAO refundDAO;
    @Mock
    private BusinessDAO businessDAO;
    @Mock
    private PaymentDAO paymentDAO;
    @Mock
    private PaymentService paymentService;
    @Mock
    private NotificationService notificationService;

    private OrderCancelRefundServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new OrderCancelRefundServiceImpl(
                refundDAO,
                businessDAO,
                paymentDAO,
                paymentService,
                notificationService);
        ReflectionTestUtils.setField(service, "portOneTestMode", true);
    }

    @Test
    void memberHistoryShouldNormalizeFiltersAndDates() {
        List<OrderCancelRefundVO> expected = List.of(new OrderCancelRefundVO());
        when(refundDAO.selectMemberCancelRefundHistory(
                eq(1L),
                eq("CANCEL"),
                eq("APPROVED"),
                any(),
                any())).thenReturn(expected);

        List<OrderCancelRefundVO> result = service.getMemberCancelRefundHistory(
                1L,
                " cancel ",
                "approved",
                LocalDate.of(2026, Month.AUGUST, 1),
                LocalDate.of(2026, Month.AUGUST, 4));

        assertSame(expected, result);
        verify(refundDAO).selectMemberCancelRefundHistory(
                1L,
                "CANCEL",
                "APPROVED",
                LocalDate.of(2026, Month.AUGUST, 1),
                LocalDate.of(2026, Month.AUGUST, 4));
    }

    @Test
    void memberHistoryShouldRejectInvalidMemberFilterAndDateRange() {
        assertThrows(IllegalArgumentException.class,
                () -> service.getMemberCancelRefundHistory(
                        null, null, null, null, null));
        assertThrows(IllegalArgumentException.class,
                () -> service.getMemberCancelRefundHistory(
                        1L, "other", null, null, null));
        assertThrows(IllegalArgumentException.class,
                () -> service.getMemberCancelRefundHistory(
                        1L, null, "done", null, null));
        LocalDate invalidStartDate =
                LocalDate.of(2026, Month.AUGUST, 5);
        LocalDate invalidEndDate =
                LocalDate.of(2026, Month.AUGUST, 1);
        assertThrows(IllegalArgumentException.class,
                () -> service.getMemberCancelRefundHistory(
                        1L,
                        "ALL",
                        "ALL",
                        invalidStartDate,
                        invalidEndDate));
    }

    @Test
    void fullCancelRequestShouldInsertEveryItemAndNotifyBusinesses() {
        OrderItemVO first = item(11L, 100L, 1001, 2, 5000);
        OrderItemVO second = item(12L, 100L, 1002, 1, 7000);
        when(refundDAO.selectCancelableItemsByOrder(1L, 100L))
                .thenReturn(List.of(first, second));
        when(refundDAO.selectNextCancelGroupNo()).thenReturn(900L);
        when(refundDAO.insertCancelRequest(any())).thenReturn(1);
        when(refundDAO.updateOrderItemCancelRequested(anyLong()))
                .thenReturn(1);

        service.requestOrderCancel(1L, 100L, " ");

        ArgumentCaptor<OrderCancelRefundVO> captor =
                ArgumentCaptor.forClass(OrderCancelRefundVO.class);
        verify(refundDAO, times(2))
                .insertCancelRequest(captor.capture());
        assertEquals("FULL", captor.getAllValues().get(0).getCancelType());
        assertEquals(10000L,
                captor.getAllValues().get(0).getRefundAmount());
        assertEquals("주문 전체 취소 요청",
                captor.getAllValues().get(0).getReason());
        verify(refundDAO).updateOrderStatusByItems(100L);
        verify(notificationService).createForCancelGroupBusinesses(
                900L,
                "REFUND_REQUESTED",
                "전체 취소/환불 요청",
                "주문번호 100의 전체 취소/환불 요청이 접수되었습니다.",
                "/business/cancel/list",
                "CANCEL",
                900L);
    }

    @Test
    void fullCancelRequestShouldRejectMissingItemsDuplicateAndWriteFailure() {
        assertThrows(IllegalArgumentException.class,
                () -> service.requestOrderCancel(1L, 0L, "사유"));

        when(refundDAO.selectCancelableItemsByOrder(1L, 100L))
                .thenReturn(List.of());
        assertThrows(IllegalArgumentException.class,
                () -> service.requestOrderCancel(1L, 100L, "사유"));

        OrderItemVO item = item(11L, 100L, 1001, 1, 1000);
        when(refundDAO.selectCancelableItemsByOrder(1L, 101L))
                .thenReturn(List.of(item));
        when(refundDAO.countWaitingCancelByOrderItemNo(11L)).thenReturn(1);
        assertThrows(IllegalArgumentException.class,
                () -> service.requestOrderCancel(1L, 101L, "사유"));

        when(refundDAO.selectCancelableItemsByOrder(1L, 102L))
                .thenReturn(List.of(item));
        when(refundDAO.countWaitingCancelByOrderItemNo(11L)).thenReturn(0);
        when(refundDAO.selectNextCancelGroupNo()).thenReturn(1L);
        when(refundDAO.insertCancelRequest(any())).thenReturn(0);
        assertThrows(IllegalStateException.class,
                () -> service.requestOrderCancel(1L, 102L, "사유"));
    }

    @Test
    void partialCancelRequestShouldPersistAndNotify() {
        OrderItemVO item = item(11L, 100L, 1001, 2, 5000);
        PaymentVO payment = payment(100L, "CARD", 10000L);
        when(refundDAO.selectCancelableItem(1L, 11L)).thenReturn(item);
        when(paymentDAO.selectPaymentByOrderNo(100L)).thenReturn(payment);
        when(refundDAO.selectNextCancelGroupNo()).thenReturn(901L);
        when(refundDAO.insertCancelRequest(any())).thenReturn(1);
        when(refundDAO.updateOrderItemCancelRequested(11L)).thenReturn(1);

        service.requestOrderItemCancel(1L, 11L, null);

        ArgumentCaptor<OrderCancelRefundVO> captor =
                ArgumentCaptor.forClass(OrderCancelRefundVO.class);
        verify(refundDAO).insertCancelRequest(captor.capture());
        assertEquals("PARTIAL", captor.getValue().getCancelType());
        assertEquals(10000L, captor.getValue().getRefundAmount());
        assertEquals("상품 부분 취소 요청", captor.getValue().getReason());
        verify(notificationService).createForCancelGroupBusinesses(
                901L,
                "REFUND_REQUESTED",
                "상품 취소/환불 요청",
                "주문번호 100의 상품 취소/환불 요청이 접수되었습니다.",
                "/business/cancel/list",
                "CANCEL",
                901L);
    }

    @Test
    void partialCancelRequestShouldRejectInvalidDuplicateMissingAndEasyPay() {
        assertThrows(IllegalArgumentException.class,
                () -> service.requestOrderItemCancel(1L, null, "사유"));

        when(refundDAO.countWaitingCancelByOrderItemNo(11L)).thenReturn(1);
        assertThrows(IllegalArgumentException.class,
                () -> service.requestOrderItemCancel(1L, 11L, "사유"));

        when(refundDAO.countWaitingCancelByOrderItemNo(12L)).thenReturn(0);
        when(refundDAO.selectCancelableItem(1L, 12L)).thenReturn(null);
        assertThrows(IllegalArgumentException.class,
                () -> service.requestOrderItemCancel(1L, 12L, "사유"));

        OrderItemVO item = item(13L, 100L, 1001, 1, 1000);
        when(refundDAO.selectCancelableItem(1L, 13L)).thenReturn(item);
        when(paymentDAO.selectPaymentByOrderNo(100L))
                .thenReturn(payment(100L, "KAKAO_PAY", 1000L));
        assertThrows(IllegalArgumentException.class,
                () -> service.requestOrderItemCancel(1L, 13L, "사유"));
        verify(refundDAO, never()).insertCancelRequest(any());
    }

    @Test
    void batchPartialRequestShouldRejectInvalidSelectionAndRemoveDuplicates() {
        assertThrows(IllegalArgumentException.class,
                () -> service.requestOrderItemsCancel(1L, List.of(), "사유"));
        List<Long> selectionWithNull =
                java.util.Arrays.asList(1L, null);
        assertThrows(IllegalArgumentException.class,
                () -> service.requestOrderItemsCancel(
                        1L,
                        selectionWithNull,
                        "사유"));
        List<Long> duplicateSelection = List.of(1L, 1L);
        assertThrows(IllegalArgumentException.class,
                () -> service.requestOrderItemsCancel(
                        1L,
                        duplicateSelection,
                        "사유"));
    }

    @Test
    void businessListShouldResolveBusinessAndNormalizeStatus() {
        BusinessVO business = business(50L);
        List<OrderCancelRefundVO> expected = List.of(new OrderCancelRefundVO());
        when(businessDAO.selectBusinessByMemberNo(5L)).thenReturn(business);
        when(refundDAO.selectCancelListByBusiness(50L, "WAITING"))
                .thenReturn(expected);

        assertSame(expected, service.getBusinessCancelList(5L, " waiting "));
        service.getBusinessCancelList(5L, "ALL");
        verify(refundDAO).selectCancelListByBusiness(50L, null);
    }

    @Test
    void approvePartialShouldRefundRestoreStockAndNotifyMember() {
        BusinessVO business = business(50L);
        OrderCancelRefundVO request = request(
                1L, 100L, 11L, 1001L, "PARTIAL", 900L, 5000L);
        request.setProductName(" 상품 ");
        PaymentVO payment = payment(100L, "CARD", 10000L);
        PaymentVO canceled = payment(100L, "CARD", 10000L);
        canceled.setCanceledAmount(5000L);

        when(businessDAO.selectBusinessByMemberNo(5L)).thenReturn(business);
        when(refundDAO.selectCancelRequestForBusiness(1L, 50L))
                .thenReturn(request);
        when(paymentDAO.selectPaymentByOrderNo(100L)).thenReturn(payment);
        when(paymentService.cancelPaidPaymentPartially(payment, 5000L, "사유"))
                .thenReturn(canceled);
        when(refundDAO.cancelOrderItem(11L)).thenReturn(1);
        when(refundDAO.restoreProductStock(1001L, 2)).thenReturn(1);
        when(refundDAO.approveCancelRequest(1L)).thenReturn(1);
        when(paymentDAO.updatePaymentPartialCanceled(canceled)).thenReturn(1);

        service.approveCancel(5L, 1L);

        verify(refundDAO).rejectSettlementByOrderItemNo(11L);
        verify(refundDAO).updateOrderStatusByItems(100L);
        verify(notificationService).createForMember(
                1L,
                "REFUND_COMPLETED",
                "환불이 완료되었습니다.",
                "‘상품’의 취소 및 결제 환불이 완료되었습니다.",
                "/order/list",
                "CANCEL",
                1L);
    }

    @Test
    void approveFullShouldWaitOrCompleteGroup() {
        BusinessVO business = business(50L);
        OrderCancelRefundVO request = request(
                1L, 100L, 11L, 1001L, "FULL", 900L, 10000L);
        when(businessDAO.selectBusinessByMemberNo(5L)).thenReturn(business);
        when(refundDAO.selectCancelRequestForBusiness(1L, 50L))
                .thenReturn(request);
        when(refundDAO.approveFullGroupForBusiness(900L, 50L)).thenReturn(1);
        when(refundDAO.countWaitingByGroup(900L)).thenReturn(1);

        service.approveCancel(5L, 1L);
        verify(paymentService, never()).cancelPaidPayment(any(), any());

        when(refundDAO.countWaitingByGroup(900L)).thenReturn(0);
        PaymentVO payment = payment(100L, "CARD", 10000L);
        PaymentVO canceled = payment(100L, "CARD", 10000L);
        when(paymentDAO.selectPaymentByOrderNo(100L)).thenReturn(payment);
        when(paymentService.cancelPaidPayment(payment, "사유"))
                .thenReturn(canceled);
        when(refundDAO.selectRequestsByGroup(900L))
                .thenReturn(List.of(request));
        when(refundDAO.restoreProductStock(1001L, 2)).thenReturn(1);
        when(refundDAO.cancelOrderItemsByGroup(900L)).thenReturn(1);
        when(paymentDAO.updatePaymentCanceled(canceled)).thenReturn(1);

        service.approveCancel(5L, 1L);

        assertEquals(10000L, canceled.getCanceledAmount());
        verify(refundDAO).rejectSettlementsByCancelGroupNo(900L);
        verify(notificationService).createForMember(
                1L,
                "REFUND_COMPLETED",
                "환불이 완료되었습니다.",
                "주문번호 100의 전체 취소 및 결제 환불이 완료되었습니다.",
                "/order/list",
                "CANCEL",
                900L);
    }

    @Test
    void approvalShouldRejectMissingBusinessRequestAndProcessedStatus() {
        when(businessDAO.selectBusinessByMemberNo(5L)).thenReturn(null);
        assertThrows(IllegalArgumentException.class,
                () -> service.approveCancel(5L, 1L));

        when(businessDAO.selectBusinessByMemberNo(6L)).thenReturn(business(60L));
        assertThrows(IllegalArgumentException.class,
                () -> service.approveCancel(6L, 0L));

        OrderCancelRefundVO processed = request(
                1L, 100L, 11L, 1001L, "PARTIAL", 1L, 1000L);
        processed.setStatus("APPROVED");
        when(refundDAO.selectCancelRequestForBusiness(2L, 60L))
                .thenReturn(processed);
        assertThrows(IllegalArgumentException.class,
                () -> service.approveCancel(6L, 2L));
    }

    @Test
    void rejectShouldHandleFullAndPartialRequests() {
        BusinessVO business = business(50L);
        when(businessDAO.selectBusinessByMemberNo(5L)).thenReturn(business);

        OrderCancelRefundVO full = request(
                1L, 100L, 11L, 1001L, "FULL", 900L, 10000L);
        when(refundDAO.selectCancelRequestForBusiness(1L, 50L))
                .thenReturn(full);
        when(refundDAO.rejectFullGroup(900L, "반려 사유")).thenReturn(2);

        service.rejectCancel(5L, 1L, " 반려 사유 ");
        verify(refundDAO).restoreOrderItemsByGroup(900L);

        OrderCancelRefundVO partial = request(
                2L, 101L, 12L, 1002L, "PARTIAL", 901L, 5000L);
        partial.setProductName(null);
        when(refundDAO.selectCancelRequestForBusiness(2L, 50L))
                .thenReturn(partial);
        when(refundDAO.rejectCancelRequest(2L, "사업자 사유로 취소 요청 반려"))
                .thenReturn(1);
        when(refundDAO.restoreOrderItemStatus(12L)).thenReturn(1);

        service.rejectCancel(5L, 2L, " ");
        verify(refundDAO).updateOrderStatusByItems(101L);
        verify(notificationService).createForMember(
                1L,
                "REFUND_REJECTED",
                "환불 요청이 반려되었습니다.",
                "주문번호 101의 취소/환불 요청이 반려되었습니다. 반려 사유: 사업자 사유로 취소 요청 반려",
                "/order/list",
                "CANCEL",
                2L);
    }

    @Test
    void reasonLengthAndMissingPaymentShouldBeRejected() {
        String tooLong = "가".repeat(501);
        OrderItemVO item = item(11L, 100L, 1001, 1, 1000);
        when(refundDAO.selectCancelableItem(1L, 11L)).thenReturn(item);
        when(paymentDAO.selectPaymentByOrderNo(100L))
                .thenReturn(payment(100L, "CARD", 1000L));
        assertThrows(IllegalArgumentException.class,
                () -> service.requestOrderItemCancel(1L, 11L, tooLong));

        when(refundDAO.selectCancelableItem(1L, 12L))
                .thenReturn(item(12L, 101L, 1001, 1, 1000));
        when(paymentDAO.selectPaymentByOrderNo(101L)).thenReturn(null);
        assertThrows(IllegalArgumentException.class,
                () -> service.requestOrderItemCancel(1L, 12L, "사유"));
    }

    private BusinessVO business(Long businessNo) {
        BusinessVO business = new BusinessVO();
        business.setBusinessNo(businessNo);
        return business;
    }

    private OrderItemVO item(
            Long orderItemNo,
            Long orderNo,
            Integer productNo,
            Integer quantity,
            Integer price) {
        OrderItemVO item = new OrderItemVO();
        item.setOrderItemNo(orderItemNo);
        item.setOrderNo(orderNo);
        item.setProductNo(productNo);
        item.setQuantity(quantity);
        item.setProductPrice(price);
        return item;
    }

    private PaymentVO payment(Long orderNo, String method, Long amount) {
        PaymentVO payment = new PaymentVO();
        payment.setOrderNo(orderNo);
        payment.setPayMethod(method);
        payment.setPaymentAmount(amount);
        payment.setCanceledAmount(0L);
        payment.setPaymentStatus("PAID");
        return payment;
    }

    private OrderCancelRefundVO request(
            Long cancelNo,
            Long orderNo,
            Long orderItemNo,
            Long productNo,
            String type,
            Long groupNo,
            Long refundAmount) {
        OrderCancelRefundVO request = new OrderCancelRefundVO();
        request.setCancelNo(cancelNo);
        request.setOrderNo(orderNo);
        request.setOrderItemNo(orderItemNo);
        request.setMemberNo(1L);
        request.setProductNo(productNo);
        request.setQuantity(2);
        request.setCancelType(type);
        request.setCancelGroupNo(groupNo);
        request.setRefundAmount(refundAmount);
        request.setReason("사유");
        request.setStatus("WAITING");
        return request;
    }
}
