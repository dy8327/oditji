package com.project.oditji.review.vo;

import java.util.Date;

/**
 * 콘텐츠 상세 페이지 리뷰 목록 표시용 VO (작성자 닉네임 포함)
 */
public class ContentReviewVO {

    private int reviewNo;
    private Long memberNo;
    private String writer;
    private double rating;
    private String reviewText;
    private String status;
    private Date createdAt;

    public ContentReviewVO() {
    }

    public int getReviewNo() { return reviewNo; }
    public void setReviewNo(int reviewNo) { this.reviewNo = reviewNo; }

    public Long getMemberNo() { return memberNo; }
    public void setMemberNo(Long memberNo) { this.memberNo = memberNo; }

    public String getWriter() { return writer; }
    public void setWriter(String writer) { this.writer = writer; }

    public double getRating() { return rating; }
    public void setRating(double rating) { this.rating = rating; }

    public String getReviewText() { return reviewText; }
    public void setReviewText(String reviewText) { this.reviewText = reviewText; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
}