package com.project.oditji.payment.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.project.oditji.payment.dao.PaymentDAO;
import com.project.oditji.payment.vo.PaymentVO;

/**
 * 결제 입력 검증, DAO 조회 및 포트원 응답 파싱 보조 로직을 검증합니다.
 */
@ExtendWith(MockitoExtension.class)
class PaymentServiceImplTest {

    @Mock
    private PaymentDAO paymentDAO;

    private PaymentServiceImpl paymentService;

    @BeforeEach
    void setUp() {
        paymentService = new PaymentServiceImpl(paymentDAO);
    }

    @Test
    void verifyPaidPaymentShouldRejectMissingOrLongPaymentId() {
        assertThrows(
                IllegalArgumentException.class,
                () -> paymentService.verifyPaidPayment(null, 1000L, "주문"));
        assertThrows(
                IllegalArgumentException.class,
                () -> paymentService.verifyPaidPayment(" ", 1000L, "주문"));
        assertThrows(
                IllegalArgumentException.class,
                () -> paymentService.verifyPaidPayment("A".repeat(101), 1000L, "주문"));

        verify(paymentDAO, never()).selectPaymentByPaymentId(org.mockito.ArgumentMatchers.anyString());
    }

    @Test
    void verifyPaidPaymentShouldRejectInvalidExpectedAmount() {
        assertThrows(
                IllegalArgumentException.class,
                () -> paymentService.verifyPaidPayment("pay-1", null, "주문"));
        assertThrows(
                IllegalArgumentException.class,
                () -> paymentService.verifyPaidPayment("pay-1", 0L, "주문"));
        assertThrows(
                IllegalArgumentException.class,
                () -> paymentService.verifyPaidPayment("pay-1", -1L, "주문"));

        verify(paymentDAO, never()).selectPaymentByPaymentId("pay-1");
    }

    @Test
    void verifyPaidPaymentShouldRejectAlreadyProcessedPayment() {
        PaymentVO existing = new PaymentVO();
        when(paymentDAO.selectPaymentByPaymentId("pay-1"))
                .thenReturn(existing);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> paymentService.verifyPaidPayment("pay-1", 1000L, "주문"));

