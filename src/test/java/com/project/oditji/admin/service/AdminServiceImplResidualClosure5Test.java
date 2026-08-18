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
import org.springframework.test.util.ReflectionTestUtils;

import com.project.oditji.admin.dao.AdminDAO;
import com.project.oditji.notification.service.NotificationService;

/** 최근 bulk 회원 처리 리팩터링과 신고 결과 알림의 잔여 조건을 닫습니다. */
class AdminServiceImplResidualClosure5Test {

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
    void bulkMemberActionShouldCoverEveryOperandOfSingleContinueFlow() {
        List<Long> deleteTargets = List.of(1L, 2L, 3L);
        when(adminDAO.selectWithdrawnMemberNos(deleteTargets)).thenReturn(List.of(1L));
        when(adminDAO.isBusinessMember(2L)).thenReturn(true);
        when(adminDAO.isBusinessMember(3L)).thenReturn(false);

        assertEquals(2, service.bulkMemberAction(deleteTargets, "delete"));

        verify(adminDAO, never()).isBusinessMember(1L);
        verify(adminDAO, never()).deleteMember(1L);
        verify(adminDAO, never()).deleteMember(2L);
        verify(adminDAO).deleteMember(3L);

        List<Long> suspendTargets = List.of(4L);
        when(adminDAO.selectWithdrawnMemberNos(suspendTargets)).thenReturn(List.of());

        assertEquals(0, service.bulkMemberAction(suspendTargets, "suspend"));

        verify(adminDAO, never()).isBusinessMember(4L);
        verify(adminDAO).updateMemberStatus(4L, "BLOCKED");
    }

    @Test
    void reviewNotificationShouldCoverNullGuardAndBothTernaryDirections() {
        ReflectionTestUtils.invokeMethod(
                service,
                "createReviewReportResultNotifications",
                null,
                "CONTENT",
                true,
                10L);

        ReflectionTestUtils.invokeMethod(
                service,
                "createReviewReportResultNotifications",
                List.of(11L),
                "CONTENT",
                true,
                20L);

        ReflectionTestUtils.invokeMethod(
                service,
                "createReviewReportResultNotifications",
                List.of(12L),
                "PRODUCT",
                false,
                30L);

        verify(notificationService).createForMember(
                11L,
                "CONTENT_REVIEW_REPORT_PROCESSED",
                "리뷰 신고 검토 완료",
                "신고하신 콘텐츠 리뷰에서 운영 정책 위반이 확인되어 해당 리뷰를 삭제했습니다.",
                null,
                "CONTENT_REVIEW",
                20L);

        verify(notificationService).createForMember(
                12L,
                "PRODUCT_REVIEW_REPORT_PROCESSED",
                "리뷰 신고 검토 완료",
                "신고하신 상품 리뷰를 검토한 결과 운영 정책 위반 사항이 확인되지 않았습니다.",
                null,
                "PRODUCT_REVIEW",
                30L);
    }
}
