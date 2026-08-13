package com.project.oditji.admin.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import com.project.oditji.admin.service.AdminService;

/** 관리자 목록 및 개별·일괄 처리 엔드포인트의 미실행 분기를 보완합니다. */
@ExtendWith(MockitoExtension.class)
class AdminControllerExtendedCoverageTest {

    @Mock
    private AdminService adminService;

    private AdminController controller;

    @BeforeEach
    void setUp() {
        controller = new AdminController(adminService);
    }

    @Test
    void individualMemberActionsShouldRetainFiltersAndHandleDeleteFailure() {
        String suspendView = controller.memberSuspend(
                10L,
                "홍 길동",
                "name",
                "ACTIVE",
                "general",
                3);
        assertTrue(suspendView.startsWith("redirect:/admin/member/list?page=3"));
        assertTrue(suspendView.contains("memberType=general"));
        verify(adminService).suspendMember(10L);

        String restoreView = controller.restoreMember(
                11L,
                null,
                " ",
                null,
                "business",
                2);
        assertEquals(
                "redirect:/admin/member/list?page=2&memberType=business",
                restoreView);
        verify(adminService).restoreMember(11L);

        RedirectAttributesModelMap successRedirect = new RedirectAttributesModelMap();
        controller.memberWithdraw(
                12L,
                null,
                null,
                null,
                null,
                1,
                successRedirect);
        assertEquals(
                "회원 정보가 완전히 삭제되었습니다.",
                successRedirect.getFlashAttributes().get("message"));
        verify(adminService).deleteMember(12L);

        doThrow(new IllegalStateException("삭제할 수 없는 회원입니다."))
                .when(adminService)
                .deleteMember(13L);
        RedirectAttributesModelMap failureRedirect = new RedirectAttributesModelMap();
        controller.memberWithdraw(
                13L,
                null,
                null,
                null,
                null,
                1,
                failureRedirect);
        assertEquals(
                "삭제할 수 없는 회원입니다.",
                failureRedirect.getFlashAttributes().get("message"));
    }

    @Test
    void contentReviewEndpointsShouldCoverListsIndividualActionsAndBulkLabels() {
        when(adminService.getContentReviewListCount("report", "영화", "content"))
                .thenReturn(12);
        ExtendedModelMap model = new ExtendedModelMap();
        assertEquals(
                "admin/review/reviewManage",
                controller.reviewList(model, "report", "영화", "content", 2));
        assertEquals("review", model.get("activeMenu"));
        verify(adminService).getContentReviewList("report", "영화", "content", 2, 10);
        verify(adminService).getContentReviewStats();

        RedirectAttributesModelMap deleteRedirect = new RedirectAttributesModelMap();
        String deleteView = controller.reviewDelete(
                20L,
                "all",
                "작성자",
                "writer",
                4,
                deleteRedirect);
        assertTrue(deleteView.contains("tab=all"));
        assertTrue(deleteView.contains("searchType=writer"));
        assertEquals("리뷰를 삭제했습니다.", deleteRedirect.getFlashAttributes().get("message"));
        verify(adminService).deleteContentReview(20L);

        RedirectAttributesModelMap approveRedirect = new RedirectAttributesModelMap();
        controller.reviewReportApprove(21L, "report", null, null, 1, approveRedirect);
        assertEquals(
                "신고를 승인하여 리뷰를 삭제했습니다.",
                approveRedirect.getFlashAttributes().get("message"));
        verify(adminService).approveContentReviewReport(21L);

        doThrow(new IllegalStateException("이미 처리된 신고입니다."))
                .when(adminService)
                .approveContentReviewReport(22L);
        RedirectAttributesModelMap approveFailure = new RedirectAttributesModelMap();
        controller.reviewReportApprove(22L, null, null, null, 1, approveFailure);
        assertEquals(
                "이미 처리된 신고입니다.",
                approveFailure.getFlashAttributes().get("message"));

        RedirectAttributesModelMap rejectRedirect = new RedirectAttributesModelMap();
        controller.reviewReportReject(23L, "report", null, null, 1, rejectRedirect);
        assertEquals("신고를 반려했습니다.", rejectRedirect.getFlashAttributes().get("message"));
        verify(adminService).rejectContentReviewReport(23L);

        doThrow(new IllegalStateException("신고가 없습니다."))
                .when(adminService)
                .rejectContentReviewReport(24L);
        RedirectAttributesModelMap rejectFailure = new RedirectAttributesModelMap();
        controller.reviewReportReject(24L, null, null, null, 1, rejectFailure);
        assertEquals("신고가 없습니다.", rejectFailure.getFlashAttributes().get("message"));

        RedirectAttributesModelMap emptyRedirect = new RedirectAttributesModelMap();
        controller.reviewBulkAction(
                "delete",
                null,
                null,
                null,
                null,
                1,
                emptyRedirect);
        assertEquals("선택된 리뷰가 없습니다.", emptyRedirect.getFlashAttributes().get("message"));

        List<Long> reviewNos = List.of(30L, 31L, 32L);
        when(adminService.bulkContentReviewAction(reviewNos, "approve")).thenReturn(1);
        RedirectAttributesModelMap bulkRedirect = new RedirectAttributesModelMap();
        controller.reviewBulkAction(
                "approve",
                reviewNos,
                "report",
                null,
                null,
                1,
                bulkRedirect);
        assertEquals(
                "2건의 리뷰를 승인(리뷰 삭제) 처리했습니다. (이미 처리되었거나 대상이 아닌 1건은 제외되었습니다.)",
                bulkRedirect.getFlashAttributes().get("message"));

        when(adminService.bulkContentReviewAction(reviewNos, "unknown"))
                .thenThrow(new IllegalArgumentException("지원하지 않는 작업입니다."));
        RedirectAttributesModelMap invalidRedirect = new RedirectAttributesModelMap();
        controller.reviewBulkAction(
                "unknown",
                reviewNos,
                null,
                null,
                null,
                1,
                invalidRedirect);
        assertEquals(
                "지원하지 않는 작업입니다.",
                invalidRedirect.getFlashAttributes().get("message"));
    }

