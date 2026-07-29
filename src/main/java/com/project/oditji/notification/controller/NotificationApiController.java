package com.project.oditji.notification.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.project.oditji.member.vo.MemberVO;
import com.project.oditji.notification.service.NotificationService;
import com.project.oditji.notification.vo.NotificationContextVO;
import com.project.oditji.notification.vo.NotificationResponseVO;

import jakarta.servlet.http.HttpSession;

/**
 * 공통 헤더에서 사용하는 관리자·사업자 업무 알림 API입니다.
 */
@RestController
@RequestMapping("/notification/api")
public class NotificationApiController {

    private static final String ROLE_ADMIN = "ADMIN";
    private static final String ROLE_BUSINESS = "BUSINESS";

    private final NotificationService notificationService;

    public NotificationApiController(
            NotificationService notificationService) {

        this.notificationService = notificationService;
    }

    @GetMapping("/context")
    public NotificationContextVO getContext(HttpSession session) {

        MemberVO loginMember = getLoginMember(session);

        if (!hasNotificationAccess(loginMember)) {
            return new NotificationContextVO(0, List.of());
        }

        return notificationService.getUnreadContext(
                loginMember.getMemberNo());
    }

    @PostMapping("/{notificationNo}/read")
    public NotificationResponseVO markAsRead(
            @PathVariable("notificationNo") Long notificationNo,
            HttpSession session) {

        MemberVO loginMember = getLoginMember(session);

        if (!hasNotificationAccess(loginMember)) {
            return new NotificationResponseVO(
                    false,
                    "알림 읽음 처리 권한이 없습니다.");
        }

        boolean updated = notificationService.markAsRead(
                notificationNo,
                loginMember.getMemberNo());

        return new NotificationResponseVO(
                updated,
                updated
                        ? "알림을 읽음 처리했습니다."
                        : "이미 읽었거나 존재하지 않는 알림입니다.");
    }

    @PostMapping("/read-all")
    public NotificationResponseVO markAllAsRead(HttpSession session) {

        MemberVO loginMember = getLoginMember(session);

        if (!hasNotificationAccess(loginMember)) {
            return new NotificationResponseVO(
                    false,
                    "알림 읽음 처리 권한이 없습니다.");
        }

        notificationService.markAllAsRead(loginMember.getMemberNo());

        return new NotificationResponseVO(
                true,
                "업무 알림을 모두 읽음 처리했습니다.");
    }

    private MemberVO getLoginMember(HttpSession session) {

        if (session == null) {
            return null;
        }

        Object loginMember = session.getAttribute("loginMember");

        return loginMember instanceof MemberVO member
                ? member
                : null;
    }

    private boolean hasNotificationAccess(MemberVO loginMember) {

        if (loginMember == null || loginMember.getMemberNo() == null) {
            return false;
        }

        return ROLE_ADMIN.equals(loginMember.getRole())
                || ROLE_BUSINESS.equals(loginMember.getRole());
    }
}
