package com.project.oditji.business.vo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.time.LocalDate;
import java.time.Month;
import java.util.List;

import org.junit.jupiter.api.Test;

/** 이벤트 등록·수정 폼 VO의 모든 getter/setter를 검증합니다. */
class EventFormVOCoverageTest {

    @Test
    void allPropertiesShouldRoundTrip() {
        EventFormVO form = new EventFormVO();
        LocalDate startDate = LocalDate.of(2026, Month.AUGUST, 10);
        LocalDate endDate = LocalDate.of(2026, Month.AUGUST, 31);
        List<Long> productNos = List.of(10L, 20L);
        List<Integer> discountRates = List.of(10, 25);

        form.setEventTitle("여름 이벤트");
        form.setDescription("이벤트 설명");
        form.setStartDate(startDate);
        form.setEndDate(endDate);
        form.setProductNoList(productNos);
        form.setDiscountRateList(discountRates);

        assertEquals("여름 이벤트", form.getEventTitle());
        assertEquals("이벤트 설명", form.getDescription());
        assertEquals(startDate, form.getStartDate());
        assertEquals(endDate, form.getEndDate());
        assertSame(productNos, form.getProductNoList());
        assertSame(discountRates, form.getDiscountRateList());
    }
}
