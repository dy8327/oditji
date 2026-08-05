package com.project.oditji.report.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.project.oditji.member.vo.MemberVO;
import com.project.oditji.report.controller.ReportController.ReportRequestDTO;
import com.project.oditji.report.service.ReportService;

import jakarta.servlet.http.HttpSession;

/** 신고 API의 로그인, 성공, 중복 신고와 입력 오류 응답을 검증합니다. */
@ExtendWith(MockitoExtension.class)
class ReportControllerCoverageTest {

    @Mock
    private ReportService reportService;

    @Mock
    private HttpSession session;

    private ReportController controller;

    @BeforeEach
    void setUp() {
        controller = new ReportController(reportService);
    }

    @Test
    void anonymousRequestShouldReturnUnauthorized() {
        ResponseEntity<Map<String, Object>> response = controller.submitReport(
                session,
                request("CONTENT", "SPAM", "상세", "10", null));

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals(Boolean.FALSE, response.getBody().get("success"));
        assertEquals(Boolean.TRUE, response.getBody().get("loginRequired"));
        assertEquals("로그인이 필요합니다.", response.getBody().get("message"));
    }

    @Test
    void validRequestShouldParseReviewNumbersAndReturnSuccess() {
        MemberVO member = member(7L);
        when(session.getAttribute("loginMember")).thenReturn(member);

        ResponseEntity<Map<String, Object>> response = controller.submitReport(
                session,
                request("CONTENT", "ABUSE", "상세 내용", " 15 ", "invalid"));

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(Boolean.TRUE, response.getBody().get("success"));
        verify(reportService).submitReport(
                7L,
                "CONTENT",
                15,
                null,
                "ABUSE",
                "상세 내용");
    }

    @Test
    void blankNumbersShouldBePassedAsNull() {
        when(session.getAttribute("loginMember")).thenReturn(member(8L));

        controller.submitReport(
                session,
                request("PRODUCT", "ETC", null, " ", null));

        verify(reportService).submitReport(8L, "PRODUCT", null, null, "ETC", null);
    }

    @Test
    void duplicateReportShouldReturnConflict() {
        when(session.getAttribute("loginMember")).thenReturn(member(9L));
        doThrow(new IllegalStateException("이미 신고했습니다."))
                .when(reportService)
                .submitReport(any(), any(), any(), any(), any(), any());

        ResponseEntity<Map<String, Object>> response = controller.submitReport(
                session,
                request("CONTENT", "SPAM", null, "1", null));

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals(Boolean.FALSE, response.getBody().get("success"));
        assertEquals("이미 신고했습니다.", response.getBody().get("message"));
    }

    @Test
    void invalidRequestShouldReturnBadRequest() {
        when(session.getAttribute("loginMember")).thenReturn(member(10L));
        doThrow(new IllegalArgumentException("신고 사유가 필요합니다."))
                .when(reportService)
                .submitReport(any(), any(), any(), any(), any(), any());

        ResponseEntity<Map<String, Object>> response = controller.submitReport(
                session,
                request("CONTENT", null, null, "1", null));

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(Boolean.FALSE, response.getBody().get("success"));
        assertEquals("신고 사유가 필요합니다.", response.getBody().get("message"));
    }

    @Test
    void requestDtoShouldExposeAllValues() {
        ReportRequestDTO dto = new ReportRequestDTO();
        dto.setReviewType("PRODUCT");
        dto.setReason("SPAM");
        dto.setDetail("detail");
        dto.setContentReviewNo("11");
        dto.setProductReviewNo("12");

        assertEquals("PRODUCT", dto.getReviewType());
        assertEquals("SPAM", dto.getReason());
        assertEquals("detail", dto.getDetail());
        assertEquals("11", dto.getContentReviewNo());
        assertEquals("12", dto.getProductReviewNo());
        assertFalse(dto.getReviewType().isBlank());
        assertTrue(dto.getProductReviewNo().startsWith("1"));
        assertNull(new ReportRequestDTO().getDetail());
    }

    private ReportRequestDTO request(
            String reviewType,
            String reason,
            String detail,
            String contentReviewNo,
            String productReviewNo) {

        ReportRequestDTO dto = new ReportRequestDTO();
        dto.setReviewType(reviewType);
        dto.setReason(reason);
        dto.setDetail(detail);
        dto.setContentReviewNo(contentReviewNo);
        dto.setProductReviewNo(productReviewNo);
        return dto;
    }

    private MemberVO member(Long memberNo) {
        MemberVO member = new MemberVO();
        member.setMemberNo(memberNo);
        return member;
    }
}
