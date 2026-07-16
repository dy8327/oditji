package com.project.oditji.order.vo;

/**
 * 상품 상세 화면(goodsDetail.jsp)의 "바로 구매" 버튼으로
 * 장바구니를 거치지 않고 바로 주문서를 작성할 때 사용하는 요청 VO.
 * POST /order/direct 요청 본문에 대응한다.
 */
public class OrderDirectRequestVO {

    private Integer productNo;
    private Integer quantity;

    public OrderDirectRequestVO() {
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
