package com.project.oditji.order.vo;

/**
 * 상품 상세 화면(goodsDetail.jsp)의 "바로 구매" 버튼으로
 * 장바구니를 거치지 않고 바로 주문서를 작성할 때 사용하는 요청 VO.
 * POST /order/direct 요청 본문에 대응한다.
 */
public class OrderDirectRequestVO {

    private Integer productNo;
    // [상품 옵션 기능 추가] 선택한 색상-사이즈 옵션
    private Long optionNo;
    private String colorName;
    private String sizeName;
    private Integer quantity;

    public OrderDirectRequestVO() {
    }

    public Long getOptionNo() {
        return optionNo;
    }

    public void setOptionNo(Long optionNo) {
        this.optionNo = optionNo;
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
}