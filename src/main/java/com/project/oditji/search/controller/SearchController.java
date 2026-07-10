package com.project.oditji.search.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.project.oditji.goods.service.GoodsService;
import com.project.oditji.goods.vo.GoodsVO;
import com.project.oditji.search.service.SearchContentPageCacheService;
import com.project.oditji.search.vo.SearchResultPageVO;
import com.project.oditji.search.vo.SearchResultVO;
import com.project.oditji.search.vo.SearchVO;

@Controller
public class SearchController {

    /*
     * 콘텐츠 탭 페이지당 개수
     */
    private static final int CONTENT_PAGE_SIZE = 10;

    /*
     * 상품 탭 페이지당 개수
     */
    private static final int GOODS_PAGE_SIZE = 12;

    /*
     * 전체 탭 콘텐츠 미리보기 개수
     */
    private static final int ALL_CONTENT_PREVIEW_SIZE = 5;

    /*
     * 전체 탭 상품 미리보기 개수
     */
    private static final int ALL_GOODS_PREVIEW_SIZE = 5;

    private final SearchContentPageCacheService
            searchContentPageCacheService;

    private final GoodsService goodsService;

    @Value("${tmdb.api.image-base-url}")
    private String imageBaseUrl;

    public SearchController(
            SearchContentPageCacheService
                    searchContentPageCacheService,
            GoodsService goodsService) {

        this.searchContentPageCacheService =
                searchContentPageCacheService;

        this.goodsService =
                goodsService;
    }

    @GetMapping("/search")
    public String searchResult(
            SearchVO searchVO,
            Model model) {

        if (searchVO == null) {

            searchVO =
                    new SearchVO();
        }

        String keyword =
                normalizeKeyword(
                        searchVO.getKeyword()
                );

        int contentPage =
                normalizePage(
                        searchVO.getContentPage()
                );

        int goodsPage =
                normalizePage(
                        searchVO.getGoodsPage()
                );

        List<String> contentTypes =
                createSafeList(
                        searchVO.getContentTypes()
                );

        List<String> genreCodes =
                createSafeList(
                        searchVO.getGenreCodes()
                );

        List<String> providerIds =
                createSafeList(
                        searchVO.getProviderIds()
                );

        String searchTab =
                normalizeSearchTab(
                        searchVO.getSearchTab()
                );

        /*
         * =====================================================
         * 상품 전체 개수 및 상품 페이지 보정
         * =====================================================
         */
        int goodsTotalCount = 0;
        int goodsTotalPages = 0;

        if (!keyword.isEmpty()) {

            goodsTotalCount =
                    goodsService.countSearchGoods(
                            keyword
                    );

            goodsTotalPages =
                    calculateTotalPages(
                            goodsTotalCount,
                            GOODS_PAGE_SIZE
                    );

            if (goodsTotalPages > 0
                    && goodsPage > goodsTotalPages) {

                goodsPage =
                        goodsTotalPages;
            }
        }

        /*
         * 정규화한 검색 조건을 다시 저장한다.
         */
        searchVO.setKeyword(
                keyword
        );

        searchVO.setContentPage(
                contentPage
        );

        searchVO.setGoodsPage(
                goodsPage
        );

        searchVO.setContentTypes(
                contentTypes
        );

        searchVO.setGenreCodes(
                genreCodes
        );

        searchVO.setProviderIds(
                providerIds
        );

        searchVO.setSearchTab(
                searchTab
        );

        /*
         * =====================================================
         * 콘텐츠 현재 페이지
         *
         * 동일 검색 조건이면 기존 누적 결과를 사용하고
         * 필요한 경우 마지막 TMDB 페이지 다음부터 이어서 수집한다.
         * =====================================================
         */
        SearchResultPageVO contentPageVO =
                searchContentPageCacheService
                        .getContentPage(
                                keyword,
                                contentPage,
                                CONTENT_PAGE_SIZE,
                                contentTypes,
                                genreCodes,
                                providerIds
                        );

        List<SearchResultVO> contentResults =
                createSafeContentList(
                        contentPageVO.getResultList()
                );

        /*
         * =====================================================
         * 전체 탭 콘텐츠
         *
         * 항상 검색 결과 1페이지 상위 5개를 사용한다.
         * 별도 TMDB 재검색 없이 동일 누적 캐시를 사용한다.
         * =====================================================
         */
        List<SearchResultVO> allContentResults =
                searchContentPageCacheService
                        .getFirstPagePreview(
                                keyword,
                                ALL_CONTENT_PREVIEW_SIZE,
                                CONTENT_PAGE_SIZE,
                                contentTypes,
                                genreCodes,
                                providerIds
                        );

        /*
         * =====================================================
         * 상품 현재 페이지
         * =====================================================
         */
        List<GoodsVO> goodsResults =
                new ArrayList<GoodsVO>();

        if (!keyword.isEmpty()
                && goodsTotalCount > 0) {

            goodsResults =
                    goodsService.searchGoods(
                            keyword,
                            goodsPage,
                            GOODS_PAGE_SIZE
                    );
        }

        if (goodsResults == null) {

            goodsResults =
                    new ArrayList<GoodsVO>();
        }

        /*
         * =====================================================
         * 전체 탭 상품
         *
         * 항상 상품 1페이지 상위 5개
         * =====================================================
         */
        List<GoodsVO> allGoodsResults =
                new ArrayList<GoodsVO>();

        if (!keyword.isEmpty()
                && goodsTotalCount > 0) {

            allGoodsResults =
                    goodsService.searchGoods(
                            keyword,
                            1,
                            ALL_GOODS_PREVIEW_SIZE
                    );
        }

        if (allGoodsResults == null) {

            allGoodsResults =
                    new ArrayList<GoodsVO>();
        }

        int contentTotalCount =
                contentPageVO.getTotalResults();

        int combinedTotalCount =
                contentTotalCount
                        + goodsTotalCount;

        /*
         * =====================================================
         * 화면 모델 데이터
         * =====================================================
         */
        model.addAttribute(
                "searchVO",
                searchVO
        );

        model.addAttribute(
                "searchResults",
                contentResults
        );

        model.addAttribute(
                "resultList",
                contentResults
        );

        model.addAttribute(
                "contentResults",
                contentResults
        );

        model.addAttribute(
                "goodsResults",
                goodsResults
        );

        model.addAttribute(
                "allContentResults",
                allContentResults
        );

        model.addAttribute(
                "allGoodsResults",
                allGoodsResults
        );

        model.addAttribute(
                "contentPageVO",
                contentPageVO
        );

        /*
         * 기존 JSP 호환용
         */
        model.addAttribute(
                "pageVO",
                contentPageVO
        );

        model.addAttribute(
                "contentCurrentPage",
                contentPage
        );

        model.addAttribute(
                "goodsCurrentPage",
                goodsPage
        );

        model.addAttribute(
                "currentPage",
                contentPage
        );

        model.addAttribute(
                "contentTotalPages",
                contentPageVO.getTotalPages()
        );

        model.addAttribute(
                "goodsTotalPages",
                goodsTotalPages
        );

        model.addAttribute(
                "totalPages",
                contentPageVO.getTotalPages()
        );

        model.addAttribute(
                "contentTotalCount",
                contentTotalCount
        );

        model.addAttribute(
                "goodsTotalCount",
                goodsTotalCount
        );

        model.addAttribute(
                "combinedTotalCount",
                combinedTotalCount
        );

        model.addAttribute(
                "totalResults",
                contentTotalCount
        );

        model.addAttribute(
                "keyword",
                keyword
        );

        model.addAttribute(
                "contentTypes",
                contentTypes
        );

        model.addAttribute(
                "genreCodes",
                genreCodes
        );

        model.addAttribute(
                "providerIds",
                providerIds
        );

        model.addAttribute(
                "searchTab",
                searchTab
        );

        model.addAttribute(
                "searchTitle",
                makeSearchTitle(
                        searchVO
                )
        );

        model.addAttribute(
                "imageBaseUrl",
                imageBaseUrl
        );

        return "search/searchResult";
    }

