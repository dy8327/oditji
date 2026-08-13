package com.project.oditji.business.vo;

import com.project.oditji.common.vo.EventProductBaseVO;

/**
 * EVENT_PRODUCT와 PRODUCT 조인 결과 한 행을 표현하는 사업자 이벤트 상품 VO입니다.
 */
public class EventProductVO extends EventProductBaseVO {

    private Long eventProdNo;
    private Long eventNo;
    private Integer discountRate;

    public Long getEventProdNo() {
        return eventProdNo;
    }

    public void setEventProdNo(Long eventProdNo) {
        this.eventProdNo = eventProdNo;
    }

    public Long getEventNo() {
        return eventNo;
    }

    public void setEventNo(Long eventNo) {
        this.eventNo = eventNo;
    }

    public Integer getDiscountRate() {
        return discountRate;
    }

    public void setDiscountRate(Integer discountRate) {
        this.discountRate = discountRate;
    }

    @Override
    public String toString() {
        return "EventProductVO{" +
                "eventProdNo=" + eventProdNo +
                ", eventNo=" + eventNo +
                ", productNo=" + getProductNo() +
                ", productName='" + getProductName() + '\'' +
                ", price=" + getPrice() +
                ", discountRate=" + discountRate +
                '}';
    }
}
