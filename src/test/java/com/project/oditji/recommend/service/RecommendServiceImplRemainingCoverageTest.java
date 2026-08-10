package com.project.oditji.recommend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

import com.project.oditji.recommend.dao.RecommendDAO;
import com.project.oditji.recommend.vo.RecommendOttScoreVO;

/**
 * OTT 추천 서비스의 private helper null 조건과 추천 이유 조합 분기를 보완합니다.
 */
class RecommendServiceImplRemainingCoverageTest {

    private RecommendServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new RecommendServiceImpl(
                Mockito.mock(RecommendDAO.class));
    }

    @Test
    void platformNumberExtractionShouldCoverNullEmptyNullEntryAndNullNumber() {
        List<Integer> nullResult =
                ReflectionTestUtils.invokeMethod(
                        service,
                        "extractPlatformNoList",
                        (Object) null);

        List<Integer> emptyResult =
                ReflectionTestUtils.invokeMethod(
                        service,
                        "extractPlatformNoList",
                        Collections.emptyList());

        RecommendOttScoreVO nullNumber =
                score(null, "No Number", 0, 0);

        RecommendOttScoreVO valid =
                score(7, "Netflix", 0, 0);

        List<RecommendOttScoreVO> platforms =
                new ArrayList<RecommendOttScoreVO>();
        platforms.add(null);
        platforms.add(nullNumber);
        platforms.add(valid);

        List<Integer> extracted =
                ReflectionTestUtils.invokeMethod(
                        service,
                        "extractPlatformNoList",
                        platforms);

        assertTrue(nullResult.isEmpty());
        assertTrue(emptyResult.isEmpty());
        assertEquals(List.of(7), extracted);
    }

    @Test
    void platformNameTextShouldCoverNullBlankAndMultipleValidNames() {
        assertEquals(
                "추천 OTT",
                ReflectionTestUtils.invokeMethod(
                        service,
                        "createRecommendedPlatformNameText",
                        (Object) null));

        assertEquals(
                "추천 OTT",
                ReflectionTestUtils.invokeMethod(
                        service,
                        "createRecommendedPlatformNameText",
                        Collections.emptyList()));

        RecommendOttScoreVO blank =
                score(1, "   ", 0, 0);
        RecommendOttScoreVO first =
                score(2, "Netflix", 0, 0);
        RecommendOttScoreVO second =
                score(3, "TVING", 0, 0);

        List<RecommendOttScoreVO> values =
                new ArrayList<RecommendOttScoreVO>();
        values.add(null);
        values.add(blank);
        values.add(first);
        values.add(second);

        assertEquals(
                "Netflix와 TVING",
                ReflectionTestUtils.invokeMethod(
                        service,
                        "createRecommendedPlatformNameText",
                        values));
    }

    @Test
    void recommendationReasonsShouldCoverEmptyListZeroScoresAndJointFlag() {
        List<String> emptyReasons =
                ReflectionTestUtils.invokeMethod(
                        service,
                        "createRecommendationReasons",
                        null,
                        5,
                        0,
                        0,
                        false);

        assertTrue(emptyReasons.isEmpty());

        RecommendOttScoreVO zero =
                score(1, null, 0, 0);

        List<String> zeroReasons =
                ReflectionTestUtils.invokeMethod(
                        service,
                        "createRecommendationReasons",
                        List.of(zero),
                        5,
                        0,
                        0,
                        false);

        assertEquals(1, zeroReasons.size());
        assertTrue(zeroReasons.get(0).startsWith("추천 OTT"));

        RecommendOttScoreVO favoriteHeavy =
                score(1, "Netflix", 1, 5);

        List<String> favoriteReasons =
                ReflectionTestUtils.invokeMethod(
                        service,
                        "createRecommendationReasons",
                        List.of(favoriteHeavy),
                        8,
                        6,
                        2,
                        true);

        assertTrue(favoriteReasons.stream()
                .anyMatch(reason ->
                        reason.contains("찜한 콘텐츠 2개")));
        assertTrue(favoriteReasons.stream()
                .anyMatch(reason ->
                        reason.contains("현재 찜한 콘텐츠")));
        assertTrue(favoriteReasons.stream()
                .anyMatch(reason ->
                        reason.contains("상위 두 OTT")));
    }

    @Test
    void recommendationReasonsShouldSkipNullPlatformsAndPreferViewReason() {
        RecommendOttScoreVO viewHeavy =
                score(1, "Netflix", 10, 1);

        List<RecommendOttScoreVO> platforms =
                new ArrayList<RecommendOttScoreVO>();
        platforms.add(null);
        platforms.add(viewHeavy);

        List<String> reasons =
                ReflectionTestUtils.invokeMethod(
                        service,
                        "createRecommendationReasons",
                        platforms,
                        10,
                        7,
                        0,
                        false);

        assertTrue(reasons.stream()
                .anyMatch(reason ->
                        reason.contains("조회 기록")));
    }

    private RecommendOttScoreVO score(
            Integer platformNo,
            String platformName,
            int viewScore,
            int favoriteScore) {

        RecommendOttScoreVO score =
                new RecommendOttScoreVO();

        score.setPlatformNo(platformNo);
        score.setPlatformName(platformName);
        score.setViewScore(viewScore);
        score.setFavoriteScore(favoriteScore);

        return score;
    }
}
