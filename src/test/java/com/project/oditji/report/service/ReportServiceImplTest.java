package com.project.oditji.report.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.project.oditji.notification.service.NotificationService;
import com.project.oditji.report.dao.ReportDAO;
import com.project.oditji.report.vo.ReportVO;

/** 리뷰 신고 입력 검증, 중복 확인, 저장과 접수 알림을 검증합니다. */
@ExtendWith(MockitoExtension.class)
class ReportServiceImplTest {

    @Mock
    private ReportDAO reportDAO;

    @Mock
    private NotificationService notificationService;

    private ReportServiceImpl reportService;

    @BeforeEach
    void setUp() {
        reportService = new ReportServiceImpl(
                reportDAO,
                notificationService);
    }

    @Test
    void submitReportShouldValidateMemberTypeTargetAndReason() {
        assertEquals(
                "로그인이 필요합니다.",
                assertThrows(
                        IllegalArgumentException.class,
                        () -> reportService.submitReport(
                                null,
                                "CONTENT",
                                1,
                                null,
                                "SPAM",
                                null)).getMessage());

        assertEquals(
                "잘못된 신고 대상입니다.",
                assertThrows(
                        IllegalArgumentException.class,
                        () -> reportService.submitReport(
                                1L,
                                "other",
                                1,
                                null,
                                "SPAM",
                                null)).getMessage());

        assertEquals(
                "신고할 리뷰 정보가 없습니다.",
                assertThrows(
                        IllegalArgumentException.class,
                        () -> reportService.submitReport(
                                1L,
                                "content",
                                0,
                                null,
                                "SPAM",
                                null)).getMessage());

        assertEquals(
                "신고할 리뷰 정보가 없습니다.",
                assertThrows(
                        IllegalArgumentException.class,
                        () -> reportService.submitReport(
                                1L,
                                "product",
                                null,
                                null,
                                "SPAM",
                                null)).getMessage());

        assertEquals(
                "신고 사유를 선택해주세요.",
                assertThrows(
                        IllegalArgumentException.class,
                        () -> reportService.submitReport(
                                1L,
                                "CONTENT",
                                1,
                                null,
                                " ",
                                null)).getMessage());

        verify(reportDAO, never()).insertReport(any());
    }

    @Test
    void submitReportShouldRejectDuplicateBeforeInsert() {
        when(reportDAO.countReport(anyMap())).thenReturn(1);

        assertEquals(
                "이미 신고한 리뷰입니다.",
                assertThrows(
                        IllegalStateException.class,
                        () -> reportService.submitReport(
                                1L,
                                " CONTENT ",
                                5,
                                null,
                                " SPAM ",
                                "detail")).getMessage());

        verify(reportDAO, never()).insertReport(any());
        verify(notificationService, never()).createForMember(
                org.mockito.ArgumentMatchers.anyLong(),
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.nullable(String.class),
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyLong());
    }

    @Test
    void contentReportShouldNormalizeInsertAndCreateNotification() {
        when(reportDAO.countReport(anyMap())).thenReturn(0);

        reportService.submitReport(
                1L,
                " content ",
                10,
                null,
                " SPAM ",
                " detail ");

        ArgumentCaptor<ReportVO> captor =
                ArgumentCaptor.forClass(ReportVO.class);
        verify(reportDAO).insertReport(captor.capture());
        ReportVO report = captor.getValue();
        assertEquals(1L, report.getMemberNo());
        assertEquals("CONTENT", report.getReviewType());
        assertEquals(10, report.getContentReviewNo());
        assertNull(report.getProductReviewNo());
        assertEquals("SPAM", report.getReason());
        assertEquals("detail", report.getDetail());

        verify(notificationService).createForMember(
                1L,
                "CONTENT_REVIEW_REPORT_RECEIVED",
                "리뷰 신고 접수",
                "콘텐츠 리뷰 신고가 접수되었습니다. 검토 후 결과를 안내해 드리겠습니다.",
                null,
                "CONTENT_REVIEW",
                10L);
    }

    @Test
    void productReportShouldStoreNullBlankDetailAndCreateNotification() {
        when(reportDAO.countReport(anyMap())).thenReturn(0);

        reportService.submitReport(
                2L,
                "PRODUCT",
                null,
                20,
                "ABUSE",
                " ");

        ArgumentCaptor<ReportVO> captor =
                ArgumentCaptor.forClass(ReportVO.class);
        verify(reportDAO).insertReport(captor.capture());
        ReportVO report = captor.getValue();
        assertEquals("PRODUCT", report.getReviewType());
        assertEquals(20, report.getProductReviewNo());
        assertNull(report.getDetail());

        verify(notificationService).createForMember(
                2L,
                "PRODUCT_REVIEW_REPORT_RECEIVED",
                "리뷰 신고 접수",
                "상품 리뷰 신고가 접수되었습니다. 검토 후 결과를 안내해 드리겠습니다.",
                null,
                "PRODUCT_REVIEW",
                20L);
    }

    @Test
    void reportedProductReviewSetShouldHandleNullAndRemoveDuplicates() {
        assertTrue(reportService.getReportedProductReviewSet(null).isEmpty());
        verify(reportDAO, never()).selectReportedProductReviewNoList(null);

        when(reportDAO.selectReportedProductReviewNoList(1L))
                .thenReturn(null)
                .thenReturn(List.of(1, 1, 2));

        assertTrue(reportService.getReportedProductReviewSet(1L).isEmpty());
        assertEquals(Set.of(1, 2),
                reportService.getReportedProductReviewSet(1L));
    }
}
