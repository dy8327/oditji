package com.project.oditji.subscription.vo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

/** 구독 계산 요청 VO의 필터 접근자와 null-safe 위시리스트 setter를 검증합니다. */
class SubscriptionCalculateRequestVOCoverageTest {

    @Test
    void gettersAndSettersShouldPreserveValues() {
        SubscriptionCalculateRequestVO vo =
                new SubscriptionCalculateRequestVO();
        List<ContentWishItemVO> wishItems =
                List.of(new ContentWishItemVO());

        assertTrue(vo.getWishItemList().isEmpty());

        vo.setWishItemList(wishItems);
        vo.setTelecomCode("SKT");
        vo.setCardCompany("현대카드");
        vo.setMembershipName("네이버");

        assertSame(wishItems, vo.getWishItemList());
        assertEquals("SKT", vo.getTelecomCode());
        assertEquals("현대카드", vo.getCardCompany());
        assertEquals("네이버", vo.getMembershipName());
    }

    @Test
    void nullWishListShouldBecomeEmptyList() {
        SubscriptionCalculateRequestVO vo =
                new SubscriptionCalculateRequestVO();

        vo.setWishItemList(null);

        assertTrue(vo.getWishItemList().isEmpty());
    }
}
