package com.project.oditji.search.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;

import com.project.oditji.goods.service.GoodsService;
import com.project.oditji.search.service.SearchContentPageCacheService;
import com.project.oditji.search.vo.SearchResultPageVO;
import com.project.oditji.search.vo.SearchVO;
import com.project.oditji.tmdb.dao.TmdbDAO;
import com.project.oditji.wish.service.WishService;

import jakarta.servlet.http.HttpSession;

/** SearchController의 null 결과 목록, 가격 교환, 상품 페이지 보정 분기를 검증합니다. */
class SearchControllerResidualFlowCoverageTest {

    @Test
    void searchShouldNormalizeNullListsSwapPricesClampGoodsPageAndReplaceNullResults() {
        SearchContentPageCacheService pageCacheService = mock(SearchContentPageCacheService.class);
        GoodsService goodsService = mock(GoodsService.class);
        TmdbDAO tmdbDAO = mock(TmdbDAO.class);
        WishService wishService = mock(WishService.class);
        HttpSession session = mock(HttpSession.class);

        SearchController controller = new SearchController(
                pageCacheService,
                goodsService,
                tmdbDAO,
                wishService);
        ReflectionTestUtils.setField(controller, "imageBaseUrl", "https://image.test/");

        SearchResultPageVO contentPage = mock(SearchResultPageVO.class);
        when(contentPage.getResultList()).thenReturn(null);
        when(contentPage.getTotalResults()).thenReturn(0);
        when(contentPage.getTotalPages()).thenReturn(0);
        when(pageCacheService.getContentPage(
                anyString(), anyInt(), anyInt(), anyList(), anyList(), anyList(), anyList()))
                .thenReturn(contentPage);
        when(pageCacheService.getFirstPagePreview(
                anyString(), anyInt(), anyInt(), anyList(), anyList(), anyList(), anyList()))
                .thenReturn(null);

        when(goodsService.countSearchGoods(
                anyString(), anyList(), any(), any(), anyBoolean(), anyBoolean()))
                .thenReturn(13);
        when(goodsService.searchGoods(
                anyString(), anyList(), any(), any(), anyBoolean(), anyBoolean(), anyInt(), anyInt()))
                .thenReturn(null);
        when(goodsService.getSearchProductTypes()).thenReturn(null);
        when(tmdbDAO.selectActivePlatformList()).thenReturn(null);
        when(wishService.getWishedProductNoSet(null)).thenReturn(Set.of());

        SearchVO searchVO = new SearchVO();
        searchVO.setKeyword("  test  ");
        searchVO.setMinPrice(9000);
        searchVO.setMaxPrice(1000);
        searchVO.setGoodsPage(99);
        searchVO.setSearchTab("goods");

        Model model = new ExtendedModelMap();
        String view = controller.searchResult(searchVO, session, model);

        assertEquals("search/searchResult", view);
        assertEquals(1000, searchVO.getMinPrice());
        assertEquals(9000, searchVO.getMaxPrice());
        assertEquals(2, searchVO.getGoodsPage());
        assertEquals("GOODS", searchVO.getSearchTab());
        assertTrue(((List<?>) model.getAttribute("allContentResults")).isEmpty());
        assertTrue(((List<?>) model.getAttribute("goodsResults")).isEmpty());
        assertTrue(((List<?>) model.getAttribute("allGoodsResults")).isEmpty());
        assertTrue(((List<?>) model.getAttribute("availableProductTypes")).isEmpty());
    }

    @Test
    void searchTitleShouldCoverKeywordOnlyAndKeywordWithFilter() {
        SearchController controller = new SearchController(
                mock(SearchContentPageCacheService.class),
                mock(GoodsService.class),
                mock(TmdbDAO.class),
                mock(WishService.class));

        SearchVO keywordOnly = new SearchVO();
        keywordOnly.setKeyword("닥터X");
        assertEquals("'닥터X' 검색 결과",
                ReflectionTestUtils.invokeMethod(controller, "makeSearchTitle", keywordOnly));

        SearchVO keywordAndFilter = new SearchVO();
        keywordAndFilter.setKeyword("닥터X");
        keywordAndFilter.setAgeRatings(List.of("15"));
        assertEquals("'닥터X' 조건 검색 결과",
                ReflectionTestUtils.invokeMethod(controller, "makeSearchTitle", keywordAndFilter));
    }
}
