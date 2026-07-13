package com.project.oditji.review.service;

import java.util.List;

import com.project.oditji.review.vo.MyReviewVO;

public interface ReviewService {

    // 콘텐츠 리뷰 + 상품 리뷰를 합쳐서 최신순으로 정렬한 목록
    List<MyReviewVO> getMyReviewList(Long memberNo);

    // 콘텐츠 리뷰 + 상품 리뷰 합산 개수 (마이페이지 활동 카운트용)
    int getMyReviewCount(Long memberNo);

    // 콘텐츠 리뷰 작성
    void writeContentReview(Long memberNo, int contentNo, double rating, String reviewText);

    // 콘텐츠 리뷰 수정 (본인 리뷰인지 확인 후 수정)
    void updateContentReview(Long memberNo, int reviewNo, double rating, String reviewText);

    // 상품 리뷰 작성
    void writeProductReview(Long memberNo, int productNo, int orderItemNo, double rating, String content);
}
