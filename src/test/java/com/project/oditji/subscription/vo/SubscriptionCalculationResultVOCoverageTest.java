package com.project.oditji.subscription.vo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

/** 구독 조합 계산 결과 VO의 기본값과 null-safe setter를 검증합니다. */
class SubscriptionCalculationResultVOCoverageTest {

    @Test
    void defaultsAndScalarSettersShouldWork() {
        SubscriptionCalculationResultVO vo =
                new SubscriptionCalculationResultVO();

        assertTrue(vo.getSelectedPlatformList().isEmpty());
        assertTrue(vo.getUnresolvedItemList().isEmpty());
        assertEquals(0, vo.getTotalMonthlyPrice());
        assertEquals(0, vo.getTotalRegularMonthlyPrice());
        assertEquals(0, vo.getAllPlatformMonthlyPrice());

        vo.setTotalMonthlyPrice(10000);
        vo.setTotalRegularMonthlyPrice(15000);
        vo.setAllPlatformMonthlyPrice(30000);

        assertEquals(10000, vo.getTotalMonthlyPrice());
        assertEquals(15000, vo.getTotalRegularMonthlyPrice());
        assertEquals(30000, vo.getAllPlatformMonthlyPrice());
    }

    @Test
    void listSettersShouldKeepSuppliedListAndReplaceNullWithEmptyList() {
        SubscriptionCalculationResultVO vo =
                new SubscriptionCalculationResultVO();
        List<PlatformPriceVO> selected = List.of(new PlatformPriceVO());
        List<ContentWishItemVO> unresolved = List.of(new ContentWishItemVO());

        vo.setSelectedPlatformList(selected);
        vo.setUnresolvedItemList(unresolved);

        assertSame(selected, vo.getSelectedPlatformList());
        assertSame(unresolved, vo.getUnresolvedItemList());

        vo.setSelectedPlatformList(null);
        vo.setUnresolvedItemList(null);

        assertTrue(vo.getSelectedPlatformList().isEmpty());
        assertTrue(vo.getUnresolvedItemList().isEmpty());
    }
}
