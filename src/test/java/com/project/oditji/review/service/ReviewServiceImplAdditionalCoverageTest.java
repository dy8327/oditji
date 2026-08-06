package com.project.oditji.review.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
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

import com.project.oditji.review.dao.ReviewDAO;
import com.project.oditji.review.vo.ContentReviewVO;
import com.project.oditji.review.vo.ContentSpoilerSourceVO;
import com.project.oditji.review.vo.ProductReviewVO;
import com.project.oditji.review.vo.ReviewVO;

/** 리뷰 조회·삭제와 자동 스포일러 판별의 남은 분기를 보완합니다. */
@ExtendWith(MockitoExtension.class)
class ReviewServiceImplAdditionalCoverageTest {

    @Mock
    private ReviewDAO reviewDAO;

    private ReviewServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new ReviewServiceImpl(reviewDAO);
    }

    @Test
    void contentReviewQueriesShouldReturnSafeValuesAndDelegate() {
        assertTrue(service.getContentReviewList(10).isEmpty());

        List<ContentReviewVO> reviews = List.of(new ContentReviewVO());
        when(reviewDAO.selectContentReviewListByContentNo(11)).thenReturn(reviews);
        when(reviewDAO.selectAvgRatingByContentNo(11)).thenReturn(4.25);
        when(reviewDAO.selectReviewCountByContentNo(11)).thenReturn(3);

        assertSame(reviews, service.getContentReviewList(11));
        assertEquals(4.25, service.getAvgRating(11));
        assertEquals(3, service.getReviewCount(11));
        assertNull(service.getMyReview(null, 11));

        ReviewVO mine = new ReviewVO();
        when(reviewDAO.selectContentReviewByMemberAndContent(any()))
                .thenReturn(mine);
        assertSame(mine, service.getMyReview(1L, 11));

        verify(reviewDAO).selectContentReviewByMemberAndContent(argThat(param ->
                Long.valueOf(1L).equals(param.get("memberNo"))
                        && Integer.valueOf(11).equals(param.get("contentNo"))));
    }

    @Test
    void reportedReviewSetShouldHandleGuestNullAndDuplicateRows() {
        assertTrue(service.getReportedReviewSet(null).isEmpty());

        when(reviewDAO.selectReportedContentReviewNoList(2L)).thenReturn(null);
        assertTrue(service.getReportedReviewSet(2L).isEmpty());

        when(reviewDAO.selectReportedContentReviewNoList(3L))
                .thenReturn(List.of(1, 1, 2));
        assertEquals(2, service.getReportedReviewSet(3L).size());
    }

    @Test
    void contentDeleteShouldRejectGuestMissingAndOtherOwner() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.deleteContentReview(null, 10L));
        verify(reviewDAO, never()).selectContentReviewByReviewNo(any());

        when(reviewDAO.selectContentReviewByReviewNo(10L)).thenReturn(null);
        assertThrows(
                IllegalArgumentException.class,
                () -> service.deleteContentReview(1L, 10L));

        ReviewVO other = new ReviewVO();
        other.setMemberNo(2L);
        when(reviewDAO.selectContentReviewByReviewNo(11L)).thenReturn(other);
        assertThrows(
                IllegalStateException.class,
                () -> service.deleteContentReview(1L, 11L));
    }

    @Test
    void productDeleteShouldValidateOwnershipAndDeleteOwnedReview() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.deleteProductReview(null, 20L));

        when(reviewDAO.selectProductReviewByReviewNo(20L)).thenReturn(null);
        assertThrows(
                IllegalArgumentException.class,
                () -> service.deleteProductReview(1L, 20L));

        ProductReviewVO other = new ProductReviewVO();
        other.setMemberNo(2L);
        when(reviewDAO.selectProductReviewByReviewNo(21L)).thenReturn(other);
        assertThrows(
                IllegalStateException.class,
                () -> service.deleteProductReview(1L, 21L));

        ProductReviewVO mine = new ProductReviewVO();
        mine.setMemberNo(1L);
        when(reviewDAO.selectProductReviewByReviewNo(22L)).thenReturn(mine);
        service.deleteProductReview(1L, 22L);
        verify(reviewDAO).deleteProductReview(22L);
    }

    @Test
    void productReviewQueriesShouldCoverInvalidNullAndNormalResults() {
        assertTrue(service.getProductReviewList(-1).isEmpty());
        assertNull(service.getProductAvgRating(0));
        assertEquals(0, service.getProductReviewCount(0));

        when(reviewDAO.selectProductReviewListByProductNo(10)).thenReturn(null);
        assertTrue(service.getProductReviewList(10).isEmpty());

        List<ProductReviewVO> reviews = List.of(new ProductReviewVO());
        when(reviewDAO.selectProductReviewListByProductNo(11)).thenReturn(reviews);
        when(reviewDAO.selectProductAvgRatingByProductNo(11)).thenReturn(4.5);
        when(reviewDAO.selectProductReviewCountByProductNo(11)).thenReturn(7);

        assertSame(reviews, service.getProductReviewList(11));
        assertEquals(4.5, service.getProductAvgRating(11));
        assertEquals(7, service.getProductReviewCount(11));
    }

    @Test
    void automaticSpoilerDetectionShouldCoverKeywordOverviewAndSafeText() {
        service.writeContentReview(
                1L,
                100,
                4.0,
                "마지막에 범인이 밝혀진다.",
                "N");

        ContentSpoilerSourceVO source = new ContentSpoilerSourceVO();
        source.setOverview("우주 비행사가 화성에서 생존을 위해 감자를 재배한다.");
        when(reviewDAO.selectContentSpoilerSource(101)).thenReturn(source);
        service.writeContentReview(
                2L,
                101,
                4.5,
                "화성에서 감자를 키우는 장면이 인상적이다.",
                "N");

        ContentSpoilerSourceVO emptySource = new ContentSpoilerSourceVO();
        emptySource.setOverview(" ");
        when(reviewDAO.selectContentSpoilerSource(102)).thenReturn(emptySource);
        service.writeContentReview(
                3L,
                102,
                3.5,
                "영상미가 좋았습니다.",
                "N");

        ArgumentCaptor<ReviewVO> captor = ArgumentCaptor.forClass(ReviewVO.class);
        verify(reviewDAO, times(3))
                .insertContentReview(captor.capture());
        assertEquals("Y", captor.getAllValues().get(0).getSpoilerYn());
        assertEquals("Y", captor.getAllValues().get(1).getSpoilerYn());
        assertEquals("N", captor.getAllValues().get(2).getSpoilerYn());
    }
}
