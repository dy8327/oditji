package com.project.oditji.order.vo;

public class OrderItemVO {

    private Long orderItemNo;
    private Long orderNo;
    private Integer productNo;
    private Integer businessNo;
    private Integer productPrice;
    private Integer quantity;
    private String status;

    /*
     * 주문 목록/상세 화면 출력용 상품 정보 (PRODUCT, PRODUCT_IMAGE, BUSINESS 조인)
     */
    private String productName;
    private String productType;
    private String mainImage;
    private String businessName;

    public OrderItemVO() {
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

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
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

    public String getBusinessName() {
        return businessName;
    }

    public void setBusinessName(String businessName) {
        this.businessName = businessName;
    }

    public long getItemTotalPrice() {

        int unitPrice = productPrice == null ? 0 : productPrice;
        int itemQuantity = quantity == null ? 0 : quantity;

        return (long) unitPrice * itemQuantity;
    }
}
