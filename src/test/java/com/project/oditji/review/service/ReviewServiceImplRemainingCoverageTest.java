package com.project.oditji.review.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.project.oditji.review.dao.ReviewDAO;
import com.project.oditji.review.vo.ContentSpoilerSourceVO;
import com.project.oditji.review.vo.MyReviewVO;
import com.project.oditji.review.vo.ProductReviewVO;
import com.project.oditji.review.vo.ReviewVO;

/**
 * ReviewServiceImpl의 기존 테스트에서 남은 검증/스포일러 판별 분기를 보완합니다.
 */
@ExtendWith(MockitoExtension.class)
class ReviewServiceImplRemainingCoverageTest {

    @Mock
    private ReviewDAO reviewDAO;

    private ReviewServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new ReviewServiceImpl(reviewDAO);
    }

    @Test
    void myReviewListShouldHandleBothDaoListsBeingNull() {
        when(reviewDAO.selectMyContentReviewList(1L)).thenReturn(null);
        when(reviewDAO.selectMyProductReviewList(1L)).thenReturn(null);

        List<MyReviewVO> result = service.getMyReviewList(1L);

        assertTrue(result.isEmpty());
    }

    @Test
    void writeContentReviewShouldCoverRemainingValidationBranches() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.writeContentReview(null, 10, 4.0, "리뷰", "N"));

        assertThrows(
                IllegalArgumentException.class,
                () -> service.writeContentReview(1L, 10, -0.1, "리뷰", "N"));

        assertThrows(
                IllegalArgumentException.class,
                () -> service.writeContentReview(1L, 10, 4.0, null, "N"));

        assertThrows(
                IllegalArgumentException.class,
                () -> service.writeContentReview(1L, 10, 4.0, "   ", "N"));

        verify(reviewDAO, never())
                .selectContentReviewByMemberAndContentAnyStatus(any());
    }

    @Test
    void updateContentReviewShouldCoverRemainingValidationAndMissingReviewBranches() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.updateContentReview(null, 10L, 20, 4.0, "리뷰", "N"));

        assertThrows(
                IllegalArgumentException.class,
                () -> service.updateContentReview(1L, 10L, 20, -0.1, "리뷰", "N"));

        assertThrows(
                IllegalArgumentException.class,
                () -> service.updateContentReview(1L, 10L, 20, 5.1, "리뷰", "N"));

        assertThrows(
                IllegalArgumentException.class,
                () -> service.updateContentReview(1L, 10L, 20, 4.0, null, "N"));

        assertThrows(
                IllegalArgumentException.class,
                () -> service.updateContentReview(1L, 10L, 20, 4.0, "   ", "N"));

        assertThrows(
                IllegalArgumentException.class,
                () -> service.updateContentReview(1L, 99L, 20, 4.0, "리뷰", "N"));

        verify(reviewDAO).selectContentReviewByReviewNo(99L);
    }

    @Test
    void writeProductReviewShouldCoverRemainingValidationBranches() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.writeProductReview(null, 20, 30, 5.0, "상품 리뷰"));

        assertThrows(
                IllegalArgumentException.class,
                () -> service.writeProductReview(1L, 20, 30, 0.5, "상품 리뷰"));

        assertThrows(
                IllegalArgumentException.class,
                () -> service.writeProductReview(1L, 20, 30, 5.5, "상품 리뷰"));

        assertThrows(
                IllegalArgumentException.class,
                () -> service.writeProductReview(1L, 20, 30, 5.0, null));

        assertThrows(
                IllegalArgumentException.class,
                () -> service.writeProductReview(1L, 20, 30, 5.0, "   "));

        verify(reviewDAO, never()).countMyOrderItem(any());
    }

    @Test
    void writeProductReviewShouldRejectExistingReviewAfterPurchaseCheck() {
        ProductReviewVO existing = new ProductReviewVO();

        when(reviewDAO.countMyOrderItem(any())).thenReturn(1);
        when(reviewDAO.selectProductReviewByOrderItem(30)).thenReturn(existing);

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> service.writeProductReview(1L, 20, 30, 4.0, "이미 작성한 상품 리뷰"));

        assertEquals(
                "해당 상품에 대한 리뷰가 이미 존재합니다.",
                exception.getMessage());
        verify(reviewDAO, never()).insertProductReview(any());
    }

    @Test
    void spoilerDetectionShouldCoverNullOverviewAndSingleMatchResult() {
        ContentSpoilerSourceVO nullOverview = new ContentSpoilerSourceVO();
        nullOverview.setOverview(null);
        when(reviewDAO.selectContentSpoilerSource(201)).thenReturn(nullOverview);

        service.writeContentReview(
                1L,
                201,
                4.0,
                "영상미가 좋았습니다.",
                "N");

        ContentSpoilerSourceVO oneMatch = new ContentSpoilerSourceVO();
        oneMatch.setOverview("탐정이 사건 현장에서 단서를 추적한다.");
        when(reviewDAO.selectContentSpoilerSource(202)).thenReturn(oneMatch);

        service.writeContentReview(
                2L,
                202,
                4.0,
                "탐정이 등장해서 흥미로웠다.",
                "N");

        ArgumentCaptor<ReviewVO> captor =
                ArgumentCaptor.forClass(ReviewVO.class);
        verify(reviewDAO, times(2))
                .insertContentReview(captor.capture());

        assertEquals("N", captor.getAllValues().get(0).getSpoilerYn());
        assertEquals("N", captor.getAllValues().get(1).getSpoilerYn());
    }

    @Test
    void spoilerOverviewKeywordLoopShouldCoverDuplicateWordGuard() {
        ContentSpoilerSourceVO source = new ContentSpoilerSourceVO();
        source.setOverview("비밀단서 비밀단서 추적과정");

        when(reviewDAO.selectContentSpoilerSource(203)).thenReturn(source);

        service.writeContentReview(
                3L,
                203,
                4.0,
                "비밀단서가 인상적이었다.",
                "N");

        ArgumentCaptor<ReviewVO> captor =
                ArgumentCaptor.forClass(ReviewVO.class);
        verify(reviewDAO).insertContentReview(captor.capture());

        assertEquals("N", captor.getValue().getSpoilerYn());
    }

    @Test
    void privateSpoilerHelpersShouldCoverUnreachableNullAndBlankInputs() {
        String nullReviewResult = ReflectionTestUtils.invokeMethod(
                service,
                "determineSpoilerYn",
                "N",
                (String) null,
                300);

        String blankReviewResult = ReflectionTestUtils.invokeMethod(
                service,
                "determineSpoilerYn",
                "N",
                "   ",
                300);

        String normalizedNull = ReflectionTestUtils.invokeMethod(
                service,
                "normalizeSpoilerText",
                (Object) null);

        assertEquals("N", nullReviewResult);
        assertEquals("N", blankReviewResult);
        assertEquals("", normalizedNull);
    }
}
