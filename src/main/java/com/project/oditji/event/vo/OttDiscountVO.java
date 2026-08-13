package com.project.oditji.event.vo;

import java.time.LocalDateTime;

public class OttDiscountVO {

    private Long discountId;
    private String platformCode;
    private String platformName;
    private String category;
    private String title;
    private String discountSummary;
    private String description;
    private String cardOrCompany;
    private String targetUrl;
    private String badgeText;
    private String startDate;
    private String endDate;
    private String isActive;

    // [SonarQube] 구형 java.util.Date 대신 Java 8+ 날짜/시간 API를 사용합니다.
    private LocalDateTime createdAt;

    // 추가된 정가 / 할인가 필드
    private Integer regularPrice;
    private Integer discountPrice;

    public Long getDiscountId() {
        return discountId;
    }

    public void setDiscountId(Long discountId) {
        this.discountId = discountId;
    }

    public String getPlatformCode() {
        return platformCode;
    }

    public void setPlatformCode(String platformCode) {
        this.platformCode = platformCode;
    }

    public String getPlatformName() {
        return platformName;
    }

    public void setPlatformName(String platformName) {
        this.platformName = platformName;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDiscountSummary() {
        return discountSummary;
    }

    public void setDiscountSummary(String discountSummary) {
        this.discountSummary = discountSummary;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCardOrCompany() {
        return cardOrCompany;
    }

    public void setCardOrCompany(String cardOrCompany) {
        this.cardOrCompany = cardOrCompany;
    }

    public String getTargetUrl() {
        return targetUrl;
    }

    public void setTargetUrl(String targetUrl) {
        this.targetUrl = targetUrl;
    }

    public String getBadgeText() {
        return badgeText;
    }

    public void setBadgeText(String badgeText) {
        this.badgeText = badgeText;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public String getIsActive() {
        return isActive;
    }

    public void setIsActive(String isActive) {
        this.isActive = isActive;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Integer getRegularPrice() {
        return regularPrice;
    }

    public void setRegularPrice(Integer regularPrice) {
        this.regularPrice = regularPrice;
    }

    public Integer getDiscountPrice() {
        return discountPrice;
    }

    public void setDiscountPrice(Integer discountPrice) {
        this.discountPrice = discountPrice;
    }

    // 할인율 계산 Getter (정가 및 할인가가 존재하고 정가가 0보다 클 때 계산)
    public Integer getDiscountRate() {
        if (regularPrice != null && discountPrice != null && regularPrice > 0 && discountPrice < regularPrice) {
            double rate = ((double) (regularPrice - discountPrice) / regularPrice) * 100;
            return (int) Math.round(rate);
        }
        return null;
    }
}