package com.project.oditji.common.vo;

import java.util.Date;

/**
 * 사업자 배송 관리 화면과 구매자 배송 조회 화면이 공통으로 사용하는
 * 배송·주문상품 기본 정보를 보관합니다.
 */
public abstract class DeliveryBaseVO extends OrderAddressBaseVO {

    private static final long serialVersionUID = 1L;

    private Integer productNo;
    private Long optionNo;
    private Integer quantity;
    private Long deliveryNo;
    private Long orderItemNo;
    private Long orderNo;
    private String productName;
    private Integer productPrice;
    private String trackingNumber;
    private String courier;
    private String status;
    private Date orderCreatedAt;

    public Integer getProductNo() {
        return productNo;
    }

    public void setProductNo(Integer productNo) {
        this.productNo = productNo;
    }

    public Long getOptionNo() {
        return optionNo;
    }

    public void setOptionNo(Long optionNo) {
        this.optionNo = optionNo;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Long getDeliveryNo() {
        return deliveryNo;
    }

    public void setDeliveryNo(Long deliveryNo) {
        this.deliveryNo = deliveryNo;
    }

    public Long getOrderItemNo() {
        return orderItemNo;
    }

    public void setOrderItemNo(Long orderItemNo) {
        this.orderItemNo = orderItemNo;
    }

    public Long getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(Long orderNo) {
        this.orderNo = orderNo;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public Integer getProductPrice() {
        return productPrice;
    }

    public void setProductPrice(Integer productPrice) {
        this.productPrice = productPrice;
    }

    public String getTrackingNumber() {
        return trackingNumber;
    }

    public void setTrackingNumber(String trackingNumber) {
        this.trackingNumber = trackingNumber;
    }

    public String getCourier() {
        return courier;
    }

    public void setCourier(String courier) {
        this.courier = courier;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getOrderCreatedAt() {
        return orderCreatedAt;
    }

    public void setOrderCreatedAt(Date orderCreatedAt) {
        this.orderCreatedAt = orderCreatedAt;
    }

    public long getItemTotalPrice() {
        int unitPrice = productPrice == null ? 0 : productPrice;
        int itemQuantity = quantity == null ? 0 : quantity;
        return (long) unitPrice * itemQuantity;
    }
}
