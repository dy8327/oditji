package com.project.oditji.notification.vo;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/** NotificationVO의 최종 fallback 분기를 검증합니다. */
class NotificationVOFinalConditionCoverageTest {

    @Test
    void unknownNonNullNotificationTypeShouldUseWorkLabel() {
        NotificationVO notification = new NotificationVO();
        notification.setNotificationType("SYSTEM_NOTICE");

        assertEquals("업무", notification.getTypeLabel());
    }
}
