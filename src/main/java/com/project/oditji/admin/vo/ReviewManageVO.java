package com.project.oditji.admin.vo;

import java.util.Date;

/**
 * 리뷰 관리 VO
 * 콘텐츠 리뷰(REVIEW)와 상품 리뷰(PRODUCT_REVIEW) 조회에 공용으로 사용한다.
 * productName은 상품 리뷰 조회 시에만 값이 채워진다.
 */
public class ReviewManageVO {

    private Long reviewNo;
    private Long memberNo;
    private String nickname;       // MEMBER 조인
    private Long contentNo;
    private String contentTitle;   // 콘텐츠 리뷰 조회 시에만 사용 (CONTENT 조인)
    private Long productNo;
    private String productName;    // 상품 리뷰 조회 시에만 사용 (PRODUCT 조인)
    private Double rating;
    private String content;
    private String status;         // 콘텐츠 리뷰에만 존재 (ACTIVE/HIDDEN/DELETED)
    private Long reportCount;       // REVIEW_REPORT 집계 (신고 탭에서만 사용)
    private Date createdAt;

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

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public Long getContentNo() {
        return contentNo;
    }

    public void setContentNo(Long contentNo) {
        this.contentNo = contentNo;
    }

    public String getContentTitle() {
        return contentTitle;
    }

    public void setContentTitle(String contentTitle) {
        this.contentTitle = contentTitle;
    }

    public Long getProductNo() {
        return productNo;
    }

    public void setProductNo(Long productNo) {
        this.productNo = productNo;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public Double getRating() {
        return rating;
    }

    public void setRating(Double rating) {
        this.rating = rating;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getReportCount() {
        return reportCount;
    }

    public void setReportCount(Long reportCount) {
        this.reportCount = reportCount;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
}
