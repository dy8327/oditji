package com.project.oditji.search.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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

import com.project.oditji.common.util.DateTimeUtil;
import com.project.oditji.search.vo.CachedContentVO;
import com.project.oditji.search.vo.SearchResultVO;
import com.project.oditji.tmdb.dao.TmdbDAO;

/** 검색 페이지 캐시의 공개일·장르·등급 helper 잔여 단축평가를 보완합니다. */
class SearchContentPageCacheServiceResidualClosure4Test {

    private SearchContentPageCacheService service;

    @BeforeEach
    void setUp() {
        service = new SearchContentPageCacheService(
                mock(SearchContentStore.class),
                mock(TmdbDAO.class));
    }

    @Test
    void upcomingPolicyShouldCoverNullRowPastTodayWindowAndBeyondWindow() {
        LocalDate today = LocalDate.now(DateTimeUtil.KOREA_ZONE);
        SearchResultVO past = result(1L, today.minusDays(1));
        SearchResultVO todayResult = result(2L, today);
        SearchResultVO tomorrow = result(3L, today.plusDays(1));
        SearchResultVO thirdDay = result(4L, today.plusDays(3));
        SearchResultVO fourthDay = result(5L, today.plusDays(4));
        SearchResultVO invalid = result(6L, null);
        invalid.setReleaseDate("invalid");

        List<SearchResultVO> values = new ArrayList<>();
        values.add(null);
        values.add(past);
        values.add(todayResult);
        values.add(tomorrow);
        values.add(thirdDay);
        values.add(fourthDay);
        values.add(invalid);

        invokeVoid("applyUpcomingReleasePolicy", values);

        assertFalse(values.contains(fourthDay));
        assertTrue(values.contains(null));
        assertFalse(past.isUpcoming());
        assertFalse(todayResult.isUpcoming());
        assertTrue(tomorrow.isUpcoming());
        assertTrue(thirdDay.isUpcoming());
        assertFalse(invalid.isUpcoming());
    }

    @Test
    void appendReleasedAndAllContentShouldCoverGuardDateAndLimitBranches() {
        LocalDate start = LocalDate.of(2026, Month.AUGUST, 1);
        LocalDate end = LocalDate.of(2026, 8, 31);

        Map<String, SearchResultVO> full = new LinkedHashMap<>();
        full.put("MOVIE:99", result(99L, start));
        invokeVoid("appendReleasedContent", full, null, start, end, 1);
        invokeVoid("appendAllContent", full, null, 1);
        assertEquals(1, full.size());

        SearchResultVO noDate = result(10L, null);
        SearchResultVO before = result(11L, start.minusDays(1));
        SearchResultVO inside = result(12L, start.plusDays(1));
        SearchResultVO after = result(13L, end.plusDays(1));

        Map<String, SearchResultVO> selected = new LinkedHashMap<>();
        invokeVoid(
                "appendReleasedContent",
                selected,
                java.util.Arrays.asList(null, noDate, before, inside, after),
                start,
                end,
                10);

        assertEquals(1, selected.size());
        assertTrue(selected.containsKey("MOVIE:12"));

        Map<String, SearchResultVO> all = new LinkedHashMap<>();
        invokeVoid(
                "appendAllContent",
                all,
                java.util.Arrays.asList(null, inside, after),
                1);

        assertEquals(1, all.size());
        assertTrue(all.containsKey("MOVIE:12"));
    }

    @Test
    void genreAndAgeHelpersShouldCoverAliasAndEveryNormalizationOperand() {
        CachedContentVO content = new CachedContentVO();
        content.setGenreText("액션·모험 SF·판타지 연속극");

        assertTrue(invokeBoolean("matchesGenreCodes", content, List.of("ACTION")));
        assertTrue(invokeBoolean("matchesGenreCodes", content, List.of("SCI_FI")));
        assertTrue(invokeBoolean("matchesGenreCodes", content, List.of("FANTASY")));
        assertTrue(invokeBoolean("matchesGenreCodes", content, List.of("ROMANCE")));
        assertFalse(invokeBoolean("matchesGenreCodes", content, List.of("CRIME")));
        assertTrue(invokeBoolean("matchesGenreCodes", content, List.of()));

        List<String> normalized = invoke(
                "normalizeAgeRatingList",
                java.util.Arrays.asList(
                        null,
                        " ",
                        "NR",
                        "Not Rated",
                        "Unrated",
                        "청소년 관람불가",
                        "19세 이상 관람가",
                        "18세 이상 관람가",
                        "15세 이상 관람가",
                        "12세 이상 관람가",
                        "7세 이상 관람가",
                        "전체 관람가",
                        "15세 이상 관람가"));

        assertTrue(normalized.contains("등급 정보 없음"));
        assertTrue(normalized.contains("청소년 관람불가"));
        assertTrue(normalized.contains("15세 이상 관람가"));
        assertTrue(normalized.contains("12세 이상 관람가"));
        assertTrue(normalized.contains("7세 이상 관람가"));
        assertTrue(normalized.contains("전체 관람가"));
        assertEquals(6, normalized.size());
    }

    private SearchResultVO result(Long tmdbId, LocalDate releaseDate) {
        SearchResultVO result = new SearchResultVO();
        result.setTmdbId(tmdbId);
        result.setContentType("MOVIE");
        result.setReleaseDate(releaseDate == null ? null : releaseDate.toString());
        return result;
    }

    private void invokeVoid(String methodName, Object... arguments) {
        ReflectionTestUtils.invokeMethod(service, methodName, arguments);
    }

    private boolean invokeBoolean(String methodName, Object... arguments) {
        Boolean result = ReflectionTestUtils.invokeMethod(service, methodName, arguments);
        return Boolean.TRUE.equals(result);
    }

    @SuppressWarnings("unchecked")
    private <T> T invoke(String methodName, Object... arguments) {
        return (T) ReflectionTestUtils.invokeMethod(service, methodName, arguments);
    }
}
