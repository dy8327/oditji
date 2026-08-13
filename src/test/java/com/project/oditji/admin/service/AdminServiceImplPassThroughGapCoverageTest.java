package com.project.oditji.admin.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.project.oditji.admin.dao.AdminDAO;
import com.project.oditji.admin.vo.ContentManageVO;
import com.project.oditji.admin.vo.PlatformVO;
import com.project.oditji.notification.service.NotificationService;

/** 관리자 서비스의 단순 목록 위임 및 콘텐츠/플랫폼 관리 잔여 라인을 보완합니다. */
class AdminServiceImplPassThroughGapCoverageTest {

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
    void listDelegatesShouldReturnDaoDefaultsAcrossRemainingAdminSections() {
        assertTrue(service.getEventList("waiting", "key", "month", 0, 10).isEmpty());
        assertEquals(0, service.getEventListCount("waiting", "key", "month"));

        assertTrue(service.getProductRequestList("waiting", "goods", "name", 0, 10).isEmpty());
        assertEquals(0, service.getProductRequestListCount("waiting", "goods", "name"));

        assertTrue(service.getOrderList("order", 0, 10).isEmpty());
        assertEquals(0, service.getOrderListCount("order"));

        assertTrue(service.getRefundList("refund", "WAITING", 0, 10).isEmpty());
        assertEquals(0, service.getRefundListCount("refund", "WAITING"));

        assertTrue(service.getBusinessApprovalList("biz", "name", 0, 10).isEmpty());
        assertEquals(0, service.getBusinessApprovalListCount("biz", "name"));

        assertTrue(service.getSettlementList("settle", "REQUESTED", "month", 0, 10).isEmpty());
        assertEquals(0, service.getSettlementListCount("settle", "REQUESTED", "month"));
    }

    @Test
    void businessListShouldRefreshSettlementRatesOnlyWhenGradeActuallyChanges() {
        when(adminDAO.updateBusinessGradesBySales()).thenReturn(1);

        assertTrue(service.getBusinessList("biz", "name", 1, 10).isEmpty());

        verify(adminDAO).updateCurrentMonthSettlementRates();
    }

    @Test
    void contentUpdateShouldCoverNullAndPopulatedPlatformLists() {
        ContentManageVO content = new ContentManageVO();
        content.setContentNo(7L);

        service.updateContent(content, null);
        verify(adminDAO).updateContent(content);

        service.updateContent(content, List.of(1L, 2L));
        verify(adminDAO).deleteContentPlatforms(7L);
        verify(adminDAO).insertContentPlatform(7L, 1L);
        verify(adminDAO).insertContentPlatform(7L, 2L);
    }

    @Test
    void platformRegisterAndUpdateShouldDelegateDirectly() {
        PlatformVO platform = new PlatformVO();

        service.registerPlatform(platform);
        service.updatePlatform(platform);

        verify(adminDAO).insertPlatform(platform);
        verify(adminDAO).updatePlatform(platform);
    }
}
