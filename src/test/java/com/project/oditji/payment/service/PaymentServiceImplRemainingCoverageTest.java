package com.project.oditji.payment.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import com.project.oditji.payment.dao.PaymentDAO;
import com.project.oditji.payment.vo.PaymentVO;

/**
 * PaymentServiceImpl의 포트원 공개 흐름과 기존 테스트에서 남은 조건 분기를 검증합니다.
 */
@ExtendWith(MockitoExtension.class)
class PaymentServiceImplRemainingCoverageTest {

    @Mock
    private PaymentDAO paymentDAO;

    private PaymentServiceImpl paymentService;
    private MockRestServiceServer server;

    @BeforeEach
    void setUp() {
        paymentService = new PaymentServiceImpl(paymentDAO);

        RestClient.Builder builder = RestClient.builder()
                .baseUrl("https://api.portone.io");
        server = MockRestServiceServer.bindTo(builder).build();
        ReflectionTestUtils.setField(paymentService, "restClient", builder.build());
        ReflectionTestUtils.setField(paymentService, "apiSecret", "test-secret");
    }

    @Test
    void verifyPaidPaymentShouldRejectNullAndEmptyPortOneResponses() {
        server.expect(requestTo("https://api.portone.io/payments/pay-null"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess());
        server.expect(requestTo("https://api.portone.io/payments/pay-empty"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));

        IllegalStateException nullResponse = assertThrows(
                IllegalStateException.class,
                () -> paymentService.verifyPaidPayment("pay-null", 1000L, "주문"));
        IllegalStateException emptyResponse = assertThrows(
                IllegalStateException.class,
                () -> paymentService.verifyPaidPayment("pay-empty", 1000L, "주문"));

        assertEquals("포트원 결제 조회 결과가 없습니다.", nullResponse.getMessage());
        assertEquals("포트원 결제 조회 결과가 없습니다.", emptyResponse.getMessage());
        server.verify();
    }

    @Test
    void verifyPaidPaymentShouldRejectMissingAndDifferentResponsePaymentIds() {
        server.expect(requestTo("https://api.portone.io/payments/pay-missing-id"))
                .andRespond(withSuccess(
                        """
                        {
                          "status": "PAID",
                          "amount": {"total": 1000}
                        }
                        """,
                        MediaType.APPLICATION_JSON));
        server.expect(requestTo("https://api.portone.io/payments/pay-different-id"))
                .andRespond(withSuccess(
                        """
                        {
                          "id": "other-payment",
                          "status": "PAID",
                          "amount": {"total": 1000}
                        }
                        """,
                        MediaType.APPLICATION_JSON));

        IllegalArgumentException missingId = assertThrows(
                IllegalArgumentException.class,
                () -> paymentService.verifyPaidPayment("pay-missing-id", 1000L, "주문"));
        IllegalArgumentException differentId = assertThrows(
                IllegalArgumentException.class,
                () -> paymentService.verifyPaidPayment("pay-different-id", 1000L, "주문"));

        assertEquals("포트원 결제 ID가 일치하지 않습니다.", missingId.getMessage());
        assertEquals("포트원 결제 ID가 일치하지 않습니다.", differentId.getMessage());
        server.verify();
    }

    @Test
    void verifyPaidPaymentShouldRejectNonPaidStatusAndAmountMismatch() {
        server.expect(requestTo("https://api.portone.io/payments/pay-ready"))
                .andRespond(withSuccess(
                        """
                        {
                          "id": "pay-ready",
                          "status": "READY",
                          "amount": {"total": 1000}
                        }
                        """,
                        MediaType.APPLICATION_JSON));
        server.expect(requestTo("https://api.portone.io/payments/pay-amount"))
                .andRespond(withSuccess(
                        """
                        {
                          "id": "pay-amount",
                          "status": "PAID",
                          "amount": {"total": 999}
                        }
                        """,
                        MediaType.APPLICATION_JSON));

        IllegalArgumentException ready = assertThrows(
                IllegalArgumentException.class,
                () -> paymentService.verifyPaidPayment("pay-ready", 1000L, "주문"));
        IllegalArgumentException amount = assertThrows(
                IllegalArgumentException.class,
                () -> paymentService.verifyPaidPayment("pay-amount", 1000L, "주문"));

        assertTrue(ready.getMessage().contains("READY"));
        assertTrue(amount.getMessage().contains("999"));
        server.verify();
    }

    @Test
    void verifyPaidPaymentShouldCoverBlankOrderNameAndRestClientFailure() {
        server.expect(requestTo("https://api.portone.io/payments/pay-blank-name"))
                .andExpect(header("Authorization", "PortOne test-secret"))
                .andRespond(withSuccess(
                        """
                        {
                          "id": "pay-blank-name",
                          "status": "PAID",
                          "amount": {"total": 1500},
                          "orderName": "   ",
                          "method": {"provider": "NAVERPAY"},
                          "channel": {"type": "TEST"},
                          "transactionId": "tx-blank-name"
                        }
                        """,
                        MediaType.APPLICATION_JSON));
        server.expect(requestTo("https://api.portone.io/payments/pay-error"))
                .andRespond(withStatus(HttpStatus.BAD_GATEWAY)
                        .body("gateway error")
                        .contentType(MediaType.TEXT_PLAIN));

        PaymentVO blankName = paymentService.verifyPaidPayment(
                "pay-blank-name",
                1500L,
                "서버 주문명");
        IllegalStateException restError = assertThrows(
                IllegalStateException.class,
                () -> paymentService.verifyPaidPayment("pay-error", 1500L, "주문"));

        assertEquals("서버 주문명", blankName.getOrderName());
        assertEquals("NAVERPAY", blankName.getPayMethod());
        assertEquals("TEST", blankName.getPgProvider());
        assertEquals("tx-blank-name", blankName.getPgTxId());
        assertEquals("포트원 결제 조회에 실패했습니다.", restError.getMessage());
        server.verify();
    }

    @Test
    void cancelPaidPaymentShouldReturnImmediatelyWhenPreLookupIsAlreadyCanceled() {
        server.expect(requestTo("https://api.portone.io/payments/pay-already"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(
                        """
                        {
                          "status": "CANCELLED",
                          "statusChangedAt": "2026-08-10T12:00:00+09:00"
                        }
                        """,
                        MediaType.APPLICATION_JSON));

        PaymentVO source = payment("pay-already", 10000L, 0L, "PAID");
        PaymentVO result = paymentService.cancelPaidPayment(source, " 이미 취소 ");

        assertEquals("CANCELED", result.getPaymentStatus());
        assertEquals(10000L, result.getCanceledAmount());
        assertEquals("이미 취소", result.getCancelReason());
        assertEquals("2026-08-10T12:00:00+09:00", result.getCanceledAt());
        server.verify();
    }

    @Test
    void cancelPaidPaymentShouldCompleteNormalCancelFlow() {
        server.expect(requestTo("https://api.portone.io/payments/pay-full"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(
                        """
                        {
                          "status": "PAID",
                          "cancellations": []
                        }
                        """,
                        MediaType.APPLICATION_JSON));
        server.expect(requestTo("https://api.portone.io/payments/pay-full/cancel"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("Authorization", "PortOne test-secret"))
                .andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));
        server.expect(requestTo("https://api.portone.io/payments/pay-full"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(
                        """
                        {
                          "status": "CANCELED",
                          "cancellations": [
                            {"canceledAt": "2026-08-10T12:01:00+09:00"}
                          ]
                        }
                        """,
                        MediaType.APPLICATION_JSON));

        PaymentVO source = payment("pay-full", 20000L, null, "PAID");
        PaymentVO result = paymentService.cancelPaidPayment(source, "   ");

        assertEquals("CANCELED", result.getPaymentStatus());
        assertEquals(20000L, result.getCanceledAmount());
        assertEquals("사용자 요청에 의한 결제 취소", result.getCancelReason());
        assertEquals("2026-08-10T12:01:00+09:00", result.getCanceledAt());
        server.verify();
    }

    @Test
    void cancelPaidPaymentShouldWrapPreLookupFailure() {
        server.expect(requestTo("https://api.portone.io/payments/pay-pre-error"))
                .andRespond(withStatus(HttpStatus.BAD_GATEWAY)
                        .body("pre lookup error")
                        .contentType(MediaType.TEXT_PLAIN));

        PaymentVO source = payment("pay-pre-error", 10000L, 0L, "PAID");
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> paymentService.cancelPaidPayment(source, "사유"));

        assertEquals("결제 취소 전 포트원 상태 조회에 실패했습니다.", exception.getMessage());
        server.verify();
    }

    @Test
    void cancelPaidPaymentShouldWrapOrdinaryCancelApiFailure() {
        expectPaidLookup("pay-cancel-error");
        server.expect(requestTo("https://api.portone.io/payments/pay-cancel-error/cancel"))
                .andRespond(withStatus(HttpStatus.BAD_GATEWAY)
                        .body("ordinary gateway failure")
                        .contentType(MediaType.TEXT_PLAIN));

        PaymentVO source = payment("pay-cancel-error", 10000L, 0L, "PAID");
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> paymentService.cancelPaidPayment(source, "사유"));

        assertEquals("포트원 결제 취소에 실패했습니다.", exception.getMessage());
        server.verify();
    }

    @Test
    void cancelPaidPaymentShouldSynchronizeAlreadyCanceledPgResponse() {
        expectPaidLookup("pay-duplicate");
        server.expect(requestTo("https://api.portone.io/payments/pay-duplicate/cancel"))
                .andRespond(withStatus(HttpStatus.BAD_GATEWAY)
                        .body("already_cancelled")
                        .contentType(MediaType.TEXT_PLAIN));
        server.expect(requestTo("https://api.portone.io/payments/pay-duplicate"))
                .andRespond(withSuccess(
                        """
                        {
                          "status": "PAID",
                          "cancellations": [
                            {
                              "totalAmount": 10000,
                              "cancelledAt": "2026-08-10T12:02:00+09:00"
                            }
                          ]
                        }
                        """,
                        MediaType.APPLICATION_JSON));

        PaymentVO source = payment("pay-duplicate", 10000L, 0L, "PAID");
        PaymentVO result = paymentService.cancelPaidPayment(source, "중복 취소 동기화");

        assertEquals("CANCELED", result.getPaymentStatus());
        assertEquals(10000L, result.getCanceledAmount());
        assertEquals("2026-08-10T12:02:00+09:00", result.getCanceledAt());
        server.verify();
    }

    @Test
    void cancelPaidPaymentShouldCoverDuplicateResponseLookupFailures() {
        expectPaidLookup("pay-duplicate-lookup-error");
        server.expect(requestTo("https://api.portone.io/payments/pay-duplicate-lookup-error/cancel"))
                .andRespond(withStatus(HttpStatus.BAD_GATEWAY)
                        .body("PAYMENT_ALREADY_CANCELLED")
                        .contentType(MediaType.TEXT_PLAIN));
        server.expect(requestTo("https://api.portone.io/payments/pay-duplicate-lookup-error"))
                .andRespond(withStatus(HttpStatus.BAD_GATEWAY)
                        .body("lookup failure")
                        .contentType(MediaType.TEXT_PLAIN));

        PaymentVO lookupErrorSource = payment("pay-duplicate-lookup-error", 10000L, 0L, "PAID");
        IllegalStateException lookupError = assertThrows(
                IllegalStateException.class,
                () -> paymentService.cancelPaidPayment(lookupErrorSource, "사유"));

        assertEquals("중복 취소 응답 후 포트원 상태 조회에 실패했습니다.", lookupError.getMessage());
        server.verify();
    }

    @Test
    void cancelPaidPaymentShouldRejectDuplicateResponseWhenPortOneIsNotFullyCanceled() {
        expectPaidLookup("pay-duplicate-not-canceled");
        server.expect(requestTo("https://api.portone.io/payments/pay-duplicate-not-canceled/cancel"))
                .andRespond(withStatus(HttpStatus.BAD_GATEWAY)
                        .body("{\"pgCode\":\"8023\"}")
                        .contentType(MediaType.APPLICATION_JSON));
        server.expect(requestTo("https://api.portone.io/payments/pay-duplicate-not-canceled"))
                .andRespond(withSuccess(
                        """
                        {
                          "status": "PAID",
                          "cancellations": [
                            {"totalAmount": 1000}
                          ]
                        }
                        """,
                        MediaType.APPLICATION_JSON));

        PaymentVO source = payment("pay-duplicate-not-canceled", 10000L, 0L, "PAID");
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> paymentService.cancelPaidPayment(source, "사유"));

        assertTrue(exception.getMessage().contains("전액 취소를 확인하지 못했습니다"));
        server.verify();
    }

