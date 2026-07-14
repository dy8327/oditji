package com.project.oditji.review.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.project.oditji.review.dao.ReviewDAO;
import com.project.oditji.review.vo.ContentReviewVO;
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

        Map<String, Object> checkParam = new HashMap<>();
        checkParam.put("memberNo", memberNo);
        checkParam.put("contentNo", contentNo);

        // 상태(ACTIVE/DELETED) 무관하고 조회 - 삭제했던 리뷰도 잡아냄
        ReviewVO existingReview =
                reviewDAO.selectContentReviewByMemberAndContentAnyStatus(checkParam);

        if (existingReview != null) {

            if (!"DELETED".equals(existingReview.getStatus())) {
                // ACTIVE 리뷰가 이미 있으면 차단
                throw new IllegalStateException("이미 작성한 리뷰가 있습니다.");
            }

            // 삭제했던 리뷰라면 새로 INSERT 하지 않고 기존 row를 재활용
            ReviewVO reactivated = new ReviewVO();
            reactivated.setReviewNo(existingReview.getReviewNo());
            reactivated.setRating(rating);
            reactivated.setReviewText(reviewText);

            reviewDAO.reactivateContentReview(reactivated);
            return;
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

    @Override
    public List<ContentReviewVO> getContentReviewList(int contentNo) {

        List<ContentReviewVO> reviewList =
                reviewDAO.selectContentReviewListByContentNo(contentNo);

        return reviewList == null
                ? Collections.emptyList()
                : reviewList;
    }

    @Override
    public Double getAvgRating(int contentNo) {
        return reviewDAO.selectAvgRatingByContentNo(contentNo);
    }

    @Override
    public int getReviewCount(int contentNo) {
        return reviewDAO.selectReviewCountByContentNo(contentNo);
    }

    @Override
    public ReviewVO getMyReview(Long memberNo, int contentNo) {

        if (memberNo == null) {
            return null;
        }

        Map<String, Object> param = new HashMap<>();
        param.put("memberNo", memberNo);
        param.put("contentNo", contentNo);

        return reviewDAO.selectContentReviewByMemberAndContent(param);
    }

    @Override
    public Set<Integer> getReportedReviewSet(Long memberNo) {

        if (memberNo == null) {
            return Collections.emptySet();
        }

        List<Integer> reportedList =
                reviewDAO.selectReportedContentReviewNoList(memberNo);

        return reportedList == null
                ? Collections.emptySet()
                : new HashSet<>(reportedList);
    }

    @Override
    @Transactional
    public void deleteContentReview(Long memberNo, int reviewNo) {

        if (memberNo == null) {
            throw new IllegalArgumentException("로그인이 필요합니다.");
        }

        ReviewVO existingReview =
                reviewDAO.selectContentReviewByReviewNo(reviewNo);

        if (existingReview == null) {
            throw new IllegalArgumentException("존재하지 않는 리뷰입니다.");
        }

        if (!memberNo.equals(existingReview.getMemberNo())) {
            throw new IllegalStateException("본인이 작성한 리뷰만 삭제할 수 있습니다.");
        }

        reviewDAO.deleteContentReview(reviewNo);
    }

    @Override
    @Transactional
    public void deleteProductReview(Long memberNo, int reviewNo) {

        if (memberNo == null) {
            throw new IllegalArgumentException("로그인이 필요합니다.");
        }

        ProductReviewVO existingReview =
                reviewDAO.selectProductReviewByReviewNo(reviewNo);

        if (existingReview == null) {
            throw new IllegalArgumentException("존재하지 않는 리뷰입니다.");
        }

        if (!memberNo.equals(existingReview.getMemberNo())) {
            throw new IllegalStateException("본인이 작성한 리뷰만 삭제할 수 있습니다.");
        }

        reviewDAO.deleteProductReview(reviewNo);
    }

    @Override
    public List<ProductReviewVO> getProductReviewList(
            int productNo) {

        if (productNo <= 0) {
            return Collections.emptyList();
        }

        List<ProductReviewVO> reviewList =
                reviewDAO.selectProductReviewListByProductNo(
                        productNo
                );

        return reviewList == null
                ? Collections.emptyList()
                : reviewList;
    }

    @Override
    public Double getProductAvgRating(
            int productNo) {

        if (productNo <= 0) {
            return null;
        }

        return reviewDAO.selectProductAvgRatingByProductNo(
                productNo
        );
    }

    @Override
    public int getProductReviewCount(
            int productNo) {

        if (productNo <= 0) {
            return 0;
        }

        return reviewDAO.selectProductReviewCountByProductNo(
                productNo
        );
    }

}