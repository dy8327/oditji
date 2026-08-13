package com.project.oditji.common.vo;

/**
 * 상품 번호, 옵션 번호, 수량과 함께 화면에 표시할 색상·사이즈명을
 * 보관하는 주문용 상품 선택 정보입니다.
 */
public abstract class ProductOptionSelectionVO
        extends ProductSelectionVO {

    private static final long serialVersionUID = 1L;

    private String colorName;
    private String sizeName;

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
}
