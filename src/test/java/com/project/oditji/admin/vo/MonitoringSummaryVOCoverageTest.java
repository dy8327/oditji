package com.project.oditji.admin.vo;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/** 관리자 모니터링 요약 VO의 모든 접근자를 검증합니다. */
class MonitoringSummaryVOCoverageTest {

    @Test
    void gettersAndSettersShouldRoundTripEveryField() {
        MonitoringSummaryVO summary = new MonitoringSummaryVO();

        assertEquals(0L, summary.getMemberCount());
        assertEquals(0L, summary.getRecentVisitorCount());
        assertEquals(0L, summary.getDeliveredOrderCount());
        assertEquals(0L, summary.getDeliveredSalesAmount());

        summary.setMemberCount(101L);
        summary.setRecentVisitorCount(77L);
        summary.setDeliveredOrderCount(33L);
        summary.setDeliveredSalesAmount(987654L);

        assertEquals(101L, summary.getMemberCount());
        assertEquals(77L, summary.getRecentVisitorCount());
        assertEquals(33L, summary.getDeliveredOrderCount());
        assertEquals(987654L, summary.getDeliveredSalesAmount());
    }
}
