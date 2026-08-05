package com.project.oditji.notification.vo;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.project.oditji.common.util.DateTimeUtil;

/**
 * 알림 표시용 파생 값과 헤더 컨텍스트의 방어 로직을 검증합니다.
 */
class NotificationVOTest {

    @Test
    void getCreatedAtEpochMsShouldHandleNullAndLocalDateTime() {

        NotificationVO notification = new NotificationVO();
        assertEquals(0L, notification.getCreatedAtEpochMs());

        LocalDateTime createdAt = LocalDateTime.ofInstant(
                Instant.ofEpochMilli(1_234L),
                DateTimeUtil.KOREA_ZONE);
        notification.setCreatedAt(createdAt);
        assertEquals(1_234L, notification.getCreatedAtEpochMs());
    }

    @Test
    void getTypeLabelShouldClassifyKnownNotificationTypes() {

        NotificationVO notification = new NotificationVO();

        notification.setNotificationType("REVIEW_REPORT_CREATED");
        assertEquals("신고", notification.getTypeLabel());

        notification.setNotificationType("DELIVERY_STARTED");
        assertEquals("배송", notification.getTypeLabel());

        notification.setNotificationType("ORDER_CREATED");
        assertEquals("주문", notification.getTypeLabel());

        notification.setNotificationType("CANCEL_APPROVED");
        assertEquals("취소", notification.getTypeLabel());

        notification.setNotificationType("SETTLEMENT_DONE");
        assertEquals("정산", notification.getTypeLabel());

        notification.setNotificationType("EVENT_APPROVED");
        assertEquals("이벤트", notification.getTypeLabel());

        notification.setNotificationType("PRODUCT_APPROVED");
        assertEquals("상품", notification.getTypeLabel());

        notification.setNotificationType("BUSINESS_APPROVED");
        assertEquals("사업자", notification.getTypeLabel());

        notification.setNotificationType(null);
        assertEquals("업무", notification.getTypeLabel());
    }

    @Test
    void notificationContextShouldNormalizeNegativeCountAndNullList() {

        NotificationContextVO context = new NotificationContextVO(-3, null);

        assertEquals(0, context.getUnreadCount());
        assertEquals(List.of(), context.getNotificationList());
    }
}
