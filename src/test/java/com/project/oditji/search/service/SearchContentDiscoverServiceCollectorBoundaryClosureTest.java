package com.project.oditji.search.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

/** Discover 바깥 연도 루프의 시작 불가 및 목표 도달 종료 조건을 보완합니다. */
class SearchContentDiscoverServiceCollectorBoundaryClosureTest {

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
    void movieCollectorShouldNotEnterYearLoopWhenConfiguredStartYearIsInFuture() {
        ReflectionTestUtils.setField(service, "startYear", 3000);

        assertTrue(service.collectMovieCandidates(1, Set.of(8)).isEmpty());

        verify(apiClient, never()).get(anyString());
    }

    @Test
    void movieCollectorShouldStopOuterLoopImmediatelyAfterTargetIsReached() {
        ReflectionTestUtils.setField(service, "startYear", 1950);

        JSONObject item = new JSONObject()
                .put("id", 1L)
                .put("adult", false)
                .put("title", "테스트 영화");
        JSONObject response = new JSONObject()
                .put("total_pages", 500)
                .put("results", new JSONArray().put(item));
        when(apiClient.get(anyString())).thenReturn(response);

        assertEquals(1, service.collectMovieCandidates(1, Set.of(8)).size());

        verify(apiClient).get(anyString());
    }
}
