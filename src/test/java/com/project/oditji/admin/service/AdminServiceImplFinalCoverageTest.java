package com.project.oditji.admin.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;

import com.project.oditji.admin.dao.AdminDAO;
import com.project.oditji.common.vo.SettlementRequestVO;
import com.project.oditji.notification.service.NotificationService;

/**
 * 관리자 서비스의 페이징, 일괄 회원처리, 사업자 등급/정산의 남은 분기를 보완합니다.
 */
class AdminServiceImplFinalCoverageTest {

    @TempDir
    Path tempDirectory;

    private AdminDAO adminDAO;
    private AdminServiceImpl service;

    @BeforeEach
    void setUp() {
        adminDAO = mock(AdminDAO.class);
        NotificationService notificationService =
                mock(NotificationService.class);

        service = new AdminServiceImpl(
                adminDAO,
                notificationService,
                tempDirectory.toString());
    }

    @Test
    void memberListShouldNormalizePageBelowOneAndKeepPositivePage() {
        when(adminDAO.selectMemberList(any()))
                .thenReturn(List.of());

        service.getMemberList(
                "k",
                "id",
                "ACTIVE",
                "USER",
                0,
                10);

        service.getMemberList(
                "k",
                "id",
                "ACTIVE",
                "USER",
                3,
                10);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String, Object>> captor =
                ArgumentCaptor.forClass(Map.class);

        verify(adminDAO,
                times(2))
                .selectMemberList(captor.capture());

        assertEquals(
                0,
                captor.getAllValues()
                        .get(0)
                        .get("offset"));

        assertEquals(
                20,
                captor.getAllValues()
                        .get(1)
                        .get("offset"));
    }

    @Test
    void bulkMemberActionShouldSkipWithdrawnMemberAndProcessRestore() {
        List<Long> memberNos =
                List.of(1L, 2L);

        when(adminDAO.selectWithdrawnMemberNos(memberNos))
                .thenReturn(List.of(1L));

        int skipped =
                service.bulkMemberAction(
                        memberNos,
                        "restore");

        assertEquals(1, skipped);

        verify(adminDAO, never())
                .updateMemberStatus(
                        1L,
                        "ACTIVE");

        verify(adminDAO)
                .updateMemberStatus(
                        2L,
                        "ACTIVE");
    }

    @Test
    void bulkMemberActionShouldRejectUnknownAction() {
        List<Long> memberNos =
                List.of(3L);

        when(adminDAO.selectWithdrawnMemberNos(memberNos))
                .thenReturn(List.of());

        assertThrows(
                IllegalArgumentException.class,
                () -> service.bulkMemberAction(
                        memberNos,
                        "UNKNOWN"));
    }

    @Test
    void getBusinessListShouldUpdateSettlementRatesOnlyWhenGradesChanged() {
        when(adminDAO.updateBusinessGradesBySales())
                .thenReturn(0, 2);

        when(adminDAO.selectBusinessList(any()))
                .thenReturn(List.of());

        service.getBusinessList(
                null,
                null,
                1,
                10);

        service.getBusinessList(
                null,
                null,
                1,
                10);

        verify(adminDAO)
                .updateCurrentMonthSettlementRates();
    }

    @Test
    void manualGradeUpdateShouldRejectUnknownGradeAndHandleDaoFailure() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.updateBusinessGrade(
                        1L,
                        "DIAMOND"));

        when(adminDAO.updateBusinessGrade(
                1L,
                "GOLD"))
                .thenReturn(0);

        assertThrows(
                IllegalStateException.class,
                () -> service.updateBusinessGrade(
                        1L,
                        " gold "));
    }

    @Test
    void confirmSettlementShouldRejectNonRequestedAndMissingSettlementRows() {
        when(adminDAO.selectSettlementRequest(10L))
                .thenReturn(null);

        assertThrows(
                IllegalStateException.class,
                () -> service.confirmSettlement(10L));

        SettlementRequestVO done =
                new SettlementRequestVO();
        done.setStatus("DONE");

        when(adminDAO.selectSettlementRequest(11L))
                .thenReturn(done);

        assertThrows(
                IllegalStateException.class,
                () -> service.confirmSettlement(11L));
    }

    @Test
    void rejectSettlementShouldCoverNullReasonAndReleasedCountFailure() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.rejectSettlement(
                        20L,
                        null));

        SettlementRequestVO request =
                new SettlementRequestVO();
        request.setStatus("REQUESTED");

        when(adminDAO.selectSettlementRequest(21L))
                .thenReturn(request);

        when(adminDAO.updateSettlementRequestStatus(
                21L,
                "REJECTED",
                "사유"))
                .thenReturn(1);

        when(adminDAO.releaseRejectedSettlementItems(21L))
                .thenReturn(0);

        assertThrows(
                IllegalStateException.class,
                () -> service.rejectSettlement(
                        21L,
                        " 사유 "));
    }
}