    @Test
    void productReviewEndpointsShouldCoverSuccessFailureAndBulkBranches() {
        when(adminService.getProductReviewListCount("all", "굿즈", "product"))
                .thenReturn(21);
        ExtendedModelMap model = new ExtendedModelMap();
        assertEquals(
                "admin/review/productReviewManage",
                controller.productReviewList(model, "all", "굿즈", "product", 3));
        verify(adminService).getProductReviewList("all", "굿즈", "product", 3, 10);
        verify(adminService).getProductReviewStats();

        RedirectAttributesModelMap deleteRedirect = new RedirectAttributesModelMap();
        controller.productReviewDelete(40L, "all", null, null, 1, deleteRedirect);
        assertEquals("리뷰를 삭제했습니다.", deleteRedirect.getFlashAttributes().get("message"));
        verify(adminService).deleteProductReview(40L);

        RedirectAttributesModelMap approveRedirect = new RedirectAttributesModelMap();
        controller.productReviewReportApprove(41L, "report", null, null, 1, approveRedirect);
        assertEquals(
                "신고를 승인하여 리뷰를 삭제했습니다.",
                approveRedirect.getFlashAttributes().get("message"));

        doThrow(new IllegalStateException("승인 실패"))
                .when(adminService)
                .approveProductReviewReport(42L);
        RedirectAttributesModelMap approveFailure = new RedirectAttributesModelMap();
        controller.productReviewReportApprove(42L, null, null, null, 1, approveFailure);
        assertEquals("승인 실패", approveFailure.getFlashAttributes().get("message"));

        RedirectAttributesModelMap rejectRedirect = new RedirectAttributesModelMap();
        controller.productReviewReportReject(43L, "report", null, null, 1, rejectRedirect);
        assertEquals("신고를 반려했습니다.", rejectRedirect.getFlashAttributes().get("message"));

        doThrow(new IllegalStateException("반려 실패"))
                .when(adminService)
                .rejectProductReviewReport(44L);
        RedirectAttributesModelMap rejectFailure = new RedirectAttributesModelMap();
        controller.productReviewReportReject(44L, null, null, null, 1, rejectFailure);
        assertEquals("반려 실패", rejectFailure.getFlashAttributes().get("message"));

        RedirectAttributesModelMap emptyRedirect = new RedirectAttributesModelMap();
        controller.productReviewBulkAction(
                "reject",
                List.of(),
                null,
                null,
                null,
                1,
                emptyRedirect);
        assertEquals("선택된 리뷰가 없습니다.", emptyRedirect.getFlashAttributes().get("message"));

        List<Long> reviewNos = List.of(50L, 51L);
        when(adminService.bulkProductReviewAction(reviewNos, "delete")).thenReturn(0);
        RedirectAttributesModelMap bulkDelete = new RedirectAttributesModelMap();
        controller.productReviewBulkAction(
                "delete",
                reviewNos,
                "all",
                null,
                null,
                2,
                bulkDelete);
        assertEquals(
                "2건의 리뷰를 삭제 처리했습니다.",
                bulkDelete.getFlashAttributes().get("message"));

        when(adminService.bulkProductReviewAction(reviewNos, "reject")).thenReturn(1);
        RedirectAttributesModelMap bulkReject = new RedirectAttributesModelMap();
        controller.productReviewBulkAction(
                "reject",
                reviewNos,
                "report",
                null,
                null,
                1,
                bulkReject);
        assertTrue(bulkReject.getFlashAttributes().get("message").toString().contains("반려"));

        when(adminService.bulkProductReviewAction(reviewNos, "invalid"))
                .thenThrow(new IllegalArgumentException("잘못된 작업"));
        RedirectAttributesModelMap invalidRedirect = new RedirectAttributesModelMap();
        controller.productReviewBulkAction(
                "invalid",
                reviewNos,
                null,
                null,
                null,
                1,
                invalidRedirect);
        assertEquals("잘못된 작업", invalidRedirect.getFlashAttributes().get("message"));
    }

