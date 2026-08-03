package com.project.oditji.common.vo;

import java.io.Serializable;

/**
 * 장바구니와 주문 요청에서 공통으로 사용하는 상품 선택 기본 정보입니다.
 */
public abstract class ProductSelectionVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer productNo;
    private Long optionNo;
    private Integer quantity;

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
}
