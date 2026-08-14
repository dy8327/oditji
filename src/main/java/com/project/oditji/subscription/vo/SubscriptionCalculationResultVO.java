package com.project.oditji.subscription.vo;

import java.util.ArrayList;
import java.util.List;

/**
 * OTT 구독 조합 계산기의 계산 결과입니다.
 *
 * 위시리스트에 담긴 모든 콘텐츠를 커버하는 조합 중 총액이 가장 저렴한 조합을 담습니다.
 * 가격 정보가 없는 플랫폼에서만 볼 수 있는 콘텐츠는 계산에서 제외하고
 * unresolvedItemList로 별도 안내합니다.
 */
public class SubscriptionCalculationResultVO {

    /** 최저 비용 조합에 포함된 플랫폼 목록 */
    private List<PlatformPriceVO> selectedPlatformList = new ArrayList<PlatformPriceVO>();

    /** 선택된 플랫폼들의 월 구독료 합계(할인 적용) */
    private int totalMonthlyPrice;

    /** 선택된 플랫폼들의 정가 합계(할인 미적용 기준, 절감 효과 비교용) */
    private int totalRegularMonthlyPrice;

    /** 모든 플랫폼을 각각 구독했을 때의 합계(비교용) */
    private int allPlatformMonthlyPrice;

    /** 가격 정보가 없는 플랫폼에서만 볼 수 있어 계산에서 제외된 콘텐츠 목록 */
    private List<ContentWishItemVO> unresolvedItemList = new ArrayList<ContentWishItemVO>();

    /**
     * 사용자가 이 계산에 사용한 위시리스트(담은 작품) 전체 목록입니다.
     * unresolvedItemList와 달리 가격 계산 성공 여부와 무관하게, 계산에 사용된
     * 모든 콘텐츠를 그대로 담아 "어떤 콘텐츠를 골라 계산했는지" 표시할 때 사용합니다.
     */
    private List<ContentWishItemVO> contentList = new ArrayList<ContentWishItemVO>();

    public List<PlatformPriceVO> getSelectedPlatformList() {
        return selectedPlatformList;
    }

    public void setSelectedPlatformList(List<PlatformPriceVO> selectedPlatformList) {
        this.selectedPlatformList =
                selectedPlatformList == null
                        ? new ArrayList<PlatformPriceVO>()
                        : selectedPlatformList;
    }

    public int getTotalMonthlyPrice() {
        return totalMonthlyPrice;
    }

    public void setTotalMonthlyPrice(int totalMonthlyPrice) {
        this.totalMonthlyPrice = totalMonthlyPrice;
    }

    public int getTotalRegularMonthlyPrice() {
        return totalRegularMonthlyPrice;
    }

    public void setTotalRegularMonthlyPrice(int totalRegularMonthlyPrice) {
        this.totalRegularMonthlyPrice = totalRegularMonthlyPrice;
    }

    public int getAllPlatformMonthlyPrice() {
        return allPlatformMonthlyPrice;
    }

    public void setAllPlatformMonthlyPrice(int allPlatformMonthlyPrice) {
        this.allPlatformMonthlyPrice = allPlatformMonthlyPrice;
    }

    public List<ContentWishItemVO> getUnresolvedItemList() {
        return unresolvedItemList;
    }

    public void setUnresolvedItemList(List<ContentWishItemVO> unresolvedItemList) {
        this.unresolvedItemList =
                unresolvedItemList == null
                        ? new ArrayList<ContentWishItemVO>()
                        : unresolvedItemList;
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
