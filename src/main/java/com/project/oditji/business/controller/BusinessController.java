package com.project.oditji.business.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/business")
public class BusinessController {


    // 사업자 메인
    @GetMapping("/main")
    public String main(Model model) {

        model.addAttribute("activeMenu", "main");

        return "business/main/businessMain";
    }


    // 상품 목록
    @GetMapping("/product/list")
    public String productList(Model model) {

        model.addAttribute("activeMenu", "product");

        return "business/goods/productList";
    }


    // 상품 등록
    @GetMapping("/product/register")
    public String productRegister(Model model) {

        model.addAttribute("activeMenu", "productRegister");

        return "business/goods/productRegister";
    }


    // 상품 수정
    @GetMapping("/product/update")
    public String productUpdate(Model model) {

        model.addAttribute("activeMenu", "productUpdate");

        return "business/goods/productUpdate";
    }


    // 이벤트 목록
    @GetMapping("/event/list")
    public String eventList(Model model) {

        model.addAttribute("activeMenu", "event");

        return "business/event/eventList";
    }


    // 이벤트 등록
    @GetMapping("/event/register")
    public String eventRegister(Model model) {

        model.addAttribute("activeMenu", "eventRegister");

        return "business/event/eventRegister";
    }


    // 이벤트 수정
    @GetMapping("/event/update")
    public String eventUpdate(Model model) {

        model.addAttribute("activeMenu", "eventUpdate");

        return "business/event/eventUpdate";
    }


    // 승인 관리
    @GetMapping("/approval")
    public String approvalList(
            @RequestParam(defaultValue = "product") String type,
            Model model) {

        model.addAttribute("currentType", type);
        model.addAttribute("activeMenu", "approval");

        return "business/approval/approvalList";
    }


    // 정산 관리
    @GetMapping("/settlement/main")
    public String settlement(Model model) {

        model.addAttribute("activeMenu", "settlement");

        return "business/settlement/settlementMain";
    }


    // 판매 현황
    @GetMapping("/settlement/sales")
    public String sales(Model model) {

        model.addAttribute("activeMenu", "sales");

        return "business/settlement/salesStatus";
    }


    // 채팅
    @GetMapping("/chat")
    public String chat(Model model) {

        model.addAttribute("activeMenu", "chat");

        return "business/community/businessChat";
    }


    // 주문 현황
    @GetMapping("/order/list")
    public String orderList(Model model) {

        model.addAttribute("activeMenu", "order");

        return "business/order/orderList";
    }


    // 배송 관리
    @GetMapping("/order/detail")
    public String orderDetail(Model model) {

        model.addAttribute("activeMenu", "delivery");

        return "business/order/orderDetail";
    }


    // 취소/환불 관리
    @GetMapping("/cancel/list")
    public String cancelList(Model model) {

        model.addAttribute("activeMenu", "cancel");

        return "business/order/cancelList";
    }

}