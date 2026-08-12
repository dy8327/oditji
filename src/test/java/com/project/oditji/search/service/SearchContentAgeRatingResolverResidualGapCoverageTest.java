package com.project.oditji.search.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

/** 연령등급 resolver의 남은 null 입력 전용 early-return 분기를 검증합니다. */
class SearchContentAgeRatingResolverResidualGapCoverageTest {

    private SearchContentAgeRatingResolver resolver;

    @BeforeEach
    void setUp() {
        resolver = new SearchContentAgeRatingResolver();
    }

    @Test
    void movieCertificationSearchShouldReturnNullWhenCountryArrayIsNull() {
        assertNull(ReflectionTestUtils.invokeMethod(
                resolver,
                "findMovieCertification",
                null,
                "KR"));
    }

    @Test
    void tvRatingSearchShouldReturnNullWhenRatingArrayIsNull() {
        assertNull(ReflectionTestUtils.invokeMethod(
                resolver,
                "findTvRating",
                null,
                "KR"));
    }

    @Test
    void usTvConverterShouldReturnUnknownForNullAndBlankSourceValues() {
        assertEquals(
                "등급 정보 없음",
                ReflectionTestUtils.invokeMethod(
                        resolver,
                        "convertUsTvAgeRating",
                        (Object) null));
        assertEquals(
                "등급 정보 없음",
                ReflectionTestUtils.invokeMethod(
                        resolver,
                        "convertUsTvAgeRating",
                        "   "));
    }

    @Test
    void restrictedSourceCheckShouldCoverNullDetailBeforeContentTypeCheck() {
        assertFalse(resolver.hasRestrictedSourceAgeRating("MOVIE", null));
    }

    @Test
    void movieAndTvParsersShouldCoverMissingResultsArrays() {
        org.json.JSONObject emptyRoot = new org.json.JSONObject();

        assertEquals("등급 정보 없음", resolver.parseMovieAgeRating(emptyRoot));
        assertEquals("등급 정보 없음", resolver.parseTvAgeRating(emptyRoot));
    }
}
