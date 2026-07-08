package com.project.oditji.member.vo;

import java.util.Date;

public class PlatformVO {

    private Long platformNo;
    private String platformName;
    private String logoImage;
    private String siteUrl;
    private String isActive;
    private Date createdAt;
    private Date updatedAt;

    public Long getPlatformNo() {
        return platformNo;
    }

    public void setPlatformNo(Long platformNo) {
        this.platformNo = platformNo;
    }

    public String getPlatformName() {
        return platformName;
    }

    public void setPlatformName(String platformName) {
        this.platformName = platformName;
    }

    public String getLogoImage() {
        return logoImage;
    }

    public void setLogoImage(String logoImage) {
        this.logoImage = logoImage;
    }

    public String getSiteUrl() {
        return siteUrl;
    }

    public void setSiteUrl(String siteUrl) {
        this.siteUrl = siteUrl;
    }

    public String getIsActive() {
        return isActive;
    }

    public void setIsActive(String isActive) {
        this.isActive = isActive;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }
}