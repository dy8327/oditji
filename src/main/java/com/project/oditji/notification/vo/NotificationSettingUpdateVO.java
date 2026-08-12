package com.project.oditji.notification.vo;

/**
 * POST /api/notification/setting 요청 바디입니다.
 */
public class NotificationSettingUpdateVO {

    private String noticeCategory;
    private boolean enabled;

    public NotificationSettingUpdateVO() {
    }

    public String getNoticeCategory() {
        return noticeCategory;
    }

    public void setNoticeCategory(String noticeCategory) {
        this.noticeCategory = noticeCategory;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
