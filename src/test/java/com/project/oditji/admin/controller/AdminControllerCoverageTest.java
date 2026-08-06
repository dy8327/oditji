package com.project.oditji.admin.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import com.project.oditji.admin.service.AdminService;
import com.project.oditji.admin.vo.AdminVO;
import com.project.oditji.admin.vo.ContentManageVO;
import com.project.oditji.admin.vo.PlatformVO;
import com.project.oditji.admin.vo.PopularClickVO;
import com.project.oditji.admin.vo.VisitorTrendVO;

/** 관리자 대시보드, 일괄 처리, 정산, 모니터링과 콘텐츠 관리 분기를 검증합니다. */
@ExtendWith(MockitoExtension.class)
class AdminControllerCoverageTest {

    @Mock
    private AdminService adminService;

    private AdminController controller;

    @BeforeEach
    void setUp() {
        controller = new AdminController(adminService);
    }

    @Test
    void adminMainAndMemberListShouldPopulateModel() {
        AdminVO dashboard = new AdminVO();
        when(adminService.getDashboardStats()).thenReturn(dashboard);
        ExtendedModelMap dashboardModel = new ExtendedModelMap();

        assertEquals("admin/main/adminMain", controller.adminMain(dashboardModel));
        assertSame(dashboard, dashboardModel.get("adminMain"));
        assertEquals("main", dashboardModel.get("activeMenu"));

        when(adminService.getMemberListCount("홍", "name", "ACTIVE", "general"))
                .thenReturn(12);
        when(adminService.getMemberList("홍", "name", "ACTIVE", "general", 2, 10))
                .thenReturn(List.of());
        ExtendedModelMap memberModel = new ExtendedModelMap();

        assertEquals(
                "admin/member/memberManage",
                controller.memberList(
                        memberModel,
                        "홍",
                        "name",
                        "ACTIVE",
                        "general",
                        2));
        assertEquals("member", memberModel.get("activeMenu"));
        assertEquals("name", memberModel.get("searchType"));
        assertEquals("ACTIVE", memberModel.get("status"));
        assertEquals("general", memberModel.get("memberType"));
    }

    @Test
    void memberBulkActionShouldHandleMissingSelectionSkippedAndInvalidAction() {
        RedirectAttributesModelMap emptyRedirect = new RedirectAttributesModelMap();
        String emptyView = controller.memberBulkAction(
                "suspend",
                null,
                "홍",
                "name",
                "ACTIVE",
                "general",
                3,
                emptyRedirect);
        assertTrue(emptyView.startsWith("redirect:/admin/member/list?page=3"));
        assertTrue(emptyView.contains("searchType=name"));
        assertTrue(emptyView.contains("status=ACTIVE"));
        assertTrue(emptyView.contains("memberType=general"));
        assertEquals(
                "선택된 회원이 없습니다.",
                emptyRedirect.getFlashAttributes().get("message"));

        List<Long> memberNos = List.of(1L, 2L, 3L);
        when(adminService.bulkMemberAction(memberNos, "restore")).thenReturn(1);
        RedirectAttributesModelMap successRedirect = new RedirectAttributesModelMap();
        controller.memberBulkAction(
                "restore",
                memberNos,
                null,
                null,
                null,
                null,
                1,
                successRedirect);
        assertEquals(
                "2명의 회원을 처리했습니다. (자동삭제 예정 회원 1명은 처리에서 제외되었습니다.)",
                successRedirect.getFlashAttributes().get("message"));

        when(adminService.bulkMemberAction(memberNos, "invalid"))
                .thenThrow(new IllegalArgumentException("지원하지 않는 일괄 작업입니다."));
        RedirectAttributesModelMap failureRedirect = new RedirectAttributesModelMap();
        controller.memberBulkAction(
                "invalid",
                memberNos,
                null,
                null,
                null,
                null,
                1,
                failureRedirect);
        assertEquals(
                "지원하지 않는 일괄 작업입니다.",
                failureRedirect.getFlashAttributes().get("message"));
    }

    @Test
    void settlementActionsShouldRetainFiltersAndExposeServiceErrors() {
        RedirectAttributesModelMap confirmRedirect = new RedirectAttributesModelMap();
        String confirmView = controller.settlementConfirm(
                10L,
                "상점",
                "WAITING",
                "month",
                4,
                confirmRedirect);
        assertTrue(confirmView.startsWith("redirect:/admin/settlement/main?page=4"));
        assertTrue(confirmView.contains("status=WAITING"));
        assertTrue(confirmView.contains("period=month"));
        verify(adminService).confirmSettlement(10L);
        assertEquals(
                "정산금 지급 완료 처리했습니다.",
                confirmRedirect.getFlashAttributes().get("message"));

        doThrow(new IllegalStateException("이미 처리된 요청입니다."))
                .when(adminService)
                .rejectSettlement(11L, "계좌 오류");
        RedirectAttributesModelMap rejectRedirect = new RedirectAttributesModelMap();
        assertEquals(
                "redirect:/admin/settlement/main?page=2&status=WAITING",
                controller.settlementReject(
                        11L,
                        "계좌 오류",
                        null,
                        "WAITING",
                        null,
                        2,
                        rejectRedirect));
        assertEquals(
                "이미 처리된 요청입니다.",
                rejectRedirect.getFlashAttributes().get("message"));
    }

