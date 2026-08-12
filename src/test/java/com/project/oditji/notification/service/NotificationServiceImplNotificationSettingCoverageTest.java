package com.project.oditji.notification.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.project.oditji.notification.dao.NotificationDAO;
import com.project.oditji.notification.vo.NotificationSettingItemVO;
import com.project.oditji.notification.vo.NotificationVO;

/**
 * [알림 수신 설정 추가]
 * 카테고리별 On/Off 조회, 저장, 발송 차단 로직을 검증합니다.
 */
@ExtendWith(MockitoExtension.class)
class NotificationServiceImplNotificationSettingCoverageTest {

    @Mock
    private NotificationDAO notificationDAO;

    private NotificationServiceImpl notificationService;

    @BeforeEach
    void setUp() {
        notificationService = new NotificationServiceImpl(notificationDAO);
    }

    @Test
    void getSettingItemsShouldMarkDisabledCategoryAsFalse() {

        when(notificationDAO.selectDisabledNoticeCategoryList(1L))
                .thenReturn(List.of("RESTOCK"));

        List<NotificationSettingItemVO> items = notificationService.getSettingItems(1L);

        assertEquals(4, items.size());

        NotificationSettingItemVO restockItem = items.stream()
                .filter(item -> "RESTOCK".equals(item.getNoticeCategory()))
                .findFirst()
                .orElseThrow();
        assertFalse(restockItem.isEnabled());

        NotificationSettingItemVO contentItem = items.stream()
                .filter(item -> "CONTENT_RELEASE".equals(item.getNoticeCategory()))
                .findFirst()
                .orElseThrow();
        assertTrue(contentItem.isEnabled());
    }

    @Test
    void getSettingItemsShouldRejectInvalidMemberNo() {
        assertThrows(IllegalArgumentException.class,
                () -> notificationService.getSettingItems(null));
    }

    @Test
    void updateSettingShouldRejectUnknownCategory() {
        assertThrows(IllegalArgumentException.class,
                () -> notificationService.updateSetting(1L, "NOT_A_CATEGORY", true));
    }

    @Test
    void updateSettingShouldMergeWithYOrN() {

        notificationService.updateSetting(1L, "RESTOCK", false);
        verify(notificationDAO).mergeNotificationSetting(1L, "RESTOCK", "N");

        notificationService.updateSetting(1L, "RESTOCK", true);
        verify(notificationDAO).mergeNotificationSetting(1L, "RESTOCK", "Y");
    }

    @Test
    void createForMemberShouldSkipWhenCategoryDisabled() {

        when(notificationDAO.countDisabledNotificationSetting(1L, "CONTENT_RELEASE"))
                .thenReturn(1);

        notificationService.createForMember(
                1L,
                "CONTENT_RELEASE",
                "제목",
                "내용",
                null,
                null,
                null);

        verify(notificationDAO, never()).insertMemberNotification(any());
    }

    @Test
    void createForMemberShouldInsertWhenCategoryEnabled() {

        when(notificationDAO.countDisabledNotificationSetting(1L, "CONTENT_RELEASE"))
                .thenReturn(0);

        notificationService.createForMember(
                1L,
                "CONTENT_RELEASE",
                "제목",
                "내용",
                null,
                null,
                null);

        verify(notificationDAO).insertMemberNotification(any(NotificationVO.class));
    }

    @Test
    void createForMemberShouldSkipSettingCheckForUnmappedType() {

        notificationService.createForMember(
                1L,
                "ORDER_ITEM_ETC",
                "제목",
                "내용",
                null,
                null,
                null);

        verify(notificationDAO, never())
                .countDisabledNotificationSetting(anyLong(), any());
        verify(notificationDAO).insertMemberNotification(any(NotificationVO.class));
    }

    @Test
    void createForBusinessShouldPassNullCategoryForUnmappedType() {

        notificationService.createForBusiness(
                10L,
                "SETTLEMENT_APPROVED",
                "제목",
                "내용",
                null,
                null,
                null);

        verify(notificationDAO).insertBusinessNotification(
                eq(10L),
                isNull(),
                any(NotificationVO.class));
    }

    /*
     * [일반회원 사업자 전환신청 폐기]
     * BUSINESS_APPROVED/REJECTED는 더 이상 알림 수신 설정 카테고리에 매핑되지 않는다.
     * (사업자 가입 승인/반려 알림 자체는 계속 발송되지만, 끄고 켤 수 있는 설정 항목에서는 제외된다.)
     */
    @Test
    void createForBusinessShouldPassNullCategoryForBusinessApprovedType() {

        notificationService.createForBusiness(
                10L,
                "BUSINESS_APPROVED",
                "제목",
                "내용",
                null,
                null,
                null);

        verify(notificationDAO).insertBusinessNotification(
                eq(10L),
                isNull(),
                any(NotificationVO.class));
    }
}
