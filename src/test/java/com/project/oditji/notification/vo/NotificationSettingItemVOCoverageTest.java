package com.project.oditji.notification.vo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/** 알림 설정 화면용 VO의 생성자와 접근자를 검증합니다. */
class NotificationSettingItemVOCoverageTest {

    @Test
    void defaultConstructorAndSettersShouldExposeAssignedValues() {
        NotificationSettingItemVO item = new NotificationSettingItemVO();

        item.setNoticeCategory("RESTOCK");
        item.setLabel("재입고 알림");
        item.setDescription("상품 또는 옵션 재입고 시 알림");
        item.setEnabled(false);

        assertEquals("RESTOCK", item.getNoticeCategory());
        assertEquals("재입고 알림", item.getLabel());
        assertEquals("상품 또는 옵션 재입고 시 알림", item.getDescription());
        assertFalse(item.isEnabled());
    }

    @Test
    void allArgumentsConstructorShouldInitializeEveryField() {
        NotificationSettingItemVO item = new NotificationSettingItemVO(
                "CONTENT_RELEASE",
                "콘텐츠 공개 알림",
                "공개 예정 콘텐츠 알림",
                true);

        assertEquals("CONTENT_RELEASE", item.getNoticeCategory());
        assertEquals("콘텐츠 공개 알림", item.getLabel());
        assertEquals("공개 예정 콘텐츠 알림", item.getDescription());
        assertTrue(item.isEnabled());
    }
}
