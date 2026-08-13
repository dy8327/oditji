package com.project.oditji.search.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.project.oditji.search.vo.CachedContentVO;

/** 연령등급 서비스의 public guard와 작은 helper 잔여 조건을 보완합니다. */
class SearchContentAgeRatingServiceResidualClosure3Test {

    private TmdbApiClient apiClient;
    private SearchContentAgeRatingService service;

    @BeforeEach
    void setUp() {
        apiClient = mock(TmdbApiClient.class);
        service = new SearchContentAgeRatingService(
                apiClient,
                mock(SearchContentPolicyService.class),
                new SearchContentAgeRatingResolver());

        ReflectionTestUtils.setField(service, "retryMaxAttempts", 2);
        ReflectionTestUtils.setField(service, "retryMaxPerRefresh", 100);
        ReflectionTestUtils.setField(service, "workerCount", 1);
    }

    @Test
    void unknownRecheckShouldCoverDisabledNullAndEmptyGuardsSeparately() {
        ReflectionTestUtils.setField(service, "retryEnabled", false);
        service.recheckUnknownAgeRatings(List.of(content(1L, "MOVIE", "등급 정보 없음")));

        ReflectionTestUtils.setField(service, "retryEnabled", true);
        service.recheckUnknownAgeRatings(null);
        service.recheckUnknownAgeRatings(List.of());

        verify(apiClient, never()).get(anyString());
    }

    @Test
    void restrictedRecheckAndManualApplyShouldCoverNullAndEmptyInputs() {
        service.recheckRestrictedAgeRatings(null);
        service.recheckRestrictedAgeRatings(List.of());
        service.applyManualOverrides(null);
        service.applyManualOverride(null);

        verify(apiClient, never()).get(anyString());
    }

    @Test
    void helperGuardsShouldCoverRestrictionCheckedAndInvalidLoadShapes() {
        CachedContentVO checked = content(2L, "MOVIE", "청소년 관람불가");
        checked.setAgeRatingRestrictionChecked(Boolean.TRUE);

        Boolean restrictedTarget = ReflectionTestUtils.invokeMethod(
                service,
                "isRestrictionRecheckTarget",
                checked);
        assertFalse(Boolean.TRUE.equals(restrictedTarget));

        assertNull(ReflectionTestUtils.invokeMethod(
                service,
                "loadAgeRating",
                new CachedContentVO()));

        CachedContentVO unknownType = content(3L, "OTHER", "등급 정보 없음");
        assertNull(ReflectionTestUtils.invokeMethod(
                service,
                "loadAgeRating",
                unknownType));

        List<CachedContentVO> same = ReflectionTestUtils.invokeMethod(
                service,
                "limitAgeRatingTargets",
                new ArrayList<CachedContentVO>(List.of(checked)),
                1);
        assertTrue(same.size() == 1);
    }

    private CachedContentVO content(Long tmdbId, String type, String ageRating) {
        CachedContentVO content = new CachedContentVO();
        content.setTmdbId(tmdbId);
        content.setContentType(type);
        content.setAgeRating(ageRating);
        return content;
    }
}
