package com.project.oditji.refund.vo;

import java.util.Date;

/**
 * 주문상품 취소 요청 정보를 저장하고 화면에 전달하는 VO.
 */
public class OrderCancelRefundVO {

    private Long cancelNo;
    private Long orderItemNo;
    private Long orderNo;
    private Long memberNo;
    private Long businessNo;
    private Long productNo;
    private String productName;
    private Integer productPrice;
    private Integer quantity;
    private Long cancelAmount;
    private String reason;
    private String status;
    private Date createdAt;
    private Date processedAt;
    private String rejectReason;

    /*
     * =========================================================
     * [전체/부분 취소 구분 기능 추가]
     *
     * cancelType : FULL(전체 취소), PARTIAL(상품별 부분 취소)
     * cancelGroupNo : 하나의 전체 취소 요청을 묶는 그룹 번호
     * refundAmount : 해당 요청의 환불 예정 금액
     * itemCount : 사업자 화면에 표시할 요청 상품 수
     * =========================================================
     */
    private String cancelType;
    private Long cancelGroupNo;
    private Long refundAmount;
    private Integer itemCount;

    /* [추가] 사용자 취소/환불 내역 조회 전용 필드 */
    private String historyType;

    public Long getCancelNo() {
        return cancelNo;
    }

    public void setCancelNo(Long cancelNo) {
        this.cancelNo = cancelNo;
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

    public Long getMemberNo() {
        return memberNo;
    }

    public void setMemberNo(Long memberNo) {
        this.memberNo = memberNo;
    }

    public Long getBusinessNo() {
        return businessNo;
    }

    public void setBusinessNo(Long businessNo) {
        this.businessNo = businessNo;
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

    public Long getCancelAmount() {
        return cancelAmount;
    }

    public void setCancelAmount(Long cancelAmount) {
        this.cancelAmount = cancelAmount;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getProcessedAt() {
        return processedAt;
    }

    public void setProcessedAt(Date processedAt) {
        this.processedAt = processedAt;
    }

    public String getRejectReason() {
        return rejectReason;
    }

    public void setRejectReason(String rejectReason) {
        this.rejectReason = rejectReason;
    }

    public String getCancelType() {
        return cancelType;
    }

    public void setCancelType(String cancelType) {
        this.cancelType = cancelType;
    }

    public Long getCancelGroupNo() {
        return cancelGroupNo;
    }

    public void setCancelGroupNo(Long cancelGroupNo) {
        this.cancelGroupNo = cancelGroupNo;
    }

    public Long getRefundAmount() {
        return refundAmount;
    }

    public void setRefundAmount(Long refundAmount) {
        this.refundAmount = refundAmount;
    }

    public String getHistoryType() {
        return historyType;
    }

    public void setHistoryType(String historyType) {
        this.historyType = historyType;
    }

    public Integer getItemCount() {
        return itemCount;
    }

    public void setItemCount(Integer itemCount) {
        this.itemCount = itemCount;
    }
}