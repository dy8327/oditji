package com.project.oditji.subscription.service;

import java.util.List;

import com.project.oditji.subscription.vo.ContentWishItemVO;
import com.project.oditji.subscription.vo.SubscriptionCalculationResultVO;
import com.project.oditji.subscription.vo.SubscriptionSavedResultVO;

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

    /**
     * 계산 결과를 SUBSCRIPTION_RESULT에 저장하고 공유 링크에 쓸 resultId를 발급합니다.
     * memberNo가 null이면 비로그인 저장으로 처리합니다.
     */
    String saveResult(
            SubscriptionCalculationResultVO result,
            Long memberNo);

    /**
     * 공유 링크의 resultId로 저장된 계산 결과를 복원합니다.
     * 존재하지 않거나 만료된 resultId면 null을 반환합니다.
     */
    SubscriptionCalculationResultVO restoreResult(String resultId);

    /**
     * [비회원 공유 링크 임시 보관 추가]
     * 만료된 비회원 결과를 정리하고 삭제된 건수를 반환합니다.
     */
    int deleteExpiredResults();

    /**
     * [마이페이지 구독 계산 결과 모달 연동 추가]
     * 로그인 회원이 저장한 결과 개수를 반환합니다(마이페이지 '나의 활동' 카드용).
     */
    int getSavedResultCount(Long memberNo);

    /**
     * [마이페이지 구독 계산 결과 모달 연동 추가]
     * 로그인 회원이 저장한 결과를 최신순으로 조회합니다.
     */
    List<SubscriptionSavedResultVO> getSavedResultsByMember(Long memberNo);

    /**
     * [마이페이지 구독 계산 결과 모달 연동 추가]
     * resultId로 저장된 결과를 삭제합니다. memberNo가 소유한 결과가 아니면 삭제되지 않습니다.
     * 실제로 삭제됐으면 true를 반환합니다.
     */
    boolean removeSavedResult(String resultId, Long memberNo);
}
