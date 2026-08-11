package com.project.oditji.subscription.vo;

/**
 * OTT_DISCOUNT_INFO 기준으로 산출한 플랫폼별 현재 최저 구독 가격입니다.
 *
 * OTT 구독 조합 계산기가 "이 플랫폼을 구독하면 한 달에 얼마인지"를
 * 판단할 때 사용합니다.
 */
public class PlatformPriceVO {

    /** OTT_DISCOUNT_INFO.PLATFORM_CODE (예: NETFLIX) */
    private String platformCode;

    /** OTT_DISCOUNT_INFO.PLATFORM_NAME (한글 표시용, 예: 넷플릭스) */
    private String platformName;

    /** 정가 */
    private Integer regularPrice;

    /** 현재 적용 가능한 최저가(할인가 vs 정가 중 낮은 값) */
    private Integer bestPrice;

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

    public Integer getRegularPrice() {
        return regularPrice;
    }

    public void setRegularPrice(Integer regularPrice) {
        this.regularPrice = regularPrice;
    }

    public Integer getBestPrice() {
        return bestPrice;
    }

    public void setBestPrice(Integer bestPrice) {
        this.bestPrice = bestPrice;
    }
}
