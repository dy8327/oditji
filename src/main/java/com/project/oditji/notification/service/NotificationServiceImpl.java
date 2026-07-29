package com.project.oditji.notification.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.project.oditji.notification.dao.NotificationDAO;
import com.project.oditji.notification.vo.NotificationContextVO;
import com.project.oditji.notification.vo.NotificationVO;

/**
 * 업무 처리 결과를 수신 회원별 알림으로 저장하고 읽음 상태를 관리합니다.
 */
@Service
public class NotificationServiceImpl implements NotificationService {

    private static final int HEADER_NOTIFICATION_LIMIT = 20;

    private final NotificationDAO notificationDAO;

    public NotificationServiceImpl(NotificationDAO notificationDAO) {
        this.notificationDAO = notificationDAO;
    }

    @Override
    @Transactional(readOnly = true)
    public NotificationContextVO getUnreadContext(Long memberNo) {

        validateMemberNo(memberNo);

        int unreadCount = notificationDAO.selectUnreadNotificationCount(
                memberNo);

        List<NotificationVO> notificationList =
                notificationDAO.selectUnreadNotificationList(
                        memberNo,
                        HEADER_NOTIFICATION_LIMIT);

        return new NotificationContextVO(
                unreadCount,
                notificationList);
    }

    @Override
    @Transactional
    public boolean markAsRead(Long notificationNo, Long memberNo) {

        validateMemberNo(memberNo);

        if (notificationNo == null || notificationNo <= 0L) {
            throw new IllegalArgumentException("올바르지 않은 알림 번호입니다.");
        }

        return notificationDAO.updateNotificationRead(
                notificationNo,
                memberNo) > 0;
    }

    @Override
    @Transactional
    public int markAllAsRead(Long memberNo) {

        validateMemberNo(memberNo);
        return notificationDAO.updateAllNotificationRead(memberNo);
    }

    @Override
    @Transactional
    public void createForMember(
            Long memberNo,
            String notificationType,
            String title,
            String message,
            String linkUrl,
            String referenceType,
            Long referenceNo) {

        if (memberNo == null || memberNo <= 0L) {
            return;
        }

        NotificationVO notification = createNotification(
                notificationType,
                title,
                message,
                linkUrl,
                referenceType,
                referenceNo);

        notification.setReceiverMemberNo(memberNo);
        notificationDAO.insertMemberNotification(notification);
    }

    @Override
    @Transactional
    public void createForAdmins(
            String notificationType,
            String title,
            String message,
            String linkUrl,
            String referenceType,
            Long referenceNo) {

        notificationDAO.insertAdminNotification(
                createNotification(
                        notificationType,
                        title,
                        message,
                        linkUrl,
                        referenceType,
                        referenceNo));
    }

    @Override
    @Transactional
    public void createForBusiness(
            Long businessNo,
            String notificationType,
            String title,
            String message,
            String linkUrl,
            String referenceType,
            Long referenceNo) {

        if (businessNo == null || businessNo <= 0L) {
            return;
        }

        notificationDAO.insertBusinessNotification(
                businessNo,
                createNotification(
                        notificationType,
                        title,
                        message,
                        linkUrl,
                        referenceType,
                        referenceNo));
    }

    @Override
    @Transactional
    public void createForProductOwner(
            Long productNo,
            String notificationType,
            String title,
            String message,
            String linkUrl,
            String referenceType,
            Long referenceNo) {

        if (productNo == null || productNo <= 0L) {
            return;
        }

        Long memberNo = notificationDAO.selectProductOwnerMemberNo(productNo);

        createForMember(
                memberNo,
                notificationType,
                title,
                message,
                linkUrl,
                referenceType,
                referenceNo);
    }

    @Override
    @Transactional
    public void createForEventOwner(
            Long eventNo,
            String notificationType,
            String title,
            String message,
            String linkUrl,
            String referenceType,
            Long referenceNo) {

        if (eventNo == null || eventNo <= 0L) {
            return;
        }

        Long memberNo = notificationDAO.selectEventOwnerMemberNo(eventNo);

        createForMember(
                memberNo,
                notificationType,
                title,
                message,
                linkUrl,
                referenceType,
                referenceNo);
    }

    @Override
    @Transactional
    public void createForOrderBusinesses(
            Long orderNo,
            String notificationType,
            String title,
            String message,
            String linkUrl,
            String referenceType,
            Long referenceNo) {

        if (orderNo == null || orderNo <= 0L) {
            return;
        }

        notificationDAO.insertOrderBusinessNotifications(
                orderNo,
                createNotification(
                        notificationType,
                        title,
                        message,
                        linkUrl,
                        referenceType,
                        referenceNo));
    }

    @Override
    @Transactional
    public void createForCancelGroupBusinesses(
            Long cancelGroupNo,
            String notificationType,
            String title,
            String message,
            String linkUrl,
            String referenceType,
            Long referenceNo) {

        if (cancelGroupNo == null || cancelGroupNo <= 0L) {
            return;
        }

        notificationDAO.insertCancelGroupBusinessNotifications(
                cancelGroupNo,
                createNotification(
                        notificationType,
                        title,
                        message,
                        linkUrl,
                        referenceType,
                        referenceNo));
    }

    private NotificationVO createNotification(
            String notificationType,
            String title,
            String message,
            String linkUrl,
            String referenceType,
            Long referenceNo) {

        NotificationVO notification = new NotificationVO();
        notification.setNotificationType(normalizeRequired(
                notificationType,
                "알림 유형"));
        notification.setTitle(normalizeRequired(title, "알림 제목"));
        notification.setMessage(normalizeRequired(message, "알림 내용"));
        notification.setLinkUrl(normalizeOptional(linkUrl));
        notification.setReferenceType(normalizeOptional(referenceType));
        notification.setReferenceNo(referenceNo);
        notification.setIsRead("N");
        return notification;
    }

    private String normalizeRequired(String value, String fieldName) {

        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + "이 없습니다.");
        }

        return value.trim();
    }

    private String normalizeOptional(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private void validateMemberNo(Long memberNo) {
        if (memberNo == null || memberNo <= 0L) {
            throw new IllegalArgumentException("로그인 회원 정보를 확인할 수 없습니다.");
        }
    }
}
