package com.project.oditji.recommend.vo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * 최종 OTT 추천 결과 VO의 getter/setter와 null 목록 방어 분기를 검증합니다.
 */
class RecommendOttResultVOCoverageTest {

    @Test
    void scalarPropertiesAndListsShouldRoundTrip() {
        RecommendOttResultVO result = new RecommendOttResultVO();

        result.setRecommendationAvailable(true);
        result.setJointRecommendation(true);
        result.setTotalInterestContentCount(8);
        result.setStatusMessage("추천 준비 완료");

        RecommendOttScoreVO score = new RecommendOttScoreVO();
        List<RecommendOttScoreVO> scores = List.of(score);
        List<String> reasons = List.of("최근 조회 기록");

        result.setRecommendedPlatformList(scores);
        result.setRankedPlatformList(scores);
        result.setRecommendationReasons(reasons);

        assertTrue(result.isRecommendationAvailable());
        assertTrue(result.isJointRecommendation());
        assertEquals(8, result.getTotalInterestContentCount());
        assertEquals("추천 준비 완료", result.getStatusMessage());
        assertSame(scores, result.getRecommendedPlatformList());
        assertSame(scores, result.getRankedPlatformList());
        assertSame(reasons, result.getRecommendationReasons());
    }

    @Test
    void nullListsShouldBeConvertedToEmptyLists() {
        RecommendOttResultVO result = new RecommendOttResultVO();

        result.setRecommendedPlatformList(null);
        result.setRankedPlatformList(null);
        result.setRecommendationReasons(null);

        assertTrue(result.getRecommendedPlatformList().isEmpty());
        assertTrue(result.getRankedPlatformList().isEmpty());
        assertTrue(result.getRecommendationReasons().isEmpty());
    }
}
