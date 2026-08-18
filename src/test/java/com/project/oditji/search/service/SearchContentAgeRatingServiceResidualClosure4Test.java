package com.project.oditji.search.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.project.oditji.search.vo.CachedContentVO;

/** 원본 등급 제한 재검사 대상 판정의 앞쪽 단락 조건을 각각 보완합니다. */
class SearchContentAgeRatingServiceResidualClosure4Test {

    private SearchContentPolicyService policyService;
    private SearchContentAgeRatingService service;

    @BeforeEach
    void setUp() {
        policyService = mock(SearchContentPolicyService.class);
        service = new SearchContentAgeRatingService(
                mock(TmdbApiClient.class),
                policyService,
                mock(SearchContentAgeRatingResolver.class));
    }

    @Test
    void restrictionTargetShouldCoverEveryEarlyReturnOperand() {
        assertFalse(isRestrictionTarget(null));

        CachedContentVO excluded = content(1L, "MOVIE", "청소년 관람불가");
        when(policyService.shouldExcludeContent(excluded)).thenReturn(true);
        assertFalse(isRestrictionTarget(excluded));

        CachedContentVO noId = content(null, "MOVIE", "청소년 관람불가");
        assertFalse(isRestrictionTarget(noId));

        CachedContentVO blankType = content(2L, "   ", "청소년 관람불가");
        assertFalse(isRestrictionTarget(blankType));

        CachedContentVO checked = content(3L, "TV", "등급 정보 없음");
        checked.setAgeRatingRestrictionChecked(Boolean.TRUE);
        assertFalse(isRestrictionTarget(checked));

        CachedContentVO adult = content(4L, "MOVIE", "청소년 관람불가");
        adult.setAgeRatingRestrictionChecked(Boolean.FALSE);
        assertTrue(isRestrictionTarget(adult));
    }

    @Test
    void manualOverrideGuardShouldCoverExcludedNoIdAndBlankTypeOperands() {
        CachedContentVO excluded = content(10L, "MOVIE", "등급 정보 없음");
        when(policyService.shouldExcludeContent(excluded)).thenReturn(true);
        service.applyManualOverride(excluded);

        service.applyManualOverride(content(null, "MOVIE", "등급 정보 없음"));
        service.applyManualOverride(content(11L, " ", "등급 정보 없음"));
    }

    private boolean isRestrictionTarget(CachedContentVO content) {
        Boolean result = ReflectionTestUtils.invokeMethod(
                service,
                "isRestrictionRecheckTarget",
                content);
        return Boolean.TRUE.equals(result);
    }

    private CachedContentVO content(Long id, String type, String rating) {
        CachedContentVO content = new CachedContentVO();
        content.setTmdbId(id);
        content.setContentType(type);
        content.setAgeRating(rating);
        return content;
    }
}
