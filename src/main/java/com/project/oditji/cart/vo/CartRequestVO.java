package com.project.oditji.cart.vo;

import java.util.List;

public class CartRequestVO {

    private Long cartItemNo;
    private Integer productNo;
    private Integer quantity;
    private List<Long> cartItemNos;

    public CartRequestVO() {
    }

    public Long getCartItemNo() {
        return cartItemNo;
    }

    public void setCartItemNo(Long cartItemNo) {
        this.cartItemNo = cartItemNo;
    }

    public Integer getProductNo() {
        return productNo;
    }

    public void setProductNo(Integer productNo) {
        this.productNo = productNo;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public List<Long> getCartItemNos() {
        return cartItemNos;
    }

    public void setCartItemNos(List<Long> cartItemNos) {
        this.cartItemNos = cartItemNos;
    }
}