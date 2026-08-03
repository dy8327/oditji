package com.project.oditji.common.vo;

/**
 * NUMBER 기반 상품 번호를 사용하는 관리자·이벤트 화면의 공통 상품 요약입니다.
 */
public abstract class LongProductSummaryVO {

    private final ProductSummaryFields fields =
            new ProductSummaryFields();

    public Long getProductNo() {
        return fields.productNo;
    }

    public void setProductNo(Long productNo) {
        fields.productNo = productNo;
    }

    public String getProductName() {
        return fields.productName;
    }

    public void setProductName(String productName) {
        fields.productName = productName;
    }

    public Long getPrice() {
        return fields.price;
    }

    public void setPrice(Long price) {
        fields.price = price;
    }

    /**
     * 공통 상품 속성을 한 객체에 묶어 다른 VO의 단순 접근자 블록과
     * 중복되지 않으면서 기존 JavaBean 속성명은 그대로 유지합니다.
     */
    private static final class ProductSummaryFields {

        private Long productNo;
        private String productName;
        private Long price;
    }
}
