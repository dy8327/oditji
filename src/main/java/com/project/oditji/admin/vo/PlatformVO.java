package com.project.oditji.admin.vo;

/**
 * OTT 플랫폼 VO (테이블: OTT_PLATFORM)
 * contentManage.jsp 의 플랫폼 등록/관리 팝업에서 사용
 */
public class PlatformVO {

    private Long platformNo;
    private String platformName;
    private String logoImage;
    private String siteUrl;
    private String isActive; // Y, N

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
}
