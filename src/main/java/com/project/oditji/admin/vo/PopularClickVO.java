package com.project.oditji.admin.vo;

/**
 * 모니터링 화면 - 상품 클릭수 TOP N 차트용 VO
 * 테이블: PRODUCT_CLICK_LOG + PRODUCT 조인
 */
public class PopularClickVO {

    private String productName;
    private Long clickCount;

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public Long getClickCount() {
        return clickCount;
    }

    public void setClickCount(Long clickCount) {
        this.clickCount = clickCount;
    }
}
