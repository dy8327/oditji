package com.project.oditji.business.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/business")
public class BusinessController {

    @GetMapping("/main")
    public String businessMain() {
        return "business/businessMain";
    }

     @GetMapping("/product/list")
    public String businessProductList() {
        return "business/businessProductList";
    }

    @GetMapping("/product/register")
    public String businessProductRegisterRequest() {
        return "business/businessProductRegisterRequest";
    }

    @GetMapping("/product/update")
    public String businessProductUpdateRequest() {
        return "business/businessProductUpdateRequest";
    }

    @GetMapping("/sales")
    public String businessSalesStatus() {
        return "business/businessSalesStatus";
    }

    @GetMapping("/settlement/main")
    public String businessSettlementMain() {
        return "business/businessSettlementMain";
    }

    @GetMapping("/settlement/expected")
    public String businessSettlementExpected() {
        return "business/businessSettlementExpected";
    }

    @GetMapping("/settlement/account")
    public String businessSettlementAccount() {
        return "business/businessSettlementAccount";
    }

    @GetMapping("/settlement/account/update")
    public String businessSettlementAccountUpdate() {
        return "business/businessSettlementAccountUpdate";
    }

    @GetMapping("/event/list")
    public String businessEventList() {
        return "business/businessEventList";
    }

    @GetMapping("/event/register")
    public String businessEventRegisterRequest() {
        return "business/businessEventRegisterRequest";
    }

    @GetMapping("/event/update")
    public String businessEventUpdateRequest() {
        return "business/businessEventUpdateRequest";
    }

    @GetMapping("/approval/product")
    public String businessApprovalProductList() {
        return "business/businessApprovalProductList";
    }

    @GetMapping("/approval/event")
    public String businessApprovalEventList() {
        return "business/businessApprovalEventList";
    }

    @GetMapping("/approval/product/detail")
    public String businessApprovalProductDetail() {
        return "business/businessApprovalProductDetail";
    }

    @GetMapping("/approval/event/detail")
    public String businessApprovalEventDetail() {
        return "business/businessApprovalEventDetail";
    }

    @GetMapping("/chat/list")
    public String businessChatList() {
        return "business/businessChatList";
    }

    @GetMapping("/chat/message")
    public String businessMessageDetail() {
        return "business/businessMessageDetail";
    }
}