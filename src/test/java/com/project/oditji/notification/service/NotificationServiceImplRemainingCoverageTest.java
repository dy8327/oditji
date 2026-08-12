package com.project.oditji.notification.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.project.oditji.notification.dao.NotificationDAO;
import com.project.oditji.notification.vo.NotificationVO;

/**
 * 알림 서비스의 남은 유효성 검사, 소유자 알림 및 일괄 사업자 알림 분기를 보완합니다.
 */
@ExtendWith(MockitoExtension.class)
class NotificationServiceImplRemainingCoverageTest {

    @Mock
    private NotificationDAO notificationDAO;

    private NotificationServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new NotificationServiceImpl(notificationDAO);
    }

    @Test
    void memberAndNotificationNumbersShouldRejectNullAndNegativeValues() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.getUnreadContext(null));

        assertThrows(
                IllegalArgumentException.class,
                () -> service.markAllAsRead(-1L));

        assertThrows(
                IllegalArgumentException.class,
                () -> service.markAsRead(null, 1L));

        assertThrows(
                IllegalArgumentException.class,
                () -> service.markAsRead(1L, null));
    }

    @Test
    void createForAdminsShouldNormalizeBlankOptionalFields() {
        service.createForAdmins(
                " ADMIN_NOTICE ",
                " 관리자 알림 ",
                " 처리할 업무가 있습니다. ",
                "   ",
                " ",
                90L);

        ArgumentCaptor<NotificationVO> captor =
                ArgumentCaptor.forClass(NotificationVO.class);
        verify(notificationDAO).insertAdminNotification(captor.capture());

        NotificationVO saved = captor.getValue();
        assertEquals("ADMIN_NOTICE", saved.getNotificationType());
        assertEquals("관리자 알림", saved.getTitle());
        assertEquals("처리할 업무가 있습니다.", saved.getMessage());
        assertNull(saved.getLinkUrl());
        assertNull(saved.getReferenceType());
        assertEquals(90L, saved.getReferenceNo());
        assertEquals("N", saved.getIsRead());
    }

    @Test
    void requiredNotificationTypeAndMessageShouldRejectNullOrBlank() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.createForAdmins(
                        null,
                        "제목",
                        "내용",
                        null,
                        null,
                        null));

        assertThrows(
                IllegalArgumentException.class,
                () -> service.createForAdmins(
                        "TYPE",
                        "제목",
                        "   ",
                        null,
                        null,
                        null));
    }

    @Test
    void createForBusinessShouldSkipNullAndInsertValidBusiness() {
        service.createForBusiness(
                null,
                "TYPE",
                "제목",
                "내용",
                null,
                null,
                null);

        verify(notificationDAO, never())
                .insertBusinessNotification(anyLong(), any(), any());

        service.createForBusiness(
                5L,
                " TYPE ",
                " 제목 ",
                " 내용 ",
                null,
                null,
                5L);

        ArgumentCaptor<NotificationVO> captor =
                ArgumentCaptor.forClass(NotificationVO.class);

        verify(notificationDAO)
                .insertBusinessNotification(eq(5L), any(), captor.capture());

        assertEquals("TYPE", captor.getValue().getNotificationType());
    }

    @Test
    void productAndEventOwnerShouldSkipInvalidIdsAndInsertResolvedOwners() {
        service.createForProductOwner(
                0L,
                "TYPE",
                "제목",
                "내용",
                null,
                null,
                null);

        service.createForEventOwner(
                null,
                "TYPE",
                "제목",
                "내용",
                null,
                null,
                null);

        verify(notificationDAO, never()).selectProductOwnerMemberNo(anyLong());
        verify(notificationDAO, never()).selectEventOwnerMemberNo(anyLong());

        when(notificationDAO.selectEventOwnerMemberNo(20L)).thenReturn(8L);

        service.createForEventOwner(
                20L,
                "EVENT_APPROVED",
                "이벤트 승인",
                "이벤트가 승인되었습니다.",
                null,
                "EVENT",
                20L);

        ArgumentCaptor<NotificationVO> captor =
                ArgumentCaptor.forClass(NotificationVO.class);
        verify(notificationDAO).insertMemberNotification(captor.capture());

        assertEquals(8L, captor.getValue().getReceiverMemberNo());
    }

    @Test
    void cancelGroupBusinessesShouldSkipInvalidAndInsertValidGroup() {
        service.createForCancelGroupBusinesses(
                -1L,
                "CANCEL",
                "취소",
                "취소 요청",
                null,
                null,
                null);

        verify(notificationDAO, never())
                .insertCancelGroupBusinessNotifications(anyLong(), any());

        service.createForCancelGroupBusinesses(
                44L,
                " CANCEL_REQUESTED ",
                " 취소 요청 ",
                " 취소 요청이 접수되었습니다. ",
                " /business/cancel ",
                " CANCEL ",
                44L);

        ArgumentCaptor<NotificationVO> captor =
                ArgumentCaptor.forClass(NotificationVO.class);

        verify(notificationDAO)
                .insertCancelGroupBusinessNotifications(
                        eq(44L),
                        captor.capture());

        assertEquals("CANCEL_REQUESTED", captor.getValue().getNotificationType());
        assertEquals("/business/cancel", captor.getValue().getLinkUrl());
    }

    @Test
    void invalidOrderAndUnresolvedProductOwnerShouldNotInsertNotifications() {
        service.createForOrderBusinesses(
                null,
                "ORDER",
                "주문",
                "주문",
                null,
                null,
                null);

        when(notificationDAO.selectProductOwnerMemberNo(33L)).thenReturn(0L);

        service.createForProductOwner(
                33L,
                "PRODUCT",
                "상품",
                "상품 알림",
                null,
                null,
                33L);

        verify(notificationDAO, never())
                .insertOrderBusinessNotifications(anyLong(), any());
        verify(notificationDAO, never())
                .insertMemberNotification(any());
    }
}
