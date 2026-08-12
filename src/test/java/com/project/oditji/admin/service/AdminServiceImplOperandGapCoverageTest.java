package com.project.oditji.admin.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.project.oditji.admin.dao.AdminDAO;
import com.project.oditji.notification.service.NotificationService;

/** 관리자 서비스의 빈 문자열/비양수 번호 short-circuit 잔여 조건을 보완합니다. */
class AdminServiceImplOperandGapCoverageTest {

    private AdminServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new AdminServiceImpl(
                mock(AdminDAO.class),
                mock(NotificationService.class),
                "build/test-admin-product");
    }

    @Test
    void gradeValidationShouldRejectBlankGradeAfterNonNullCheck() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.updateBusinessGrade(1L, "   "));
    }

    @Test
    void settlementNumberValidationShouldReachNonPositiveSecondOperand() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.confirmSettlement(0L));

        assertThrows(
                IllegalArgumentException.class,
                () -> service.rejectSettlement(0L, "사유"));
    }

    @Test
    void rejectSettlementShouldNormalizeNonNullBlankReasonToEmpty() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.rejectSettlement(1L, "   "));
    }

    @Test
    void bulkMemberActionShouldReturnZeroForNonNullEmptySelection() {
        assertEquals(
                0,
                service.bulkMemberAction(
                        Collections.emptyList(),
                        "suspend"));
    }
}
