package com.project.oditji.review.service;

import java.util.List;
import java.util.Set;

import com.project.oditji.review.vo.ContentReviewVO;
import com.project.oditji.review.vo.MyReviewVO;
import com.project.oditji.review.vo.ReviewVO;

public interface ReviewService {

    List<MyReviewVO> getMyReviewList(Long memberNo);
    int getMyReviewCount(Long memberNo);
    void writeContentReview(Long memberNo, int contentNo, double rating, String reviewText);
    void updateContentReview(Long memberNo, int reviewNo, double rating, String reviewText);
    void writeProductReview(Long memberNo, int productNo, int orderItemNo, double rating, String content);

    // ===== 콘텐츠 상세페이지용 신규 추가 =====

    // 콘텐츠 리뷰 목록
    List<ContentReviewVO> getContentReviewList(int contentNo);

    // 콘텐츠 평균 평점 (리뷰 없으면 null)
    Double getAvgRating(int contentNo);

    // 콘텐츠 리뷰 개수
    int getReviewCount(int contentNo);

    // 로그인 회원이 이 콘텐츠에 작성한 리뷰 (없으면 null → 신규 작성 폼 노출)
    ReviewVO getMyReview(Long memberNo, int contentNo);

    // 로그인 회원이 신고한 리뷰번호 Set (비로그인이면 빈 Set)
    Set<Integer> getReportedReviewSet(Long memberNo);

    // ===== 삭제 기능 신규 추가 =====

    void deleteContentReview(Long memberNo, int reviewNo);
    void deleteProductReview(Long memberNo, int reviewNo);
}