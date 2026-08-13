package com.project.oditji.admin.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.project.oditji.admin.dao.AdminDAO;
import com.project.oditji.admin.vo.AdminVO;
import com.project.oditji.admin.vo.BusinessManageVO;
import com.project.oditji.admin.vo.ContentManageVO;
import com.project.oditji.admin.vo.MemberManageVO;
import com.project.oditji.admin.vo.MonitoringVO;
import com.project.oditji.admin.vo.PlatformVO;
import com.project.oditji.admin.vo.PopularClickVO;
import com.project.oditji.admin.vo.ReviewManageVO;
import com.project.oditji.admin.vo.VisitorTrendVO;
import com.project.oditji.common.util.DateTimeUtil;
import com.project.oditji.common.vo.SettlementRequestVO;
import com.project.oditji.notification.service.NotificationService;

/**
 * 관리자 회원·리뷰·이벤트·상품·사업자·정산 처리의 정상 및 실패 분기를 검증합니다.
 */
@ExtendWith(MockitoExtension.class)
class AdminServiceImplTest {

    @Mock
    private AdminDAO adminDAO;

    @Mock
    private NotificationService notificationService;

    @TempDir
    Path tempDirectory;

    private AdminServiceImpl adminService;

    @BeforeEach
    void setUp() {
        adminService = new AdminServiceImpl(
                adminDAO,
                notificationService,
                tempDirectory.toString());
    }

    @Test
    void dashboardAndSimpleStatisticsShouldDelegate() {
        AdminVO dashboard = new AdminVO();
        when(adminDAO.selectDashboardStats()).thenReturn(dashboard);

        assertSame(dashboard, adminService.getDashboardStats());

        adminService.getMemberStats();
        adminService.getContentReviewStats();
        adminService.getProductReviewStats();
        adminService.getEventStats();
        adminService.getProductStats();
        adminService.getOrderStats();
        adminService.getBusinessStats();
        adminService.getSettlementStats();

        verify(adminDAO).selectMemberStats();
        verify(adminDAO).selectContentReviewStats();
        verify(adminDAO).selectProductReviewStats();
        verify(adminDAO).selectEventStats();
        verify(adminDAO).selectProductStats();
        verify(adminDAO).selectOrderStats();
        verify(adminDAO).selectBusinessStats();
        verify(adminDAO).selectSettlementStats();
    }

    @Test
    void getMemberListShouldNormalizePageAndCalculateDeleteDays() {
        MemberManageVO withdrawn = new MemberManageVO();
        withdrawn.setStatus("WITHDRAWN");
        withdrawn.setWithdrawnAt(LocalDateTime.now(DateTimeUtil.KOREA_ZONE).minusDays(8));

        MemberManageVO active = new MemberManageVO();
        active.setStatus("ACTIVE");

        when(adminDAO.selectMemberList(any()))
                .thenReturn(List.of(withdrawn, active));

        List<MemberManageVO> result = adminService.getMemberList(
                "kim", "name", "ALL", "USER", 0, 20);

        assertEquals(2, result.size());
        assertEquals(0, withdrawn.getRemainingDeleteDays().intValue());
        assertEquals(null, active.getRemainingDeleteDays());

        @SuppressWarnings({ "unchecked", "rawtypes" })
        ArgumentCaptor<Map<String, Object>> captor =
                (ArgumentCaptor) ArgumentCaptor.forClass(Map.class);
        verify(adminDAO).selectMemberList(captor.capture());

        Map<String, Object> param = captor.getValue();
        assertEquals("kim", param.get("keyword"));
        assertEquals("name", param.get("searchType"));
        assertEquals("ALL", param.get("status"));
        assertEquals("USER", param.get("memberType"));
        assertEquals(0, param.get("offset"));
        assertEquals(20, param.get("pageSize"));
    }

    @Test
    void memberCountAndStatusChangesShouldDelegate() {
        when(adminDAO.selectMemberListCount(any())).thenReturn(12);

        assertEquals(
                12,
                adminService.getMemberListCount("a", "id", "ACTIVE", "USER"));

        adminService.suspendMember(1L);
        adminService.restoreMember(2L);

        verify(adminDAO).updateMemberStatus(1L, "BLOCKED");
        verify(adminDAO).updateMemberStatus(2L, "ACTIVE");
    }

