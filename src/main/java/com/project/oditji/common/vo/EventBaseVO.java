package com.project.oditji.common.vo;

import java.util.Date;

/**
 * 사용자 이벤트 조회, 사업자 이벤트 관리, 관리자 이벤트 관리에서 공통으로 사용하는
 * 이벤트 기본 정보입니다.
 *
 * 시작일과 종료일은 화면 용도에 따라 Date 또는 LocalDate를 사용하므로
 * 각 하위 VO에서 별도로 정의합니다.
 */
public abstract class EventBaseVO {

    private Long eventNo;
    private String title;
    private String description;
    private String bannerImage;
    private String status;
    private Date createdAt;

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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getBannerImage() {
        return bannerImage;
    }

    public void setBannerImage(String bannerImage) {
        this.bannerImage = bannerImage;
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
}
