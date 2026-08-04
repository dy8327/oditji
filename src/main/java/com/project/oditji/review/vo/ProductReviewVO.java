package com.project.oditji.review.vo;

/**
 * PRODUCT_REVIEW 테이블 매핑 VO.
 * 상품 리뷰 작성, 조회, 삭제에 사용합니다.
 */
public class ProductReviewVO extends ProductReviewBaseVO {

    private int productNo;
    private int orderItemNo;
    private Long memberNo;
    private String writer;
    private String profileImage;

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
