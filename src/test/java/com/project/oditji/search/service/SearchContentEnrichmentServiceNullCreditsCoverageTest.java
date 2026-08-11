package com.project.oditji.search.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.project.oditji.search.vo.CachedContentVO;

/** 상세 응답에 credits가 없고 기존 장르/평점이 이미 있는 경우의 잔여 분기를 보완합니다. */
class SearchContentEnrichmentServiceNullCreditsCoverageTest {

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
        providerRegistry = new TmdbProviderRegistry(Map.of(), Map.of());

        lenient().when(apiClient.getBaseUrl()).thenReturn("https://api.test");
        lenient().when(apiClient.getLanguage()).thenReturn("ko-KR");
        lenient().when(apiClient.getRegion()).thenReturn("KR");
        lenient().when(apiClient.encode(anyString()))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void movieWithoutCreditsShouldKeepExistingGenreAndScoreAndReturnNullPeopleFields() {
        JSONObject detail = new JSONObject()
                .put("id", 101L)
                .put("title", "영화")
                .put("original_title", "Movie")
                .put("release_date", "2026-08-01")
                .put("runtime", 120)
                .put("release_dates", new JSONObject().put("results", new JSONArray()));

        when(apiClient.get(anyString())).thenAnswer(invocation -> {
            String url = invocation.getArgument(0);
            if (url.endsWith("/watch/providers")) {
                return new JSONObject();
            }
            return detail;
        });

        CachedContentVO candidate = candidate(101L, "MOVIE");
        candidate.setGenreText("기존 장르");
        candidate.setTmdbScore(7.5);

        List<CachedContentVO> result = service.enrichBatch(
                List.of(candidate),
                providerRegistry);

        assertEquals(1, result.size());
        assertEquals("기존 장르", candidate.getGenreText());
        assertEquals(7.5, candidate.getTmdbScore());
        assertNull(candidate.getDirector());
        assertNull(candidate.getCastNames());
    }

    @Test
    void tvWithoutCreditsShouldUseNullCreatorAndCastBranches() {
        JSONObject detail = new JSONObject()
                .put("id", 202L)
                .put("name", "TV")
                .put("original_name", "TV Original")
                .put("first_air_date", "2026-08-02")
                .put("number_of_episodes", 8)
                .put("content_ratings", new JSONObject().put("results", new JSONArray()));

        when(apiClient.get(anyString())).thenAnswer(invocation -> {
            String url = invocation.getArgument(0);
            if (url.endsWith("/watch/providers")) {
                return new JSONObject();
            }
            return detail;
        });

        CachedContentVO candidate = candidate(202L, "TV");
        candidate.setGenreText("기존 TV 장르");
        candidate.setTmdbScore(8.2);

        List<CachedContentVO> result = service.enrichBatch(
                List.of(candidate),
                providerRegistry);

        assertEquals(1, result.size());
        assertEquals("기존 TV 장르", candidate.getGenreText());
        assertEquals(8.2, candidate.getTmdbScore());
        assertNull(candidate.getDirector());
        assertNull(candidate.getCastNames());
    }

    private CachedContentVO candidate(Long tmdbId, String contentType) {
        CachedContentVO candidate = new CachedContentVO();
        candidate.setTmdbId(tmdbId);
        candidate.setContentType(contentType);
        return candidate;
    }
}
