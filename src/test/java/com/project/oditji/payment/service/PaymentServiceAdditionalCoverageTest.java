package com.project.oditji.payment.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientResponseException;

import com.project.oditji.payment.dao.PaymentDAO;
import com.project.oditji.payment.vo.PaymentVO;

/**
 * 포트원 중복 취소 판별과 취소 결과 변환 등 남은 보조 분기를 검증합니다.
 */
class PaymentServiceAdditionalCoverageTest {

    private PaymentServiceImpl paymentService;

    @BeforeEach
    void setUp() {
        paymentService = new PaymentServiceImpl(mock(PaymentDAO.class));
    }

    @Test
    void alreadyCanceledResponseShouldRecognizeSupportedResponseShapes() {
        assertFalse((Boolean) invokePrivate(
                "isAlreadyCanceledResponse",
                (Object) null));

        assertFalse((Boolean) invokePrivate(
                "isAlreadyCanceledResponse",
                responseException(" ")));

        assertTrue((Boolean) invokePrivate(
                "isAlreadyCanceledResponse",
                responseException("payment_already_cancelled")));
        assertTrue((Boolean) invokePrivate(
                "isAlreadyCanceledResponse",
                responseException("already_canceled")));
        assertTrue((Boolean) invokePrivate(
                "isAlreadyCanceledResponse",
                responseException("{\"pgCode\":\"8023\"}")));
        assertTrue((Boolean) invokePrivate(
                "isAlreadyCanceledResponse",
                responseException("기승인 취소된 거래")));
        assertFalse((Boolean) invokePrivate(
                "isAlreadyCanceledResponse",
                responseException("temporary gateway error")));
    }

    @Test
    void createCanceledPaymentShouldCopyIdentityAndUsePortOneTimestamp() {
        PaymentVO original = new PaymentVO();
        original.setPaymentNo(10L);
        original.setOrderNo(20L);
        original.setPaymentId("pay-10");
        original.setPaymentAmount(30000L);

        PaymentVO result = invokePrivate(
                "createCanceledPaymentVO",
                original,
                Map.of("statusChangedAt", "2026-08-05T15:00:00+09:00"),
                "사용자 취소");

        assertEquals(10L, result.getPaymentNo());
        assertEquals(20L, result.getOrderNo());
        assertEquals("pay-10", result.getPaymentId());
        assertEquals(30000L, result.getPaymentAmount());
        assertEquals(30000L, result.getCanceledAmount());
        assertEquals("CANCELED", result.getPaymentStatus());
        assertEquals("2026-08-05T15:00:00+09:00", result.getCanceledAt());
        assertEquals("사용자 취소", result.getCancelReason());
    }

    @Test
    void cancelableValidationShouldAcceptPaidAndPartialCanceledStatuses() {
        PaymentVO paid = payment("pay-paid", "PAID");
        PaymentVO partial = payment("pay-partial", "PARTIAL_CANCELED");

        assertDoesNotThrow(() -> invokePrivate("validateCancelablePayment", paid));
        assertDoesNotThrow(() -> invokePrivate("validateCancelablePayment", partial));
    }

    @Test
    void portOneExceptionShouldPreserveMessageAndCause() {
        RestClientResponseException cause = responseException("gateway failure");

        IllegalStateException result = invokePrivate(
                "createPortOneException",
                "포트원 요청 실패",
                cause);

        assertEquals("포트원 요청 실패", result.getMessage());
        assertSame(cause, result.getCause());
    }

    @Test
    void fullyCanceledCheckShouldRejectInvalidExpectedAmounts() {
        assertFalse((Boolean) invokePrivate(
                "isFullyCanceledPayment",
                Map.of("status", "PAID"),
                (Long) null));
        assertFalse((Boolean) invokePrivate(
                "isFullyCanceledPayment",
                Map.of("status", "PAID"),
                0L));
        assertFalse((Boolean) invokePrivate(
                "isFullyCanceledPayment",
                Map.of("status", "PAID"),
                -1L));
        assertTrue((Boolean) invokePrivate(
                "isFullyCanceledPayment",
                Map.of("status", "CANCELLED"),
                (Long) null));
    }

    private PaymentVO payment(String paymentId, String status) {
        PaymentVO payment = new PaymentVO();
        payment.setPaymentId(paymentId);
        payment.setPaymentStatus(status);
        payment.setPaymentAmount(1000L);
        payment.setCanceledAmount(0L);
        return payment;
    }

    private RestClientResponseException responseException(String body) {
        return HttpServerErrorException.create(
                HttpStatus.BAD_GATEWAY,
                "Bad Gateway",
                HttpHeaders.EMPTY,
                body.getBytes(StandardCharsets.UTF_8),
                StandardCharsets.UTF_8);
    }

    @SuppressWarnings("unchecked")
    private <T> T invokePrivate(String methodName, Object... arguments) {
        return (T) ReflectionTestUtils.invokeMethod(
                paymentService,
                methodName,
                arguments);
    }
}
