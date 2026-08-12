package com.project.oditji.goods.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.project.oditji.goods.dao.GoodsDAO;

/** 가격 범위 정규화의 min 존재/max null 단축평가 분기를 보완합니다. */
class GoodsServiceImplPriceBoundsConditionClosureTest {

    private GoodsServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new GoodsServiceImpl(mock(GoodsDAO.class));
    }

    @Test
    void priceBoundsShouldEvaluateSecondPresenceConditionWhenOnlyMinExists() {
        Object bounds = ReflectionTestUtils.invokeMethod(
                service,
                "normalizePriceBounds",
                100,
                null);

        assertEquals(
                100,
                ((Integer) ReflectionTestUtils.invokeMethod(bounds, "minPrice")).intValue());
        assertNull(ReflectionTestUtils.invokeMethod(bounds, "maxPrice"));
    }
}
