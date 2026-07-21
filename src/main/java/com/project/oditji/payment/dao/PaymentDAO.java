package com.project.oditji.payment.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.project.oditji.payment.vo.PaymentVO;

@Mapper
public interface PaymentDAO {

        /**
         * 실제 결제내역 저장
         */
        int insertPayment(
                        PaymentVO paymentVO);

        /**
         * 결제 ID 중복 조회
         */
        PaymentVO selectPaymentByPaymentId(
                        @Param("paymentId") String paymentId);

        /**
         * 주문 번호에 연결된 결제 조회
         */
        PaymentVO selectPaymentByOrderNo(
                        @Param("orderNo") Long orderNo);

        /**
         * 포트원 취소 성공 후 DB 결제 상태를 변경한다.
         */
        int updatePaymentCanceled(
                        PaymentVO paymentVO);

        /**
         * 전체 결제내역 조회
         */
        List<PaymentVO> selectPaymentList();
}