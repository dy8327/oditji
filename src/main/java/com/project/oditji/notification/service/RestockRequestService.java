package com.project.oditji.notification.service;

/**
 * 사용자의 품절 상품 재입고 알림 신청을 관리합니다.
 */
public interface RestockRequestService {

    /**
     * 상품 전체 또는 특정 옵션의 재입고 알림 신청 여부를 확인합니다.
     *
     * @param memberNo  회원 번호
     * @param productNo 상품 번호
     * @param optionNo  옵션 번호
     *                  NULL이면 상품 전체 신청을 의미합니다.
     */
    boolean isRequested(Long memberNo, Long productNo, Long optionNo);

    /**
     * 상품 전체 또는 특정 옵션 재입고 알림을 신청합니다.
     *
     * @param memberNo  회원 번호
     * @param productNo 상품 번호
     * @param optionNo  옵션 번호
     *                  NULL이면 상품 전체 신청입니다.
     */
    void requestRestockNotification(Long memberNo, Long productNo, Long optionNo);

    /**
     * 상품 전체 또는 특정 옵션 재입고 알림 신청을 취소합니다.
     */
    void cancelRestockNotification(Long memberNo, Long productNo, Long optionNo);
}
