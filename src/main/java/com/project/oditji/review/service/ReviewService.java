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

        // =========================
        // 콘텐츠 리뷰 작성 / 수정
        // =========================

        void writeContentReview(
                        Long memberNo,
                        int contentNo,
                        double rating,
                        String reviewText,
                        // [추가] 사용자가 선택한 스포일러 포함 여부
                        String spoilerYn);

        void updateContentReview(
                        Long memberNo,
                        Long reviewNo,
                        // [추가] 수정 리뷰의 줄거리 비교에 사용
                        int contentNo,
                        double rating,
                        String reviewText,
                        // [추가] 사용자가 선택한 스포일러 포함 여부
                        String spoilerYn);

        // =========================
        // 상품 리뷰 작성
        // =========================

        void writeProductReview(
                        Long memberNo,
                        int productNo,
                        int orderItemNo,
                        double rating,
                        String content);

        // =========================
        // 콘텐츠 리뷰 조회
        // =========================

        List<ContentReviewVO> getContentReviewList(int contentNo);

        Double getAvgRating(int contentNo);

        int getReviewCount(int contentNo);

        ReviewVO getMyReview(
                        Long memberNo,
                        int contentNo);

        Set<Integer> getReportedReviewSet(
                        Long memberNo);

        // =========================
        // 리뷰 삭제
        // =========================

        void deleteContentReview(
                        Long memberNo,
                        Long reviewNo);

        void deleteProductReview(
                        Long memberNo,
                        Long reviewNo);

        // =========================
        // 상품 리뷰 조회
        // =========================

        List<ProductReviewVO> getProductReviewList(
                        int productNo);

        Double getProductAvgRating(
                        int productNo);

        int getProductReviewCount(
                        int productNo);

}