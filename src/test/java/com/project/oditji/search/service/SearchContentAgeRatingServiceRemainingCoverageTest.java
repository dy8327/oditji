package com.project.oditji.search.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.project.oditji.search.vo.CachedContentVO;

/**
 * 연령등급 서비스의 수동 보강, retry/restriction 대상, null helper 조건을 보완합니다.
 */
class SearchContentAgeRatingServiceRemainingCoverageTest {

    private SearchContentPolicyService contentPolicyService;
    private SearchContentAgeRatingResolver ageRatingResolver;
    private SearchContentAgeRatingService service;

    @BeforeEach
    void setUp() {
        TmdbApiClient apiClient =
                mock(TmdbApiClient.class);
        contentPolicyService =
                mock(SearchContentPolicyService.class);
        ageRatingResolver =
                mock(SearchContentAgeRatingResolver.class);

        service = new SearchContentAgeRatingService(
                apiClient,
                contentPolicyService,
                ageRatingResolver);
    }

    @Test
    void manualOverrideShouldCoverEveryEarlyReturnAndSuccessfulLookup() {
        service.applyManualOverrides(null);
        service.applyManualOverride(null);

        CachedContentVO excluded =
                content(1L, "MOVIE", "등급 정보 없음");
        when(contentPolicyService.shouldExcludeContent(excluded))
                .thenReturn(true);
        service.applyManualOverride(excluded);

        CachedContentVO noId =
                content(null, "MOVIE", "등급 정보 없음");
        service.applyManualOverride(noId);

        CachedContentVO blankType =
                content(2L, "   ", "등급 정보 없음");
        service.applyManualOverride(blankType);

        CachedContentVO valid =
                content(3L, " movie ", "등급 정보 없음");

        manualOverrideMap().set(
                Map.of(
                        "MOVIE:3",
                        "15세 이상 관람가"));

        service.applyManualOverride(valid);

        assertEquals(
                "15세 이상 관람가",
                valid.getAgeRating());
    }

    @Test
    void restrictionTargetShouldCoverNullExcludedMetadataCheckedAndBothAllowedRatings() {
        assertFalse(isRestrictionTarget(null));

        CachedContentVO excluded =
                content(1L, "MOVIE", "청소년 관람불가");
        when(contentPolicyService.shouldExcludeContent(excluded))
                .thenReturn(true);
        assertFalse(isRestrictionTarget(excluded));

        CachedContentVO noId =
                content(null, "MOVIE", "청소년 관람불가");
        assertFalse(isRestrictionTarget(noId));

        CachedContentVO blankType =
                content(2L, " ", "청소년 관람불가");
        assertFalse(isRestrictionTarget(blankType));

        CachedContentVO checked =
                content(3L, "TV", "청소년 관람불가");
        checked.setAgeRatingRestrictionChecked(Boolean.TRUE);
        assertFalse(isRestrictionTarget(checked));

        assertTrue(isRestrictionTarget(
                content(4L, "MOVIE", "청소년 관람불가")));
        assertTrue(isRestrictionTarget(
                content(5L, "TV", "등급 정보 없음")));
        assertFalse(isRestrictionTarget(
                content(6L, "MOVIE", "15세 이상 관람가")));
    }

    @Test
    void retryTargetShouldCoverManualOverrideAndRetryCountBoundaries() {
        CachedContentVO valid =
                content(10L, "MOVIE", "등급 정보 없음");

        valid.setAgeRatingRetryCount(null);
        assertTrue(isRetryTarget(valid, 2));

        valid.setAgeRatingRetryCount(-1);
        assertTrue(isRetryTarget(valid, 2));

        valid.setAgeRatingRetryCount(2);
        assertFalse(isRetryTarget(valid, 2));

        manualOverrideMap().set(
                Map.of(
                        "MOVIE:10",
                        "12세 이상 관람가"));
        valid.setAgeRatingRetryCount(0);

        assertFalse(isRetryTarget(valid, 2));

        CachedContentVO known =
                content(11L, "TV", "15세 이상 관람가");
        assertFalse(isRetryTarget(known, 2));
    }

