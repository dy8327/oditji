package com.project.oditji.admin.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.project.oditji.admin.dao.AdminDAO;
import com.project.oditji.common.vo.SettlementRequestVO;
import com.project.oditji.notification.service.NotificationService;

/** 관리자 회원 목록과 정산 처리의 잔여 경계 분기를 보완합니다. */
class AdminServiceImplResidualClosure4Test {

    @TempDir
    Path tempDirectory;

    private AdminDAO adminDAO;
    private NotificationService notificationService;
    private AdminServiceImpl service;

    @BeforeEach
    void setUp() {
        adminDAO = mock(AdminDAO.class);
        notificationService = mock(NotificationService.class);
        service = new AdminServiceImpl(
                adminDAO,
                notificationService,
                tempDirectory.toString());
    }

    @Test
    void memberListShouldCoverInvalidAndProvidedPageBranches() {
        when(adminDAO.selectMemberList(anyMap())).thenReturn(List.of());

        assertEquals(0, service.getMemberList(null, null, null, null, 0, 10).size());
        assertEquals(0, service.getMemberList(null, null, null, null, 3, 10).size());
    }

    @Test
    void confirmSettlementShouldCoverNullZeroAndSuccessfulRequestedFlow() {
        Long nullRequestNo = null;
        assertThrows(
                IllegalArgumentException.class,
                () -> service.confirmSettlement(nullRequestNo));
        assertThrows(
                IllegalArgumentException.class,
                () -> service.confirmSettlement(0L));

        SettlementRequestVO request = settlementRequest(10L, "REQUESTED");
        when(adminDAO.selectSettlementRequest(10L)).thenReturn(request);
        when(adminDAO.updateSettlementRequestStatus(10L, "DONE", null)).thenReturn(1);
        when(adminDAO.completeSettlementItems(10L)).thenReturn(1);

        assertDoesNotThrow(() -> service.confirmSettlement(10L));

        verify(notificationService).createForBusiness(
                20L,
                "SETTLEMENT_APPROVED",
                "정산 지급 완료",
                "2026-08 정산금 지급이 완료되었습니다.",
                "/business/settlement/complete",
                "SETTLEMENT_REQUEST",
                10L);
    }

    @Test
    void rejectSettlementShouldCoverRequestNumberAndReasonOperandsThenSuccess() {
        Long nullRequestNo = null;
        assertThrows(
                IllegalArgumentException.class,
                () -> service.rejectSettlement(nullRequestNo, "사유"));
        assertThrows(
                IllegalArgumentException.class,
                () -> service.rejectSettlement(0L, "사유"));
        assertThrows(
                IllegalArgumentException.class,
                () -> service.rejectSettlement(1L, null));
        assertThrows(
                IllegalArgumentException.class,
                () -> service.rejectSettlement(1L, "   "));

        SettlementRequestVO request = settlementRequest(30L, "REQUESTED");
        when(adminDAO.selectSettlementRequest(30L)).thenReturn(request);
        when(adminDAO.updateSettlementRequestStatus(30L, "REJECTED", "계좌 확인 필요"))
                .thenReturn(1);
        when(adminDAO.releaseRejectedSettlementItems(30L)).thenReturn(1);

        assertDoesNotThrow(
                () -> service.rejectSettlement(30L, "  계좌 확인 필요  "));

        verify(notificationService).createForBusiness(
                20L,
                "SETTLEMENT_REJECTED",
                "정산 요청 반려",
                "2026-08 정산 요청이 반려되었습니다. 사유: 계좌 확인 필요",
                "/business/settlement/complete",
                "SETTLEMENT_REQUEST",
                30L);
    }

    private SettlementRequestVO settlementRequest(Long requestNo, String status) {
        SettlementRequestVO request = new SettlementRequestVO();
        request.setRequestNo(requestNo);
        request.setBusinessNo(20L);
        request.setSettlementMonth("2026-08");
        request.setStatus(status);
        return request;
    }
}