    @Test
    void bulkMemberActionShouldReturnZeroForMissingSelection() {
        assertEquals(0, adminService.bulkMemberAction(null, "suspend"));
        assertEquals(0, adminService.bulkMemberAction(List.of(), "suspend"));
        verify(adminDAO, never()).selectWithdrawnMemberNos(any());
    }

    @Test
    void bulkMemberActionShouldSkipWithdrawnMembersAndProcessOthers() {
        List<Long> memberNos = List.of(1L, 2L, 3L);
        when(adminDAO.selectWithdrawnMemberNos(memberNos))
                .thenReturn(List.of(2L));

        int skipped = adminService.bulkMemberAction(memberNos, "suspend");

        assertEquals(1, skipped);
        verify(adminDAO).updateMemberStatus(1L, "BLOCKED");
        verify(adminDAO, never()).updateMemberStatus(2L, "BLOCKED");
        verify(adminDAO).updateMemberStatus(3L, "BLOCKED");
    }

    @Test
    void bulkMemberActionShouldSupportRestoreDeleteAndRejectUnknownAction() {
        when(adminDAO.selectWithdrawnMemberNos(List.of(1L)))
                .thenReturn(List.of());
        adminService.bulkMemberAction(List.of(1L), "restore");
        verify(adminDAO).updateMemberStatus(1L, "ACTIVE");

        when(adminDAO.selectWithdrawnMemberNos(List.of(2L)))
                .thenReturn(List.of());
        adminService.bulkMemberAction(List.of(2L), "delete");
        verify(adminDAO).deleteMember(2L);

        List<Long> unknownActionMemberNos = List.of(3L);
        when(adminDAO.selectWithdrawnMemberNos(unknownActionMemberNos))
                .thenReturn(List.of());
        assertThrows(
                IllegalArgumentException.class,
                () -> adminService.bulkMemberAction(unknownActionMemberNos, "unknown"));
    }

    @Test
    void deleteMemberShouldDeleteForeignKeyDataBeforeMember() {
        adminService.deleteMember(10L);

        InOrder order = inOrder(adminDAO);
        order.verify(adminDAO).deleteReviewReportByMemberNo(10L);
        order.verify(adminDAO).deleteSettlementByMemberNo(10L);
        order.verify(adminDAO).deleteProductReviewByMemberNo(10L);
        order.verify(adminDAO).deleteCancelRequestByMemberNo(10L);
        order.verify(adminDAO).deleteDeliveryByMemberNo(10L);
        order.verify(adminDAO).deleteOrderItemByMemberNo(10L);
        order.verify(adminDAO).deleteOrdersByMemberNo(10L);
        order.verify(adminDAO).deleteCartItemByMemberNo(10L);
        order.verify(adminDAO).deleteCartByMemberNo(10L);
        order.verify(adminDAO).deleteProductWishByMemberNo(10L);
        order.verify(adminDAO).deleteFavoriteByMemberNo(10L);
        order.verify(adminDAO).deleteReviewByMemberNo(10L);
        order.verify(adminDAO).deleteContentViewHistoryByMemberNo(10L);
        order.verify(adminDAO).deleteProductClickLogByMemberNo(10L);
        order.verify(adminDAO).deleteAccessLogByMemberNo(10L);
        order.verify(adminDAO).deleteAdminLogByAdminNo(10L);
        order.verify(adminDAO).deleteMemberPlatformByMemberNo(10L);
        order.verify(adminDAO).deleteMemberSocialByMemberNo(10L);
        order.verify(adminDAO).deleteSubscriptionResultByMemberNo(10L);
        order.verify(adminDAO).deleteNotificationSettingByMemberNo(10L);
        order.verify(adminDAO).deleteSearchKeywordHistoryByMemberNo(10L);
        order.verify(adminDAO).deleteChatRoomReadStateByMemberNo(10L);
        order.verify(adminDAO).deleteMember(10L);
    }

    @Test
    void deleteExpiredWithdrawMembersShouldDeleteEverySelectedMember() {
        when(adminDAO.selectExpiredWithdrawMembers())
                .thenReturn(List.of(11L, 12L));

        adminService.deleteExpiredWithdrawMembers();

        verify(adminDAO).deleteMember(11L);
        verify(adminDAO).deleteMember(12L);
    }

