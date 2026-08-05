package com.project.oditji.search.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Year;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.project.oditji.search.vo.CachedContentVO;

/** Discover 후보 URL 구성, 필터링, 변환, 보완 수집과 중복 제거를 검증합니다. */
@ExtendWith(MockitoExtension.class)
class SearchContentDiscoverServiceCoverageTest {

    @Mock
    private TmdbApiClient apiClient;

    @Mock
    private SearchContentPolicyService contentPolicyService;

    private SearchContentDiscoverService service;

    @BeforeEach
    void setUp() {
        service = new SearchContentDiscoverService(apiClient, contentPolicyService);
        ReflectionTestUtils.setField(service, "startYear", Year.now().getValue() + 1);
        ReflectionTestUtils.setField(service, "supplementEnabled", true);
        ReflectionTestUtils.setField(service, "supplementYears", 1);
        ReflectionTestUtils.setField(service, "supplementPagesPerYear", 2);
        ReflectionTestUtils.setField(service, "supplementMaxCandidatesPerType", 10);

        lenient().when(apiClient.getBaseUrl()).thenReturn("https://api.test/3");
        lenient().when(apiClient.getLanguage()).thenReturn("ko-KR");
        lenient().when(apiClient.getRegion()).thenReturn("KR");
        lenient().when(apiClient.encode(anyString()))
                .thenAnswer(invocation -> invocation.getArgument(0));
        lenient().when(contentPolicyService.shouldExcludeContent(
                org.mockito.ArgumentMatchers.isNull(),
                org.mockito.ArgumentMatchers.isNull(),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any()))
                .thenAnswer(invocation -> {
                    String title = invocation.getArgument(2);
                    return title != null && title.contains("차단");
                });
    }

    @Test
    void movieCandidatesShouldBuildProviderUrlAndConvertAllowedItems() throws JSONException {
        JSONObject response = response(2,
                movie(1L, "영화", "Original", false, 28, 18),
                movie(2L, "성인", "Adult", true, 35),
                movie(3L, "차단 제목", "Blocked", false, 35),
                movie(0L, "잘못된 ID", "Invalid", false, 99),
                new JSONObject()
                        .put("id", 4L)
                        .put("title", " ")
                        .put("original_title", "원제로 대체")
                        .put("release_date", JSONObject.NULL)
                        .put("poster_path", JSONObject.NULL)
                        .put("vote_average", JSONObject.NULL)
                        .put("popularity", 5.0)
                        .put("genre_ids", new JSONArray().put(28).put(28).put(9999)));

        when(apiClient.get(anyString())).thenReturn(response);

        Set<Integer> providers = new LinkedHashSet<Integer>(Arrays.asList(
                8,
                null,
                -1,
                337));
        List<CachedContentVO> result = service.collectMovieCandidates(2, providers);

        assertEquals(2, result.size());
        assertEquals(1L, result.get(0).getTmdbId());
        assertEquals("MOVIE", result.get(0).getContentType());
        assertEquals("영화", result.get(0).getTitle());
        assertEquals("액션, 드라마", result.get(0).getGenreText());
        assertEquals(8.1, result.get(0).getTmdbScore());
        assertEquals("원제로 대체", result.get(1).getTitle());
        assertNull(result.get(1).getReleaseDate());
        assertNull(result.get(1).getPosterPath());
        assertNull(result.get(1).getTmdbScore());
        assertEquals("액션", result.get(1).getGenreText());

        ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
        verify(apiClient).get(urlCaptor.capture());
        String url = urlCaptor.getValue();
        assertTrue(url.contains("/discover/movie"));
        assertTrue(url.contains("with_watch_providers=8|337"));
        assertTrue(url.contains("primary_release_year="));
        assertTrue(url.contains("include_video=false"));
    }

