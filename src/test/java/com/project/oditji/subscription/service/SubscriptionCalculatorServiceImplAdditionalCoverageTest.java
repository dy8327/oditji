package com.project.oditji.subscription.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.project.oditji.event.dao.OttDiscountDAO;
import com.project.oditji.subscription.vo.ContentWishItemVO;
import com.project.oditji.subscription.vo.PlatformPriceVO;
import com.project.oditji.subscription.vo.SubscriptionCalculationResultVO;

/** 구독 조합 계산기의 null, 필터, 가격 정제, 동률 및 방어 분기를 보완합니다. */
class SubscriptionCalculatorServiceImplAdditionalCoverageTest {

    private OttDiscountDAO ottDiscountDAO;
    private com.project.oditji.subscription.dao.SubscriptionDAO subscriptionDAO;
    private SubscriptionCalculatorServiceImpl service;

    @BeforeEach
    void setUp() {
        ottDiscountDAO = mock(OttDiscountDAO.class);
        subscriptionDAO = mock(com.project.oditji.subscription.dao.SubscriptionDAO.class);
        service = new SubscriptionCalculatorServiceImpl(ottDiscountDAO, subscriptionDAO);
    }

    @Test
    void nullWishlistShouldReturnDefaultResult() {
        SubscriptionCalculationResultVO result =
                service.calculate(null, null, null, null);

        assertTrue(result.getSelectedPlatformList().isEmpty());
        assertTrue(result.getUnresolvedItemList().isEmpty());
        assertEquals(0, result.getTotalMonthlyPrice());
    }

    @Test
    void calculateShouldTrimSelectedFilterAndConvertBlankFiltersToNull() {
        when(ottDiscountDAO.selectBestPriceByPlatform("SKT", null, null))
                .thenReturn(List.of(price("NETFLIX", 17000, 12000)));

        SubscriptionCalculationResultVO result = service.calculate(
                List.of(wishItem("Netflix")),
                "  SKT  ",
                "   ",
                null);

        assertEquals(12000, result.getTotalMonthlyPrice());
        verify(ottDiscountDAO)
                .selectBestPriceByPlatform("SKT", null, null);
    }

    @Test
    void nullPriceListShouldMakeEveryRealItemUnresolved() {
        when(ottDiscountDAO.selectBestPriceByPlatform(any(), any(), any()))
                .thenReturn(null);
        ContentWishItemVO item = wishItem("Netflix");

        SubscriptionCalculationResultVO result =
                service.calculate(List.of(item), null, null, null);

        assertEquals(List.of(item), result.getUnresolvedItemList());
        assertTrue(result.getSelectedPlatformList().isEmpty());
    }

    @Test
    void invalidPriceRowsAndNullWishlistItemShouldBeIgnored() {
        PlatformPriceVO missingCode = price(null, 10000, 8000);
        PlatformPriceVO missingBestPrice = price("TVING", 10000, null);
        List<PlatformPriceVO> prices = new ArrayList<PlatformPriceVO>();
        prices.add(null);
        prices.add(missingCode);
        prices.add(missingBestPrice);
        prices.add(price("NETFLIX", 17000, 12000));
        when(ottDiscountDAO.selectBestPriceByPlatform(any(), any(), any()))
                .thenReturn(prices);

        List<ContentWishItemVO> wishItems = new ArrayList<ContentWishItemVO>();
        wishItems.add(null);
        wishItems.add(wishItem(null, "Apple TV+", "TVING", "Netflix"));

        SubscriptionCalculationResultVO result =
                service.calculate(wishItems, null, null, null);

        assertEquals(12000, result.getTotalMonthlyPrice());
        assertEquals(Set.of("NETFLIX"), selectedCodes(result));
        assertTrue(result.getUnresolvedItemList().isEmpty());
    }

    @Test
    void regularPriceNullShouldFallBackToBestPriceForRegularTotal() {
        when(ottDiscountDAO.selectBestPriceByPlatform(any(), any(), any()))
                .thenReturn(List.of(price("NETFLIX", null, 9000)));

        SubscriptionCalculationResultVO result =
                service.calculate(
                        List.of(wishItem("Netflix")),
                        null,
                        null,
                        null);

        assertEquals(9000, result.getTotalMonthlyPrice());
        assertEquals(9000, result.getTotalRegularMonthlyPrice());
    }

