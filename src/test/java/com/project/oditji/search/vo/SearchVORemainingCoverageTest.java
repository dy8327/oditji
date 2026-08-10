package com.project.oditji.search.vo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * 검색 VO의 페이지 보정, null 컬렉션 방어와 단축 평가 조건을 보완합니다.
 */
class SearchVORemainingCoverageTest {

    @Test
    void listSettersShouldConvertNullToEmptyLists() {
        SearchVO search = new SearchVO();

        search.setContentCategories(null);
        search.setGenreCodes(null);
        search.setProviderIds(null);
        search.setAgeRatings(null);
        search.setProductTypes(null);

        assertTrue(search.getContentCategories().isEmpty());
        assertTrue(search.getGenreCodes().isEmpty());
        assertTrue(search.getProviderIds().isEmpty());
        assertTrue(search.getAgeRatings().isEmpty());
        assertTrue(search.getProductTypes().isEmpty());
    }

    @Test
    void pageGettersShouldNormalizeNonPositiveValuesAndLegacyPage() {
        SearchVO search = new SearchVO();

        search.setContentPage(0);
        search.setGoodsPage(-5);

        assertEquals(1, search.getContentPage());
        assertEquals(1, search.getGoodsPage());

        search.setPage(3);
        assertEquals(3, search.getPage());
        assertEquals(3, search.getContentPage());

        search.setGoodsPage(4);
        assertEquals(4, search.getGoodsPage());
    }

    @Test
    void scalarFieldsShouldRoundTrip() {
        SearchVO search = new SearchVO();

        search.setKeyword("keyword");
        search.setMinPrice(1000);
        search.setMaxPrice(5000);
        search.setDiscountOnly(true);
        search.setInStockOnly(true);
        search.setSearchTab("GOODS");

        assertEquals("keyword", search.getKeyword());
        assertEquals(1000, search.getMinPrice());
        assertEquals(5000, search.getMaxPrice());
        assertTrue(search.isDiscountOnly());
        assertTrue(search.isInStockOnly());
        assertEquals("GOODS", search.getSearchTab());
    }

    @Test
    void hasMethodsShouldAlsoCoverActuallyNullBackingLists() {
        SearchVO search = new SearchVO();

        ReflectionTestUtils.setField(
                search,
                "contentCategories",
                null);
        assertFalse(search.hasContentCategories());

        ReflectionTestUtils.setField(
                search,
                "genreCodes",
                null);
        assertFalse(search.hasGenreCodes());

        ReflectionTestUtils.setField(
                search,
                "providerIds",
                null);
        assertFalse(search.hasProviderIds());

        ReflectionTestUtils.setField(
                search,
                "ageRatings",
                null);
        assertFalse(search.hasAgeRatings());

        ReflectionTestUtils.setField(
                search,
                "productTypes",
                null);
        assertFalse(search.hasProductTypes());
    }

    @Test
    void filterCompositionShouldCoverLaterOrOperands() {
        SearchVO search = new SearchVO();

        search.setContentCategories(List.of());
        search.setGenreCodes(List.of());
        search.setProviderIds(List.of());
        search.setAgeRatings(List.of());

        search.setProductTypes(List.of());
        search.setMinPrice(null);
        search.setMaxPrice(null);
        search.setDiscountOnly(false);
        search.setInStockOnly(false);

        assertFalse(search.hasGoodsFilter());
        assertFalse(search.hasFilter());

        search.setInStockOnly(true);

        assertTrue(search.hasGoodsFilter());
        assertTrue(search.hasFilter());
    }
}