    @Test
    void cancelPaidPaymentShouldCoverPostCancelLookupFailureAndUnconfirmedStatus() {
        expectPaidLookup("pay-post-error");
        expectSuccessfulCancel("pay-post-error");
        server.expect(requestTo("https://api.portone.io/payments/pay-post-error"))
                .andRespond(withStatus(HttpStatus.BAD_GATEWAY)
                        .body("post lookup error")
                        .contentType(MediaType.TEXT_PLAIN));

        PaymentVO postErrorSource = payment("pay-post-error", 10000L, 0L, "PAID");
        IllegalStateException postError = assertThrows(
                IllegalStateException.class,
                () -> paymentService.cancelPaidPayment(postErrorSource, "사유"));

        assertEquals("취소 후 포트원 결제 상태 조회에 실패했습니다.", postError.getMessage());
        server.verify();
    }

    @Test
    void cancelPaidPaymentShouldRejectSuccessfulApiWhenFinalStateIsNotCanceled() {
        expectPaidLookup("pay-not-confirmed");
        expectSuccessfulCancel("pay-not-confirmed");
        server.expect(requestTo("https://api.portone.io/payments/pay-not-confirmed"))
                .andRespond(withSuccess(
                        """
                        {
                          "status": "PAID",
                          "cancellations": []
                        }
                        """,
                        MediaType.APPLICATION_JSON));

        PaymentVO source = payment("pay-not-confirmed", 10000L, 0L, "PAID");
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> paymentService.cancelPaidPayment(source, "사유"));

