package com.project.oditji.review.vo;

import java.time.LocalDateTime;

/**
 * 상품 리뷰 원본과 마이페이지 통합 리뷰가 공통으로 사용하는 표시 정보입니다.
 */
public abstract class ProductReviewBaseVO {

    private int reviewNo;
    private double rating;
    private String content;
    private LocalDateTime createdAt;

    public int getReviewNo() {
        return reviewNo;
    }

    public void setReviewNo(int reviewNo) {
        this.reviewNo = reviewNo;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
