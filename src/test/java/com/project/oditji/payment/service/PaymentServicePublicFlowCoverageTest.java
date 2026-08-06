package com.project.oditji.payment.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import com.project.oditji.payment.dao.PaymentDAO;
import com.project.oditji.payment.vo.PaymentVO;

/** 실제 HTTP 호출 대신 Mock 서버로 포트원 결제 확인·부분 환불 공개 흐름을 검증합니다. */
@ExtendWith(MockitoExtension.class)
class PaymentServicePublicFlowCoverageTest {

    @Mock
    private PaymentDAO paymentDAO;

    private PaymentServiceImpl service;
    private MockRestServiceServer server;

    @BeforeEach
    void setUp() {
        service = new PaymentServiceImpl(paymentDAO);

        RestClient.Builder builder = RestClient.builder()
                .baseUrl("https://api.portone.io");
        server = MockRestServiceServer.bindTo(builder).build();
        ReflectionTestUtils.setField(service, "restClient", builder.build());
        ReflectionTestUtils.setField(service, "apiSecret", "test-secret");
    }

    @Test
    void verifyPaidPaymentShouldMapSuccessfulPortOneResponse() {
        server.expect(requestTo("https://api.portone.io/payments/pay-100"))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header("Authorization", "PortOne test-secret"))
                .andRespond(withSuccess(
                        """
                        {
                          "id": "pay-100",
                          "status": "PAID",
                          "amount": {"total": 15000},
                          "orderName": "ODITJI 상품 주문",
                          "method": {"type": "CARD", "provider": "KAKAOPAY"},
                          "channel": {"pgProvider": "INICIS"},
                          "pgTxId": "pg-100",
                          "paidAt": "2026-08-06T02:00:00Z"
                        }
                        """,
                        MediaType.APPLICATION_JSON));

        PaymentVO result = service.verifyPaidPayment(
                "pay-100",
                15000L,
                "서버 주문명");

        assertEquals("pay-100", result.getPaymentId());
        assertEquals("ODITJI 상품 주문", result.getOrderName());
        assertEquals(15000L, result.getPaymentAmount());
        assertEquals("CARD", result.getPayMethod());
        assertEquals("PAID", result.getPaymentStatus());
        assertEquals("INICIS", result.getPgProvider());
        assertEquals("pg-100", result.getPgTxId());
        assertEquals("2026-08-06T02:00:00Z", result.getPaidAt());
        server.verify();
    }

    @Test
    void verifyPaidPaymentShouldUseExpectedOrderNameWhenResponseOmitsIt() {
        server.expect(requestTo("https://api.portone.io/payments/pay-101"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(
                        """
                        {
                          "id": "pay-101",
                          "status": "PAID",
                          "amount": {"total": 20000},
                          "method": {},
                          "channel": {}
                        }
                        """,
                        MediaType.APPLICATION_JSON));

        PaymentVO result = service.verifyPaidPayment(
                "pay-101",
                20000L,
                "서버 주문명");

        assertEquals("서버 주문명", result.getOrderName());
        assertEquals("CARD", result.getPayMethod());
        assertEquals("INICIS", result.getPgProvider());
        assertNull(result.getPgTxId());
        server.verify();
    }

    @Test
    void partialCancelShouldReturnAccumulatedPartialAndFullCanceledStates() {
        server.expect(requestTo("https://api.portone.io/payments/pay-200/cancel"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("Authorization", "PortOne test-secret"))
                .andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));
        server.expect(requestTo("https://api.portone.io/payments/pay-201/cancel"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));

        PaymentVO payment = payment("pay-200", 10000L, 1000L, "PARTIAL_CANCELED");
        PaymentVO partial = service.cancelPaidPaymentPartially(
                payment,
                3000L,
                " 부분 환불 ");

        assertEquals(4000L, partial.getCanceledAmount());
        assertEquals("PARTIAL_CANCELED", partial.getPaymentStatus());
        assertEquals("부분 환불", partial.getCancelReason());

        PaymentVO remaining = payment("pay-201", 10000L, 4000L, "PARTIAL_CANCELED");
        PaymentVO canceled = service.cancelPaidPaymentPartially(
                remaining,
                6000L,
                null);

        assertEquals(10000L, canceled.getCanceledAmount());
        assertEquals("CANCELED", canceled.getPaymentStatus());
        assertEquals("사용자 요청에 의한 결제 취소", canceled.getCancelReason());
        server.verify();
    }

    private PaymentVO payment(
            String paymentId,
            Long amount,
            Long canceledAmount,
            String status) {
        PaymentVO payment = new PaymentVO();
        payment.setPaymentNo(1L);
        payment.setOrderNo(2L);
        payment.setPaymentId(paymentId);
        payment.setPaymentAmount(amount);
        payment.setCanceledAmount(canceledAmount);
        payment.setPaymentStatus(status);
        return payment;
    }
}
