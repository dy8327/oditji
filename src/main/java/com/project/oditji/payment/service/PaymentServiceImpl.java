package com.project.oditji.payment.service;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import com.project.oditji.payment.dao.PaymentDAO;
import com.project.oditji.payment.vo.PaymentVO;

@Service
public class PaymentServiceImpl implements PaymentService {

    private static final String PAID_STATUS = "PAID";

    private static final String CANCELLED_STATUS = "CANCELLED";

    private static final String CANCELED_STATUS = "CANCELED";

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

            if (responsePaymentId == null || !paymentId.equals(responsePaymentId)) {
                throw new IllegalArgumentException(
                        "포트원 결제 ID가 일치하지 않습니다.");
            }

            String paymentStatus = readString(payment, "status");

            if (!PAID_STATUS.equals(paymentStatus)) {
                throw new IllegalArgumentException("결제가 완료되지 않았습니다. 현재 상태: " + paymentStatus);
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
            String savedOrderName = portOneOrderName == null || portOneOrderName.isBlank() ? expectedOrderName
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

            // [수정] 반복적인 예외 객체 생성 로직을 헬퍼 메소드(createPortOneException)로 공통화
            throw createPortOneException(
                    "포트원 결제 조회에 실패했습니다.",
                    e);
        }
    }

    /**
     * [신규 추가] 이미 완료된 결제를 포트원 API를 통해 취소 처리하는 서비스 메소드
     *
     * @param paymentVO 기존 결제 정보 객체
     * @param reason    취소 요청 사유
     * @return 취소 상태 및 취소 일시가 반영된 PaymentVO 객체
     */
    @Override
    public PaymentVO cancelPaidPayment(
            PaymentVO paymentVO,
            String reason) {

        // 1. 요청 파라미터 및 결제 상태 검증
        if (paymentVO == null) {
            throw new IllegalArgumentException("결제내역을 찾을 수 없습니다.");
        }

        validatePaymentId(paymentVO.getPaymentId());

        if (!PAID_STATUS.equals(paymentVO.getPaymentStatus())) {
            if (CANCELED_STATUS.equals(paymentVO.getPaymentStatus())) {
                throw new IllegalArgumentException("이미 취소된 결제입니다.");
            }
            throw new IllegalArgumentException("결제 완료 상태의 결제만 취소할 수 있습니다.");
        }

        // 2. 취소 사유 정형화 (공백 처리 및 기본값 세팅)
        String normalizedReason = normalizeCancelReason(reason);

        // 3. 포트원 API 요청 바디 세팅
        Map<String, Object> cancelRequestBody = new HashMap<String, Object>();
        cancelRequestBody.put("reason", normalizedReason);

        try {
            // 4. 포트원 결제 취소 API 호출 (POST /payments/{paymentId}/cancel)
            restClient.post()
                    .uri("/payments/{paymentId}/cancel", paymentVO.getPaymentId())
                    .header("Authorization", "PortOne " + apiSecret)
                    .body(cancelRequestBody)
                    .retrieve()
                    .body(new ParameterizedTypeReference<Map<String, Object>>() {
                    });

        } catch (HttpClientErrorException.Conflict e) {

            /*
             * 동일 결제가 포트원에서 이미 취소된 경우에도
             * 현재 결제 상태를 재조회하여 DB 상태를 동기화한다.
             */
            String responseBody = e.getResponseBodyAsString();

            // 이미 취소된 에러 코드가 아닌 진짜 충돌 예외라면 예외를 발생시킨다.
            if (responseBody == null
                    || (!responseBody.contains("PAYMENT_ALREADY_CANCELLED")
                            && !responseBody.contains("ALREADY_CANCELLED"))) {

                throw createPortOneException(
                        "포트원 결제 취소 요청이 충돌했습니다.",
                        e);
            }

        } catch (RestClientResponseException e) {
            throw createPortOneException(
                    "포트원 결제 취소에 실패했습니다.",
                    e);
        }

        // 5. 취소 처리 후 최종 결제 상태 확증을 위한 포트원 재조회
        Map<String, Object> canceledPayment;

        try {
            canceledPayment = requestPortOnePayment(paymentVO.getPaymentId());
        } catch (RestClientResponseException e) {
            throw createPortOneException(
                    "취소 후 포트원 결제 상태 조회에 실패했습니다.",
                    e);
        }

        // 6. 취소 상태 검증 (CANCELLED 또는 CANCELED 여부 확인)
        String portOneStatus = readString(canceledPayment, "status");

        if (!CANCELLED_STATUS.equals(portOneStatus)
                && !CANCELED_STATUS.equals(portOneStatus)) {
            throw new IllegalStateException(
                    "포트원 결제 상태가 취소 상태가 아닙니다. 현재 상태: " + portOneStatus);
        }

        // 7. DB 업데이트를 위한 취소 정보 VO 객체 구성 및 반환
        PaymentVO canceledPaymentVO = new PaymentVO();

        canceledPaymentVO.setPaymentNo(paymentVO.getPaymentNo());
        canceledPaymentVO.setOrderNo(paymentVO.getOrderNo());
        canceledPaymentVO.setPaymentId(paymentVO.getPaymentId());
        canceledPaymentVO.setPaymentStatus(CANCELED_STATUS);
        canceledPaymentVO.setCanceledAt(extractCanceledAt(canceledPayment));
        canceledPaymentVO.setCancelReason(normalizedReason);

        return canceledPaymentVO;
    }

    /*
     * =========================================================
     * [부분 환불 기능 추가]
     *
     * 포트원 V2 결제 취소 API에 amount를 전달하여
     * 주문상품 금액만 부분 취소한다.
     * =========================================================
     */
    @Override
    public PaymentVO cancelPaidPaymentPartially(
            PaymentVO paymentVO,
            Long cancelAmount,
            String reason) {

        if (paymentVO == null) {
            throw new IllegalArgumentException("결제내역을 찾을 수 없습니다.");
        }

        validatePaymentId(paymentVO.getPaymentId());

        if (cancelAmount == null || cancelAmount <= 0) {
            throw new IllegalArgumentException("부분 환불 금액이 올바르지 않습니다.");
        }

        long alreadyCanceled = paymentVO.getCanceledAmount() == null
                ? 0L
                : paymentVO.getCanceledAmount();
        long remainingAmount = paymentVO.getPaymentAmount() - alreadyCanceled;

        if (cancelAmount > remainingAmount) {
            throw new IllegalArgumentException("남은 결제 금액보다 큰 금액은 환불할 수 없습니다.");
        }

        if (!PAID_STATUS.equals(paymentVO.getPaymentStatus())
                && !"PARTIAL_CANCELED".equals(paymentVO.getPaymentStatus())) {
            throw new IllegalArgumentException("결제 완료 또는 부분 취소 상태의 결제만 환불할 수 있습니다.");
        }

        String normalizedReason = normalizeCancelReason(reason);

        Map<String, Object> body = new HashMap<String, Object>();
        body.put("reason", normalizedReason);
        body.put("amount", cancelAmount);
        body.put("currentCancellableAmount", remainingAmount);

        try {
            restClient.post()
                    .uri("/payments/{paymentId}/cancel", paymentVO.getPaymentId())
                    .header("Authorization", "PortOne " + apiSecret)
                    .body(body)
                    .retrieve()
                    .body(new ParameterizedTypeReference<Map<String, Object>>() {
                    });
        } catch (RestClientResponseException e) {

            /*
             * =========================================================
             * [포트원 간편결제 부분취소 제한 오류 처리]
             *
             * 테스트 채널의 간편결제 부분취소 제한 오류인 경우
             * PG사 원문 대신 사용자가 이해할 수 있는 메시지를 반환한다.
             * =========================================================
             */
            String responseBody = e.getResponseBodyAsString();

            if (responseBody != null
                    && responseBody.contains(
                            "\"pgCode\":\"500503\"")) {

                throw new IllegalStateException(
                        "해당 간편결제는 부분 환불을 지원하지 않습니다. "
                                + "전체 주문 취소를 이용해주세요.",
                        e);
            }

            throw createPortOneException(
                    "포트원 부분 환불에 실패했습니다.",
                    e);
        }

        long canceledTotal = alreadyCanceled + cancelAmount;

        PaymentVO result = new PaymentVO();
        result.setPaymentNo(paymentVO.getPaymentNo());
        result.setOrderNo(paymentVO.getOrderNo());
        result.setPaymentId(paymentVO.getPaymentId());
        result.setPaymentAmount(paymentVO.getPaymentAmount());
        result.setCanceledAmount(canceledTotal);
        result.setPaymentStatus(
                canceledTotal >= paymentVO.getPaymentAmount()
                        ? CANCELED_STATUS
                        : "PARTIAL_CANCELED");
        result.setCanceledAt(OffsetDateTime.now().toString());
        result.setCancelReason(normalizedReason);

        return result;
    }

    @Override
    public PaymentVO getPaymentByPaymentId(String paymentId) {

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
                .uri("/payments/{paymentId}", paymentId)
                .header("Authorization", "PortOne " + apiSecret)
                .retrieve()
                .body(new ParameterizedTypeReference<Map<String, Object>>() {
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

        if (transactionId != null && !transactionId.isBlank()) {
            return transactionId;
        }

        return null;
    }

    /**
     * [신규 추가] 포트원 응답 Map 구조에서 취소 일시(canceledAt)를 파싱 및 추출하는 메소드
     * 
     * @param payment 포트원 응답 데이터 Map
     * @return 취소 일시 문자열 (ISO Format)
     */
    private String extractCanceledAt(
            Map<String, Object> payment) {

        // 1. cancellations 배열이 존재하는지 파싱
        Object cancellationsObject = payment.get("cancellations");

        if (cancellationsObject instanceof List<?> cancellations
                && !cancellations.isEmpty()) {

            // 가장 최근의 취소 정보 추출
            Object latestCancellation = cancellations.get(cancellations.size() - 1);

            if (latestCancellation instanceof Map<?, ?> cancellationMap) {

                // 영문 스펠링 차이(cancelledAt / canceledAt)를 고려한 안전한 추출
                Object canceledAt = cancellationMap.get("cancelledAt");

                if (canceledAt == null) {
                    canceledAt = cancellationMap.get("canceledAt");
                }

                if (canceledAt != null) {
                    return String.valueOf(canceledAt);
                }
            }
        }

        // 2. cancellations 배열이 없는 경우 statusChangedAt 대체 확인
        String statusChangedAt = readString(payment, "statusChangedAt");

        if (statusChangedAt != null && !statusChangedAt.isBlank()) {
            return statusChangedAt;
        }

        /*
         * 포트원 응답에 취소 일시가 없는 예외 상황을 대비한다.
         * PAYMENT.CANCELED_AT은 VARCHAR2 컬럼이므로 ISO 문자열로 저장한다.
         */
        return OffsetDateTime.now().toString();
    }

    /**
     * [신규 추가] 입력받은 취소 사유의 공백 제거 및 유효성 검증 메소드
     * 
     * @param reason 클라이언트 요청 취소 사유
     * @return 정형화된 취소 사유 문자열
     */
    private String normalizeCancelReason(
            String reason) {

        String normalizedReason = reason == null ? "" : reason.trim();

        // 사유 미입력 시 기본 문구 세팅
        if (normalizedReason.isEmpty()) {
            normalizedReason = "사용자 요청에 의한 결제 취소";
        }

        // DB 컬럼 길이 제한 대응 (500자 제한)
        if (normalizedReason.length() > 500) {
            throw new IllegalArgumentException(
                    "취소 사유는 500자 이하로 입력해주세요.");
        }

        return normalizedReason;
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
            return Long.valueOf(String.valueOf(value));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * [신규 추가] 포트원 RestClient 통신 실패 예외 메시지 생성을 전담하는 헬퍼 메소드
     * 
     * @param message 예외 프론트 메시지
     * @param e       RestClientResponseException 원인 예외
     * @return 래핑된 IllegalStateException
     */
    private IllegalStateException createPortOneException(
            String message,
            RestClientResponseException e) {

        return new IllegalStateException(
                message
                        + " HTTP 상태: "
                        + e.getStatusCode()
                        + ", 응답: "
                        + e.getResponseBodyAsString(),
                e);
    }

    private void validatePaymentId(
            String paymentId) {

        if (paymentId == null || paymentId.isBlank()) {
            throw new IllegalArgumentException(
                    "결제 ID가 없습니다.");
        }

        if (paymentId.length() > 100) {
            throw new IllegalArgumentException(
                    "결제 ID 길이가 올바르지 않습니다.");
        }
    }
}