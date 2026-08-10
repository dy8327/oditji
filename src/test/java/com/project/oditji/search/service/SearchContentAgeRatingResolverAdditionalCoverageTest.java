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
 * 연령등급 resolver의 세부 검색/변환 조건을 추가 검증합니다.
 */
class SearchContentAgeRatingResolverAdditionalCoverageTest {

    private SearchContentAgeRatingResolver resolver;

    @BeforeEach
    void setUp() {
        resolver = new SearchContentAgeRatingResolver();
    }

    @Test
    void movieCertificationSearchShouldSkipWrongCountryNullReleaseAndBlankCertification() {
        JSONArray countries = new JSONArray()
                .put(JSONObject.NULL)
                .put(new JSONObject()
                        .put("iso_3166_1", "US")
                        .put("release_dates", new JSONArray()))
                .put(new JSONObject()
                        .put("iso_3166_1", "KR")
                        .put("release_dates", new JSONArray()
                                .put(JSONObject.NULL)
                                .put(new JSONObject().put("certification", " "))
                                .put(new JSONObject().put("certification", "15"))));

        String certification = ReflectionTestUtils.invokeMethod(
                resolver,
                "findMovieCertification",
                countries,
                "KR");

        assertEquals("15", certification);

        String missing = ReflectionTestUtils.invokeMethod(
                resolver,
                "findMovieCertification",
                countries,
                "JP");

        assertNull(missing);
    }

    @Test
    void tvRatingSearchShouldSkipWrongCountryBlankAndNullRows() {
        JSONArray ratings = new JSONArray()
                .put(JSONObject.NULL)
                .put(new JSONObject()
                        .put("iso_3166_1", "KR")
                        .put("rating", " "))
                .put(new JSONObject()
                        .put("iso_3166_1", "US")
                        .put("rating", "TV-14"))
                .put(new JSONObject()
                        .put("iso_3166_1", "KR")
                        .put("rating", "12"));

        assertEquals(
                "12",
                ReflectionTestUtils.invokeMethod(
                        resolver,
                        "findTvRating",
                        ratings,
                        "KR"));

        assertNull(
                ReflectionTestUtils.invokeMethod(
                        resolver,
                        "findTvRating",
                        ratings,
                        "JP"));
    }

    @Test
    void japanAndUnitedStatesConvertersShouldCoverBlankAndUnknownBranches() {
        assertEquals(
                "등급 정보 없음",
                ReflectionTestUtils.invokeMethod(
                        resolver,
                        "convertJapanMovieAgeRating",
                        (Object) null));

        assertEquals(
                "등급 정보 없음",
                ReflectionTestUtils.invokeMethod(
                        resolver,
                        "convertJapanMovieAgeRating",
                        "G"));

        assertEquals(
                "등급 정보 없음",
                ReflectionTestUtils.invokeMethod(
                        resolver,
                        "convertUsMovieAgeRating",
                        " "));

        assertEquals(
                "등급 정보 없음",
                ReflectionTestUtils.invokeMethod(
                        resolver,
                        "convertUsMovieAgeRating",
                        "X"));

        assertEquals(
                "등급 정보 없음",
                ReflectionTestUtils.invokeMethod(
                        resolver,
                        "convertUsTvAgeRating",
                        "UNKNOWN"));
    }

    @Test
    void restrictedUsTvRatingShouldCoverBlankAllowedAndBlockedCodes() {
        assertFalse(invokeRestrictedTv(null));
        assertFalse(invokeRestrictedTv(" "));
        assertFalse(invokeRestrictedTv("TV-MA"));
        assertTrue(invokeRestrictedTv("TV-MA-S"));
        assertTrue(invokeRestrictedTv("TV MA LS"));
        assertTrue(invokeRestrictedTv("TV_MA_SV"));
        assertTrue(invokeRestrictedTv("TV-MA-LSV"));
    }

    @Test
    void hasTextShouldReturnFalseForNullBlankAndTrueForText() {
        assertFalse(invokeHasText(null));
        assertFalse(invokeHasText("   "));
        assertTrue(invokeHasText("value"));
    }

    private boolean invokeRestrictedTv(String rating) {
        Boolean result = ReflectionTestUtils.invokeMethod(
                resolver,
                "isRestrictedUsTvRating",
                rating);
        return Boolean.TRUE.equals(result);
    }

    private boolean invokeHasText(String value) {
        Boolean result = ReflectionTestUtils.invokeMethod(
                resolver,
                "hasText",
                value);
        return Boolean.TRUE.equals(result);
    }
}
