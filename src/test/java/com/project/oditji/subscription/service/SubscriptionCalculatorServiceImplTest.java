package com.project.oditji.subscription.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.project.oditji.event.dao.OttDiscountDAO;
import com.project.oditji.subscription.dao.SubscriptionDAO;
import com.project.oditji.subscription.vo.ContentWishItemVO;
import com.project.oditji.subscription.vo.PlatformPriceVO;
import com.project.oditji.subscription.vo.SubscriptionCalculationResultVO;
import com.project.oditji.subscription.vo.SubscriptionShareVO;

/** 위시리스트를 가장 저렴하게 커버하는 OTT 조합 계산 로직을 검증합니다. */
@ExtendWith(MockitoExtension.class)
class SubscriptionCalculatorServiceImplTest {

    @Mock
    private OttDiscountDAO ottDiscountDAO;

    @Mock
    private SubscriptionDAO subscriptionDAO;

    private SubscriptionCalculatorServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new SubscriptionCalculatorServiceImpl(ottDiscountDAO, subscriptionDAO);
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

    /* ---------- 결과 저장 / 공유 ---------- */

    @Test
    void saveResultShouldThrowWhenResultIsNull() {
        assertThrows(IllegalArgumentException.class,
                () -> service.saveResult(null, 1L));
    }

    @Test
    void saveResultShouldThrowWhenSelectedPlatformListIsEmpty() {
        SubscriptionCalculationResultVO result = new SubscriptionCalculationResultVO();

        assertThrows(IllegalArgumentException.class,
                () -> service.saveResult(result, 1L));
    }

    @Test
    void saveResultShouldPersistShareVoAndReturnResultId() {
        SubscriptionCalculationResultVO result = new SubscriptionCalculationResultVO();
        result.setSelectedPlatformList(List.of(price("NETFLIX", 11900)));
        result.setTotalRegularMonthlyPrice(17000);
        result.setTotalMonthlyPrice(11900);
        result.setAllPlatformMonthlyPrice(17000);

        String resultId = service.saveResult(result, 5L);

        assertNotNull(resultId);
        assertTrue(resultId.startsWith("SUBS_"));

        ArgumentCaptor<SubscriptionShareVO> captor =
                ArgumentCaptor.forClass(SubscriptionShareVO.class);
        verify(subscriptionDAO).insertResult(captor.capture());

        SubscriptionShareVO saved = captor.getValue();
        assertEquals(resultId, saved.getResultId());
        assertEquals(5L, saved.getMemberNo());
        assertEquals(17000, saved.getTotalPrice());
        assertEquals(5100, saved.getDiscountPrice());
        assertEquals(11900, saved.getFinalPrice());
        assertTrue(saved.getSelectedServicesJson().contains("NETFLIX"));
    }

    @Test
    void saveResultShouldAllowNullMemberNoForGuest() {
        SubscriptionCalculationResultVO result = new SubscriptionCalculationResultVO();
        result.setSelectedPlatformList(List.of(price("TVING", 13500)));

        service.saveResult(result, null);

        ArgumentCaptor<SubscriptionShareVO> captor =
                ArgumentCaptor.forClass(SubscriptionShareVO.class);
        verify(subscriptionDAO).insertResult(captor.capture());

        assertNull(captor.getValue().getMemberNo());
    }

    @Test
    void restoreResultShouldReturnNullWhenResultIdIsBlank() {
        assertNull(service.restoreResult("   "));
        assertNull(service.restoreResult(null));
    }

    @Test
    void restoreResultShouldReturnNullWhenNotFound() {
        when(subscriptionDAO.selectResultById(anyString())).thenReturn(null);

        assertNull(service.restoreResult("SUBS_UNKNOWN"));
    }

    @Test
    void restoreResultShouldRebuildCalculationResultFromSavedJson() {
        SubscriptionCalculationResultVO original = new SubscriptionCalculationResultVO();
        original.setSelectedPlatformList(List.of(price("NETFLIX", 11900)));
        original.setTotalRegularMonthlyPrice(17000);
        original.setTotalMonthlyPrice(11900);
        original.setAllPlatformMonthlyPrice(17000);
        original.setUnresolvedItemList(List.of(wishItem("WATCHA")));

        ArgumentCaptor<SubscriptionShareVO> captor =
                ArgumentCaptor.forClass(SubscriptionShareVO.class);
        service.saveResult(original, null);
        verify(subscriptionDAO).insertResult(captor.capture());

        SubscriptionShareVO saved = captor.getValue();
        when(subscriptionDAO.selectResultById(saved.getResultId())).thenReturn(saved);

        SubscriptionCalculationResultVO restored =
                service.restoreResult(saved.getResultId());

        assertNotNull(restored);
        assertEquals(1, restored.getSelectedPlatformList().size());
        assertEquals("NETFLIX", restored.getSelectedPlatformList().get(0).getPlatformCode());
        assertEquals(11900, restored.getSelectedPlatformList().get(0).getBestPrice());
        assertEquals(17000, restored.getTotalRegularMonthlyPrice());
        assertEquals(11900, restored.getTotalMonthlyPrice());
        assertEquals(17000, restored.getAllPlatformMonthlyPrice());
        assertEquals(1, restored.getUnresolvedItemList().size());
    }

    @Test
    void restoreResultShouldReturnDefensiveResultWhenJsonIsCorrupted() {
        SubscriptionShareVO corrupted = new SubscriptionShareVO();
        corrupted.setResultId("SUBS_BROKEN");
        corrupted.setTotalPrice(17000);
        corrupted.setFinalPrice(11900);
        corrupted.setSelectedServicesJson("{not-valid-json");

        when(subscriptionDAO.selectResultById("SUBS_BROKEN")).thenReturn(corrupted);

        SubscriptionCalculationResultVO restored = service.restoreResult("SUBS_BROKEN");

        assertNotNull(restored);
        assertEquals(17000, restored.getTotalRegularMonthlyPrice());
        assertEquals(11900, restored.getTotalMonthlyPrice());
        assertTrue(restored.getSelectedPlatformList().isEmpty());
    }
}
