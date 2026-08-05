package com.project.oditji.search.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.project.oditji.search.vo.CachedContentVO;

/** 영화·TV 상세 보강, 제공처 매핑, 제외 등급과 개별 후보 실패 분기를 검증합니다. */
@ExtendWith(MockitoExtension.class)
class SearchContentEnrichmentServiceExtendedCoverageTest {

    @Mock
    private TmdbApiClient apiClient;

    @Mock
    private SearchContentManualOverrideService manualOverrideService;

    private SearchContentEnrichmentService service;

    private TmdbProviderRegistry providerRegistry;

    @BeforeEach
    void setUp() {
        service = new SearchContentEnrichmentService(
                apiClient,
                manualOverrideService,
                new SearchContentAgeRatingResolver());
        providerRegistry = new TmdbProviderRegistry(
                Map.of(8, "netflix", 337, "disney"),
                Map.of(8, "netflix", 356, "wavve"));

        lenient().when(apiClient.getBaseUrl()).thenReturn("https://api.test");
        lenient().when(apiClient.getLanguage()).thenReturn("ko-KR");
        lenient().when(apiClient.encode("ko-KR")).thenReturn("ko-KR");
        lenient().when(apiClient.getRegion()).thenReturn("KR");
    }

    @Test
    void movieShouldFillMissingDetailAndSupportedProviders() {
        JSONObject detail = new JSONObject("""
                {
                  "id":101,
                  "title":"영화 제목",
                  "original_title":"Original Movie",
                  "release_date":"2026-01-02",
                  "poster_path":"/movie.jpg",
                  "runtime":120,
                  "popularity":55.5,
                  "vote_average":8.1,
                  "genres":[
                    {"name":"드라마"},
                    {"name":"드라마"},
                    {"name":"액션"},
                    "invalid"
                  ],
                  "release_dates":{
                    "results":[
                      {
                        "iso_3166_1":"KR",
                        "release_dates":[{"certification":"15"}]
                      }
                    ]
                  },
                  "credits":{
                    "crew":[
                      {"job":"Director","name":"감독 A"},
                      {"job":"Producer","name":"제작자"},
                      {"job":"director","name":"감독 B"},
                      {"job":"Director","name":"감독 A"}
                    ],
                    "cast":[
                      {"name":"배우1"},
                      {"name":"배우2"},
                      {"name":"배우3"},
                      {"name":"배우4"},
                      {"name":"배우5"},
                      {"name":"배우6"},
                      {"name":"배우1"},
                      "invalid"
                    ]
                  }
                }
                """);
        JSONObject providers = new JSONObject("""
                {
                  "results":{
                    "KR":{
                      "flatrate":[
                        {"provider_id":8},
                        {"provider_id":337},
                        {"provider_id":999},
                        {"provider_id":8},
                        "invalid"
                      ]
                    }
                  }
                }
                """);
        stubResponses(detail, providers);

        CachedContentVO candidate = candidate(101L, "MOVIE");
        List<CachedContentVO> result =
                service.enrichBatch(List.of(candidate), providerRegistry);

        assertEquals(1, result.size());
        assertEquals("영화 제목", candidate.getTitle());
        assertEquals("Original Movie", candidate.getOriginalTitle());
        assertEquals("2026-01-02", candidate.getReleaseDate());
        assertEquals("/movie.jpg", candidate.getPosterPath());
        assertEquals(120, candidate.getRuntime());
        assertNull(candidate.getEpisodeCount());
        assertEquals(55.5, candidate.getPopularity());
        assertEquals(8.1, candidate.getTmdbScore());
        assertEquals("드라마, 액션", candidate.getGenreText());
        assertEquals("15세 이상 관람가", candidate.getAgeRating());
        assertEquals("감독 A, 감독 B", candidate.getDirector());
        assertEquals("배우1, 배우2, 배우3, 배우4, 배우5", candidate.getCastNames());
        assertEquals(List.of("netflix", "disney"), candidate.getPlatformKeys());
        assertEquals(Boolean.TRUE, candidate.getAgeRatingRestrictionChecked());
        assertTrue(candidate.getSearchText().contains("영화제목"));
        verify(manualOverrideService).apply(candidate);
    }

