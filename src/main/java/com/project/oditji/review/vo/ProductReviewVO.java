package com.project.oditji.review.vo;

import java.util.Date;

/**
 * PRODUCT_REVIEW 테이블 매핑 VO.
 * 상품 리뷰 작성, 조회, 삭제에 사용한다.
 */
public class ProductReviewVO {

    private int reviewNo;
    private int productNo;
    private int orderItemNo;
    private Long memberNo;
    private double rating;
    private String content;
    private Date createdAt;

    /*
     * 상품 상세 리뷰 목록 출력용 회원 정보
     */
    private String writer;
    private String profileImage;

    public ProductReviewVO() {
    }

    public int getReviewNo() {
        return reviewNo;
    }

    public void setReviewNo(int reviewNo) {
        this.reviewNo = reviewNo;
    }

    public int getProductNo() {
        return productNo;
    }

    public void setProductNo(int productNo) {
        this.productNo = productNo;
    }

    public int getOrderItemNo() {
        return orderItemNo;
    }

    public void setOrderItemNo(int orderItemNo) {
        this.orderItemNo = orderItemNo;
    }

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

    public String getWriter() {
        return writer;
    }

    public void setWriter(String writer) {
        this.writer = writer;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }
}