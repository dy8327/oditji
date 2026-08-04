package com.project.oditji.cart.vo;

import java.util.Date;

public class CartVO {

    private Long cartNo;
    private Long memberNo;
    private Date createdAt;

    public Long getCartNo() {
        return cartNo;
    }

    public void setCartNo(Long cartNo) {
        this.cartNo = cartNo;
    }

    public Long getMemberNo() {
        return memberNo;
    }

    public void setMemberNo(Long memberNo) {
        this.memberNo = memberNo;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
}