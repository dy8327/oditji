package com.project.oditji.review.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.project.oditji.review.dao.ReviewDAO;
import com.project.oditji.review.vo.ContentSpoilerSourceVO;

/** 스포일러 판별 helper의 마지막 short-circuit 조건을 직접 보완합니다. */
class ReviewServiceImplResidualConditionClosure2Test {

    private ReviewDAO reviewDAO;
    private ReviewServiceImpl service;

    @BeforeEach
    void setUp() {
        reviewDAO = mock(ReviewDAO.class);
        service = new ReviewServiceImpl(reviewDAO);
    }

    @Test
    void spoilerDetectionShouldReturnNoWhenSourceItselfIsMissing() {
        when(reviewDAO.selectContentSpoilerSource(401)).thenReturn(null);

        String result = ReflectionTestUtils.invokeMethod(
                service,
                "determineSpoilerYn",
                "N",
                "영상미와 음악이 좋았습니다.",
                401);

        assertEquals("N", result);
    }

    @Test
    void overviewKeywordLoopShouldEvaluateShortWordStopWordAndNormalWordOperands() {
        Integer matched = ReflectionTestUtils.invokeMethod(
                service,
                "countOverviewKeywordMatches",
                "ab 그리고 비밀단서 비밀단서 추적과정",
                "비밀단서가 인상적이었습니다.");

        assertEquals(1, matched);
    }

    @Test
    void spoilerDetectionShouldEvaluateBlankOverviewAfterNonNullSource() {
        ContentSpoilerSourceVO source = new ContentSpoilerSourceVO();
        source.setOverview("   ");
        when(reviewDAO.selectContentSpoilerSource(402)).thenReturn(source);

        String result = ReflectionTestUtils.invokeMethod(
                service,
                "determineSpoilerYn",
                "N",
                "평범한 감상입니다.",
                402);

        assertEquals("N", result);
    }
}
