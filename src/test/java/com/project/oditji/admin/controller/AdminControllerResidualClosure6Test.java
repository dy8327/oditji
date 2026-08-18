package com.project.oditji.admin.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.project.oditji.admin.service.AdminService;

/** 관리자 일괄 처리의 skipped=0 경로와 상품 상태 메시지 잔여 분기를 보완합니다. */
class AdminControllerResidualClosure6Test {

    private AdminService adminService;
    private AdminController controller;

    @BeforeEach
    void setUp() {
        adminService = mock(AdminService.class);
        controller = new AdminController(adminService);
    }

    @Test
    void memberAndContentReviewBulkShouldCoverZeroSkippedBranch() {
        RedirectAttributes memberRedirect = mock(RedirectAttributes.class);
        RedirectAttributes reviewRedirect = mock(RedirectAttributes.class);

        when(adminService.bulkMemberAction(List.of(1L), "restore"))
                .thenReturn(0);
        when(adminService.bulkContentReviewAction(List.of(2L), "approve"))
                .thenReturn(0);

        assertEquals(
                "redirect:/admin/member/list?page=1",
                controller.memberBulkAction(
                        "restore",
                        List.of(1L),
                        null,
                        null,
                        null,
                        null,
                        1,
                        memberRedirect));

        assertEquals(
                "redirect:/admin/review/list?page=1",
                controller.reviewBulkAction(
                        "approve",
                        List.of(2L),
                        null,
                        null,
                        null,
                        1,
                        reviewRedirect));

        verify(memberRedirect).addFlashAttribute(
                "message",
                "1명의 회원을 처리했습니다.");
        verify(reviewRedirect).addFlashAttribute(
                "message",
                "1건의 리뷰를 승인(리뷰 삭제) 처리했습니다.");
    }

    @Test
    void productHandlersShouldCoverNullAndDeleteRequestedStatusMessages() {
        RedirectAttributes approveRedirect = mock(RedirectAttributes.class);
        RedirectAttributes rejectRedirect = mock(RedirectAttributes.class);

        controller.productApprove(
                10L,
                null,
                null,
                null,
                null,
                1,
                approveRedirect);

        controller.productReject(
                11L,
                null,
                null,
                null,
                "DELETE_REQUESTED",
                1,
                rejectRedirect);

        verify(approveRedirect).addFlashAttribute(
                "message",
                "상품 요청을 승인했습니다.");
        verify(rejectRedirect).addFlashAttribute(
                "message",
                "상품 삭제 요청을 반려했습니다.");
    }
}
