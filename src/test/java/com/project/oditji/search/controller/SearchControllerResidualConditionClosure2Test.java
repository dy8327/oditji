package com.project.oditji.search.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.ui.ExtendedModelMap;

import com.project.oditji.goods.service.GoodsService;
import com.project.oditji.search.service.SearchContentPageCacheService;
import com.project.oditji.search.service.SearchKeywordHistoryService;
import com.project.oditji.search.vo.SearchResultPageVO;
import com.project.oditji.search.vo.SearchVO;
import com.project.oditji.tmdb.dao.TmdbDAO;
import com.project.oditji.wish.service.WishService;

import jakarta.servlet.http.HttpSession;

/** 가격 교환과 상품 페이지 보정의 잔여 단축평가 분기를 보완합니다. */
class SearchControllerResidualConditionClosure2Test {

    @Test
    void minOnlyAndInRangeGoodsPageShouldCoverFalseOperands() {
        SearchContentPageCacheService pageCacheService =
                mock(SearchContentPageCacheService.class);
        SearchKeywordHistoryService keywordHistoryService =
                mock(SearchKeywordHistoryService.class);
        GoodsService goodsService = mock(GoodsService.class);
        TmdbDAO tmdbDAO = mock(TmdbDAO.class);
        WishService wishService = mock(WishService.class);
        HttpSession session = mock(HttpSession.class);

        SearchController controller = new SearchController(
                pageCacheService,
                keywordHistoryService,
                goodsService,
                tmdbDAO,
                wishService);
        ReflectionTestUtils.setField(
                controller,
                "imageBaseUrl",
                "https://image.test/");

        SearchResultPageVO page = new SearchResultPageVO();
        page.setResultList(List.of());
        page.setTotalPages(0);
        page.setTotalResults(0);

        when(pageCacheService.getContentPage(
                anyString(),
                anyInt(),
                anyInt(),
                anyList(),
                anyList(),
                anyList(),
                anyList()))
                .thenReturn(page);
        when(pageCacheService.getFirstPagePreview(
                anyString(),
                anyInt(),
                anyInt(),
                anyList(),
                anyList(),
                anyList(),
                anyList()))
                .thenReturn(List.of());
        when(goodsService.countSearchGoods(
                anyString(),
                anyList(),
                any(),
                any(),
                anyBoolean(),
                anyBoolean()))
                .thenReturn(1);
        when(goodsService.searchGoods(
                anyString(),
                anyList(),
                any(),
                any(),
                anyBoolean(),
                anyBoolean(),
                anyInt(),
                anyInt()))
                .thenReturn(List.of());
        when(goodsService.getSearchProductTypes())
                .thenReturn(List.of());
        when(tmdbDAO.selectActivePlatformList())
                .thenReturn(List.of());

        SearchVO searchVO = new SearchVO();
        searchVO.setMinPrice(1000);
        searchVO.setMaxPrice(null);
        searchVO.setGoodsPage(1);

        String view = controller.searchResult(
                searchVO,
                session,
                new ExtendedModelMap());

        assertEquals("search/searchResult", view);
        assertEquals(1000, searchVO.getMinPrice());
        assertNull(searchVO.getMaxPrice());
        assertEquals(1, searchVO.getGoodsPage());
    }

    @Test
    void maxOnlyShouldRemainUnswappedWhenMinimumIsNull() {
        SearchContentPageCacheService pageCacheService =
                mock(SearchContentPageCacheService.class);
        GoodsService goodsService = mock(GoodsService.class);

        SearchController controller = new SearchController(
                pageCacheService,
                mock(SearchKeywordHistoryService.class),
                goodsService,
                mock(TmdbDAO.class),
                mock(WishService.class));
        ReflectionTestUtils.setField(
                controller,
                "imageBaseUrl",
                "https://image.test/");

        SearchResultPageVO page = new SearchResultPageVO();
        page.setResultList(List.of());

        when(pageCacheService.getContentPage(
                anyString(),
                anyInt(),
                anyInt(),
                anyList(),
                anyList(),
                anyList(),
                anyList()))
                .thenReturn(page);
        when(pageCacheService.getFirstPagePreview(
                anyString(),
                anyInt(),
                anyInt(),
                anyList(),
                anyList(),
                anyList(),
                anyList()))
                .thenReturn(List.of());
        when(goodsService.getSearchProductTypes())
                .thenReturn(List.of());

        SearchVO searchVO = new SearchVO();
        searchVO.setMaxPrice(5000);

        controller.searchResult(
                searchVO,
                mock(HttpSession.class),
                new ExtendedModelMap());

        assertNull(searchVO.getMinPrice());
        assertEquals(5000, searchVO.getMaxPrice());
    }
}
