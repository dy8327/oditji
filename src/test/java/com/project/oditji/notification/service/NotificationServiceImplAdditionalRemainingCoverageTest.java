package com.project.oditji.notification.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.project.oditji.notification.dao.NotificationDAO;
import com.project.oditji.notification.vo.NotificationContextVO;
import com.project.oditji.notification.vo.NotificationVO;

/**
 * Batch01 이후 남은 알림 읽음/수신자 해석/필수 제목 검증 분기를 보완합니다.
 */
@ExtendWith(MockitoExtension.class)
class NotificationServiceImplAdditionalRemainingCoverageTest {

    @Mock
    private NotificationDAO notificationDAO;

    private NotificationServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new NotificationServiceImpl(notificationDAO);
    }

    @Test
    void unreadContextAndReadUpdatesShouldCoverSuccessAndNoUpdateResults() {
        NotificationVO notification = new NotificationVO();
        List<NotificationVO> notifications = List.of(notification);

        when(notificationDAO.selectUnreadNotificationCount(1L))
                .thenReturn(3);
        when(notificationDAO.selectUnreadNotificationList(1L, 20))
                .thenReturn(notifications);

        NotificationContextVO context =
                service.getUnreadContext(1L);

        assertEquals(3, context.getUnreadCount());
        assertSame(notifications, context.getNotificationList());

        when(notificationDAO.updateNotificationRead(10L, 1L))
                .thenReturn(1);
        when(notificationDAO.updateNotificationRead(11L, 1L))
                .thenReturn(0);

        assertTrue(service.markAsRead(10L, 1L));
        assertFalse(service.markAsRead(11L, 1L));

        when(notificationDAO.updateAllNotificationRead(1L))
                .thenReturn(4);
        assertEquals(4, service.markAllAsRead(1L));
    }

    @Test
    void zeroMemberAndNotificationNumbersShouldBeRejected() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.getUnreadContext(0L));

        assertThrows(
                IllegalArgumentException.class,
                () -> service.markAsRead(0L, 1L));
    }

    @Test
    void createForMemberShouldSkipInvalidReceiverAndInsertValidReceiver() {
        service.createForMember(
                null,
                "TYPE",
                "제목",
                "내용",
                null,
                null,
                null);

        service.createForMember(
                0L,
                "TYPE",
                "제목",
                "내용",
                null,
                null,
                null);

        verify(notificationDAO, never())
                .insertMemberNotification(any());

        service.createForMember(
                7L,
                " TYPE ",
                " 제목 ",
                " 내용 ",
                " /member ",
                " MEMBER ",
                7L);

        ArgumentCaptor<NotificationVO> captor =
                ArgumentCaptor.forClass(NotificationVO.class);
        verify(notificationDAO).insertMemberNotification(captor.capture());

        NotificationVO saved = captor.getValue();
        assertEquals(7L, saved.getReceiverMemberNo());
        assertEquals("TYPE", saved.getNotificationType());
        assertEquals("제목", saved.getTitle());
        assertEquals("내용", saved.getMessage());
        assertEquals("/member", saved.getLinkUrl());
        assertEquals("MEMBER", saved.getReferenceType());
    }

    @Test
    void titleValidationShouldCoverNullAndBlankCases() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.createForAdmins(
                        "TYPE",
                        null,
                        "내용",
                        null,
                        null,
                        null));

        assertThrows(
                IllegalArgumentException.class,
                () -> service.createForAdmins(
                        "TYPE",
                        "   ",
                        "내용",
                        null,
                        null,
                        null));
    }

    @Test
    void productAndEventOwnersShouldCoverResolvedAndUnresolvedMembers() {
        when(notificationDAO.selectProductOwnerMemberNo(30L))
                .thenReturn(9L);

        service.createForProductOwner(
                30L,
                "PRODUCT",
                "상품",
                "상품 알림",
                null,
                null,
                30L);

        verify(notificationDAO).insertMemberNotification(any());

        when(notificationDAO.selectEventOwnerMemberNo(40L))
                .thenReturn(null);

        service.createForEventOwner(
                40L,
                "EVENT",
                "이벤트",
                "이벤트 알림",
                null,
                null,
                40L);

        verify(notificationDAO)
                .selectEventOwnerMemberNo(40L);
    }

    @Test
    void validOrderBusinessNotificationShouldReachDao() {
        service.createForOrderBusinesses(
                50L,
                " ORDER_CREATED ",
                " 주문 ",
                " 주문이 생성되었습니다. ",
                null,
                null,
                50L);

        ArgumentCaptor<NotificationVO> captor =
                ArgumentCaptor.forClass(NotificationVO.class);

        verify(notificationDAO)
                .insertOrderBusinessNotifications(
                        eq(50L),
                        captor.capture());

        assertEquals(
                "ORDER_CREATED",
                captor.getValue().getNotificationType());
    }
}