        assertEquals("이미 처리된 결제입니다.", exception.getMessage());
    }

    @Test
    void getPaymentByPaymentIdShouldValidateAndDelegate() {
        PaymentVO payment = new PaymentVO();
        when(paymentDAO.selectPaymentByPaymentId("pay-2"))
                .thenReturn(payment);

        assertSame(payment, paymentService.getPaymentByPaymentId("pay-2"));
        assertThrows(
                IllegalArgumentException.class,
                () -> paymentService.getPaymentByPaymentId(" "));
    }

    @Test
    void getPaymentListShouldReturnDaoListOrEmptyList() {
        List<PaymentVO> list = List.of(new PaymentVO());
        when(paymentDAO.selectPaymentList())
                .thenReturn(list)
                .thenReturn(null);

        assertSame(list, paymentService.getPaymentList());
        assertTrue(paymentService.getPaymentList().isEmpty());
    }

    @Test
    void cancelPaidPaymentShouldRejectInvalidPaymentStateBeforeApiCall() {
        assertEquals(
                "결제내역을 찾을 수 없습니다.",
                assertThrows(
                        IllegalArgumentException.class,
                        () -> paymentService.cancelPaidPayment(null, "사유"))
                        .getMessage());

        PaymentVO missingId = createPayment(null, 1000L, 0L, "PAID");
        assertThrows(
                IllegalArgumentException.class,
                () -> paymentService.cancelPaidPayment(missingId, "사유"));

        PaymentVO canceled = createPayment("pay-3", 1000L, 1000L, "CANCELED");
        assertEquals(
                "이미 취소된 결제입니다.",
                assertThrows(
                        IllegalArgumentException.class,
                        () -> paymentService.cancelPaidPayment(canceled, "사유"))
                        .getMessage());

        PaymentVO ready = createPayment("pay-4", 1000L, 0L, "READY");
        assertEquals(
                "결제 완료 또는 부분 취소 상태의 결제만 취소할 수 있습니다.",
                assertThrows(
                        IllegalArgumentException.class,
                        () -> paymentService.cancelPaidPayment(ready, "사유"))
                        .getMessage());
    }

    @Test
    void cancelPaidPaymentPartiallyShouldRejectInvalidArgumentsBeforeApiCall() {
        assertThrows(
                IllegalArgumentException.class,
                () -> paymentService.cancelPaidPaymentPartially(null, 100L, "사유"));

        PaymentVO payment = createPayment("pay-5", 1000L, 0L, "PAID");
        assertThrows(
                IllegalArgumentException.class,
                () -> paymentService.cancelPaidPaymentPartially(payment, null, "사유"));
        assertThrows(
                IllegalArgumentException.class,
                () -> paymentService.cancelPaidPaymentPartially(payment, 0L, "사유"));
        assertThrows(
                IllegalArgumentException.class,
                () -> paymentService.cancelPaidPaymentPartially(payment, 1001L, "사유"));

        PaymentVO canceled = createPayment("pay-6", 1000L, 1000L, "CANCELED");
        assertThrows(
                IllegalArgumentException.class,
                () -> paymentService.cancelPaidPaymentPartially(canceled, 1L, "사유"));
    }

    @Test
    void extractPaidAmountShouldSupportNestedAndFlatAmounts() {
        assertEquals(
                1500L,
                ((Long) invokePrivate(
                        "extractPaidAmount",
                        Map.of("amount", Map.of("total", 1500)))).longValue());

        assertEquals(
                2500L,
                ((Long) invokePrivate(
                        "extractPaidAmount",
                        Map.of("totalAmount", "2500"))).longValue());

        assertThrows(
                IllegalStateException.class,
                () -> invokePrivate(
                        "extractPaidAmount",
                        Map.of("amount", Map.of("total", "invalid"))));
    }

    @Test
    void paymentMethodAndPgProviderShouldUseFallbackValues() {
        assertEquals(
                "CARD",
                invokePrivate(
                        "extractPayMethod",
                        Map.of()));
        assertEquals(
                "EASY_PAY",
                invokePrivate(
                        "extractPayMethod",
                        Map.of("method", Map.of("type", "EASY_PAY"))));
        assertEquals(
                "KAKAOPAY",
                invokePrivate(
                        "extractPayMethod",
                        Map.of("method", Map.of("provider", "KAKAOPAY"))));

        assertEquals(
                "INICIS",
                invokePrivate(
                        "extractPgProvider",
                        Map.of()));
        assertEquals(
                "NICE",
                invokePrivate(
                        "extractPgProvider",
                        Map.of("channel", Map.of("pgProvider", "NICE"))));
        assertEquals(
                "LIVE",
                invokePrivate(
                        "extractPgProvider",
                        Map.of("channel", Map.of("type", "LIVE"))));
    }

    @Test
    void transactionIdExtractionShouldPreferPgTransactionId() {
        assertEquals(
                "pg-1",
                invokePrivate(
                        "extractPgTxId",
                        Map.of("pgTxId", "pg-1", "transactionId", "tx-1")));
        assertEquals(
                "tx-2",
                invokePrivate(
                        "extractPgTxId",
                        Map.of("pgTxId", " ", "transactionId", "tx-2")));
        assertNull(
                invokePrivate(
                        "extractPgTxId",
                        Map.of()));
    }

    @Test
    void canceledAtExtractionShouldUseLatestCancellationOrStatusChangedAt() {
        Map<String, Object> first = Map.of("canceledAt", "2026-08-01T10:00:00+09:00");
        Map<String, Object> latest = Map.of("cancelledAt", "2026-08-02T10:00:00+09:00");

        assertEquals(
                "2026-08-02T10:00:00+09:00",
                invokePrivate(
                        "extractCanceledAt",
                        Map.of("cancellations", List.of(first, latest))));
        assertEquals(
                "2026-08-03T10:00:00+09:00",
                invokePrivate(
                        "extractCanceledAt",
                        Map.of("statusChangedAt", "2026-08-03T10:00:00+09:00")));

        String generated = (String) invokePrivate(
                "extractCanceledAt",
                Map.of());
        assertFalse(generated.isBlank());
    }

    @Test
    void totalCanceledAmountShouldSupportDifferentPortOneShapes() {
        List<Object> cancellations = List.of(
                Map.of("totalAmount", 100),
                Map.of("amount", Map.of("total", "200")),
                Map.of("amount", 300),
                Map.of("amount", "invalid"),
                "invalid-entry");

        assertEquals(
                600L,
                ((Long) invokePrivate(
                        "extractTotalCanceledAmount",
                        Map.of("cancellations", cancellations))).longValue());
        assertEquals(
                0L,
                ((Long) invokePrivate(
                        "extractTotalCanceledAmount",
                        Map.of())).longValue());
    }

    @Test
    void fullyCanceledCheckShouldUseStatusOrAccumulatedAmount() {
        assertTrue((Boolean) invokePrivate(
                "isFullyCanceledPayment",
                Map.of("status", "CANCELED"),
                1000L));
        assertTrue((Boolean) invokePrivate(
                "isFullyCanceledPayment",
                Map.of(
                        "status", "PAID",
                        "cancellations", List.of(Map.of("totalAmount", 1000))),
                1000L));
        assertFalse((Boolean) invokePrivate(
                "isFullyCanceledPayment",
                Map.of("status", "PAID"),
                1000L));
        assertFalse((Boolean) invokePrivate(
                "isFullyCanceledPayment",
                Map.of(),
                1000L));
    }

    @Test
    void genericStringAndLongParsingShouldHandleNullAndInvalidValues() {
        assertNull(invokePrivate(
                "readString",
                null,
                "key"));
        assertNull(invokePrivate(
                "readString",
                Map.of("key", "null"),
                "key"));
        assertEquals(
                "123",
                invokePrivate(
                        "readString",
                        Map.of("key", 123),
                        "key"));

        assertEquals(
                10L,
                ((Long) invokePrivate(
                        "parseLong",
                        10)).longValue());
        assertEquals(
                20L,
                ((Long) invokePrivate(
                        "parseLong",
                        "20")).longValue());
        assertNull(invokePrivate(
                "parseLong",
                "invalid"));
        assertNull(invokePrivate(
                "parseLong",
                (Object) null));
    }

    @Test
    void cancelReasonNormalizationShouldTrimDefaultAndLimitLength() {
        assertEquals(
                "사용자 요청에 의한 결제 취소",
                invokePrivate(
                        "normalizeCancelReason",
                        (Object) null));
        assertEquals(
                "단순 변심",
                invokePrivate(
                        "normalizeCancelReason",
                        "  단순 변심  "));
        assertThrows(
                IllegalArgumentException.class,
                () -> invokePrivate(
                        "normalizeCancelReason",
                        "가".repeat(501)));
    }

    private PaymentVO createPayment(
            String paymentId,
            Long paymentAmount,
            Long canceledAmount,
            String status) {

        PaymentVO payment = new PaymentVO();
        payment.setPaymentNo(1L);
        payment.setOrderNo(2L);
        payment.setPaymentId(paymentId);
        payment.setPaymentAmount(paymentAmount);
        payment.setCanceledAmount(canceledAmount);
        payment.setPaymentStatus(status);
        return payment;
    }

    /**
     * Spring 테스트 유틸리티를 사용해 private 보조 메서드를 호출합니다.
     * 테스트 파일에는 직접적인 리플렉션 접근성 변경 코드를 두지 않습니다.
     *
     * @param methodName 호출할 private 메서드명
     * @param arguments 메서드에 전달할 인수
     * @return private 메서드 실행 결과
     */
    private Object invokePrivate(String methodName, Object... arguments) {
        return ReflectionTestUtils.invokeMethod(paymentService, methodName, arguments);
    }
}