package com.project.oditji.admin.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.test.util.ReflectionTestUtils;

import com.project.oditji.admin.dao.AdminDAO;
import com.project.oditji.admin.vo.MemberManageVO;
import com.project.oditji.notification.service.NotificationService;

/**
 * 관리자 서비스의 신고 알림 helper와 남은 null/empty/경계값 조건을 보완합니다.
 */
class AdminServiceImplAdditionalRemainingCoverageTest {

    @TempDir
    Path tempDirectory;

    private AdminDAO adminDAO;
    private NotificationService notificationService;
    private AdminServiceImpl service;

    @BeforeEach
    void setUp() {
        adminDAO = mock(AdminDAO.class);
        notificationService =
                mock(NotificationService.class);

        service = new AdminServiceImpl(
                adminDAO,
                notificationService,
                tempDirectory.toString());
    }

    @Test
    void reviewNotificationHelperShouldIgnoreNullAndEmptyReporterLists() {
        invokeReviewNotification(
                null,
                "CONTENT",
                true,
                1L);

        invokeReviewNotification(
                List.of(),
                "PRODUCT",
                false,
                2L);

        verify(notificationService, never())
                .createForMember(
                        any(),
                        any(),
                        any(),
                        any(),
                        any(),
                        any(),
                        any());
    }

    @Test
    void productRejectedReportNotificationShouldUseProductTypeAndRejectedMessage() {
        invokeReviewNotification(
                List.of(7L),
                "PRODUCT",
                false,
                10L);

        verify(notificationService)
                .createForMember(
                        7L,
                        "PRODUCT_REVIEW_REPORT_PROCESSED",
                        "리뷰 신고 검토 완료",
                        "신고하신 상품 리뷰를 검토한 결과 운영 정책 위반 사항이 확인되지 않았습니다.",
                        null,
                        "PRODUCT_REVIEW",
                        10L);
    }

    @Test
    void contentAcceptedReportNotificationShouldUseContentTypeAndAcceptedMessage() {
        invokeReviewNotification(
                List.of(8L),
                "CONTENT",
                true,
                11L);

        verify(notificationService)
                .createForMember(
                        8L,
                        "CONTENT_REVIEW_REPORT_PROCESSED",
                        "리뷰 신고 검토 완료",
                        "신고하신 콘텐츠 리뷰에서 운영 정책 위반이 확인되어 해당 리뷰를 삭제했습니다.",
                        null,
                        "CONTENT_REVIEW",
                        11L);
    }

    @Test
    void expiredWithdrawMemberShouldClampRemainingDaysToZero() {
        MemberManageVO member =
                new MemberManageVO();
        member.setStatus("WITHDRAWN");
        member.setWithdrawnAt(
                LocalDateTime.now()
                        .minusDays(20));

        ReflectionTestUtils.invokeMethod(
                service,
                "fillRemainingDeleteDays",
                member);

        assertEquals(
                0,
                member.getRemainingDeleteDays());
    }

    @Test
    void bulkReviewActionsShouldReturnZeroForEmptySelections() {
        assertEquals(
                0,
                service.bulkContentReviewAction(
                        List.of(),
                        "approve"));

        assertEquals(
                0,
                service.bulkProductReviewAction(
                        null,
                        "reject"));
    }

    @Test
    void businessGradeValidationShouldCoverZeroNumberAndNullGrade() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.updateBusinessGrade(
                        0L,
                        "GOLD"));

        assertThrows(
                IllegalArgumentException.class,
                () -> service.updateBusinessGrade(
                        1L,
                        null));
    }

    private void invokeReviewNotification(
            List<Long> reporterMemberNos,
            String reviewType,
            boolean accepted,
            Long reviewNo) {

        ReflectionTestUtils.invokeMethod(
                service,
                "createReviewReportResultNotifications",
                reporterMemberNos,
                reviewType,
                accepted,
                reviewNo);
    }
}
