package com.project.oditji.goods.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.project.oditji.goods.dao.GoodsDAO;

/**
 * GoodsServiceImpl의 정렬/가격/페이지 helper 경계값을 추가 검증합니다.
 */
class GoodsServiceImplAdditionalCoverageTest {

    private GoodsServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new GoodsServiceImpl(mock(GoodsDAO.class));
    }

    @Test
    void listTypeAndSortShouldCoverNullBlankKnownAndUnknownValues() {
        assertEquals("all", invoke("normalizeListType", (Object) null));
        assertEquals("popular", invoke("normalizeListType", " POPULAR "));
        assertEquals("all", invoke("normalizeListType", "latest"));

        assertEquals("popular", invoke("normalizeSort", (Object) null));
        assertEquals("popular", invoke("normalizeSort", " "));
        assertEquals("latest", invoke("normalizeSort", " LATEST "));
        assertEquals("price_asc", invoke("normalizeSort", "PRICE_ASC"));
        assertEquals("price_desc", invoke("normalizeSort", "price_desc"));
        assertEquals("title", invoke("normalizeSort", "TITLE"));
        assertEquals("popular", invoke("normalizeSort", "unsupported"));
    }

    @Test
    void priceAndPageHelpersShouldCoverEveryBoundary() {
        assertNull(invoke("normalizePrice", (Object) null));
        assertEquals(0, ((Integer) invoke("normalizePrice", -1)).intValue());
        assertEquals(100, ((Integer) invoke("normalizePrice", 100)).intValue());

        assertEquals(1, ((Integer) invoke("normalizePage", 0)).intValue());
        assertEquals(3, ((Integer) invoke("normalizePage", 3)).intValue());

        assertEquals(12, ((Integer) invoke("normalizePageSize", 0)).intValue());
        assertEquals(12, ((Integer) invoke("normalizePageSize", -1)).intValue());
        assertEquals(50, ((Integer) invoke("normalizePageSize", 50)).intValue());
        assertEquals(100, ((Integer) invoke("normalizePageSize", 500)).intValue());
    }

    @Test
    void priceBoundsShouldSwapOnlyWhenBothNormalizedValuesArePresentAndReversed() {
        Object normal = invoke("normalizePriceBounds", 100, 200);
        assertEquals(100, ((Integer) ReflectionTestUtils.invokeMethod(normal, "minPrice")).intValue());
        assertEquals(200, ((Integer) ReflectionTestUtils.invokeMethod(normal, "maxPrice")).intValue());

        Object swapped = invoke("normalizePriceBounds", 500, 100);
        assertEquals(100, ((Integer) ReflectionTestUtils.invokeMethod(swapped, "minPrice")).intValue());
        assertEquals(500, ((Integer) ReflectionTestUtils.invokeMethod(swapped, "maxPrice")).intValue());

        Object onlyMax = invoke("normalizePriceBounds", null, 300);
        assertNull(ReflectionTestUtils.invokeMethod(onlyMax, "minPrice"));
        assertEquals(300, ((Integer) ReflectionTestUtils.invokeMethod(onlyMax, "maxPrice")).intValue());
    }

    @Test
    void textFiltersShouldNormalizeNullDuplicateAndAllowedValues() {
        Object filters = invoke(
                "normalizeTextFilters",
                null,
                Arrays.asList(null, " BOOK ", "BOOK", " "),
                Arrays.asList("UNDER_10000", "BAD", "UNDER_10000"),
                Arrays.asList("IN_STOCK", "BAD", "SOLD_OUT"));

        assertEquals("", ReflectionTestUtils.invokeMethod(filters, "keyword"));
        assertEquals(List.of("BOOK"), ReflectionTestUtils.invokeMethod(filters, "productTypes"));
        assertEquals(List.of("UNDER_10000"), ReflectionTestUtils.invokeMethod(filters, "priceRanges"));
        assertEquals(List.of("IN_STOCK", "SOLD_OUT"), ReflectionTestUtils.invokeMethod(filters, "stockStatus"));
        assertEquals(
                1,
                ((List<?>) ReflectionTestUtils.invokeMethod(
                        filters,
                        "productTypes"))
                        .size());
    }

    private Object invoke(String methodName, Object... arguments) {
        return ReflectionTestUtils.invokeMethod(
                service,
                methodName,
                arguments);
    }
}
