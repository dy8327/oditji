package com.project.oditji.recommend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.project.oditji.recommend.dao.RecommendDAO;
import com.project.oditji.recommend.vo.RecommendOttContentVO;
import com.project.oditji.recommend.vo.RecommendOttResultVO;
import com.project.oditji.recommend.vo.RecommendOttScoreVO;

/** 개인화 OTT 추천의 최소 조건, 정렬, 단독·공동 추천과 이유 문구를 검증합니다. */
@ExtendWith(MockitoExtension.class)
class RecommendServiceImplTest {

    @Mock
    private RecommendDAO recommendDAO;

    private RecommendServiceImpl recommendService;

    @BeforeEach
    void setUp() {
        recommendService = new RecommendServiceImpl(recommendDAO);
    }

    @Test
    void recommendationShouldRequireValidLoggedInMember() {
        RecommendOttResultVO nullResult =
                recommendService.getOttRecommendation(null);
        RecommendOttResultVO zeroResult =
                recommendService.getOttRecommendation(0L);

        assertFalse(nullResult.isRecommendationAvailable());
        assertFalse(zeroResult.isRecommendationAvailable());
        assertTrue(nullResult.getStatusMessage().contains("로그인"));
        verify(recommendDAO, never()).countDistinctInterestContent(0L);
    }

    @Test
    void recommendationShouldStopWhenTotalInterestIsInsufficient() {
        when(recommendDAO.countDistinctInterestContent(1L)).thenReturn(4);
        when(recommendDAO.selectOttInterestScoreList(1L)).thenReturn(
                List.of(score(1, "Netflix", 100, 10, 10, 10, 5)));

        RecommendOttResultVO result =
                recommendService.getOttRecommendation(1L);

        assertFalse(result.isRecommendationAvailable());
        assertEquals(4, result.getTotalInterestContentCount());
        assertEquals(1, result.getRankedPlatformList().size());
        assertTrue(result.getStatusMessage().contains("관심 기록이 부족"));
    }

    @Test
    void recommendationShouldHandleNullAndEmptyScoreLists() {
        when(recommendDAO.countDistinctInterestContent(1L)).thenReturn(5);
        when(recommendDAO.selectOttInterestScoreList(1L))
                .thenReturn(null)
                .thenReturn(new ArrayList<RecommendOttScoreVO>());

        RecommendOttResultVO nullListResult =
                recommendService.getOttRecommendation(1L);
        RecommendOttResultVO emptyListResult =
                recommendService.getOttRecommendation(1L);

        assertFalse(nullListResult.isRecommendationAvailable());
        assertFalse(emptyListResult.isRecommendationAvailable());
        assertTrue(nullListResult.getRankedPlatformList().isEmpty());
        assertTrue(nullListResult.getStatusMessage().contains("OTT 정보"));
    }

    @Test
    void recommendationShouldRequireMinimumTopScoreAndContentCount() {
        when(recommendDAO.countDistinctInterestContent(1L)).thenReturn(10);
        when(recommendDAO.selectOttInterestScoreList(1L))
                .thenReturn(List.of(
                        score(1, "Netflix", 14, 10, 4, 5, 2)))
                .thenReturn(List.of(
                        score(1, "Netflix", 20, 15, 5, 3, 2)));

        RecommendOttResultVO lowScore =
                recommendService.getOttRecommendation(1L);
        RecommendOttResultVO lowContent =
                recommendService.getOttRecommendation(1L);

        assertFalse(lowScore.isRecommendationAvailable());
        assertTrue(lowScore.getStatusMessage().contains("관심도가 충분"));
        assertFalse(lowContent.isRecommendationAvailable());
        assertTrue(lowContent.getStatusMessage().contains("종류가 조금 더"));
    }

    @Test
    void recommendationShouldSortScoresAndCreateStandaloneRecommendation() {
        RecommendOttScoreVO lower =
                score(2, "TVING", 18, 12, 6, 5, 1);
        RecommendOttScoreVO first =
                score(1, "Netflix", 25, 20, 5, 6, 2);
        List<RecommendOttContentVO> favorites =
                List.of(new RecommendOttContentVO());
        List<RecommendOttContentVO> viewed =
                List.of(new RecommendOttContentVO());

        when(recommendDAO.countDistinctInterestContent(1L)).thenReturn(8);
        when(recommendDAO.selectOttInterestScoreList(1L)).thenReturn(
                new ArrayList<>(List.of(lower, first)));
        when(recommendDAO.selectFavoriteContentList(1L, 1))
                .thenReturn(favorites);
        when(recommendDAO.selectRecentViewedContentList(1L, 1))
                .thenReturn(viewed);
        when(recommendDAO.countDistinctInterestContentByPlatforms(
                1L,
                List.of(1))).thenReturn(6);
        when(recommendDAO.countDistinctFavoriteContentByPlatforms(
                1L,
                List.of(1))).thenReturn(2);

        RecommendOttResultVO result =
                recommendService.getOttRecommendation(1L);

        assertTrue(result.isRecommendationAvailable());
        assertFalse(result.isJointRecommendation());
        assertSame(first, result.getRankedPlatformList().get(0));
        assertEquals(List.of(first), result.getRecommendedPlatformList());
        assertSame(favorites, first.getFavoriteContentList());
        assertSame(viewed, first.getViewedContentList());
        assertTrue(result.getStatusMessage().startsWith("Netflix"));
        assertTrue(result.getRecommendationReasons().stream()
                .anyMatch(reason -> reason.contains("8개 중 6개")));
        assertTrue(result.getRecommendationReasons().stream()
                .anyMatch(reason -> reason.contains("찜한 콘텐츠 2개")));
        assertTrue(result.getRecommendationReasons().stream()
                .anyMatch(reason -> reason.contains("조회 기록")));
    }

