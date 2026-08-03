package com.project.oditji.review.vo;

import java.util.Date;

/**
 * REVIEW 테이블(콘텐츠 리뷰) 원본 매핑 VO.
 * 리뷰 작성/수정 시 사용.
 */
public class ReviewVO {

    private Long reviewNo;
    private Long memberNo;
    private int contentNo;
    private double rating;
    private String reviewText;

    // [추가] 스포일러 포함 여부(Y/N)
    private String spoilerYn;
    private String status;
    private Date createdAt;
    private Date updatedAt;

    public Long getReviewNo() {
        return reviewNo;
    }

    public void setReviewNo(Long reviewNo) {
        this.reviewNo = reviewNo;
    }

    public Long getMemberNo() {
        return memberNo;
    }

    public void setMemberNo(Long memberNo) {
        this.memberNo = memberNo;
    }

    public int getContentNo() {
        return contentNo;
    }

    public void setContentNo(int contentNo) {
        this.contentNo = contentNo;
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

    // [추가] 스포일러 포함 여부 getter/setter
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

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }
}