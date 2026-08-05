package com.project.oditji.search.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.project.oditji.search.vo.CachedContentVO;

/**
 * 검색 캐시의 노출 정책, 연령등급 변환, 저장소와 OTT 제공처 매핑을 검증합니다.
 */
@ExtendWith(MockitoExtension.class)
class SearchPolicyCoverageTest {

    @Mock
    private TmdbApiClient apiClient;

    private SearchContentAgeRatingResolver ageRatingResolver;
    private SearchContentPolicyService policyService;

    @BeforeEach
    void setUp() {
        ageRatingResolver = new SearchContentAgeRatingResolver();
        policyService = new SearchContentPolicyService();
        ReflectionTestUtils.setField(
                policyService,
                "koreanEnglishTitleOnly",
                true);
    }

    @Test
    void ageRatingResolverShouldDetectRestrictedMovieAndTvRatings() throws JSONException {
        JSONObject restrictedMovie = movieRatings(
                country("JP", "R18+"),
                country("US", "R"));
        JSONObject restrictedUsMovie = movieRatings(
                country("US", "NC-17"));
        JSONObject normalMovie = movieRatings(
                country("KR", "15"));

        assertTrue(ageRatingResolver.hasRestrictedMovieRating(restrictedMovie));
        assertTrue(ageRatingResolver.hasRestrictedMovieRating(restrictedUsMovie));
        assertFalse(ageRatingResolver.hasRestrictedMovieRating(normalMovie));
        assertFalse(ageRatingResolver.hasRestrictedMovieRating(null));

        JSONObject restrictedTv = tvRatings(
                rating("US", "TV-MA-LSV"));
        JSONObject normalTv = tvRatings(
                rating("US", "TV-14"));

        assertTrue(ageRatingResolver.hasRestrictedTvRating(restrictedTv));
        assertFalse(ageRatingResolver.hasRestrictedTvRating(normalTv));
        assertFalse(ageRatingResolver.hasRestrictedTvRating(null));

        JSONObject movieDetail = new JSONObject()
                .put("release_dates", restrictedMovie);
        JSONObject tvDetail = new JSONObject()
                .put("content_ratings", restrictedTv);

        assertTrue(ageRatingResolver.hasRestrictedSourceAgeRating(
                "movie",
                movieDetail));
        assertTrue(ageRatingResolver.hasRestrictedSourceAgeRating(
                "TV",
                tvDetail));
        assertFalse(ageRatingResolver.hasRestrictedSourceAgeRating(
                "OTHER",
                movieDetail));
        assertFalse(ageRatingResolver.hasRestrictedSourceAgeRating(
                null,
                movieDetail));
        assertFalse(ageRatingResolver.hasRestrictedSourceAgeRating(
                "MOVIE",
                null));
    }

    @Test
    void ageRatingResolverShouldParseMovieRatingsByCountryPriority() throws JSONException {
        assertEquals(
                "15세 이상 관람가",
                ageRatingResolver.parseMovieAgeRating(
                        movieRatings(
                                country("KR", "15세"),
                                country("US", "G"))));
        assertEquals(
                "15세 이상 관람가",
                ageRatingResolver.parseMovieAgeRating(
                        movieRatings(country("JP", "PG-12"))));
        assertEquals(
                "청소년 관람불가",
                ageRatingResolver.parseMovieAgeRating(
                        movieRatings(country("JP", "R15+"))));
        assertEquals(
                "전체 관람가",
                ageRatingResolver.parseMovieAgeRating(
                        movieRatings(country("US", "G"))));
        assertEquals(
                "12세 이상 관람가",
                ageRatingResolver.parseMovieAgeRating(
                        movieRatings(country("US", "PG-13"))));
        assertEquals(
                "15세 이상 관람가",
                ageRatingResolver.parseMovieAgeRating(
                        movieRatings(country("US", "R"))));
        assertEquals(
                "청소년 관람불가",
                ageRatingResolver.parseMovieAgeRating(
                        movieRatings(country("US", "NC17"))));
        assertEquals(
                "등급 정보 없음",
                ageRatingResolver.parseMovieAgeRating(
                        movieRatings(country("US", "UNRATED"))));
        assertEquals(
                "등급 정보 없음",
                ageRatingResolver.parseMovieAgeRating(null));
    }

