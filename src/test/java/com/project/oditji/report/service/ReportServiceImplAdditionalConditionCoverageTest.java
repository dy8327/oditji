package com.project.oditji.report.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.project.oditji.notification.service.NotificationService;
import com.project.oditji.report.dao.ReportDAO;
import com.project.oditji.report.vo.ReportVO;

/**
 * 기존 신고 테스트에서 남은 OR 조건의 반대 피연산자와 detail null 분기를 보완합니다.
 */
class ReportServiceImplAdditionalConditionCoverageTest {

    private ReportDAO reportDAO;
    private ReportServiceImpl service;

    @BeforeEach
    void setUp() {
        reportDAO = mock(ReportDAO.class);
        NotificationService notificationService =
                mock(NotificationService.class);
        service = new ReportServiceImpl(
                reportDAO,
                notificationService);
    }

    @Test
    void nullReviewTypeShouldBeRejected() {
        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> service.submitReport(
                                1L,
                                null,
                                1,
                                null,
                                "SPAM",
                                null));

        assertEquals(
                "잘못된 신고 대상입니다.",
                exception.getMessage());
    }

    @Test
    void nullContentReviewNumberShouldCoverFirstContentTargetCondition() {
        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> service.submitReport(
                                1L,
                                "CONTENT",
                                null,
                                null,
                                "SPAM",
                                null));

        assertEquals(
                "신고할 리뷰 정보가 없습니다.",
                exception.getMessage());
    }

    @Test
    void zeroProductReviewNumberShouldCoverSecondProductTargetCondition() {
        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> service.submitReport(
                                1L,
                                "PRODUCT",
                                null,
                                0,
                                "SPAM",
                                null));

        assertEquals(
                "신고할 리뷰 정보가 없습니다.",
                exception.getMessage());
    }

    @Test
    void nullReasonShouldCoverFirstReasonCondition() {
        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> service.submitReport(
                                1L,
                                "CONTENT",
                                10,
                                null,
                                null,
                                null));

        assertEquals(
                "신고 사유를 선택해주세요.",
                exception.getMessage());
    }

    @Test
    void nullDetailShouldBeStoredAsNullForValidReport() {
        when(reportDAO.countReport(anyMap()))
                .thenReturn(0);

        service.submitReport(
                2L,
                "PRODUCT",
                null,
                20,
                "ABUSE",
                null);

        ArgumentCaptor<ReportVO> captor =
                ArgumentCaptor.forClass(ReportVO.class);

        verify(reportDAO).insertReport(captor.capture());

        assertNull(captor.getValue().getDetail());
    }
}
