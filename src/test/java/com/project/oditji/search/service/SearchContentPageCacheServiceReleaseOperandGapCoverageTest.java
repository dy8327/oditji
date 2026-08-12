package com.project.oditji.search.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.project.oditji.search.vo.SearchResultVO;
import com.project.oditji.tmdb.dao.TmdbDAO;

/** 출시일 목록 helper의 null/범위 경계 및 limit 단락 조건을 보완합니다. */
class SearchContentPageCacheServiceReleaseOperandGapCoverageTest {

    private SearchContentPageCacheService service;

    @BeforeEach
    void setUp() {
        service = new SearchContentPageCacheService(
                mock(SearchContentStore.class),
                mock(TmdbDAO.class));
    }

    @Test
    void upcomingPolicyShouldReturnForNullAndEmptyLists() {
        ReflectionTestUtils.invokeMethod(
                service,
                "applyUpcomingReleasePolicy",
                (Object) null);

        List<SearchResultVO> empty = new ArrayList<SearchResultVO>();
        ReflectionTestUtils.invokeMethod(
                service,
                "applyUpcomingReleasePolicy",
                empty);

        assertTrue(empty.isEmpty());
    }

    @Test
    void appendReleasedContentShouldCoverNullInvalidBeforeAfterAndBoundaryDates() {
        LocalDate start = LocalDate.of(2026, Month.JANUARY, 10);
        LocalDate end = LocalDate.of(2026, Month.JANUARY, 20);

        List<SearchResultVO> source = new ArrayList<SearchResultVO>();
        source.add(null);
        source.add(result(1L, null));
        source.add(result(2L, "   "));
        source.add(result(3L, "invalid"));
        source.add(result(4L, "2026-01-09"));
        source.add(result(5L, "2026-01-21"));
        source.add(result(6L, "2026-01-10"));
        source.add(result(7L, "2026-01-20"));
        source.add(result(8L, "2026-01-15"));

        Map<String, SearchResultVO> selected = new LinkedHashMap<String, SearchResultVO>();

        ReflectionTestUtils.invokeMethod(
                service,
                "appendReleasedContent",
                selected,
                source,
                start,
                end,
                10);

        assertEquals(3, selected.size());
        assertTrue(selected.containsKey("MOVIE:6"));
        assertTrue(selected.containsKey("MOVIE:7"));
        assertTrue(selected.containsKey("MOVIE:8"));
    }

    @Test
    void limitListShouldCoverNullEmptyZeroWholeAndTruncatedResults() {
        List<SearchResultVO> one = List.of(result(10L, "2026-01-01"));
        List<SearchResultVO> two = List.of(
                result(11L, "2026-01-01"),
                result(12L, "2026-01-02"));

        List<?> nullSource = ReflectionTestUtils.invokeMethod(
                service,
                "limitList",
                null,
                1);
        List<?> emptySource = ReflectionTestUtils.invokeMethod(
                service,
                "limitList",
                List.of(),
                1);
        List<?> zeroLimit = ReflectionTestUtils.invokeMethod(
                service,
                "limitList",
                one,
                0);
        List<?> whole = ReflectionTestUtils.invokeMethod(
                service,
                "limitList",
                one,
                2);
        List<?> truncated = ReflectionTestUtils.invokeMethod(
                service,
                "limitList",
                two,
                1);

        assertTrue(nullSource.isEmpty());
        assertTrue(emptySource.isEmpty());
        assertTrue(zeroLimit.isEmpty());
        assertEquals(1, whole.size());
        assertEquals(1, truncated.size());
    }

    private SearchResultVO result(Long tmdbId, String releaseDate) {
        SearchResultVO result = new SearchResultVO();
        result.setTmdbId(tmdbId);
        result.setContentType("MOVIE");
        result.setReleaseDate(releaseDate);
        return result;
    }
}
