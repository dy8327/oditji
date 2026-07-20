package com.project.oditji.event.vo;

import java.util.Date;
import java.util.List;

public class EventVO {

    // EVENT
    private Long eventNo;
    private String title;
    private String bannerImage;
    private Date startDate;
    private Date endDate;
    private String status;
    private Date createdAt;


    // 이벤트 상품 목록
    private List<EventProductVO> products;


    public Long getEventNo() {
        return eventNo;
    }

    public void setEventNo(Long eventNo) {
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

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
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

    public List<EventProductVO> getProducts() {
        return products;
    }

    public void setProducts(List<EventProductVO> products) {
        this.products = products;
    }
}