        assertTrue(exception.getMessage().contains("현재 상태: PAID"));
        server.verify();
    }

    @Test
    void partialCancelShouldCoverNullCanceledAmountPaidStatusAndPortOneErrors() {
        server.expect(requestTo("https://api.portone.io/payments/pay-partial-null/cancel"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));
        server.expect(requestTo("https://api.portone.io/payments/pay-partial-unsupported/cancel"))
                .andRespond(withStatus(HttpStatus.BAD_GATEWAY)
                        .body("{\"pgCode\":\"500503\"}")
                        .contentType(MediaType.APPLICATION_JSON));
        server.expect(requestTo("https://api.portone.io/payments/pay-partial-error/cancel"))
                .andRespond(withStatus(HttpStatus.BAD_GATEWAY)
                        .body("ordinary partial cancel error")
                        .contentType(MediaType.TEXT_PLAIN));

        PaymentVO nullCanceled = payment("pay-partial-null", 10000L, null, "PAID");
        PaymentVO result = paymentService.cancelPaidPaymentPartially(
                nullCanceled,
                2500L,
                " 부분 취소 ");

        assertEquals(2500L, result.getCanceledAmount());
        assertEquals("PARTIAL_CANCELED", result.getPaymentStatus());
        assertEquals("부분 취소", result.getCancelReason());

        PaymentVO unsupported = payment("pay-partial-unsupported", 10000L, 0L, "PAID");
        IllegalStateException unsupportedException = assertThrows(
                IllegalStateException.class,
                () -> paymentService.cancelPaidPaymentPartially(unsupported, 1000L, "사유"));
        assertTrue(unsupportedException.getMessage().contains("부분 환불을 지원하지 않습니다"));

        PaymentVO ordinaryError = payment("pay-partial-error", 10000L, 0L, "PAID");
        IllegalStateException ordinaryException = assertThrows(
                IllegalStateException.class,
                () -> paymentService.cancelPaidPaymentPartially(ordinaryError, 1000L, "사유"));
        assertEquals("포트원 부분 환불에 실패했습니다.", ordinaryException.getMessage());
        server.verify();
    }

    @Test
    void helperBranchesShouldCoverNullKeysBlankTransactionAndCancellationShapes() {
        assertNull(invokePrivate("readString", Map.of("key", "value"), (String) null));
        assertNull(invokePrivate("readString", Map.of(), "missing"));

        assertNull(invokePrivate(
                "extractPgTxId",
                Map.of("pgTxId", " ", "transactionId", " ")));

        assertFalse((Boolean) invokePrivate(
                "isFullyCanceledPayment",
                (Map<String, Object>) null,
                1000L));

        assertTrue((Boolean) invokePrivate(
                "isAlreadyCanceledResponse",
                responseExceptionBody("already_cancelled")));

        List<Object> cancellations = new ArrayList<Object>();
        cancellations.add(Map.of("totalAmount", -100));
        cancellations.add(Map.of("totalAmount", 0));
        cancellations.add(Map.of("amount", Map.of("total", 200)));
        assertEquals(
                200L,
                ((Long) invokePrivate(
                        "extractTotalCanceledAmount",
                        Map.of("cancellations", cancellations))).longValue());

        Map<String, Object> blankStatusChangedAt = new HashMap<String, Object>();
        blankStatusChangedAt.put("cancellations", List.of("not-a-map"));
        blankStatusChangedAt.put("statusChangedAt", "   ");
        String generated = invokePrivate("extractCanceledAt", blankStatusChangedAt);
        assertFalse(generated.isBlank());
    }

    private void expectPaidLookup(String paymentId) {
        server.expect(requestTo("https://api.portone.io/payments/" + paymentId))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(
                        """
                        {
                          "status": "PAID",
                          "cancellations": []
                        }
                        """,
                        MediaType.APPLICATION_JSON));
    }

    private void expectSuccessfulCancel(String paymentId) {
        server.expect(requestTo("https://api.portone.io/payments/" + paymentId + "/cancel"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));
    }

    private PaymentVO payment(
            String paymentId,
            Long amount,
            Long canceledAmount,
            String status) {
        PaymentVO payment = new PaymentVO();
        payment.setPaymentNo(10L);
        payment.setOrderNo(20L);
        payment.setPaymentId(paymentId);
        payment.setPaymentAmount(amount);
        payment.setCanceledAmount(canceledAmount);
        payment.setPaymentStatus(status);
        return payment;
    }

    private RestClientResponseException responseExceptionBody(String body) {
        return HttpServerErrorException.create(
                HttpStatus.BAD_GATEWAY,
                "Bad Gateway",
                org.springframework.http.HttpHeaders.EMPTY,
                body.getBytes(java.nio.charset.StandardCharsets.UTF_8),
                java.nio.charset.StandardCharsets.UTF_8);
    }

    @SuppressWarnings("unchecked")
    private <T> T invokePrivate(String methodName, Object... arguments) {
        return (T) ReflectionTestUtils.invokeMethod(
                paymentService,
                methodName,
                arguments);
    }
}
