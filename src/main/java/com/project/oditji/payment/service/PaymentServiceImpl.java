package com.project.oditji.payment.service;

import com.project.oditji.payment.dao.PaymentDAO;
import com.project.oditji.payment.vo.PaymentCancelRequestVO;
import com.project.oditji.payment.vo.PaymentCompleteRequestVO;
import com.project.oditji.payment.vo.PaymentCompleteResponseVO;
import com.project.oditji.payment.vo.PaymentTestVO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.HttpClientErrorException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PaymentServiceImpl implements PaymentService {

    private final RestClient restClient;
    private final PaymentDAO paymentDAO;

    @Value("${portone.api-secret}")
    private String apiSecret;

    public PaymentServiceImpl(PaymentDAO paymentDAO) {
        this.restClient = RestClient.builder()
                .baseUrl("https://api.portone.io")
                .build();
        this.paymentDAO = paymentDAO;
    }

    @Override
    public PaymentCompleteResponseVO completePayment(PaymentCompleteRequestVO requestVO) {

        if (requestVO == null) {
            return PaymentCompleteResponseVO.fail("요청 정보가 없습니다.");
        }

        if (requestVO.getPaymentId() == null || requestVO.getPaymentId().isBlank()) {
            return PaymentCompleteResponseVO.fail("paymentId가 없습니다.");
        }

        try {
            Map<String, Object> payment = getPortOnePayment(requestVO.getPaymentId());

            if (payment == null) {
                return PaymentCompleteResponseVO.fail("포트원 결제 조회 결과가 없습니다.");
            }

            System.out.println("========== 포트원 결제 조회 결과 ==========");
            System.out.println(payment);
            System.out.println("========================================");

            String status = String.valueOf(payment.get("status"));
            int paidAmount = extractPaidAmount(payment);

            if (!"PAID".equals(status)) {
                return PaymentCompleteResponseVO.fail("결제 상태가 PAID가 아닙니다. 현재 상태: " + status);
            }

            if (paidAmount != requestVO.getTotalAmount()) {
                return PaymentCompleteResponseVO.fail(
                        "결제 금액이 일치하지 않습니다. 요청 금액: "
                                + requestVO.getTotalAmount()
                                + ", 실제 결제 금액: "
                                + paidAmount
                );
            }

            PaymentTestVO existingPayment = paymentDAO.selectPaymentTestByPaymentId(requestVO.getPaymentId());

            if (existingPayment != null) {
                return PaymentCompleteResponseVO.success("이미 저장된 결제입니다.");
            }

            PaymentTestVO paymentTestVO = new PaymentTestVO();
            paymentTestVO.setPaymentId(String.valueOf(payment.get("id")));
            paymentTestVO.setOrderName(String.valueOf(payment.get("orderName")));
            paymentTestVO.setPaymentAmount(paidAmount);
            paymentTestVO.setPaymentStatus(status);
            paymentTestVO.setPayMethod(extractPayMethod(payment));
            paymentTestVO.setPgProvider(extractPgProvider(payment));
            paymentTestVO.setPgTxId(String.valueOf(payment.get("pgTxId")));
            paymentTestVO.setPaidAt(String.valueOf(payment.get("paidAt")));

            int insertResult = paymentDAO.insertPaymentTest(paymentTestVO);

            if (insertResult != 1) {
                return PaymentCompleteResponseVO.fail("결제 정보 DB 저장에 실패했습니다.");
            }

            return PaymentCompleteResponseVO.success("결제 검증 및 DB 저장 성공");

        } catch (Exception e) {
            e.printStackTrace();
            return PaymentCompleteResponseVO.fail("결제 검증 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    @Override
    public List<PaymentTestVO> getPaymentTestList() {
        return paymentDAO.selectPaymentTestList();
    }

    @Override
    public PaymentCompleteResponseVO cancelPayment(PaymentCancelRequestVO requestVO) {

        if (requestVO == null) {
            return PaymentCompleteResponseVO.fail("취소 요청 정보가 없습니다.");
        }

        if (requestVO.getPaymentId() == null || requestVO.getPaymentId().isBlank()) {
            return PaymentCompleteResponseVO.fail("paymentId가 없습니다.");
        }

        String reason = requestVO.getReason();

        if (reason == null || reason.isBlank()) {
            reason = "테스트 결제 취소";
        }

        try {
            PaymentTestVO paymentTestVO = paymentDAO.selectPaymentTestByPaymentId(requestVO.getPaymentId());

            if (paymentTestVO == null) {
                return PaymentCompleteResponseVO.fail("DB에 저장된 결제 정보가 없습니다.");
            }

            if ("CANCELED".equals(paymentTestVO.getPaymentStatus())) {
                return PaymentCompleteResponseVO.success("이미 취소된 결제입니다.");
            }

            if (!"PAID".equals(paymentTestVO.getPaymentStatus())) {
                return PaymentCompleteResponseVO.fail("PAID 상태의 결제만 취소할 수 있습니다. 현재 상태: " + paymentTestVO.getPaymentStatus());
            }

            Map<String, Object> cancelRequestBody = new HashMap<>();
            cancelRequestBody.put("reason", reason);

            try {
                Map<String, Object> cancelResult = restClient.post()
                        .uri("/payments/{paymentId}/cancel", requestVO.getPaymentId())
                        .header("Authorization", "PortOne " + apiSecret)
                        .body(cancelRequestBody)
                        .retrieve()
                        .body(new ParameterizedTypeReference<Map<String, Object>>() {});

                System.out.println("========== 포트원 결제 취소 결과 ==========");
                System.out.println(cancelResult);
                System.out.println("========================================");

            } catch (HttpClientErrorException.Conflict e) {
                String responseBody = e.getResponseBodyAsString();

                if (responseBody != null && responseBody.contains("PAYMENT_ALREADY_CANCELLED")) {
                    System.out.println("이미 포트원에서 취소된 결제입니다. DB 상태만 수정합니다.");
                } else {
                    e.printStackTrace();
                    return PaymentCompleteResponseVO.fail("결제 취소 충돌 오류가 발생했습니다.");
                }
            }
            
            Map<String, Object> payment = getPortOnePayment(requestVO.getPaymentId());

            String portOneStatus = String.valueOf(payment.get("status"));

            if (!"CANCELLED".equals(portOneStatus) && !"CANCELED".equals(portOneStatus)) {
                return PaymentCompleteResponseVO.fail("포트원 결제 취소 후 상태가 취소 상태가 아닙니다. 현재 상태: " + portOneStatus);
            }

            PaymentTestVO updateVO = new PaymentTestVO();
            updateVO.setPaymentId(requestVO.getPaymentId());
            updateVO.setCanceledAt(String.valueOf(payment.get("statusChangedAt")));
            updateVO.setCancelReason(reason);

            int updateResult = paymentDAO.updatePaymentTestCanceled(updateVO);

            if (updateResult != 1) {
                return PaymentCompleteResponseVO.fail("DB 결제 상태 변경에 실패했습니다.");
            }

            return PaymentCompleteResponseVO.success("결제 취소 및 DB 상태 변경 성공");

        } catch (Exception e) {
            e.printStackTrace();
            return PaymentCompleteResponseVO.fail("결제 취소 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    private Map<String, Object> getPortOnePayment(String paymentId) {
        return restClient.get()
                .uri("/payments/{paymentId}", paymentId)
                .header("Authorization", "PortOne " + apiSecret)
                .retrieve()
                .body(new ParameterizedTypeReference<Map<String, Object>>() {});
    }

    private int extractPaidAmount(Map<String, Object> payment) {
        Object amountObject = payment.get("amount");

        if (amountObject instanceof Map<?, ?> amountMap) {
            Object totalObject = amountMap.get("total");

            if (totalObject != null) {
                return Integer.parseInt(String.valueOf(totalObject));
            }
        }

        Object totalAmountObject = payment.get("totalAmount");

        if (totalAmountObject != null) {
            return Integer.parseInt(String.valueOf(totalAmountObject));
        }

        return 0;
    }

    private String extractPayMethod(Map<String, Object> payment) {
        Object methodObject = payment.get("method");

        if (methodObject instanceof Map<?, ?> methodMap) {
            Object providerObject = methodMap.get("provider");

            if (providerObject != null) {
                return String.valueOf(providerObject);
            }

            Object typeObject = methodMap.get("type");

            if (typeObject != null) {
                return String.valueOf(typeObject);
            }
        }

        return "UNKNOWN";
    }

    private String extractPgProvider(Map<String, Object> payment) {
        Object channelObject = payment.get("channel");

        if (channelObject instanceof Map<?, ?> channelMap) {
            Object pgProviderObject = channelMap.get("pgProvider");

            if (pgProviderObject != null) {
                return String.valueOf(pgProviderObject);
            }
        }

        return "UNKNOWN";
    }
}