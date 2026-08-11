package com.project.oditji.admin.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;

import com.project.oditji.admin.service.AdminService;
import com.project.oditji.admin.vo.ContentManageVO;
import com.project.oditji.admin.vo.PlatformVO;
import com.project.oditji.common.vo.PageVO;

/** 관리자 컨트롤러의 페이징 보정과 콘텐츠/플랫폼 관리 잔여 경로를 보완합니다. */
class AdminControllerFinalGapCoverageTest {

    private static final String CONTENT_LIST_REDIRECT = "redirect:/admin/content/list";

    private AdminService adminService;
    private AdminController controller;

    @BeforeEach
    void setUp() {
        adminService = mock(AdminService.class);
        controller = new AdminController(adminService);
    }

    @Test
    void memberPaginationAndListShouldNormalizeInvalidRequestedPage() {
        PageVO pagination = ReflectionTestUtils.invokeMethod(controller, "buildMemberPagination", -5, 0);
        assertEquals(1, pagination.getCurrentPage());

        Model model = new ExtendedModelMap();
        assertEquals(
                "admin/member/memberManage",
                controller.memberList(model, null, "all", null, "all", -5));
        assertTrue(model.containsAttribute("pagination"));

        verify(adminService).getMemberList(null, "all", null, "all", 1, 10);
    }

    @Test
    void eventAndProductListsShouldCreatePaginationForZeroPage() {
        Model eventModel = new ExtendedModelMap();
        Model productModel = new ExtendedModelMap();

        assertEquals(
                "admin/event/eventManage",
                controller.eventList(eventModel, "waiting", "month", "key", 0));
        assertEquals(
                "admin/goods/productManage",
                controller.productList(productModel, "waiting", "key", "name", 0));

        assertTrue(eventModel.containsAttribute("pagination"));
        assertTrue(productModel.containsAttribute("pagination"));
    }

    @Test
    void contentUpdateAndPlatformManagementShouldDelegateAndReturnContentListRedirect() {
        assertEquals(
                CONTENT_LIST_REDIRECT,
                controller.contentUpdate(
                        7L,
                        "제목",
                        "MOVIE",
                        "액션",
                        "배우",
                        "설명",
                        120,
                        "2026-08-11",
                        null));
        verify(adminService).updateContent(any(ContentManageVO.class), isNull());

        assertEquals(
                CONTENT_LIST_REDIRECT,
                controller.contentPlatformRegister("Platform", "https://example.com", null));
        verify(adminService).registerPlatform(any(PlatformVO.class));

        assertEquals(
                CONTENT_LIST_REDIRECT,
                controller.contentPlatformUpdate(3L, "https://updated.example.com", "N"));
        verify(adminService).updatePlatform(any(PlatformVO.class));
    }
}
