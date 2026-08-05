package com.project.oditji.admin.vo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import com.project.oditji.common.util.DateTimeUtil;

/**
 * 관리자 이벤트 VO의 할인 금액, 승인 상태와 진행 상태 분기를 검증합니다.
 */
class EventManageVOBranchCoverageTest {

    @Test
    void accessorsAndDiscountedPriceShouldHandleValuesAndNulls() {
        EventManageVO event = new EventManageVO();
        LocalDate start = relativeDate(-1);
        LocalDate end = relativeDate(1);

        event.setBusinessName("ODITJI STORE");
        event.setStartDate(start);
        event.setEndDate(end);
        event.setProductNo(10L);
        event.setProductName("테스트 상품");
        event.setPrice(15000L);
        event.setEventDiscountRate(20);
        event.setProductDetail("상세 설명");

        assertEquals("ODITJI STORE", event.getBusinessName());
        assertEquals(start, event.getStartDate());
        assertEquals(end, event.getEndDate());
        assertEquals(10L, event.getProductNo());
        assertEquals("테스트 상품", event.getProductName());
        assertEquals(15000L, event.getPrice());
        assertEquals(20, event.getEventDiscountRate());
        assertEquals("상세 설명", event.getProductDetail());
        assertEquals(12000L, event.getDiscountedPrice());

        event.setPrice(null);
        assertNull(event.getDiscountedPrice());
        event.setPrice(15000L);
        event.setEventDiscountRate(null);
        assertNull(event.getDiscountedPrice());
    }

    @Test
    void approvalStatusShouldNormalizeWaitingApprovedEndAndRejected() {
        EventManageVO event = new EventManageVO();

        event.setStatus(null);
        assertEquals("WAITING", event.getApprovalStatus());
        assertEquals("대기", event.getApprovalStatusLabel());

        event.setStatus("APPROVED");
        assertEquals("APPROVED", event.getApprovalStatus());
        assertEquals("승인", event.getApprovalStatusLabel());

        event.setStatus("END");
        assertEquals("APPROVED", event.getApprovalStatus());
        assertEquals("승인", event.getApprovalStatusLabel());

        event.setStatus("REJECTED");
        assertEquals("REJECTED", event.getApprovalStatus());
        assertEquals("반려", event.getApprovalStatusLabel());
    }

    @Test
    void progressStatusShouldHandleUnavailableUpcomingOngoingAndEnded() {
        EventManageVO event = new EventManageVO();

        event.setStatus("WAITING");
        assertNull(event.getProgressStatus());
        assertEquals("-", event.getProgressStatusLabel());

        event.setStatus("APPROVED");
        assertNull(event.getProgressStatus());

        event.setStartDate(relativeDate(1));
        event.setEndDate(relativeDate(2));
        assertEquals("UPCOMING", event.getProgressStatus());
        assertEquals("예정", event.getProgressStatusLabel());

        event.setStartDate(relativeDate(-1));
        event.setEndDate(relativeDate(1));
        assertEquals("ONGOING", event.getProgressStatus());
        assertEquals("진행중", event.getProgressStatusLabel());

        event.setStartDate(relativeDate(-2));
        event.setEndDate(relativeDate(-1));
        assertEquals("ENDED", event.getProgressStatus());
        assertEquals("종료", event.getProgressStatusLabel());
    }

    private LocalDate relativeDate(int offsetDays) {
        return LocalDate.now(DateTimeUtil.KOREA_ZONE).plusDays(offsetDays);
    }
}
