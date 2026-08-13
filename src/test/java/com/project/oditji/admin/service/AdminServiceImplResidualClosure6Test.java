package com.project.oditji.admin.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.project.oditji.admin.dao.AdminDAO;
import com.project.oditji.notification.service.NotificationService;

/**
 * 관리자 리뷰 일괄 처리의 아직 직접 타지 않은 switch/catch 경로를 보완합니다.
 */
class AdminServiceImplResidualClosure6Test {

    @TempDir
    Path tempDirectory;

    private AdminDAO adminDAO;
    private NotificationService notificationService;
    private AdminServiceImpl service;

    @BeforeEach
    void setUp() {
        adminDAO = mock(AdminDAO.class);
        notificationService = mock(NotificationService.class);
        service = new AdminServiceImpl(
                adminDAO,
                notificationService,
                tempDirectory.toString());
    }

    @Test
    void bulkContentReviewShouldCoverApproveSuccessFailureAndDeleteCases() {
        when(adminDAO.selectWaitingContentReviewReporterMemberNos(1L)).thenReturn(List.of());
        when(adminDAO.selectWaitingContentReviewReporterMemberNos(2L)).thenReturn(List.of());
        when(adminDAO.updateContentReviewReportStatus(1L, "ACCEPTED")).thenReturn(1);
        when(adminDAO.updateContentReviewReportStatus(2L, "ACCEPTED")).thenReturn(0);

        assertEquals(
                1,
                service.bulkContentReviewAction(List.of(1L, 2L), "approve"));
        verify(adminDAO).deleteContentReview(1L);
        verify(adminDAO, never()).deleteContentReview(2L);

        assertEquals(
                0,
                service.bulkContentReviewAction(List.of(3L), "delete"));
        verify(adminDAO).deleteContentReview(3L);
    }

    @Test
    void bulkProductReviewShouldCoverRejectSuccessAndApproveFailureCatch() {
        when(adminDAO.selectWaitingProductReviewReporterMemberNos(10L)).thenReturn(List.of());
        when(adminDAO.updateProductReviewReportStatus(10L, "REJECTED")).thenReturn(1);

        assertEquals(
                0,
                service.bulkProductReviewAction(List.of(10L), "reject"));

        when(adminDAO.selectWaitingProductReviewReporterMemberNos(11L)).thenReturn(List.of());
        when(adminDAO.updateProductReviewReportStatus(11L, "ACCEPTED")).thenReturn(0);

        assertEquals(
                1,
                service.bulkProductReviewAction(List.of(11L), "approve"));
        verify(adminDAO, never()).adminDeleteProductReview(11L);
    }
}
