package com.project.oditji.review.vo;

import java.time.LocalDateTime;

/**
 * 콘텐츠 리뷰 원본 VO와 목록 표시용 VO가 공통으로 사용하는 필드입니다.
 */
public abstract class ReviewBaseVO {

    private Long memberNo;
    private double rating;
    private String reviewText;
    private String spoilerYn;
    private String status;
    private LocalDateTime createdAt;

    public Long getMemberNo() {
        return memberNo;
    }

    public void setMemberNo(Long memberNo) {
        this.memberNo = memberNo;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public String getReviewText() {
        return reviewText;
    }

    public void setReviewText(String reviewText) {
        this.reviewText = reviewText;
    }

    public String getSpoilerYn() {
        return spoilerYn;
    }

    public void setSpoilerYn(String spoilerYn) {
        this.spoilerYn = spoilerYn;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
