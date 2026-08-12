package com.project.oditji.business.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.project.oditji.business.dao.BusinessDAO;
import com.project.oditji.common.vo.SettlementRequestVO;
import com.project.oditji.content.service.ContentService;
import com.project.oditji.notification.service.NotificationService;
import com.project.oditji.search.service.SearchContentStore;
import com.project.oditji.tmdb.service.TmdbService;

/** 사전 정산 요청 생성과 월 도래 자동 확정의 입력/실패/성공 분기를 검증합니다. */
class BusinessServiceImplEarlySettlementCoverageTest {

    @TempDir
    Path tempDirectory;

    private BusinessDAO businessDAO;
    private NotificationService notificationService;
    private BusinessServiceImpl service;

    @BeforeEach
    void setUp() {
        businessDAO = mock(BusinessDAO.class);
        notificationService = mock(NotificationService.class);

        service = new BusinessServiceImpl(
                businessDAO,
                mock(ContentService.class),
                mock(SearchContentStore.class),
                mock(TmdbService.class),
                notificationService,
                tempDirectory.resolve("product").toString(),
                tempDirectory.resolve("event").toString());
    }

    @Test
    void requestEarlySettlementShouldRejectInvalidBusinessAndMissingTarget() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.requestEarlySettlement(0L));

        when(businessDAO.selectEarlySettlementRequestTarget(1L)).thenReturn(null);

        assertThrows(
                IllegalStateException.class,
                () -> service.requestEarlySettlement(1L));
    }

    @Test
    void requestEarlySettlementShouldRejectEveryInvalidTargetAmountCondition() {
        assertEarlyTargetRejected(request(null, 1000L));
        assertEarlyTargetRejected(request(0, 1000L));
        assertEarlyTargetRejected(request(1, null));
        assertEarlyTargetRejected(request(1, 0L));
    }

    @Test
    void requestEarlySettlementShouldRejectEveryMissingAccountCondition() {
        SettlementRequestVO missingBank = request(1, 1000L);
        missingBank.setBankName(null);
        assertEarlyAccountRejected(missingBank);

        SettlementRequestVO blankBank = request(1, 1000L);
        blankBank.setBankName("   ");
        assertEarlyAccountRejected(blankBank);

        SettlementRequestVO missingAccount = request(1, 1000L);
        missingAccount.setAccountNumber(null);
        assertEarlyAccountRejected(missingAccount);

        SettlementRequestVO blankAccount = request(1, 1000L);
        blankAccount.setAccountNumber("   ");
        assertEarlyAccountRejected(blankAccount);

        SettlementRequestVO missingHolder = request(1, 1000L);
        missingHolder.setAccountHolder(null);
        assertEarlyAccountRejected(missingHolder);

        SettlementRequestVO blankHolder = request(1, 1000L);
        blankHolder.setAccountHolder("   ");
        assertEarlyAccountRejected(blankHolder);
    }

    @Test
    void requestEarlySettlementShouldRejectDuplicateMonthAndInsertFailure() {
        SettlementRequestVO duplicate = request(2, 2000L);
        duplicate.setSettlementMonth("2026-09");
        when(businessDAO.selectEarlySettlementRequestTarget(20L)).thenReturn(duplicate);
        when(businessDAO.countActiveSettlementRequestByMonth(20L, "2026-09"))
                .thenReturn(1);

        assertThrows(
                IllegalStateException.class,
                () -> service.requestEarlySettlement(20L));
        verify(businessDAO, never()).insertEarlySettlementRequest(duplicate);

        SettlementRequestVO insertFailure = request(3, 3000L);
        insertFailure.setSettlementMonth("2026-10");
        when(businessDAO.selectEarlySettlementRequestTarget(21L)).thenReturn(insertFailure);
        when(businessDAO.countActiveSettlementRequestByMonth(21L, "2026-10"))
                .thenReturn(0);
        when(businessDAO.insertEarlySettlementRequest(insertFailure)).thenReturn(0);

        assertThrows(
                IllegalStateException.class,
                () -> service.requestEarlySettlement(21L));
    }

    @Test
    void requestEarlySettlementShouldInsertValidRequest() {
        SettlementRequestVO request = request(4, 4000L);
        request.setSettlementMonth("2026-11");
        when(businessDAO.selectEarlySettlementRequestTarget(30L)).thenReturn(request);
        when(businessDAO.countActiveSettlementRequestByMonth(30L, "2026-11"))
                .thenReturn(0);
        when(businessDAO.insertEarlySettlementRequest(request)).thenReturn(1);

        service.requestEarlySettlement(30L);

        verify(businessDAO).insertEarlySettlementRequest(request);
    }

    @Test
    void finalizeEarlySettlementRequestsShouldReturnZeroForNullAndEmptyLists() {
        when(businessDAO.selectEarlySettlementRequestsToFinalize())
                .thenReturn(null)
                .thenReturn(Collections.emptyList());

        assertEquals(0, service.finalizeEarlySettlementRequests());
        assertEquals(0, service.finalizeEarlySettlementRequests());
    }

    @Test
    void finalizeEarlySettlementRequestsShouldSkipInvalidRowsAndRejectUnlinkableRequest() {
        SettlementRequestVO missingRequestNo = finalizedRequest(null, 10L);
        SettlementRequestVO missingBusinessNo = finalizedRequest(102L, null);
        SettlementRequestVO unlinkable = finalizedRequest(103L, 13L);

        when(businessDAO.selectEarlySettlementRequestsToFinalize())
                .thenReturn(Arrays.asList(
                        null,
                        missingRequestNo,
                        missingBusinessNo,
                        unlinkable));
        when(businessDAO.updateSettlementRequestNo(13L, 103L)).thenReturn(0);

        assertEquals(0, service.finalizeEarlySettlementRequests());

        verify(businessDAO).rejectEarlySettlementRequest(
                103L,
                "정산 확정 시점에 정산 가능한 배송 완료 내역이 없습니다.");
        verify(businessDAO, never()).refreshFinalizedEarlySettlementRequest(103L);
    }

    @Test
    void finalizeEarlySettlementRequestsShouldThrowWhenFinalAmountRefreshFails() {
        SettlementRequestVO request = finalizedRequest(201L, 21L);
        when(businessDAO.selectEarlySettlementRequestsToFinalize())
                .thenReturn(List.of(request));
        when(businessDAO.updateSettlementRequestNo(21L, 201L)).thenReturn(2);
        when(businessDAO.refreshFinalizedEarlySettlementRequest(201L)).thenReturn(0);

        assertThrows(
                IllegalStateException.class,
                () -> service.finalizeEarlySettlementRequests());

        verify(notificationService, never()).createForAdmins(
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyLong());
    }

    @Test
    void finalizeEarlySettlementRequestsShouldRefreshNotifyAndCountValidRows() {
        SettlementRequestVO first = finalizedRequest(301L, 31L);
        SettlementRequestVO second = finalizedRequest(302L, 32L);

        when(businessDAO.selectEarlySettlementRequestsToFinalize())
                .thenReturn(List.of(first, second));
        when(businessDAO.updateSettlementRequestNo(31L, 301L)).thenReturn(1);
        when(businessDAO.updateSettlementRequestNo(32L, 302L)).thenReturn(3);
        when(businessDAO.refreshFinalizedEarlySettlementRequest(301L)).thenReturn(1);
        when(businessDAO.refreshFinalizedEarlySettlementRequest(302L)).thenReturn(1);

        assertEquals(2, service.finalizeEarlySettlementRequests());

        verify(notificationService).createForAdmins(
                "SETTLEMENT_REQUEST",
                "사업자 사전 정산 요청 확정",
                "사업자가 미리 신청한 정산 요청이 월 마감 후 자동 확정되었습니다.",
                "/admin/settlement/main",
                "SETTLEMENT_REQUEST",
                301L);
        verify(notificationService).createForAdmins(
                "SETTLEMENT_REQUEST",
                "사업자 사전 정산 요청 확정",
                "사업자가 미리 신청한 정산 요청이 월 마감 후 자동 확정되었습니다.",
                "/admin/settlement/main",
                "SETTLEMENT_REQUEST",
                302L);
    }

    private void assertEarlyTargetRejected(SettlementRequestVO request) {
        long businessNo = 10L;
        when(businessDAO.selectEarlySettlementRequestTarget(businessNo)).thenReturn(request);

        assertThrows(
                IllegalStateException.class,
                () -> service.requestEarlySettlement(businessNo));
    }

    private void assertEarlyAccountRejected(SettlementRequestVO request) {
        long businessNo = 11L;
        when(businessDAO.selectEarlySettlementRequestTarget(businessNo)).thenReturn(request);

        assertThrows(
                IllegalStateException.class,
                () -> service.requestEarlySettlement(businessNo));
    }

    private static SettlementRequestVO request(Integer orderCount, Long settledAmount) {
        SettlementRequestVO request = new SettlementRequestVO();
        request.setOrderCount(orderCount);
        request.setSettledAmount(settledAmount);
        request.setBankName("테스트은행");
        request.setAccountNumber("1234567890");
        request.setAccountHolder("예금주");
        request.setSettlementMonth("2026-09");
        return request;
    }

    private static SettlementRequestVO finalizedRequest(Long requestNo, Long businessNo) {
        SettlementRequestVO request = new SettlementRequestVO();
        request.setRequestNo(requestNo);
        request.setBusinessNo(businessNo);
        return request;
    }
}
