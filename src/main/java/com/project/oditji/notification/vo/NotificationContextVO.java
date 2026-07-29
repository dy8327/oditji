package com.project.oditji.notification.vo;

import java.util.List;

/**
 * 공통 헤더에 전달할 현재 사용자의 미읽은 업무 알림 정보입니다.
 */
public class NotificationContextVO {

    private final int unreadCount;
    private final List<NotificationVO> notificationList;

    public NotificationContextVO(
            int unreadCount,
            List<NotificationVO> notificationList) {

        this.unreadCount = Math.max(0, unreadCount);
        this.notificationList = notificationList == null
                ? List.of()
                : List.copyOf(notificationList);
    }

    public int getUnreadCount() {
        return unreadCount;
    }

    public List<NotificationVO> getNotificationList() {
        return notificationList;
    }
}
