package com.project.oditji.cart.vo;

import java.util.List;

public class CartRequestVO {

    private Long cartItemNo;
    private Integer productNo;
    // [상품 옵션 기능 추가] 선택한 색상-사이즈 조합 번호
    private Long optionNo;
    private Integer quantity;
    private List<Long> cartItemNos;

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

    public Long getOptionNo() {
        return optionNo;
    }

    public void setOptionNo(Long optionNo) {
        this.optionNo = optionNo;
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