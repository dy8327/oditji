package com.project.oditji.admin.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.project.oditji.admin.service.AdminService;

/** 관리자 컨트롤러의 예외 메시지와 빈 모니터링 목록 잔여 경로를 보완합니다. */
class AdminControllerResidualClosure3Test {

    private AdminService adminService;
    private AdminController controller;

    @BeforeEach
    void setUp() {
        adminService = mock(AdminService.class);
        controller = new AdminController(adminService);
    }

    @Test
    void memberBulkActionShouldExposeServiceValidationMessage() {
        RedirectAttributes redirectAttributes = mock(RedirectAttributes.class);
        doThrow(new IllegalArgumentException("잘못된 처리 유형"))
                .when(adminService)
                .bulkMemberAction(List.of(1L), "unknown");

        String result = controller.memberBulkAction(
                "unknown",
                List.of(1L),
                null,
                null,
                null,
                null,
                1,
                redirectAttributes);

        assertEquals("redirect:/admin/member/list?page=1", result);
        verify(redirectAttributes).addFlashAttribute("message", "잘못된 처리 유형");
    }

    @Test
    void settlementHandlersShouldExposeServiceFailuresAndPreserveRedirectFilters() {
        RedirectAttributes confirmRedirect = mock(RedirectAttributes.class);
        RedirectAttributes rejectRedirect = mock(RedirectAttributes.class);

        doThrow(new IllegalStateException("지급 실패"))
                .when(adminService)
                .confirmSettlement(10L);
        doThrow(new IllegalArgumentException("반려 실패"))
                .when(adminService)
                .rejectSettlement(11L, "사유");

        String confirm = controller.settlementConfirm(
                10L,
                "상점",
                "REQUESTED",
                "2026-08",
                2,
                confirmRedirect);
        String reject = controller.settlementReject(
                11L,
                "사유",
                "상점",
                "REQUESTED",
                "2026-08",
                3,
                rejectRedirect);

        verify(confirmRedirect).addFlashAttribute("message", "지급 실패");
        verify(rejectRedirect).addFlashAttribute("message", "반려 실패");
        org.junit.jupiter.api.Assertions.assertTrue(confirm.contains("page=2"));
        org.junit.jupiter.api.Assertions.assertTrue(reject.contains("page=3"));
    }

    @Test
    void monitoringShouldAlsoHandleEmptyChartLists() {
        when(adminService.getMonitoringList()).thenReturn(List.of());
        when(adminService.getVisitorTrend()).thenReturn(List.of());
        when(adminService.getPopularProductClicks()).thenReturn(List.of());

        ExtendedModelMap model = new ExtendedModelMap();
        assertEquals("admin/monitoring/monitoring", controller.monitoring(model));
        assertEquals("[]", model.get("visitorTrendJson"));
        assertEquals("[]", model.get("popularClicksJson"));
    }
}
