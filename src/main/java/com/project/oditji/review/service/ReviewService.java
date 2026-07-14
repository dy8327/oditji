package com.project.oditji.review.service;

import java.util.List;
import java.util.Set;

import com.project.oditji.review.vo.ContentReviewVO;
import com.project.oditji.review.vo.MyReviewVO;
import com.project.oditji.review.vo.ProductReviewVO;
import com.project.oditji.review.vo.ReviewVO;

public interface ReviewService {

    List<MyReviewVO> getMyReviewList(Long memberNo);
    int getMyReviewCount(Long memberNo);

    void writeContentReview(
            Long memberNo,
            int contentNo,
            double rating,
            String reviewText
    );

    void updateContentReview(
            Long memberNo,
            int reviewNo,
            double rating,
            String reviewText
    );

    void writeProductReview(
            Long memberNo,
            int productNo,
            int orderItemNo,
            double rating,
            String content
    );

    List<ContentReviewVO> getContentReviewList(int contentNo);
    Double getAvgRating(int contentNo);
    int getReviewCount(int contentNo);
    ReviewVO getMyReview(Long memberNo, int contentNo);
    Set<Integer> getReportedReviewSet(Long memberNo);

    void deleteContentReview(Long memberNo, int reviewNo);
    void deleteProductReview(Long memberNo, int reviewNo);

    List<ProductReviewVO> getProductReviewList(int productNo);
    Double getProductAvgRating(int productNo);
    int getProductReviewCount(int productNo);
}