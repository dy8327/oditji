package com.project.oditji.notification.service;

import com.project.oditji.notification.vo.NotificationContextVO;

/**
 * 일반 회원, 사업자, 관리자 공통 알림 기능을 제공합니다.
 */
public interface NotificationService {

        NotificationContextVO getUnreadContext(Long memberNo);

        boolean markAsRead(Long notificationNo, Long memberNo);

        int markAllAsRead(Long memberNo);

        void createForMember(
                        Long memberNo,
                        String notificationType,
                        String title,
                        String message,
                        String linkUrl,
                        String referenceType,
                        Long referenceNo);

        void createForAdmins(
                        String notificationType,
                        String title,
                        String message,
                        String linkUrl,
                        String referenceType,
                        Long referenceNo);

        void createForBusiness(
                        Long businessNo,
                        String notificationType,
                        String title,
                        String message,
                        String linkUrl,
                        String referenceType,
                        Long referenceNo);

        void createForProductOwner(
                        Long productNo,
                        String notificationType,
                        String title,
                        String message,
                        String linkUrl,
                        String referenceType,
                        Long referenceNo);

        void createForEventOwner(
                        Long eventNo,
                        String notificationType,
                        String title,
                        String message,
                        String linkUrl,
                        String referenceType,
                        Long referenceNo);

        void createForOrderBusinesses(
                        Long orderNo,
                        String notificationType,
                        String title,
                        String message,
                        String linkUrl,
                        String referenceType,
                        Long referenceNo);

        void createForCancelGroupBusinesses(
                        Long cancelGroupNo,
                        String notificationType,
                        String title,
                        String message,
                        String linkUrl,
                        String referenceType,
                        Long referenceNo);

        /**
         * [재고 부족 알림 추가]
         * 주문 후 상품 재고가 기준 수량 이하이면 사업자에게 알림을 생성합니다.
         */
        void createLowStockNotificationIfNeeded(Long productNo);

        /**
         * [재입고 알림 추가]
         * 품절 상품이 다시 입고되면 대기 중인 신청 회원에게 알림을 생성합니다.
         */
        void createRestockNotifications(Long productNo, String productName);

        /**
         * [옵션별 재입고 알림 추가]
         * 특정 옵션의 재고가 0개에서 1개 이상으로 증가하면
         * 해당 OPTION_NO의 재입고 신청 회원에게 알림을 생성합니다.
         */
        void createOptionRestockNotifications(
                        Long productNo,
                        Long optionNo,
                        String productName,
                        String colorName,
                        String sizeName);
}
