package com.project.oditji.search.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * 연령등급 변환기의 국가별 변환, 제한 등급 및 내부 null 조건을 폭넓게 검증합니다.
 */
class SearchContentAgeRatingResolverCoverageTest {

    private SearchContentAgeRatingResolver resolver;

    @BeforeEach
    void setUp() {
        resolver = new SearchContentAgeRatingResolver();
    }

    @Test
    void restrictedSourceCheckShouldHandleNullBlankUnknownAndBothMediaTypes() {
        JSONObject movieDetail = new JSONObject()
                .put(
                        "release_dates",
                        movieReleaseRoot("JP", "R18+"));

        JSONObject tvDetail = new JSONObject()
                .put(
                        "content_ratings",
                        tvRatingRoot("US", "TV-MA-S"));

        assertFalse(resolver.hasRestrictedSourceAgeRating(null, movieDetail));
        assertFalse(resolver.hasRestrictedSourceAgeRating("   ", movieDetail));
        assertFalse(resolver.hasRestrictedSourceAgeRating("OTHER", movieDetail));
        assertTrue(resolver.hasRestrictedSourceAgeRating("movie", movieDetail));
        assertTrue(resolver.hasRestrictedSourceAgeRating("tv", tvDetail));
    }

    @Test
    void restrictedMovieRatingsShouldCoverJapanUnitedStatesAndMissingArrays() {
        assertFalse(resolver.hasRestrictedMovieRating(null));
        assertFalse(resolver.hasRestrictedMovieRating(new JSONObject()));
        assertFalse(resolver.hasRestrictedMovieRating(
                new JSONObject().put("results", "not-array")));

        assertTrue(resolver.hasRestrictedMovieRating(
                movieReleaseRoot("JP", "R18 +")));
        assertTrue(resolver.hasRestrictedMovieRating(
                movieReleaseRoot("US", "NC-17")));
        assertFalse(resolver.hasRestrictedMovieRating(
                movieReleaseRoot("US", "R")));
    }

    @Test
    void restrictedTvRatingsShouldCoverAllBlockedCombinations() {
        assertFalse(resolver.hasRestrictedTvRating(null));
        assertFalse(resolver.hasRestrictedTvRating(new JSONObject()));

        assertTrue(resolver.hasRestrictedTvRating(
                tvRatingRoot("US", "TV-MA-S")));
        assertTrue(resolver.hasRestrictedTvRating(
                tvRatingRoot("US", "TV MA LS")));
        assertTrue(resolver.hasRestrictedTvRating(
                tvRatingRoot("US", "TV_MA_SV")));
        assertTrue(resolver.hasRestrictedTvRating(
                tvRatingRoot("US", "TV-MA-LSV")));

        assertFalse(resolver.hasRestrictedTvRating(
                tvRatingRoot("US", "TV-MA")));
        assertFalse(resolver.hasRestrictedTvRating(
                tvRatingRoot("KR", "18")));
    }

    @Test
    void movieAgeParsingShouldUseKoreaThenJapanThenUnitedStates() {
        assertEquals(
                "등급 정보 없음",
                resolver.parseMovieAgeRating(null));
        assertEquals(
                "전체 관람가",
                resolver.parseMovieAgeRating(
                        movieReleaseRoot("KR", "ALL")));
        assertEquals(
                "15세 이상 관람가",
                resolver.parseMovieAgeRating(
                        movieReleaseRoot("JP", "PG12")));
        assertEquals(
                "청소년 관람불가",
                resolver.parseMovieAgeRating(
                        movieReleaseRoot("JP", "R15+")));

        JSONObject japanUnknownThenUs =
                new JSONObject().put(
                        "results",
                        new JSONArray()
                                .put(movieCountry("JP", "G"))
                                .put(movieCountry("US", "PG-13")));

        assertEquals(
                "12세 이상 관람가",
                resolver.parseMovieAgeRating(japanUnknownThenUs));

        assertEquals(
                "15세 이상 관람가",
                resolver.parseMovieAgeRating(
                        movieReleaseRoot("US", "R")));
        assertEquals(
                "청소년 관람불가",
                resolver.parseMovieAgeRating(
                        movieReleaseRoot("US", "NC17")));
        assertEquals(
                "등급 정보 없음",
                resolver.parseMovieAgeRating(
                        movieReleaseRoot("US", "UNKNOWN")));
    }

    @Test
    void tvAgeParsingShouldCoverKoreanAndUnitedStatesMappings() {
        assertEquals(
                "등급 정보 없음",
                resolver.parseTvAgeRating(null));
        assertEquals(
                "15세 이상 관람가",
                resolver.parseTvAgeRating(
                        tvRatingRoot("KR", "15")));

        assertEquals(
                "전체 관람가",
                resolver.parseTvAgeRating(
                        tvRatingRoot("US", "TV-Y")));
        assertEquals(
                "7세 이상 관람가",
                resolver.parseTvAgeRating(
                        tvRatingRoot("US", "TVY7")));
        assertEquals(
                "12세 이상 관람가",
                resolver.parseTvAgeRating(
                        tvRatingRoot("US", "TV-PG")));
        assertEquals(
                "15세 이상 관람가",
                resolver.parseTvAgeRating(
                        tvRatingRoot("US", "TV14")));
        assertEquals(
                "청소년 관람불가",
                resolver.parseTvAgeRating(
                        tvRatingRoot("US", "TV-MA-LSV")));
        assertEquals(
                "등급 정보 없음",
                resolver.parseTvAgeRating(
                        tvRatingRoot("US", "UNKNOWN")));
    }

