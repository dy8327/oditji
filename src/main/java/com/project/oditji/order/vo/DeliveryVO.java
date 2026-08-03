package com.project.oditji.order.vo;

import java.util.Date;

/*
 * =========================================================
 * 사용자(구매자)가 보는 배송 조회 화면용 VO
 *
 * ORDER_ITEM, ORDERS, PRODUCT, PRODUCT_IMAGE, DELIVERY 정보를
 * 한 번에 담아 orderList.jsp의 배송 조회 모달 출력에 사용한다.
 *
 * business.vo.DeliveryManageVO(사업자 배송 관리용)와 필드 구성을
 * 최대한 동일하게 맞추고, 화면에 상품 이미지를 보여주기 위해
 * mainImage 필드를 추가했다.
 * =========================================================
 */
public class DeliveryVO {

    private Long deliveryNo;
    private Long orderItemNo;
    private Long orderNo;
    private Integer productNo;
    private String productName;
    private String mainImage;
    private Integer productPrice;
    private Integer quantity;
    private String receiverName;
    private String receiverPhone;
    private String address;
    private String trackingNumber;
    private String courier;
    private String status;
    private Date orderCreatedAt;

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

    public Integer getProductNo() {
        return productNo;
    }

    public void setProductNo(Integer productNo) {
        this.productNo = productNo;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getMainImage() {
        return mainImage;
    }

    public void setMainImage(String mainImage) {
        this.mainImage = mainImage;
    }

    public Integer getProductPrice() {
        return productPrice;
    }

    public void setProductPrice(Integer productPrice) {
        this.productPrice = productPrice;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public String getReceiverName() {
        return receiverName;
    }

    public void setReceiverName(String receiverName) {
        this.receiverName = receiverName;
    }

    public String getReceiverPhone() {
        return receiverPhone;
    }

    public void setReceiverPhone(String receiverPhone) {
        this.receiverPhone = receiverPhone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
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

    /* 배송 조회 화면에서 해당 주문상품의 결제금액(상품가격 * 수량)을 출력하기 위한 계산 메서드. */
    public long getItemTotalPrice() {
        int unitPrice = productPrice == null ? 0 : productPrice;
        int itemQuantity = quantity == null ? 0 : quantity;
        return (long) unitPrice * itemQuantity;
    }
}
