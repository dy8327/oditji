package com.project.oditji.notification.service;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.project.oditji.notification.dao.NotificationDAO;
import com.project.oditji.notification.vo.NotificationContextVO;
import com.project.oditji.notification.vo.NotificationVO;

/**
 * 서비스 처리 결과를 수신 회원별 알림으로 저장하고 읽음 상태를 관리합니다.
 */
@Service
public class NotificationServiceImpl implements NotificationService {

    private static final int HEADER_NOTIFICATION_LIMIT = 20;

    private static final int LOW_STOCK_THRESHOLD = 5;
    private static final String NOTIFICATION_TYPE_LOW_STOCK = "LOW_STOCK";
    private static final String NOTIFICATION_TYPE_RESTOCKED = "RESTOCKED";
    private static final String REFERENCE_TYPE_PRODUCT = "PRODUCT";

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

        List<NotificationVO> notificationList = notificationDAO.selectUnreadNotificationList(
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

        createForMemberInternal(
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

        createForMemberInternal(
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

        createForMemberInternal(
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

    @Override
    @Transactional
    public void createLowStockNotificationIfNeeded(Long productNo) {

        if (productNo == null || productNo <= 0L) {
            return;
        }

        Map<String, Object> productInfo = notificationDAO.selectProductStockNotificationInfo(productNo);

        if (productInfo == null || productInfo.isEmpty()) {
            return;
        }

        Long businessNo = toLong(productInfo.get("BUSINESS_NO"));
        Integer stock = toInteger(productInfo.get("STOCK"));
        String productName = String.valueOf(productInfo.get("PRODUCT_NAME"));

        if (businessNo == null || stock == null || stock > LOW_STOCK_THRESHOLD) {
            return;
        }

        /*
         * [재고 부족 알림 추가]
         * 같은 상품에 대한 미읽음 LOW_STOCK 알림이 이미 있으면
         * 주문마다 동일 알림이 반복 생성되지 않도록 건너뜁니다.
         */
        int duplicateCount = notificationDAO.countUnreadBusinessProductNotification(
                businessNo,
                NOTIFICATION_TYPE_LOW_STOCK,
                productNo);

        if (duplicateCount > 0) {
            return;
        }

        createForBusiness(
                businessNo,
                NOTIFICATION_TYPE_LOW_STOCK,
                "상품 재고 부족",
                productName + " 상품의 남은 재고가 " + stock + "개입니다.",
                "/business/product/list",
                REFERENCE_TYPE_PRODUCT,
                productNo);
    }

    @Override
    @Transactional
    public void createRestockNotifications(Long productNo, String productName) {

        if (productNo == null || productNo <= 0L) {
            return;
        }

        String normalizedProductName = productName == null || productName.isBlank()
                ? "신청하신 상품"
                : productName.trim();

        NotificationVO notification = createNotification(
                NOTIFICATION_TYPE_RESTOCKED,
                "상품 재입고 안내",
                normalizedProductName + " 상품이 재입고되었습니다.",
                "/goods/goodsDetail/" + productNo,
                REFERENCE_TYPE_PRODUCT,
                productNo);

        /*
         * [재입고 알림 추가]
         * WAITING 상태의 신청자에게만 알림을 넣은 뒤
         * 같은 트랜잭션에서 신청 상태를 NOTIFIED로 변경합니다.
         */
        int insertedCount = notificationDAO.insertRestockMemberNotifications(
                productNo,
                notification);

        if (insertedCount > 0) {
            notificationDAO.updateRestockRequestsNotified(productNo);
        }
    }

    private Long toLong(Object value) {
        if (value instanceof Number number) {
            return number.longValue();
        }
        return null;
    }

    private Integer toInteger(Object value) {
        if (value instanceof Number number) {
            return number.intValue();
        }
        return null;
    }

    private void createForMemberInternal(
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

    @Override
    @Transactional
    public void createOptionRestockNotifications(
            Long productNo,
            Long optionNo,
            String productName,
            String colorName,
            String sizeName) {

        if (productNo == null || productNo <= 0L
                || optionNo == null || optionNo <= 0L) {

            return;
        }

        String normalizedProductName = productName == null || productName.isBlank()
                ? "신청하신 상품"
                : productName.trim();

        String normalizedColorName = colorName == null || colorName.isBlank()
                ? "-"
                : colorName.trim();

        String normalizedSizeName = sizeName == null || sizeName.isBlank()
                ? "-"
                : sizeName.trim();

        NotificationVO notification = createNotification(
                NOTIFICATION_TYPE_RESTOCKED,
                "상품 재입고 안내",
                normalizedProductName
                        + "의 "
                        + normalizedColorName
                        + " / "
                        + normalizedSizeName
                        + " 옵션이 재입고되었습니다.",
                "/goods/goodsDetail/" + productNo,
                REFERENCE_TYPE_PRODUCT,
                productNo);

        /*
         * [옵션별 재입고 알림 추가]
         * 해당 OPTION_NO를 WAITING 상태로 신청한 회원에게만
         * 재입고 알림을 생성합니다.
         */
        int insertedCount = notificationDAO.insertOptionRestockMemberNotifications(
                productNo,
                optionNo,
                notification);

        /*
         * 실제 알림이 생성된 신청만 NOTIFIED로 변경합니다.
         */
        if (insertedCount > 0) {

            notificationDAO.updateOptionRestockRequestsNotified(
                    productNo,
                    optionNo);
        }
    }
}
