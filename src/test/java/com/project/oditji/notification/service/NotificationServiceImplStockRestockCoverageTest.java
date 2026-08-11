package com.project.oditji.notification.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.project.oditji.notification.dao.NotificationDAO;
import com.project.oditji.notification.vo.NotificationVO;

/**
 * 재고 부족 및 재입고 알림의 남은 분기와 메시지 생성 결과를 검증합니다.
 */
@ExtendWith(MockitoExtension.class)
class NotificationServiceImplStockRestockCoverageTest {

    @Mock
    private NotificationDAO notificationDAO;

    private NotificationServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new NotificationServiceImpl(notificationDAO);
    }

    @Test
    void lowStockShouldSkipInvalidProductNumbersWithoutDaoAccess() {
        service.createLowStockNotificationIfNeeded(null);
        service.createLowStockNotificationIfNeeded(0L);

        verifyNoInteractions(notificationDAO);
    }

    @Test
    void lowStockShouldSkipMissingOrEmptyProductInfo() {
        when(notificationDAO.selectProductStockNotificationInfo(10L))
                .thenReturn(null);
        when(notificationDAO.selectProductStockNotificationInfo(11L))
                .thenReturn(Map.of());

        service.createLowStockNotificationIfNeeded(10L);
        service.createLowStockNotificationIfNeeded(11L);

        verify(notificationDAO, never())
                .countUnreadBusinessProductNotification(anyLong(), any(), anyLong());
        verify(notificationDAO, never())
                .insertBusinessNotification(anyLong(), any());
    }

    @Test
    void lowStockShouldSkipInvalidBusinessStockAndStockAboveThreshold() {
        when(notificationDAO.selectProductStockNotificationInfo(20L))
                .thenReturn(Map.of(
                        "BUSINESS_NO", "invalid",
                        "STOCK", 5,
                        "PRODUCT_NAME", "상품A"));
        when(notificationDAO.selectProductStockNotificationInfo(21L))
                .thenReturn(Map.of(
                        "BUSINESS_NO", 3L,
                        "STOCK", "invalid",
                        "PRODUCT_NAME", "상품B"));
        when(notificationDAO.selectProductStockNotificationInfo(22L))
                .thenReturn(Map.of(
                        "BUSINESS_NO", 3L,
                        "STOCK", 6,
                        "PRODUCT_NAME", "상품C"));

        service.createLowStockNotificationIfNeeded(20L);
        service.createLowStockNotificationIfNeeded(21L);
        service.createLowStockNotificationIfNeeded(22L);

        verify(notificationDAO, never())
                .countUnreadBusinessProductNotification(anyLong(), any(), anyLong());
        verify(notificationDAO, never())
                .insertBusinessNotification(anyLong(), any());
    }

    @Test
    void lowStockShouldSkipDuplicateAndCreateNewNotificationAtThreshold() {
        when(notificationDAO.selectProductStockNotificationInfo(30L))
                .thenReturn(Map.of(
                        "BUSINESS_NO", 7L,
                        "STOCK", 4,
                        "PRODUCT_NAME", "중복상품"));
        when(notificationDAO.countUnreadBusinessProductNotification(
                7L,
                "LOW_STOCK",
                30L))
                .thenReturn(1);

        service.createLowStockNotificationIfNeeded(30L);

        verify(notificationDAO, never())
                .insertBusinessNotification(anyLong(), any());

        when(notificationDAO.selectProductStockNotificationInfo(31L))
                .thenReturn(Map.of(
                        "BUSINESS_NO", 8L,
                        "STOCK", 5,
                        "PRODUCT_NAME", "재고상품"));
        when(notificationDAO.countUnreadBusinessProductNotification(
                8L,
                "LOW_STOCK",
                31L))
                .thenReturn(0);

        service.createLowStockNotificationIfNeeded(31L);

        ArgumentCaptor<NotificationVO> captor =
                ArgumentCaptor.forClass(NotificationVO.class);
        verify(notificationDAO)
                .insertBusinessNotification(eq(8L), captor.capture());

        NotificationVO saved = captor.getValue();
        assertEquals("LOW_STOCK", saved.getNotificationType());
        assertEquals("상품 재고 부족", saved.getTitle());
        assertEquals("재고상품 상품의 남은 재고가 5개입니다.", saved.getMessage());
        assertEquals("/business/product/list", saved.getLinkUrl());
        assertEquals("PRODUCT", saved.getReferenceType());
        assertEquals(31L, saved.getReferenceNo());
        assertEquals("N", saved.getIsRead());
        assertNull(saved.getReceiverMemberNo());
    }

    @Test
    void productRestockShouldSkipInvalidIdsAndNotUpdateWhenNoNotificationsInserted() {
        service.createRestockNotifications(null, "상품");
        service.createRestockNotifications(0L, "상품");

        verifyNoInteractions(notificationDAO);

        when(notificationDAO.insertRestockMemberNotifications(
                eq(40L),
                any(NotificationVO.class)))
                .thenReturn(0);

        service.createRestockNotifications(40L, null);

        ArgumentCaptor<NotificationVO> captor =
                ArgumentCaptor.forClass(NotificationVO.class);
        verify(notificationDAO)
                .insertRestockMemberNotifications(eq(40L), captor.capture());
        verify(notificationDAO, never())
                .updateRestockRequestsNotified(anyLong());

        NotificationVO saved = captor.getValue();
        assertEquals("RESTOCKED", saved.getNotificationType());
        assertEquals("상품 재입고 안내", saved.getTitle());
        assertEquals("신청하신 상품 상품이 재입고되었습니다.", saved.getMessage());
        assertEquals("/goods/goodsDetail/40", saved.getLinkUrl());
        assertEquals("PRODUCT", saved.getReferenceType());
        assertEquals(40L, saved.getReferenceNo());
    }

    @Test
    void productRestockShouldNormalizeBlankAndTrimmedNamesAndUpdateRequests() {
        when(notificationDAO.insertRestockMemberNotifications(
                eq(41L),
                any(NotificationVO.class)))
                .thenReturn(1);

        service.createRestockNotifications(41L, "   ");

        ArgumentCaptor<NotificationVO> blankCaptor =
                ArgumentCaptor.forClass(NotificationVO.class);
        verify(notificationDAO)
                .insertRestockMemberNotifications(eq(41L), blankCaptor.capture());
        verify(notificationDAO).updateRestockRequestsNotified(41L);
        assertEquals(
                "신청하신 상품 상품이 재입고되었습니다.",
                blankCaptor.getValue().getMessage());

        when(notificationDAO.insertRestockMemberNotifications(
                eq(42L),
                any(NotificationVO.class)))
                .thenReturn(2);

        service.createRestockNotifications(42L, "  정상상품  ");

        ArgumentCaptor<NotificationVO> nameCaptor =
                ArgumentCaptor.forClass(NotificationVO.class);
        verify(notificationDAO)
                .insertRestockMemberNotifications(eq(42L), nameCaptor.capture());
        verify(notificationDAO).updateRestockRequestsNotified(42L);
        assertEquals(
                "정상상품 상품이 재입고되었습니다.",
                nameCaptor.getValue().getMessage());
    }

    @Test
    void optionRestockShouldSkipEveryInvalidIdentifierCombination() {
        service.createOptionRestockNotifications(
                null, 1L, "상품", "검정", "M");
        service.createOptionRestockNotifications(
                0L, 1L, "상품", "검정", "M");
        service.createOptionRestockNotifications(
                1L, null, "상품", "검정", "M");
        service.createOptionRestockNotifications(
                1L, 0L, "상품", "검정", "M");

        verifyNoInteractions(notificationDAO);
    }

    @Test
    void optionRestockShouldUseFallbackTextsAndSkipUpdateWhenNothingInserted() {
        when(notificationDAO.insertOptionRestockMemberNotifications(
                eq(50L),
                eq(500L),
                any(NotificationVO.class)))
                .thenReturn(0);

        service.createOptionRestockNotifications(
                50L,
                500L,
                null,
                null,
                null);

        ArgumentCaptor<NotificationVO> captor =
                ArgumentCaptor.forClass(NotificationVO.class);
        verify(notificationDAO)
                .insertOptionRestockMemberNotifications(
                        eq(50L),
                        eq(500L),
                        captor.capture());
        verify(notificationDAO, never())
                .updateOptionRestockRequestsNotified(anyLong(), anyLong());

        NotificationVO saved = captor.getValue();
        assertEquals("RESTOCKED", saved.getNotificationType());
        assertEquals("상품 재입고 안내", saved.getTitle());
        assertEquals(
                "신청하신 상품의 - / - 옵션이 재입고되었습니다.",
                saved.getMessage());
        assertEquals("/goods/goodsDetail/50", saved.getLinkUrl());
        assertEquals("PRODUCT", saved.getReferenceType());
        assertEquals(50L, saved.getReferenceNo());
    }

    @Test
    void optionRestockShouldCoverBlankAndTrimmedTextsAndUpdateRequests() {
        when(notificationDAO.insertOptionRestockMemberNotifications(
                eq(51L),
                eq(501L),
                any(NotificationVO.class)))
                .thenReturn(1);

        service.createOptionRestockNotifications(
                51L,
                501L,
                "   ",
                "   ",
                "   ");

        ArgumentCaptor<NotificationVO> blankCaptor =
                ArgumentCaptor.forClass(NotificationVO.class);
        verify(notificationDAO)
                .insertOptionRestockMemberNotifications(
                        eq(51L),
                        eq(501L),
                        blankCaptor.capture());
        verify(notificationDAO)
                .updateOptionRestockRequestsNotified(51L, 501L);
        assertEquals(
                "신청하신 상품의 - / - 옵션이 재입고되었습니다.",
                blankCaptor.getValue().getMessage());

        when(notificationDAO.insertOptionRestockMemberNotifications(
                eq(52L),
                eq(502L),
                any(NotificationVO.class)))
                .thenReturn(3);

        service.createOptionRestockNotifications(
                52L,
                502L,
                "  후드티  ",
                "  블랙  ",
                "  XL  ");

        ArgumentCaptor<NotificationVO> valueCaptor =
                ArgumentCaptor.forClass(NotificationVO.class);
        verify(notificationDAO)
                .insertOptionRestockMemberNotifications(
                        eq(52L),
                        eq(502L),
                        valueCaptor.capture());
        verify(notificationDAO)
                .updateOptionRestockRequestsNotified(52L, 502L);
        assertEquals(
                "후드티의 블랙 / XL 옵션이 재입고되었습니다.",
                valueCaptor.getValue().getMessage());
    }
}
