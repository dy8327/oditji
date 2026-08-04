package com.project.oditji.order.vo;

import com.project.oditji.common.vo.ProductSaleInfoVO;

/**
 * 주문서 작성 단계에서 세션에 보관하는 임시 주문 품목 정보입니다.
 */
public class OrderSheetItemVO extends ProductSaleInfoVO {

    private static final long serialVersionUID = 1L;

    private Integer businessNo;
    private Long cartItemNo;

    public Integer getBusinessNo() {
        return businessNo;
    }

    public void setBusinessNo(Integer businessNo) {
        this.businessNo = businessNo;
    }

    public Long getCartItemNo() {
        return cartItemNo;
    }

    public void setCartItemNo(Long cartItemNo) {
        this.cartItemNo = cartItemNo;
    }
}
