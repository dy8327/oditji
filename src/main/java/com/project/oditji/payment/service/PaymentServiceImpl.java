package com.project.oditji.payment.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import com.project.oditji.payment.dao.PaymentDAO;
import com.project.oditji.payment.vo.PaymentVO;

@Service
public class PaymentServiceImpl implements PaymentService {

    private static final String PAID_STATUS = "PAID";

    private final RestClient restClient;
    private final PaymentDAO paymentDAO;

    @Value("${portone.api-secret}")
    private String apiSecret;

    public PaymentServiceImpl(
            PaymentDAO paymentDAO) {

        this.paymentDAO = paymentDAO;

        this.restClient = RestClient.builder()
                .baseUrl("https://api.portone.io")
                .build();
    }

    @Override
    public PaymentVO verifyPaidPayment(
            String paymentId,
            Long expectedAmount,
            String expectedOrderName) {

        validatePaymentId(paymentId);

        if (expectedAmount == null || expectedAmount <= 0) {
            throw new IllegalArgumentException(
                    "서버 결제 금액이 올바르지 않습니다.");
        }

        /*
         * 동일 paymentId가 이미 DB에 저장되어 있으면
         * 중복 주문 생성을 차단한다.
         */
        PaymentVO existingPayment = paymentDAO.selectPaymentByPaymentId(paymentId);

        if (existingPayment != null) {
            throw new IllegalArgumentException(
                    "이미 처리된 결제입니다.");
        }

        try {

            Map<String, Object> payment = requestPortOnePayment(paymentId);

            if (payment == null || payment.isEmpty()) {
                throw new IllegalStateException(
                        "포트원 결제 조회 결과가 없습니다.");
            }

            String responsePaymentId = readString(payment, "id");

            if (responsePaymentId == null
                    || !paymentId.equals(responsePaymentId)) {

                throw new IllegalArgumentException(
                        "포트원 결제 ID가 일치하지 않습니다.");
            }

            String paymentStatus = readString(payment, "status");

            if (!PAID_STATUS.equals(paymentStatus)) {
                throw new IllegalArgumentException(
                        "결제가 완료되지 않았습니다. 현재 상태: "
                                + paymentStatus);
            }

            long paidAmount = extractPaidAmount(payment);

            if (paidAmount != expectedAmount.longValue()) {

                throw new IllegalArgumentException(
                        "결제 금액이 일치하지 않습니다. "
                                + "서버 주문 금액: "
                                + expectedAmount
                                + "원, 포트원 결제 금액: "
                                + paidAmount
                                + "원");
            }

            String portOneOrderName = readString(payment, "orderName");

            /*
             * 주문명은 PG사 또는 포트원 응답 과정에서
             * 표현이 일부 달라질 수 있으므로 금액처럼 강제 실패시키지는 않는다.
             * 응답 주문명이 없으면 서버 주문명을 저장한다.
             */
            String savedOrderName = portOneOrderName == null
                    || portOneOrderName.isBlank()
                            ? expectedOrderName
                            : portOneOrderName;

            PaymentVO paymentVO = new PaymentVO();

            paymentVO.setOrderName(savedOrderName);
            paymentVO.setPaymentId(paymentId);
            paymentVO.setPaymentAmount(paidAmount);
            paymentVO.setPayMethod(extractPayMethod(payment));
            paymentVO.setPaymentStatus(PAID_STATUS);
            paymentVO.setPgProvider(extractPgProvider(payment));
            paymentVO.setPgTxId(extractPgTxId(payment));
            paymentVO.setPaidAt(readString(payment, "paidAt"));

            return paymentVO;

        } catch (RestClientResponseException e) {

            String responseBody = e.getResponseBodyAsString();

            throw new IllegalStateException(
                    "포트원 결제 조회에 실패했습니다. HTTP 상태: "
                            + e.getStatusCode()
                            + ", 응답: "
                            + responseBody,
                    e);
        }
    }

    @Override
    public PaymentVO getPaymentByPaymentId(
            String paymentId) {

        validatePaymentId(paymentId);

        return paymentDAO.selectPaymentByPaymentId(paymentId);
    }

    @Override
    public List<PaymentVO> getPaymentList() {

        List<PaymentVO> paymentList = paymentDAO.selectPaymentList();

        if (paymentList == null) {
            return new ArrayList<PaymentVO>();
        }

        return paymentList;
    }

    private Map<String, Object> requestPortOnePayment(
            String paymentId) {

        return restClient.get()
                .uri(
                        "/payments/{paymentId}",
                        paymentId)
                .header(
                        "Authorization",
                        "PortOne " + apiSecret)
                .retrieve()
                .body(
                        new ParameterizedTypeReference<Map<String, Object>>() {
                        });
    }

    private long extractPaidAmount(
            Map<String, Object> payment) {

        Object amountObject = payment.get("amount");

        if (amountObject instanceof Map<?, ?> amountMap) {

            Object totalObject = amountMap.get("total");

            Long amount = parseLong(totalObject);

            if (amount != null) {
                return amount.longValue();
            }
        }

        Object totalAmountObject = payment.get("totalAmount");

        Long totalAmount = parseLong(totalAmountObject);

        if (totalAmount != null) {
            return totalAmount.longValue();
        }

        throw new IllegalStateException(
                "포트원 응답에서 결제 금액을 확인할 수 없습니다.");
    }

    private String extractPayMethod(
            Map<String, Object> payment) {

        Object methodObject = payment.get("method");

        if (methodObject instanceof Map<?, ?> methodMap) {

            Object typeObject = methodMap.get("type");

            if (typeObject != null) {
                return String.valueOf(typeObject);
            }

            Object providerObject = methodMap.get("provider");

            if (providerObject != null) {
                return String.valueOf(providerObject);
            }
        }

        return "CARD";
    }

    private String extractPgProvider(
            Map<String, Object> payment) {

        Object channelObject = payment.get("channel");

        if (channelObject instanceof Map<?, ?> channelMap) {

            Object pgProviderObject = channelMap.get("pgProvider");

            if (pgProviderObject != null) {
                return String.valueOf(pgProviderObject);
            }

            Object typeObject = channelMap.get("type");

            if (typeObject != null) {
                return String.valueOf(typeObject);
            }
        }

        return "INICIS";
    }

    private String extractPgTxId(
            Map<String, Object> payment) {

        String pgTxId = readString(payment, "pgTxId");

        if (pgTxId != null && !pgTxId.isBlank()) {
            return pgTxId;
        }

        String transactionId = readString(payment, "transactionId");

        if (transactionId != null
                && !transactionId.isBlank()) {

            return transactionId;
        }

        return null;
    }

    private String readString(
            Map<String, Object> source,
            String key) {

        if (source == null || key == null) {
            return null;
        }

        Object value = source.get(key);

        if (value == null) {
            return null;
        }

        String result = String.valueOf(value);

        if ("null".equalsIgnoreCase(result)) {
            return null;
        }

        return result;
    }

    private Long parseLong(
            Object value) {

        if (value == null) {
            return null;
        }

        if (value instanceof Number number) {
            return number.longValue();
        }

        try {
            return Long.valueOf(
                    String.valueOf(value));

        } catch (NumberFormatException e) {
            return null;
        }
    }

    private void validatePaymentId(
            String paymentId) {

        if (paymentId == null
                || paymentId.isBlank()) {

            throw new IllegalArgumentException(
                    "결제 ID가 없습니다.");
        }

        if (paymentId.length() > 100) {
            throw new IllegalArgumentException(
                    "결제 ID 길이가 올바르지 않습니다.");
        }
    }
}