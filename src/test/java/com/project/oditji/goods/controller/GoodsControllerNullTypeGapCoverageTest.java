package com.project.oditji.goods.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.project.oditji.goods.service.GoodsService;
import com.project.oditji.report.service.ReportService;
import com.project.oditji.review.service.ReviewService;
import com.project.oditji.wish.service.WishService;

/** 상품 목록 유형 정규화의 null 삼항 분기를 보완합니다. */
class GoodsControllerNullTypeGapCoverageTest {

    @Test
    void normalizeListTypeShouldUseAllWhenTypeIsNull() {
        GoodsController controller = new GoodsController(
                mock(GoodsService.class),
                mock(ReviewService.class),
                mock(ReportService.class),
                mock(WishService.class));

        String normalized = ReflectionTestUtils.invokeMethod(
                controller,
                "normalizeListType",
                (Object) null);

        assertEquals("all", normalized);
    }
}