    @Test
    void eventAndProductRequestEndpointsShouldCoverMessageAndErrorBranches() {
        when(adminService.getEventListCount("waiting", "여름", "month")).thenReturn(18);
        ExtendedModelMap eventModel = new ExtendedModelMap();
        assertEquals(
                "admin/event/eventManage",
                controller.eventList(eventModel, "waiting", "month", "여름", 2));
        verify(adminService).getEventList("waiting", "여름", "month", 2, 10);
        verify(adminService).getEventStats();

        RedirectAttributesModelMap eventApprove = new RedirectAttributesModelMap();
        String eventView = controller.eventApprove(
                60L,
                "waiting",
                "month",
                "여름 이벤트",
                2,
                eventApprove);
        assertTrue(eventView.contains("tab=waiting"));
        assertTrue(eventView.contains("period=month"));
        assertEquals("이벤트 요청을 승인했습니다.", eventApprove.getFlashAttributes().get("message"));

        doThrow(new IllegalArgumentException("승인할 수 없습니다."))
                .when(adminService)
                .approveEvent(61L);
        RedirectAttributesModelMap eventApproveFailure = new RedirectAttributesModelMap();
        controller.eventApprove(61L, null, null, null, 1, eventApproveFailure);
        assertEquals("승인할 수 없습니다.", eventApproveFailure.getFlashAttributes().get("message"));

        RedirectAttributesModelMap eventReject = new RedirectAttributesModelMap();
        controller.eventReject(62L, null, null, null, 1, eventReject);
        assertEquals("이벤트 요청을 반려했습니다.", eventReject.getFlashAttributes().get("message"));

        doThrow(new IllegalStateException("이미 처리되었습니다."))
                .when(adminService)
                .rejectEvent(63L);
        RedirectAttributesModelMap eventRejectFailure = new RedirectAttributesModelMap();
        controller.eventReject(63L, null, null, null, 1, eventRejectFailure);
        assertEquals("이미 처리되었습니다.", eventRejectFailure.getFlashAttributes().get("message"));

        when(adminService.getProductRequestListCount("waiting", "포스터", "product"))
                .thenReturn(8);
        ExtendedModelMap productModel = new ExtendedModelMap();
        assertEquals(
                "admin/goods/productManage",
                controller.productList(productModel, "waiting", "포스터", "product", 1));
        verify(adminService).getProductRequestList("waiting", "포스터", "product", 1, 10);
        verify(adminService).getProductStats();

        RedirectAttributesModelMap approveNormal = new RedirectAttributesModelMap();
        controller.productApprove(70L, "waiting", null, null, "WAITING", 1, approveNormal);
        assertEquals("상품 요청을 승인했습니다.", approveNormal.getFlashAttributes().get("message"));

        RedirectAttributesModelMap approveDelete = new RedirectAttributesModelMap();
        controller.productApprove(
                71L,
                "delete",
                null,
                null,
                "DELETE_REQUESTED",
                1,
                approveDelete);
        assertEquals(
                "상품 삭제 요청을 승인하여 상품을 최종 삭제했습니다.",
                approveDelete.getFlashAttributes().get("message"));

        doThrow(new IllegalStateException("상품 승인 실패"))
                .when(adminService)
                .approveProduct(72L);
        RedirectAttributesModelMap approveFailure = new RedirectAttributesModelMap();
        controller.productApprove(72L, null, null, null, null, 1, approveFailure);
        assertEquals("상품 승인 실패", approveFailure.getFlashAttributes().get("message"));

        RedirectAttributesModelMap rejectNormal = new RedirectAttributesModelMap();
        controller.productReject(73L, "waiting", null, null, "WAITING", 1, rejectNormal);
        assertEquals("상품 요청을 반려했습니다.", rejectNormal.getFlashAttributes().get("message"));

        RedirectAttributesModelMap rejectDelete = new RedirectAttributesModelMap();
        controller.productReject(
                74L,
                "delete",
                null,
                null,
                "DELETE_REQUESTED",
                1,
                rejectDelete);
        assertEquals("상품 삭제 요청을 반려했습니다.", rejectDelete.getFlashAttributes().get("message"));

        doThrow(new IllegalArgumentException("상품 반려 실패"))
                .when(adminService)
                .rejectProduct(75L);
        RedirectAttributesModelMap rejectFailure = new RedirectAttributesModelMap();
        controller.productReject(75L, null, null, null, null, 1, rejectFailure);
        assertEquals("상품 반려 실패", rejectFailure.getFlashAttributes().get("message"));
    }

