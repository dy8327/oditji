package com.project.oditji.admin.controller;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.util.UriComponentsBuilder;

import com.project.oditji.admin.service.AdminService;
import com.project.oditji.admin.vo.ContentManageVO;
import com.project.oditji.admin.vo.PlatformVO;
import com.project.oditji.admin.vo.PopularClickVO;
import com.project.oditji.admin.vo.VisitorTrendVO;
import com.project.oditji.common.vo.PageVO;

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

    private static final int MEMBER_PAGE_SIZE = 10;
    private static final int MEMBER_PAGE_BLOCK_SIZE = 5;

    @GetMapping("/member/list")
    public String memberList(
            Model model,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false, defaultValue = "all") String searchType,
            @RequestParam(required = false) String status,
            @RequestParam(required = false, defaultValue = "all") String memberType,
            @RequestParam(required = false, defaultValue = "1") int page) {

        int totalCount = adminService.getMemberListCount(keyword, searchType, status, memberType);
        PageVO pagination = buildMemberPagination(page, totalCount);

        model.addAttribute("activeMenu", "member");
        model.addAttribute("memberList",
                adminService.getMemberList(keyword, searchType, status, memberType, pagination.getCurrentPage(),
                        MEMBER_PAGE_SIZE));
        model.addAttribute("memberStats", adminService.getMemberStats());
        model.addAttribute("pagination", pagination);
        model.addAttribute("searchType", searchType);
        model.addAttribute("status", status);
        model.addAttribute("memberType", memberType);

        return "admin/member/memberManage";
    }

    @PostMapping("/member/suspend")
    public String memberSuspend(
            @RequestParam Long memberNo,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String searchType,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String memberType,
            @RequestParam(required = false, defaultValue = "1") int page) {

        adminService.suspendMember(memberNo);
        return "redirect:" + memberListRedirectUrl(keyword, searchType, status, memberType, page);
    }

    @PostMapping("/member/withdraw")
    public String memberWithdraw(
            @RequestParam Long memberNo,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String searchType,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String memberType,
            @RequestParam(required = false, defaultValue = "1") int page,
            RedirectAttributes ra) {

        // 관리자가 탈퇴 처리하면 대기 상태 없이 즉시 DB에서 완전히 삭제한다.
        try {
            adminService.deleteMember(memberNo);
            ra.addFlashAttribute("message", "회원 정보가 완전히 삭제되었습니다.");
        } catch (IllegalStateException e) {
            ra.addFlashAttribute("message", e.getMessage());
        }

        return "redirect:" + memberListRedirectUrl(keyword, searchType, status, memberType, page);
    }

    @PostMapping("/member/restore")
    public String restoreMember(
            @RequestParam Long memberNo,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String searchType,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String memberType,
            @RequestParam(required = false, defaultValue = "1") int page) {

        adminService.restoreMember(memberNo);
        return "redirect:" + memberListRedirectUrl(keyword, searchType, status, memberType, page);
    }

    /**
     * 목록 화면에서 체크박스로 선택한 회원들을 정지 / 복구 / 완전삭제 중 하나로 일괄 처리한다.
     */
    @PostMapping("/member/bulk")
    public String memberBulkAction(
            @RequestParam String action,
            @RequestParam(required = false) List<Long> memberNos,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String searchType,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String memberType,
            @RequestParam(required = false, defaultValue = "1") int page,
            RedirectAttributes ra) {

        if (memberNos == null || memberNos.isEmpty()) {
            ra.addFlashAttribute("message", "선택된 회원이 없습니다.");
            return "redirect:" + memberListRedirectUrl(keyword, searchType, status, memberType, page);
        }

        try {
            int skippedCount = adminService.bulkMemberAction(memberNos, action);
            int processedCount = memberNos.size() - skippedCount;

            StringBuilder message = new StringBuilder(processedCount + "명의 회원을 처리했습니다.");
            if (skippedCount > 0) {
                // 자동삭제 대기 중인 회원은 화면에서 선택이 막혀 있지만,
                // 우회 요청 등으로 포함된 경우를 대비해 실제로 몇 명이 제외되었는지 안내한다.
                message.append(" (자동삭제 예정 회원 ").append(skippedCount).append("명은 처리에서 제외되었습니다.)");
            }
            ra.addFlashAttribute("message", message.toString());

        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("message", e.getMessage());
        }

        return "redirect:" + memberListRedirectUrl(keyword, searchType, status, memberType, page);
    }

    /**
     * PageVO에는 계산 로직이 없으므로(값을 담는 순수 VO), 여기서 현재 페이지/전체 페이지 수를 계산해 채운다.
     * 한 페이지에 MEMBER_PAGE_SIZE(10)건, 페이지 번호는 MEMBER_PAGE_BLOCK_SIZE(5)개 단위로 끊어서
     * 보여준다.
     */
    private PageVO buildMemberPagination(int requestedPage, int totalCount) {

        int totalPage = (int) Math.ceil((double) totalCount / MEMBER_PAGE_SIZE);
        if (totalPage < 1) {
            totalPage = 1;
        }

        int currentPage = requestedPage;
        if (currentPage < 1) {
            currentPage = 1;
        } else if (currentPage > totalPage) {
            currentPage = totalPage;
        }

        int startPage = ((currentPage - 1) / MEMBER_PAGE_BLOCK_SIZE) * MEMBER_PAGE_BLOCK_SIZE + 1;
        int endPage = Math.min(startPage + MEMBER_PAGE_BLOCK_SIZE - 1, totalPage);

        PageVO pageVO = new PageVO();
        pageVO.setCurrentPage(currentPage);
        pageVO.setPageSize(MEMBER_PAGE_SIZE);
        pageVO.setTotalCount(totalCount);
        pageVO.setTotalPage(totalPage);
        pageVO.setStartPage(startPage);
        pageVO.setEndPage(endPage);
        pageVO.setPrev(startPage > 1);
        pageVO.setNext(endPage < totalPage);

        return pageVO;
    }

    /**
     * 개별/일괄 처리 후 방금 보고 있던 검색어·필터·페이지 상태 그대로 목록으로 돌아가기 위한 리다이렉트 URL을 만든다.
     */
    private String memberListRedirectUrl(String keyword, String searchType, String status, String memberType,
            int page) {

        UriComponentsBuilder builder = UriComponentsBuilder.fromPath("/admin/member/list")
                .queryParam("page", page);

        if (keyword != null && !keyword.isBlank()) {
            builder.queryParam("keyword", keyword);
        }
        if (searchType != null && !searchType.isBlank()) {
            builder.queryParam("searchType", searchType);
        }
        if (status != null && !status.isBlank()) {
            builder.queryParam("status", status);
        }
        if (memberType != null && !memberType.isBlank()) {
            builder.queryParam("memberType", memberType);
        }

        return builder.build().toUriString();
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
            @RequestParam(required = false) String period,
            @RequestParam(required = false) String keyword) {

        model.addAttribute("activeMenu", "event");

        model.addAttribute(
                "eventRequestList",
                adminService.getEventList(tab, keyword, period));

        model.addAttribute("eventStats", adminService.getEventStats());

        return "admin/event/eventManage";
    }

    @PostMapping("/event/approve")
    public String eventApprove(
            @RequestParam Long eventNo,
            @RequestParam(required = false) String tab,
            @RequestParam(required = false) String period,
            @RequestParam(required = false) String keyword) {

        adminService.approveEvent(eventNo);

        return "redirect:" + eventListRedirectUrl(tab, period, keyword);
    }

    @PostMapping("/event/reject")
    public String eventReject(
            @RequestParam Long eventNo,
            @RequestParam(required = false) String tab,
            @RequestParam(required = false) String period,
            @RequestParam(required = false) String keyword) {

        adminService.rejectEvent(eventNo);

        return "redirect:" + eventListRedirectUrl(tab, period, keyword);
    }


    /**
     * 이벤트 승인/반려 처리 후 방금 보고 있던 상태·기간·검색어 필터 그대로 목록으로 돌아가기 위한 리다이렉트 URL을 만든다.
     */
    private String eventListRedirectUrl(String tab, String period, String keyword) {

        UriComponentsBuilder builder = UriComponentsBuilder.fromPath("/admin/event/list");

        if (tab != null && !tab.isBlank()) {
            builder.queryParam("tab", tab);
        }
        if (period != null && !period.isBlank()) {
            builder.queryParam("period", period);
        }
        if (keyword != null && !keyword.isBlank()) {
            builder.queryParam("keyword", keyword);
        }

        return builder.build().toUriString();
    }

    // ===================== 4. 상품 관리 (사업자 등록/수정/삭제 요청 처리) =====================
    //
    // [정리됨] eventManage.jsp / memberManage.jsp와 톤앤매너를 통일하면서
    // tab 파라미터의 의미도 "요청 유형(register/update/delete)"에서
    // EVENT 관리와 동일한 "PRODUCT.STATUS 값" 기준으로 정리했다.
    // tab: 빈값(전체) / waiting(승인 대기) / approved(승인 완료) / delete(삭제 요청)

    @GetMapping("/product/list")
    public String productList(Model model,
            @RequestParam(required = false) String tab,
            @RequestParam(required = false) String keyword) {

        model.addAttribute("activeMenu", "product");
        model.addAttribute("productRequestList", adminService.getProductRequestList(tab, keyword));
        model.addAttribute("productStats", adminService.getProductStats());

        return "admin/goods/productManage";
    }

    @PostMapping("/product/approve")
    public String productApprove(
            @RequestParam Long productNo,
            @RequestParam(required = false) String tab,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            RedirectAttributes redirectAttributes) {

        try {
            adminService.approveProduct(productNo);

            // 처리 대상 상품이 삭제 요청 상태였다면 실제 DB 삭제가 완료되었다는 메시지를 보여준다.
            // (모달을 열 때 넘겨받은 해당 상품의 상태값을 기준으로 판단하므로,
            //  '전체' 탭에서 삭제 요청 건을 승인하는 경우에도 정확한 안내 문구가 나온다.)
            if ("DELETE_REQUESTED".equals(status)) {
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

        return "redirect:" + productListRedirectUrl(tab, keyword);
    }

    @PostMapping("/product/reject")
    public String productReject(
            @RequestParam Long productNo,
            @RequestParam(required = false) String tab,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            RedirectAttributes redirectAttributes) {

        try {
            adminService.rejectProduct(productNo);

            // 삭제 요청 반려 시에는 상품을 기존 승인 상태로 복구한다.
            if ("DELETE_REQUESTED".equals(status)) {
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

        return "redirect:" + productListRedirectUrl(tab, keyword);
    }

    /**
     * 상품 승인/반려 처리 후 방금 보고 있던 상태(tab)·검색어(keyword) 필터
     * 그대로 목록으로 돌아가기 위한 리다이렉트 URL을 만든다.
     * (eventListRedirectUrl과 동일한 방식. 예전에는 keyword가 유지되지 않아
     * 검색 중 승인/반려하면 검색 결과가 초기화되는 문제가 있었다.)
     */
    private String productListRedirectUrl(String tab, String keyword) {

        UriComponentsBuilder builder = UriComponentsBuilder.fromPath("/admin/product/list");

        if (tab != null && !tab.isBlank()) {
            builder.queryParam("tab", tab);
        }
        if (keyword != null && !keyword.isBlank()) {
            builder.queryParam("keyword", keyword);
        }

        return builder.build().toUriString();
    }

    // ===================== 5. 주문/환불 조회 (조회 전용) =====================
    // 배송 상태 변경, 주문 취소, 환불 승인/거절은 사업자(Business)가 처리한다.
    // 관리자는 분쟁 확인 등을 위해 상세 내역만 조회할 수 있다.

    @GetMapping("/order/list")
    public String orderList(Model model,
            @RequestParam(required = false) String tab,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status) {
        model.addAttribute("activeMenu", "order");

        if ("refund".equals(tab)) {
            model.addAttribute("refundList", adminService.getRefundList(keyword, status));
        } else {
            model.addAttribute("orderList", adminService.getOrderList(keyword));
        }

        return "admin/order/orderManage";
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

    /* [수정] 화면에서 전달한 사업자 번호와 정산 월을 기준으로 일괄 확인한다. */
    @PostMapping("/settlement/confirm")
    public String settlementConfirm(
            @RequestParam Long businessNo,
            @RequestParam String settlementMonth) {
        adminService.confirmSettlement(businessNo, settlementMonth);
        return "redirect:/admin/settlement/main";
    }

    /* [수정] 화면에서 전달한 사업자 번호와 정산 월을 기준으로 일괄 반려한다. */
    @PostMapping("/settlement/reject")
    public String settlementReject(
            @RequestParam Long businessNo,
            @RequestParam String settlementMonth) {
        adminService.rejectSettlement(businessNo, settlementMonth);
        return "redirect:/admin/settlement/main";
    }

    // ===================== 8. 시스템 관리 (모니터링) =====================

    @GetMapping("/monitoring")
    public String monitoring(Model model) {
        model.addAttribute("activeMenu", "monitoring");
        model.addAttribute("monitoringList", adminService.getMonitoringList());

        List<VisitorTrendVO> visitorTrend = adminService.getVisitorTrend();
        List<PopularClickVO> popularClicks = adminService.getPopularProductClicks();

        model.addAttribute("visitorTrend", visitorTrend);
        model.addAttribute("popularClicks", popularClicks);

        // admin.js는 정적 파일이라 JSTL/EL을 쓸 수 없으므로,
        // 차트에 필요한 데이터를 여기서 JSON 문자열로 만들어 JSP의 data-* 속성으로 넘긴다.
        JSONArray visitorJson = new JSONArray();
        for (VisitorTrendVO v : visitorTrend) {
            JSONObject o = new JSONObject();
            o.put("date", v.getAccessDate());
            o.put("count", v.getVisitorCount());
            visitorJson.put(o);
        }
        model.addAttribute("visitorTrendJson", visitorJson.toString());

        JSONArray popularJson = new JSONArray();
        for (PopularClickVO p : popularClicks) {
            JSONObject o = new JSONObject();
            o.put("name", p.getProductName());
            o.put("count", p.getClickCount());
            popularJson.put(o);
        }
        model.addAttribute("popularClicksJson", popularJson.toString());

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