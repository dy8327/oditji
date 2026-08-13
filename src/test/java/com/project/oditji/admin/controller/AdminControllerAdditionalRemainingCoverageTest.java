package com.project.oditji.admin.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

import com.project.oditji.admin.service.AdminService;
import com.project.oditji.admin.vo.PlatformVO;

/**
 * 관리자 컨트롤러의 각 목록 리다이렉트 URL 조건과 플랫폼 기본 활성값 분기를 보완합니다.
 */
class AdminControllerAdditionalRemainingCoverageTest {

    private AdminService adminService;
    private AdminController controller;

    @BeforeEach
    void setUp() {
        adminService = mock(AdminService.class);
        controller = new AdminController(adminService);
    }

    @Test
    void memberRedirectShouldCoverNullBlankAndPresentFilters() {
        String minimal = invokeUrl(
                "memberListRedirectUrl",
                null,
                " ",
                null,
                "   ",
                2);

        assertTrue(minimal.contains("page=2"));
        assertFalse(minimal.contains("keyword="));
        assertFalse(minimal.contains("searchType="));
        assertFalse(minimal.contains("status="));
        assertFalse(minimal.contains("memberType="));

        String full = invokeUrl(
                "memberListRedirectUrl",
                "keyword",
                "id",
                "ACTIVE",
                "USER",
                3);

        assertTrue(full.contains("keyword=keyword"));
        assertTrue(full.contains("searchType=id"));
        assertTrue(full.contains("status=ACTIVE"));
        assertTrue(full.contains("memberType=USER"));
    }

    @Test
    void reviewAndProductReviewRedirectsShouldCoverEveryOptionalFilter() {
        assertOptionalUrl(
                "reviewListRedirectUrl",
                "/admin/review/list",
                "report",
                "keyword",
                "content",
                4);

        assertOptionalUrl(
                "productReviewListRedirectUrl",
                "/admin/productReview/list",
                "report",
                "keyword",
                "product",
                5);
    }

    @Test
    void eventAndProductRedirectsShouldCoverBlankAndPresentFilters() {
        String emptyEvent = invokeUrl(
                "eventListRedirectUrl",
                " ",
                null,
                "   ",
                1);

        assertFalse(emptyEvent.contains("tab="));
        assertFalse(emptyEvent.contains("period="));
        assertFalse(emptyEvent.contains("keyword="));

        String fullEvent = invokeUrl(
                "eventListRedirectUrl",
                "approval",
                "30",
                "summer",
                2);

        assertTrue(fullEvent.contains("tab=approval"));
        assertTrue(fullEvent.contains("period=30"));
        assertTrue(fullEvent.contains("keyword=summer"));

        assertOptionalUrl(
                "productListRedirectUrl",
                "/admin/product/list",
                "approval",
                "goods",
                "name",
                6);
    }

    @Test
    void businessAndSettlementRedirectsShouldCoverBothSides() {
        String business = invokeUrl(
                "businessListRedirectUrl",
                "approval",
                null,
                " ",
                7);

        assertTrue(business.contains("tab=approval"));
        assertFalse(business.contains("searchType="));
        assertFalse(business.contains("keyword="));

        String businessFull = invokeUrl(
                "businessListRedirectUrl",
                "info",
                "business",
                "name",
                8);

        assertTrue(businessFull.contains("searchType=name"));
        assertTrue(businessFull.contains("keyword=business"));

        String settlement = invokeUrl(
                "settlementListRedirectUrl",
                null,
                " ",
                null,
                9);

        assertFalse(settlement.contains("keyword="));
        assertFalse(settlement.contains("status="));
        assertFalse(settlement.contains("period="));

        String settlementFull = invokeUrl(
                "settlementListRedirectUrl",
                "store",
                "REQUESTED",
                "2026-08",
                10);

        assertTrue(settlementFull.contains("keyword=store"));
        assertTrue(settlementFull.contains("status=REQUESTED"));
        assertTrue(settlementFull.contains("period=2026-08"));
    }

    @Test
    void platformRegistrationShouldDefaultNullActiveFlagAndKeepExplicitFlag() {
        controller.contentPlatformRegister(
                "Platform A",
                "https://a.example",
                null);

        controller.contentPlatformRegister(
                "Platform B",
                "https://b.example",
                "N");

        ArgumentCaptor<PlatformVO> captor =
                ArgumentCaptor.forClass(
                        PlatformVO.class);

        verify(adminService,
                times(2))
                .registerPlatform(captor.capture());

        assertEquals(
                "Y",
                captor.getAllValues()
                        .get(0)
                        .getIsActive());

        assertEquals(
                "N",
                captor.getAllValues()
                        .get(1)
                        .getIsActive());
    }

    private void assertOptionalUrl(
            String methodName,
            String path,
            String tab,
            String keyword,
            String searchType,
            int page) {

        String minimal = invokeUrl(
                methodName,
                " ",
                " ",
                null,
                page);

        assertTrue(minimal.startsWith(path));
        assertFalse(minimal.contains("tab="));
        assertFalse(minimal.contains("keyword="));
        assertFalse(minimal.contains("searchType="));

        String full = invokeUrl(
                methodName,
                tab,
                keyword,
                searchType,
                page);

        assertTrue(full.contains("tab=" + tab));
        assertTrue(full.contains("keyword=" + keyword));
        assertTrue(full.contains("searchType=" + searchType));
    }

    private String invokeUrl(
            String methodName,
            Object... arguments) {

        return ReflectionTestUtils.invokeMethod(
                controller,
                methodName,
                arguments);
    }
}
