package com.project.oditji.search.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Year;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.project.oditji.search.vo.CachedContentVO;

/** Discover 수집 루프의 남은 short-circuit 종료 조건을 보완합니다. */
class SearchContentDiscoverServiceLoopBoundaryCoverageTest {

    private TmdbApiClient apiClient;
    private SearchContentDiscoverService service;

    @BeforeEach
    void setUp() {
        apiClient = mock(TmdbApiClient.class);
        service = new SearchContentDiscoverService(
                apiClient,
                mock(SearchContentPolicyService.class));
        ReflectionTestUtils.setField(service, "startYear", 1950);

        // Discover URL 생성에 필요한 TmdbApiClient 기본값을 명시합니다.
        when(apiClient.getBaseUrl()).thenReturn("https://api.test/3");
        when(apiClient.getLanguage()).thenReturn("ko-KR");
        when(apiClient.getRegion()).thenReturn("KR");
        when(apiClient.encode(anyString()))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void privateSupplementLoopsShouldCoverFirstAndSecondConditionFailures() {
        int currentYear = Year.now().getValue() + 1;

        List<CachedContentVO> noYears = ReflectionTestUtils.invokeMethod(
                service,
                "collectSupplementByType",
                "movie",
                "MOVIE",
                0,
                1,
                10);
        assertTrue(noYears.isEmpty());

        List<CachedContentVO> maxReached = ReflectionTestUtils.invokeMethod(
                service,
                "collectSupplementByType",
                "movie",
                "MOVIE",
                1,
                1,
                0);
        assertTrue(maxReached.isEmpty());

        List<CachedContentVO> result = new ArrayList<CachedContentVO>();
        ReflectionTestUtils.invokeMethod(
                service,
                "collectSupplementYear",
                result,
                "movie",
                "MOVIE",
                0,
                10,
                currentYear);
        assertTrue(result.isEmpty());

        result.add(content(99L));
        ReflectionTestUtils.invokeMethod(
                service,
                "collectSupplementYear",
                result,
                "movie",
                "MOVIE",
                1,
                1,
                currentYear);
        assertEquals(1, result.size());
    }

    @Test
    void discoverYearShouldCoverTotalPageAndTargetReachedLoopExits() {
        JSONObject onePage = response(1, contentJson(1L));
        when(apiClient.get(anyString())).thenReturn(onePage);

        List<CachedContentVO> targetReached = new ArrayList<CachedContentVO>();
        ReflectionTestUtils.invokeMethod(
                service,
                "collectDiscoverYear",
                targetReached,
                "movie",
                "MOVIE",
                1,
                "8",
                2026);
        assertEquals(1, targetReached.size());

        List<CachedContentVO> pageEnded = new ArrayList<CachedContentVO>();
        ReflectionTestUtils.invokeMethod(
                service,
                "collectDiscoverYear",
                pageEnded,
                "movie",
                "MOVIE",
                2,
                "8",
                2026);
        assertEquals(1, pageEnded.size());
    }

    @Test
    void supplementYearShouldCoverPageGreaterThanReportedTotalPages() {
        JSONObject firstPage = response(2, contentJson(1L));
        JSONObject secondPage = response(1, contentJson(2L));
        when(apiClient.get(anyString())).thenReturn(firstPage, secondPage);

        List<CachedContentVO> result = new ArrayList<CachedContentVO>();
        ReflectionTestUtils.invokeMethod(
                service,
                "collectSupplementYear",
                result,
                "movie",
                "MOVIE",
                3,
                10,
                2026);

        assertEquals(1, result.size());
    }

    private CachedContentVO content(Long tmdbId) {
        CachedContentVO content = new CachedContentVO();
        content.setTmdbId(tmdbId);
        content.setContentType("MOVIE");
        return content;
    }

    private JSONObject contentJson(long tmdbId) {
        return new JSONObject()
                .put("id", tmdbId)
                .put("adult", false)
                .put("title", "테스트");
    }

    private JSONObject response(int totalPages, JSONObject item) {
        return new JSONObject()
                .put("total_pages", totalPages)
                .put("results", new JSONArray().put(item));
    }
}
