package com.project.oditji.payment.controller;

import com.project.oditji.payment.service.PaymentService;
import com.project.oditji.payment.vo.PaymentCancelRequestVO;
import com.project.oditji.payment.vo.PaymentCompleteRequestVO;
import com.project.oditji.payment.vo.PaymentCompleteResponseVO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/payment")
public class PaymentController {

    private final PaymentService paymentService;

    @Value("${portone.store-id}")
    private String storeId;

    @Value("${portone.payment.channel-key}")
    private String paymentChannelKey;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @GetMapping("/test")
    public String paymentTest(Model model) {
        model.addAttribute("storeId", storeId);
        model.addAttribute("channelKey", paymentChannelKey);

        model.addAttribute("orderName", "ODITJI 테스트 굿즈");
        model.addAttribute("totalAmount", 1000);

        return "payment/paymentTest";
    }

    @PostMapping("/complete")
    @ResponseBody
    public PaymentCompleteResponseVO completePayment(@RequestBody PaymentCompleteRequestVO requestVO) {
        return paymentService.completePayment(requestVO);
    }

    @PostMapping("/cancel")
    public String cancelPayment(PaymentCancelRequestVO requestVO) {
        PaymentCompleteResponseVO responseVO = paymentService.cancelPayment(requestVO);

        System.out.println("========== 결제 취소 처리 결과 ==========");
        System.out.println("success = " + responseVO.isSuccess());
        System.out.println("message = " + responseVO.getMessage());
        System.out.println("=====================================");

        return "redirect:/payment/list";
    }

    @GetMapping("/success")
    public String paymentSuccess() {
        return "payment/paymentSuccess";
    }

    @GetMapping("/fail")
    public String paymentFail() {
        return "payment/paymentFail";
    }

    @GetMapping("/list")
    public String paymentList(Model model) {
        model.addAttribute("paymentList", paymentService.getPaymentTestList());
        return "payment/paymentList";
    }
}