    private List<SearchResultVO> createSafeContentList(
            List<SearchResultVO> sourceList) {

        if (sourceList == null) {

            return new ArrayList<SearchResultVO>();
        }

        return sourceList;
    }

    private int calculateTotalPages(
            int totalCount,
            int pageSize) {

        if (totalCount <= 0
                || pageSize <= 0) {

            return 0;
        }

        return (totalCount + pageSize - 1)
                / pageSize;
    }

    private int normalizePage(
            int page) {

        return page <= 0
                ? 1
                : page;
    }

    private String normalizeKeyword(
            String keyword) {

        if (keyword == null) {

            return "";
        }

        return keyword.trim();
    }

    private String normalizeSearchTab(
            String searchTab) {

        if (searchTab == null) {

            return "ALL";
        }

        String normalizedTab =
                searchTab.trim()
                        .toUpperCase();

        if ("CONTENT".equals(
                normalizedTab
        )) {

            return "CONTENT";
        }

        if ("GOODS".equals(
                normalizedTab
        )) {

            return "GOODS";
        }

        return "ALL";
    }

    private List<String> createSafeList(
            List<String> sourceList) {

        List<String> safeList =
                new ArrayList<String>();

        if (sourceList == null) {

            return safeList;
        }

        for (String value : sourceList) {

            if (value == null) {
                continue;
            }

            String normalizedValue =
                    value.trim();

            if (normalizedValue.isEmpty()) {
                continue;
            }

            if (!safeList.contains(
                    normalizedValue
            )) {

                safeList.add(
                        normalizedValue
                );
            }
        }

        return safeList;
    }

    private String makeSearchTitle(
            SearchVO searchVO) {

        boolean hasKeyword =
                searchVO != null
                        && searchVO.hasKeyword();

        boolean hasFilter =
                searchVO != null
                        && searchVO.hasFilter();

        if (hasKeyword
                && hasFilter) {

            return "'"
                    + searchVO.getKeyword()
                    + "' 조건 검색 결과";
        }

        if (hasKeyword) {

            return "'"
                    + searchVO.getKeyword()
                    + "' 검색 결과";
        }

        if (hasFilter) {

            return "선택 조건 검색 결과";
        }

        return "지금 인기 있는 콘텐츠";
    }
}