    @Test
    void orderAndBusinessListsShouldCoverBothTabsAndBusinessActions() {
        when(adminService.getOrderListCount("주문")).thenReturn(15);
        ExtendedModelMap orderModel = new ExtendedModelMap();
        assertEquals(
                "admin/order/orderManage",
                controller.orderList(orderModel, "order", "주문", null, 2));
        verify(adminService).getOrderList("주문", 2, 10);
        verify(adminService).getOrderStats();

        when(adminService.getRefundListCount("환불", "WAITING")).thenReturn(25);
        ExtendedModelMap refundModel = new ExtendedModelMap();
        assertEquals(
                "admin/order/orderManage",
                controller.orderList(refundModel, "refund", "환불", "WAITING", 3));
        verify(adminService).getRefundList("환불", "WAITING", 3, 10);

        when(adminService.getBusinessListCount("상점", "name")).thenReturn(11);
        ExtendedModelMap businessModel = new ExtendedModelMap();
        assertEquals(
                "admin/business/businessManage",
                controller.businessList(businessModel, "info", "상점", "name", 2));
        verify(adminService).getBusinessList("상점", "name", 2, 10);
        verify(adminService).getBusinessStats();

        when(adminService.getBusinessApprovalListCount("대기", "id")).thenReturn(22);
        ExtendedModelMap approvalModel = new ExtendedModelMap();
        controller.businessList(approvalModel, "approval", "대기", "id", 3);
        verify(adminService).getBusinessApprovalList("대기", "id", 3, 10);

        String gradeView = controller.businessGrade(80L, "GOLD", "상점", "name", 4);
        assertTrue(gradeView.contains("tab=info"));
        assertTrue(gradeView.contains("page=4"));
        verify(adminService).updateBusinessGrade(80L, "GOLD");

        String approveView = controller.businessApprove(81L, "대기", "id", 2);
        assertTrue(approveView.contains("tab=approval"));
        verify(adminService).approveBusiness(81L);

        String rejectView = controller.businessReject(82L, null, null, 1);
        assertEquals(
                "redirect:/admin/business/list?tab=approval&page=1",
                rejectView);
        verify(adminService).rejectBusiness(82L);
    }

    @Test
    void settlementListShouldPopulateModelAndClampOutOfRangePage() {
        when(adminService.getSettlementListCount("상점", "WAITING", "month"))
                .thenReturn(12);
        ExtendedModelMap model = new ExtendedModelMap();

        assertEquals(
                "admin/settlement/settlementManage",
                controller.settlementMain(model, "상점", "WAITING", "month", 99));
        verify(adminService).getSettlementList("상점", "WAITING", "month", 2, 10);
        verify(adminService).getSettlementStats();
        assertEquals("settlement", model.get("activeMenu"));
    }
}