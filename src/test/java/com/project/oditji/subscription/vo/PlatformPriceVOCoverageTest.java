package com.project.oditji.subscription.vo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

/** 플랫폼 구독료 VO의 접근자와 할인율 조건을 검증합니다. */
class PlatformPriceVOCoverageTest {

    @Test
    void gettersAndSettersShouldPreserveAssignedValues() {
        PlatformPriceVO vo = new PlatformPriceVO();

        vo.setPlatformCode("NETFLIX");
        vo.setPlatformName("넷플릭스");
        vo.setRegularPrice(17000);
        vo.setBestPrice(12000);
        vo.setDiscountSource("현대카드");
        vo.setDiscountTitle("OTT 할인");

        assertEquals("NETFLIX", vo.getPlatformCode());
        assertEquals("넷플릭스", vo.getPlatformName());
        assertEquals(17000, vo.getRegularPrice());
        assertEquals(12000, vo.getBestPrice());
        assertEquals("현대카드", vo.getDiscountSource());
        assertEquals("OTT 할인", vo.getDiscountTitle());
    }

    @Test
    void discountRateShouldCoverEveryGuardAndRoundedDiscount() {
        PlatformPriceVO vo = new PlatformPriceVO();

        assertNull(vo.getDiscountRate());

        vo.setRegularPrice(10000);
        assertNull(vo.getDiscountRate());

        vo.setBestPrice(9000);
        vo.setRegularPrice(0);
        assertNull(vo.getDiscountRate());

        vo.setRegularPrice(10000);
        vo.setBestPrice(10000);
        assertNull(vo.getDiscountRate());

        vo.setBestPrice(11000);
        assertNull(vo.getDiscountRate());

        vo.setBestPrice(8500);
        assertEquals(15, vo.getDiscountRate());

        vo.setRegularPrice(3000);
        vo.setBestPrice(2000);
        assertEquals(33, vo.getDiscountRate());
    }
}
