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
         * 포트원에 결제 전액 취소를 요청한 뒤
         * 취소 상태를 재조회하여 반환한다.
         */
        PaymentVO cancelPaidPayment(
                        PaymentVO paymentVO,
                        String reason);

        /**
         * [부분 환불 기능 추가]
         * 주문상품 금액만 포트원에 부분 취소하고 DB 반영용 VO를 반환한다.
         */
        PaymentVO cancelPaidPaymentPartially(
                        PaymentVO paymentVO,
                        Long cancelAmount,
                        String reason);

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