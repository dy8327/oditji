package com.project.oditji.review.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.project.oditji.common.util.DateTimeUtil;
import com.project.oditji.review.dao.ReviewDAO;
import com.project.oditji.review.vo.MyReviewVO;
import com.project.oditji.review.vo.ProductReviewVO;
import com.project.oditji.review.vo.ReviewVO;

/**
 * 콘텐츠·상품 리뷰의 작성, 수정, 삭제, 목록 결합 로직을 검증합니다.
 */
@ExtendWith(MockitoExtension.class)
class ReviewServiceImplTest {

    @Mock
    private ReviewDAO reviewDAO;

    private ReviewServiceImpl reviewService;

    @BeforeEach
    void setUp() {
        reviewService = new ReviewServiceImpl(reviewDAO);
    }

    @Test
    void getMyReviewListShouldMergeAndSortNewestFirst() {

        MyReviewVO oldReview = createMyReview(1, 1_000L);
        MyReviewVO newReview = createMyReview(2, 2_000L);

        when(reviewDAO.selectMyContentReviewList(1L))
                .thenReturn(List.of(oldReview));
        when(reviewDAO.selectMyProductReviewList(1L))
                .thenReturn(List.of(newReview));

        List<MyReviewVO> result = reviewService.getMyReviewList(1L);

        assertEquals(List.of(newReview, oldReview), result);
    }

    @Test
    void getMyReviewCountShouldAddContentAndProductCounts() {

        when(reviewDAO.countMyContentReview(1L)).thenReturn(2);
        when(reviewDAO.countMyProductReview(1L)).thenReturn(3);

        assertEquals(5, reviewService.getMyReviewCount(1L));
    }

    @Test
    void writeContentReviewShouldInsertNewReview() {

        reviewService.writeContentReview(
                1L,
                10,
                4.5,
                "결말이 인상적이었습니다.",
                "Y");

        ArgumentCaptor<ReviewVO> captor =
                ArgumentCaptor.forClass(ReviewVO.class);
        verify(reviewDAO).insertContentReview(captor.capture());

        ReviewVO saved = captor.getValue();
        assertEquals(1L, saved.getMemberNo());
        assertEquals(10, saved.getContentNo());
        assertEquals(4.5, saved.getRating());
        assertEquals("결말이 인상적이었습니다.", saved.getReviewText());
        assertEquals("Y", saved.getSpoilerYn());
    }

    @Test
    void writeContentReviewShouldReactivateDeletedReview() {

        ReviewVO existing = new ReviewVO();
        existing.setReviewNo(99L);
        existing.setStatus("DELETED");
        when(reviewDAO.selectContentReviewByMemberAndContentAnyStatus(any()))
                .thenReturn(existing);

        reviewService.writeContentReview(
                1L,
                10,
                4.0,
                "다시 작성한 리뷰입니다.",
                "N");

        ArgumentCaptor<ReviewVO> captor =
                ArgumentCaptor.forClass(ReviewVO.class);
        verify(reviewDAO).reactivateContentReview(captor.capture());
        verify(reviewDAO, never()).insertContentReview(any());

        assertEquals(99L, captor.getValue().getReviewNo());
        assertEquals(4.0, captor.getValue().getRating());
        assertEquals("N", captor.getValue().getSpoilerYn());
    }

    @Test
    void writeContentReviewShouldRejectExistingActiveReview() {

        ReviewVO existing = new ReviewVO();
        existing.setStatus("ACTIVE");
        when(reviewDAO.selectContentReviewByMemberAndContentAnyStatus(any()))
                .thenReturn(existing);

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> reviewService.writeContentReview(
                        1L,
                        10,
                        4.0,
                        "이미 작성한 리뷰입니다.",
                        "N"));

