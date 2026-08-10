package com.project.oditji.notification.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;

import com.project.oditji.common.util.LoginMemberUtil;
import com.project.oditji.notification.service.RestockRequestService;

import jakarta.servlet.http.HttpSession;

/**
 * 상품 상세 화면의 재입고 알림 신청/취소 API입니다.
 */
@RestController
@RequestMapping("/api/restock")
public class RestockRequestApiController {

    private final RestockRequestService restockRequestService;

    public RestockRequestApiController(RestockRequestService restockRequestService) {
        this.restockRequestService = restockRequestService;
    }

    @GetMapping("/{productNo}")
    public ResponseEntity<Map<String, Object>> getRequestStatus(
            @PathVariable Long productNo,

            /*
             * [옵션별 재입고 알림 추가]
             * optionNo가 없으면 상품 전체 신청 여부,
             * 값이 있으면 특정 옵션 신청 여부를 조회합니다.
             */
            @RequestParam(required = false) Long optionNo,

            HttpSession session) {

        Long memberNo = LoginMemberUtil.getLoginMemberNo(session);

        if (memberNo == null) {
            return ResponseEntity.ok(Map.of("login", false, "requested", false));
        }

        boolean requested = restockRequestService.isRequested(memberNo, productNo, optionNo);

        return ResponseEntity.ok(Map.of("login", true, "requested", requested));
    }

    @PostMapping("/{productNo}")
    public ResponseEntity<Map<String, Object>> requestRestock(
            @PathVariable Long productNo,

            /*
             * [옵션별 재입고 알림 추가]
             * NULL이면 상품 전체,
             * 값이 있으면 특정 옵션 재입고 신청입니다.
             */
            @RequestParam(required = false) Long optionNo,

            HttpSession session) {

        Long memberNo = LoginMemberUtil.getLoginMemberNo(session);

        if (memberNo == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("success", false, "message", "로그인이 필요합니다."));
        }

        restockRequestService.requestRestockNotification(memberNo, productNo, optionNo);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "requested", true,
                "message", "재입고 알림 신청이 완료되었습니다."));
    }

    @DeleteMapping("/{productNo}")
    public ResponseEntity<Map<String, Object>> cancelRestock(
            @PathVariable Long productNo,

            /*
             * [옵션별 재입고 알림 추가]
             * NULL이면 상품 전체 신청 취소,
             * 값이 있으면 특정 옵션 신청 취소입니다.
             */
            @RequestParam(required = false) Long optionNo,

            HttpSession session) {

        Long memberNo = LoginMemberUtil.getLoginMemberNo(session);

        if (memberNo == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("success", false, "message", "로그인이 필요합니다."));
        }

        restockRequestService.cancelRestockNotification(memberNo, productNo, optionNo);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "requested", false,
                "message", "재입고 알림 신청이 취소되었습니다."));
    }
}