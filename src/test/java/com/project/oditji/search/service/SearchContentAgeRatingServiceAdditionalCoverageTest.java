package com.project.oditji.search.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.project.oditji.search.vo.CachedContentVO;

/**
 * 연령등급 서비스의 정렬/제한/작업결과 helper 분기를 추가 검증합니다.
 */
class SearchContentAgeRatingServiceAdditionalCoverageTest {

    private SearchContentAgeRatingService service;

    @BeforeEach
    void setUp() {
        service = new SearchContentAgeRatingService(
                mock(TmdbApiClient.class),
                mock(SearchContentPolicyService.class),
                mock(SearchContentAgeRatingResolver.class));
    }

    @Test
    void targetLimitShouldReturnSameListOrNewLimitedList() {
        CachedContentVO first = content(1L, "MOVIE", 30.0);
        CachedContentVO second = content(2L, "TV", 20.0);

        List<CachedContentVO> source =
                new ArrayList<CachedContentVO>(
                        List.of(first, second));

        @SuppressWarnings("unchecked")
        List<CachedContentVO> same =
                (List<CachedContentVO>) ReflectionTestUtils.invokeMethod(
                        service,
                        "limitAgeRatingTargets",
                        source,
                        2);

        assertSame(source, same);

        @SuppressWarnings("unchecked")
        List<CachedContentVO> limited =
                (List<CachedContentVO>) ReflectionTestUtils.invokeMethod(
                        service,
                        "limitAgeRatingTargets",
                        source,
                        1);

        assertEquals(1, limited.size());
        assertSame(first, limited.get(0));
    }

    @Test
    void targetSortShouldOrderPopularityThenTypeThenIdWithNullsLast() {
        CachedContentVO low = content(3L, "TV", 10.0);
        CachedContentVO highMovie = content(2L, "MOVIE", 30.0);
        CachedContentVO highTv = content(1L, "TV", 30.0);
        CachedContentVO nullPopularity = content(4L, "MOVIE", null);

        List<CachedContentVO> values =
                new ArrayList<CachedContentVO>(
                        List.of(low, highTv, nullPopularity, highMovie));

        ReflectionTestUtils.invokeMethod(
                service,
                "sortAgeRatingTargets",
                values);

        assertSame(highMovie, values.get(0));
        assertSame(highTv, values.get(1));
        assertSame(low, values.get(2));
        assertSame(nullPopularity, values.get(3));
    }

    @Test
    void workerCountShouldClampToOneAndTwelve() {
        ReflectionTestUtils.setField(service, "workerCount", -3);
        assertEquals(
                1,
                ((Integer) ReflectionTestUtils.invokeMethod(
                        service,
                        "normalizeWorkerCount")).intValue());

        ReflectionTestUtils.setField(service, "workerCount", 99);
        assertEquals(
                12,
                ((Integer) ReflectionTestUtils.invokeMethod(
                        service,
                        "normalizeWorkerCount")).intValue());
    }

    @Test
    void executionFailureShouldReturnNullInsteadOfPropagating() {
        CompletableFuture<Object> failed =
                new CompletableFuture<Object>();
        failed.completeExceptionally(
                new IllegalStateException("failure"));

        Object result = ReflectionTestUtils.invokeMethod(
                service,
                "getAgeRatingResult",
                (Future<?>) failed,
                "중단");

        assertNull(result);
    }

    @Test
    void removeBlockedContentsShouldOnlyMutateWhenBlockedListHasValues() {
        CachedContentVO first = content(1L, "MOVIE", 1.0);
        CachedContentVO second = content(2L, "MOVIE", 2.0);

        List<CachedContentVO> contents =
                new ArrayList<CachedContentVO>(
                        List.of(first, second));

        ReflectionTestUtils.invokeMethod(
                service,
                "removeBlockedContents",
                contents,
                List.of());

        assertEquals(2, contents.size());

        ReflectionTestUtils.invokeMethod(
                service,
                "removeBlockedContents",
                contents,
                List.of(first));

        assertEquals(List.of(second), contents);
        assertFalse(contents.contains(first));
        assertTrue(contents.contains(second));
    }

    private CachedContentVO content(
            Long id,
            String type,
            Double popularity) {

        CachedContentVO content = new CachedContentVO();
        content.setTmdbId(id);
        content.setContentType(type);
        content.setPopularity(popularity);
        return content;
    }
}
