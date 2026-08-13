package com.project.oditji.search.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.project.oditji.search.vo.CachedContentVO;

/** 연령등급 결과 적용 helper의 성공/제한/기존등급 조합을 보완합니다. */
class SearchContentAgeRatingServiceResultApplicationCoverageTest {

    private TmdbApiClient apiClient;
    private SearchContentAgeRatingResolver resolver;
    private SearchContentAgeRatingService service;

    @BeforeEach
    void setUp() {
        apiClient = mock(TmdbApiClient.class);
        resolver = mock(SearchContentAgeRatingResolver.class);
        service = new SearchContentAgeRatingService(
                apiClient,
                mock(SearchContentPolicyService.class),
                resolver);

        when(apiClient.getBaseUrl()).thenReturn("https://api.test/3");
        when(apiClient.get(any(String.class))).thenReturn(new JSONObject());
    }

    @Test
    void unknownResultShouldApplyRatingRetryMetadataAndBlockedResultShouldBeRemovedCandidate() {
        CachedContentVO normal = content(10L, "등급 정보 없음");
        Object normalResult = movieResult(normal, "15세 이상 관람가", false);
        List<CachedContentVO> blocked = new ArrayList<CachedContentVO>();

        ReflectionTestUtils.invokeMethod(
                service,
                "applyUnknownAgeRatingResult",
                normalResult,
                blocked);

        assertEquals("15세 이상 관람가", normal.getAgeRating());
        assertEquals(Integer.valueOf(1), normal.getAgeRatingRetryCount());
        assertEquals(Boolean.TRUE, normal.getAgeRatingRestrictionChecked());
        assertTrue(blocked.isEmpty());

        CachedContentVO restricted = content(11L, "등급 정보 없음");
        Object restrictedResult = movieResult(restricted, "청소년 관람불가", true);

        ReflectionTestUtils.invokeMethod(
                service,
                "applyUnknownAgeRatingResult",
                restrictedResult,
                blocked);

        assertEquals(1, blocked.size());
        assertSame(restricted, blocked.get(0));
        assertEquals(Boolean.TRUE, restricted.getAgeRatingRestrictionChecked());
    }

    @Test
    void restrictedRecheckShouldUpdateOnlyUnknownContentWhenNewRatingIsKnown() {
        List<CachedContentVO> blocked = new ArrayList<CachedContentVO>();

        CachedContentVO unknown = content(20L, "등급 정보 없음");
        Object knownResult = movieResult(unknown, "12세 이상 관람가", false);
        ReflectionTestUtils.invokeMethod(
                service,
                "applyRestrictedAgeRatingResult",
                knownResult,
                blocked);
        assertEquals("12세 이상 관람가", unknown.getAgeRating());

        CachedContentVO alreadyAdult = content(21L, "청소년 관람불가");
        Object otherKnownResult = movieResult(alreadyAdult, "15세 이상 관람가", false);
        ReflectionTestUtils.invokeMethod(
                service,
                "applyRestrictedAgeRatingResult",
                otherKnownResult,
                blocked);
        assertEquals("청소년 관람불가", alreadyAdult.getAgeRating());

        CachedContentVO stillUnknown = content(22L, "등급 정보 없음");
        Object unknownResult = movieResult(stillUnknown, "등급 정보 없음", false);
        ReflectionTestUtils.invokeMethod(
                service,
                "applyRestrictedAgeRatingResult",
                unknownResult,
                blocked);
        assertEquals("등급 정보 없음", stillUnknown.getAgeRating());
    }

    private Object movieResult(
            CachedContentVO content,
            String ageRating,
            boolean restricted) {

        when(resolver.hasRestrictedMovieRating(any(JSONObject.class)))
                .thenReturn(restricted);
        when(resolver.parseMovieAgeRating(any(JSONObject.class)))
                .thenReturn(ageRating);

        return ReflectionTestUtils.invokeMethod(
                service,
                "loadAgeRating",
                content);
    }

    private CachedContentVO content(Long tmdbId, String ageRating) {
        CachedContentVO content = new CachedContentVO();
        content.setTmdbId(tmdbId);
        content.setContentType("MOVIE");
        content.setAgeRating(ageRating);
        return content;
    }
}
