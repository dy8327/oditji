package com.project.oditji.search.service;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
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

/**
 * Discover 수집기의 빈 결과 break 라인과 nullableString의 null 반환 피연산자를 검증합니다.
 */
class SearchContentDiscoverServiceUltimateResidualCoverageTest {

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
    void discoverYearShouldBreakImmediatelyWhenResultsArrayIsEmpty() {
        when(apiClient.get(anyString())).thenReturn(new JSONObject()
                .put("total_pages", 10)
                .put("results", new JSONArray()));

        List<CachedContentVO> result = new ArrayList<CachedContentVO>();

        ReflectionTestUtils.invokeMethod(
                service,
                "collectDiscoverYear",
                result,
                "movie",
                "MOVIE",
                10,
                "8",
                2026);

        assertTrue(result.isEmpty());
        verify(apiClient).get(anyString());
    }

    @Test
    void nullableStringShouldHandleOptStringReturningNullAfterGuardPasses() {
        JSONObject json = mock(JSONObject.class);
        when(json.has("name")).thenReturn(true);
        when(json.isNull("name")).thenReturn(false);
        when(json.optString("name", null)).thenReturn(null);

        String value = ReflectionTestUtils.invokeMethod(
                service,
                "nullableString",
                json,
                "name");

        assertNull(value);
    }
}
