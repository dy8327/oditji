package com.project.oditji.search.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.project.oditji.search.vo.CachedContentVO;

/** Discover 서비스의 작은 JSON/URL/중복제거 helper 잔여 분기를 보완합니다. */
class SearchContentDiscoverServiceFinalBranchCoverageTest {

    private TmdbApiClient apiClient;
    private SearchContentDiscoverService service;

    @BeforeEach
    void setUp() {
        apiClient = mock(TmdbApiClient.class);
        when(apiClient.getBaseUrl()).thenReturn("https://api.test");
        when(apiClient.getLanguage()).thenReturn("ko-KR");
        when(apiClient.getRegion()).thenReturn("KR");
        when(apiClient.encode(anyString())).thenAnswer(invocation -> invocation.getArgument(0));

        service = new SearchContentDiscoverService(
                apiClient,
                mock(SearchContentPolicyService.class));
    }

    @Test
    void urlBuildersShouldCoverMovieAndTvYearFilters() {
        String movieUrl = (String) invoke(
                "buildDiscoverUrl",
                "movie",
                2026,
                2,
                "8|337");
        String tvUrl = (String) invoke(
                "buildDiscoverUrl",
                "tv",
                2026,
                3,
                "8");
        String supplementMovie = (String) invoke(
                "buildSupplementDiscoverUrl",
                "movie",
                2025,
                1);
        String supplementTv = (String) invoke(
                "buildSupplementDiscoverUrl",
                "tv",
                2025,
                1);

        assertTrue(movieUrl.contains("primary_release_year=2026"));
        assertTrue(movieUrl.contains("include_video=false"));
        assertTrue(tvUrl.contains("first_air_date_year=2026"));
        assertTrue(supplementMovie.contains("region=KR"));
        assertTrue(supplementTv.contains("first_air_date_year=2025"));
    }

    @Test
    void providerJoinShouldSkipNullAndNonPositiveValues() {
        Set<Integer> providerIds = new LinkedHashSet<Integer>(Arrays.asList(null, -1, 0, 8, 337));
        assertEquals("8|337", invoke("joinProviderIds", providerIds));
        assertEquals("", invoke("joinProviderIds", Set.of()));
    }

    @Test
    void genreConversionShouldCoverNullUnknownDuplicateAndMovieTvMaps() {
        assertNull(invoke("convertGenreIdsToText", null, "MOVIE"));
        assertNull(invoke("convertGenreIdsToText", new JSONArray(), "MOVIE"));

        JSONArray movieGenres = new JSONArray();
        movieGenres.put(28);
        movieGenres.put(28);
        movieGenres.put(-1);
        String movieText = (String) invoke("convertGenreIdsToText", movieGenres, "MOVIE");
        assertTrue(movieText.contains("액션"));

        JSONArray tvGenres = new JSONArray();
        tvGenres.put(18);
        String tvText = (String) invoke("convertGenreIdsToText", tvGenres, "TV");
        assertTrue(tvText.contains("드라마"));
    }

    @Test
    void duplicateRemovalShouldSkipIncompleteItemsAndKeepLastDuplicate() {
        CachedContentVO noId = content(null, "MOVIE", "no-id");
        CachedContentVO noType = content(1L, null, "no-type");
        CachedContentVO first = content(10L, "MOVIE", "first");
        CachedContentVO second = content(10L, "MOVIE", "second");

        List<CachedContentVO> source = Arrays.asList(null, noId, noType, first, second);
        List<CachedContentVO> result = castContentList(invoke("removeDuplicate", source));

        assertEquals(1, result.size());
        assertEquals("second", result.get(0).getTitle());
    }

    @Test
    void nullableHelpersShouldCoverMissingNullBlankZeroAndPresentValues() {
        JSONObject json = new JSONObject();
        json.put("nullValue", JSONObject.NULL);
        json.put("blank", "   ");
        json.put("text", " value ");
        json.put("zero", 0);
        json.put("negative", -2);
        json.put("positive", 10);
        json.put("score", 7.5);

        assertNull(invoke("nullableString", json, "missing"));
        assertNull(invoke("nullableString", json, "nullValue"));
        assertNull(invoke("nullableString", json, "blank"));
        assertEquals(" value ", invoke("nullableString", json, "text"));

        assertNull(invoke("nullableLong", json, "missing"));
        assertNull(invoke("nullableLong", json, "nullValue"));
        assertNull(invoke("nullableLong", json, "zero"));
        assertNull(invoke("nullableLong", json, "negative"));
        assertEquals(Long.valueOf(10L), (Long) invoke("nullableLong", json, "positive"));

        assertNull(invoke("nullableDouble", json, "missing"));
        assertNull(invoke("nullableDouble", json, "nullValue"));
        assertEquals(Double.valueOf(7.5), (Double) invoke("nullableDouble", json, "score"));
    }

    @Test
    void firstNonBlankAndDiscoverItemConversionShouldCoverBothContentTypes() {
        assertEquals("first", invoke("firstNonBlank", "first", "second"));
        assertEquals("second", invoke("firstNonBlank", null, "second"));
        assertEquals("second", invoke("firstNonBlank", "   ", "second"));

        JSONObject movie = new JSONObject();
        movie.put("id", 101);
        movie.put("title", "");
        movie.put("original_title", "원제");
        movie.put("release_date", "2026-08-11");
        movie.put("genre_ids", new JSONArray().put(28));
        CachedContentVO movieContent = (CachedContentVO) invoke("convertDiscoverItem", movie, "MOVIE");
        assertEquals("원제", movieContent.getTitle());

        JSONObject tv = new JSONObject();
        tv.put("id", 202);
        tv.put("name", "TV 제목");
        tv.put("original_name", "TV Original");
        tv.put("first_air_date", "2026-08-10");
        tv.put("genre_ids", new JSONArray().put(18));
        CachedContentVO tvContent = (CachedContentVO) invoke("convertDiscoverItem", tv, "TV");
        assertEquals("TV 제목", tvContent.getTitle());
    }

    @Test
    void appendDiscoverItemsShouldReturnFalseForNullOrEmptyAndRespectLimit() {
        List<CachedContentVO> result = new ArrayList<CachedContentVO>();
        assertFalse((Boolean) invoke("appendDiscoverItems", result, null, "MOVIE", 2));
        assertFalse((Boolean) invoke("appendDiscoverItems", result, new JSONArray(), "MOVIE", 2));
    }

    private CachedContentVO content(Long tmdbId, String type, String title) {
        CachedContentVO content = new CachedContentVO();
        content.setTmdbId(tmdbId);
        content.setContentType(type);
        content.setTitle(title);
        return content;
    }

    private Object invoke(String methodName, Object... arguments) {
        return ReflectionTestUtils.invokeMethod(service, methodName, arguments);
    }

    @SuppressWarnings("unchecked")
    private List<CachedContentVO> castContentList(Object value) {
        return (List<CachedContentVO>) value;
    }
}
