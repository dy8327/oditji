package com.project.oditji.search.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.project.oditji.search.vo.CachedContentVO;

/** 보완 수집 페이지 종료 조건의 남은 OR 피연산자를 직접 검증합니다. */
class SearchContentDiscoverServiceSupplementOperandClosureTest {

    private TmdbApiClient apiClient;
    private SearchContentDiscoverService service;

    @BeforeEach
    void setUp() {
        apiClient = mock(TmdbApiClient.class);
        service = new SearchContentDiscoverService(
                apiClient,
                mock(SearchContentPolicyService.class));

        when(apiClient.getBaseUrl()).thenReturn("https://api.test/3");
        when(apiClient.getLanguage()).thenReturn("ko-KR");
        when(apiClient.getRegion()).thenReturn("KR");
        when(apiClient.encode(anyString()))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void supplementYearShouldStopWhenTmdbReportsNoPages() {
        when(apiClient.get(anyString())).thenReturn(new JSONObject()
                .put("total_pages", 0)
                .put("results", new JSONArray()));

        List<CachedContentVO> result = new ArrayList<CachedContentVO>();

        ReflectionTestUtils.invokeMethod(
                service,
                "collectSupplementYear",
                result,
                "movie",
                "MOVIE",
                2,
                10,
                2026);

        assertEquals(0, result.size());
        verify(apiClient).get(anyString());
    }

    @Test
    void supplementYearShouldStopWhenResultsAreEmptyAfterPageChecksPass() {
        when(apiClient.get(anyString())).thenReturn(new JSONObject()
                .put("total_pages", 1)
                .put("results", new JSONArray()));

        List<CachedContentVO> result = new ArrayList<CachedContentVO>();

        ReflectionTestUtils.invokeMethod(
                service,
                "collectSupplementYear",
                result,
                "movie",
                "MOVIE",
                2,
                10,
                2026);

        assertEquals(0, result.size());
        verify(apiClient).get(anyString());
    }

    @Test
    void supplementYearShouldEvaluatePageGreaterThanTotalPagesOnSecondRequest() {
        JSONObject first = new JSONObject()
                .put("total_pages", 1)
                .put("results", new JSONArray().put(new JSONObject()
                        .put("id", 1L)
                        .put("adult", false)
                        .put("title", "테스트 영화")));
        JSONObject second = new JSONObject()
                .put("total_pages", 1)
                .put("results", new JSONArray());
        when(apiClient.get(anyString())).thenReturn(first, second);

        List<CachedContentVO> result = new ArrayList<CachedContentVO>();

        ReflectionTestUtils.invokeMethod(
                service,
                "collectSupplementYear",
                result,
                "movie",
                "MOVIE",
                2,
                10,
                2026);

        assertEquals(1, result.size());
        verify(apiClient, times(2)).get(anyString());
    }
}
