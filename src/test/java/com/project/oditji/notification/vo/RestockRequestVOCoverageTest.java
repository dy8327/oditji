package com.project.oditji.notification.vo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.LocalDateTime;
import java.time.Month;

import org.junit.jupiter.api.Test;

/** 재입고 알림 신청 VO의 기본값과 접근자를 검증합니다. */
class RestockRequestVOCoverageTest {

    @Test
    void newInstanceShouldExposeNullDefaults() {
        RestockRequestVO vo = new RestockRequestVO();

        assertNull(vo.getRestockRequestNo());
        assertNull(vo.getMemberNo());
        assertNull(vo.getOptionNo());
        assertNull(vo.getProductNo());
        assertNull(vo.getStatus());
        assertNull(vo.getCreatedAt());
        assertNull(vo.getNotifiedAt());
    }

    @Test
    void gettersAndSettersShouldRoundTripAllFields() {
        LocalDateTime createdAt = LocalDateTime.of(
                2026, Month.AUGUST, 11, 9, 30);
        LocalDateTime notifiedAt = LocalDateTime.of(
                2026, Month.AUGUST, 11, 10, 0);

        RestockRequestVO vo = new RestockRequestVO();
        vo.setRestockRequestNo(1L);
        vo.setMemberNo(2L);
        vo.setOptionNo(3L);
        vo.setProductNo(4L);
        vo.setStatus("WAITING");
        vo.setCreatedAt(createdAt);
        vo.setNotifiedAt(notifiedAt);

        assertEquals(1L, vo.getRestockRequestNo());
        assertEquals(2L, vo.getMemberNo());
        assertEquals(3L, vo.getOptionNo());
        assertEquals(4L, vo.getProductNo());
        assertEquals("WAITING", vo.getStatus());
        assertEquals(createdAt, vo.getCreatedAt());
        assertEquals(notifiedAt, vo.getNotifiedAt());
    }
}