    @Test
    void koreanNormalizationShouldCoverEveryAgeFamilyAndUnknowns() {
        assertEquals(
                "등급 정보 없음",
                resolver.normalizeKoreanAgeRating(null));
        assertEquals(
                "등급 정보 없음",
                resolver.normalizeKoreanAgeRating(" "));

        assertEquals(
                "전체 관람가",
                resolver.normalizeKoreanAgeRating("전체 이용가"));
        assertEquals(
                "7세 이상 관람가",
                resolver.normalizeKoreanAgeRating("7세"));
        assertEquals(
                "12세 이상 관람가",
                resolver.normalizeKoreanAgeRating("12+"));
        assertEquals(
                "15세 이상 관람가",
                resolver.normalizeKoreanAgeRating("15 세"));
        assertEquals(
                "청소년 관람불가",
                resolver.normalizeKoreanAgeRating("19+"));
        assertEquals(
                "청소년 관람불가",
                resolver.normalizeKoreanAgeRating("청불"));
        assertEquals(
                "청소년 관람불가",
                resolver.normalizeKoreanAgeRating("제한상영가"));
        assertEquals(
                "등급 정보 없음",
                resolver.normalizeKoreanAgeRating("PG"));
    }

    @Test
    void normalizedRatingRecognitionShouldCoverBlankKnownAndUnknownValues() {
        assertFalse(resolver.isNormalizedAgeRating(null));
        assertFalse(resolver.isNormalizedAgeRating(" "));
        assertTrue(resolver.isNormalizedAgeRating("전체 관람가"));
        assertTrue(resolver.isNormalizedAgeRating("7세 이상 관람가"));
        assertTrue(resolver.isNormalizedAgeRating("12세 이상 관람가"));
        assertTrue(resolver.isNormalizedAgeRating("15세 이상 관람가"));
        assertTrue(resolver.isNormalizedAgeRating("청소년 관람불가"));
        assertTrue(resolver.isNormalizedAgeRating("등급 정보 없음"));
        assertFalse(resolver.isNormalizedAgeRating("UNKNOWN"));
    }

    @Test
    void internalSearchHelpersShouldCoverNullInvalidAndMatchingEntries() {
        JSONArray countries = new JSONArray()
                .put(JSONObject.NULL)
                .put(new JSONObject()
                        .put("iso_3166_1", "US"))
                .put(movieCountry("JP", "R18+"));

        JSONArray found = ReflectionTestUtils.invokeMethod(
                resolver,
                "findMovieReleaseDates",
                countries,
                "JP");
        assertEquals(2, found.length());

        JSONArray missing = ReflectionTestUtils.invokeMethod(
                resolver,
                "findMovieReleaseDates",
                countries,
                "KR");
        assertNull(missing);

        Boolean noCountries = ReflectionTestUtils.invokeMethod(
                resolver,
                "containsMovieCertification",
                null,
                "JP",
                "R18+");
        assertFalse(noCountries);

        Boolean blankCountry = ReflectionTestUtils.invokeMethod(
                resolver,
                "containsMovieCertification",
                countries,
                " ",
                "R18+");
        assertFalse(blankCountry);

        Boolean blankRestricted = ReflectionTestUtils.invokeMethod(
                resolver,
                "containsMovieCertification",
                countries,
                "JP",
                " ");
        assertFalse(blankRestricted);

        JSONArray releases = new JSONArray()
                .put(JSONObject.NULL)
                .put(new JSONObject().put("certification", "R"))
                .put(new JSONObject().put("certification", "NC-17"));

        Boolean contains = ReflectionTestUtils.invokeMethod(
                resolver,
                "containsCertification",
                releases,
                "NC17");
        assertTrue(contains);

        Boolean nullArray = ReflectionTestUtils.invokeMethod(
                resolver,
                "containsCertification",
                null,
                "NC17");
        assertFalse(nullArray);

        String normalizedNull = ReflectionTestUtils.invokeMethod(
                resolver,
                "normalizeRatingCode",
                (Object) null);
        assertEquals("", normalizedNull);
    }

    private JSONObject movieReleaseRoot(
            String countryCode,
            String certification) {

        return new JSONObject().put(
                "results",
                new JSONArray().put(
                        movieCountry(
                                countryCode,
                                certification)));
    }

    private JSONObject movieCountry(
            String countryCode,
            String certification) {

        return new JSONObject()
                .put("iso_3166_1", countryCode)
                .put(
                        "release_dates",
                        new JSONArray()
                                .put(JSONObject.NULL)
                                .put(new JSONObject()
                                        .put(
                                                "certification",
                                                certification)));
    }

    private JSONObject tvRatingRoot(
            String countryCode,
            String rating) {

        return new JSONObject().put(
                "results",
                new JSONArray()
                        .put(JSONObject.NULL)
                        .put(new JSONObject()
                                .put("iso_3166_1", countryCode)
                                .put("rating", rating)));
    }
}
