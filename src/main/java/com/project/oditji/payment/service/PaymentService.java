package com.project.oditji.payment.service;

import java.util.List;

import com.project.oditji.payment.vo.PaymentVO;

public interface PaymentService {

    /**
     * 포트원 서버 API에서 결제 정보를 조회하고
     * 상태와 실제 결제 금액을 검증한다.
     */
    PaymentVO verifyPaidPayment(
            String paymentId,
            Long expectedAmount,
            String expectedOrderName);

    /**
     * 결제 ID로 DB 결제내역 조회
     */
    PaymentVO getPaymentByPaymentId(
            String paymentId);

    /**
     * 전체 결제내역 조회
     */
    List<PaymentVO> getPaymentList();
}