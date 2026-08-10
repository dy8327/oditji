package com.project.oditji.refund.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.Month;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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

/**
 * OrderCancelRefundServiceImpl에서 기존 테스트가 지나가지 않은 조건/예외 분기를 보완합니다.
 */
@ExtendWith(MockitoExtension.class)
class OrderCancelRefundServiceImplRemainingCoverageTest {

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
    void memberHistoryShouldCoverNullBlankAllAndRemainingValidFilters() {
        LocalDate startDate = LocalDate.of(2026, Month.AUGUST, 1);
        LocalDate endDate = LocalDate.of(2026, Month.AUGUST, 10);

        service.getMemberCancelRefundHistory(1L, null, null, startDate, null);
        service.getMemberCancelRefundHistory(1L, "   ", "ALL", null, endDate);
        service.getMemberCancelRefundHistory(1L, "refund", "waiting", startDate, endDate);
        service.getMemberCancelRefundHistory(1L, "cancel", "rejected", startDate, endDate);

        verify(refundDAO).selectMemberCancelRefundHistory(1L, null, null, startDate, null);
        verify(refundDAO).selectMemberCancelRefundHistory(1L, null, null, null, endDate);
        verify(refundDAO).selectMemberCancelRefundHistory(1L, "REFUND", "WAITING", startDate, endDate);
        verify(refundDAO).selectMemberCancelRefundHistory(1L, "CANCEL", "REJECTED", startDate, endDate);
    }

    @Test
    void memberValidationShouldRejectZeroMemberNumber() {
        Long invalidMemberNo = 0L;

        assertThrows(
                IllegalArgumentException.class,
                () -> service.getMemberCancelRefundHistory(
                        invalidMemberNo,
                        null,
                        null,
                        null,
                        null));
    }

    @Test
    void fullCancelShouldRejectNullOrderNullItemsAndTooLongReason() {
        Long nullOrderNo = null;
        String normalReason = "사유";
        String tooLongReason = "가".repeat(501);

        assertThrows(
                IllegalArgumentException.class,
                () -> service.requestOrderCancel(1L, nullOrderNo, normalReason));

        when(refundDAO.selectCancelableItemsByOrder(1L, 100L)).thenReturn(null);
        assertThrows(
                IllegalArgumentException.class,
                () -> service.requestOrderCancel(1L, 100L, normalReason));

        assertThrows(
                IllegalArgumentException.class,
                () -> service.requestOrderCancel(1L, 101L, tooLongReason));
    }

    @Test
    void fullCancelShouldFailWhenOrderItemStatusUpdateFails() {
        OrderItemVO item = item(11L, 100L, 1001, 1, 1000);
        when(refundDAO.selectCancelableItemsByOrder(1L, 100L)).thenReturn(List.of(item));
        when(refundDAO.selectNextCancelGroupNo()).thenReturn(900L);
        when(refundDAO.insertCancelRequest(any(OrderCancelRefundVO.class))).thenReturn(1);
        when(refundDAO.updateOrderItemCancelRequested(11L)).thenReturn(0);

        assertThrows(
                IllegalStateException.class,
                () -> service.requestOrderCancel(1L, 100L, "사유"));

        verify(refundDAO, never()).updateOrderStatusByItems(100L);
    }

    @Test
    void batchCancelShouldRejectNullAndNonPositiveSelections() {
        List<Long> nullSelection = null;
        List<Long> zeroSelection = List.of(0L);
        List<Long> negativeSelection = List.of(-1L);

        assertThrows(
                IllegalArgumentException.class,
                () -> service.requestOrderItemsCancel(1L, nullSelection, "사유"));
        assertThrows(
                IllegalArgumentException.class,
                () -> service.requestOrderItemsCancel(1L, zeroSelection, "사유"));
        assertThrows(
                IllegalArgumentException.class,
                () -> service.requestOrderItemsCancel(1L, negativeSelection, "사유"));
    }

