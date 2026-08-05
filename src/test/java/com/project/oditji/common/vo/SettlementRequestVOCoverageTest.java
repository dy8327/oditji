package com.project.oditji.common.vo;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDateTime;
import java.time.Month;

import org.junit.jupiter.api.Test;

/**
 * 정산 요청 조회 결과의 모든 필드가 손실 없이 전달되는지 검증합니다.
 */
class SettlementRequestVOCoverageTest {

    @Test
    void allSettlementRequestFieldsShouldRoundTrip() {
        SettlementRequestVO request = new SettlementRequestVO();
        LocalDateTime requestedAt = LocalDateTime.of(2026, Month.AUGUST, 5, 10, 0);
        LocalDateTime processedAt = LocalDateTime.of(2026, Month.AUGUST, 5, 11, 0);

        request.setRequestNo(1L);
        request.setBusinessNo(2L);
        request.setBusinessName("테스트사업자");
        request.setSettlementMonth("2026-08");
        request.setTotalAmount(100000L);
        request.setFeeAmount(10000L);
        request.setSettledAmount(90000L);
        request.setOrderCount(3);
        request.setBankName("테스트은행");
        request.setAccountNumber("1234567890");
        request.setAccountHolder("테스트사업자");
        request.setStatus("WAITING");
        request.setRequestedAt(requestedAt);
        request.setProcessedAt(processedAt);
        request.setRejectReason("정보 확인 필요");

        assertEquals(1L, request.getRequestNo());
        assertEquals(2L, request.getBusinessNo());
        assertEquals("테스트사업자", request.getBusinessName());
        assertEquals("2026-08", request.getSettlementMonth());
        assertEquals(100000L, request.getTotalAmount());
        assertEquals(10000L, request.getFeeAmount());
        assertEquals(90000L, request.getSettledAmount());
        assertEquals(3, request.getOrderCount());
        assertEquals("테스트은행", request.getBankName());
        assertEquals("1234567890", request.getAccountNumber());
        assertEquals("테스트사업자", request.getAccountHolder());
        assertEquals("WAITING", request.getStatus());
        assertEquals(requestedAt, request.getRequestedAt());
        assertEquals(processedAt, request.getProcessedAt());
        assertEquals("정보 확인 필요", request.getRejectReason());
    }
}
