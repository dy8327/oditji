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

    // ===== 콘텐츠 상세페이지용 신규 추가 =====

    // 특정 콘텐츠의 리뷰 목록 (작성자 닉네임 포함, 최신순)
    List<ContentReviewVO> selectContentReviewListByContentNo(int contentNo);

    // 특정 콘텐츠의 평균 평점
    Double selectAvgRatingByContentNo(int contentNo);

    // 특정 콘텐츠의 리뷰 개수
    int selectReviewCountByContentNo(int contentNo);

    // 로그인 회원이 신고한 콘텐츠 리뷰번호 목록 (신고완료 버튼 표시용)
    List<Integer> selectReportedContentReviewNoList(Long memberNo);

    // ===== 삭제 기능 신규 추가 =====

    // 콘텐츠 리뷰 삭제 (STATUS = 'DELETED' 로 변경하는 soft delete)
    int deleteContentReview(int reviewNo);

    // 상품 리뷰 단건 조회 (삭제 시 작성자 본인 확인용)
    ProductReviewVO selectProductReviewByReviewNo(int reviewNo);

    // 상품 리뷰 삭제 (실제 row 삭제)
    int deleteProductReview(int reviewNo);

    // ===== 삭제된 리뷰 재작성(재활성화) 신규 추가 =====

    // 상태(ACTIVE/DELETED) 무관하고 (회원, 콘텐츠) 리뷰 조회 - 재작성 여부 판단용
    ReviewVO selectContentReviewByMemberAndContentAnyStatus(Map<String, Object> param);

    // 삭제됐던 리뷰를 재활성화(재작성)
    int reactivateContentReview(ReviewVO review);
}