    @Test
    void monitoringShouldBuildChartJsonForJsp() {
        VisitorTrendVO visitor = new VisitorTrendVO();
        visitor.setAccessDate("2026-08-06");
        visitor.setVisitorCount(15L);
        PopularClickVO click = new PopularClickVO();
        click.setProductName("테스트 상품");
        click.setClickCount(7L);

        when(adminService.getMonitoringList()).thenReturn(List.of());
        when(adminService.getVisitorTrend()).thenReturn(List.of(visitor));
        when(adminService.getPopularProductClicks()).thenReturn(List.of(click));
        ExtendedModelMap model = new ExtendedModelMap();

        assertEquals(
                "admin/monitoring/monitoring",
                controller.monitoring(model));
        assertEquals("monitoring", model.get("activeMenu"));
        assertEquals(List.of(visitor), model.get("visitorTrend"));
        assertEquals(List.of(click), model.get("popularClicks"));
        assertTrue(model.get("visitorTrendJson").toString().contains("2026-08-06"));
        assertTrue(model.get("visitorTrendJson").toString().contains("15"));
        assertTrue(model.get("popularClicksJson").toString().contains("테스트 상품"));
        assertTrue(model.get("popularClicksJson").toString().contains("7"));
    }

    @Test
    void contentManagementShouldMapRequestValuesToServiceObjects() {
        List<ContentManageVO> contents = List.of(new ContentManageVO());
        List<PlatformVO> platforms = List.of(new PlatformVO());
        when(adminService.getContentList("영화")).thenReturn(contents);
        when(adminService.getPlatformList()).thenReturn(platforms);
        ExtendedModelMap model = new ExtendedModelMap();

        assertEquals(
                "admin/content/contentManage",
                controller.contentList(model, "영화"));
        assertSame(contents, model.get("contentList"));
        assertSame(platforms, model.get("platformList"));

        assertEquals(
                "redirect:/admin/content/list",
                controller.contentUpdate(
                        100L,
                        "수정 제목",
                        "MOVIE",
                        "드라마",
                        "배우1, 배우2",
                        "줄거리",
                        120,
                        "2026-08-06",
                        List.of(1L, 2L)));
        ArgumentCaptor<ContentManageVO> contentCaptor =
                ArgumentCaptor.forClass(ContentManageVO.class);
        verify(adminService).updateContent(
                contentCaptor.capture(),
                org.mockito.ArgumentMatchers.eq(List.of(1L, 2L)));
        ContentManageVO content = contentCaptor.getValue();
        assertEquals(100L, content.getContentNo());
        assertEquals("수정 제목", content.getTitle());
        assertEquals("MOVIE", content.getContentType());
        assertEquals("드라마", content.getGenreText());
        assertEquals("배우1, 배우2", content.getCastNames());
        assertEquals("줄거리", content.getOverview());
        assertEquals(120, content.getRuntime());

        assertEquals(
                "redirect:/admin/content/list",
                controller.contentPlatformRegister("새 플랫폼", "https://example.com", null));
        ArgumentCaptor<PlatformVO> registerCaptor =
                ArgumentCaptor.forClass(PlatformVO.class);
        verify(adminService).registerPlatform(registerCaptor.capture());
        assertEquals("새 플랫폼", registerCaptor.getValue().getPlatformName());
        assertEquals("https://example.com", registerCaptor.getValue().getSiteUrl());
        assertEquals("Y", registerCaptor.getValue().getIsActive());

        assertEquals(
                "redirect:/admin/content/list",
                controller.contentPlatformUpdate(3L, "https://updated.example.com", "N"));
        ArgumentCaptor<PlatformVO> updateCaptor =
                ArgumentCaptor.forClass(PlatformVO.class);
        verify(adminService).updatePlatform(updateCaptor.capture());
        assertEquals(3L, updateCaptor.getValue().getPlatformNo());
        assertEquals("https://updated.example.com", updateCaptor.getValue().getSiteUrl());
        assertEquals("N", updateCaptor.getValue().getIsActive());
    }
}
