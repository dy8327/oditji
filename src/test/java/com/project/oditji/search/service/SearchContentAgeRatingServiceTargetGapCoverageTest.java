package com.project.oditji.search.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.project.oditji.search.vo.CachedContentVO;

/** 연령등급 서비스의 재조회/제한 재검사 대상 수집과 정렬 helper 잔여 분기를 보완합니다. */
class SearchContentAgeRatingServiceTargetGapCoverageTest {

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
    void retryTargetCollectionShouldSkipNullExcludedInvalidAndCompletedContents() {
        CachedContentVO excluded = content(1L, "MOVIE", "등급 정보 없음", 0, 1.0);
        when(policyService.shouldExcludeContent(excluded)).thenReturn(true);

        CachedContentVO noId = content(null, "MOVIE", "등급 정보 없음", 0, 2.0);
        CachedContentVO blankType = content(2L, " ", "등급 정보 없음", 0, 3.0);
        CachedContentVO normalRating = content(3L, "TV", "15세 이상 관람가", 0, 4.0);
        CachedContentVO attemptsDone = content(4L, "TV", "등급 정보 없음", 2, 5.0);
        CachedContentVO target = content(5L, "TV", "등급 정보 없음", 1, 6.0);

        List<CachedContentVO> source = new ArrayList<CachedContentVO>();
        source.add(null);
        source.add(excluded);
        source.add(noId);
        source.add(blankType);
        source.add(normalRating);
        source.add(attemptsDone);
        source.add(target);

        Object raw = ReflectionTestUtils.invokeMethod(service, "collectRetryTargets", source, 2);
        List<?> targets = (List<?>) raw;

        assertEquals(1, targets.size());
        assertSame(target, targets.get(0));
    }

    @Test
    void restrictionCollectionShouldRequireAdultOrUnknownAndUncheckedMetadata() {
        CachedContentVO adult = content(10L, "MOVIE", "청소년 관람불가", 0, 1.0);
        CachedContentVO unknown = content(11L, "TV", "등급 정보 없음", 0, 2.0);
        CachedContentVO normal = content(12L, "TV", "12세 이상 관람가", 0, 3.0);
        CachedContentVO checked = content(13L, "MOVIE", "청소년 관람불가", 0, 4.0);
        checked.setAgeRatingRestrictionChecked(Boolean.TRUE);

        Object raw = ReflectionTestUtils.invokeMethod(
                service,
                "collectRestrictionRecheckTargets",
                List.of(adult, unknown, normal, checked));
        List<?> targets = (List<?>) raw;

        assertEquals(2, targets.size());
        assertTrue(targets.contains(adult));
        assertTrue(targets.contains(unknown));
    }

    @Test
    void targetSortingLimitingWorkerClampAndBlockedRemovalShouldCoverBoundaries() {
        CachedContentVO low = content(1L, "TV", "등급 정보 없음", 0, 1.0);
        CachedContentVO high = content(2L, "MOVIE", "등급 정보 없음", 0, 10.0);
        CachedContentVO noPopularity = content(3L, "MOVIE", "등급 정보 없음", 0, null);

        List<CachedContentVO> targets = new ArrayList<CachedContentVO>(List.of(low, noPopularity, high));
        ReflectionTestUtils.invokeMethod(service, "sortAgeRatingTargets", targets);
        assertSame(high, targets.get(0));
        assertSame(noPopularity, targets.get(2));

        Object sameRaw = ReflectionTestUtils.invokeMethod(service, "limitAgeRatingTargets", targets, 5);
        assertSame(targets, sameRaw);

        Object limitedRaw = ReflectionTestUtils.invokeMethod(service, "limitAgeRatingTargets", targets, 1);
        List<?> limited = (List<?>) limitedRaw;
        assertEquals(1, limited.size());
        assertSame(high, limited.get(0));

        ReflectionTestUtils.setField(service, "workerCount", 0);
        assertEquals(1, ((Integer) ReflectionTestUtils.invokeMethod(service, "normalizeWorkerCount")).intValue());
        ReflectionTestUtils.setField(service, "workerCount", 99);
        assertEquals(12, ((Integer) ReflectionTestUtils.invokeMethod(service, "normalizeWorkerCount")).intValue());
        ReflectionTestUtils.setField(service, "workerCount", 5);
        assertEquals(5, ((Integer) ReflectionTestUtils.invokeMethod(service, "normalizeWorkerCount")).intValue());

        List<CachedContentVO> contents = new ArrayList<CachedContentVO>(targets);
        ReflectionTestUtils.invokeMethod(service, "removeBlockedContents", contents, List.of());
        assertEquals(3, contents.size());
        ReflectionTestUtils.invokeMethod(service, "removeBlockedContents", contents, List.of(high));
        assertEquals(2, contents.size());
    }

    private CachedContentVO content(
            Long id,
            String type,
            String rating,
            Integer retryCount,
            Double popularity) {

        CachedContentVO content = new CachedContentVO();
        content.setTmdbId(id);
        content.setContentType(type);
        content.setAgeRating(rating);
        content.setAgeRatingRetryCount(retryCount);
        content.setPopularity(popularity);
        return content;
    }
}
