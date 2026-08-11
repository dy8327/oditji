package com.project.oditji.search.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.Month;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.project.oditji.content.vo.ContentVO;
import com.project.oditji.search.vo.CachedContentVO;
import com.project.oditji.search.vo.SearchResultVO;
import com.project.oditji.tmdb.dao.TmdbDAO;

/** 출시 캘린더와 메인 최근 본 콘텐츠 매칭의 신규 분기를 검증합니다. */
@ExtendWith(MockitoExtension.class)
class SearchContentPageCacheServiceReleaseRecentCoverageTest {

    @Mock
    private SearchContentStore searchContentStore;

    @Mock
    private TmdbDAO tmdbDAO;

    private SearchContentPageCacheService service;

    @BeforeEach
    void setUp() {
        service = new SearchContentPageCacheService(searchContentStore, tmdbDAO);
        lenient().when(tmdbDAO.selectActivePlatformList()).thenReturn(List.of());
    }

    @Test
    void releaseCalendarShouldFilterYearMonthIgnoreInvalidDateAndSortByReleaseDate() {
        when(searchContentStore.getAll()).thenReturn(List.of(
                cached(1L, "MOVIE", "8월 20일", "2026-08-20", 100.0),
                cached(2L, "TV", "8월 5일", "2026-08-05", 10.0),
                cached(3L, "MOVIE", "7월", "2026-07-31", 500.0),
                cached(4L, "TV", "다른 연도", "2025-08-10", 400.0),
                cached(5L, "MOVIE", "잘못된 날짜", "not-a-date", 300.0)));

        List<SearchResultVO> result = service.getReleaseCalendarContent(
                2026,
                Month.AUGUST.getValue());

        assertEquals(2, result.size());
        assertEquals("8월 5일", result.get(0).getTitle());
        assertEquals("8월 20일", result.get(1).getTitle());
    }

    @Test
    void releaseCalendarShouldReturnEmptyWhenNoDateMatches() {
        when(searchContentStore.getAll()).thenReturn(List.of(
                cached(1L, "MOVIE", "1월 작품", "2026-01-02", 1.0)));

        assertTrue(service.getReleaseCalendarContent(
                2026,
                Month.FEBRUARY.getValue()).isEmpty());
    }

    @Test
    void recentlyViewedShouldReturnEmptyBeforeReadingCacheForNullOrEmptyInput() {
        assertTrue(service.getMainRecentlyViewedContent(null).isEmpty());
        assertTrue(service.getMainRecentlyViewedContent(List.of()).isEmpty());

        verify(searchContentStore, never()).getAll();
        verify(tmdbDAO, never()).selectActivePlatformList();
    }

    @Test
    void recentlyViewedShouldKeepHistoryOrderNormalizeTypeAndSkipMissingCacheContent() {
        when(searchContentStore.getAll()).thenReturn(List.of(
                cached(100L, "MOVIE", "첫 후보", "2026-08-01", 200.0),
                cached(200L, "TV", "두 번째 후보", "2026-08-02", 100.0)));

        ContentVO viewedTv = viewed(200L, " tv ");
        ContentVO missing = viewed(999L, "MOVIE");
        ContentVO viewedMovie = viewed(100L, "movie");

        List<SearchResultVO> result = service.getMainRecentlyViewedContent(
                List.of(viewedTv, missing, viewedMovie));

        assertEquals(2, result.size());
        assertEquals(Long.valueOf(200L), result.get(0).getTmdbId());
        assertEquals(Long.valueOf(100L), result.get(1).getTmdbId());
    }

    @Test
    void recentlyViewedShouldSafelyBuildKeyForNullContentTypeWithoutMatching() {
        when(searchContentStore.getAll()).thenReturn(List.of(
                cached(100L, "MOVIE", "후보", "2026-08-01", 1.0)));

        ContentVO viewed = viewed(100L, null);

        assertTrue(service.getMainRecentlyViewedContent(List.of(viewed)).isEmpty());
    }

    private CachedContentVO cached(
            Long tmdbId,
            String contentType,
            String title,
            String releaseDate,
            Double popularity) {

        CachedContentVO content = new CachedContentVO();
        content.setTmdbId(tmdbId);
        content.setContentType(contentType);
        content.setTitle(title);
        content.setOriginalTitle(title + " 원제");
        content.setReleaseDate(releaseDate);
        content.setLastAirDate(releaseDate);
        content.setPlatformKeys(List.of("netflix"));
        content.setTmdbScore(7.0);
        content.setPopularity(popularity);
        return content;
    }

    private ContentVO viewed(Long tmdbId, String contentType) {
        ContentVO content = new ContentVO();
        content.setTmdbId(tmdbId);
        content.setContentType(contentType);
        content.setReleaseDate(LocalDate.of(2026, Month.AUGUST, 1));
        return content;
    }
}