    @Test
    void batchCancelShouldProcessDistinctItemsWhenTestModeIsDisabled() {
        ReflectionTestUtils.setField(service, "portOneTestMode", false);

        OrderItemVO first = item(11L, 100L, 1001, 1, 1000);
        OrderItemVO second = item(12L, 101L, 1002, 2, 2000);
        PaymentVO easyPay = payment(100L, "KAKAO_PAY", 1000L);
        PaymentVO anotherEasyPay = payment(101L, "NAVER_PAY", 4000L);
        List<Long> selectedItems = List.of(11L, 12L);

        when(refundDAO.selectCancelableItem(1L, 11L)).thenReturn(first);
        when(refundDAO.selectCancelableItem(1L, 12L)).thenReturn(second);
        when(paymentDAO.selectPaymentByOrderNo(100L)).thenReturn(easyPay);
        when(paymentDAO.selectPaymentByOrderNo(101L)).thenReturn(anotherEasyPay);
        when(refundDAO.selectNextCancelGroupNo()).thenReturn(901L, 902L);
        when(refundDAO.insertCancelRequest(any(OrderCancelRefundVO.class))).thenReturn(1);
        when(refundDAO.updateOrderItemCancelRequested(11L)).thenReturn(1);
        when(refundDAO.updateOrderItemCancelRequested(12L)).thenReturn(1);

        service.requestOrderItemsCancel(1L, selectedItems, "사유");

        verify(refundDAO, times(2)).insertCancelRequest(any(OrderCancelRefundVO.class));
        verify(refundDAO).updateOrderStatusByItems(100L);
        verify(refundDAO).updateOrderStatusByItems(101L);
        verify(notificationService, times(2)).createForCancelGroupBusinesses(
                any(Long.class),
                any(String.class),
                any(String.class),
                any(String.class),
                any(String.class),
                any(String.class),
                any(Long.class));
    }

    @Test
    void partialCancelShouldRejectZeroItemNumber() {
        Long invalidOrderItemNo = 0L;

        assertThrows(
                IllegalArgumentException.class,
                () -> service.requestOrderItemCancel(1L, invalidOrderItemNo, "사유"));
    }

    @Test
    void partialCancelShouldFailWhenRequestInsertFails() {
        OrderItemVO item = item(11L, 100L, 1001, 1, 1000);
        PaymentVO payment = payment(100L, "CARD", 1000L);
        when(refundDAO.selectCancelableItem(1L, 11L)).thenReturn(item);
        when(paymentDAO.selectPaymentByOrderNo(100L)).thenReturn(payment);
        when(refundDAO.selectNextCancelGroupNo()).thenReturn(900L);
        when(refundDAO.insertCancelRequest(any(OrderCancelRefundVO.class))).thenReturn(0);

        assertThrows(
                IllegalStateException.class,
                () -> service.requestOrderItemCancel(1L, 11L, "사유"));
    }

    @Test
    void partialCancelShouldFailWhenOrderItemStatusUpdateFails() {
        OrderItemVO item = item(11L, 100L, 1001, 1, 1000);
        PaymentVO payment = payment(100L, "CARD", 1000L);
        when(refundDAO.selectCancelableItem(1L, 11L)).thenReturn(item);
        when(paymentDAO.selectPaymentByOrderNo(100L)).thenReturn(payment);
        when(refundDAO.selectNextCancelGroupNo()).thenReturn(900L);
        when(refundDAO.insertCancelRequest(any(OrderCancelRefundVO.class))).thenReturn(1);
        when(refundDAO.updateOrderItemCancelRequested(11L)).thenReturn(0);

        assertThrows(
                IllegalStateException.class,
                () -> service.requestOrderItemCancel(1L, 11L, "사유"));

        verify(refundDAO, never()).updateOrderStatusByItems(100L);
    }

