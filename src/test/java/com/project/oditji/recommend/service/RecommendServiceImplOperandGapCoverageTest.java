package com.project.oditji.recommend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.project.oditji.recommend.dao.RecommendDAO;
import com.project.oditji.recommend.vo.RecommendOttResultVO;
import com.project.oditji.recommend.vo.RecommendOttScoreVO;

/** OTT 추천의 마지막 OR/AND 및 빈 플랫폼 번호 조건을 보완합니다. */
class RecommendServiceImplOperandGapCoverageTest {

    private RecommendDAO recommendDAO;
    private RecommendServiceImpl service;

    @BeforeEach
    void setUp() {
        recommendDAO = mock(RecommendDAO.class);
        service = new RecommendServiceImpl(recommendDAO);
    }

    @Test
    void platformNameHelperShouldCoverNullNameOperandAndEmptyReasonList() {
        RecommendOttScoreVO nullName = score(1, null, 0, 0, 0, 0, 0);

        assertEquals(
                "추천 OTT",
                ReflectionTestUtils.invokeMethod(
                        service,
                        "createRecommendedPlatformNameText",
                        List.of(nullName)));

        List<String> reasons = ReflectionTestUtils.invokeMethod(
                service,
                "createRecommendationReasons",
                Collections.emptyList(),
                5,
                0,
                0,
                false);

        assertTrue(reasons.isEmpty());
    }

    @Test
    void recommendationShouldShortCircuitJointCheckWhenSecondScoreIsTooLow() {
        RecommendOttScoreVO first = score(1, "Netflix", 25, 20, 5, 6, 1);
        RecommendOttScoreVO second = score(2, "TVING", 14, 12, 2, 5, 0);

        when(recommendDAO.countDistinctInterestContent(90L)).thenReturn(8);
        when(recommendDAO.selectOttInterestScoreList(90L)).thenReturn(
                new ArrayList<RecommendOttScoreVO>(List.of(first, second)));

        RecommendOttResultVO result = service.getOttRecommendation(90L);

        assertTrue(result.isRecommendationAvailable());
        assertFalse(result.isJointRecommendation());
        assertEquals(1, result.getRecommendedPlatformList().size());
    }

    @Test
    void recommendationShouldSkipUnionCountWhenRecommendedPlatformNumberIsNull() {
        RecommendOttScoreVO first = score(null, "Netflix", 25, 20, 5, 6, 1);

        when(recommendDAO.countDistinctInterestContent(91L)).thenReturn(8);
        when(recommendDAO.selectOttInterestScoreList(91L)).thenReturn(
                new ArrayList<RecommendOttScoreVO>(List.of(first)));

        RecommendOttResultVO result = service.getOttRecommendation(91L);

        assertTrue(result.isRecommendationAvailable());
        verify(recommendDAO, never())
                .countDistinctInterestContentByPlatforms(eq(91L), anyList());
        verify(recommendDAO, never())
                .countDistinctFavoriteContentByPlatforms(eq(91L), anyList());
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
