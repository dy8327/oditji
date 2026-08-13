package com.project.oditji.notification.vo;

/**
 * 마이페이지 알림 수신 설정 화면에 노출할 카테고리 1건의 정보입니다.
 *
 * NOTIFICATION_SETTING 테이블에 행이 없으면 기본값(수신함)으로 간주하는
 * "옵트아웃" 모델이므로, enabled는 실제 저장된 값이 아니라
 * 저장된 값이 없을 때 true를 기본으로 계산한 결과입니다.
 */
public class NotificationSettingItemVO {

    private String noticeCategory;
    private String label;
    private String description;
    private boolean enabled;

    public NotificationSettingItemVO() {
    }

    public NotificationSettingItemVO(
            String noticeCategory,
            String label,
            String description,
            boolean enabled) {

        this.noticeCategory = noticeCategory;
        this.label = label;
        this.description = description;
        this.enabled = enabled;
    }

    public String getNoticeCategory() {
        return noticeCategory;
    }

    public void setNoticeCategory(String noticeCategory) {
        this.noticeCategory = noticeCategory;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