    @Test
    void businessListCountShouldNormalizeNullBlankAllAndExplicitStatus() {
        BusinessVO business = business(50L);
        when(businessDAO.selectBusinessByMemberNo(5L)).thenReturn(business);
        when(refundDAO.selectCancelListByBusinessCount(50L, null)).thenReturn(0);
        when(refundDAO.selectCancelListByBusinessCount(50L, "APPROVED")).thenReturn(3);

        assertEquals(0, service.getBusinessCancelListCount(5L, null));
        assertEquals(0, service.getBusinessCancelListCount(5L, "   "));
        assertEquals(0, service.getBusinessCancelListCount(5L, "ALL"));
        assertEquals(3, service.getBusinessCancelListCount(5L, " approved "));

        verify(refundDAO, times(3)).selectCancelListByBusinessCount(50L, null);
        verify(refundDAO).selectCancelListByBusinessCount(50L, "APPROVED");
    }

    @Test
    void businessListShouldCoverNullStatusAndNonPositivePageArguments() {
        BusinessVO business = business(50L);
        when(businessDAO.selectBusinessByMemberNo(5L)).thenReturn(business);

        service.getBusinessCancelList(5L, null, 0, 0);

        verify(refundDAO).selectCancelListByBusiness(50L, null, 0, 0);
    }

    @Test
    void approveShouldRejectMissingRequest() {
        BusinessVO business = business(50L);
        when(businessDAO.selectBusinessByMemberNo(5L)).thenReturn(business);
        when(refundDAO.selectCancelRequestForBusiness(1L, 50L)).thenReturn(null);

        assertThrows(
                IllegalArgumentException.class,
                () -> service.approveCancel(5L, 1L));
    }

    @Test
    void approveFullShouldFailWhenBusinessApprovalUpdateFails() {
        OrderCancelRefundVO request = fullRequest();
        stubWaitingRequest(request);
        when(refundDAO.approveFullGroupForBusiness(900L, 50L)).thenReturn(0);

        assertThrows(
                IllegalStateException.class,
                () -> service.approveCancel(5L, 1L));
    }

    @Test
    void approveFullShouldRejectAlreadyRejectedGroup() {
        OrderCancelRefundVO request = fullRequest();
        stubWaitingRequest(request);
        when(refundDAO.approveFullGroupForBusiness(900L, 50L)).thenReturn(1);
        when(refundDAO.countRejectedByGroup(900L)).thenReturn(1);

        assertThrows(
                IllegalArgumentException.class,
                () -> service.approveCancel(5L, 1L));

        verify(paymentService, never()).cancelPaidPayment(any(PaymentVO.class), any(String.class));
    }

    @Test
    void approveFullShouldFailWhenStockRestoreFails() {
        OrderCancelRefundVO request = fullRequest();
        PaymentVO payment = payment(100L, "CARD", 10000L);
        PaymentVO canceledPayment = payment(100L, "CARD", 10000L);
        stubFullApprovalUntilGroupCompletion(request, payment, canceledPayment);
        when(refundDAO.selectRequestsByGroup(900L)).thenReturn(List.of(request));
        when(refundDAO.restoreProductStock(1001L, 2)).thenReturn(0);

        assertThrows(
                IllegalStateException.class,
                () -> service.approveCancel(5L, 1L));
    }

    @Test
    void approveFullShouldFailWhenCanceledItemCountDoesNotMatchGroupSize() {
        OrderCancelRefundVO request = fullRequest();
        PaymentVO payment = payment(100L, "CARD", 10000L);
        PaymentVO canceledPayment = payment(100L, "CARD", 10000L);
        stubFullApprovalUntilGroupCompletion(request, payment, canceledPayment);
        when(refundDAO.selectRequestsByGroup(900L)).thenReturn(List.of(request));
        when(refundDAO.restoreProductStock(1001L, 2)).thenReturn(1);
        when(refundDAO.cancelOrderItemsByGroup(900L)).thenReturn(0);

        assertThrows(
                IllegalStateException.class,
                () -> service.approveCancel(5L, 1L));
    }

