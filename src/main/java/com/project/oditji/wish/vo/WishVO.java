package com.project.oditji.wish.vo;

import java.time.LocalDateTime;

public class WishVO {

    private Long wishNo;
    private Long memberNo;
    private Integer productNo;
    private LocalDateTime createdAt;

    public Long getWishNo() {
        return wishNo;
    }

    public void setWishNo(Long wishNo) {
        this.wishNo = wishNo;
    }

    public Long getMemberNo() {
        return memberNo;
    }

    public void setMemberNo(Long memberNo) {
        this.memberNo = memberNo;
    }

    public Integer getProductNo() {
        return productNo;
    }

    public void setProductNo(Integer productNo) {
        this.productNo = productNo;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "WishVO [wishNo=" + wishNo
                + ", memberNo=" + memberNo
                + ", productNo=" + productNo
                + ", createdAt=" + createdAt + "]";
    }
}
