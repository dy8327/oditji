package com.project.oditji.business.vo;

import java.util.Date;

public class GoodsManageVO {

    // PRODUCT
    private long productNo;
    private long businessNo;
    private long contentNo;

    /*
     * JSONL 콘텐츠 검색 결과를 상품 등록 요청까지 유지하기 위한 값입니다.
     * 실제 PRODUCT 저장 전 ContentService가 CONTENT_NO를 준비합니다.
     */
    private Long tmdbId;
    private String contentType;
    private Long tmdbActorId;

    // PRODUCT.ACTOR_NO는 NULL 허용
    private Long actorNo;

    private String productName;
    private String productType;
    private long price;
    private int discountRate;
    private int stock;
    private String description;
    private String status;
    private Date createdAt;

    // 조회용
    private String contentTitle;
    private String actorName;
    private String businessName;

    // PRODUCT_IMAGE
    private long imageNo;
    private String imagePath;
    private String isMain;

    // 인기 상품 조회용
    private int clickCount;

    public GoodsManageVO() {
    }

    public long getProductNo() {
        return productNo;
    }

    public void setProductNo(long productNo) {
        this.productNo = productNo;
    }

    public long getBusinessNo() {
        return businessNo;
    }

    public void setBusinessNo(long businessNo) {
        this.businessNo = businessNo;
    }

    public long getContentNo() {
        return contentNo;
    }

    public void setContentNo(long contentNo) {
        this.contentNo = contentNo;
    }

    public Long getTmdbId() {
        return tmdbId;
    }

    public void setTmdbId(Long tmdbId) {
        this.tmdbId = tmdbId;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public Long getTmdbActorId() {
        return tmdbActorId;
    }

    public void setTmdbActorId(Long tmdbActorId) {
        this.tmdbActorId = tmdbActorId;
    }

    public Long getActorNo() {
        return actorNo;
    }

    public void setActorNo(Long actorNo) {
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

    public long getPrice() {
        return price;
    }

    public void setPrice(long price) {
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

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public String getContentTitle() {
        return contentTitle;
    }

    public void setContentTitle(String contentTitle) {
        this.contentTitle = contentTitle;
    }

    public String getActorName() {
        return actorName;
    }

    public void setActorName(String actorName) {
        this.actorName = actorName;
    }

    public String getBusinessName() {
        return businessName;
    }

    public void setBusinessName(String businessName) {
        this.businessName = businessName;
    }

    public long getImageNo() {
        return imageNo;
    }

    public void setImageNo(long imageNo) {
        this.imageNo = imageNo;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public String getIsMain() {
        return isMain;
    }

    public void setIsMain(String isMain) {
        this.isMain = isMain;
    }

    public int getClickCount() {
        return clickCount;
    }

    public void setClickCount(int clickCount) {
        this.clickCount = clickCount;
    }    

    @Override
    public String toString() {
        return "GoodsManageVO{" +
                "productNo=" + productNo +
                ", businessNo=" + businessNo +
                ", contentNo=" + contentNo +
                ", tmdbId=" + tmdbId +
                ", contentType='" + contentType + '\'' +
                ", tmdbActorId=" + tmdbActorId +
                ", actorNo=" + actorNo +
                ", productName='" + productName + '\'' +
                ", productType='" + productType + '\'' +
                ", price=" + price +
                ", discountRate=" + discountRate +
                ", stock=" + stock +
                ", description='" + description + '\'' +
                ", status='" + status + '\'' +
                ", createdAt=" + createdAt +
                ", contentTitle='" + contentTitle + '\'' +
                ", actorName='" + actorName + '\'' +
                ", businessName='" + businessName + '\'' +
                ", imageNo=" + imageNo +
                ", imagePath='" + imagePath + '\'' +
                ", isMain='" + isMain + '\'' +
                '}';
    }
}