package com.project.oditji.admin.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class AdminController {

    // 관리자 메인 페이지
    @GetMapping("/main")
    public String adminMain(Model model) {
        model.addAttribute("activeMenu", "main");
        return "admin/main/adminMain";
    }

    // 1. 회원 관리
    @GetMapping("/member/list")
    public String memberList(Model model) {
        model.addAttribute("activeMenu", "member");
        return "admin/member/memberManage";
    }

    // 2. 콘텐츠 관리
    @GetMapping("/content/list")
    public String contentList(Model model) {
        model.addAttribute("activeMenu", "content");
        return "admin/content/contentManage";
    }

    // 3. 주문 관리
    @GetMapping("/order/list")
    public String orderList(Model model) {
        model.addAttribute("activeMenu", "order");
        return "admin/order/orderManage";
    }

    // 4. 리뷰 관리
    @GetMapping("/review/list")
    public String reviewList(Model model) {
        model.addAttribute("activeMenu", "review");
        return "admin/review/reviewManage";
    }

    // 5. 사업자 관리
    @GetMapping("/business/list")
    public String businessList(Model model) {
        model.addAttribute("activeMenu", "business");
        return "admin/business/businessManage";
    }

    // 6. 정산 관리
    @GetMapping("/settlement/main")
    public String settlementMain(Model model) {
        model.addAttribute("activeMenu", "settlement");
        return "admin/settlement/settlementManage";
    }

    // 7. 이벤트 관리
    @GetMapping("/event/list")
    public String eventList(Model model) {
        model.addAttribute("activeMenu", "event");
        return "admin/event/eventManage";
    }

    // 8. 모니터링
    @GetMapping("/monitoring")
    public String monitoring(Model model) {
        model.addAttribute("activeMenu", "monitoring");
        return "admin/monitoring/monitoring";
    }
}
