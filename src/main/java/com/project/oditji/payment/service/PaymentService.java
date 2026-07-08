package com.project.oditji.payment.service;

import com.project.oditji.payment.vo.PaymentCancelRequestVO;
import com.project.oditji.payment.vo.PaymentCompleteRequestVO;
import com.project.oditji.payment.vo.PaymentCompleteResponseVO;
import com.project.oditji.payment.vo.PaymentTestVO;

import java.util.List;

public interface PaymentService {

    PaymentCompleteResponseVO completePayment(PaymentCompleteRequestVO requestVO);

    List<PaymentTestVO> getPaymentTestList();

    PaymentCompleteResponseVO cancelPayment(PaymentCancelRequestVO requestVO);
}