package com.project.oditji.subscription.vo;

import java.util.ArrayList;
import java.util.List;

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

    /** 현재 적용 가능한 최저가(사용자가 선택한 할인 조건에 부합하는 할인가, 없으면 정가) */
    private Integer bestPrice;

    /** 할인이 적용됐을 때의 출처(카드사/통신사/멤버십명). 정가가 그대로 적용된 경우 null */
    private String discountSource;

    /** 할인이 적용됐을 때의 혜택 제목(OTT_DISCOUNT_INFO.TITLE). 정가가 그대로 적용된 경우 null */
    private String discountTitle;

    /**
     * 선택된 조합 중 이 플랫폼에서 볼 수 있는 콘텐츠 목록입니다.
     * (마이페이지 모달/공유 결과 화면에서 "이 OTT에는 어떤 작품이 있는지" 묶어서 보여줄 때 사용)
     */
    private List<ContentWishItemVO> contentList = new ArrayList<ContentWishItemVO>();

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

    public String getDiscountSource() {
        return discountSource;
    }

    public void setDiscountSource(String discountSource) {
        this.discountSource = discountSource;
    }

    public String getDiscountTitle() {
        return discountTitle;
    }

    public void setDiscountTitle(String discountTitle) {
        this.discountTitle = discountTitle;
    }

    /** 정가 대비 할인율(%). 할인이 적용되지 않았으면 null */
    public Integer getDiscountRate() {
        if (regularPrice != null && bestPrice != null
                && regularPrice > 0 && bestPrice < regularPrice) {
            double rate = ((double) (regularPrice - bestPrice) / regularPrice) * 100;
            return (int) Math.round(rate);
        }
        return null;
    }

    public List<ContentWishItemVO> getContentList() {
        return contentList;
    }

    public void setContentList(List<ContentWishItemVO> contentList) {
        this.contentList =
                contentList == null
                        ? new ArrayList<ContentWishItemVO>()
                        : contentList;
    }
}
