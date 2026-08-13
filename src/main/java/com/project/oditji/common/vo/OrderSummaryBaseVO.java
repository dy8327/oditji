package com.project.oditji.common.vo;

import java.time.LocalDateTime;

/**
 * 사용자 주문 목록과 관리자 주문 조회가 공통으로 사용하는 주문 기본 정보입니다.
 */
public abstract class OrderSummaryBaseVO extends OrderAddressBaseVO {

    private static final long serialVersionUID = 1L;

    private Long orderNo;
    private Long totalAmount;
    private String orderStatus;
    private LocalDateTime createdAt;

    public Long getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(Long orderNo) {
        this.orderNo = orderNo;
    }

    public Long getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(Long totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(String orderStatus) {
        this.orderStatus = orderStatus;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