    @Test
    void tvCandidatesShouldUseTvFieldsAndStopOnEmptyResults() throws JSONException {
        JSONObject response = response(1,
                new JSONObject()
                        .put("id", 10L)
                        .put("name", " ")
                        .put("original_name", "TV Original")
                        .put("first_air_date", "2026-08-01")
                        .put("poster_path", "/tv.jpg")
                        .put("vote_average", 7.5)
                        .put("popularity", 10.0)
                        .put("genre_ids", new JSONArray().put(10759).put(10765)));
        when(apiClient.get(anyString())).thenReturn(response);

        List<CachedContentVO> result = service.collectTvCandidates(5, Set.of(8));

        assertEquals(1, result.size());
        assertEquals("TV", result.get(0).getContentType());
        assertEquals("TV Original", result.get(0).getTitle());
        assertEquals("2026-08-01", result.get(0).getReleaseDate());
        assertEquals("액션·모험, SF·판타지", result.get(0).getGenreText());

        ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
        verify(apiClient).get(urlCaptor.capture());
        assertTrue(urlCaptor.getValue().contains("first_air_date_year="));
        assertFalse(urlCaptor.getValue().contains("include_video=false"));
    }

    @Test
    void invalidTargetsAndProvidersShouldReturnEmptyWithoutApiCall() {
        assertTrue(service.collectMovieCandidates(0, Set.of(8)).isEmpty());
        assertTrue(service.collectMovieCandidates(10, null).isEmpty());
        assertTrue(service.collectMovieCandidates(10, Set.of()).isEmpty());
        assertTrue(service.collectTvCandidates(-1, Set.of(8)).isEmpty());
    }

    @Test
    void supplementShouldCollectMovieAndTvThenRemoveDuplicateKeys() throws JSONException {
        JSONObject movieResponse = response(1,
                movie(100L, "보완 영화", "Supplement Movie", false, 28),
                movie(100L, "중복 영화", "Duplicate", false, 35));
        JSONObject tvResponse = response(1,
                new JSONObject()
                        .put("id", 200L)
                        .put("name", "보완 TV")
                        .put("original_name", "Supplement TV")
                        .put("first_air_date", "2026-01-01")
                        .put("popularity", 5.0)
                        .put("genre_ids", new JSONArray()));

        when(apiClient.get(anyString())).thenAnswer(invocation -> {
            String url = invocation.getArgument(0);
            return url.contains("/discover/movie")
                    ? movieResponse
                    : tvResponse;
        });

        List<CachedContentVO> result = service.collectSupplementCandidates();

        assertEquals(2, result.size());
        assertEquals("중복 영화", result.get(0).getTitle());
        assertEquals(100L, result.get(0).getTmdbId());
        assertEquals(200L, result.get(1).getTmdbId());
        assertNull(result.get(1).getGenreText());
    }

    @Test
    void supplementShouldHonorDisabledAndZeroMaximumSettings() {
        ReflectionTestUtils.setField(service, "supplementEnabled", false);
        assertTrue(service.collectSupplementCandidates().isEmpty());

        ReflectionTestUtils.setField(service, "supplementEnabled", true);
        ReflectionTestUtils.setField(service, "supplementMaxCandidatesPerType", 0);
        assertTrue(service.collectSupplementCandidates().isEmpty());
    }

    @Test
    void supplementShouldStopWhenApiReportsNoPagesOrNoItems() throws JSONException {
        ReflectionTestUtils.setField(service, "supplementYears", 99);
        ReflectionTestUtils.setField(service, "supplementPagesPerYear", 99);
        ReflectionTestUtils.setField(service, "supplementMaxCandidatesPerType", 6000);
        when(apiClient.get(anyString())).thenReturn(
                new JSONObject()
                        .put("total_pages", 0)
                        .put("results", new JSONArray()));

        assertTrue(service.collectSupplementCandidates().isEmpty());
    }

    private JSONObject response(int totalPages, JSONObject... items) throws JSONException {
        JSONArray results = new JSONArray();
        for (JSONObject item : items) {
            results.put(item);
        }
        return new JSONObject()
                .put("total_pages", totalPages)
                .put("results", results);
    }

    private JSONObject movie(
            long id,
            String title,
            String originalTitle,
            boolean adult,
            int... genreIds) throws JSONException {
        JSONArray genres = new JSONArray();
        for (int genreId : genreIds) {
            genres.put(genreId);
        }
        return new JSONObject()
                .put("id", id)
                .put("title", title)
                .put("original_title", originalTitle)
                .put("release_date", "2026-08-01")
                .put("poster_path", "/movie.jpg")
                .put("vote_average", 8.1)
                .put("popularity", 20.0)
                .put("adult", adult)
                .put("genre_ids", genres);
    }
}
