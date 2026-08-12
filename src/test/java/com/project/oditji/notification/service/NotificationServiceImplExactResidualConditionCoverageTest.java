package com.project.oditji.notification.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.project.oditji.notification.dao.NotificationDAO;

/**
 * 알림 대상 번호 검증의 OR 단락 평가에서 남아 있는 반대 피연산자 분기를 보완합니다.
 */
class NotificationServiceImplExactResidualConditionCoverageTest {

    private NotificationDAO notificationDAO;
    private NotificationServiceImpl service;

    @BeforeEach
    void setUp() {
        notificationDAO = mock(NotificationDAO.class);
        service = new NotificationServiceImpl(notificationDAO);
    }

    @Test
    void businessZeroProductNullAndEventZeroShouldAllReturnBeforeDaoWrite() {
        service.createForBusiness(
                0L,
                "TYPE",
                "제목",
                "내용",
                null,
                null,
                null);

        service.createForProductOwner(
                null,
                "TYPE",
                "제목",
                "내용",
                null,
                null,
                null);

        service.createForEventOwner(
                0L,
                "TYPE",
                "제목",
                "내용",
                null,
                null,
                null);

        verify(notificationDAO, never()).insertBusinessNotification(anyLong(), any(), any());
        verify(notificationDAO, never()).selectProductOwnerMemberNo(anyLong());
        verify(notificationDAO, never()).selectEventOwnerMemberNo(anyLong());
    }
}