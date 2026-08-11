package com.project.oditji.subscription.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.project.oditji.event.dao.OttDiscountDAO;
import com.project.oditji.subscription.vo.ContentWishItemVO;
import com.project.oditji.subscription.vo.PlatformPriceVO;
import com.project.oditji.subscription.vo.SubscriptionCalculationResultVO;

/** 위시리스트를 가장 저렴하게 커버하는 OTT 조합 계산 로직을 검증합니다. */
@ExtendWith(MockitoExtension.class)
class SubscriptionCalculatorServiceImplTest {

    @Mock
    private OttDiscountDAO ottDiscountDAO;

    private SubscriptionCalculatorServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new SubscriptionCalculatorServiceImpl(ottDiscountDAO);
    }

    @Test
    void emptyWishlistShouldReturnEmptyResultWithoutQueryingPrices() {
        SubscriptionCalculationResultVO result = service.calculate(List.of(), null, null, null);

        assertTrue(result.getSelectedPlatformList().isEmpty());
        assertEquals(0, result.getTotalMonthlyPrice());
    }

    @Test
    void singlePlatformShouldBeSelectedWhenOnlyOneCoversAllContent() {
        stubPrices(
                price("NETFLIX", 17000),
                price("TVING", 13900));

        ContentWishItemVO itemA = wishItem("Netflix");
        ContentWishItemVO itemB = wishItem("Netflix", "TVING");

        SubscriptionCalculationResultVO result =
                service.calculate(List.of(itemA, itemB), null, null, null);

        assertEquals(Set.of("NETFLIX"), codesOf(result));
        assertEquals(17000, result.getTotalMonthlyPrice());
        assertTrue(result.getUnresolvedItemList().isEmpty());
    }

    @Test
    void shouldPickCheaperTwoPlatformComboOverExpensiveSinglePlatform() {
        /*
         * NETFLIX 혼자 두 작품 다 커버 가능하지만 20000원.
         * TVING(6000) + WATCHA(7000) 조합이 두 작품을 나눠 커버하며 13000원으로 더 싸다.
         */
        stubPrices(
                price("NETFLIX", 20000),
                price("TVING", 6000),
                price("WATCHA", 7000));

        ContentWishItemVO itemA = wishItem("Netflix", "TVING");
        ContentWishItemVO itemB = wishItem("Netflix", "Watcha");

        SubscriptionCalculationResultVO result =
                service.calculate(List.of(itemA, itemB), null, null, null);

        assertEquals(Set.of("TVING", "WATCHA"), codesOf(result));
        assertEquals(13000, result.getTotalMonthlyPrice());
    }

    @Test
    void contentOnlyOnUnpricedPlatformShouldBeUnresolvedAndExcludedFromTotal() {
        stubPrices(price("NETFLIX", 17000));

        ContentWishItemVO covered = wishItem("Netflix");
        ContentWishItemVO uncovered = wishItem("Apple TV+");

        SubscriptionCalculationResultVO result =
                service.calculate(List.of(covered, uncovered), null, null, null);

        assertEquals(Set.of("NETFLIX"), codesOf(result));
        assertEquals(17000, result.getTotalMonthlyPrice());
        assertEquals(1, result.getUnresolvedItemList().size());
        assertEquals(uncovered, result.getUnresolvedItemList().get(0));
    }

    @Test
    void allPlatformMonthlyPriceShouldSumEveryPlatformInUniverse() {
        stubPrices(
                price("NETFLIX", 17000),
                price("TVING", 13900));

        ContentWishItemVO itemA = wishItem("Netflix");
        ContentWishItemVO itemB = wishItem("TVING");

        SubscriptionCalculationResultVO result =
                service.calculate(List.of(itemA, itemB), null, null, null);

        assertEquals(30900, result.getAllPlatformMonthlyPrice());
    }

    private void stubPrices(PlatformPriceVO... prices) {
        when(ottDiscountDAO.selectBestPriceByPlatform(any(), any(), any()))
                .thenReturn(List.of(prices));
    }

    private PlatformPriceVO price(String code, int bestPrice) {
        PlatformPriceVO priceVO = new PlatformPriceVO();
        priceVO.setPlatformCode(code);
        priceVO.setPlatformName(code);
        priceVO.setRegularPrice(bestPrice);
        priceVO.setBestPrice(bestPrice);
        return priceVO;
    }

    private ContentWishItemVO wishItem(String... contentPlatformNames) {
        ContentWishItemVO item = new ContentWishItemVO();
        item.setTitle("title-" + String.join(",", contentPlatformNames));
        item.setPlatformNameList(List.of(contentPlatformNames));
        return item;
    }

    private Set<String> codesOf(SubscriptionCalculationResultVO result) {
        return result.getSelectedPlatformList()
                .stream()
                .map(PlatformPriceVO::getPlatformCode)
                .collect(Collectors.toSet());
    }
}
