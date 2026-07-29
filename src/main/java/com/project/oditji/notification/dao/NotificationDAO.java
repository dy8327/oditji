package com.project.oditji.notification.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.project.oditji.notification.vo.NotificationVO;

/**
 * 관리자·사업자 공통 업무 알림의 저장과 읽음 상태를 처리합니다.
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
}
