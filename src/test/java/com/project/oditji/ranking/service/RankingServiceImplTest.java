package com.project.oditji.ranking.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.project.oditji.search.service.SearchContentPageCacheService;
import com.project.oditji.search.vo.SearchResultVO;

/** 인기도와 평점 가중치 기반 랭킹의 필터·정렬·플랫폼 분기를 검증합니다. */
@ExtendWith(MockitoExtension.class)
class RankingServiceImplTest {

    @Mock
    private SearchContentPageCacheService pageCacheService;

    private RankingServiceImpl rankingService;

    @BeforeEach
    void setUp() {
        rankingService = new RankingServiceImpl(pageCacheService);
    }

    @Test
    void overallRankingShouldFilterLowRatingsAndSortByWeightedScore() {
        SearchResultVO highPopularity = content("인기", 6.0, 1000.0);
        SearchResultVO highRating = content("평점", 10.0, 10.0);
        SearchResultVO lowRating = content("제외", 5.9, 10000.0);
        SearchResultVO nullNumbers = content("null", null, null);

        when(pageCacheService.getMainRecommendedContent(
                List.of(),
                50000)).thenReturn(List.of(
                        lowRating,
                        highRating,
                        nullNumbers,
                        highPopularity));

        List<SearchResultVO> result =
                rankingService.getOverallPopularRanking(2);

        assertEquals(2, result.size());
        assertSame(highPopularity, result.get(0));
        assertSame(highRating, result.get(1));
    }

    @Test
    void overallRankingShouldUseDefaultAndMaximumLimits() {
        List<SearchResultVO> many = java.util.stream.IntStream.range(0, 120)
                .mapToObj(index -> content(
                        "콘텐츠" + index,
                        7.0,
                        (double) index + 1.0))
                .toList();
        when(pageCacheService.getMainRecommendedContent(anyList(), eq(50000)))
                .thenReturn(many);

        assertEquals(10,
                rankingService.getOverallPopularRanking(0).size());
        assertEquals(100,
                rankingService.getOverallPopularRanking(1000).size());
    }

    @Test
    void rankingShouldReturnEmptyForNullEmptyOrFilteredCandidates() {
        when(pageCacheService.getMainRecommendedContent(anyList(), eq(50000)))
                .thenReturn(null)
                .thenReturn(List.of())
                .thenReturn(Arrays.asList(
                        content("낮음", 4.0, 100.0),
                        null));

        assertTrue(rankingService.getOverallPopularRanking(10).isEmpty());
        assertTrue(rankingService.getOverallPopularRanking(10).isEmpty());
        assertTrue(rankingService.getOverallPopularRanking(10).isEmpty());
    }

    @Test
    void platformRankingShouldNormalizeSupportedNamesAndRejectUnknown() {
        SearchResultVO content = content("넷플릭스", 8.0, 100.0);
        when(pageCacheService.getMainRecommendedContent(
                List.of("Netflix"),
                50000)).thenReturn(List.of(content));

        List<SearchResultVO> result =
                rankingService.getPlatformPopularRanking("넷플릭스", 1);

        assertEquals(List.of(content), result);
        verify(pageCacheService).getMainRecommendedContent(
                List.of("Netflix"),
                50000);

        assertTrue(rankingService.getPlatformPopularRanking(
                "unknown",
                10).isEmpty());
    }

    @Test
    void allPlatformRankingsShouldKeepConfiguredOrder() {
        when(pageCacheService.getMainRecommendedContent(anyList(), eq(50000)))
                .thenReturn(List.of());

        Map<String, List<SearchResultVO>> result =
                rankingService.getAllPlatformPopularRankings(-1);

        assertEquals(
                List.of(
                        "Netflix",
                        "TVING",
                        "wavve",
                        "Disney Plus",
                        "Watcha",
                        "Coupangplay"),
                result.keySet().stream().toList());
        assertTrue(result.values().stream().allMatch(List::isEmpty));
    }

    @Test
    void rankingShouldHandleZeroAndNegativePopularityAndTieBreakers() {
        SearchResultVO nullPopularity = content("null", 8.0, null);
        SearchResultVO negativePopularity = content("negative", 8.0, -10.0);
        SearchResultVO positivePopularity = content("positive", 8.0, 1.0);
        SearchResultVO sameScoreHigherRating = content("rating", 9.0, 0.0);

        when(pageCacheService.getMainRecommendedContent(anyList(), eq(50000)))
                .thenReturn(List.of(
                        nullPopularity,
                        negativePopularity,
                        positivePopularity,
                        sameScoreHigherRating));

        List<SearchResultVO> result =
                rankingService.getOverallPopularRanking(10);

        assertEquals(4, result.size());
        assertSame(positivePopularity, result.get(0));
        assertTrue(result.indexOf(sameScoreHigherRating)
                < result.indexOf(nullPopularity));
    }

    private SearchResultVO content(
            String title,
            Double score,
            Double popularity) {

        SearchResultVO content = new SearchResultVO();
        content.setTitle(title);
        content.setTmdbScore(score);
        content.setPopularity(popularity);
        return content;
    }
}
