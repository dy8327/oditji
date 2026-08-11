package com.project.oditji.search.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.json.JSONObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.project.oditji.search.vo.CachedContentVO;

/** 상세 보강의 InterruptedException 처리와 detail ID 단락 조건을 보완합니다. */
class SearchContentEnrichmentServiceInterruptedShortCircuitCoverageTest {

    private TmdbApiClient apiClient;
    private SearchContentManualOverrideService manualOverrideService;
    private SearchContentEnrichmentService service;
    private TmdbProviderRegistry providerRegistry;

    @BeforeEach
    void setUp() {
        apiClient = mock(TmdbApiClient.class);
        manualOverrideService = mock(SearchContentManualOverrideService.class);
        service = new SearchContentEnrichmentService(
                apiClient,
                manualOverrideService,
                new SearchContentAgeRatingResolver());
        providerRegistry = new TmdbProviderRegistry(
                Map.of(8, "netflix"),
                Map.of(8, "netflix"));

        ReflectionTestUtils.setField(service, "workerCount", 1);
        when(apiClient.getBaseUrl()).thenReturn("https://api.test");
        when(apiClient.getLanguage()).thenReturn("ko-KR");
        when(apiClient.encode("ko-KR")).thenReturn("ko-KR");
        when(apiClient.getRegion()).thenReturn("KR");
    }

    @AfterEach
    void clearInterruptFlag() {
        Thread.interrupted();
    }

    @Test
    void interruptedFutureGetShouldRestoreInterruptFlagAndThrowSafeException() {
        when(apiClient.get(anyString())).thenAnswer(invocation -> {
            Thread.sleep(150L);
            return new JSONObject().put("id", 1L);
        });

        CachedContentVO candidate = candidate(1L, "MOVIE");
        List<CachedContentVO> batch = List.of(candidate);

        Thread.currentThread().interrupt();

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> service.enrichBatch(batch, providerRegistry));

        assertEquals(
                "검색 콘텐츠 상세 보강이 중단되었습니다.",
                exception.getMessage());
        assertTrue(Thread.currentThread().isInterrupted());
    }

    @Test
    void nonEmptyDetailWithNonPositiveIdShouldBeDiscardedBeforePolicyChecks() {
        JSONObject invalidDetail = new JSONObject()
                .put("id", 0)
                .put("title", "invalid");
        when(apiClient.get(anyString())).thenReturn(invalidDetail);

        CachedContentVO candidate = candidate(2L, "MOVIE");
        List<CachedContentVO> batch = List.of(candidate);

        List<CachedContentVO> result = service.enrichBatch(batch, providerRegistry);

        assertTrue(result.isEmpty());
        verify(manualOverrideService, never()).apply(candidate);
    }

    @Test
    void movieAndTvMediaTypeBranchesShouldBuildDifferentDetailUrls() {
        SearchContentAgeRatingResolver resolver = mock(SearchContentAgeRatingResolver.class);
        SearchContentEnrichmentService localService = new SearchContentEnrichmentService(
                apiClient,
                manualOverrideService,
                resolver);
        ReflectionTestUtils.setField(localService, "workerCount", 1);

        JSONObject movieDetail = new JSONObject().put("id", 10L);
        JSONObject emptyProviders = new JSONObject();
        JSONObject tvDetail = new JSONObject().put("id", 20L);

        when(resolver.hasRestrictedSourceAgeRating("MOVIE", movieDetail)).thenReturn(false);
        when(resolver.parseMovieAgeRating(null)).thenReturn("등급 정보 없음");
        when(resolver.hasRestrictedSourceAgeRating("TV", tvDetail)).thenReturn(false);
        when(resolver.parseTvAgeRating(null)).thenReturn("등급 정보 없음");

        when(apiClient.get(anyString())).thenReturn(
                movieDetail,
                emptyProviders,
                tvDetail,
                emptyProviders);

        CachedContentVO movie = candidate(10L, "MOVIE");
        CachedContentVO tv = candidate(20L, "TV");

        List<CachedContentVO> movieResult = localService.enrichBatch(
                List.of(movie),
                providerRegistry);
        List<CachedContentVO> tvResult = localService.enrichBatch(
                List.of(tv),
                providerRegistry);

        assertEquals(1, movieResult.size());
        assertEquals(1, tvResult.size());
    }

    private CachedContentVO candidate(Long tmdbId, String contentType) {
        CachedContentVO candidate = new CachedContentVO();
        candidate.setTmdbId(tmdbId);
        candidate.setContentType(contentType);
        return candidate;
    }
}
