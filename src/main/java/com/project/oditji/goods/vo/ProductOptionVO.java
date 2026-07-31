package com.project.oditji.goods.vo;

/**
 * [상품 옵션 기능 추가] 의상/신발의 색상-사이즈 조합별 재고를 보관합니다.
 */
public class ProductOptionVO {
    private Long optionNo;
    private Long productNo;
    private String colorName;
    private String sizeName;
    private Integer stock;

    public Long getOptionNo() {
        return optionNo;
    }

    public void setOptionNo(Long optionNo) {
        this.optionNo = optionNo;
    }

    public Long getProductNo() {
        return productNo;
    }

    public void setProductNo(Long productNo) {
        this.productNo = productNo;
    }

    public String getColorName() {
        return colorName;
    }

    public void setColorName(String colorName) {
        this.colorName = colorName;
    }

    public String getSizeName() {
        return sizeName;
    }

    public void setSizeName(String sizeName) {
        this.sizeName = sizeName;
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }
}