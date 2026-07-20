package com.project.oditji.report.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.project.oditji.member.vo.MemberVO;
import com.project.oditji.report.service.ReportService;

import jakarta.servlet.http.HttpSession;

/**
 * 콘텐츠 리뷰 / 상품 리뷰 신고 API.
 * 프론트(report.js)는 review/goods 상세페이지 공통 모달에서
 * POST /report/submit 으로 신고를 접수한다.
 */
@RestController
@RequestMapping("/report")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @PostMapping("/submit")
    public ResponseEntity<Map<String, Object>> submitReport(
            HttpSession session,
            @RequestBody ReportRequestDTO request) {

        Map<String, Object> result = new HashMap<>();

        MemberVO loginMember =
                (MemberVO) session.getAttribute("loginMember");

        if (loginMember == null) {

            result.put("success", false);
            result.put("loginRequired", true);
            result.put("message", "로그인이 필요합니다.");

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(result);
        }

        Integer contentReviewNo = parseNo(request.getContentReviewNo());
        Integer productReviewNo = parseNo(request.getProductReviewNo());

        try {

            reportService.submitReport(
                    loginMember.getMemberNo(),
                    request.getReviewType(),
                    contentReviewNo,
                    productReviewNo,
                    request.getReason(),
                    request.getDetail());

            result.put("success", true);

            return ResponseEntity.ok(result);

        } catch (IllegalStateException e) {

            // 이미 신고한 리뷰
            result.put("success", false);
            result.put("message", e.getMessage());

            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(result);

        } catch (IllegalArgumentException e) {

            result.put("success", false);
            result.put("message", e.getMessage());

            return ResponseEntity
                    .badRequest()
                    .body(result);
        }
    }

    private Integer parseNo(String value) {

        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        try {
            return Integer.valueOf(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * 신고 등록 요청 바디.
     * reviewNo 계열 값은 프론트에서 hidden input(.value) 문자열로 넘어오므로
     * String 으로 받아 서버에서 안전하게 파싱한다.
     */
    public static class ReportRequestDTO {

        private String reviewType;
        private String reason;
        private String detail;
        private String contentReviewNo;
        private String productReviewNo;

        public String getReviewType() {
            return reviewType;
        }

        public void setReviewType(String reviewType) {
            this.reviewType = reviewType;
        }

        public String getReason() {
            return reason;
        }

        public void setReason(String reason) {
            this.reason = reason;
        }

        public String getDetail() {
            return detail;
        }

        public void setDetail(String detail) {
            this.detail = detail;
        }

        public String getContentReviewNo() {
            return contentReviewNo;
        }

        public void setContentReviewNo(String contentReviewNo) {
            this.contentReviewNo = contentReviewNo;
        }

        public String getProductReviewNo() {
            return productReviewNo;
        }

        public void setProductReviewNo(String productReviewNo) {
            this.productReviewNo = productReviewNo;
        }
    }
}
