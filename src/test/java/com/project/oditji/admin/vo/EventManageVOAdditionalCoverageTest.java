package com.project.oditji.admin.vo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import com.project.oditji.common.util.DateTimeUtil;

/**
 * 기존 EventManageVOBranchCoverageTest에서 빠진 progress 조건의 중간 단축평가 분기를 보완합니다.
 */
class EventManageVOAdditionalCoverageTest {

    @Test
    void approvedEventWithStartDateButMissingEndDateShouldReturnNoProgress() {
        EventManageVO event =
                new EventManageVO();

        event.setStatus("APPROVED");
        event.setStartDate(
                LocalDate.now(
                        DateTimeUtil.KOREA_ZONE));
        event.setEndDate(null);

        assertNull(
                event.getProgressStatus());
        assertEquals(
                "-",
                event.getProgressStatusLabel());
    }

    @Test
    void unknownStatusShouldRemainWaitingAndUnavailableForProgress() {
        EventManageVO event =
                new EventManageVO();

        event.setStatus("UNKNOWN");
        event.setStartDate(
                LocalDate.now(
                        DateTimeUtil.KOREA_ZONE)
                        .minusDays(1));
        event.setEndDate(
                LocalDate.now(
                        DateTimeUtil.KOREA_ZONE)
                        .plusDays(1));

        assertEquals(
                "WAITING",
                event.getApprovalStatus());
        assertNull(
                event.getProgressStatus());
    }
}
