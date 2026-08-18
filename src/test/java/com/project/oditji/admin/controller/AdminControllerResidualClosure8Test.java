package com.project.oditji.admin.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import com.project.oditji.admin.service.AdminService;

/**
 * SonarQube에 남은 관리자 리뷰 일괄 처리 switch 분기를 보완합니다.
 */
class AdminControllerResidualClosure8Test {

    private AdminService adminService;
    private AdminController controller;

    @BeforeEach
    void setUp() {
        adminService = mock(AdminService.class);
        controller = new AdminController(adminService);
    }

    @Test
    void contentReviewBulkShouldCoverApproveAndRejectLabels() {
        List<Long> approveNos = List.of(101L);
        RedirectAttributesModelMap approveRedirect =
                new RedirectAttributesModelMap();

        assertEquals(
                "redirect:/admin/review/list?page=1",
                controller.reviewBulkAction(
                        "approve",
                        approveNos,
                        null,
                        null,
                        null,
                        1,
                        approveRedirect));

        assertEquals(
                "1건의 리뷰를 승인(리뷰 삭제) 처리했습니다.",
                approveRedirect.getFlashAttributes().get("message"));
        verify(adminService).bulkContentReviewAction(
                approveNos,
                "approve");

        List<Long> rejectNos = List.of(102L);
        RedirectAttributesModelMap rejectRedirect =
                new RedirectAttributesModelMap();

        assertEquals(
                "redirect:/admin/review/list?page=1",
                controller.reviewBulkAction(
                        "reject",
                        rejectNos,
                        null,
                        null,
                        null,
                        1,
                        rejectRedirect));

        assertEquals(
                "1건의 리뷰를 반려 처리했습니다.",
                rejectRedirect.getFlashAttributes().get("message"));
        verify(adminService).bulkContentReviewAction(
                rejectNos,
                "reject");
    }

    @Test
    void productReviewBulkShouldCoverApproveLabel() {
        List<Long> reviewNos = List.of(201L);
        RedirectAttributesModelMap redirect =
                new RedirectAttributesModelMap();

        assertEquals(
                "redirect:/admin/productReview/list?page=1",
                controller.productReviewBulkAction(
                        "approve",
                        reviewNos,
                        null,
                        null,
                        null,
                        1,
                        redirect));

        assertEquals(
                "1건의 리뷰를 승인(리뷰 삭제) 처리했습니다.",
                redirect.getFlashAttributes().get("message"));
        verify(adminService).bulkProductReviewAction(
                reviewNos,
                "approve");
    }
}
