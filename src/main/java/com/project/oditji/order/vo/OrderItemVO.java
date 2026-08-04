package com.project.oditji.order.vo;

import com.project.oditji.common.vo.ProductSelectionVO;

public class OrderItemVO extends ProductSelectionVO {

    private Long orderItemNo;
    private Long orderNo;
    private String colorName;
    private String sizeName;
    private Integer businessNo;
    private Integer productPrice;
    private String status;

    /*
     * 주문 목록/상세 화면 출력용 상품 정보 (PRODUCT, PRODUCT_IMAGE, BUSINESS 조인)
     */
    private String productName;
    private String productType;
    private String mainImage;
    private String businessName;

    /*
     * =========================================================
     * [수정] 사용자 주문내역 버튼 노출 판단용 배송 상태
     *
     * CONFIRMED : 주문 확인중(취소 가능)
     * PREPARING : 배송 준비 중(취소/환불 버튼 숨김)
     * SHIPPING : 배송 중(취소/환불 버튼 숨김)
     * DELIVERED : 배송 완료(환불 가능)
     * =========================================================
     */
    private String deliveryStatus;

    /*
     * =========================================================
     * [부분 취소 처리 결과 표시용 필드 추가]
     *
     * 사용자 주문내역에서 최근 부분 취소 요청의 상태와
     * 사업자 반려 사유를 표시하기 위한 조회 전용 필드이다.
     * =========================================================
     */
    private String cancelRequestStatus;
    private String cancelType;
    private String cancelRejectReason;

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

    public Integer getBusinessNo() {
        return businessNo;
    }

    public void setBusinessNo(Integer businessNo) {
        this.businessNo = businessNo;
    }

    public Integer getProductPrice() {
        return productPrice;
    }

    public void setProductPrice(Integer productPrice) {
        this.productPrice = productPrice;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getProductType() {
        return productType;
    }

    public void setProductType(String productType) {
        this.productType = productType;
    }

    public String getMainImage() {
        return mainImage;
    }

    public void setMainImage(String mainImage) {
        this.mainImage = mainImage;
    }

    public String getDeliveryStatus() {
        return deliveryStatus;
    }

    public void setDeliveryStatus(String deliveryStatus) {
        this.deliveryStatus = deliveryStatus;
    }

    /** 결제 완료이면서 배송 상태가 주문 확인중인 상품만 주문 취소 가능 */
    public boolean isCancelEligible() {
        return "PAID".equals(status) && "CONFIRMED".equals(deliveryStatus);
    }

    /** 배송 완료 상품만 환불 신청 가능 */
    public boolean isRefundEligible() {
        return "DELIVERED".equals(status) && "DELIVERED".equals(deliveryStatus);
    }

    public String getBusinessName() {
        return businessName;
    }

    public void setBusinessName(String businessName) {
        this.businessName = businessName;
    }

    public String getCancelRequestStatus() {
        return cancelRequestStatus;
    }

    public void setCancelRequestStatus(String cancelRequestStatus) {
        this.cancelRequestStatus = cancelRequestStatus;
    }

    public String getCancelType() {
        return cancelType;
    }

    public void setCancelType(String cancelType) {
        this.cancelType = cancelType;
    }

    public String getCancelRejectReason() {
        return cancelRejectReason;
    }

    public void setCancelRejectReason(String cancelRejectReason) {
        this.cancelRejectReason = cancelRejectReason;
    }

    public long getItemTotalPrice() {

        int unitPrice = productPrice == null ? 0 : productPrice;
        Integer quantity = getQuantity();
        int itemQuantity = quantity == null ? 0 : quantity;

        return (long) unitPrice * itemQuantity;
    }
}