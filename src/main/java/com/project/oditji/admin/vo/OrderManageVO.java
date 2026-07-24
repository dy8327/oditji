package com.project.oditji.admin.vo;

import java.util.Date;

/**
 * 주문/환불 관리 VO (조회 전용)
 *
 * 관리자 화면에서는 주문/배송/환불을 직접 처리하지 않는다.
 * 실제 처리(배송상태 변경, 주문취소, 환불 승인/거절)는 사업자(Business)가 담당하며,
 * 관리자는 분쟁 확인 등을 위해 상세 내역만 조회할 수 있다.
 *
 * 주문 탭: ORDER_ITEM + ORDERS + PRODUCT + CONTENT + DELIVERY 조인
 * 환불 탭: CANCEL_REQUEST + ORDER_ITEM + PRODUCT + CONTENT + MEMBER 조인
 */
public class OrderManageVO {

    // 공통 / 주문 탭
    private Long orderNo;
    private Long orderItemNo;
    private Long productNo;
    private String productName;
    private String contentTitle;
    private Long productPrice;
    private Long quantity;
    private String status;         // 주문 탭: DELIVERY.STATUS (PREPARING/SHIPPING/DELIVERED/CONFIRMED)
    private Long businessNo;
    private String memberId;       // 구매자 아이디
    private Date createdAt;

    // 주문 탭 - 주문 전체 정보
    private Long totalAmount;      // ORDERS.TOTAL_AMOUNT
    private String orderStatus;    // ORDERS.ORDER_STATUS (주문 전체 상태)

    // 주문 탭 - 배송지 정보 (조회 전용)
    private String receiverName;
    private String receiverPhone;
    private String address;

    // 주문 탭 - 배송 추적 정보 (조회 전용)
    private String trackingNumber;
    private String courier;

    // 환불 탭 - 취소/환불 상세 (조회 전용)
    private Long cancelNo;         // CANCEL_REQUEST.CANCEL_NO
    private String cancelType;     // CANCEL_REQUEST.CANCEL_TYPE (FULL/PARTIAL)
    private Long refundAmount;     // CANCEL_REQUEST.REFUND_AMOUNT
    private String reason;         // CANCEL_REQUEST.REASON (환불 요청 사유)
    private String cancelStatus;   // CANCEL_REQUEST.STATUS (WAITING/APPROVED/REJECTED)
    private String rejectReason;   // CANCEL_REQUEST.REJECT_REASON (사업자 반려 사유)
    private Date processedAt;      // CANCEL_REQUEST.PROCESSED_AT (사업자 처리일)

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

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
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

    public Long getCancelNo() {
        return cancelNo;
    }

    public void setCancelNo(Long cancelNo) {
        this.cancelNo = cancelNo;
    }

    public String getCancelType() {
        return cancelType;
    }

    public void setCancelType(String cancelType) {
        this.cancelType = cancelType;
    }

    public Long getRefundAmount() {
        return refundAmount;
    }

    public void setRefundAmount(Long refundAmount) {
        this.refundAmount = refundAmount;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getCancelStatus() {
        return cancelStatus;
    }

    public void setCancelStatus(String cancelStatus) {
        this.cancelStatus = cancelStatus;
    }

    public String getRejectReason() {
        return rejectReason;
    }

    public void setRejectReason(String rejectReason) {
        this.rejectReason = rejectReason;
    }

    public Date getProcessedAt() {
        return processedAt;
    }

    public void setProcessedAt(Date processedAt) {
        this.processedAt = processedAt;
    }
}
