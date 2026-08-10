package com.project.oditji.notification.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.project.oditji.notification.vo.NotificationVO;

/**
 * 공통 알림의 저장과 읽음 상태를 처리합니다.
 */
@Mapper
public interface NotificationDAO {

        int insertMemberNotification(NotificationVO notification);

        int insertAdminNotification(NotificationVO notification);

        int insertBusinessNotification(
                        @Param("businessNo") Long businessNo,
                        @Param("notification") NotificationVO notification);

        int insertOrderBusinessNotifications(
                        @Param("orderNo") Long orderNo,
                        @Param("notification") NotificationVO notification);

        int insertCancelGroupBusinessNotifications(
                        @Param("cancelGroupNo") Long cancelGroupNo,
                        @Param("notification") NotificationVO notification);

        Long selectProductOwnerMemberNo(@Param("productNo") Long productNo);

        Long selectEventOwnerMemberNo(@Param("eventNo") Long eventNo);

        List<NotificationVO> selectUnreadNotificationList(
                        @Param("memberNo") Long memberNo,
                        @Param("limit") int limit);

        int selectUnreadNotificationCount(@Param("memberNo") Long memberNo);

        int updateNotificationRead(
                        @Param("notificationNo") Long notificationNo,
                        @Param("memberNo") Long memberNo);

        int updateAllNotificationRead(@Param("memberNo") Long memberNo);

        /* [재고 알림 추가] 상품의 현재 재고/사업자 정보를 조회합니다. */
        java.util.Map<String, Object> selectProductStockNotificationInfo(
                        @Param("productNo") Long productNo);

        /* [재고 부족 알림 추가] 같은 상품의 미읽음 LOW_STOCK 알림 중복 여부를 확인합니다. */
        int countUnreadBusinessProductNotification(
                        @Param("businessNo") Long businessNo,
                        @Param("notificationType") String notificationType,
                        @Param("productNo") Long productNo);

        /* [재입고 알림 추가] 대기 중 신청자에게 공통 알림을 일괄 저장합니다. */
        int insertRestockMemberNotifications(
                        @Param("productNo") Long productNo,
                        @Param("notification") NotificationVO notification);

        /* [재입고 알림 추가] 발송 완료된 재입고 신청을 NOTIFIED 상태로 변경합니다. */
        int updateRestockRequestsNotified(@Param("productNo") Long productNo);

        /*
         * [옵션별 재입고 알림 추가]
         * 특정 OPTION_NO를 기다리고 있는 회원들에게
         * 재입고 알림을 일괄 저장합니다.
         */
        int insertOptionRestockMemberNotifications(
                        @Param("productNo") Long productNo,
                        @Param("optionNo") Long optionNo,
                        @Param("notification") NotificationVO notification);

        /*
         * [옵션별 재입고 알림 추가]
         * 알림을 발송한 옵션 재입고 신청을
         * NOTIFIED 상태로 변경합니다.
         */
        int updateOptionRestockRequestsNotified(
                        @Param("productNo") Long productNo,
                        @Param("optionNo") Long optionNo);
}
