package com.project.oditji.search.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
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

/** 상세 보강 서비스의 기존값 유지/JSON helper 잔여 조건을 보완합니다. */
class SearchContentEnrichmentServiceNextGapCoverageTest {

    private TmdbApiClient apiClient;
    private SearchContentManualOverrideService manualOverrideService;
    private SearchContentAgeRatingResolver ageRatingResolver;
    private SearchContentEnrichmentService service;

    @BeforeEach
    void setUp() {
        apiClient = mock(TmdbApiClient.class);
        manualOverrideService = mock(SearchContentManualOverrideService.class);
        ageRatingResolver = mock(SearchContentAgeRatingResolver.class);
        service = new SearchContentEnrichmentService(
                apiClient,
                manualOverrideService,
                ageRatingResolver);
    }

    @Test
    void reusableDetailShouldCoverMovieManualRuntimeFinalCondition() {
        CachedContentVO movie = reusable("MOVIE");
        when(ageRatingResolver.isNormalizedAgeRating(movie.getAgeRating())).thenReturn(true);
        when(manualOverrideService.contains(movie)).thenReturn(true);

        assertFalse(service.hasReusableDetail(movie));

        movie.setRuntime(120);
        assertTrue(service.hasReusableDetail(movie));

        when(manualOverrideService.contains(movie)).thenReturn(false);
        movie.setRuntime(null);
        assertTrue(service.hasReusableDetail(movie));
    }

    @Test
    void filledMovieBasicDetailShouldKeepExistingValues() {
        CachedContentVO movie = reusable("MOVIE");
        movie.setOriginalTitle("기존 원제");
        movie.setReleaseDate("2026-08-01");
        movie.setRuntime(110);
        movie.setPosterPath("/old.jpg");
        movie.setPopularity(10.0);

        JSONObject detail = new JSONObject()
                .put("title", "새 제목")
                .put("original_title", "새 원제")
                .put("release_date", "2026-08-12")
                .put("runtime", 130)
                .put("poster_path", "/new.jpg")
                .put("popularity", 99.0);

        ReflectionTestUtils.invokeMethod(service, "fillBasicContentDetail", movie, detail);

        assertEquals("기존 제목", movie.getTitle());
        assertEquals("기존 원제", movie.getOriginalTitle());
        assertEquals("2026-08-01", movie.getReleaseDate());
        assertEquals(110, movie.getRuntime());
        assertEquals("/old.jpg", movie.getPosterPath());
        assertEquals(10.0, movie.getPopularity());
    }

    @Test
    void filledTvBasicDetailShouldKeepExistingValuesAndRefreshLastAirDate() {
        CachedContentVO tv = reusable("TV");
        tv.setOriginalTitle("기존 TV 원제");
        tv.setReleaseDate("2026-07-01");
        tv.setRuntime(50);
        tv.setPosterPath("/old-tv.jpg");
        tv.setPopularity(20.0);

        JSONObject detail = new JSONObject()
                .put("name", "새 TV 제목")
                .put("original_name", "새 TV 원제")
                .put("first_air_date", "2026-08-12")
                .put("last_air_date", "2026-08-10")
                .put("episode_run_time", new JSONArray().put(60))
                .put("poster_path", "/new-tv.jpg")
                .put("popularity", 100.0);

        ReflectionTestUtils.invokeMethod(service, "fillBasicContentDetail", tv, detail);

        assertEquals("기존 제목", tv.getTitle());
        assertEquals("기존 TV 원제", tv.getOriginalTitle());
        assertEquals("2026-07-01", tv.getReleaseDate());
        assertEquals(50, tv.getRuntime());
        assertEquals("2026-08-10", tv.getLastAirDate());
        assertEquals("/old-tv.jpg", tv.getPosterPath());
        assertEquals(20.0, tv.getPopularity());
    }

    @Test
    void nullableStringShouldCoverPresentKeyWhoseOptStringReturnsNull() {
        JSONObject unusualJson = new JSONObject() {
            @Override
            public boolean has(String key) {
                return true;
            }

            @Override
            public boolean isNull(String key) {
                return false;
            }

            @Override
            public String optString(String key, String defaultValue) {
                return null;
            }
        };

        String value = ReflectionTestUtils.invokeMethod(
                service,
                "nullableString",
                unusualJson,
                "value");

        assertNull(value);
    }

    @Test
    void platformLoaderShouldCoverMissingResultsAndMissingKoreaSeparately() {
        when(apiClient.getBaseUrl()).thenReturn("https://api.test/3");
        when(apiClient.getRegion()).thenReturn("KR");
        TmdbProviderRegistry registry = new TmdbProviderRegistry(
                Map.of(8, "netflix"),
                Map.of(8, "netflix"));

        when(apiClient.get("https://api.test/3/movie/1/watch/providers"))
                .thenReturn(new JSONObject());
        List<String> missingResults = ReflectionTestUtils.invokeMethod(
                service,
                "loadPlatformKeys",
                1L,
                "MOVIE",
                "movie",
                registry);
        assertTrue(missingResults.isEmpty());

        when(apiClient.get("https://api.test/3/movie/2/watch/providers"))
                .thenReturn(new JSONObject().put("results", new JSONObject()));
        List<String> missingKorea = ReflectionTestUtils.invokeMethod(
                service,
                "loadPlatformKeys",
                2L,
                "MOVIE",
                "movie",
                registry);
        assertTrue(missingKorea.isEmpty());
    }

    @Test
    void basicDetailGuardShouldCoverNullCandidateAndNullDetailSeparately() {
        CachedContentVO candidate = reusable("MOVIE");

        ReflectionTestUtils.invokeMethod(
                service,
                "fillBasicContentDetail",
                null,
                new JSONObject());
        ReflectionTestUtils.invokeMethod(
                service,
                "fillBasicContentDetail",
                candidate,
                null);

        assertEquals("기존 제목", candidate.getTitle());
    }

    private CachedContentVO reusable(String type) {
        CachedContentVO content = new CachedContentVO();
        content.setTmdbId(1L);
        content.setContentType(type);
        content.setTitle("기존 제목");
        content.setGenreText("드라마");
        content.setPlatformKeys(List.of("netflix"));
        content.setSearchText("기존검색어");
        content.setAgeRating("15세 이상 관람가");
        if ("TV".equals(type)) {
            content.setEpisodeCount(10);
            content.setLastAirDate("2026-08-01");
        }
        return content;
    }
}
