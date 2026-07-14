package com.project.oditji.admin.vo;

import java.util.Date;

/**
 * 주문/환불 관리 VO
 * 주문 탭: ORDER_ITEM + ORDERS + PRODUCT + CONTENT + DELIVERY 조인
 * 환불 탭: CANCEL_REQUEST + ORDER_ITEM + PRODUCT + CONTENT + MEMBER 조인
 */
public class OrderManageVO {

    private Long orderNo;
    private Long orderItemNo;
    private Long productNo;
    private String productName;
    private String contentTitle;
    private Long productPrice;
    private Long quantity;
    private String status;      // 주문 탭: DELIVERY.STATUS (PREPARING/SHIPPING/DELIVERED/CONFIRMED)
    private Long businessNo;
    private String memberId;    // 환불 탭: 구매자 아이디
    private Long cancelNo;      // 환불 탭: CANCEL_REQUEST.CANCEL_NO
    private String reason;      // 환불 탭: CANCEL_REQUEST.REASON
    private Date createdAt;

    public Long getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(Long orderNo) {
        this.orderNo = orderNo;
    }

    public Long getOrderItemNo() {
        return orderItemNo;
    }

    public void setOrderItemNo(Long orderItemNo) {
        this.orderItemNo = orderItemNo;
    }

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

    public String getContentTitle() {
        return contentTitle;
    }

    public void setContentTitle(String contentTitle) {
        this.contentTitle = contentTitle;
    }

    public Long getProductPrice() {
        return productPrice;
    }

    public void setProductPrice(Long productPrice) {
        this.productPrice = productPrice;
    }

    public Long getQuantity() {
        return quantity;
    }

    public void setQuantity(Long quantity) {
        this.quantity = quantity;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getBusinessNo() {
        return businessNo;
    }

    public void setBusinessNo(Long businessNo) {
        this.businessNo = businessNo;
    }

    public String getMemberId() {
        return memberId;
    }

    public void setMemberId(String memberId) {
        this.memberId = memberId;
    }

    public Long getCancelNo() {
        return cancelNo;
    }

    public void setCancelNo(Long cancelNo) {
        this.cancelNo = cancelNo;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
}
