package com.project.oditji.goods.vo;

public class GoodsVO {

    private int productNo;
    private int businessNo;
    private int contentNo;
    private Integer actorNo;

    private String productName;
    private String productType;

    private int price;
    private int discountRate;
    private int stock;

    private String description;
    private String status;

    private String businessName;
    private String mainImage;

    public int getProductNo() {
        return productNo;
    }

    public void setProductNo(int productNo) {
        this.productNo = productNo;
    }

    public int getBusinessNo() {
        return businessNo;
    }

    public void setBusinessNo(int businessNo) {
        this.businessNo = businessNo;
    }

    public int getContentNo() {
        return contentNo;
    }

    public void setContentNo(int contentNo) {
        this.contentNo = contentNo;
    }

    public Integer getActorNo() {
        return actorNo;
    }

    public void setActorNo(Integer actorNo) {
        this.actorNo = actorNo;
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

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public int getDiscountRate() {
        return discountRate;
    }

    public void setDiscountRate(int discountRate) {
        this.discountRate = discountRate;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

        if (discountRate <= 0) {
            return price;
        }

        int discountAmount =
                price * discountRate / 100;

        return price - discountAmount;
    }
}