    @Test
    void tvShouldUseEpisodeDateRuntimeCreatorAndTvProviderMap() {
        JSONObject detail = new JSONObject("""
                {
                  "id":202,
                  "name":"드라마 제목",
                  "original_name":"Original Series",
                  "first_air_date":"2025-01-01",
                  "last_air_date":"2026-01-01",
                  "last_episode_to_air":{"air_date":"2026-02-02"},
                  "poster_path":"/tv.jpg",
                  "episode_run_time":[0,-1,55],
                  "number_of_episodes":16,
                  "popularity":44.0,
                  "vote_average":7.7,
                  "genres":[{"name":"코미디"}],
                  "content_ratings":{
                    "results":[{"iso_3166_1":"KR","rating":"12"}]
                  },
                  "created_by":[
                    {"name":"작가 A"},
                    {"name":"작가 A"},
                    {"name":"작가 B"},
                    "invalid"
                  ],
                  "credits":{
                    "cast":[{"name":"배우 A"},{"name":"배우 B"}]
                  }
                }
                """);
        JSONObject providers = new JSONObject("""
                {
                  "results":{
                    "KR":{
                      "flatrate":[
                        {"provider_id":356},
                        {"provider_id":8}
                      ]
                    }
                  }
                }
                """);
        stubResponses(detail, providers);

        CachedContentVO candidate = candidate(202L, "TV");
        List<CachedContentVO> result =
                service.enrichBatch(List.of(candidate), providerRegistry);

        assertEquals(1, result.size());
        assertEquals("드라마 제목", candidate.getTitle());
        assertEquals("Original Series", candidate.getOriginalTitle());
        assertEquals("2025-01-01", candidate.getReleaseDate());
        assertEquals("2026-02-02", candidate.getLastAirDate());
        assertEquals(55, candidate.getRuntime());
        assertEquals(16, candidate.getEpisodeCount());
        assertEquals("12세 이상 관람가", candidate.getAgeRating());
        assertEquals("작가 A, 작가 B", candidate.getDirector());
        assertEquals("배우 A, 배우 B", candidate.getCastNames());
        assertEquals(List.of("wavve", "netflix"), candidate.getPlatformKeys());
    }

    @Test
    void tvShouldFallbackToLastAirDateAndAllowMissingProviderResults() {
        JSONObject detail = new JSONObject("""
                {
                  "id":203,
                  "name":"TV",
                  "last_air_date":"2026-03-03",
                  "episode_run_time":[0],
                  "number_of_episodes":0,
                  "content_ratings":{"results":[]},
                  "credits":{}
                }
                """);
        stubResponses(detail, new JSONObject());

        CachedContentVO candidate = candidate(203L, "TV");
        List<CachedContentVO> result =
                service.enrichBatch(List.of(candidate), providerRegistry);

        assertEquals(1, result.size());
        assertEquals("2026-03-03", candidate.getLastAirDate());
        assertNull(candidate.getRuntime());
        assertNull(candidate.getEpisodeCount());
        assertEquals("등급 정보 없음", candidate.getAgeRating());
        assertTrue(candidate.getPlatformKeys().isEmpty());
    }

    @Test
    void invalidOrRestrictedDetailShouldExcludeCandidateBeforeOverrides() {
        when(apiClient.get(anyString()))
                .thenReturn(new JSONObject())
                .thenReturn(new JSONObject("""
                        {
                          "id":301,
                          "release_dates":{
                            "results":[
                              {
                                "iso_3166_1":"US",
                                "release_dates":[{"certification":"NC-17"}]
                              }
                            ]
                          }
                        }
                        """));

        CachedContentVO invalid = candidate(300L, "MOVIE");
        CachedContentVO restricted = candidate(301L, "MOVIE");

        assertTrue(service.enrichBatch(
                List.of(invalid, restricted),
                providerRegistry).isEmpty());
        verify(manualOverrideService, never()).apply(invalid);
        verify(manualOverrideService, never()).apply(restricted);
    }

    @Test
    void oneCandidateFailureShouldNotStopSuccessfulCandidate() {
        JSONObject detail = new JSONObject("""
                {
                  "id":402,
                  "title":"정상 영화",
                  "release_dates":{"results":[]},
                  "credits":{}
                }
                """);
        JSONObject providers = new JSONObject(
                "{\"results\":{\"KR\":{\"flatrate\":[{\"provider_id\":8}]}}}");

        when(apiClient.get(anyString())).thenAnswer(invocation -> {
            String url = invocation.getArgument(0);
            if (url.contains("/401?")) {
                throw new IllegalStateException("network");
            }
            if (url.contains("/watch/providers")) {
                return providers;
            }
            return detail;
        });

        List<CachedContentVO> result = service.enrichBatch(
                List.of(candidate(401L, "MOVIE"), candidate(402L, "MOVIE")),
                providerRegistry);

        assertEquals(1, result.size());
        assertEquals(402L, result.get(0).getTmdbId());
    }

    private CachedContentVO candidate(Long tmdbId, String contentType) {
        CachedContentVO candidate = new CachedContentVO();
        candidate.setTmdbId(tmdbId);
        candidate.setContentType(contentType);
        return candidate;
    }

    private void stubResponses(JSONObject detail, JSONObject providers) {
        when(apiClient.get(anyString())).thenAnswer(invocation -> {
            String url = invocation.getArgument(0);
            return url.contains("/watch/providers")
                    ? providers
                    : detail;
        });
    }
}
