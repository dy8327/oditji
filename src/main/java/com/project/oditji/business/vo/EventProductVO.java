package com.project.oditji.business.vo;

/*
 * =========================================================
 * 이벤트-상품 연결 조회용 VO
 *
 * EVENT_PRODUCT + PRODUCT 조인 결과 한 행을 표현한다.
 *
 * EventManageVO.productNoList / discountRateList는
 * "등록/수정 화면에서 서버로 전송하는 입력값" 전용이고,
 * 이 VO는 "이미 저장된 연결 상품을 화면에 다시 보여줄 때" 전용이다.
 * 용도를 분리해서 값이 서로 덮어써지는 문제를 없앤다.
 * =========================================================
 */
public class EventProductVO {

    private long eventProdNo;
    private long eventNo;
    private long productNo;
    private String productName;
    private long price;
    private int discountRate;


    public long getEventProdNo() {
        return eventProdNo;
    }

    public void setEventProdNo(long eventProdNo) {
        this.eventProdNo = eventProdNo;
    }

    public long getEventNo() {
        return eventNo;
    }

    public void setEventNo(long eventNo) {
        this.eventNo = eventNo;
    }

    public long getProductNo() {
        return productNo;
    }

    public void setProductNo(long productNo) {
        this.productNo = productNo;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public long getPrice() {
        return price;
    }

    public void setPrice(long price) {
        this.price = price;
    }

    public int getDiscountRate() {
        return discountRate;
    }

    public void setDiscountRate(int discountRate) {
        this.discountRate = discountRate;
    }

    @Override
    public String toString() {
        return "EventProductVO{" +
                "eventProdNo=" + eventProdNo +
                ", eventNo=" + eventNo +
                ", productNo=" + productNo +
                ", productName='" + productName + '\'' +
                ", price=" + price +
                ", discountRate=" + discountRate +
                '}';
    }
}
