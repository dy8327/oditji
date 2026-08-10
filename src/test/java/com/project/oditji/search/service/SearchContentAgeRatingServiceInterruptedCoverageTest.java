package com.project.oditji.search.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.concurrent.Future;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.project.oditji.search.vo.CachedContentVO;

/**
 * 연령등급 Future 중단과 restriction target 수집 조건을 보완합니다.
 */
class SearchContentAgeRatingServiceInterruptedCoverageTest {

    private SearchContentPolicyService policyService;
    private SearchContentAgeRatingResolver resolver;
    private SearchContentAgeRatingService service;

    @BeforeEach
    void setUp() {
        policyService =
                mock(SearchContentPolicyService.class);
        resolver =
                mock(SearchContentAgeRatingResolver.class);

        service = new SearchContentAgeRatingService(
                mock(TmdbApiClient.class),
                policyService,
                resolver);
    }

    @AfterEach
    void clearInterruptFlag() {
        Thread.interrupted();
    }

    @Test
    void interruptedFutureShouldRestoreInterruptFlagAndThrowConfiguredMessage() throws Exception {
        Future<?> future = mock(Future.class);

        when(future.get())
                .thenThrow(
                        new InterruptedException(
                                "interrupted"));

        IllegalStateException exception =
                assertThrows(
                        IllegalStateException.class,
                        () -> ReflectionTestUtils.invokeMethod(
                                service,
                                "getAgeRatingResult",
                                future,
                                "중단 메시지"));

        assertEquals(
                "중단 메시지",
                exception.getMessage());
        assertTrue(
                Thread.currentThread()
                        .isInterrupted());
    }

    @Test
    void restrictionTargetCollectionShouldKeepOnlyEligibleRows() {
        CachedContentVO excluded =
                content(
                        1L,
                        "MOVIE",
                        "청소년 관람불가");
        when(policyService.shouldExcludeContent(excluded))
                .thenReturn(true);

        CachedContentVO checked =
                content(
                        2L,
                        "MOVIE",
                        "청소년 관람불가");
        checked.setAgeRatingRestrictionChecked(Boolean.TRUE);

        CachedContentVO eligible =
                content(
                        3L,
                        "TV",
                        "등급 정보 없음");

        @SuppressWarnings("unchecked")
        List<CachedContentVO> result =
                (List<CachedContentVO>)
                        ReflectionTestUtils.invokeMethod(
                                service,
                                "collectRestrictionRecheckTargets",
                                List.of(
                                        excluded,
                                        checked,
                                        eligible));

        assertEquals(
                List.of(eligible),
                result);
    }

    private CachedContentVO content(
            Long id,
            String type,
            String ageRating) {

        CachedContentVO content =
                new CachedContentVO();
        content.setTmdbId(id);
        content.setContentType(type);
        content.setAgeRating(ageRating);
        return content;
    }
}
