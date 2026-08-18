package com.project.oditji.admin.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ui.ExtendedModelMap;

import com.project.oditji.admin.service.AdminService;
import com.project.oditji.admin.vo.MonitoringSummaryVO;

/** 관리자 모니터링 기간 switch와 7일/장기 추이 분기의 잔여 커버리지를 보완합니다. */
class AdminControllerResidualClosure7Test {

    private AdminService adminService;
    private AdminController controller;

    @BeforeEach
    void setUp() {
        adminService = mock(AdminService.class);
        controller = new AdminController(adminService);

        when(adminService.getMonitoringList()).thenReturn(List.of());
        when(adminService.getMonitoringSummaryByPeriod(anyString()))
                .thenReturn(new MonitoringSummaryVO());
        when(adminService.getVisitorTrendByPeriod(anyString()))
                .thenReturn(List.of());
        when(adminService.getVisitorTrend()).thenReturn(List.of());
        when(adminService.getPopularProductClicksByPeriod(anyString()))
                .thenReturn(List.of());
    }

    @Test
    void monitoringShouldKeepEverySupportedLongPeriodAndUsePeriodTrendQuery() {
        for (String period : List.of("3m", "6m", "1y")) {
            ExtendedModelMap model = new ExtendedModelMap();

            assertEquals(
                    "admin/monitoring/monitoring",
                    controller.monitoring(period, model));
            assertEquals(period, model.get("period"));
        }

        verify(adminService).getVisitorTrendByPeriod("3m");
        verify(adminService).getVisitorTrendByPeriod("6m");
        verify(adminService).getVisitorTrendByPeriod("1y");
        verify(adminService, times(3)).getMonitoringList();
    }

    @Test
    void monitoringShouldNormalizeUnsupportedPeriodToSevenDays() {
        ExtendedModelMap model = new ExtendedModelMap();

        assertEquals(
                "admin/monitoring/monitoring",
                controller.monitoring("unsupported", model));
        assertEquals("7d", model.get("period"));

        verify(adminService).getMonitoringSummaryByPeriod("7d");
        verify(adminService).getVisitorTrend();
        verify(adminService).getPopularProductClicksByPeriod("7d");
    }
}