    @Test
    void ageRatingResolverShouldParseTvAndNormalizeKoreanRatings() throws JSONException {
        assertEquals(
                "12세 이상 관람가",
                ageRatingResolver.parseTvAgeRating(
                        tvRatings(rating("KR", "12세"))));
        assertEquals(
                "전체 관람가",
                ageRatingResolver.parseTvAgeRating(
                        tvRatings(rating("US", "TV-Y"))));
        assertEquals(
                "7세 이상 관람가",
                ageRatingResolver.parseTvAgeRating(
                        tvRatings(rating("US", "TV-Y7"))));
        assertEquals(
                "12세 이상 관람가",
                ageRatingResolver.parseTvAgeRating(
                        tvRatings(rating("US", "TV-PG"))));
        assertEquals(
                "15세 이상 관람가",
                ageRatingResolver.parseTvAgeRating(
                        tvRatings(rating("US", "TV-14"))));
        assertEquals(
                "청소년 관람불가",
                ageRatingResolver.parseTvAgeRating(
                        tvRatings(rating("US", "TV-MA-S"))));
        assertEquals(
                "등급 정보 없음",
                ageRatingResolver.parseTvAgeRating(null));

        assertEquals("전체 관람가",
                ageRatingResolver.normalizeKoreanAgeRating("ALL"));
        assertEquals("전체 관람가",
                ageRatingResolver.normalizeKoreanAgeRating("0+"));
        assertEquals("7세 이상 관람가",
                ageRatingResolver.normalizeKoreanAgeRating("7세"));
        assertEquals("12세 이상 관람가",
                ageRatingResolver.normalizeKoreanAgeRating("12+"));
        assertEquals("15세 이상 관람가",
                ageRatingResolver.normalizeKoreanAgeRating("15"));
        assertEquals("청소년 관람불가",
                ageRatingResolver.normalizeKoreanAgeRating("제한 상영가"));
        assertEquals("등급 정보 없음",
                ageRatingResolver.normalizeKoreanAgeRating("unknown"));
        assertEquals("등급 정보 없음",
                ageRatingResolver.normalizeKoreanAgeRating(null));

        assertTrue(ageRatingResolver.isNormalizedAgeRating("전체 관람가"));
        assertTrue(ageRatingResolver.isNormalizedAgeRating("등급 정보 없음"));
        assertFalse(ageRatingResolver.isNormalizedAgeRating("R"));
        assertFalse(ageRatingResolver.isNormalizedAgeRating(null));
    }

    @Test
    void policyServiceShouldBlockExplicitContentKeysAndTitles() {
        assertTrue(policyService.shouldExcludeByContentKey(88090L, "tv"));
        assertFalse(policyService.shouldExcludeByContentKey(88090L, "movie"));
        assertFalse(policyService.shouldExcludeByContentKey(null, "TV"));
        assertFalse(policyService.shouldExcludeByContentKey(0L, "TV"));
        assertFalse(policyService.shouldExcludeByContentKey(88090L, " "));

        assertTrue(policyService.shouldExcludeByTitle(
                "무삭제판 성인영화",
                null));
        assertTrue(policyService.shouldExcludeByTitle(
                "An Adult Movie",
                null));
        assertTrue(policyService.shouldExcludeByTitle(
                "A PORN STAR story",
                null));
        assertTrue(policyService.shouldExcludeByTitle(
                "Drama",
                "Sex Tape"));
        assertFalse(policyService.shouldExcludeByTitle(
                "정상 영화",
                "Normal Film"));
        assertFalse(policyService.shouldExcludeByTitle(null, null));
    }

    @Test
    void policyServiceShouldApplyDisplayLanguageRule() {
        assertFalse(policyService.shouldExcludeByDisplayTitleLanguage("한국 Drama 2026!"));
        assertTrue(policyService.shouldExcludeByDisplayTitleLanguage("鬼滅の刃"));
        assertTrue(policyService.shouldExcludeByDisplayTitleLanguage("Привет"));
        assertFalse(policyService.shouldExcludeByDisplayTitleLanguage(null));
        assertFalse(policyService.shouldExcludeByDisplayTitleLanguage(" "));

        ReflectionTestUtils.setField(
                policyService,
                "koreanEnglishTitleOnly",
                false);
        assertFalse(policyService.shouldExcludeByDisplayTitleLanguage("鬼滅の刃"));
    }

    @Test
    void policyServiceShouldEvaluateCachedContent() {
        CachedContentVO blocked = createContent(
                88090L,
                "TV",
                "정상 제목",
                "Normal");
        CachedContentVO blockedByTitle = createContent(
                1L,
                "MOVIE",
                "에로 비디오",
                null);
        CachedContentVO allowed = createContent(
                2L,
                "MOVIE",
                "정상 영화",
                "Normal Film");

        assertTrue(policyService.shouldExcludeContent(blocked));
        assertTrue(policyService.shouldExcludeContent(blockedByTitle));
        assertFalse(policyService.shouldExcludeContent(allowed));
        assertFalse(policyService.shouldExcludeContent(null));
    }

