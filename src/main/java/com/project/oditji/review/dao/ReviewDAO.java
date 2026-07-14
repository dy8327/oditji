package com.project.oditji.review.dao;

import java.util.List;
import java.util.Map;

import com.project.oditji.review.vo.ContentReviewVO;
import com.project.oditji.review.vo.MyReviewVO;
import com.project.oditji.review.vo.ProductReviewVO;
import com.project.oditji.review.vo.ReviewVO;

public interface ReviewDAO {

    List<MyReviewVO> selectMyContentReviewList(Long memberNo);
    List<MyReviewVO> selectMyProductReviewList(Long memberNo);
    int countMyContentReview(Long memberNo);
    int countMyProductReview(Long memberNo);

    int insertContentReview(ReviewVO review);
    int updateContentReview(ReviewVO review);
    ReviewVO selectContentReviewByReviewNo(int reviewNo);
    int insertProductReview(ProductReviewVO productReview);
    ReviewVO selectContentReviewByMemberAndContent(Map<String, Object> param);
    ProductReviewVO selectProductReviewByOrderItem(int orderItemNo);

    List<ContentReviewVO> selectContentReviewListByContentNo(int contentNo);
    Double selectAvgRatingByContentNo(int contentNo);
    int selectReviewCountByContentNo(int contentNo);
    List<Integer> selectReportedContentReviewNoList(Long memberNo);

    int deleteContentReview(int reviewNo);
    ProductReviewVO selectProductReviewByReviewNo(int reviewNo);
    int deleteProductReview(int reviewNo);

    ReviewVO selectContentReviewByMemberAndContentAnyStatus(
            Map<String, Object> param
    );

    int reactivateContentReview(ReviewVO review);

    List<ProductReviewVO> selectProductReviewListByProductNo(int productNo);
    Double selectProductAvgRatingByProductNo(int productNo);
    int selectProductReviewCountByProductNo(int productNo);
}