package com.project.oditji.search.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.LinkedHashMap;
import java.util.Map;

import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.project.oditji.search.vo.CachedContentVO;

/** 연령등급 서비스의 수동 override 파서와 작은 helper 잔여 분기를 보완합니다. */
class SearchContentAgeRatingServiceFinalBranchCoverageTest {

    private SearchContentAgeRatingResolver ageRatingResolver;
    private SearchContentAgeRatingService service;

    @BeforeEach
    void setUp() {
        ageRatingResolver = mock(SearchContentAgeRatingResolver.class);
        service = new SearchContentAgeRatingService(
                mock(TmdbApiClient.class),
                mock(SearchContentPolicyService.class),
                ageRatingResolver);
    }

    @Test
    void manualOverrideParserShouldHandleSectionsFlatEntriesInvalidIdsAndUnknownRatings() {
        JSONObject movieSection = new JSONObject();
        movieSection.put("123", "15세 이상 관람가");
        movieSection.put("bad-id", "12세 이상 관람가");
        movieSection.put("0", "전체 관람가");
        movieSection.put("124", "UNKNOWN-RATING");

        JSONObject root = new JSONObject();
        root.put("MOVIE", movieSection);
        root.put("TV-456", "12세 이상 관람가");
        root.put("MOVIE-x", "7세 이상 관람가");
        root.put("TV-0", "전체 관람가");
        root.put("OTHER-1", "15세 이상 관람가");

        when(ageRatingResolver.normalizeKoreanAgeRating("UNKNOWN-RATING"))
                .thenReturn("등급 정보 없음");

        Map<String, String> result = castMap(invoke("parseManualOverrides", root));

        assertEquals("15세 이상 관람가", result.get("MOVIE:123"));
        assertEquals("12세 이상 관람가", result.get("TV:456"));
        assertFalse(result.containsKey("MOVIE:124"));
        assertFalse(result.containsKey("TV:0"));
    }

    @Test
    void flatAndSectionReadersShouldCoverInvalidPrefixesSeparatorsAndNumberFormats() {
        JSONObject root = new JSONObject();
        root.put("MOVIE-", "15세 이상 관람가");
        root.put("-123", "15세 이상 관람가");
        root.put("MOVIE-not-number", "15세 이상 관람가");
        root.put(" TV-777 ", "7세 이상 관람가");

        Map<String, String> result = new LinkedHashMap<String, String>();
        invoke("readFlatManualOverride", root, "MOVIE-", result);
        invoke("readFlatManualOverride", root, "-123", result);
        invoke("readFlatManualOverride", root, "MOVIE-not-number", result);
        invoke("readFlatManualOverride", root, " TV-777 ", result);

        assertEquals("7세 이상 관람가", result.get("TV:777"));

        JSONObject section = new JSONObject();
        section.put("bad", "전체 관람가");
        section.put("-3", "전체 관람가");
        section.put("888", "청소년 관람불가");
        invoke("addManualAgeRating", section, "bad", "MOVIE", result);
        invoke("addManualAgeRating", section, "-3", "MOVIE", result);
        invoke("addManualAgeRating", section, "888", "MOVIE", result);
        assertEquals("청소년 관람불가", result.get("MOVIE:888"));
    }

    @Test
    void normalizeManualAgeRatingShouldCoverNoTextExactNormalizedAndUnknownValues() {
        assertNull(invoke("normalizeManualAgeRating", new Object[] { null }));
        assertNull(invoke("normalizeManualAgeRating", "   "));
        assertEquals("전체 관람가", invoke("normalizeManualAgeRating", " 전체 관람가 "));
        assertEquals("7세 이상 관람가", invoke("normalizeManualAgeRating", "7세 이상 관람가"));
        assertEquals("12세 이상 관람가", invoke("normalizeManualAgeRating", "12세 이상 관람가"));
        assertEquals("15세 이상 관람가", invoke("normalizeManualAgeRating", "15세 이상 관람가"));
        assertEquals("청소년 관람불가", invoke("normalizeManualAgeRating", "청소년 관람불가"));

        when(ageRatingResolver.normalizeKoreanAgeRating("PG12"))
                .thenReturn("12세 이상 관람가");
        when(ageRatingResolver.normalizeKoreanAgeRating("UNKNOWN"))
                .thenReturn("등급 정보 없음");

        assertEquals("12세 이상 관람가", invoke("normalizeManualAgeRating", "PG12"));
        assertNull(invoke("normalizeManualAgeRating", "UNKNOWN"));
    }

    @Test
    void retryCountHasTextAndNullableJsonHelpersShouldCoverBothSides() {
        assertEquals(0, (Integer) invoke("getRetryCount", new Object[] { null }));

        CachedContentVO content = new CachedContentVO();
        content.setAgeRatingRetryCount(null);
        assertEquals(0, (Integer) invoke("getRetryCount", content));
        content.setAgeRatingRetryCount(-2);
        assertEquals(0, (Integer) invoke("getRetryCount", content));
        content.setAgeRatingRetryCount(3);
        assertEquals(3, (Integer) invoke("getRetryCount", content));

        assertFalse((Boolean) invoke("hasText", new Object[] { null }));
        assertFalse((Boolean) invoke("hasText", "   "));
        assertTrue((Boolean) invoke("hasText", " value "));

        JSONObject json = new JSONObject();
        invoke("putNullable", json, "nullValue", null);
        invoke("putNullable", json, "text", "value");
        assertTrue(json.isNull("nullValue"));
        assertEquals("value", json.getString("text"));
    }

    @Test
    void missingCandidateJsonShouldWriteNullAndPresentFields() {
        CachedContentVO content = new CachedContentVO();
        content.setTmdbId(100L);
        content.setTitle(null);
        content.setReleaseDate("2026-08-11");
        content.setPopularity(null);
        content.setAgeRatingRetryCount(2);
        content.setAgeRatingLastCheckedAt(null);

        JSONObject json = (JSONObject) invoke("createMissingCandidateJson", content);

        assertEquals(100L, json.getLong("tmdbId"));
        assertTrue(json.isNull("title"));
        assertEquals("2026-08-11", json.getString("releaseDate"));
        assertTrue(json.isNull("popularity"));
        assertEquals(2, json.getInt("retryCount"));
        assertTrue(json.isNull("lastCheckedAt"));
    }

    private Object invoke(String methodName, Object... arguments) {
        return ReflectionTestUtils.invokeMethod(service, methodName, arguments);
    }

    @SuppressWarnings("unchecked")
    private Map<String, String> castMap(Object value) {
        return (Map<String, String>) value;
    }
}
