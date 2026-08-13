package com.project.oditji.notification.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
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
 * 업무 알림 생성 대상, 입력값 정규화, 읽음 처리를 검증합니다.
 */
@ExtendWith(MockitoExtension.class)
class NotificationServiceImplTest {

    @Mock
    private NotificationDAO notificationDAO;

    private NotificationServiceImpl notificationService;

    @BeforeEach
    void setUp() {
        notificationService = new NotificationServiceImpl(notificationDAO);
    }

    @Test
    void getUnreadContextShouldReturnCountAndHeaderList() {

        NotificationVO notification = new NotificationVO();
        when(notificationDAO.selectUnreadNotificationCount(1L)).thenReturn(3);
        when(notificationDAO.selectUnreadNotificationList(1L, 20))
                .thenReturn(List.of(notification));

        NotificationContextVO result = notificationService.getUnreadContext(1L);

        assertEquals(3, result.getUnreadCount());
        assertEquals(List.of(notification), result.getNotificationList());
    }

    @Test
    void getUnreadContextShouldRejectInvalidMember() {

        assertThrows(
                IllegalArgumentException.class,
                () -> notificationService.getUnreadContext(0L));
    }

    @Test
    void markAsReadShouldReturnUpdateResult() {

        when(notificationDAO.updateNotificationRead(10L, 1L)).thenReturn(1);
        when(notificationDAO.updateNotificationRead(11L, 1L)).thenReturn(0);

        assertTrue(notificationService.markAsRead(10L, 1L));
        assertFalse(notificationService.markAsRead(11L, 1L));
    }

    @Test
    void markAsReadShouldRejectInvalidNotificationNumber() {

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> notificationService.markAsRead(0L, 1L));

        assertEquals("올바르지 않은 알림 번호입니다.", exception.getMessage());
    }

    @Test
    void markAllAsReadShouldReturnUpdatedCount() {

        when(notificationDAO.updateAllNotificationRead(1L)).thenReturn(4);

        assertEquals(4, notificationService.markAllAsRead(1L));
    }

    @Test
    void createForMemberShouldTrimRequiredAndOptionalFields() {

        notificationService.createForMember(
                1L,
                " PRODUCT_APPROVED ",
                " 상품 승인 ",
                " 상품이 승인되었습니다. ",
                " /business/products ",
                " PRODUCT ",
                10L);

        ArgumentCaptor<NotificationVO> captor =
                ArgumentCaptor.forClass(NotificationVO.class);
        verify(notificationDAO).insertMemberNotification(captor.capture());

        NotificationVO saved = captor.getValue();
        assertEquals(1L, saved.getReceiverMemberNo());
        assertEquals("PRODUCT_APPROVED", saved.getNotificationType());
        assertEquals("상품 승인", saved.getTitle());
        assertEquals("상품이 승인되었습니다.", saved.getMessage());
        assertEquals("/business/products", saved.getLinkUrl());
        assertEquals("PRODUCT", saved.getReferenceType());
        assertEquals(10L, saved.getReferenceNo());
        assertEquals("N", saved.getIsRead());
    }

    @Test
    void createForMemberShouldSkipInvalidReceiver() {

        notificationService.createForMember(
                null,
                "TYPE",
                "제목",
                "내용",
                null,
                null,
                null);

        verify(notificationDAO, never()).insertMemberNotification(any());
    }

    @Test
    void createForAdminsShouldRejectBlankRequiredField() {

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> notificationService.createForAdmins(
                        "TYPE",
                        " ",
                        "내용",
                        null,
                        null,
                        null));

        assertEquals("알림 제목이 없습니다.", exception.getMessage());
    }

    @Test
    void createForBusinessShouldSkipInvalidBusinessNumber() {

        notificationService.createForBusiness(
                0L,
                "TYPE",
                "제목",
                "내용",
                null,
                null,
                null);

        verify(notificationDAO, never()).insertBusinessNotification(
                org.mockito.ArgumentMatchers.anyLong(),
                any(),
                any());
    }

    @Test
    void createForProductOwnerShouldInsertForResolvedMember() {

        when(notificationDAO.selectProductOwnerMemberNo(10L)).thenReturn(7L);

        notificationService.createForProductOwner(
                10L,
                "PRODUCT_REJECTED",
                "상품 반려",
                "상품이 반려되었습니다.",
                null,
                "PRODUCT",
                10L);

        ArgumentCaptor<NotificationVO> captor =
                ArgumentCaptor.forClass(NotificationVO.class);
        verify(notificationDAO).insertMemberNotification(captor.capture());
        assertEquals(7L, captor.getValue().getReceiverMemberNo());
    }

    @Test
    void createForEventOwnerShouldSkipWhenOwnerCannotBeResolved() {

        when(notificationDAO.selectEventOwnerMemberNo(20L)).thenReturn(null);

        notificationService.createForEventOwner(
                20L,
                "EVENT_REJECTED",
                "이벤트 반려",
                "이벤트가 반려되었습니다.",
                null,
                "EVENT",
                20L);

        verify(notificationDAO, never()).insertMemberNotification(any());
    }

    @Test
    void createForOrderBusinessesShouldInsertOnlyForValidOrderNumber() {

        notificationService.createForOrderBusinesses(
                -1L,
                "ORDER_CREATED",
                "신규 주문",
                "새 주문이 있습니다.",
                null,
                "ORDER",
                30L);

        verify(notificationDAO, never()).insertOrderBusinessNotifications(
                org.mockito.ArgumentMatchers.anyLong(),
                any());

        notificationService.createForOrderBusinesses(
                30L,
                "ORDER_CREATED",
                "신규 주문",
                "새 주문이 있습니다.",
                null,
                "ORDER",
                30L);

        verify(notificationDAO).insertOrderBusinessNotifications(
                org.mockito.ArgumentMatchers.eq(30L),
                any(NotificationVO.class));
    }
}
