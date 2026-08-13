package com.project.oditji.search.vo;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

/** 검색 조건 VO의 키워드·콘텐츠·상품 필터 조합 분기를 검증합니다. */
class SearchVOBranchCoverageTest {

    @Test
    void emptySearchShouldReportNoFilters() {
        SearchVO search = new SearchVO();
        assertFalse(search.hasKeyword());
        assertFalse(search.hasContentCategories());
        assertFalse(search.hasGenreCodes());
        assertFalse(search.hasProviderIds());
        assertFalse(search.hasAgeRatings());
        assertFalse(search.hasProductTypes());
        assertFalse(search.hasGoodsFilter());
        assertFalse(search.hasFilter());
    }

    @Test
    void keywordAndEachContentCollectionShouldActivateFilter() {
        SearchVO search = new SearchVO();
        search.setKeyword(" 검색 ");
        assertTrue(search.hasKeyword());
        assertFalse(search.hasFilter());

        search.setKeyword(" ");
        search.setContentCategories(List.of("MOVIE"));
        assertTrue(search.hasContentCategories());
        assertTrue(search.hasFilter());

        search.setContentCategories(List.of());
        search.setGenreCodes(List.of("28"));
        assertTrue(search.hasGenreCodes());

        search.setGenreCodes(null);
        search.setProviderIds(List.of("8"));
        assertTrue(search.hasProviderIds());

        search.setProviderIds(List.of());
        search.setAgeRatings(List.of("15"));
        assertTrue(search.hasAgeRatings());
    }

    @Test
    void goodsFiltersShouldCoverTypePriceDiscountAndStock() {
        SearchVO search = new SearchVO();
        search.setProductTypes(List.of("FIGURE"));
        assertTrue(search.hasProductTypes());
        assertTrue(search.hasGoodsFilter());

        search.setProductTypes(List.of());
        search.setMinPrice(1000);
        assertTrue(search.hasGoodsFilter());

        search.setMinPrice(null);
        search.setMaxPrice(10000);
        assertTrue(search.hasGoodsFilter());

        search.setMaxPrice(null);
        search.setDiscountOnly(true);
        assertTrue(search.hasGoodsFilter());

        search.setDiscountOnly(false);
        search.setInStockOnly(true);
        assertTrue(search.hasGoodsFilter());
        assertTrue(search.hasFilter());
    }
}
