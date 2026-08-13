package com.project.oditji.payment.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientResponseException;

import com.project.oditji.payment.dao.PaymentDAO;

/** 포트원 중복 취소 응답 판별 OR 조건의 각 잔여 피연산자를 직접 보완합니다. */
class PaymentServiceImplAlreadyCanceledOperandCoverageTest {

    private PaymentServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new PaymentServiceImpl(mock(PaymentDAO.class));
    }

    @Test
    void alreadyCanceledResponseShouldRecognizeEverySupportedSignature() {
        assertTrue(alreadyCanceled("PAYMENT_ALREADY_CANCELLED"));
        assertTrue(alreadyCanceled("ALREADY_CANCELED"));
        assertTrue(alreadyCanceled("{\"pgCode\":\"8023\"}"));
        assertTrue(alreadyCanceled("기승인 취소된 거래"));
        assertFalse(alreadyCanceled("ordinary gateway error"));
    }

    private boolean alreadyCanceled(String body) {
        Boolean result = ReflectionTestUtils.invokeMethod(
                service,
                "isAlreadyCanceledResponse",
                response(body));
        return Boolean.TRUE.equals(result);
    }

    private RestClientResponseException response(String body) {
        return HttpServerErrorException.create(
                HttpStatus.BAD_GATEWAY,
                "Bad Gateway",
                HttpHeaders.EMPTY,
                body.getBytes(StandardCharsets.UTF_8),
                StandardCharsets.UTF_8);
    }
}
