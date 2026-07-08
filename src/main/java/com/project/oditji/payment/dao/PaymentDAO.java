package com.project.oditji.payment.dao;

import com.project.oditji.payment.vo.PaymentTestVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface PaymentDAO {

    int insertPaymentTest(PaymentTestVO paymentTestVO);

    List<PaymentTestVO> selectPaymentTestList();

    PaymentTestVO selectPaymentTestByPaymentId(String paymentId);

    int updatePaymentTestCanceled(PaymentTestVO paymentTestVO);
}