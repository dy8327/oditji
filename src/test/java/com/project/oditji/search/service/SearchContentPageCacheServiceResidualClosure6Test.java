package com.project.oditji.search.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.project.oditji.content.vo.ContentVO;
import com.project.oditji.search.vo.CachedContentVO;
import com.project.oditji.search.vo.SearchResultVO;
import com.project.oditji.tmdb.dao.TmdbDAO;

/** 검색 페이지 캐시의 날짜/최근조회/관련콘텐츠 helper 잔여 조건을 보완합니다. */
class SearchContentPageCacheServiceResidualClosure6Test {

    private SearchContentStore searchContentStore;
    private TmdbDAO tmdbDAO;
    private SearchContentPageCacheService service;

    @BeforeEach
    void setUp() {
        searchContentStore = mock(SearchContentStore.class);
        tmdbDAO = mock(TmdbDAO.class);
        service = new SearchContentPageCacheService(searchContentStore, tmdbDAO);
        when(tmdbDAO.selectActivePlatformList()).thenReturn(List.of());
    }

    @Test
    void appendReleasedContentShouldEvaluateSourceNullAfterSizeGuardIsFalse() {
        Map<String, SearchResultVO> selected = new LinkedHashMap<>();
        LocalDate start = LocalDate.of(2026, Month.AUGUST, 1);
        LocalDate end = LocalDate.of(2026, Month.AUGUST, 31);

        invokeVoid("appendReleasedContent", selected, null, start, end, 3);

        assertTrue(selected.isEmpty());
    }

    @Test
    void appendAllContentShouldEvaluateSourceNullAfterSizeGuardIsFalse() {
        Map<String, SearchResultVO> selected = new LinkedHashMap<>();

        invokeVoid("appendAllContent", selected, null, 3);

        assertTrue(selected.isEmpty());
    }

    @Test
    void releaseCalendarShouldEvaluateYearAndMonthOperandsIndividually() {
        CachedContentVO wrongYear = cached(1L, "MOVIE", "2025-08-13");
        CachedContentVO wrongMonth = cached(2L, "MOVIE", "2026-07-13");
        CachedContentVO correct = cached(3L, "MOVIE", "2026-08-13");
        CachedContentVO invalid = cached(4L, "MOVIE", "invalid-date");

        when(searchContentStore.getAll())
                .thenReturn(List.of(wrongYear, wrongMonth, correct, invalid));

        List<SearchResultVO> result = service.getReleaseCalendarContent(2026, 8);

        assertEquals(1, result.size());
        assertEquals(3L, result.get(0).getTmdbId());
    }

    @Test
    void recentlyViewedShouldCoverMatchedAndMissingCandidates() {
        CachedContentVO existing = cached(10L, "MOVIE", "2026-08-01");
        when(searchContentStore.getAll()).thenReturn(List.of(existing));

        ContentVO matched = new ContentVO();
        matched.setTmdbId(10L);
        matched.setContentType("MOVIE");

        ContentVO missing = new ContentVO();
        missing.setTmdbId(11L);
        missing.setContentType("MOVIE");

        List<SearchResultVO> result = service.getMainRecentlyViewedContent(
                List.of(matched, missing));

        assertEquals(1, result.size());
        assertEquals(10L, result.get(0).getTmdbId());
    }

    @Test
    void recentlyViewedGuardShouldCoverNullAndEmptyLists() {
        assertTrue(service.getMainRecentlyViewedContent(null).isEmpty());
        assertTrue(service.getMainRecentlyViewedContent(new ArrayList<ContentVO>()).isEmpty());
    }

    @Test
    void relatedContentGuardShouldEvaluateLimitAfterNonNullContent() {
        ContentVO current = new ContentVO();
        current.setTmdbId(10L);
        current.setContentType("MOVIE");

        assertTrue(service.getRelatedContentList(current, 0).isEmpty());
    }

    @Test
    void distinctContentShouldEvaluateEveryValidationOperand() {
        Map<String, SearchResultVO> selected = new LinkedHashMap<>();

        invokeVoid("putDistinctContent", selected, null);
        invokeVoid("putDistinctContent", selected, result(null, "MOVIE"));
        invokeVoid("putDistinctContent", selected, result(20L, null));
        invokeVoid("putDistinctContent", selected, result(21L, "MOVIE"));

        assertEquals(1, selected.size());
    }

    @Test
    void releaseDateParserShouldEvaluateBlankOperandAfterNonNullValue() {
        assertNull(invoke("parseReleaseDate", "   "));
        assertNull(invoke("parseReleaseDate", "invalid-date"));
        assertEquals(
                LocalDate.of(2026, Month.AUGUST, 13),
                invoke("parseReleaseDate", " 2026-08-13 "));
    }

    @Test
    void matchedValueHelpersShouldCoverCandidateAndTargetGuardOperands() {
        assertEquals("", invoke("findFirstOriginalMatchedValue", "액션", null));
        assertEquals("", invoke("findFirstOriginalMatchedValue", "액션", "   "));
        assertEquals("", invoke("findOriginalValue", "액션, 드라마", null));
        assertEquals("", invoke("findOriginalValue", "액션, 드라마", "   "));
        assertEquals("드라마", invoke("findFirstOriginalMatchedValue", "드라마", "액션, 드라마"));
    }

    @Test
    void appendReleasedContentShouldEvaluateDateRangeOperands() {
        LocalDate start = LocalDate.of(2026, Month.AUGUST, 1);
        LocalDate end = LocalDate.of(2026, Month.AUGUST, 31);
        SearchResultVO before = result(30L, "MOVIE");
        before.setReleaseDate("2026-07-31");
        SearchResultVO after = result(31L, "MOVIE");
        after.setReleaseDate("2026-09-01");
        SearchResultVO inside = result(32L, "MOVIE");
        inside.setReleaseDate("2026-08-13");

        Map<String, SearchResultVO> selected = new LinkedHashMap<>();
        invokeVoid(
                "appendReleasedContent",
                selected,
                Arrays.asList(null, before, after, inside),
                start,
                end,
                10);

        assertEquals(1, selected.size());
        assertTrue(selected.containsKey("MOVIE:32"));
    }

    private CachedContentVO cached(Long tmdbId, String type, String releaseDate) {
        CachedContentVO content = new CachedContentVO();
        content.setTmdbId(tmdbId);
        content.setContentType(type);
        content.setTitle("테스트 " + tmdbId);
        content.setReleaseDate(releaseDate);
        content.setPlatformKeys(List.of("netflix"));
        return content;
    }

    private SearchResultVO result(Long tmdbId, String type) {
        SearchResultVO result = new SearchResultVO();
        result.setTmdbId(tmdbId);
        result.setContentType(type);
        return result;
    }

    private void invokeVoid(String methodName, Object... arguments) {
        ReflectionTestUtils.invokeMethod(service, methodName, arguments);
    }

    @SuppressWarnings("unchecked")
    private <T> T invoke(String methodName, Object... arguments) {
        return (T) ReflectionTestUtils.invokeMethod(service, methodName, arguments);
    }
}
