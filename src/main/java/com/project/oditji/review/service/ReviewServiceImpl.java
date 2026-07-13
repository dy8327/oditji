package com.project.oditji.review.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.project.oditji.review.dao.ReviewDAO;
import com.project.oditji.review.vo.MyReviewVO;
import com.project.oditji.review.vo.ProductReviewVO;
import com.project.oditji.review.vo.ReviewVO;

@Service
public class ReviewServiceImpl implements ReviewService {

    private final ReviewDAO reviewDAO;

    public ReviewServiceImpl(ReviewDAO reviewDAO) {
        this.reviewDAO = reviewDAO;
    }

    @Override
    public List<MyReviewVO> getMyReviewList(Long memberNo) {

        List<MyReviewVO> contentReviewList =
                reviewDAO.selectMyContentReviewList(memberNo);

        List<MyReviewVO> productReviewList =
                reviewDAO.selectMyProductReviewList(memberNo);

        List<MyReviewVO> myReviewList = new ArrayList<>();

        if (contentReviewList != null) {
            myReviewList.addAll(contentReviewList);
        }

        if (productReviewList != null) {
            myReviewList.addAll(productReviewList);
        }

        // 콘텐츠 리뷰 / 상품 리뷰를 합친 뒤 작성일 기준 최신순 정렬
        myReviewList.sort(
                Comparator.comparing(MyReviewVO::getCreatedAt).reversed());

        return myReviewList;
    }

    @Override
    public int getMyReviewCount(Long memberNo) {

        int contentReviewCount = reviewDAO.countMyContentReview(memberNo);
        int productReviewCount = reviewDAO.countMyProductReview(memberNo);

        return contentReviewCount + productReviewCount;
    }

    @Override
    @Transactional
    public void writeContentReview(
            Long memberNo, int contentNo, double rating, String reviewText) {

        if (memberNo == null) {
            throw new IllegalArgumentException("로그인이 필요합니다.");
        }

        if (rating < 0 || rating > 5) {
            throw new IllegalArgumentException("별점은 0~5점 사이여야 합니다.");
        }

        if (reviewText == null || reviewText.trim().isEmpty()) {
            throw new IllegalArgumentException("리뷰 내용을 입력해주세요.");
        }

        // UQ_REVIEW(MEMBER_NO, CONTENT_NO) 제약조건 대응 - 중복 작성 방지
        Map<String, Object> checkParam = new HashMap<>();
        checkParam.put("memberNo", memberNo);
        checkParam.put("contentNo", contentNo);

        ReviewVO existingReview =
                reviewDAO.selectContentReviewByMemberAndContent(checkParam);

        if (existingReview != null) {
            throw new IllegalStateException("이미 작성한 리뷰가 있습니다.");
        }

        ReviewVO review = new ReviewVO();
        review.setMemberNo(memberNo);
        review.setContentNo(contentNo);
        review.setRating(rating);
        review.setReviewText(reviewText);

        reviewDAO.insertContentReview(review);
    }

    @Override
    @Transactional
    public void updateContentReview(
            Long memberNo, int reviewNo, double rating, String reviewText) {

        if (memberNo == null) {
            throw new IllegalArgumentException("로그인이 필요합니다.");
        }

        if (rating < 0 || rating > 5) {
            throw new IllegalArgumentException("별점은 0~5점 사이여야 합니다.");
        }

        if (reviewText == null || reviewText.trim().isEmpty()) {
            throw new IllegalArgumentException("리뷰 내용을 입력해주세요.");
        }

        ReviewVO existingReview =
                reviewDAO.selectContentReviewByReviewNo(reviewNo);

        if (existingReview == null) {
            throw new IllegalArgumentException("존재하지 않는 리뷰입니다.");
        }

        if (!memberNo.equals(existingReview.getMemberNo())) {
            throw new IllegalStateException("본인이 작성한 리뷰만 수정할 수 있습니다.");
        }

        ReviewVO review = new ReviewVO();
        review.setReviewNo(reviewNo);
        review.setRating(rating);
        review.setReviewText(reviewText);

        reviewDAO.updateContentReview(review);
    }

    @Override
    @Transactional
    public void writeProductReview(
            Long memberNo, int productNo, int orderItemNo, double rating, String content) {

        if (memberNo == null) {
            throw new IllegalArgumentException("로그인이 필요합니다.");
        }

        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("별점은 1~5점 사이여야 합니다.");
        }

        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("리뷰 내용을 입력해주세요.");
        }

        // 같은 주문상품(ORDER_ITEM_NO)에는 리뷰를 중복으로 작성할 수 없도록 확인
        ProductReviewVO existingReview =
                reviewDAO.selectProductReviewByOrderItem(orderItemNo);

        if (existingReview != null) {
            throw new IllegalStateException("이미 작성한 리뷰가 있습니다.");
        }

        ProductReviewVO productReview = new ProductReviewVO();
        productReview.setProductNo(productNo);
        productReview.setOrderItemNo(orderItemNo);
        productReview.setMemberNo(memberNo);
        productReview.setRating(rating);
        productReview.setContent(content);

        reviewDAO.insertProductReview(productReview);
    }
}
