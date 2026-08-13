package com.project.oditji.subscription.vo;

import java.util.ArrayList;
import java.util.List;

/**
 * OTT 구독 조합 계산기 "/api/subscription/calculate" 요청 본문입니다.
 *
 * 위시리스트뿐 아니라 사용자가 화면에서 선택한 할인 조건(통신사/카드사/멤버십)을 함께
 * 받아, 실제로 적용 가능한 할인만 반영한 "현실적인" 최저가를 계산할 수 있게 합니다.
 * 세 조건 모두 선택하지 않았다면(null 또는 빈 문자열) 정가만 적용됩니다.
 */
public class SubscriptionCalculateRequestVO {

    private List<ContentWishItemVO> wishItemList = new ArrayList<ContentWishItemVO>();

    /** 선택한 통신사 (예: SKT, KT, LGU+). 미선택 시 null */
    private String telecomCode;

    /** 선택한 카드사 (OTT_DISCOUNT_INFO.CARD_OR_COMPANY 값, 예: 현대카드). 미선택 시 null */
    private String cardCompany;

    /** 선택한 멤버십 제공사 (OTT_DISCOUNT_INFO.CARD_OR_COMPANY 값, 예: 네이버). 미선택 시 null */
    private String membershipName;

    public List<ContentWishItemVO> getWishItemList() {
        return wishItemList;
    }

    public void setWishItemList(List<ContentWishItemVO> wishItemList) {
        this.wishItemList =
                wishItemList == null
                        ? new ArrayList<ContentWishItemVO>()
                        : wishItemList;
    }

    public String getTelecomCode() {
        return telecomCode;
    }

    public void setTelecomCode(String telecomCode) {
        this.telecomCode = telecomCode;
    }

    public String getCardCompany() {
        return cardCompany;
    }

    public void setCardCompany(String cardCompany) {
        this.cardCompany = cardCompany;
    }

    public String getMembershipName() {
        return membershipName;
    }

    public void setMembershipName(String membershipName) {
        this.membershipName = membershipName;
    }
}
