package com.project.oditji.notification.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.project.oditji.common.util.LoginMemberUtil;
import com.project.oditji.notification.service.NotificationService;
import com.project.oditji.notification.vo.NotificationSettingItemVO;
import com.project.oditji.notification.vo.NotificationSettingUpdateVO;

import jakarta.servlet.http.HttpSession;

/**
 * 마이페이지 알림 수신 설정(카테고리별 On/Off) API입니다.
 */
@RestController
@RequestMapping("/api/notification")
public class NotificationSettingApiController {

    private static final String RESPONSE_SUCCESS = "success";
    private static final String RESPONSE_MESSAGE = "message";

    private final NotificationService notificationService;

    public NotificationSettingApiController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping("/setting")
    public ResponseEntity<Map<String, Object>> getSettingList(HttpSession session) {

        Long memberNo = LoginMemberUtil.getLoginMemberNo(session);

        if (memberNo == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of(RESPONSE_SUCCESS, false, RESPONSE_MESSAGE, "로그인이 필요합니다."));
        }

        List<NotificationSettingItemVO> settingList = notificationService.getSettingItems(memberNo);

        return ResponseEntity.ok(Map.of(
                RESPONSE_SUCCESS, true,
                "settingList", settingList));
    }

    @PostMapping("/setting")
    public ResponseEntity<Map<String, Object>> updateSetting(
            @RequestBody NotificationSettingUpdateVO request,
            HttpSession session) {

        Long memberNo = LoginMemberUtil.getLoginMemberNo(session);

        if (memberNo == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of(RESPONSE_SUCCESS, false, RESPONSE_MESSAGE, "로그인이 필요합니다."));
        }

        try {
            notificationService.updateSetting(
                    memberNo,
                    request == null ? null : request.getNoticeCategory(),
                    request != null && request.isEnabled());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of(RESPONSE_SUCCESS, false, RESPONSE_MESSAGE, e.getMessage()));
        }

        return ResponseEntity.ok(Map.of(
                RESPONSE_SUCCESS, true,
                RESPONSE_MESSAGE, "알림 수신 설정이 저장되었습니다."));
    }
}
