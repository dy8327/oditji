package com.project.oditji.common.vo;

/**
 * 상품 목록, 장바구니, 주문서에서 공통으로 사용하는 상품 판매 정보입니다.
 */
public abstract class ProductSaleInfoVO extends ProductOptionSelectionVO {

    private static final long serialVersionUID = 1L;

    private String productName;
    private String productType;
    private Integer price;
    private Integer discountRate;
    private Integer stock;
    private String status;
    private String businessName;
    private String mainImage;

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
        int originalPrice = price == null ? 0 : price;
        int rate = discountRate == null ? 0 : Math.clamp(discountRate, 0, 100);

        return originalPrice * (100 - rate) / 100;
    }

    public long getItemTotalPrice() {
        Integer quantity = getQuantity();
        int itemQuantity = quantity == null ? 0 : quantity;

        return (long) getDiscountPrice() * itemQuantity;
    }

    public boolean isAvailable() {
        int currentStock = stock == null ? 0 : stock;
        Integer quantity = getQuantity();
        int currentQuantity = quantity == null ? 0 : quantity;

        return "APPROVED".equals(status)
                && currentStock > 0
                && currentQuantity >= 1
                && currentQuantity <= currentStock;
    }

    public boolean isSoldOut() {
        return stock == null || stock <= 0;
    }
}
