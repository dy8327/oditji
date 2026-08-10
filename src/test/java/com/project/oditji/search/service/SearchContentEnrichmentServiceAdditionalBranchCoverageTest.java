package com.project.oditji.search.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.project.oditji.search.vo.CachedContentVO;

/**
 * 상세 보강 서비스의 기본정보 유지/대체 및 provider 누락 조건을 추가 검증합니다.
 */
class SearchContentEnrichmentServiceAdditionalBranchCoverageTest {

    private TmdbApiClient apiClient;
    private SearchContentEnrichmentService service;

    @BeforeEach
    void setUp() {
        apiClient = mock(TmdbApiClient.class);

        service = new SearchContentEnrichmentService(
                apiClient,
                mock(SearchContentManualOverrideService.class),
                mock(SearchContentAgeRatingResolver.class));
    }

    @Test
    void basicDetailShouldIgnoreNullArguments() {
        CachedContentVO content = new CachedContentVO();

        ReflectionTestUtils.invokeMethod(
                service,
                "fillBasicContentDetail",
                null,
                new JSONObject());

        ReflectionTestUtils.invokeMethod(
                service,
                "fillBasicContentDetail",
                content,
                null);

        assertNull(content.getTitle());
    }

    @Test
    void movieBasicDetailShouldKeepExistingValuesAndFillOnlyMissingFields() {
        CachedContentVO content = new CachedContentVO();
        content.setContentType("MOVIE");
        content.setTitle("기존 제목");
        content.setOriginalTitle("기존 원제");
        content.setReleaseDate("2020-01-01");
        content.setRuntime(90);

        JSONObject detail = new JSONObject()
                .put("title", "새 제목")
                .put("original_title", "새 원제")
                .put("release_date", "2026-01-01")
                .put("runtime", 120);

        ReflectionTestUtils.invokeMethod(
                service,
                "fillBasicContentDetail",
                content,
                detail);

        assertEquals("기존 제목", content.getTitle());
        assertEquals("기존 원제", content.getOriginalTitle());
        assertEquals("2020-01-01", content.getReleaseDate());
        assertEquals(90, content.getRuntime());
    }

    @Test
    void tvBasicDetailShouldUseFallbackLastAirDateAndIgnoreExistingRuntime() {
        CachedContentVO content = new CachedContentVO();
        content.setContentType("TV");
        content.setRuntime(45);

        JSONObject detail = new JSONObject()
                .put("name", "TV 제목")
                .put("original_name", "Original")
                .put("first_air_date", "2025-01-01")
                .put("last_air_date", "2026-02-01")
                .put("episode_run_time", new JSONArray().put(60));

        ReflectionTestUtils.invokeMethod(
                service,
                "fillBasicContentDetail",
                content,
                detail);

        assertEquals("TV 제목", content.getTitle());
        assertEquals("2026-02-01", content.getLastAirDate());
        assertEquals(45, content.getRuntime());
    }

    @Test
    void providerLookupShouldReturnEmptyForMissingResultsOrMissingRegion() {
        when(apiClient.getBaseUrl()).thenReturn("https://api.test");
        when(apiClient.getRegion()).thenReturn("KR");

        when(apiClient.get(anyString()))
                .thenReturn(new JSONObject());

        @SuppressWarnings("unchecked")
        List<String> noResults =
                (List<String>) ReflectionTestUtils.invokeMethod(
                        service,
                        "loadPlatformKeys",
                        1L,
                        "MOVIE",
                        "movie",
                        mock(TmdbProviderRegistry.class));

        assertTrue(noResults.isEmpty());

        when(apiClient.get(anyString()))
                .thenReturn(new JSONObject()
                        .put("results", new JSONObject()));

        @SuppressWarnings("unchecked")
        List<String> noKorea =
                (List<String>) ReflectionTestUtils.invokeMethod(
                        service,
                        "loadPlatformKeys",
                        1L,
                        "MOVIE",
                        "movie",
                        mock(TmdbProviderRegistry.class));

        assertTrue(noKorea.isEmpty());
    }

    @Test
    void providerAdderShouldIgnoreNullArrayUnsupportedZeroAndBlankMapping() {
        TmdbProviderRegistry registry = mock(TmdbProviderRegistry.class);
        when(registry.getProviderMap("MOVIE"))
                .thenReturn(Map.of(8, "netflix", 9, " "));

        when(apiClient.getBaseUrl()).thenReturn("https://api.test");
        when(apiClient.getRegion()).thenReturn("KR");

        JSONObject root = new JSONObject()
                .put("results", new JSONObject()
                        .put("KR", new JSONObject()
                                .put("flatrate", new JSONArray()
                                        .put(JSONObject.NULL)
                                        .put(new JSONObject().put("provider_id", 0))
                                        .put(new JSONObject().put("provider_id", 9))
                                        .put(new JSONObject().put("provider_id", 8)))));

        when(apiClient.get(anyString())).thenReturn(root);

        @SuppressWarnings("unchecked")
        List<String> result =
                (List<String>) ReflectionTestUtils.invokeMethod(
                        service,
                        "loadPlatformKeys",
                        1L,
                        "MOVIE",
                        "movie",
                        registry);

        assertEquals(List.of("netflix"), result);
    }
}
