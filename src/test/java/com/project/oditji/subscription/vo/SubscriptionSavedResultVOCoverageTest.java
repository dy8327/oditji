package com.project.oditji.subscription.vo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

/** 마이페이지 저장 구독 결과 VO의 기본값과 모든 getter/setter를 검증합니다. */
class SubscriptionSavedResultVOCoverageTest {

    @Test
    void defaultsAndScalarAccessorsShouldPreserveValues() {
        SubscriptionSavedResultVO vo = new SubscriptionSavedResultVO();

        assertNotNull(vo.getPlatformNameList());
        assertTrue(vo.getPlatformNameList().isEmpty());
        assertTrue(vo.getContentList().isEmpty());
        assertTrue(vo.getPlatformGroupList().isEmpty());
        assertEquals(0, vo.getTotalPrice());
        assertEquals(0, vo.getDiscountPrice());
        assertEquals(0, vo.getFinalPrice());

        LocalDateTime createdAt = LocalDateTime.of(2026, Month.AUGUST, 13, 10, 20);

        vo.setResultId("SUBS_VO_1");
        vo.setCreatedAt(createdAt);
        vo.setTotalPrice(30000);
        vo.setDiscountPrice(7000);
        vo.setFinalPrice(23000);

        assertEquals("SUBS_VO_1", vo.getResultId());
        assertEquals(createdAt, vo.getCreatedAt());
        assertEquals(30000, vo.getTotalPrice());
        assertEquals(7000, vo.getDiscountPrice());
        assertEquals(23000, vo.getFinalPrice());
    }

    @Test
    void listSettersShouldKeepProvidedListsAndNormalizeNull() {
        SubscriptionSavedResultVO vo = new SubscriptionSavedResultVO();
        List<String> platformNames =
                new ArrayList<String>(List.of("Netflix", "TVING"));
        List<ContentWishItemVO> contentList =
                new ArrayList<ContentWishItemVO>(List.of(new ContentWishItemVO()));
        List<PlatformPriceVO> platformGroupList =
                new ArrayList<PlatformPriceVO>(List.of(new PlatformPriceVO()));

        vo.setPlatformNameList(platformNames);
        vo.setContentList(contentList);
        vo.setPlatformGroupList(platformGroupList);

        assertSame(platformNames, vo.getPlatformNameList());
        assertSame(contentList, vo.getContentList());
        assertSame(platformGroupList, vo.getPlatformGroupList());

        vo.setPlatformNameList(null);
        vo.setContentList(null);
        vo.setPlatformGroupList(null);

        assertNotNull(vo.getPlatformNameList());
        assertNotNull(vo.getContentList());
        assertNotNull(vo.getPlatformGroupList());
        assertTrue(vo.getPlatformNameList().isEmpty());
        assertTrue(vo.getContentList().isEmpty());
        assertTrue(vo.getPlatformGroupList().isEmpty());
    }
}
