package com.project.oditji.admin.service;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.project.oditji.admin.dao.AdminDAO;
import com.project.oditji.notification.service.NotificationService;

/** 관리자 화면 통계 조회용 단순 DAO 위임 메서드의 잔여 라인을 보완합니다. */
class AdminServiceImplStatsGetterCoverageTest {

    @TempDir
    Path tempDirectory;

    private AdminDAO adminDAO;
    private AdminServiceImpl service;

    @BeforeEach
    void setUp() {
        adminDAO = mock(AdminDAO.class);
        service = new AdminServiceImpl(
                adminDAO,
                mock(NotificationService.class),
                tempDirectory.toString());
    }

    @Test
    void everyStatsGetterShouldDelegateToItsDaoQuery() {
        assertNull(service.getDashboardStats());
        assertNull(service.getMemberStats());
        assertNull(service.getContentReviewStats());
        assertNull(service.getProductReviewStats());
        assertNull(service.getEventStats());
        assertNull(service.getProductStats());
        assertNull(service.getOrderStats());
        assertNull(service.getBusinessStats());
        assertNull(service.getSettlementStats());

        verify(adminDAO).selectDashboardStats();
        verify(adminDAO).selectMemberStats();
        verify(adminDAO).selectContentReviewStats();
        verify(adminDAO).selectProductReviewStats();
        verify(adminDAO).selectEventStats();
        verify(adminDAO).selectProductStats();
        verify(adminDAO).selectOrderStats();
        verify(adminDAO).selectBusinessStats();
        verify(adminDAO).selectSettlementStats();
    }
}
