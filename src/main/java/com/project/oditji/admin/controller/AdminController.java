package com.project.oditji.admin.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.project.oditji.admin.service.AdminService;
import com.project.oditji.admin.vo.ContentManageVO;
import com.project.oditji.admin.vo.PlatformVO;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    // ===================== 관리자 홈 =====================

    // 관리자 메인 페이지 (대시보드)
    @GetMapping("/main")
    public String adminMain(Model model) {
        model.addAttribute("activeMenu", "main");
        model.addAttribute("adminMain", adminService.getDashboardStats());
        return "admin/main/adminMain";
    }

    // ===================== 1. 회원 관리 =====================

    @GetMapping("/member/list")
    public String memberList(Model model, @RequestParam(required = false) String keyword) {
        model.addAttribute("activeMenu", "member");
        model.addAttribute("memberList", adminService.getMemberList(keyword));
        return "admin/member/memberManage";
    }

    @PostMapping("/member/suspend")
    public String memberSuspend(@RequestParam Long memberNo) {
        adminService.suspendMember(memberNo);
        return "redirect:/admin/member/list";
    }

    @PostMapping("/member/withdraw")
    public String memberWithdraw(
            @RequestParam Long memberNo,
            RedirectAttributes ra) {

        // 관리자가 탈퇴 처리하면 대기 상태 없이 즉시 DB에서 완전히 삭제한다.
        try {
            adminService.deleteMember(memberNo);
            ra.addFlashAttribute("message", "회원 정보가 완전히 삭제되었습니다.");
        } catch (IllegalStateException e) {
            ra.addFlashAttribute("message", e.getMessage());
        }

        return "redirect:/admin/member/list";
    }

    @PostMapping("/member/restore")
    public String restoreMember(@RequestParam Long memberNo) {
        adminService.restoreMember(memberNo);
        return "redirect:/admin/member/list";
    }

    // ===================== 2. 리뷰 관리 (콘텐츠 리뷰 / 상품 리뷰) =====================

    // 2-1. 콘텐츠 리뷰 관리 (전체 리뷰, 신고 내역)
    @GetMapping("/review/list")
    public String reviewList(Model model,
            @RequestParam(required = false) String tab,
            @RequestParam(required = false) String keyword) {
        model.addAttribute("activeMenu", "review");
        model.addAttribute("reviewList", adminService.getContentReviewList(tab, keyword));
        return "admin/review/reviewManage";
    }

    @PostMapping("/review/delete")
    public String reviewDelete(@RequestParam Long reviewNo,
            @RequestParam(required = false) String tab) {
        adminService.deleteContentReview(reviewNo);
        return "redirect:/admin/review/list?tab=" + tab;
    }

    // 신고 승인: 신고를 인정하여 리뷰를 삭제 처리한다.
    @PostMapping("/review/report/approve")
    public String reviewReportApprove(
            @RequestParam Long reviewNo,
            @RequestParam(required = false) String tab,
            RedirectAttributes redirectAttributes) {

        try {
            adminService.approveContentReviewReport(reviewNo);
            redirectAttributes.addFlashAttribute("message", "신고를 승인하여 리뷰를 삭제했습니다.");
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("message", e.getMessage());
        }

        return "redirect:/admin/review/list?tab=" + tab;
    }

    // 신고 반려: 신고를 기각하고 리뷰는 그대로 유지한다.
    @PostMapping("/review/report/reject")
    public String reviewReportReject(
            @RequestParam Long reviewNo,
            @RequestParam(required = false) String tab,
            RedirectAttributes redirectAttributes) {

        try {
            adminService.rejectContentReviewReport(reviewNo);
            redirectAttributes.addFlashAttribute("message", "신고를 반려했습니다.");
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("message", e.getMessage());
        }

        return "redirect:/admin/review/list?tab=" + tab;
    }

    // 2-2. 상품 리뷰 관리 (전체 리뷰, 신고 내역)
    @GetMapping("/productReview/list")
    public String productReviewList(Model model,
            @RequestParam(required = false) String tab,
            @RequestParam(required = false) String keyword) {
        model.addAttribute("activeMenu", "productReview");
        model.addAttribute("productReviewList", adminService.getProductReviewList(tab, keyword));
        return "admin/review/productReviewManage";
    }

    @PostMapping("/productReview/delete")
    public String productReviewDelete(@RequestParam Long reviewNo,
            @RequestParam(required = false) String tab) {
        adminService.deleteProductReview(reviewNo);
        return "redirect:/admin/productReview/list?tab=" + tab;
    }

    // 신고 승인: 신고를 인정하여 상품 리뷰를 삭제 처리한다.
    @PostMapping("/productReview/report/approve")
    public String productReviewReportApprove(
            @RequestParam Long reviewNo,
            @RequestParam(required = false) String tab,
            RedirectAttributes redirectAttributes) {

        try {
            adminService.approveProductReviewReport(reviewNo);
            redirectAttributes.addFlashAttribute("message", "신고를 승인하여 리뷰를 삭제했습니다.");
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("message", e.getMessage());
        }

        return "redirect:/admin/productReview/list?tab=" + tab;
    }

    // 신고 반려: 신고를 기각하고 리뷰는 그대로 유지한다.
    @PostMapping("/productReview/report/reject")
    public String productReviewReportReject(
            @RequestParam Long reviewNo,
            @RequestParam(required = false) String tab,
            RedirectAttributes redirectAttributes) {

        try {
            adminService.rejectProductReviewReport(reviewNo);
            redirectAttributes.addFlashAttribute("message", "신고를 반려했습니다.");
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("message", e.getMessage());
        }

        return "redirect:/admin/productReview/list?tab=" + tab;
    }

    // ===================== 3. 이벤트 관리 (사업자 등록/수정/연장 요청 처리) =====================

    @GetMapping("/event/list")
    public String eventList(
            Model model,
            @RequestParam(required = false) String tab,
            @RequestParam(required = false) String keyword) {

        model.addAttribute("activeMenu", "event");

        model.addAttribute(
            "eventRequestList",
            adminService.getEventList(tab, keyword)
        );

        return "admin/event/eventManage";
    }


    @PostMapping("/event/approve")
    public String eventApprove(
            @RequestParam Long eventNo,
            @RequestParam(required = false) String tab) {

        adminService.approveEvent(eventNo);

        return "redirect:/admin/event/list?tab=" + tab;
    }


    @PostMapping("/event/reject")
    public String eventReject(
            @RequestParam Long eventNo,
            @RequestParam(required = false) String tab) {

        adminService.rejectEvent(eventNo);

        return "redirect:/admin/event/list?tab=" + tab;
    }

    // ===================== 4. 상품 관리 (사업자 등록/수정/삭제 요청 처리) =====================

    @GetMapping("/product/list")
    public String productList(Model model,
            @RequestParam(required = false) String tab,
            @RequestParam(required = false) String keyword) {

        model.addAttribute("activeMenu", "product");
        model.addAttribute("currentTab", tab);
        model.addAttribute("productRequestList", adminService.getProductRequestList(tab, keyword));

        return "admin/goods/productManage";
    }

    @PostMapping("/product/approve")
    public String productApprove(
            @RequestParam Long productNo,
            @RequestParam(required = false) String tab,
            RedirectAttributes redirectAttributes) {

        try {
            adminService.approveProduct(productNo);

            // 삭제 요청 탭에서 승인한 경우에는 실제 DB 삭제가 완료되었다는 메시지를 표시한다.
            if ("delete".equals(tab)) {
                redirectAttributes.addFlashAttribute(
                        "message",
                        "상품 삭제 요청을 승인하여 상품을 최종 삭제했습니다.");
            } else {
                redirectAttributes.addFlashAttribute(
                        "message",
                        "상품 요청을 승인했습니다.");
            }

        } catch (IllegalArgumentException | IllegalStateException e) {
            redirectAttributes.addFlashAttribute("message", e.getMessage());
        }

        return "redirect:/admin/product/list?tab=" + tab;
    }

    @PostMapping("/product/reject")
    public String productReject(
            @RequestParam Long productNo,
            @RequestParam(required = false) String tab,
            RedirectAttributes redirectAttributes) {

        try {
            adminService.rejectProduct(productNo);

            // 삭제 요청 반려 시에는 상품을 기존 승인 상태로 복구한다.
            if ("delete".equals(tab)) {
                redirectAttributes.addFlashAttribute(
                        "message",
                        "상품 삭제 요청을 반려했습니다.");
            } else {
                redirectAttributes.addFlashAttribute(
                        "message",
                        "상품 요청을 반려했습니다.");
            }

        } catch (IllegalArgumentException | IllegalStateException e) {
            redirectAttributes.addFlashAttribute("message", e.getMessage());
        }

        return "redirect:/admin/product/list?tab=" + tab;
    }

    // ===================== 5. 주문 관리 =====================

    @GetMapping("/order/list")
    public String orderList(Model model,
            @RequestParam(required = false) String tab,
            @RequestParam(required = false) String keyword) {
        model.addAttribute("activeMenu", "order");

        if ("refund".equals(tab)) {
            model.addAttribute("refundList", adminService.getRefundList(keyword));
        } else {
            model.addAttribute("orderList", adminService.getOrderList(keyword));
        }

        return "admin/order/orderManage";
    }

    @PostMapping("/order/status-update")
    public String orderStatusUpdate(@RequestParam Long orderNo, @RequestParam String orderStatus) {
        adminService.updateOrderStatus(orderNo, orderStatus);
        return "redirect:/admin/order/list";
    }

    @PostMapping("/order/cancel")
    public String orderCancel(@RequestParam Long orderItemNo) {
        adminService.cancelOrder(orderItemNo);
        return "redirect:/admin/order/list";
    }

    @PostMapping("/order/refund-approve")
    public String orderRefundApprove(@RequestParam Long cancelNo) {
        adminService.approveRefund(cancelNo);
        return "redirect:/admin/order/list?tab=refund";
    }

    @PostMapping("/order/refund-reject")
    public String orderRefundReject(@RequestParam Long cancelNo) {
        adminService.rejectRefund(cancelNo);
        return "redirect:/admin/order/list?tab=refund";
    }

    // ===================== 6. 사업자 관리 (목록 / 입점 승인) =====================

    @GetMapping("/business/list")
    public String businessList(Model model,
            @RequestParam(required = false) String tab,
            @RequestParam(required = false) String keyword) {
        model.addAttribute("activeMenu", "business");

        if ("approval".equals(tab)) {
            model.addAttribute("approvalList", adminService.getBusinessApprovalList(keyword));
        } else {
            model.addAttribute("businessList", adminService.getBusinessList(keyword));
        }

        return "admin/business/businessManage";
    }

    @PostMapping("/business/grade")
    public String businessGrade(@RequestParam Long businessNo, @RequestParam String gradeName) {
        adminService.updateBusinessGrade(businessNo, gradeName);
        return "redirect:/admin/business/list?tab=info";
    }

    @PostMapping("/business/approve")
    public String businessApprove(@RequestParam Long businessNo) {
        adminService.approveBusiness(businessNo);
        return "redirect:/admin/business/list?tab=approval";
    }

    @PostMapping("/business/reject")
    public String businessReject(@RequestParam Long businessNo) {
        adminService.rejectBusiness(businessNo);
        return "redirect:/admin/business/list?tab=approval";
    }

    // ===================== 7. 정산 관리 (사업자 입금 확인) =====================

    @GetMapping("/settlement/main")
    public String settlementMain(Model model, @RequestParam(required = false) String keyword) {
        model.addAttribute("activeMenu", "settlement");
        model.addAttribute("settlementList", adminService.getSettlementList(keyword));
        return "admin/settlement/settlementManage";
    }

    @PostMapping("/settlement/confirm")
    public String settlementConfirm(@RequestParam Long settlementNo) {
        adminService.confirmSettlement(settlementNo);
        return "redirect:/admin/settlement/main";
    }

    @PostMapping("/settlement/reject")
    public String settlementReject(@RequestParam Long settlementNo) {
        adminService.rejectSettlement(settlementNo);
        return "redirect:/admin/settlement/main";
    }

    // ===================== 8. 시스템 관리 (모니터링) =====================

    @GetMapping("/monitoring")
    public String monitoring(Model model) {
        model.addAttribute("activeMenu", "monitoring");
        model.addAttribute("monitoringList", adminService.getMonitoringList());
        return "admin/monitoring/monitoring";
    }

    // ===================== (사이드바 미노출) 콘텐츠 관리 =====================

    @GetMapping("/content/list")
    public String contentList(Model model, @RequestParam(required = false) String keyword) {
        model.addAttribute("activeMenu", "content");
        model.addAttribute("contentList", adminService.getContentList(keyword));
        model.addAttribute("platformList", adminService.getPlatformList());
        return "admin/content/contentManage";
    }

    @PostMapping("/content/update")
    public String contentUpdate(@RequestParam Long contentNo,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String contentType,
            @RequestParam(required = false) String genreText,
            @RequestParam(required = false) String castNames,
            @RequestParam(required = false) String overview,
            @RequestParam(required = false) Integer runtime,
            @RequestParam(required = false) String releaseDate,
            @RequestParam(required = false) List<Long> platformNos) {

        ContentManageVO content = new ContentManageVO();
        content.setContentNo(contentNo);
        content.setTitle(title);
        content.setContentType(contentType);
        content.setGenreText(genreText);
        content.setCastNames(castNames);
        content.setOverview(overview);
        content.setRuntime(runtime);
        // releaseDate 문자열 -> Date 변환은 화면(포맷) 확정 후 공용 유틸로 처리 필요 (현재는 생략)

        adminService.updateContent(content, platformNos);

        return "redirect:/admin/content/list";
    }

    @PostMapping("/content/platform/register")
    public String contentPlatformRegister(@RequestParam String platformName,
            @RequestParam(required = false) String siteUrl,
            @RequestParam(required = false) String isActive) {

        PlatformVO platform = new PlatformVO();
        platform.setPlatformName(platformName);
        platform.setSiteUrl(siteUrl);
        platform.setIsActive(isActive == null ? "Y" : isActive);
        // 로고 이미지 파일 업로드 처리는 파일 저장 로직 확정 후 별도 구현 필요

        adminService.registerPlatform(platform);

        return "redirect:/admin/content/list";
    }

    @PostMapping("/content/platform/update")
    public String contentPlatformUpdate(@RequestParam Long platformNo,
            @RequestParam(required = false) String siteUrl,
            @RequestParam(required = false) String isActive) {

        PlatformVO platform = new PlatformVO();
        platform.setPlatformNo(platformNo);
        platform.setSiteUrl(siteUrl);
        platform.setIsActive(isActive);

        adminService.updatePlatform(platform);

        return "redirect:/admin/content/list";
    }
}