    @Test
    void approveFullShouldFailWhenCanceledPaymentPersistenceFails() {
        OrderCancelRefundVO request = fullRequest();
        PaymentVO payment = payment(100L, "CARD", 10000L);
        PaymentVO canceledPayment = payment(100L, "CARD", 10000L);
        stubFullApprovalUntilGroupCompletion(request, payment, canceledPayment);
        when(refundDAO.selectRequestsByGroup(900L)).thenReturn(List.of(request));
        when(refundDAO.restoreProductStock(1001L, 2)).thenReturn(1);
        when(refundDAO.cancelOrderItemsByGroup(900L)).thenReturn(1);
        when(paymentDAO.updatePaymentCanceled(canceledPayment)).thenReturn(0);

        assertThrows(
                IllegalStateException.class,
                () -> service.approveCancel(5L, 1L));

        verify(refundDAO).rejectSettlementsByCancelGroupNo(900L);
    }

    @Test
    void approvePartialShouldRejectNullAndZeroRefundAmount() {
        OrderCancelRefundVO nullAmountRequest = partialRequest(1L, null);
        OrderCancelRefundVO zeroAmountRequest = partialRequest(2L, 0L);
        PaymentVO payment = payment(100L, "CARD", 10000L);

        when(businessDAO.selectBusinessByMemberNo(5L)).thenReturn(business(50L));
        when(refundDAO.selectCancelRequestForBusiness(1L, 50L)).thenReturn(nullAmountRequest);
        when(refundDAO.selectCancelRequestForBusiness(2L, 50L)).thenReturn(zeroAmountRequest);
        when(paymentDAO.selectPaymentByOrderNo(100L)).thenReturn(payment);

        assertThrows(
                IllegalArgumentException.class,
                () -> service.approveCancel(5L, 1L));
        assertThrows(
                IllegalArgumentException.class,
                () -> service.approveCancel(5L, 2L));
    }

    @Test
    void approvePartialShouldFailWhenOrderItemCancellationFails() {
        OrderCancelRefundVO request = partialRequest(1L, 5000L);
        PaymentVO payment = payment(100L, "CARD", 10000L);
        PaymentVO canceledPayment = payment(100L, "CARD", 10000L);
        stubPartialApprovalStart(request, payment, canceledPayment);
        when(refundDAO.cancelOrderItem(11L)).thenReturn(0);

        assertThrows(
                IllegalStateException.class,
                () -> service.approveCancel(5L, 1L));
    }

    @Test
    void approvePartialShouldFailWhenStockRestoreFails() {
        OrderCancelRefundVO request = partialRequest(1L, 5000L);
        PaymentVO payment = payment(100L, "CARD", 10000L);
        PaymentVO canceledPayment = payment(100L, "CARD", 10000L);
        stubPartialApprovalStart(request, payment, canceledPayment);
        when(refundDAO.cancelOrderItem(11L)).thenReturn(1);
        when(refundDAO.restoreProductStock(1001L, 2)).thenReturn(0);

        assertThrows(
                IllegalStateException.class,
                () -> service.approveCancel(5L, 1L));

        verify(refundDAO).rejectSettlementByOrderItemNo(11L);
    }

    @Test
    void approvePartialShouldFailWhenApprovalPersistenceFails() {
        OrderCancelRefundVO request = partialRequest(1L, 5000L);
        PaymentVO payment = payment(100L, "CARD", 10000L);
        PaymentVO canceledPayment = payment(100L, "CARD", 10000L);
        stubPartialApprovalStart(request, payment, canceledPayment);
        when(refundDAO.cancelOrderItem(11L)).thenReturn(1);
        when(refundDAO.restoreProductStock(1001L, 2)).thenReturn(1);
        when(refundDAO.approveCancelRequest(1L)).thenReturn(0);

        assertThrows(
                IllegalStateException.class,
                () -> service.approveCancel(5L, 1L));
    }

