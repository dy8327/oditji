package com.project.oditji.payment.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.project.oditji.payment.dao.PaymentDAO;

import static org.mockito.Mockito.mock;

/** 취소 응답 파싱의 빈 배열/스칼라 amount 잔여 조건을 보완합니다. */
class PaymentServiceImplResidualConditionClosure2Test {

    private PaymentServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new PaymentServiceImpl(mock(PaymentDAO.class));
    }

    @Test
    void canceledAtShouldCoverPresentButEmptyCancellationList() {
        String result = invoke(
                "extractCanceledAt",
                Map.of("cancellations", List.of()));

        assertFalse(result.isBlank());
    }

    @Test
    void totalCanceledAmountShouldCoverScalarAmountFallback() {
        long result = invoke(
                "extractTotalCanceledAmount",
                Map.of(
                        "cancellations",
                        List.of(Map.of("amount", "250"))));

        assertEquals(250L, result);
    }

    @Test
    void totalCanceledAmountShouldIgnoreNullAndNonPositiveFallbackValues() {
        long result = invoke(
                "extractTotalCanceledAmount",
                Map.of(
                        "cancellations",
                        List.of(
                                Map.of("amount", "invalid"),
                                Map.of("amount", "0"),
                                Map.of("amount", "-10"))));

        assertEquals(0L, result);
    }

    @SuppressWarnings("unchecked")
    private <T> T invoke(String methodName, Object... arguments) {
        return (T) ReflectionTestUtils.invokeMethod(
                service,
                methodName,
                arguments);
    }
}