        assertEquals("이미 작성한 리뷰가 있습니다.", exception.getMessage());
    }

    @Test
    void writeContentReviewShouldRejectInvalidRating() {

        assertThrows(
                IllegalArgumentException.class,
                () -> reviewService.writeContentReview(
                        1L,
                        10,
                        6.0,
                        "리뷰",
                        "N"));

        verify(reviewDAO, never())
                .selectContentReviewByMemberAndContentAnyStatus(any());
    }

    @Test
    void updateContentReviewShouldUpdateOwnedReview() {

        ReviewVO existing = new ReviewVO();
        existing.setMemberNo(1L);
        when(reviewDAO.selectContentReviewByReviewNo(50L)).thenReturn(existing);

        reviewService.updateContentReview(
                1L,
                50L,
                10,
                3.5,
                "수정된 리뷰입니다.",
                "Y");

        ArgumentCaptor<ReviewVO> captor =
                ArgumentCaptor.forClass(ReviewVO.class);
        verify(reviewDAO).updateContentReview(captor.capture());

        assertEquals(50L, captor.getValue().getReviewNo());
        assertEquals(3.5, captor.getValue().getRating());
        assertEquals("Y", captor.getValue().getSpoilerYn());
    }

    @Test
    void updateContentReviewShouldRejectOtherMembersReview() {

        ReviewVO existing = new ReviewVO();
        existing.setMemberNo(2L);
        when(reviewDAO.selectContentReviewByReviewNo(50L)).thenReturn(existing);

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> reviewService.updateContentReview(
                        1L,
                        50L,
                        10,
                        3.5,
                        "수정 시도",
                        "N"));

        assertEquals("본인이 작성한 리뷰만 수정할 수 있습니다.", exception.getMessage());
    }

    @Test
    void writeProductReviewShouldInsertWhenPurchasedAndNotReviewed() {

        when(reviewDAO.countMyOrderItem(any())).thenReturn(1);
        when(reviewDAO.selectProductReviewByOrderItem(30)).thenReturn(null);

        reviewService.writeProductReview(
                1L,
                20,
                30,
                5.0,
                "좋은 상품입니다.");

        ArgumentCaptor<ProductReviewVO> captor =
                ArgumentCaptor.forClass(ProductReviewVO.class);
        verify(reviewDAO).insertProductReview(captor.capture());

        ProductReviewVO saved = captor.getValue();
        assertEquals(20, saved.getProductNo());
        assertEquals(30, saved.getOrderItemNo());
        assertEquals(1L, saved.getMemberNo());
        assertEquals(5.0, saved.getRating());
    }

    @Test
    void writeProductReviewShouldRejectNonPurchasedProduct() {

        when(reviewDAO.countMyOrderItem(any())).thenReturn(0);

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> reviewService.writeProductReview(
                        1L,
                        20,
                        30,
                        5.0,
                        "좋은 상품입니다."));

        assertEquals("구매한 상품만 리뷰를 작성할 수 있습니다.", exception.getMessage());
    }

    @Test
    void getReportedReviewSetShouldConvertListToSet() {

        when(reviewDAO.selectReportedContentReviewNoList(1L))
                .thenReturn(List.of(10, 10, 20));

        assertEquals(Set.of(10, 20), reviewService.getReportedReviewSet(1L));
    }

    @Test
    void deleteContentReviewShouldDeleteOwnedReview() {

        ReviewVO existing = new ReviewVO();
        existing.setMemberNo(1L);
        when(reviewDAO.selectContentReviewByReviewNo(50L)).thenReturn(existing);

        reviewService.deleteContentReview(1L, 50L);

        verify(reviewDAO).deleteContentReview(50L);
    }

    @Test
    void getProductReviewListShouldSkipDaoForInvalidProductNumber() {

        assertTrue(reviewService.getProductReviewList(0).isEmpty());
        verify(reviewDAO, never())
                .selectProductReviewListByProductNo(
                        org.mockito.ArgumentMatchers.anyInt());
    }

    private MyReviewVO createMyReview(int reviewNo, long createdAt) {

        MyReviewVO review = new MyReviewVO();
        review.setReviewNo(reviewNo);
        review.setCreatedAt(LocalDateTime.ofInstant(
                Instant.ofEpochMilli(createdAt),
                DateTimeUtil.KOREA_ZONE));
        return review;
    }
}
