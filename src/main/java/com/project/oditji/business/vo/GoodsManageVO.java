package com.project.oditji.business.vo;

import java.util.Date;

public class GoodsManageVO {

    /* =========================
       PRODUCT 테이블
    ========================= */

    private long productNo;
    private long businessNo;
    private long contentNo;

    // 배우는 선택 사항이므로 Long
    private Long actorNo;

    private String productName;
    private String productType;

    // 입력값이 비어 있을 수 있으므로 래퍼 타입 사용
    private Long price;
    private Integer discountRate;
    private Integer stock;

    private String description;
    private String status;
    private Date createdAt;

    /* =========================
       PRODUCT_IMAGE 테이블
    ========================= */

    private long imageNo;
    private String imagePath;
    private String isMain;

    /* =========================
       JOIN 및 화면 출력용
    ========================= */

    private String businessName;
    private String contentTitle;
    private String actorName;

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

    public Long getPrice() {
        return price;
    }

    public void setPrice(Long price) {
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

    public String getBusinessName() {
        return businessName;
    }

    public void setBusinessName(String businessName) {
        this.businessName = businessName;
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
}