    @Test
    void approvePartialShouldFailWhenPaymentPersistenceFails() {
        OrderCancelRefundVO request = partialRequest(1L, 5000L);
        PaymentVO payment = payment(100L, "CARD", 10000L);
        PaymentVO canceledPayment = payment(100L, "CARD", 10000L);
        stubPartialApprovalStart(request, payment, canceledPayment);
        when(refundDAO.cancelOrderItem(11L)).thenReturn(1);
        when(refundDAO.restoreProductStock(1001L, 2)).thenReturn(1);
        when(refundDAO.approveCancelRequest(1L)).thenReturn(1);
        when(paymentDAO.updatePaymentPartialCanceled(canceledPayment)).thenReturn(0);

        assertThrows(
                IllegalStateException.class,
                () -> service.approveCancel(5L, 1L));
    }

    @Test
    void rejectFullShouldFailWhenGroupRejectUpdateFails() {
        OrderCancelRefundVO request = fullRequest();
        stubWaitingRequest(request);
        when(refundDAO.rejectFullGroup(900L, "반려 사유")).thenReturn(0);

        assertThrows(
                IllegalStateException.class,
                () -> service.rejectCancel(5L, 1L, "반려 사유"));
    }

    @Test
    void rejectPartialShouldFailWhenRejectRequestUpdateFails() {
        OrderCancelRefundVO request = partialRequest(1L, 5000L);
        stubWaitingRequest(request);
        when(refundDAO.rejectCancelRequest(1L, "반려 사유")).thenReturn(0);

        assertThrows(
                IllegalStateException.class,
                () -> service.rejectCancel(5L, 1L, "반려 사유"));
    }

    @Test
    void rejectPartialShouldFailWhenOrderItemRestoreFails() {
        OrderCancelRefundVO request = partialRequest(1L, 5000L);
        stubWaitingRequest(request);
        when(refundDAO.rejectCancelRequest(1L, "반려 사유")).thenReturn(1);
        when(refundDAO.restoreOrderItemStatus(11L)).thenReturn(0);

        assertThrows(
                IllegalStateException.class,
                () -> service.rejectCancel(5L, 1L, "반려 사유"));
    }

    @Test
    void rejectPartialShouldUseOrderNumberWhenProductNameIsBlank() {
        OrderCancelRefundVO request = partialRequest(1L, 5000L);
        request.setProductName("   ");
        stubWaitingRequest(request);
        when(refundDAO.rejectCancelRequest(1L, "반려 사유")).thenReturn(1);
        when(refundDAO.restoreOrderItemStatus(11L)).thenReturn(1);

        service.rejectCancel(5L, 1L, "반려 사유");

        verify(notificationService).createForMember(
                1L,
                "REFUND_REJECTED",
                "환불 요청이 반려되었습니다.",
                "주문번호 100의 취소/환불 요청이 반려되었습니다. 반려 사유: 반려 사유",
                "/order/list",
                "CANCEL",
                1L);
    }

