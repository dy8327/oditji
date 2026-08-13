package com.project.oditji.admin.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.project.oditji.admin.service.AdminService;

/**
 * AdminController의 null/empty 선택값과 탭 분기 양쪽을 추가 검증합니다.
 */
class AdminControllerMoreConditionCoverageTest {

    private AdminService adminService;
    private AdminController controller;
    private RedirectAttributes redirectAttributes;

    @BeforeEach
    void setUp() {
        adminService = mock(AdminService.class);
        redirectAttributes = mock(RedirectAttributes.class);
        controller = new AdminController(adminService);
    }

    @Test
    void bulkActionsShouldCoverEmptyListsSeparatelyFromNullLists() {
        assertEquals(
                "redirect:/admin/member/list?page=1",
                controller.memberBulkAction(
                        "suspend",
                        List.of(),
                        null,
                        null,
                        null,
                        null,
                        1,
                        redirectAttributes));

        assertEquals(
                "redirect:/admin/review/list?page=1",
                controller.reviewBulkAction(
                        "delete",
                        List.of(),
                        null,
                        null,
                        null,
                        1,
                        redirectAttributes));

        assertEquals(
                "redirect:/admin/productReview/list?page=1",
                controller.productReviewBulkAction(
                        "delete",
                        List.of(),
                        null,
                        null,
                        null,
                        1,
                        redirectAttributes));
    }

    @Test
    void bulkMessagesShouldCoverSkippedZeroAndPositiveBranches() {
        when(adminService.bulkMemberAction(
                List.of(1L, 2L),
                "restore"))
                .thenReturn(0);

        controller.memberBulkAction(
                "restore",
                List.of(1L, 2L),
                null,
                null,
                null,
                null,
                1,
                redirectAttributes);

        verify(redirectAttributes)
                .addFlashAttribute(
                        "message",
                        "2명의 회원을 처리했습니다.");

        when(adminService.bulkContentReviewAction(
                List.of(10L, 11L),
                "approve"))
                .thenReturn(1);

        controller.reviewBulkAction(
                "approve",
                List.of(10L, 11L),
                null,
                null,
                null,
                1,
                redirectAttributes);

        verify(redirectAttributes)
                .addFlashAttribute(
                        "message",
                        "1건의 리뷰를 승인(리뷰 삭제) 처리했습니다. (이미 처리되었거나 대상이 아닌 1건은 제외되었습니다.)");
    }

    @Test
    void productApproveAndRejectShouldCoverDeleteRequestedAndNormalMessages() {
        controller.productApprove(
                1L,
                null,
                null,
                null,
                "DELETE_REQUESTED",
                1,
                redirectAttributes);

        verify(redirectAttributes)
                .addFlashAttribute(
                        "message",
                        "상품 삭제 요청을 승인하여 상품을 최종 삭제했습니다.");

        controller.productReject(
                2L,
                null,
                null,
                null,
                "WAITING",
                1,
                redirectAttributes);

        verify(redirectAttributes)
                .addFlashAttribute(
                        "message",
                        "상품 요청을 반려했습니다.");
    }

    @Test
    void orderAndBusinessListsShouldCoverBothTabBranches() {
        when(adminService.getOrderListCount(null))
                .thenReturn(0);
        when(adminService.getRefundListCount(null, null))
                .thenReturn(0);
        when(adminService.getBusinessListCount(null, null))
                .thenReturn(0);
        when(adminService.getBusinessApprovalListCount(null, null))
                .thenReturn(0);

        ExtendedModelMap model = new ExtendedModelMap();

        assertEquals(
                "admin/order/orderManage",
                controller.orderList(
                        model,
                        null,
                        null,
                        null,
                        1));

        assertEquals(
                "admin/order/orderManage",
                controller.orderList(
                        new ExtendedModelMap(),
                        "refund",
                        null,
                        null,
                        1));

        assertEquals(
                "admin/business/businessManage",
                controller.businessList(
                        new ExtendedModelMap(),
                        null,
                        null,
                        null,
                        1));

        assertEquals(
                "admin/business/businessManage",
                controller.businessList(
                        new ExtendedModelMap(),
                        "approval",
                        null,
                        null,
                        1));
    }
}
