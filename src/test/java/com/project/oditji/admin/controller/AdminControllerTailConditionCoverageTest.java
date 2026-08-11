package com.project.oditji.admin.controller;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.project.oditji.admin.service.AdminService;

/** 관리자 컨트롤러의 null/blank 단축평가와 일괄처리 잔여 분기를 보완합니다. */
class AdminControllerTailConditionCoverageTest {

    private AdminService adminService;
    private AdminController controller;

    @BeforeEach
    void setUp() {
        adminService = mock(AdminService.class);
        controller = new AdminController(adminService);
    }

    @Test
    void emptyNonNullSelectionsShouldUseSecondOperandOfBulkGuards() {
        RedirectAttributes memberRedirect = mock(RedirectAttributes.class);
        RedirectAttributes reviewRedirect = mock(RedirectAttributes.class);
        RedirectAttributes productReviewRedirect = mock(RedirectAttributes.class);

        controller.memberBulkAction(
                "block",
                List.of(),
                null,
                null,
                null,
                null,
                1,
                memberRedirect);
        controller.reviewBulkAction(
                "delete",
                List.of(),
                null,
                null,
                null,
                1,
                reviewRedirect);
        controller.productReviewBulkAction(
                "delete",
                List.of(),
                null,
                null,
                null,
                1,
                productReviewRedirect);

        verify(memberRedirect).addFlashAttribute("message", "선택된 회원이 없습니다.");
        verify(reviewRedirect).addFlashAttribute("message", "선택된 리뷰가 없습니다.");
        verify(productReviewRedirect).addFlashAttribute("message", "선택된 리뷰가 없습니다.");
    }

    @Test
    void skippedBulkRowsAndDefaultLabelsShouldBuildExclusionMessages() {
        RedirectAttributes memberRedirect = mock(RedirectAttributes.class);
        RedirectAttributes reviewRedirect = mock(RedirectAttributes.class);
        RedirectAttributes productReviewRedirect = mock(RedirectAttributes.class);

        when(adminService.bulkMemberAction(List.of(1L, 2L), "block"))
                .thenReturn(1);
        when(adminService.bulkContentReviewAction(List.of(3L, 4L), "unknown"))
                .thenReturn(1);
        when(adminService.bulkProductReviewAction(List.of(5L, 6L), "unknown"))
                .thenReturn(1);

        controller.memberBulkAction(
                "block",
                List.of(1L, 2L),
                null,
                null,
                null,
                null,
                1,
                memberRedirect);
        controller.reviewBulkAction(
                "unknown",
                List.of(3L, 4L),
                null,
                null,
                null,
                1,
                reviewRedirect);
        controller.productReviewBulkAction(
                "unknown",
                List.of(5L, 6L),
                null,
                null,
                null,
                1,
                productReviewRedirect);

        verify(memberRedirect).addFlashAttribute(
                "message",
                "1명의 회원을 처리했습니다. (자동삭제 예정 회원 1명은 처리에서 제외되었습니다.)");
        verify(reviewRedirect).addFlashAttribute(
                "message",
                "1건의 리뷰를 처리 처리했습니다. (이미 처리되었거나 대상이 아닌 1건은 제외되었습니다.)");
        verify(productReviewRedirect).addFlashAttribute(
                "message",
                "1건의 리뷰를 처리 처리했습니다. (이미 처리되었거나 대상이 아닌 1건은 제외되었습니다.)");
    }

    @Test
    void redirectBuildersShouldCoverNonNullBlankSecondOperands() {
        String member = invokeUrl(
                "memberListRedirectUrl",
                " ",
                " ",
                " ",
                " ",
                2);
        assertFalse(member.contains("keyword="));
        assertFalse(member.contains("searchType="));
        assertFalse(member.contains("status="));
        assertFalse(member.contains("memberType="));

        assertBlankOptionalUrl("reviewListRedirectUrl", "tab", "keyword", "searchType", 3);
        assertBlankOptionalUrl("productReviewListRedirectUrl", "tab", "keyword", "searchType", 4);

        String event = invokeUrl(
                "eventListRedirectUrl",
                " ",
                " ",
                " ",
                5);
        assertFalse(event.contains("tab="));
        assertFalse(event.contains("period="));
        assertFalse(event.contains("keyword="));

        assertBlankOptionalUrl("productListRedirectUrl", "tab", "keyword", "searchType", 6);

        String business = invokeUrl(
                "businessListRedirectUrl",
                "approval",
                " ",
                " ",
                7);
        assertTrue(business.contains("tab=approval"));
        assertFalse(business.contains("searchType="));
        assertFalse(business.contains("keyword="));

        String settlement = invokeUrl(
                "settlementListRedirectUrl",
                " ",
                " ",
                " ",
                8);
        assertFalse(settlement.contains("keyword="));
        assertFalse(settlement.contains("status="));
        assertFalse(settlement.contains("period="));
    }

    private void assertBlankOptionalUrl(
            String methodName,
            String firstName,
            String secondName,
            String thirdName,
            int page) {

        String url = invokeUrl(methodName, " ", " ", " ", page);
        assertFalse(url.contains(firstName + "="));
        assertFalse(url.contains(secondName + "="));
        assertFalse(url.contains(thirdName + "="));
    }

    private String invokeUrl(String methodName, Object... args) {
        return ReflectionTestUtils.invokeMethod(controller, methodName, args);
    }
}