    @Test
    void recommendationShouldCreateJointRecommendationAndCombinedReasons() {
        RecommendOttScoreVO netflix =
                score(1, "Netflix", 25, 10, 15, 6, 3);
        RecommendOttScoreVO tving =
                score(2, "TVING", 22, 8, 14, 5, 2);
        RecommendOttScoreVO third =
                score(3, "Watcha", 20, 18, 2, 7, 1);

        when(recommendDAO.countDistinctInterestContent(1L)).thenReturn(12);
        when(recommendDAO.selectOttInterestScoreList(1L)).thenReturn(
                new ArrayList<>(List.of(third, tving, netflix)));
        when(recommendDAO.selectFavoriteContentList(
                org.mockito.ArgumentMatchers.eq(1L),
                org.mockito.ArgumentMatchers.anyInt()))
                .thenReturn(null);
        when(recommendDAO.selectRecentViewedContentList(
                org.mockito.ArgumentMatchers.eq(1L),
                org.mockito.ArgumentMatchers.anyInt()))
                .thenReturn(null);
        when(recommendDAO.countDistinctInterestContentByPlatforms(
                1L,
                List.of(1, 2))).thenReturn(9);
        when(recommendDAO.countDistinctFavoriteContentByPlatforms(
                1L,
                List.of(1, 2))).thenReturn(4);

        RecommendOttResultVO result =
                recommendService.getOttRecommendation(1L);

        assertTrue(result.isRecommendationAvailable());
        assertTrue(result.isJointRecommendation());
        assertEquals(List.of(netflix, tving),
                result.getRecommendedPlatformList());
        assertTrue(result.getStatusMessage().contains("Netflix와 TVING"));
        assertTrue(netflix.getFavoriteContentList().isEmpty());
        assertTrue(tving.getViewedContentList().isEmpty());
        assertTrue(result.getRecommendationReasons().stream()
                .anyMatch(reason -> reason.contains("Netflix와 TVING")));
        assertTrue(result.getRecommendationReasons().stream()
                .anyMatch(reason -> reason.contains("찜한 콘텐츠 4개")));
        assertTrue(result.getRecommendationReasons().stream()
                .anyMatch(reason -> reason.contains("현재 찜한 콘텐츠")));
        assertTrue(result.getRecommendationReasons().stream()
                .anyMatch(reason -> reason.contains("상위 두 OTT")));
    }

    @Test
    void recommendationShouldNotJoinWhenGapOrSecondScoreFailsCondition() {
        RecommendOttScoreVO first =
                score(1, "Netflix", 25, 20, 5, 6, 1);
        RecommendOttScoreVO gapFive =
                score(2, "TVING", 20, 15, 5, 5, 1);

        when(recommendDAO.countDistinctInterestContent(1L)).thenReturn(8);
        when(recommendDAO.selectOttInterestScoreList(1L)).thenReturn(
                new ArrayList<>(List.of(first, gapFive)));
        when(recommendDAO.countDistinctInterestContentByPlatforms(
                1L,
                List.of(1))).thenReturn(6);
        when(recommendDAO.countDistinctFavoriteContentByPlatforms(
                1L,
                List.of(1))).thenReturn(0);

        RecommendOttResultVO result =
                recommendService.getOttRecommendation(1L);

        assertFalse(result.isJointRecommendation());
        assertEquals(1, result.getRecommendedPlatformList().size());
        assertFalse(result.getRecommendationReasons().stream()
                .anyMatch(reason -> reason.contains("찜한 콘텐츠")));
    }

    @Test
    void scoreSortingShouldUseContentAndFavoriteCountsAsTieBreakers() {
        RecommendOttScoreVO lowContent =
                score(1, "A", 20, 10, 10, 4, 5);
        RecommendOttScoreVO highContentLowFavorite =
                score(2, "B", 20, 10, 10, 5, 1);
        RecommendOttScoreVO highContentHighFavorite =
                score(3, "C", 20, 10, 10, 5, 3);

        when(recommendDAO.countDistinctInterestContent(1L)).thenReturn(4);
        when(recommendDAO.selectOttInterestScoreList(1L)).thenReturn(
                new ArrayList<>(List.of(
                        lowContent,
                        highContentLowFavorite,
                        highContentHighFavorite)));

        RecommendOttResultVO result =
                recommendService.getOttRecommendation(1L);

        assertEquals(
                List.of(
                        highContentHighFavorite,
                        highContentLowFavorite,
                        lowContent),
                result.getRankedPlatformList());
    }

    private RecommendOttScoreVO score(
            Integer platformNo,
            String platformName,
            int totalScore,
            int viewScore,
            int favoriteScore,
            int interestCount,
            int favoriteCount) {

        RecommendOttScoreVO score = new RecommendOttScoreVO();
        score.setPlatformNo(platformNo);
        score.setPlatformName(platformName);
        score.setTotalScore(totalScore);
        score.setViewScore(viewScore);
        score.setFavoriteScore(favoriteScore);
        score.setInterestContentCount(interestCount);
        score.setFavoriteContentCount(favoriteCount);
        return score;
    }
}