    @Test
    void contentStoreShouldReplaceFilterAndFindContent() {
        SearchContentStore store = new SearchContentStore(policyService);
        CachedContentVO allowed = createContent(
                10L,
                "movie",
                "정상 영화",
                "Normal Film");
        CachedContentVO blocked = createContent(
                88090L,
                "TV",
                "정상 제목",
                "Normal");

        assertTrue(store.isEmpty());
        assertNull(store.getLastUpdatedAt());
        assertNull(store.findByTmdbIdAndContentType(null, "MOVIE"));
        assertNull(store.findByTmdbIdAndContentType(10L, " "));

        store.replaceAll(List.of(allowed, blocked));

        assertEquals(1, store.size());
        assertFalse(store.isEmpty());
        assertSame(allowed, store.getAll().get(0));
        assertSame(allowed,
                store.findByTmdbIdAndContentType(10L, " movie "));
        assertNull(store.findByTmdbIdAndContentType(88090L, "TV"));
        assertNotNull(store.getLastUpdatedAt());

        List<CachedContentVO> storedContents = store.getAll();
        CachedContentVO additionalContent = new CachedContentVO();
        assertThrows(
                UnsupportedOperationException.class,
                () -> storedContents.add(additionalContent));

        store.replaceAll(null);
        assertTrue(store.isEmpty());
    }

    @Test
    void providerRegistryShouldProtectMapsAndSelectByType() {
        TmdbProviderRegistry empty = new TmdbProviderRegistry(null, null);
        assertFalse(empty.hasAnyProvider());
        assertTrue(empty.getMovieProviderIds().isEmpty());
        assertTrue(empty.getTvProviderIds().isEmpty());

        TmdbProviderRegistry registry = new TmdbProviderRegistry(
                Map.of(8, "netflix"),
                Map.of(97, "watcha"));

        assertTrue(registry.hasAnyProvider());
        assertEquals(Map.of(8, "netflix"),
                registry.getProviderMap("MOVIE"));
        assertEquals(Map.of(97, "watcha"),
                registry.getProviderMap("TV"));
        assertEquals(Map.of(97, "watcha"),
                registry.getProviderMap(null));
        Map<Integer, String> movieProviders =
                registry.getProviderMap("MOVIE");
        assertThrows(
                UnsupportedOperationException.class,
                () -> movieProviders.put(9, "tving"));
    }

    @Test
    void providerServiceShouldLoadSupportedProvidersAndRejectEmptyRegistry() throws JSONException {
        TmdbProviderService service = new TmdbProviderService(apiClient);

        when(apiClient.getBaseUrl()).thenReturn("https://api.test");
        when(apiClient.getLanguage()).thenReturn("ko-KR");
        when(apiClient.getRegion()).thenReturn("KR");
        when(apiClient.encode(anyString())).thenAnswer(
                invocation -> invocation.getArgument(0));

        JSONObject movieProviders = new JSONObject()
                .put("results", new JSONArray()
                        .put(provider(8, "Netflix"))
                        .put(provider(0, "TVING"))
                        .put(JSONObject.NULL)
                        .put(provider(999, "Other")));
        JSONObject tvProviders = new JSONObject()
                .put("results", new JSONArray()
                        .put(provider(97, "Watcha"))
                        .put(provider(337, "Disney Plus")));

        when(apiClient.get(org.mockito.ArgumentMatchers.contains("/movie")))
                .thenReturn(movieProviders);
        when(apiClient.get(org.mockito.ArgumentMatchers.contains("/tv")))
                .thenReturn(tvProviders);

        TmdbProviderRegistry registry = service.loadRegistry();

        assertEquals("netflix", registry.getProviderMap("MOVIE").get(8));
        assertEquals("watcha", registry.getProviderMap("TV").get(97));
        assertEquals("disney", registry.getProviderMap("TV").get(337));
        assertEquals("", service.normalizePlatformName("Other"));
        assertEquals("coupang", service.normalizePlatformName("쿠팡플레이"));

        when(apiClient.get(anyString())).thenReturn(new JSONObject());
        assertThrows(IllegalStateException.class, service::loadRegistry);
    }

    private CachedContentVO createContent(
            Long tmdbId,
            String contentType,
            String title,
            String originalTitle) {

        CachedContentVO content = new CachedContentVO();
        content.setTmdbId(tmdbId);
        content.setContentType(contentType);
        content.setTitle(title);
        content.setOriginalTitle(originalTitle);
        return content;
    }

    private JSONObject movieRatings(JSONObject... countries) throws JSONException {
        return new JSONObject().put(
                "results",
                new JSONArray(countries));
    }

    private JSONObject country(String countryCode, String certification) throws JSONException {
        return new JSONObject()
                .put("iso_3166_1", countryCode)
                .put("release_dates", new JSONArray()
                        .put(new JSONObject()
                                .put("certification", certification)));
    }

    private JSONObject tvRatings(JSONObject... ratings) throws JSONException {
        return new JSONObject().put(
                "results",
                new JSONArray(ratings));
    }

    private JSONObject rating(String countryCode, String rating) throws JSONException {
        return new JSONObject()
                .put("iso_3166_1", countryCode)
                .put("rating", rating);
    }

    private JSONObject provider(int providerId, String providerName) throws JSONException {
        return new JSONObject()
                .put("provider_id", providerId)
                .put("provider_name", providerName);
    }
}