    @Test
    void contentReviewListShouldSelectNormalOrReportQuery() {
        List<ReviewManageVO> normal = List.of(new ReviewManageVO());
        List<ReviewManageVO> report = List.of(new ReviewManageVO(), new ReviewManageVO());
        when(adminDAO.selectContentReviewList(any())).thenReturn(normal);
        when(adminDAO.selectContentReviewReportList(any())).thenReturn(report);

        assertSame(normal, adminService.getContentReviewList(
                "all", "key", "title", 2, 10));
        assertSame(report, adminService.getContentReviewList(
                "report", "key", "title", 2, 10));

        when(adminDAO.selectContentReviewListCount(any())).thenReturn(3);
        when(adminDAO.selectContentReviewReportListCount(any())).thenReturn(4);
        assertEquals(3, adminService.getContentReviewListCount("all", "", "all"));
        assertEquals(4, adminService.getContentReviewListCount("report", "", "all"));
    }

    @Test
    void approveContentReviewReportShouldUpdateDeleteAndNotifyReporters() {
        when(adminDAO.selectWaitingContentReviewReporterMemberNos(50L))
                .thenReturn(List.of(1L, 2L));
        when(adminDAO.updateContentReviewReportStatus(50L, "ACCEPTED"))
                .thenReturn(2);

        adminService.approveContentReviewReport(50L);

        verify(adminDAO).deleteContentReview(50L);
        verify(notificationService).createForMember(
                eq(1L),
                eq("CONTENT_REVIEW_REPORT_PROCESSED"),
                eq("리뷰 신고 검토 완료"),
                any(),
                eq(null),
                eq("CONTENT_REVIEW"),
                eq(50L));
        verify(notificationService).createForMember(
                eq(2L),
                eq("CONTENT_REVIEW_REPORT_PROCESSED"),
                eq("리뷰 신고 검토 완료"),
                any(),
                eq(null),
                eq("CONTENT_REVIEW"),
                eq(50L));
    }

