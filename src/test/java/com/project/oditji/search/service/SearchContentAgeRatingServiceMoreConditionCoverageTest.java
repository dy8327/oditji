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

/**
 * 연령등급 수동 override 파싱 helper의 prefix, separator, ID, 등급값 조건을 보완합니다.
 */
class SearchContentAgeRatingServiceMoreConditionCoverageTest {

    private SearchContentAgeRatingResolver resolver;
    private SearchContentAgeRatingService service;

    @BeforeEach
    void setUp() {
        resolver = mock(SearchContentAgeRatingResolver.class);

        service = new SearchContentAgeRatingService(
                mock(TmdbApiClient.class),
                mock(SearchContentPolicyService.class),
                resolver);
    }

    @Test
    void manualSectionKeyShouldOnlyRecognizeExactMovieAndTvNames() {
        assertTrue(isSection("MOVIE"));
        assertTrue(isSection("TV"));
        assertFalse(isSection("movie"));
        assertFalse(isSection("OTHER"));
    }

    @Test
    void flatOverrideReaderShouldSkipWrongPrefixMissingIdNonNumericZeroAndUnknownRating() {
        Map<String, String> result =
                new LinkedHashMap<String, String>();

        JSONObject root = new JSONObject()
                .put("OTHER-1", "전체 관람가")
                .put("MOVIE-", "전체 관람가")
                .put("MOVIE-bad", "전체 관람가")
                .put("MOVIE-0", "전체 관람가")
                .put("MOVIE-10", "UNKNOWN")
                .put("TV-20", "15세 이상 관람가");

        when(resolver.normalizeKoreanAgeRating("UNKNOWN"))
                .thenReturn("등급 정보 없음");

        readFlat(root, "OTHER-1", result);
        readFlat(root, "MOVIE-", result);
        readFlat(root, "MOVIE-bad", result);
        readFlat(root, "MOVIE-0", result);
        readFlat(root, "MOVIE-10", result);
        readFlat(root, "TV-20", result);

        assertEquals(
                Map.of(
                        "TV:20",
                        "15세 이상 관람가"),
                result);
    }

    @Test
    void manualSectionReaderShouldIgnoreMissingSectionBadIdsZeroIdsAndKeepValidEntry() {
        Map<String, String> result =
                new LinkedHashMap<String, String>();

        JSONObject root = new JSONObject();

        ReflectionTestUtils.invokeMethod(
                service,
                "readManualSection",
                root,
                "MOVIE",
                result);

        assertTrue(result.isEmpty());

        root.put(
                "MOVIE",
                new JSONObject()
                        .put("bad", "전체 관람가")
                        .put("0", "전체 관람가")
                        .put("15", "12세 이상 관람가"));

        ReflectionTestUtils.invokeMethod(
                service,
                "readManualSection",
                root,
                "MOVIE",
                result);

        assertEquals(
                "12세 이상 관람가",
                result.get("MOVIE:15"));
        assertEquals(1, result.size());
    }

    @Test
    void retryCountAndTextHelpersShouldCoverNullNegativeAndPositiveValues() {
        assertEquals(0, retryCount(null));

        CachedContentVO content = new CachedContentVO();

        content.setAgeRatingRetryCount(null);
        assertEquals(0, retryCount(content));

        content.setAgeRatingRetryCount(-3);
        assertEquals(0, retryCount(content));

        content.setAgeRatingRetryCount(4);
        assertEquals(4, retryCount(content));

        assertFalse(hasText(null));
        assertFalse(hasText("   "));
        assertTrue(hasText("value"));

        assertEquals(
                "MOVIE:30",
                ReflectionTestUtils.invokeMethod(
                        service,
                        "createKey",
                        " movie ",
                        30L));
    }

    @Test
    void manualAgeNormalizerShouldKeepKnownValuesAndDropUnknownResolverResult() {
        assertNull(normalize(null));
        assertNull(normalize(" "));

        assertEquals(
                "전체 관람가",
                normalize(" 전체 관람가 "));

        when(resolver.normalizeKoreanAgeRating("12세"))
                .thenReturn("12세 이상 관람가");

        assertEquals(
                "12세 이상 관람가",
                normalize("12세"));

        when(resolver.normalizeKoreanAgeRating("UNKNOWN"))
                .thenReturn("등급 정보 없음");

        assertNull(normalize("UNKNOWN"));
    }

    private boolean isSection(String key) {
        Boolean result = ReflectionTestUtils.invokeMethod(
                service,
                "isManualSectionKey",
                key);
        return Boolean.TRUE.equals(result);
    }

    private void readFlat(
            JSONObject root,
            String key,
            Map<String, String> result) {

        ReflectionTestUtils.invokeMethod(
                service,
                "readFlatManualOverride",
                root,
                key,
                result);
    }

    private int retryCount(CachedContentVO content) {
        Integer result = ReflectionTestUtils.invokeMethod(
                service,
                "getRetryCount",
                content);
        return result.intValue();
    }

    private boolean hasText(String value) {
        Boolean result = ReflectionTestUtils.invokeMethod(
                service,
                "hasText",
                value);
        return Boolean.TRUE.equals(result);
    }

    private String normalize(String value) {
        return ReflectionTestUtils.invokeMethod(
                service,
                "normalizeManualAgeRating",
                value);
    }
}
