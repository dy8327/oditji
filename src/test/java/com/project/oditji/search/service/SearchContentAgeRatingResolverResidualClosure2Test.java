package com.project.oditji.search.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

/** 연령등급 resolver의 국가 일치 후 누락/빈 배열 잔여 분기를 보완합니다. */
class SearchContentAgeRatingResolverResidualClosure2Test {

    private SearchContentAgeRatingResolver resolver;

    @BeforeEach
    void setUp() {
        resolver = new SearchContentAgeRatingResolver();
    }

    @Test
    void countryMovieCertificationShouldReturnNullWhenMatchedCountryHasNoReleaseDates() {
        JSONObject country = new JSONObject()
                .put("iso_3166_1", "KR");

        String result = ReflectionTestUtils.invokeMethod(
                resolver,
                "findCountryMovieCertification",
                country,
                "KR");

        assertNull(result);
    }

    @Test
    void countryMovieCertificationShouldSkipNullAndBlankReleaseRowsBeforeValue() {
        JSONObject country = new JSONObject()
                .put("iso_3166_1", "KR")
                .put("release_dates", new JSONArray()
                        .put(JSONObject.NULL)
                        .put(new JSONObject().put("certification", "   "))
                        .put(new JSONObject().put("certification", "12")));

        String result = ReflectionTestUtils.invokeMethod(
                resolver,
                "findCountryMovieCertification",
                country,
                "KR");

        assertEquals("12", result);
    }

    @Test
    void restrictedCertificationShouldEvaluateNonNullRowsThatDoNotMatch() {
        JSONArray releases = new JSONArray()
                .put(JSONObject.NULL)
                .put(new JSONObject().put("certification", "R"));

        Boolean result = ReflectionTestUtils.invokeMethod(
                resolver,
                "containsCertification",
                releases,
                "NC17");

        assertNotEquals(Boolean.TRUE, result);
    }

    @Test
    void tvRatingSearchShouldFinishWithNullAfterMatchingCountryHasBlankRating() {
        JSONArray ratings = new JSONArray()
                .put(new JSONObject()
                        .put("iso_3166_1", "US")
                        .put("rating", "   "));

        String result = ReflectionTestUtils.invokeMethod(
                resolver,
                "findTvRating",
                ratings,
                "US");

        assertNull(result);
    }
}
