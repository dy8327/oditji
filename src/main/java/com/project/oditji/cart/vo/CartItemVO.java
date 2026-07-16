package com.project.oditji.cart.vo;

import java.util.Date;

public class CartItemVO {

    private Long cartItemNo;
    private Long cartNo;
    private Integer productNo;
    private Integer quantity;
    private Date createdAt;

    /*
     * 장바구니 화면 출력 및 검증용 상품 정보
     */
    private String productName;
    private String productType;
    private Integer price;
    private Integer discountRate;
    private Integer stock;
    private String status;
    private String businessName;
    private String mainImage;

    public CartItemVO() {
    }

    public Long getCartItemNo() {
        return cartItemNo;
    }

    public void setCartItemNo(Long cartItemNo) {
        this.cartItemNo = cartItemNo;
    }

    public Long getCartNo() {
        return cartNo;
    }

    public void setCartNo(Long cartNo) {
        this.cartNo = cartNo;
    }

    public Integer getProductNo() {
        return productNo;
    }

    public void setProductNo(Integer productNo) {
        this.productNo = productNo;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
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

    public Integer getPrice() {
        return price;
    }

    public void setPrice(Integer price) {
        this.price = price;
    }

    public Integer getDiscountRate() {
        return discountRate;
    }

    public void setDiscountRate(Integer discountRate) {
        this.discountRate = discountRate;
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getBusinessName() {
        return businessName;
    }

    public void setBusinessName(String businessName) {
        this.businessName = businessName;
    }

    public String getMainImage() {
        return mainImage;
    }

    public void setMainImage(String mainImage) {
        this.mainImage = mainImage;
    }

    public int getDiscountPrice() {

        int originalPrice = price == null
                ? 0
                : price;

        int rate = discountRate == null
                ? 0
                : discountRate;

        if (rate < 0) {
            rate = 0;
        }

        if (rate > 100) {
            rate = 100;
        }

        return originalPrice * (100 - rate) / 100;
    }

    public long getItemTotalPrice() {

        int itemQuantity = quantity == null
                ? 0
                : quantity;

        return (long) getDiscountPrice()
                * itemQuantity;
    }

    public boolean isAvailable() {

        int currentStock = stock == null
                ? 0
                : stock;

        int currentQuantity = quantity == null
                ? 0
                : quantity;

        return "APPROVED".equals(status)
                && currentStock > 0
                && currentQuantity >= 1
                && currentQuantity <= currentStock;
    }

    public boolean isSoldOut() {

        int currentStock = stock == null
                ? 0
                : stock;

        return currentStock <= 0;
    }
}