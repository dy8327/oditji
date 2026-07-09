package com.project.oditji.admin.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/admin")
public class AdminController {

    // ===================== 관리자 홈 =====================

    // 관리자 메인 페이지 (대시보드)
    @GetMapping("/main")
    public String adminMain(Model model) {
        model.addAttribute("activeMenu", "main");
        return "admin/main/adminMain";
    }

    // ===================== 1. 회원 관리 (기존 방식 유지) =====================

    @GetMapping("/member/list")
    public String memberList(Model model) {
        model.addAttribute("activeMenu", "member");
        return "admin/member/memberManage";
    }

    @PostMapping("/member/suspend")
    public String memberSuspend(@RequestParam Long memberNo) {
        return "redirect:/admin/member/list";
    }

    @PostMapping("/member/withdraw")
    public String memberWithdraw(@RequestParam Long memberNo) {
        return "redirect:/admin/member/list";
    }

    @PostMapping("/member/grade")
    public String memberGrade(@RequestParam Long memberNo, @RequestParam String grade) {
        return "redirect:/admin/member/list";
    }

    // ===================== 2. 리뷰 관리 (콘텐츠 리뷰 / 상품 리뷰) =====================

    // 2-1. 콘텐츠 리뷰 관리 (전체 리뷰, 신고 내역)
    @GetMapping("/review/list")
    public String reviewList(Model model) {
        model.addAttribute("activeMenu", "review");
        return "admin/review/reviewManage";
    }

    @PostMapping("/review/delete")
    public String reviewDelete(@RequestParam Long reviewNo,
                                @RequestParam(required = false) String tab) {
        return "redirect:/admin/review/list?tab=" + tab;
    }

    // 2-2. 상품 리뷰 관리 (전체 리뷰, 신고 내역)
    @GetMapping("/productReview/list")
    public String productReviewList(Model model) {
        model.addAttribute("activeMenu", "productReview");
        return "admin/review/productReviewManage";
    }

    @PostMapping("/productReview/delete")
    public String productReviewDelete(@RequestParam Long reviewNo,
                                    @RequestParam(required = false) String tab) {
        return "redirect:/admin/productReview/list?tab=" + tab;
    }

    // ===================== 3. 이벤트 관리 (사업자 등록/수정/연장 요청 처리) =====================

    @GetMapping("/event/list")
    public String eventList(Model model, @RequestParam(required = false) String tab) {
        model.addAttribute("activeMenu", "event");
        return "admin/event/eventManage";
    }

    @PostMapping("/event/approve")
    public String eventApprove(@RequestParam Long requestNo,
                                @RequestParam(required = false) String tab) {
        return "redirect:/admin/event/list?tab=" + tab;
    }

    @PostMapping("/event/reject")
    public String eventReject(@RequestParam Long requestNo,
                               @RequestParam(required = false) String tab) {
        return "redirect:/admin/event/list?tab=" + tab;
    }

    // ===================== 4. 상품 관리 (사업자 등록/수정/삭제 요청 처리) =====================

    @GetMapping("/product/list")
    public String productList(Model model,
                            @RequestParam(required = false) String tab) {

        model.addAttribute("activeMenu", "product");
        model.addAttribute("currentTab", tab);

        return "admin/goods/productManage";
    }

    @PostMapping("/product/approve")
    public String productApprove(@RequestParam Long productNo,
                                @RequestParam(required = false) String tab) {

        return "redirect:/admin/product/list?tab=" + tab;
    }

    @PostMapping("/product/reject")
    public String productReject(@RequestParam Long productNo,
                                @RequestParam(required = false) String tab) {

        return "redirect:/admin/product/list?tab=" + tab;
    }

    // ===================== 5. 주문 관리 (기존 방식 유지) =====================

    @GetMapping("/order/list")
    public String orderList(Model model, @RequestParam(required = false) String tab) {
        model.addAttribute("activeMenu", "order");
        return "admin/order/orderManage";
    }

    @PostMapping("/order/status-update")
    public String orderStatusUpdate(@RequestParam Long orderNo, @RequestParam String orderStatus) {
        return "redirect:/admin/order/list";
    }

    @PostMapping("/order/cancel")
    public String orderCancel(@RequestParam Long orderItemNo) {
        return "redirect:/admin/order/list";
    }

    @PostMapping("/order/refund-approve")
    public String orderRefundApprove(@RequestParam Long cancelNo) {
        return "redirect:/admin/order/list?tab=refund";
    }

    @PostMapping("/order/refund-reject")
    public String orderRefundReject(@RequestParam Long cancelNo) {
        return "redirect:/admin/order/list?tab=refund";
    }

    // ===================== 6. 사업자 관리 (목록 / 입점 승인) =====================

    @GetMapping("/business/list")
    public String businessList(Model model, @RequestParam(required = false) String tab) {
        model.addAttribute("activeMenu", "business");
        return "admin/business/businessManage";
    }

    @PostMapping("/business/grade")
    public String businessGrade(@RequestParam Long businessNo, @RequestParam String gradeName) {
        return "redirect:/admin/business/list?tab=info";
    }

    @PostMapping("/business/approve")
    public String businessApprove(@RequestParam Long businessNo) {
        return "redirect:/admin/business/list?tab=approval";
    }

    @PostMapping("/business/reject")
    public String businessReject(@RequestParam Long businessNo) {
        return "redirect:/admin/business/list?tab=approval";
    }

    // ===================== 7. 정산 관리 (사업자 입금 확인) =====================

    @GetMapping("/settlement/main")
    public String settlementMain(Model model) {
        model.addAttribute("activeMenu", "settlement");
        return "admin/settlement/settlementManage";
    }

    @PostMapping("/settlement/confirm")
    public String settlementConfirm(@RequestParam Long settlementNo) {
        return "redirect:/admin/settlement/main";
    }

    @PostMapping("/settlement/reject")
    public String settlementReject(@RequestParam Long settlementNo) {
        return "redirect:/admin/settlement/main";
    }

    // ===================== 8. 시스템 관리 (모니터링) =====================

    @GetMapping("/monitoring")
    public String monitoring(Model model) {
        model.addAttribute("activeMenu", "monitoring");
        return "admin/monitoring/monitoring";
    }

    // ===================== (사이드바 미노출) 콘텐츠 관리 - 기존 페이지 유지 =====================
    // 새 메뉴 구조에는 없지만 화면 자체는 남겨두었으므로 매핑도 유지합니다.

    @GetMapping("/content/list")
    public String contentList(Model model) {
        model.addAttribute("activeMenu", "content");
        return "admin/content/contentManage";
    }

    @PostMapping("/content/update")
    public String contentUpdate(@RequestParam Long contentNo) {
        return "redirect:/admin/content/list";
    }

    @PostMapping("/content/platform/register")
    public String contentPlatformRegister(@RequestParam String platformName) {
        return "redirect:/admin/content/list";
    }

    @PostMapping("/content/platform/update")
    public String contentPlatformUpdate(@RequestParam Long platformNo) {
        return "redirect:/admin/content/list";
    }
}