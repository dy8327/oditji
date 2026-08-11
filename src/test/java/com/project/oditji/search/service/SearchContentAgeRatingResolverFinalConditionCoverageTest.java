package com.project.oditji.search.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * 연령등급 정규화의 OR 체인과 국가별 변환 switch의 동일 의미 표기를 검증합니다.
 */
class SearchContentAgeRatingResolverFinalConditionCoverageTest {

    private SearchContentAgeRatingResolver resolver;

    @BeforeEach
    void setUp() {
        resolver = new SearchContentAgeRatingResolver();
    }

    @Test
    void koreanNormalizationShouldCoverAlternativeOperandsIndividually() {
        assertEquals("전체 관람가", resolver.normalizeKoreanAgeRating("ALL"));
        assertEquals("전체 관람가", resolver.normalizeKoreanAgeRating("전체"));
        assertEquals("전체 관람가", resolver.normalizeKoreanAgeRating("전체관람가"));
        assertEquals("전체 관람가", resolver.normalizeKoreanAgeRating("0"));
        assertEquals("전체 관람가", resolver.normalizeKoreanAgeRating("0+"));

        assertEquals("7세 이상 관람가", resolver.normalizeKoreanAgeRating("7"));
        assertEquals("7세 이상 관람가", resolver.normalizeKoreanAgeRating("7+"));

        assertEquals("12세 이상 관람가", resolver.normalizeKoreanAgeRating("12"));
        assertEquals("12세 이상 관람가", resolver.normalizeKoreanAgeRating("12세"));

        assertEquals("15세 이상 관람가", resolver.normalizeKoreanAgeRating("15"));
        assertEquals("15세 이상 관람가", resolver.normalizeKoreanAgeRating("15+"));

        assertEquals("청소년 관람불가", resolver.normalizeKoreanAgeRating("18"));
        assertEquals("청소년 관람불가", resolver.normalizeKoreanAgeRating("18+"));
        assertEquals("청소년 관람불가", resolver.normalizeKoreanAgeRating("19"));
        assertEquals("청소년 관람불가", resolver.normalizeKoreanAgeRating("18세"));
        assertEquals("청소년 관람불가", resolver.normalizeKoreanAgeRating("19세"));
        assertEquals("청소년 관람불가", resolver.normalizeKoreanAgeRating("청소년 관람불가"));
    }

    @Test
    void countryConvertersShouldCoverAllEquivalentSwitchLabels() {
        assertEquals("15세 이상 관람가", invoke("convertJapanMovieAgeRating", "PG12"));
        assertEquals("청소년 관람불가", invoke("convertJapanMovieAgeRating", "R15"));

        assertEquals("전체 관람가", invoke("convertUsMovieAgeRating", "G"));
        assertEquals("12세 이상 관람가", invoke("convertUsMovieAgeRating", "PG"));
        assertEquals("12세 이상 관람가", invoke("convertUsMovieAgeRating", "PG13"));
        assertEquals("청소년 관람불가", invoke("convertUsMovieAgeRating", "NC-17"));

        assertEquals("전체 관람가", invoke("convertUsTvAgeRating", "TVG"));
        assertEquals("전체 관람가", invoke("convertUsTvAgeRating", "TVY"));
        assertEquals("7세 이상 관람가", invoke("convertUsTvAgeRating", "TV-Y7"));
        assertEquals("12세 이상 관람가", invoke("convertUsTvAgeRating", "TVPG"));
        assertEquals("15세 이상 관람가", invoke("convertUsTvAgeRating", "TV-14"));
        assertEquals("청소년 관람불가", invoke("convertUsTvAgeRating", "TVMA"));
    }

    private String invoke(String methodName, String value) {
        return ReflectionTestUtils.invokeMethod(resolver, methodName, value);
    }
}
