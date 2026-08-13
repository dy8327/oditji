package com.project.oditji.admin.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import com.project.oditji.admin.service.AdminService;

/** 정산 확인/반려 컨트롤러의 기존 테스트 반대쪽 성공·예외 분기를 보완합니다. */
class AdminControllerSettlementOppositeBranchCoverageTest {

    private AdminService adminService;
    private AdminController controller;

    @BeforeEach
    void setUp() {
        adminService = mock(AdminService.class);
        controller = new AdminController(adminService);
    }

    @Test
    void confirmShouldExposeServiceFailureMessageAndKeepFilters() {
        doThrow(new IllegalArgumentException("처리할 수 없는 요청입니다."))
                .when(adminService)
                .confirmSettlement(20L);

        RedirectAttributesModelMap redirectAttributes =
                new RedirectAttributesModelMap();

        String view = controller.settlementConfirm(
                20L,
                "상점",
                "REQUESTED",
                "2026-08",
                3,
                redirectAttributes);

        assertEquals(
                "처리할 수 없는 요청입니다.",
                redirectAttributes.getFlashAttributes().get("message"));
        assertEquals(
                "redirect:/admin/settlement/main?page=3&keyword=상점&status=REQUESTED&period=2026-08",
                view);
    }

    @Test
    void rejectShouldCoverSuccessMessageAndMinimalRedirect() {
        RedirectAttributesModelMap redirectAttributes =
                new RedirectAttributesModelMap();

        String view = controller.settlementReject(
                21L,
                "계좌 확인",
                null,
                null,
                null,
                1,
                redirectAttributes);

        assertEquals(
                "정산 요청을 반려했습니다.",
                redirectAttributes.getFlashAttributes().get("message"));
        assertEquals(
                "redirect:/admin/settlement/main?page=1",
                view);
        verify(adminService).rejectSettlement(21L, "계좌 확인");
    }
}
