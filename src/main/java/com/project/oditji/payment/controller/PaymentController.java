package com.project.oditji.payment.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.project.oditji.payment.service.PaymentService;
import com.project.oditji.payment.vo.PaymentVO;

@Controller
@RequestMapping("/payment")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(
            PaymentService paymentService) {

        this.paymentService = paymentService;
    }

    /**
     * 실제 테스트 결제는 상품 주문서를 통해 진행한다.
     */
    @GetMapping("/test")
    public String paymentTest() {

        return "redirect:/goods/list";
    }

    /**
     * DB에 저장된 실제 결제내역
     */
    @GetMapping("/list")
    public String paymentList(
            Model model) {

        List<PaymentVO> paymentList = paymentService.getPaymentList();

        model.addAttribute(
                "paymentList",
                paymentList);

        return "payment/paymentList";
    }
}