    @Test
    void manualAgeNormalizationShouldCoverDirectResolverAndUnknownValues() {
        assertNull(normalizeManual(null));
        assertNull(normalizeManual("   "));

        assertEquals(
                "전체 관람가",
                normalizeManual(" 전체 관람가 "));
        assertEquals(
                "7세 이상 관람가",
                normalizeManual("7세 이상 관람가"));
        assertEquals(
                "12세 이상 관람가",
                normalizeManual("12세 이상 관람가"));
        assertEquals(
                "15세 이상 관람가",
                normalizeManual("15세 이상 관람가"));
        assertEquals(
                "청소년 관람불가",
                normalizeManual("청소년 관람불가"));

        when(ageRatingResolver.normalizeKoreanAgeRating("12세"))
                .thenReturn("12세 이상 관람가");
        assertEquals(
                "12세 이상 관람가",
                normalizeManual("12세"));

        when(ageRatingResolver.normalizeKoreanAgeRating("UNKNOWN"))
                .thenReturn("등급 정보 없음");
        assertNull(normalizeManual("UNKNOWN"));
    }

    @Test
    void parseManualOverridesShouldIgnoreMalformedEntriesAndKeepValidSectionAndFlatValues() {
        when(ageRatingResolver.normalizeKoreanAgeRating("15세"))
                .thenReturn("15세 이상 관람가");

        JSONObject movieSection =
                new JSONObject()
                        .put("100", "전체 관람가")
                        .put("-1", "12세 이상 관람가")
                        .put("bad", "15세 이상 관람가");

        JSONObject root =
                new JSONObject()
                        .put("MOVIE", movieSection)
                        .put("TV", "not-object")
                        .put("MOVIE-200", "15세")
                        .put("TV-", "12세 이상 관람가")
                        .put("OTHER-300", "15세 이상 관람가")
                        .put("MOVIE-bad", "15세 이상 관람가");

        Map<String, String> parsed =
                ReflectionTestUtils.invokeMethod(
                        service,
                        "parseManualOverrides",
                        root);

        assertEquals(
                "전체 관람가",
                parsed.get("MOVIE:100"));
        assertEquals(
                "15세 이상 관람가",
                parsed.get("MOVIE:200"));
        assertEquals(2, parsed.size());
    }

    @Test
    void lookupMetadataAndNullableHelpersShouldCoverNullAndNormalValues() {
        service.markLookupCompleted(null);

        CachedContentVO content =
                content(30L, "MOVIE", "등급 정보 없음");

        service.markLookupCompleted(content);

        assertTrue(
                content.getAgeRatingLastCheckedAt() != null
                        && !content.getAgeRatingLastCheckedAt().isBlank());

        Integer nullRetry = ReflectionTestUtils.invokeMethod(
                service,
                "getRetryCount",
                (Object) null);
        assertEquals(0, nullRetry.intValue());

        content.setAgeRatingRetryCount(-5);
        Integer negativeRetry = ReflectionTestUtils.invokeMethod(
                service,
                "getRetryCount",
                content);
        assertEquals(0, negativeRetry.intValue());

        JSONObject json = new JSONObject();
        ReflectionTestUtils.invokeMethod(
                service,
                "putNullable",
                json,
                "nullValue",
                null);
        ReflectionTestUtils.invokeMethod(
                service,
                "putNullable",
                json,
                "value",
                "text");

        assertTrue(json.isNull("nullValue"));
        assertEquals("text", json.getString("value"));

        assertEquals(
                "TV:99",
                ReflectionTestUtils.invokeMethod(
                        service,
                        "createKey",
                        " tv ",
                        99L));
    }

    @SuppressWarnings("unchecked")
    private AtomicReference<Map<String, String>> manualOverrideMap() {
        return (AtomicReference<Map<String, String>>)
                ReflectionTestUtils.getField(
                        service,
                        "manualOverrideMap");
    }

    private boolean isRestrictionTarget(
            CachedContentVO content) {

        Boolean result = ReflectionTestUtils.invokeMethod(
                service,
                "isRestrictionRecheckTarget",
                content);

        return Boolean.TRUE.equals(result);
    }

    private boolean isRetryTarget(
            CachedContentVO content,
            int maxAttempts) {

        Boolean result = ReflectionTestUtils.invokeMethod(
                service,
                "isRetryTarget",
                content,
                maxAttempts);

        return Boolean.TRUE.equals(result);
    }

    private String normalizeManual(
            String rawValue) {

        return ReflectionTestUtils.invokeMethod(
                service,
                "normalizeManualAgeRating",
                rawValue);
    }

    private CachedContentVO content(
            Long tmdbId,
            String contentType,
            String ageRating) {

        CachedContentVO content =
                new CachedContentVO();
        content.setTmdbId(tmdbId);
        content.setContentType(contentType);
        content.setAgeRating(ageRating);
        return content;
    }
}