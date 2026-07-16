package com.project.oditji.business.vo;

import java.time.LocalDate;
import java.util.Date;

public class EventManageVO {

    // EVENT
    private long eventNo;
    private String title;
    private String bannerImage;
    private LocalDate startDate;
    private LocalDate endDate;
    private String status;
    private Date createdAt;

    // EVENT_PRODUCT
    private long eventProdNo;
    private Long productNo;
    private int eventDiscountRate;

    // 조회 화면 표시용
    private String productName;

    /*
     * 이벤트 등록 권한 및 연결 상품 소유 여부 검증용이다.
     * EVENT 테이블에는 BUSINESS_NO 컬럼이 없으므로
     * EVENT INSERT 대상에는 포함하지 않는다.
     */
    private long businessNo;

    public EventManageVO() {
    }

    public long getEventNo() {
        return eventNo;
    }

    public void setEventNo(long eventNo) {
        this.eventNo = eventNo;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBannerImage() {
        return bannerImage;
    }

    public void setBannerImage(String bannerImage) {
        this.bannerImage = bannerImage;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
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

    public long getEventProdNo() {
        return eventProdNo;
    }

    public void setEventProdNo(long eventProdNo) {
        this.eventProdNo = eventProdNo;
    }

    public Long getProductNo() {
        return productNo;
    }

    public void setProductNo(Long productNo) {
        this.productNo = productNo;
    }

    public int getEventDiscountRate() {
        return eventDiscountRate;
    }

    public void setEventDiscountRate(int eventDiscountRate) {
        this.eventDiscountRate = eventDiscountRate;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public long getBusinessNo() {
        return businessNo;
    }

    public void setBusinessNo(long businessNo) {
        this.businessNo = businessNo;
    }

    @Override
    public String toString() {
        return "EventManageVO{" +
                "eventNo=" + eventNo +
                ", title='" + title + '\'' +
                ", bannerImage='" + bannerImage + '\'' +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", status='" + status + '\'' +
                ", createdAt=" + createdAt +
                ", eventProdNo=" + eventProdNo +
                ", productNo=" + productNo +
                ", eventDiscountRate=" + eventDiscountRate +
                ", productName='" + productName + '\'' +
                ", businessNo=" + businessNo +
                '}';
    }
}
