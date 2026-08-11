package com.project.oditji.admin.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.file.Path;
import java.util.Collections;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.project.oditji.admin.dao.AdminDAO;
import com.project.oditji.notification.service.NotificationService;

/** 아직 직접 호출되지 않은 관리자 서비스 공개 래퍼 메서드를 보완합니다. */
@ExtendWith(MockitoExtension.class)
class AdminServiceImplPublicWrapperCoverageTest {

    @Mock
    private AdminDAO adminDAO;

    @Mock
    private NotificationService notificationService;

    @TempDir
    Path tempDirectory;

    private AdminServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new AdminServiceImpl(
                adminDAO,
                notificationService,
                tempDirectory.toString());
    }

    @Test
    void deleteProductReviewShouldDelegateDirectly() {
        assertDoesNotThrow(() -> service.deleteProductReview(10L));

        verify(adminDAO).adminDeleteProductReview(10L);
    }

    @Test
    void rejectProductReviewReportShouldCoverPublicWrapperSuccessPath() {
        when(adminDAO.selectWaitingProductReviewReporterMemberNos(20L))
                .thenReturn(Collections.emptyList());
        when(adminDAO.updateProductReviewReportStatus(20L, "REJECTED"))
                .thenReturn(1);

        assertDoesNotThrow(() -> service.rejectProductReviewReport(20L));

        verify(adminDAO).selectWaitingProductReviewReporterMemberNos(20L);
        verify(adminDAO).updateProductReviewReportStatus(20L, "REJECTED");
    }

    @Test
    void businessListCountShouldDelegateKeywordAndSearchType() {
        when(adminDAO.selectBusinessListCount(org.mockito.ArgumentMatchers.anyMap()))
                .thenReturn(7);

        assertEquals(
                7,
                service.getBusinessListCount("검색어", "businessName"));

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String, Object>> paramCaptor =
                ArgumentCaptor.forClass(Map.class);

        verify(adminDAO).selectBusinessListCount(paramCaptor.capture());
        assertEquals("검색어", paramCaptor.getValue().get("keyword"));
        assertEquals("businessName", paramCaptor.getValue().get("searchType"));
    }
}
