package com.project.oditji.common.vo;

/**
 * NUMBER 기반 상품 번호를 사용하는 관리자·이벤트 화면의 공통 상품 요약입니다.
 */
public abstract class LongProductSummaryVO {

    private Long productNo;
    private String productName;
    private Long price;

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

    public Long getPrice() {
        return price;
    }

    public void setPrice(Long price) {
        this.price = price;
    }
}
