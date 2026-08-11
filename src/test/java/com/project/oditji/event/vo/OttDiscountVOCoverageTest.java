package com.project.oditji.event.vo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

/** OTT 할인 VO의 접근자와 할인율 계산의 단락 평가 분기를 검증합니다. */
class OttDiscountVOCoverageTest {

    @Test
    void gettersAndSettersShouldRoundTripAllFields() {
        OttDiscountVO vo = new OttDiscountVO();
        LocalDateTime createdAt = LocalDateTime.of(2026, 8, 11, 9, 30);

        vo.setDiscountId(1L);
        vo.setPlatformCode("NETFLIX");
        vo.setPlatformName("넷플릭스");
        vo.setCategory("CARD");
        vo.setTitle("카드 할인");
        vo.setDiscountSummary("월 5천원 할인");
        vo.setDescription("상세 설명");
        vo.setCardOrCompany("테스트카드");
        vo.setTargetUrl("https://example.com");
        vo.setBadgeText("BEST");
        vo.setStartDate("2026-08-01");
        vo.setEndDate("2026-08-31");
        vo.setIsActive("Y");
        vo.setCreatedAt(createdAt);
        vo.setRegularPrice(20000);
        vo.setDiscountPrice(15000);

        assertEquals(1L, vo.getDiscountId());
        assertEquals("NETFLIX", vo.getPlatformCode());
        assertEquals("넷플릭스", vo.getPlatformName());
        assertEquals("CARD", vo.getCategory());
        assertEquals("카드 할인", vo.getTitle());
        assertEquals("월 5천원 할인", vo.getDiscountSummary());
        assertEquals("상세 설명", vo.getDescription());
        assertEquals("테스트카드", vo.getCardOrCompany());
        assertEquals("https://example.com", vo.getTargetUrl());
        assertEquals("BEST", vo.getBadgeText());
        assertEquals("2026-08-01", vo.getStartDate());
        assertEquals("2026-08-31", vo.getEndDate());
        assertEquals("Y", vo.getIsActive());
        assertEquals(createdAt, vo.getCreatedAt());
        assertEquals(20000, vo.getRegularPrice());
        assertEquals(15000, vo.getDiscountPrice());
        assertEquals(25, vo.getDiscountRate());
    }

    @Test
    void discountRateShouldReturnNullWhenRegularPriceIsNull() {
        OttDiscountVO vo = new OttDiscountVO();
        vo.setDiscountPrice(1000);

        assertNull(vo.getDiscountRate());
    }

    @Test
    void discountRateShouldReturnNullWhenDiscountPriceIsNull() {
        OttDiscountVO vo = new OttDiscountVO();
        vo.setRegularPrice(1000);

        assertNull(vo.getDiscountRate());
    }

    @Test
    void discountRateShouldReturnNullWhenRegularPriceIsNotPositive() {
        OttDiscountVO vo = new OttDiscountVO();
        vo.setRegularPrice(0);
        vo.setDiscountPrice(0);

        assertNull(vo.getDiscountRate());
    }

    @Test
    void discountRateShouldReturnNullWhenDiscountIsNotLowerThanRegularPrice() {
        OttDiscountVO vo = new OttDiscountVO();
        vo.setRegularPrice(1000);
        vo.setDiscountPrice(1000);

        assertNull(vo.getDiscountRate());

        vo.setDiscountPrice(1200);
        assertNull(vo.getDiscountRate());
    }

    @Test
    void discountRateShouldRoundCalculatedPercentage() {
        OttDiscountVO vo = new OttDiscountVO();
        vo.setRegularPrice(3000);
        vo.setDiscountPrice(1999);

        assertEquals(33, vo.getDiscountRate());
    }
}
