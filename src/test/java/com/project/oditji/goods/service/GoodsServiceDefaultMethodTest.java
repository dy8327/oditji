package com.project.oditji.goods.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.project.oditji.goods.vo.GoodsVO;

/** 상품 검색 서비스의 기존 호출부 호환 기본 메서드를 검증합니다. */
class GoodsServiceDefaultMethodTest {

    private GoodsService service;
    private List<String> productTypes;

    @BeforeEach
    void setUp() {
        service = mock(GoodsService.class, CALLS_REAL_METHODS);
        productTypes = List.of("굿즈");
    }

    @Test
    void searchWithTypeShouldDelegateWithoutExtendedFilters() {
        List<GoodsVO> expected = List.of(new GoodsVO());
        when(service.searchGoods(
                "검색어",
                productTypes,
                1000,
                50000,
                true,
                true,
                null,
                null,
                "popular",
                1,
                10)).thenReturn(expected);

        List<GoodsVO> result = service.searchGoods(
                "검색어",
                productTypes,
                1000,
                50000,
                true,
                true,
                "popular",
                1,
                10);

        assertSame(expected, result);
        verify(service).searchGoods(
                "검색어",
                productTypes,
                1000,
                50000,
                true,
                true,
                null,
                null,
                "popular",
                1,
                10);
    }

    @Test
    void searchWithoutTypeShouldUseAllAndNoExtendedFilters() {
        List<GoodsVO> expected = List.of(new GoodsVO());
        when(service.searchGoods(
                "검색어",
                productTypes,
                null,
                null,
                false,
                false,
                null,
                null,
                "all",
                2,
                20)).thenReturn(expected);

        List<GoodsVO> result = service.searchGoods(
                "검색어",
                productTypes,
                null,
                null,
                false,
                false,
                2,
                20);

        assertSame(expected, result);
        verify(service).searchGoods(
                "검색어",
                productTypes,
                null,
                null,
                false,
                false,
                null,
                null,
                "all",
                2,
                20);
    }

    @Test
    void countWithoutExtendedFiltersShouldDelegateWithNullLists() {
        when(service.countSearchGoods(
                "검색어",
                productTypes,
                1000,
                50000,
                true,
                false,
                null,
                null)).thenReturn(3);

        int result = service.countSearchGoods(
                "검색어",
                productTypes,
                1000,
                50000,
                true,
                false);

        assertEquals(3, result);
        verify(service).countSearchGoods(
                "검색어",
                productTypes,
                1000,
                50000,
                true,
                false,
                null,
                null);
    }
}
