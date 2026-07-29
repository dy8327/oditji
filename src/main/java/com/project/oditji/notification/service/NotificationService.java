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
}
