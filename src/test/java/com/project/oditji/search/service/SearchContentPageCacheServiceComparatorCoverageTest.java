package com.project.oditji.search.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.project.oditji.search.vo.SearchResultVO;
import com.project.oditji.tmdb.dao.TmdbDAO;

/**
 * 콘텐츠 목록 정렬 switch의 rating/latest/title/default 경로와 null 보조값을 검증합니다.
 */
class SearchContentPageCacheServiceComparatorCoverageTest {

    private SearchContentPageCacheService service;

    @BeforeEach
    void setUp() {
        service = new SearchContentPageCacheService(
                mock(SearchContentStore.class),
                mock(TmdbDAO.class));
    }

    @Test
    void ratingSortShouldUsePopularityAsTieBreakerAndKeepNullScoreLast() {
        SearchResultVO lowPopularity =
                result(
                        "B",
                        "2026-08-01",
                        8.0,
                        10.0);

        SearchResultVO highPopularity =
                result(
                        "A",
                        "2026-08-01",
                        8.0,
                        20.0);

        SearchResultVO nullScore =
                result(
                        "C",
                        "2026-08-01",
                        null,
                        100.0);

        List<SearchResultVO> values =
                new ArrayList<SearchResultVO>(
                        List.of(
                                nullScore,
                                lowPopularity,
                                highPopularity));

        sort(values, "all", "rating");

        assertSame(highPopularity, values.get(0));
        assertSame(lowPopularity, values.get(1));
        assertSame(nullScore, values.get(2));
    }

    @Test
    void latestSortShouldUsePopularityThenScoreAndPutNullDateLast() {
        SearchResultVO lowerScore =
                result(
                        "A",
                        "2026-08-10",
                        7.0,
                        50.0);

        SearchResultVO higherScore =
                result(
                        "B",
                        "2026-08-10",
                        8.0,
                        50.0);

        SearchResultVO older =
                result(
                        "C",
                        "2026-08-09",
                        10.0,
                        100.0);

        SearchResultVO nullDate =
                result(
                        "D",
                        null,
                        10.0,
                        100.0);

        List<SearchResultVO> values =
                new ArrayList<SearchResultVO>(
                        List.of(
                                nullDate,
                                older,
                                lowerScore,
                                higherScore));

        sort(values, "new", "latest");

        assertSame(higherScore, values.get(0));
        assertSame(lowerScore, values.get(1));
        assertSame(older, values.get(2));
        assertSame(nullDate, values.get(3));
    }

    @Test
    void titleSortShouldIgnoreCaseAndUsePopularityAsTieBreaker() {
        SearchResultVO lower =
                result(
                        "alpha",
                        null,
                        1.0,
                        10.0);

        SearchResultVO higher =
                result(
                        "ALPHA",
                        null,
                        1.0,
                        20.0);

        List<SearchResultVO> values =
                new ArrayList<SearchResultVO>(
                        List.of(
                                lower,
                                higher));

        sort(values, "all", "title");

        assertSame(higher, values.get(0));
        assertSame(lower, values.get(1));
    }

    @Test
    void defaultPopularSortShouldUseScoreTieBreakerAndPlaceNullPopularityLast() {
        SearchResultVO lowerScore =
                result(
                        "A",
                        null,
                        7.0,
                        30.0);

        SearchResultVO higherScore =
                result(
                        "B",
                        null,
                        9.0,
                        30.0);

        SearchResultVO nullPopularity =
                result(
                        "C",
                        null,
                        10.0,
                        null);

        List<SearchResultVO> values =
                new ArrayList<SearchResultVO>(
                        List.of(
                                nullPopularity,
                                lowerScore,
                                higherScore));

        sort(values, "all", "invalid-sort");

        assertSame(higherScore, values.get(0));
        assertSame(lowerScore, values.get(1));
        assertSame(nullPopularity, values.get(2));
        assertEquals(3, values.size());
    }

    private void sort(
            List<SearchResultVO> values,
            String type,
            String sort) {

        ReflectionTestUtils.invokeMethod(
                service,
                "sortContentList",
                values,
                type,
                sort);
    }

    private SearchResultVO result(
            String title,
            String releaseDate,
            Double score,
            Double popularity) {

        SearchResultVO result =
                new SearchResultVO();
        result.setTitle(title);
        result.setReleaseDate(releaseDate);
        result.setTmdbScore(score);
        result.setPopularity(popularity);
        return result;
    }
}