    @Test
    void easyPayDetectionShouldCoverNullBlankAndEverySupportedAlias() {
        PaymentVO nullMethod = payment(1L, null, 1000L);
        PaymentVO blankMethod = payment(1L, "   ", 1000L);
        PaymentVO easyPay = payment(1L, "easy-pay", 1000L);
        PaymentVO kakaoPay = payment(1L, "KAKAO PAY", 1000L);
        PaymentVO naverPay = payment(1L, "naver_pay", 1000L);
        PaymentVO tossPay = payment(1L, "toss-pay", 1000L);
        PaymentVO payco = payment(1L, "payco", 1000L);
        PaymentVO samsungPay = payment(1L, "samsung pay", 1000L);
        PaymentVO ssgPay = payment(1L, "ssg_pay", 1000L);
        PaymentVO lPay = payment(1L, "l-pay", 1000L);
        PaymentVO card = payment(1L, "CARD", 1000L);

        assertFalse(invokeIsEasyPay(null));
        assertFalse(invokeIsEasyPay(nullMethod));
        assertFalse(invokeIsEasyPay(blankMethod));
        assertTrue(invokeIsEasyPay(easyPay));
        assertTrue(invokeIsEasyPay(kakaoPay));
        assertTrue(invokeIsEasyPay(naverPay));
        assertTrue(invokeIsEasyPay(tossPay));
        assertTrue(invokeIsEasyPay(payco));
        assertTrue(invokeIsEasyPay(samsungPay));
        assertTrue(invokeIsEasyPay(ssgPay));
        assertTrue(invokeIsEasyPay(lPay));
        assertFalse(invokeIsEasyPay(card));
    }

    @Test
    void partialRequestShouldBlockRemainingEasyPayAliasesInTestMode() {
        List<String> methods = Arrays.asList(
                "EASY_PAY",
                "NAVER_PAY",
                "TOSS_PAY",
                "PAYCO",
                "SAMSUNG_PAY",
                "SSG_PAY",
                "LPAY");

        long orderItemNo = 20L;
        long orderNo = 200L;
        for (String method : methods) {
            OrderItemVO item = item(orderItemNo, orderNo, 1001, 1, 1000);
            PaymentVO payment = payment(orderNo, method, 1000L);
            when(refundDAO.selectCancelableItem(1L, orderItemNo)).thenReturn(item);
            when(paymentDAO.selectPaymentByOrderNo(orderNo)).thenReturn(payment);

            Long currentOrderItemNo = orderItemNo;
            assertThrows(
                    IllegalArgumentException.class,
                    () -> service.requestOrderItemCancel(1L, currentOrderItemNo, "사유"));

            orderItemNo++;
            orderNo++;
        }

        verify(refundDAO, never()).insertCancelRequest(any(OrderCancelRefundVO.class));
    }

    private boolean invokeIsEasyPay(PaymentVO payment) {
        Boolean result = ReflectionTestUtils.invokeMethod(service, "isEasyPay", payment);
        return Boolean.TRUE.equals(result);
    }

    private void stubWaitingRequest(OrderCancelRefundVO request) {
        when(businessDAO.selectBusinessByMemberNo(5L)).thenReturn(business(50L));
        when(refundDAO.selectCancelRequestForBusiness(request.getCancelNo(), 50L)).thenReturn(request);
    }

    private void stubFullApprovalUntilGroupCompletion(
            OrderCancelRefundVO request,
            PaymentVO payment,
            PaymentVO canceledPayment) {
        stubWaitingRequest(request);
        when(refundDAO.approveFullGroupForBusiness(900L, 50L)).thenReturn(1);
        when(refundDAO.countWaitingByGroup(900L)).thenReturn(0);
        when(paymentDAO.selectPaymentByOrderNo(100L)).thenReturn(payment);
        when(paymentService.cancelPaidPayment(payment, "사유")).thenReturn(canceledPayment);
    }

    private void stubPartialApprovalStart(
            OrderCancelRefundVO request,
            PaymentVO payment,
            PaymentVO canceledPayment) {
        stubWaitingRequest(request);
        when(paymentDAO.selectPaymentByOrderNo(100L)).thenReturn(payment);
        when(paymentService.cancelPaidPaymentPartially(payment, 5000L, "사유"))
                .thenReturn(canceledPayment);
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

    private OrderCancelRefundVO fullRequest() {
        return request(1L, 100L, 11L, 1001L, "FULL", 900L, 10000L);
    }

    private OrderCancelRefundVO partialRequest(Long cancelNo, Long refundAmount) {
        return request(cancelNo, 100L, 11L, 1001L, "PARTIAL", 901L, refundAmount);
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
