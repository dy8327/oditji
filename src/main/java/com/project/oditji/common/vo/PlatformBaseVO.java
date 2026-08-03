package com.project.oditji.common.vo;

/**
 * 콘텐츠 조회, 회원 OTT 설정, 관리자 플랫폼 관리에서 공통으로 사용하는
 * OTT 플랫폼 기본 정보입니다.
 *
 * 모듈별 platformNo 타입과 생성·수정일 사용 여부가 다르므로
 * 해당 값은 각 하위 VO에서 별도로 정의합니다.
 */
public abstract class PlatformBaseVO {

    private String platformName;
    private String logoImage;
    private String siteUrl;
    private String isActive;

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
