package com.project.oditji.search.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.project.oditji.search.vo.CachedContentVO;

/** 연령등급 서비스의 수동 적용/제한 재검사 short-circuit 잔여 조건을 보완합니다. */
class SearchContentAgeRatingServiceNextGapCoverageTest {

    private TmdbApiClient apiClient;
    private SearchContentPolicyService policyService;
    private SearchContentAgeRatingResolver resolver;
    private SearchContentAgeRatingService service;

    @BeforeEach
    void setUp() {
        apiClient = mock(TmdbApiClient.class);
        policyService = mock(SearchContentPolicyService.class);
        resolver = mock(SearchContentAgeRatingResolver.class);
        service = new SearchContentAgeRatingService(apiClient, policyService, resolver);
    }

    @Test
    void manualOverrideGuardShouldReachEveryShortCircuitPosition() {
        service.applyManualOverride(null);

        CachedContentVO excluded = content(1L, "MOVIE", "등급 정보 없음");
        when(policyService.shouldExcludeContent(excluded)).thenReturn(true);
        service.applyManualOverride(excluded);
        assertEquals("등급 정보 없음", excluded.getAgeRating());

        CachedContentVO noId = content(null, "MOVIE", "등급 정보 없음");
        service.applyManualOverride(noId);
        assertEquals("등급 정보 없음", noId.getAgeRating());

        CachedContentVO blankType = content(2L, "   ", "등급 정보 없음");
        service.applyManualOverride(blankType);
        assertEquals("등급 정보 없음", blankType.getAgeRating());

        manualOverrides().set(Map.of("MOVIE:3", "15세 이상 관람가"));
        CachedContentVO valid = content(3L, "MOVIE", "등급 정보 없음");
        service.applyManualOverride(valid);
        assertEquals("15세 이상 관람가", valid.getAgeRating());
    }

    @Test
    void restrictionTargetGuardShouldReachEveryShortCircuitPosition() {
        assertFalse(isRestrictionTarget(null));

        CachedContentVO excluded = content(10L, "MOVIE", "청소년 관람불가");
        when(policyService.shouldExcludeContent(excluded)).thenReturn(true);
        assertFalse(isRestrictionTarget(excluded));

        CachedContentVO noId = content(null, "MOVIE", "청소년 관람불가");
        assertFalse(isRestrictionTarget(noId));

        CachedContentVO blankType = content(11L, " ", "청소년 관람불가");
        assertFalse(isRestrictionTarget(blankType));

        CachedContentVO checked = content(12L, "MOVIE", "청소년 관람불가");
        checked.setAgeRatingRestrictionChecked(Boolean.TRUE);
        assertFalse(isRestrictionTarget(checked));

        CachedContentVO adult = content(13L, "MOVIE", "청소년 관람불가");
        assertTrue(isRestrictionTarget(adult));

        CachedContentVO unknown = content(14L, "TV", "등급 정보 없음");
        assertTrue(isRestrictionTarget(unknown));
    }

    @Test
    void normalizedMovieAndTvTypesShouldLoadTheirDedicatedAgeRatingEndpoints() {
        when(apiClient.getBaseUrl()).thenReturn("https://api.test/3");
        when(apiClient.get("https://api.test/3/movie/20/release_dates"))
                .thenReturn(new JSONObject());
        when(apiClient.get("https://api.test/3/tv/21/content_ratings"))
                .thenReturn(new JSONObject());
        when(resolver.parseMovieAgeRating(any(JSONObject.class)))
                .thenReturn("12세 이상 관람가");
        when(resolver.parseTvAgeRating(any(JSONObject.class)))
                .thenReturn("15세 이상 관람가");

        Object movieResult = ReflectionTestUtils.invokeMethod(
                service,
                "loadAgeRating",
                content(20L, " movie ", "등급 정보 없음"));
        Object tvResult = ReflectionTestUtils.invokeMethod(
                service,
                "loadAgeRating",
                content(21L, " tv ", "등급 정보 없음"));

        assertNotNull(movieResult);
        assertNotNull(tvResult);
    }

    @Test
    void retryTargetShouldTreatNullRetryCountAsZeroAtBoundary() {
        CachedContentVO content = content(30L, "MOVIE", "등급 정보 없음");
        content.setAgeRatingRetryCount(null);
        manualOverrides().set(Map.of());

        Boolean retry = ReflectionTestUtils.invokeMethod(service, "isRetryTarget", content, 1);

        assertEquals(Boolean.TRUE, retry);
    }

    private boolean isRestrictionTarget(CachedContentVO content) {
        Boolean result = ReflectionTestUtils.invokeMethod(
                service,
                "isRestrictionRecheckTarget",
                content);
        return Boolean.TRUE.equals(result);
    }

    @SuppressWarnings("unchecked")
    private AtomicReference<Map<String, String>> manualOverrides() {
        return (AtomicReference<Map<String, String>>) ReflectionTestUtils.getField(
                service,
                "manualOverrideMap");
    }

    private CachedContentVO content(Long id, String type, String rating) {
        CachedContentVO content = new CachedContentVO();
        content.setTmdbId(id);
        content.setContentType(type);
        content.setAgeRating(rating);
        return content;
    }
}
