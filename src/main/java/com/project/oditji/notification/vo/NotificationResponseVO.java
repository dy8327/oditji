package com.project.oditji.notification.vo;

/**
 * 업무 알림 읽음 처리 API의 공통 응답입니다.
 */
public class NotificationResponseVO {

    private final boolean success;
    private final String message;

    public NotificationResponseVO(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }
}
