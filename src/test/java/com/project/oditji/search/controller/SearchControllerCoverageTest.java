package com.project.oditji.search.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.ui.ExtendedModelMap;

import com.project.oditji.goods.service.GoodsService;
import com.project.oditji.goods.vo.GoodsVO;
import com.project.oditji.search.service.SearchContentPageCacheService;
import com.project.oditji.search.vo.SearchResultPageVO;
import com.project.oditji.search.vo.SearchResultVO;
import com.project.oditji.search.vo.SearchVO;
import com.project.oditji.tmdb.dao.TmdbDAO;
import com.project.oditji.tmdb.vo.OttPlatformVO;

/** 통합 검색 입력값 정규화, 페이징, 제목 및 OTT 로고 구성을 검증합니다. */
@ExtendWith(MockitoExtension.class)
class SearchControllerCoverageTest {

    @Mock
    private SearchContentPageCacheService searchContentPageCacheService;

    @Mock
    private GoodsService goodsService;

    @Mock
    private TmdbDAO tmdbDAO;

    private SearchController controller;

    @BeforeEach
    void setUp() {
        controller = new SearchController(
                searchContentPageCacheService,
                goodsService,
                tmdbDAO);
        ReflectionTestUtils.setField(
                controller,
                "imageBaseUrl",
                "https://image.test/");
    }

    @Test
    void nullSearchRequestShouldUseSafeDefaultsAndEmptyCollections() {
        SearchResultPageVO page = page(null, 0, 0);
        when(goodsService.countSearchGoods(
                "", List.of(), null, null, false, false))
                .thenReturn(0);
        when(searchContentPageCacheService.getContentPage(
                "", 1, 10,
                List.of(), List.of(), List.of(), List.of()))
                .thenReturn(page);
        when(searchContentPageCacheService.getFirstPagePreview(
                "", 5, 10,
                List.of(), List.of(), List.of(), List.of()))
                .thenReturn(null);
        when(goodsService.getSearchProductTypes()).thenReturn(null);
        when(tmdbDAO.selectActivePlatformList()).thenReturn(null);

        ExtendedModelMap model = new ExtendedModelMap();
        String view = controller.searchResult(null, model);

        assertEquals("search/searchResult", view);
        assertEquals("", model.get("keyword"));
        assertEquals(1, model.get("contentCurrentPage"));
        assertEquals(1, model.get("goodsCurrentPage"));
        assertEquals(0, model.get("combinedTotalCount"));
        assertEquals(List.of(), model.get("contentResults"));
        assertEquals(List.of(), model.get("goodsResults"));
        assertEquals(List.of(), model.get("availableProductTypes"));
        assertEquals(Map.of(), model.get("ottLogoMap"));
        assertEquals("지금 인기 있는 콘텐츠와 상품", model.get("searchTitle"));
        assertEquals("https://image.test/", model.get("imageBaseUrl"));
    }

    @Test
    void searchShouldNormalizeFiltersSwapPricesAndClampGoodsPage() {
        SearchVO searchVO = new SearchVO();
        searchVO.setKeyword("  test  ");
        searchVO.setContentPage(0);
        searchVO.setGoodsPage(99);
        searchVO.setContentCategories(Arrays.asList(
                " movie ", "MOVIE", "invalid", null, "drama"));
        searchVO.setGenreCodes(Arrays.asList("28", " ", null, "28", "35"));
        searchVO.setProviderIds(Arrays.asList("8", "8", "337"));
        searchVO.setAgeRatings(Arrays.asList("15세 이상 관람가", " "));
        searchVO.setProductTypes(Arrays.asList("BOOK", "BOOK", " "));
        searchVO.setMinPrice(50000);
        searchVO.setMaxPrice(-100);
        searchVO.setDiscountOnly(true);
        searchVO.setInStockOnly(true);
        searchVO.setSearchTab(" goods ");

        SearchResultVO content = new SearchResultVO();
        SearchResultPageVO contentPage = page(List.of(content), 2, 13);
        GoodsVO goods = new GoodsVO();
        when(goodsService.countSearchGoods(
                "test", List.of("BOOK"), 0, 50000, true, true))
                .thenReturn(25);
        when(searchContentPageCacheService.getContentPage(
                "test", 1, 10,
                List.of("MOVIE", "DRAMA"),
                List.of("28", "35"),
                List.of("8", "337"),
                List.of("15세 이상 관람가")))
                .thenReturn(contentPage);
        when(searchContentPageCacheService.getFirstPagePreview(
                "test", 5, 10,
                List.of("MOVIE", "DRAMA"),
                List.of("28", "35"),
                List.of("8", "337"),
                List.of("15세 이상 관람가")))
                .thenReturn(null);
        when(goodsService.searchGoods(
                "test", List.of("BOOK"), 0, 50000,
                true, true, 3, 12))
                .thenReturn(List.of(goods));
        when(goodsService.searchGoods(
                "test", List.of("BOOK"), 0, 50000,
                true, true, 1, 5))
                .thenReturn(null);
        when(goodsService.getSearchProductTypes()).thenReturn(List.of("BOOK", "OST"));
        when(tmdbDAO.selectActivePlatformList()).thenReturn(Arrays.asList(
                platform("Netflix", "/netflix.png"),
                platform("Disney Plus", "/disney.png"),
                platform(null, "/missing-name.png"),
                platform("TVING", " "),
                null));

        ExtendedModelMap model = new ExtendedModelMap();
        String view = controller.searchResult(searchVO, model);

        assertEquals("search/searchResult", view);
        assertEquals("test", searchVO.getKeyword());
        assertEquals(1, searchVO.getContentPage());
        assertEquals(3, searchVO.getGoodsPage());
        assertEquals(List.of("MOVIE", "DRAMA"), searchVO.getContentCategories());
        assertEquals(List.of("28", "35"), searchVO.getGenreCodes());
        assertEquals(List.of("8", "337"), searchVO.getProviderIds());
        assertEquals(List.of("BOOK"), searchVO.getProductTypes());
        assertEquals(0, searchVO.getMinPrice());
        assertEquals(50000, searchVO.getMaxPrice());
        assertEquals("GOODS", searchVO.getSearchTab());
        assertEquals(13, model.get("contentTotalCount"));
        assertEquals(25, model.get("goodsTotalCount"));
        assertEquals(38, model.get("combinedTotalCount"));
        assertEquals(List.of(goods), model.get("goodsResults"));
        assertEquals(List.of(), model.get("allContentResults"));
        assertEquals(List.of(), model.get("allGoodsResults"));
        assertEquals("'test' 조건 검색 결과", model.get("searchTitle"));

        @SuppressWarnings("unchecked")
        Map<String, String> logoMap = (Map<String, String>) model.get("ottLogoMap");
        assertEquals("/netflix.png", logoMap.get("netflix"));
        assertEquals("/disney.png", logoMap.get("disney"));
        assertEquals(2, logoMap.size());
    }

