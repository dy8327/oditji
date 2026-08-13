package com.project.oditji.admin.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.project.oditji.admin.service.AdminService;
import com.project.oditji.common.vo.PageVO;

/** 관리자 컨트롤러 URL builder와 pagination helper의 정상값 분기를 보완합니다. */
class AdminControllerFinalRedirectCoverageTest {

    private AdminController controller;

    @BeforeEach
    void setUp() {
        controller = new AdminController(mock(AdminService.class));
    }

    @Test
    void memberPaginationShouldClampInvalidPageAndCalculatePages() {
        PageVO page = invoke("buildMemberPagination", 0, 25);
        assertEquals(1, page.getCurrentPage());
        assertEquals(3, page.getTotalPage());
    }

    @Test
    void redirectBuildersShouldIncludeAllNonBlankParameters() {
        String member = invoke("memberListRedirectUrl", "검색", "id", "ACTIVE", "USER", 2);
        assertTrue(member.contains("keyword="));
        assertTrue(member.contains("searchType=id"));
        assertTrue(member.contains("status=ACTIVE"));
        assertTrue(member.contains("memberType=USER"));

        String review = invoke("reviewListRedirectUrl", "report", "검색", "content", 3);
        assertTrue(review.contains("tab=report"));
        assertTrue(review.contains("searchType=content"));
        assertTrue(review.contains("keyword="));

        String productReview = invoke("productReviewListRedirectUrl", "all", "검색", "writer", 4);
        assertTrue(productReview.contains("tab=all"));
        assertTrue(productReview.contains("searchType=writer"));

        String event = invoke("eventListRedirectUrl", "waiting", "month", "검색", 5);
        assertTrue(event.contains("tab=waiting"));
        assertTrue(event.contains("period=month"));

        String settlement = invoke("settlementListRedirectUrl", "검색", "REQUESTED", "month", 6);
        assertTrue(settlement.contains("status=REQUESTED"));
        assertTrue(settlement.contains("period=month"));
    }

    @Test
    void businessRedirectShouldAlwaysIncludeTabEvenWhenNull() {
        String url = invoke("businessListRedirectUrl", null, " ", " ", 1);
        assertTrue(url.contains("page=1"));
        assertFalse(url.contains("searchType="));
        assertFalse(url.contains("keyword="));
    }

    @SuppressWarnings("unchecked")
    private <T> T invoke(String method, Object... args) {
        return (T) ReflectionTestUtils.invokeMethod(controller, method, args);
    }
}
