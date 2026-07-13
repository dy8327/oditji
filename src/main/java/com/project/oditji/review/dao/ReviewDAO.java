package com.project.oditji.review.dao;

import java.util.List;
import java.util.Map;

import com.project.oditji.review.vo.MyReviewVO;
import com.project.oditji.review.vo.ProductReviewVO;
import com.project.oditji.review.vo.ReviewVO;

public interface ReviewDAO {

    // 로그인 회원이 작성한 콘텐츠 리뷰 목록 (REVIEW 테이블)
    List<MyReviewVO> selectMyContentReviewList(Long memberNo);

    // 로그인 회원이 작성한 상품 리뷰 목록 (PRODUCT_REVIEW 테이블)
    List<MyReviewVO> selectMyProductReviewList(Long memberNo);

    // 로그인 회원이 작성한 콘텐츠 리뷰 개수
    int countMyContentReview(Long memberNo);

    // 로그인 회원이 작성한 상품 리뷰 개수
    int countMyProductReview(Long memberNo);

    // ================= 리뷰 작성 =================

    // 콘텐츠 리뷰 등록
    int insertContentReview(ReviewVO review);

    // 콘텐츠 리뷰 수정
    int updateContentReview(ReviewVO review);

    // 리뷰번호로 콘텐츠 리뷰 단건 조회 (수정 시 작성자 본인 확인용)
    ReviewVO selectContentReviewByReviewNo(int reviewNo);

    // 상품 리뷰 등록
    int insertProductReview(ProductReviewVO productReview);

    // 회원이 해당 콘텐츠에 이미 리뷰를 남겼는지 확인 (UQ_REVIEW 제약조건 대응)
    ReviewVO selectContentReviewByMemberAndContent(Map<String, Object> param);

    // 해당 주문상품(ORDER_ITEM_NO)에 이미 리뷰가 작성됐는지 확인
    ProductReviewVO selectProductReviewByOrderItem(int orderItemNo);
}