    @Test
    void emptySearchShouldUseContentAndGoodsSpecificTitles() {
        SearchVO contentSearch = new SearchVO();
        contentSearch.setSearchTab("content");
        SearchVO goodsSearch = new SearchVO();
        goodsSearch.setSearchTab("goods");
        stubEmptySearch();

        ExtendedModelMap contentModel = new ExtendedModelMap();
        ExtendedModelMap goodsModel = new ExtendedModelMap();
        controller.searchResult(contentSearch, contentModel);
        controller.searchResult(goodsSearch, goodsModel);

        assertEquals("지금 인기 있는 콘텐츠", contentModel.get("searchTitle"));
        assertEquals("현재 판매 중인 상품", goodsModel.get("searchTitle"));
    }

    @Test
    void keywordOnlyAndFilterOnlyShouldUseDifferentTitles() {
        SearchVO keywordSearch = new SearchVO();
        keywordSearch.setKeyword("  영화  ");
        SearchVO filterSearch = new SearchVO();
        filterSearch.setGenreCodes(List.of("28"));

        stubSearch("영화", List.of());
        ExtendedModelMap keywordModel = new ExtendedModelMap();
        controller.searchResult(keywordSearch, keywordModel);
        assertEquals("'영화' 검색 결과", keywordModel.get("searchTitle"));

        stubSearch("", List.of("28"));
        ExtendedModelMap filterModel = new ExtendedModelMap();
        controller.searchResult(filterSearch, filterModel);
        assertEquals("선택 조건 검색 결과", filterModel.get("searchTitle"));
    }

    private void stubEmptySearch() {
        stubSearch("", List.of());
    }

    private void stubSearch(String keyword, List<String> genres) {
        SearchResultPageVO page = page(List.of(), 0, 0);
        when(goodsService.countSearchGoods(
                keyword, List.of(), null, null, false, false))
                .thenReturn(0);
        when(searchContentPageCacheService.getContentPage(
                keyword, 1, 10,
                List.of(), genres, List.of(), List.of()))
                .thenReturn(page);
        when(searchContentPageCacheService.getFirstPagePreview(
                keyword, 5, 10,
                List.of(), genres, List.of(), List.of()))
                .thenReturn(List.of());
        when(goodsService.getSearchProductTypes()).thenReturn(List.of());
        when(tmdbDAO.selectActivePlatformList()).thenReturn(List.of());

    }

    private SearchResultPageVO page(
            List<SearchResultVO> results,
            int totalPages,
            int totalResults) {
        SearchResultPageVO page = new SearchResultPageVO();
        page.setResultList(results);
        page.setTotalPages(totalPages);
        page.setTotalResults(totalResults);
        return page;
    }

    private OttPlatformVO platform(String name, String logo) {
        OttPlatformVO platform = new OttPlatformVO();
        platform.setPlatformName(name);
        platform.setLogoImage(logo);
        return platform;
    }
}
