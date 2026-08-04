package com.project.oditji.cart.vo;

import java.util.Date;

import com.project.oditji.common.vo.ProductSaleInfoVO;

/**
 * 장바구니 화면 출력 및 주문 가능 여부 검증에 사용하는 품목 VO입니다.
 */
public class CartItemVO extends ProductSaleInfoVO {

    private static final long serialVersionUID = 1L;

    private Long cartItemNo;
    private Long cartNo;
    private Date createdAt;

    public Long getCartItemNo() {
        return cartItemNo;
    }

    public void setCartItemNo(Long cartItemNo) {
        this.cartItemNo = cartItemNo;
    }

    public Long getCartNo() {
        return cartNo;
    }

    public void setCartNo(Long cartNo) {
        this.cartNo = cartNo;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
}
