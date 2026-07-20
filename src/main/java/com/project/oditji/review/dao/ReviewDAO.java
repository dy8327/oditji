package com.project.oditji.review.dao;

import java.util.List;
import java.util.Map;

import com.project.oditji.review.vo.ContentReviewVO;
import com.project.oditji.review.vo.MyReviewVO;
import com.project.oditji.review.vo.ProductReviewVO;
import com.project.oditji.review.vo.ReviewVO;

public interface ReviewDAO {

    // 마이페이지 리뷰 조회
    List<MyReviewVO> selectMyContentReviewList(Long memberNo);

    List<MyReviewVO> selectMyProductReviewList(Long memberNo);

    int countMyContentReview(Long memberNo);

    int countMyProductReview(Long memberNo);


    // 콘텐츠 리뷰 작성 / 수정
    int insertContentReview(ReviewVO review);

    int updateContentReview(ReviewVO review);

    ReviewVO selectContentReviewByReviewNo(Long reviewNo);


    // 상품 리뷰 작성
    int insertProductReview(ProductReviewVO productReview);

    ProductReviewVO selectProductReviewByOrderItem(int orderItemNo);


    // 리뷰 작성 여부 확인
    ReviewVO selectContentReviewByMemberAndContent(
            Map<String, Object> param
    );

    ReviewVO selectContentReviewByMemberAndContentAnyStatus(
            Map<String, Object> param
    );


    // 콘텐츠 리뷰 목록 / 통계
    List<ContentReviewVO> selectContentReviewListByContentNo(int contentNo);

    Double selectAvgRatingByContentNo(int contentNo);

    int selectReviewCountByContentNo(int contentNo);


    // 신고된 콘텐츠 리뷰 조회
    List<Integer> selectReportedContentReviewNoList(Long memberNo);


    // 리뷰 삭제
    int deleteContentReview(Long reviewNo);

    ProductReviewVO selectProductReviewByReviewNo(Long reviewNo);

    int deleteProductReview(Long reviewNo);


    // 삭제된 콘텐츠 리뷰 복구
    int reactivateContentReview(ReviewVO review);


    // 상품 리뷰 목록 / 통계
    List<ProductReviewVO> selectProductReviewListByProductNo(int productNo);

    Double selectProductAvgRatingByProductNo(int productNo);

    int selectProductReviewCountByProductNo(int productNo);


    // 주문 상품 리뷰 작성 가능 여부 확인
    int countMyOrderItem(Map<String, Object> param);

}