package com.project.oditji.review.vo;

import java.util.Date;

/**
 * 마이페이지 "내가 작성한 리뷰" 목록 표시용 VO.
 * REVIEW(콘텐츠 리뷰) / PRODUCT_REVIEW(상품 리뷰) 두 테이블을
 * reviewType 값으로 구분하여 하나의 목록으로 합쳐서 보여주기 위한 통합 VO.
 */
public class MyReviewVO {

    private int reviewNo;

    // "CONTENT" 또는 "PRODUCT"
    private String reviewType;

    // reviewType == CONTENT 이면 CONTENT_NO, PRODUCT 이면 PRODUCT_NO
    private int targetNo;

    // 콘텐츠 제목 또는 상품명
    private String title;

    // 콘텐츠 포스터 경로 또는 상품 대표 이미지 경로
    private String thumbnail;

    private double rating;

    // 콘텐츠 리뷰는 REVIEW_TEXT, 상품 리뷰는 CONTENT 컬럼
    private String content;

    private Date createdAt;

    public MyReviewVO() {
    }

    public int getReviewNo() {
        return reviewNo;
    }

    public void setReviewNo(int reviewNo) {
        this.reviewNo = reviewNo;
    }

    public String getReviewType() {
        return reviewType;
    }

    public void setReviewType(String reviewType) {
        this.reviewType = reviewType;
    }

    public int getTargetNo() {
        return targetNo;
    }

    public void setTargetNo(int targetNo) {
        this.targetNo = targetNo;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
}
