package com.project.oditji.admin.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.test.util.ReflectionTestUtils;

import com.project.oditji.admin.dao.AdminDAO;
import com.project.oditji.notification.service.NotificationService;

/** 관리자 서비스의 신규 회원 정리 단계와 신고 결과 알림 잔여 분기를 보완합니다. */
class AdminServiceImplResidualClosure3Test {

    @TempDir
    Path tempDir;

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
                tempDir.toString());
    }

    @Test
    void memberDeleteShouldExecuteRecentlyAddedMemberOwnedCleanupSteps() {
        service.deleteMember(42L);

        verify(adminDAO).deleteSubscriptionResultByMemberNo(42L);
        verify(adminDAO).deleteNotificationSettingByMemberNo(42L);
        verify(adminDAO).deleteSearchKeywordHistoryByMemberNo(42L);
        verify(adminDAO).deleteMember(42L);
    }

    @Test
    void reviewResultNotificationShouldCoverEmptyAndProductRejectedBranches() {
        ReflectionTestUtils.invokeMethod(
                service,
                "createReviewReportResultNotifications",
                List.of(),
                "PRODUCT",
                false,
                7L);

        ReflectionTestUtils.invokeMethod(
                service,
                "createReviewReportResultNotifications",
                List.of(10L),
                "PRODUCT",
                false,
                7L);

        verify(notificationService).createForMember(
                10L,
                "PRODUCT_REVIEW_REPORT_PROCESSED",
                "리뷰 신고 검토 완료",
                "신고하신 상품 리뷰를 검토한 결과 운영 정책 위반 사항이 확인되지 않았습니다.",
                null,
                "PRODUCT_REVIEW",
                7L);
    }

    @Test
    void paginationHelpersShouldKeepProvidedValuesAndNormalizeInvalidPage() {
        java.util.Map<String, Object> parameters = ReflectionTestUtils.invokeMethod(
                service,
                "keywordSearchTypeParam",
                "keyword",
                "name");

        assertEquals("keyword", parameters.get("keyword"));
        assertEquals("name", parameters.get("searchType"));

        java.util.Map<String, Object> paged = ReflectionTestUtils.invokeMethod(
                service,
                "withPaging",
                parameters,
                0,
                10);

        assertTrue(paged.containsKey("offset"));
        assertEquals(10, paged.get("pageSize"));
    }
}