    @Test
    void rejectContentReviewReportShouldFailWhenNoWaitingReportExists() {
        when(adminDAO.selectWaitingContentReviewReporterMemberNos(51L))
                .thenReturn(List.of());
        when(adminDAO.updateContentReviewReportStatus(51L, "REJECTED"))
                .thenReturn(0);

        assertThrows(
                IllegalStateException.class,
                () -> adminService.rejectContentReviewReport(51L));

        verify(notificationService, never()).createForMember(
                any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void bulkContentReviewActionShouldCountAlreadyProcessedReports() {
        when(adminDAO.selectWaitingContentReviewReporterMemberNos(1L))
                .thenReturn(List.of());
        when(adminDAO.selectWaitingContentReviewReporterMemberNos(2L))
                .thenReturn(List.of());
        when(adminDAO.updateContentReviewReportStatus(1L, "REJECTED"))
                .thenReturn(1);
        when(adminDAO.updateContentReviewReportStatus(2L, "REJECTED"))
                .thenReturn(0);

        assertEquals(
                1,
                adminService.bulkContentReviewAction(List.of(1L, 2L), "reject"));
        assertEquals(0, adminService.bulkContentReviewAction(null, "reject"));
    }

    @Test
    void productReviewListAndApprovalShouldUseProductQueries() {
        List<ReviewManageVO> reportList = List.of(new ReviewManageVO());
        when(adminDAO.selectProductReviewReportList(any())).thenReturn(reportList);
        when(adminDAO.selectProductReviewReportListCount(any())).thenReturn(5);

        assertSame(reportList, adminService.getProductReviewList(
                "report", "key", "content", 1, 10));
        assertEquals(5, adminService.getProductReviewListCount(
                "report", "key", "content"));

        when(adminDAO.selectWaitingProductReviewReporterMemberNos(60L))
                .thenReturn(List.of(3L));
        when(adminDAO.updateProductReviewReportStatus(60L, "ACCEPTED"))
                .thenReturn(1);

        adminService.approveProductReviewReport(60L);

        verify(adminDAO).adminDeleteProductReview(60L);
        verify(notificationService).createForMember(
                eq(3L),
                eq("PRODUCT_REVIEW_REPORT_PROCESSED"),
                eq("리뷰 신고 검토 완료"),
                any(),
                eq(null),
                eq("PRODUCT_REVIEW"),
                eq(60L));
    }

    @Test
    void bulkProductReviewActionShouldDeleteAndRejectInvalidAction() {
        assertEquals(
                0,
                adminService.bulkProductReviewAction(List.of(10L, 11L), "delete"));
        verify(adminDAO).adminDeleteProductReview(10L);
        verify(adminDAO).adminDeleteProductReview(11L);

        List<Long> unknownActionReviewNos = List.of(12L);
        assertThrows(
                IllegalArgumentException.class,
                () -> adminService.bulkProductReviewAction(unknownActionReviewNos, "unknown"));
    }

    @Test
    void eventApprovalShouldValidateUpdateAndNotifyOwner() {
        assertThrows(
                IllegalArgumentException.class,
                () -> adminService.approveEvent(null));
        assertThrows(
                IllegalArgumentException.class,
                () -> adminService.rejectEvent(0L));

        when(adminDAO.updateEventStatus(20L, "APPROVED")).thenReturn(0);
        assertThrows(
                IllegalStateException.class,
                () -> adminService.approveEvent(20L));

        when(adminDAO.updateEventStatus(21L, "APPROVED")).thenReturn(1);
        adminService.approveEvent(21L);
        verify(notificationService).createForEventOwner(
                21L,
                "EVENT_APPROVED",
                "이벤트 승인 완료",
                "요청한 이벤트가 승인되었습니다.",
                "/business/event/list",
                "EVENT",
                21L);

        when(adminDAO.updateEventStatus(22L, "REJECTED")).thenReturn(1);
        adminService.rejectEvent(22L);
        verify(notificationService).createForEventOwner(
                22L,
                "EVENT_REJECTED",
                "이벤트 승인 반려",
                "요청한 이벤트가 반려되었습니다. 이벤트 목록을 확인해주세요.",
                "/business/event/list",
                "EVENT",
                22L);
    }

    @Test
    void productApprovalShouldValidateNormalApprovalAndFailure() {
        assertThrows(
                IllegalArgumentException.class,
                () -> adminService.approveProduct(0L));

        when(adminDAO.selectProductStatusByNo(30L)).thenReturn(null);
        assertThrows(
                IllegalArgumentException.class,
                () -> adminService.approveProduct(30L));

        when(adminDAO.selectProductStatusByNo(31L)).thenReturn("WAITING");
        when(adminDAO.updateProductStatus(31L, "APPROVED")).thenReturn(0);
        assertThrows(
                IllegalStateException.class,
                () -> adminService.approveProduct(31L));

        when(adminDAO.selectProductStatusByNo(32L)).thenReturn("WAITING");
        when(adminDAO.updateProductStatus(32L, "APPROVED")).thenReturn(1);
        adminService.approveProduct(32L);
        verify(notificationService).createForProductOwner(
                32L,
                "PRODUCT_APPROVED",
                "상품 승인 완료",
                "등록 또는 수정한 상품이 승인되었습니다.",
                "/business/product/list",
                "PRODUCT",
                32L);
    }

    @Test
    void deleteRequestedProductShouldBlockExistingOrders() {
        when(adminDAO.selectProductStatusByNo(40L))
                .thenReturn("DELETE_REQUESTED");
        when(adminDAO.countOrderItemByProductNo(40L)).thenReturn(1);

        assertThrows(
                IllegalStateException.class,
                () -> adminService.approveProduct(40L));

        verify(adminDAO, never()).deleteProduct(40L);
    }

    @Test
    void deleteRequestedProductShouldDeleteChildrenAndPhysicalImage() throws Exception {
        Path image = tempDirectory.resolve("product-image.png");
        Files.writeString(image, "image");

        when(adminDAO.selectProductStatusByNo(41L))
                .thenReturn("DELETE_REQUESTED");
        when(adminDAO.countOrderItemByProductNo(41L)).thenReturn(0);
        when(adminDAO.selectProductImagePathList(41L))
                .thenReturn(List.of("/uploads/product/product-image.png", " "));
        when(adminDAO.deleteProduct(41L)).thenReturn(1);

        adminService.approveProduct(41L);

        verify(adminDAO).deleteProductReviewByProductNo(41L);
        verify(adminDAO).deleteCartItemByProductNo(41L);
        verify(adminDAO).deleteProductWishByProductNo(41L);
        verify(adminDAO).deleteProductClickLogByProductNo(41L);
        verify(adminDAO).deleteEventProductByProductNo(41L);
        verify(adminDAO).deleteProductImageByProductNo(41L);
        verify(adminDAO).deleteProduct(41L);
        assertFalse(Files.exists(image));
    }

    @Test
    void rejectProductShouldRestoreDeleteRequestOrUseRejectedStatus() {
        when(adminDAO.selectProductStatusByNo(50L))
                .thenReturn("DELETE_REQUESTED");
        when(adminDAO.updateProductStatus(50L, "APPROVED")).thenReturn(1);
        adminService.rejectProduct(50L);
        verify(notificationService).createForProductOwner(
                50L,
                "PRODUCT_DELETE_REJECTED",
                "상품 삭제 요청 반려",
                "상품 삭제 요청이 반려되어 기존 승인 상태로 복구되었습니다.",
                "/business/product/list",
                "PRODUCT",
                50L);

        when(adminDAO.selectProductStatusByNo(51L)).thenReturn("WAITING");
        when(adminDAO.updateProductStatus(51L, "REJECTED")).thenReturn(1);
        adminService.rejectProduct(51L);
        verify(notificationService).createForProductOwner(
                51L,
                "PRODUCT_REJECTED",
                "상품 승인 반려",
                "상품 승인 요청이 반려되었습니다. 상품 목록을 확인해주세요.",
                "/business/product/list",
                "PRODUCT",
                51L);
    }

    @Test
    void businessListShouldRefreshSettlementRatesOnlyWhenGradeChanges() {
        List<BusinessManageVO> list = List.of(new BusinessManageVO());
        when(adminDAO.updateBusinessGradesBySales()).thenReturn(1, 0);
        when(adminDAO.selectBusinessList(any())).thenReturn(list);

        assertSame(list, adminService.getBusinessList("", "all", 1, 10));
        verify(adminDAO).updateCurrentMonthSettlementRates();

        assertSame(list, adminService.getBusinessList("", "all", 1, 10));
    }

    @Test
    void updateBusinessGradeShouldValidateNormalizeAndRefreshSettlement() {
        assertThrows(
                IllegalArgumentException.class,
                () -> adminService.updateBusinessGrade(null, "GOLD"));
        assertThrows(
                IllegalArgumentException.class,
                () -> adminService.updateBusinessGrade(1L, " "));
        assertThrows(
                IllegalArgumentException.class,
                () -> adminService.updateBusinessGrade(1L, "DIAMOND"));

        when(adminDAO.updateBusinessGrade(1L, "GOLD")).thenReturn(0);
        assertThrows(
                IllegalStateException.class,
                () -> adminService.updateBusinessGrade(1L, " gold "));

        when(adminDAO.updateBusinessGrade(2L, "VIP")).thenReturn(1);
        adminService.updateBusinessGrade(2L, "vip");
        verify(adminDAO).updateCurrentMonthSettlementRateByBusiness(2L);
    }

    @Test
    void automaticBusinessGradeUpdateShouldRefreshRatesOnlyWhenNeeded() {
        when(adminDAO.updateBusinessGradesBySales()).thenReturn(3, 0);

        assertEquals(3, adminService.updateBusinessGradesBySales());
        verify(adminDAO).updateCurrentMonthSettlementRates();
        assertEquals(0, adminService.updateBusinessGradesBySales());
    }

    @Test
    void businessApprovalAndRejectionShouldRequireOneUpdatedRow() {
        when(adminDAO.updateBusinessStatus(70L, "APPROVED")).thenReturn(0);
        assertThrows(
                IllegalStateException.class,
                () -> adminService.approveBusiness(70L));

        when(adminDAO.updateBusinessStatus(71L, "APPROVED")).thenReturn(1);
        adminService.approveBusiness(71L);
        verify(notificationService).createForBusiness(
                71L,
                "BUSINESS_APPROVED",
                "사업자 승인 완료",
                "사업자 가입 신청이 승인되었습니다.",
                "/member/mypage",
                "BUSINESS",
                71L);

        when(adminDAO.updateBusinessStatus(72L, "REJECTED")).thenReturn(1);
        adminService.rejectBusiness(72L);
        verify(notificationService).createForBusiness(
                72L,
                "BUSINESS_REJECTED",
                "사업자 승인 반려",
                "사업자 가입 신청이 반려되었습니다.",
                "/member/mypage",
                "BUSINESS",
                72L);
    }

    @Test
    void settlementProcessingShouldValidateArgumentsAndRequestState() {
        assertThrows(
                IllegalArgumentException.class,
                () -> adminService.confirmSettlement(0L));
        assertThrows(
                IllegalArgumentException.class,
                () -> adminService.rejectSettlement(0L, "반려 사유"));
        assertThrows(
                IllegalArgumentException.class,
                () -> adminService.rejectSettlement(1L, " "));

        when(adminDAO.selectSettlementRequest(1L)).thenReturn(null);
        assertThrows(
                IllegalStateException.class,
                () -> adminService.confirmSettlement(1L));

        SettlementRequestVO completedRequest = settlementRequest(
                2L,
                20L,
                "2026-08",
                "DONE");
        when(adminDAO.selectSettlementRequest(2L)).thenReturn(completedRequest);
        assertThrows(
                IllegalStateException.class,
                () -> adminService.confirmSettlement(2L));

        when(adminDAO.selectSettlementRequest(3L)).thenReturn(null);
        assertThrows(
                IllegalStateException.class,
                () -> adminService.rejectSettlement(3L, "계좌 확인 필요"));

        SettlementRequestVO rejectedRequest = settlementRequest(
                4L,
                40L,
                "2026-08",
                "REJECTED");
        when(adminDAO.selectSettlementRequest(4L)).thenReturn(rejectedRequest);
        assertThrows(
                IllegalStateException.class,
                () -> adminService.rejectSettlement(4L, "계좌 확인 필요"));
    }

    @Test
    void confirmSettlementShouldValidateUpdatesAndNotifyBusiness() {
        SettlementRequestVO updateFailureRequest = settlementRequest(
                10L,
                100L,
                "2026-08",
                "REQUESTED");
        when(adminDAO.selectSettlementRequest(10L)).thenReturn(updateFailureRequest);
        when(adminDAO.updateSettlementRequestStatus(10L, "DONE", null))
                .thenReturn(0);

        assertThrows(
                IllegalStateException.class,
                () -> adminService.confirmSettlement(10L));

        SettlementRequestVO itemFailureRequest = settlementRequest(
                11L,
                110L,
                "2026-08",
                "REQUESTED");
        when(adminDAO.selectSettlementRequest(11L)).thenReturn(itemFailureRequest);
        when(adminDAO.updateSettlementRequestStatus(11L, "DONE", null))
                .thenReturn(1);
        when(adminDAO.completeSettlementItems(11L)).thenReturn(0);

        assertThrows(
                IllegalStateException.class,
                () -> adminService.confirmSettlement(11L));

        SettlementRequestVO successRequest = settlementRequest(
                12L,
                120L,
                "2026-08",
                "REQUESTED");
        when(adminDAO.selectSettlementRequest(12L)).thenReturn(successRequest);
        when(adminDAO.updateSettlementRequestStatus(12L, "DONE", null))
                .thenReturn(1);
        when(adminDAO.completeSettlementItems(12L)).thenReturn(2);

        adminService.confirmSettlement(12L);

        verify(notificationService).createForBusiness(
                120L,
                "SETTLEMENT_APPROVED",
                "정산 지급 완료",
                "2026-08 정산금 지급이 완료되었습니다.",
                "/business/settlement/complete",
                "SETTLEMENT_REQUEST",
                12L);
    }

    @Test
    void rejectSettlementShouldValidateUpdatesAndNotifyBusiness() {
        SettlementRequestVO updateFailureRequest = settlementRequest(
                20L,
                200L,
                "2026-08",
                "REQUESTED");
        when(adminDAO.selectSettlementRequest(20L)).thenReturn(updateFailureRequest);
        when(adminDAO.updateSettlementRequestStatus(
                20L,
                "REJECTED",
                "계좌 확인 필요"))
                .thenReturn(0);

        assertThrows(
                IllegalStateException.class,
                () -> adminService.rejectSettlement(
                        20L,
                        "계좌 확인 필요"));

        SettlementRequestVO releaseFailureRequest = settlementRequest(
                21L,
                210L,
                "2026-08",
                "REQUESTED");
        when(adminDAO.selectSettlementRequest(21L)).thenReturn(releaseFailureRequest);
        when(adminDAO.updateSettlementRequestStatus(
                21L,
                "REJECTED",
                "계좌 확인 필요"))
                .thenReturn(1);
        when(adminDAO.releaseRejectedSettlementItems(21L)).thenReturn(0);

        assertThrows(
                IllegalStateException.class,
                () -> adminService.rejectSettlement(
                        21L,
                        "계좌 확인 필요"));

        SettlementRequestVO successRequest = settlementRequest(
                22L,
                220L,
                "2026-08",
                "REQUESTED");
        when(adminDAO.selectSettlementRequest(22L)).thenReturn(successRequest);
        when(adminDAO.updateSettlementRequestStatus(
                22L,
                "REJECTED",
                "계좌 확인 필요"))
                .thenReturn(1);
        when(adminDAO.releaseRejectedSettlementItems(22L)).thenReturn(3);

        adminService.rejectSettlement(
                22L,
                "  계좌 확인 필요  ");

        verify(notificationService).createForBusiness(
                220L,
                "SETTLEMENT_REJECTED",
                "정산 요청 반려",
                "2026-08 정산 요청이 반려되었습니다. 사유: 계좌 확인 필요",
                "/business/settlement/complete",
                "SETTLEMENT_REQUEST",
                22L);
    }

    private SettlementRequestVO settlementRequest(
            Long requestNo,
            Long businessNo,
            String settlementMonth,
            String status) {

        SettlementRequestVO request = new SettlementRequestVO();
        request.setRequestNo(requestNo);
        request.setBusinessNo(businessNo);
        request.setSettlementMonth(settlementMonth);
        request.setStatus(status);
        return request;
    }

    @Test
    void monitoringAndContentQueriesShouldDelegate() {
        List<MonitoringVO> monitoring = List.of(new MonitoringVO());
        List<VisitorTrendVO> visitors = List.of(new VisitorTrendVO());
        List<PopularClickVO> clicks = List.of(new PopularClickVO());
        List<ContentManageVO> contents = List.of(new ContentManageVO());
        List<PlatformVO> platforms = List.of(new PlatformVO());

        when(adminDAO.selectMonitoringList()).thenReturn(monitoring);
        when(adminDAO.selectVisitorTrend()).thenReturn(visitors);
        when(adminDAO.selectPopularProductClicks()).thenReturn(clicks);
        when(adminDAO.selectAdminContentList("key")).thenReturn(contents);
        when(adminDAO.selectPlatformList()).thenReturn(platforms);

        assertSame(monitoring, adminService.getMonitoringList());
        assertSame(visitors, adminService.getVisitorTrend());
        assertSame(clicks, adminService.getPopularProductClicks());
        assertSame(contents, adminService.getContentList("key"));
        assertSame(platforms, adminService.getPlatformList());
    }

    @Test
    void updateContentShouldReplacePlatformsOnlyWhenListIsProvided() {
        ContentManageVO content = new ContentManageVO();
        content.setContentNo(90L);

        adminService.updateContent(content, null);
        verify(adminDAO).updateContent(content);
        verify(adminDAO, never()).deleteContentPlatforms(90L);

        adminService.updateContent(content, List.of(1L, 2L));
        verify(adminDAO).deleteContentPlatforms(90L);
        verify(adminDAO).insertContentPlatform(90L, 1L);
        verify(adminDAO).insertContentPlatform(90L, 2L);
    }

    @Test
    void platformCreateAndUpdateShouldDelegate() {
        PlatformVO platform = new PlatformVO();
        adminService.registerPlatform(platform);
        adminService.updatePlatform(platform);

        verify(adminDAO).insertPlatform(platform);
        verify(adminDAO).updatePlatform(platform);
    }
}