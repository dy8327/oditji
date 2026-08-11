package com.project.oditji.search.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.project.oditji.search.vo.CachedContentVO;

/** 연령등급 재조회 대상 판정의 단락 조건과 잘못된 타입 분기를 보완합니다. */
class SearchContentAgeRatingServiceShortCircuitCoverageTest {

    private TmdbApiClient apiClient;
    private SearchContentPolicyService policyService;
    private SearchContentAgeRatingService service;

    @BeforeEach
    void setUp() {
        apiClient = mock(TmdbApiClient.class);
        policyService = mock(SearchContentPolicyService.class);
        service = new SearchContentAgeRatingService(
                apiClient,
                policyService,
                mock(SearchContentAgeRatingResolver.class));
    }

    @Test
    void retryTargetShouldCoverEachEarlyReturnPosition() {
        assertFalse(isRetryTarget(null, 2));

        CachedContentVO excluded = content(1L, "MOVIE", "등급 정보 없음");
        when(policyService.shouldExcludeContent(excluded)).thenReturn(true);
        assertFalse(isRetryTarget(excluded, 2));

        CachedContentVO noId = content(null, "MOVIE", "등급 정보 없음");
        assertFalse(isRetryTarget(noId, 2));

        CachedContentVO blankType = content(2L, "   ", "등급 정보 없음");
        assertFalse(isRetryTarget(blankType, 2));

        CachedContentVO known = content(3L, "MOVIE", "15세 이상 관람가");
        assertFalse(isRetryTarget(known, 2));

        CachedContentVO manual = content(4L, "MOVIE", "등급 정보 없음");
        manualOverrideMap().set(Map.of("MOVIE:4", "12세 이상 관람가"));
        assertFalse(isRetryTarget(manual, 2));

        manualOverrideMap().set(Map.of());
        manual.setAgeRatingRetryCount(1);
        assertTrue(isRetryTarget(manual, 2));

        manual.setAgeRatingRetryCount(2);
        assertFalse(isRetryTarget(manual, 2));
    }

    @Test
    void restrictionTargetShouldCoverFalseMetadataAndUnsupportedAge() {
        CachedContentVO uncheckedAdult = content(10L, "MOVIE", "청소년 관람불가");
        uncheckedAdult.setAgeRatingRestrictionChecked(Boolean.FALSE);
        assertTrue(isRestrictionTarget(uncheckedAdult));

        CachedContentVO uncheckedUnknown = content(11L, "TV", "등급 정보 없음");
        uncheckedUnknown.setAgeRatingRestrictionChecked(null);
        assertTrue(isRestrictionTarget(uncheckedUnknown));

        CachedContentVO unsupported = content(12L, "TV", "12세 이상 관람가");
        unsupported.setAgeRatingRestrictionChecked(Boolean.FALSE);
        assertFalse(isRestrictionTarget(unsupported));
    }

    @Test
    void loadAgeRatingShouldRejectIncompleteAndUnknownContentTypes() {
        assertNull(loadAgeRating(null));
        assertNull(loadAgeRating(content(null, "MOVIE", "등급 정보 없음")));
        assertNull(loadAgeRating(content(20L, "  ", "등급 정보 없음")));
        assertNull(loadAgeRating(content(21L, "OTHER", "등급 정보 없음")));
    }

    @Test
    void manualOverrideMissShouldLeaveExistingRatingUntouched() {
        CachedContentVO content = content(30L, " movie ", "등급 정보 없음");
        manualOverrideMap().set(Map.of());

        service.applyManualOverride(content);

        assertEquals("등급 정보 없음", content.getAgeRating());
    }

    private boolean isRetryTarget(CachedContentVO content, int maxAttempts) {
        Boolean result = ReflectionTestUtils.invokeMethod(
                service,
                "isRetryTarget",
                content,
                maxAttempts);
        return Boolean.TRUE.equals(result);
    }

    private boolean isRestrictionTarget(CachedContentVO content) {
        Boolean result = ReflectionTestUtils.invokeMethod(
                service,
                "isRestrictionRecheckTarget",
                content);
        return Boolean.TRUE.equals(result);
    }

    private Object loadAgeRating(CachedContentVO content) {
        return ReflectionTestUtils.invokeMethod(
                service,
                "loadAgeRating",
                content);
    }

    @SuppressWarnings("unchecked")
    private AtomicReference<Map<String, String>> manualOverrideMap() {
        return (AtomicReference<Map<String, String>>) ReflectionTestUtils.getField(
                service,
                "manualOverrideMap");
    }

    private CachedContentVO content(Long id, String type, String ageRating) {
        CachedContentVO content = new CachedContentVO();
        content.setTmdbId(id);
        content.setContentType(type);
        content.setAgeRating(ageRating);
        return content;
    }
}