    @Test
    void equalPriceShouldPreferLaterSinglePlatformOverEarlierTwoPlatformCombo() {
        when(ottDiscountDAO.selectBestPriceByPlatform(any(), any(), any()))
                .thenReturn(List.of(
                        price("NETFLIX", 5, 5),
                        price("TVING", 5, 5),
                        price("WATCHA", 10, 10)));

        ContentWishItemVO first = wishItem("Netflix", "Watcha");
        ContentWishItemVO second = wishItem("TVING", "Watcha");

        SubscriptionCalculationResultVO result =
                service.calculate(
                        List.of(first, second),
                        null,
                        null,
                        null);

        assertEquals(Set.of("WATCHA"), selectedCodes(result));
        assertEquals(10, result.getTotalMonthlyPrice());
    }

    @Test
    void equalPriceAndEqualSizeShouldKeepFirstCoveringPlatform() {
        when(ottDiscountDAO.selectBestPriceByPlatform(any(), any(), any()))
                .thenReturn(List.of(
                        price("NETFLIX", 10, 10),
                        price("TVING", 10, 10)));

        SubscriptionCalculationResultVO result =
                service.calculate(
                        List.of(wishItem("Netflix", "TVING")),
                        null,
                        null,
                        null);

        assertEquals(Set.of("NETFLIX"), selectedCodes(result));
        assertEquals(10, result.getTotalMonthlyPrice());
    }

    @Test
    void blankToNullShouldCoverNullBlankAndTrimmedValues() {
        assertNull(invokeBlankToNull(null));
        assertNull(invokeBlankToNull("   "));
        assertEquals("KT", invokeBlankToNull("  KT  "));
    }

    @Test
    void cheapestCombinationShouldReturnEmptyUniverseWhenNoPlatformsExist() {
        Set<String> universe = new LinkedHashSet<String>();

        Set<String> result = invokeFindCheapest(
                universe,
                List.of(),
                Map.of());

        assertSame(universe, result);
    }

    @Test
    void cheapestCombinationShouldUseUniverseFallbackAboveBruteForceLimit() {
        Set<String> universe = new LinkedHashSet<String>();
        Map<String, PlatformPriceVO> prices =
                new LinkedHashMap<String, PlatformPriceVO>();

        for (int index = 0; index < 21; index++) {
            String code = "P" + index;
            universe.add(code);
            prices.put(code, price(code, 1, 1));
        }

        Set<String> result = invokeFindCheapest(
                universe,
                List.of(Set.of("P0")),
                prices);

        assertSame(universe, result);
    }

    @Test
    void cheapestCombinationShouldFallBackWhenRequirementsCannotBeCovered() {
        Set<String> universe = new LinkedHashSet<String>(List.of("A"));
        Map<String, PlatformPriceVO> prices =
                Map.of("A", price("A", 10, 10));

        Set<String> result = invokeFindCheapest(
                universe,
                List.of(Set.of("B")),
                prices);

        assertSame(universe, result);
    }

    @Test
    void coversAllRequirementsShouldCoverHitMissAndMultipleCodes() {
        Set<String> candidate = Set.of("B");

        assertTrue(invokeCoversAll(
                candidate,
                List.of(Set.of("A", "B"))));
        assertFalse(invokeCoversAll(
                candidate,
                List.of(Set.of("A"))));
    }

    private String invokeBlankToNull(String value) {
        return ReflectionTestUtils.invokeMethod(
                service,
                "blankToNull",
                value);
    }

    @SuppressWarnings("unchecked")
    private Set<String> invokeFindCheapest(
            Set<String> universe,
            List<Set<String>> requirements,
            Map<String, PlatformPriceVO> prices) {

        return (Set<String>) ReflectionTestUtils.invokeMethod(
                service,
                "findCheapestCombination",
                universe,
                requirements,
                prices);
    }

    private boolean invokeCoversAll(
            Set<String> candidate,
            List<Set<String>> requirements) {

        Boolean result = ReflectionTestUtils.invokeMethod(
                service,
                "coversAllRequirements",
                candidate,
                requirements);

        return Boolean.TRUE.equals(result);
    }

    private PlatformPriceVO price(
            String code,
            Integer regularPrice,
            Integer bestPrice) {

        PlatformPriceVO price = new PlatformPriceVO();
        price.setPlatformCode(code);
        price.setPlatformName(code);
        price.setRegularPrice(regularPrice);
        price.setBestPrice(bestPrice);
        return price;
    }

    private ContentWishItemVO wishItem(String... platformNames) {
        ContentWishItemVO item = new ContentWishItemVO();
        List<String> names = new ArrayList<String>();
        for (String platformName : platformNames) {
            names.add(platformName);
        }
        item.setPlatformNameList(names);
        return item;
    }

    private Set<String> selectedCodes(
            SubscriptionCalculationResultVO result) {

        Set<String> codes = new LinkedHashSet<String>();
        for (PlatformPriceVO price : result.getSelectedPlatformList()) {
            codes.add(price.getPlatformCode());
        }
        return codes;
    }
}
