package com.project.oditji.notification.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.project.oditji.notification.dao.NotificationDAO;
import com.project.oditji.notification.vo.NotificationContextVO;
import com.project.oditji.notification.vo.NotificationSettingItemVO;
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
    // [SonarQube] 반복되는 알림 타입/카테고리 문자열을 상수로 관리합니다.
    private static final String NOTIFICATION_TYPE_CONTENT_RELEASE = "CONTENT_RELEASE";
    private static final String NOTICE_CATEGORY_ORDER_DELIVERY = "ORDER_DELIVERY";
    private static final String NOTICE_CATEGORY_REVIEW_REPORT = "REVIEW_REPORT";
    private static final String REFERENCE_TYPE_PRODUCT = "PRODUCT";

    /*
     * [알림 수신 설정 추가]
     * 회원(마이페이지)이 실제로 받는 NOTIFICATION_TYPE을 On/Off 가능한
     * 카테고리로 묶은 매핑입니다. 여기에 없는 타입(관리자/사업자 전용 알림 등)은
     * 설정 대상이 아니므로 항상 발송됩니다.
     */
    private static final Map<String, String> NOTIFICATION_TYPE_TO_CATEGORY = createTypeToCategoryMap();

    /*
     * [알림 수신 설정 추가]
     * 마이페이지 토글 UI에 노출할 카테고리 목록(코드, 표시명, 설명) 입니다.
     * LinkedHashMap으로 노출 순서를 고정합니다.
     */
    private static final Map<String, String[]> NOTICE_CATEGORY_DEFINITIONS = createCategoryDefinitions();

    private static final String ENABLED_YES = "Y";
    private static final String ENABLED_NO = "N";

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
                resolveNoticeCategory(notificationType),
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

        /*
         * 같은 클래스의 @Transactional 메서드를 직접 호출하면 프록시를 우회하므로,
         * 현재 트랜잭션 안에서 DAO 저장 로직을 직접 수행합니다.
         */
        notificationDAO.insertBusinessNotification(
                businessNo,
                null,
                createNotification(
                        NOTIFICATION_TYPE_LOW_STOCK,
                        "상품 재고 부족",
                        productName + " 상품의 남은 재고가 " + stock + "개입니다.",
                        "/business/product/list",
                        REFERENCE_TYPE_PRODUCT,
                        productNo));
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

        /*
         * [알림 수신 설정 추가]
         * 설정 카테고리가 있는 타입에 한해 회원이 꺼두었는지 확인한 뒤,
         * 꺼두었다면 알림을 생성하지 않고 조용히 건너뜁니다.
         */
        String noticeCategory = resolveNoticeCategory(notificationType);

        if (noticeCategory != null
                && notificationDAO.countDisabledNotificationSetting(memberNo, noticeCategory) > 0) {

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

    @Override
    @Transactional(readOnly = true)
    public List<NotificationSettingItemVO> getSettingItems(Long memberNo) {

        validateMemberNo(memberNo);

        List<String> disabledCategoryList = notificationDAO.selectDisabledNoticeCategoryList(memberNo);

        List<NotificationSettingItemVO> items = new ArrayList<>();

        for (Map.Entry<String, String[]> entry : NOTICE_CATEGORY_DEFINITIONS.entrySet()) {

            String category = entry.getKey();
            String[] labelAndDescription = entry.getValue();
            boolean enabled = !disabledCategoryList.contains(category);

            items.add(new NotificationSettingItemVO(
                    category,
                    labelAndDescription[0],
                    labelAndDescription[1],
                    enabled));
        }

        return items;
    }

    @Override
    @Transactional
    public void updateSetting(Long memberNo, String noticeCategory, boolean enabled) {

        validateMemberNo(memberNo);

        if (noticeCategory == null || !NOTICE_CATEGORY_DEFINITIONS.containsKey(noticeCategory)) {
            throw new IllegalArgumentException("올바르지 않은 알림 카테고리입니다.");
        }

        notificationDAO.mergeNotificationSetting(
                memberNo,
                noticeCategory,
                enabled ? ENABLED_YES : ENABLED_NO);
    }

    /*
     * [알림 수신 설정 추가]
     * NOTIFICATION_TYPE에 대응하는 설정 카테고리를 반환합니다.
     * 설정 대상이 아닌 타입(관리자/사업자 업무 알림 등)은 null을 반환합니다.
     */
    private String resolveNoticeCategory(String notificationType) {
        return NOTIFICATION_TYPE_TO_CATEGORY.get(notificationType);
    }

    /*
     * [알림 수신 설정 추가]
     * 회원이 실제로 수신하는 NOTIFICATION_TYPE → 설정 카테고리 매핑입니다.
     */
    private static Map<String, String> createTypeToCategoryMap() {

        Map<String, String> map = new LinkedHashMap<>();

        // 찜한 콘텐츠 출시 알림
        map.put(NOTIFICATION_TYPE_CONTENT_RELEASE, NOTIFICATION_TYPE_CONTENT_RELEASE);

        // 재입고 알림 (전체/옵션 공통)
        map.put(NOTIFICATION_TYPE_RESTOCKED, "RESTOCK");

        // 주문/배송/환불 알림
        map.put("DELIVERY_PREPARING", NOTICE_CATEGORY_ORDER_DELIVERY);
        map.put("DELIVERY_SHIPPED", NOTICE_CATEGORY_ORDER_DELIVERY);
        map.put("DELIVERY_DELIVERED", NOTICE_CATEGORY_ORDER_DELIVERY);
        map.put("REFUND_COMPLETED", NOTICE_CATEGORY_ORDER_DELIVERY);
        map.put("REFUND_REJECTED", NOTICE_CATEGORY_ORDER_DELIVERY);

        // 리뷰 신고 처리 알림
        map.put("CONTENT_REVIEW_REPORT_RECEIVED", NOTICE_CATEGORY_REVIEW_REPORT);
        map.put("PRODUCT_REVIEW_REPORT_RECEIVED", NOTICE_CATEGORY_REVIEW_REPORT);
        map.put("CONTENT_REVIEW_REPORT_PROCESSED", NOTICE_CATEGORY_REVIEW_REPORT);
        map.put("PRODUCT_REVIEW_REPORT_PROCESSED", NOTICE_CATEGORY_REVIEW_REPORT);

        return map;
    }

    /*
     * [알림 수신 설정 추가]
     * 마이페이지 토글 UI에 노출할 카테고리 정의(표시명, 설명)입니다.
     */
    private static Map<String, String[]> createCategoryDefinitions() {

        Map<String, String[]> map = new LinkedHashMap<>();

        map.put(NOTIFICATION_TYPE_CONTENT_RELEASE, new String[] {
                "콘텐츠 출시 알림",
                "찜한 콘텐츠가 새로 출시되면 알려드립니다." });

        map.put("RESTOCK", new String[] {
                "재입고 알림",
                "재입고 신청한 상품이 다시 입고되면 알려드립니다." });

        map.put(NOTICE_CATEGORY_ORDER_DELIVERY, new String[] {
                "주문·배송·환불 알림",
                "주문한 상품의 배송 진행 상황과 환불 처리 결과를 알려드립니다." });

        map.put(NOTICE_CATEGORY_REVIEW_REPORT, new String[] {
                "리뷰 신고 처리 알림",
                "내가 접수한 리뷰 신고의 접수/처리 결과를 알려드립니다." });

        return map;
    }
}
