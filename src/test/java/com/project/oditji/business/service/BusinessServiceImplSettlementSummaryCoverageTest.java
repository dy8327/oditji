package com.project.oditji.business.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.project.oditji.business.dao.BusinessDAO;
import com.project.oditji.business.vo.SettlementManageVO;
import com.project.oditji.content.service.ContentService;
import com.project.oditji.notification.service.NotificationService;
import com.project.oditji.search.service.SearchContentStore;
import com.project.oditji.tmdb.service.TmdbService;

/** 정산 월 선택 요약의 미커버 본문과 current/next 분기를 보완합니다. */
@ExtendWith(MockitoExtension.class)
class BusinessServiceImplSettlementSummaryCoverageTest {

    @Mock
    private BusinessDAO businessDAO;

    @Mock
    private ContentService contentService;

    @Mock
    private SearchContentStore searchContentStore;

    @Mock
    private TmdbService tmdbService;

    @Mock
    private NotificationService notificationService;

    private BusinessServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new BusinessServiceImpl(
                businessDAO,
                contentService,
                searchContentStore,
                tmdbService,
                notificationService,
                "uploads/product",
                "uploads/event");
    }

    @Test
    void settlementSummaryShouldRejectInvalidBusinessNumber() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.getSettlementSummary(0L, "next"));
    }

    @Test
    void settlementSummaryShouldCoverCurrentNextAndNullDaoResult() {
        SettlementManageVO currentSummary = new SettlementManageVO();
        currentSummary.setSettledAmount(120_000L);
        currentSummary.setUnsettledAmount(30_000L);

        when(businessDAO.selectSettlementSummaryByPeriod(
                eq(11L),
                any(LocalDate.class),
                any(LocalDate.class),
                anyString()))
                .thenReturn(currentSummary);

        when(businessDAO.selectSettlementSummaryByPeriod(
                eq(12L),
                any(LocalDate.class),
                any(LocalDate.class),
                anyString()))
                .thenReturn(null);

        SettlementManageVO current = service.getSettlementSummary(11L, null);
        SettlementManageVO next = service.getSettlementSummary(12L, "NeXt");

        assertSame(currentSummary, current);
        assertEquals(150_000L, current.getSettlementTotalAmount());
        assertNotNull(next);
        assertEquals(0L, next.getSettlementTotalAmount());

        ArgumentCaptor<LocalDate> startDateCaptor = ArgumentCaptor.forClass(LocalDate.class);
        ArgumentCaptor<LocalDate> endDateCaptor = ArgumentCaptor.forClass(LocalDate.class);
        ArgumentCaptor<String> settlementMonthCaptor = ArgumentCaptor.forClass(String.class);

        verify(businessDAO, times(2)).selectSettlementSummaryByPeriod(
                anyLong(),
                startDateCaptor.capture(),
                endDateCaptor.capture(),
                settlementMonthCaptor.capture());

        List<LocalDate> startDates = startDateCaptor.getAllValues();
        List<LocalDate> endDates = endDateCaptor.getAllValues();
        List<String> settlementMonths = settlementMonthCaptor.getAllValues();

        LocalDate currentStart = startDates.get(0);
        LocalDate currentEnd = endDates.get(0);
        LocalDate nextStart = startDates.get(1);
        LocalDate nextEnd = endDates.get(1);

        assertEquals(1, currentStart.getDayOfMonth());
        assertEquals(currentStart.plusMonths(1), currentEnd);
        assertEquals(currentStart.plusMonths(1), nextStart);
        assertEquals(nextStart.plusMonths(1), nextEnd);

        assertEquals(
                String.format("%04d-%02d", currentEnd.getYear(), currentEnd.getMonthValue()),
                settlementMonths.get(0));
        assertEquals(
                String.format("%04d-%02d", nextEnd.getYear(), nextEnd.getMonthValue()),
                settlementMonths.get(1));

        assertEquals(
                currentStart + " ~ " + currentEnd.minusDays(1),
                current.getSettlementPeriod());
        assertEquals(
                "매월 10일 (" + currentEnd.withDayOfMonth(10) + ")",
                current.getSettlementDateLabel());
        assertEquals(
                nextStart + " ~ " + nextEnd.minusDays(1),
                next.getSettlementPeriod());
        assertEquals(
                "매월 10일 (" + nextEnd.withDayOfMonth(10) + ")",
                next.getSettlementDateLabel());
    }
}
