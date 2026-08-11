package com.project.oditji.subscription.service;

import java.util.List;

import com.project.oditji.subscription.vo.ContentWishItemVO;
import com.project.oditji.subscription.vo.SubscriptionCalculationResultVO;

/**
 * "보고 싶은 작품 목록"을 가장 저렴하게 커버하는 OTT 구독 조합을 계산합니다.
 */
public interface SubscriptionCalculatorService {

    /**
     * 위시리스트에 담긴 콘텐츠를 모두 볼 수 있으면서 월 구독료 합계가
     * 가장 저렴한 플랫폼 조합을 계산합니다.
     *
     * telecomCode/cardCompany/membershipName은 사용자가 실제로 보유한 할인 조건이며,
     * 이 조건과 일치하는 할인만 가격 계산에 반영됩니다(모두 null이면 정가 기준).
     *
     * 가격 정보가 없는 플랫폼에서만 볼 수 있는 콘텐츠는 계산에서 제외하고
     * 결과의 unresolvedItemList로 안내합니다.
     */
    SubscriptionCalculationResultVO calculate(
            List<ContentWishItemVO> wishItemList,
            String telecomCode,
            String cardCompany,
            String membershipName);
}
