package com.project.oditji.search.controller;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

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
import com.project.oditji.tmdb.dao.TmdbDAO;
import com.project.oditji.tmdb.vo.OttPlatformVO;

@Controller
public class SearchController {

    private static final int CONTENT_PAGE_SIZE = 10;
    private static final int GOODS_PAGE_SIZE = 12;
    private static final int ALL_CONTENT_PREVIEW_SIZE = 5;
    private static final int ALL_GOODS_PREVIEW_SIZE = 5;

    private final SearchContentPageCacheService searchContentPageCacheService;
    private final GoodsService goodsService;
    private final TmdbDAO tmdbDAO;

    @Value("${tmdb.api.image-base-url}")
    private String imageBaseUrl;

    public SearchController(
            SearchContentPageCacheService searchContentPageCacheService,
            GoodsService goodsService,
            TmdbDAO tmdbDAO) {

        this.searchContentPageCacheService =
                searchContentPageCacheService;

        this.goodsService =
                goodsService;

        this.tmdbDAO =
                tmdbDAO;
    }

    @GetMapping("/search")
    public String searchResult(
            SearchVO searchVO,
            Model model) {

        if (searchVO == null) {
            searchVO = new SearchVO();
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

        List<String> contentCategories =
                normalizeContentCategories(
                        searchVO.getContentCategories()
                );

        List<String> genreCodes =
                createSafeList(
                        searchVO.getGenreCodes()
                );

        List<String> providerIds =
                createSafeList(
                        searchVO.getProviderIds()
                );

        List<String> productTypes =
                createSafeList(
                        searchVO.getProductTypes()
                );

        Integer minPrice =
                normalizePrice(
                        searchVO.getMinPrice()
                );

        Integer maxPrice =
                normalizePrice(
                        searchVO.getMaxPrice()
                );

        if (minPrice != null
                && maxPrice != null
                && minPrice > maxPrice) {

            int temporaryPrice =
                    minPrice;

            minPrice =
                    maxPrice;

            maxPrice =
                    temporaryPrice;
        }

        boolean discountOnly =
                searchVO.isDiscountOnly();

        boolean inStockOnly =
                searchVO.isInStockOnly();

        String searchTab =
                normalizeSearchTab(
                        searchVO.getSearchTab()
                );

        int goodsTotalCount = 0;
        int goodsTotalPages = 0;

        if (!keyword.isEmpty()) {

            goodsTotalCount =
                    goodsService.countSearchGoods(
                            keyword,
                            productTypes,
                            minPrice,
                            maxPrice,
                            discountOnly,
                            inStockOnly
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

        searchVO.setKeyword(
                keyword
        );

        searchVO.setContentPage(
                contentPage
        );

        searchVO.setGoodsPage(
                goodsPage
        );

        searchVO.setContentCategories(
                contentCategories
        );

        searchVO.setGenreCodes(
                genreCodes
        );

        searchVO.setProviderIds(
                providerIds
        );

        searchVO.setProductTypes(
                productTypes
        );

        searchVO.setMinPrice(
                minPrice
        );

        searchVO.setMaxPrice(
                maxPrice
        );

        searchVO.setDiscountOnly(
                discountOnly
        );

        searchVO.setInStockOnly(
                inStockOnly
        );

        searchVO.setSearchTab(
                searchTab
        );

        SearchResultPageVO contentPageVO =
                searchContentPageCacheService.getContentPage(
                        keyword,
                        contentPage,
                        CONTENT_PAGE_SIZE,
                        contentCategories,
                        genreCodes,
                        providerIds
                );

        List<SearchResultVO> contentResults =
                contentPageVO.getResultList() == null
                        ? new ArrayList<SearchResultVO>()
                        : contentPageVO.getResultList();

        List<SearchResultVO> allContentResults =
                searchContentPageCacheService.getFirstPagePreview(
                        keyword,
                        ALL_CONTENT_PREVIEW_SIZE,
                        CONTENT_PAGE_SIZE,
                        contentCategories,
                        genreCodes,
                        providerIds
                );

        List<GoodsVO> goodsResults =
                new ArrayList<GoodsVO>();

        if (!keyword.isEmpty()
                && goodsTotalCount > 0) {

            goodsResults =
                    goodsService.searchGoods(
                            keyword,
                            productTypes,
                            minPrice,
                            maxPrice,
                            discountOnly,
                            inStockOnly,
                            goodsPage,
                            GOODS_PAGE_SIZE
                    );
        }

        if (goodsResults == null) {
            goodsResults =
                    new ArrayList<GoodsVO>();
        }

        List<GoodsVO> allGoodsResults =
                new ArrayList<GoodsVO>();

        if (!keyword.isEmpty()
                && goodsTotalCount > 0) {

            allGoodsResults =
                    goodsService.searchGoods(
                            keyword,
                            productTypes,
                            minPrice,
                            maxPrice,
                            discountOnly,
                            inStockOnly,
                            1,
                            ALL_GOODS_PREVIEW_SIZE
                    );
        }

        if (allGoodsResults == null) {
            allGoodsResults =
                    new ArrayList<GoodsVO>();
        }

        List<String> availableProductTypes =
                goodsService.getSearchProductTypes();

        int contentTotalCount =
                contentPageVO.getTotalResults();

        int combinedTotalCount =
                contentTotalCount
                        + goodsTotalCount;

        /*
         * OTT_PLATFORM 테이블의 활성 플랫폼 로고를
         * 사이드바에서 사용하기 위한 Map으로 변환한다.
         *
         * 키 예:
         * netflix, tving, wavve, disney, watcha, coupang
         */
        Map<String, String> ottLogoMap =
                createOttLogoMap(
                        tmdbDAO.selectActivePlatformList()
                );

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
                "contentCategories",
                contentCategories
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
                "productTypes",
                productTypes
        );

        model.addAttribute(
                "availableProductTypes",
                availableProductTypes
        );

        model.addAttribute(
                "minPrice",
                minPrice
        );

        model.addAttribute(
                "maxPrice",
                maxPrice
        );

        model.addAttribute(
                "discountOnly",
                discountOnly
        );

        model.addAttribute(
                "inStockOnly",
                inStockOnly
        );

        model.addAttribute(
                "searchTab",
                searchTab
        );

        model.addAttribute(
                "searchTitle",
                makeSearchTitle(searchVO)
        );

        model.addAttribute(
                "imageBaseUrl",
                imageBaseUrl
        );

        model.addAttribute(
                "ottLogoMap",
                ottLogoMap
        );

        return "search/searchResult";
    }

    private Map<String, String> createOttLogoMap(
            List<OttPlatformVO> platformList) {

        Map<String, String> logoMap =
                new LinkedHashMap<String, String>();

        if (platformList == null) {
            return logoMap;
        }

        for (OttPlatformVO platform : platformList) {

            if (platform == null
                    || platform.getPlatformName() == null
                    || platform.getLogoImage() == null
                    || platform.getLogoImage().isBlank()) {
                continue;
            }

            String platformKey =
                    normalizePlatformName(
                            platform.getPlatformName()
                    );

            if (!platformKey.isEmpty()) {

                logoMap.put(
                        platformKey,
                        platform.getLogoImage()
                );
            }
        }

        return logoMap;
    }

    private String normalizePlatformName(
            String platformName) {

        if (platformName == null) {
            return "";
        }

        String normalized =
                platformName
                        .trim()
                        .toLowerCase(Locale.ROOT)
                        .replaceAll(
                                "[^a-z0-9]",
                                ""
                        );

        if (normalized.contains("netflix")) {
            return "netflix";
        }

        if (normalized.contains("tving")) {
            return "tving";
        }

        if (normalized.contains("wavve")) {
            return "wavve";
        }

        if (normalized.contains("disney")) {
            return "disney";
        }

        if (normalized.contains("watcha")) {
            return "watcha";
        }

        if (normalized.contains("coupang")) {
            return "coupang";
        }

        return normalized;
    }

    private List<String> normalizeContentCategories(
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

            String normalized =
                    value.trim().toUpperCase();

            if (("MOVIE".equals(normalized)
                    || "DRAMA".equals(normalized)
                    || "ANIMATION".equals(normalized)
                    || "VARIETY".equals(normalized)
                    || "DOCUMENTARY".equals(normalized))
                    && !safeList.contains(normalized)) {

                safeList.add(
                        normalized
                );
            }
        }

        return safeList;
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

            if (!normalizedValue.isEmpty()
                    && !safeList.contains(
                            normalizedValue
                    )) {

                safeList.add(
                        normalizedValue
                );
            }
        }

        return safeList;
    }

    private Integer normalizePrice(
            Integer price) {

        if (price == null) {
            return null;
        }

        return Math.max(
                price,
                0
        );
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

        return keyword == null
                ? ""
                : keyword.trim();
    }

    private String normalizeSearchTab(
            String searchTab) {

        if (searchTab == null) {
            return "ALL";
        }

        String normalized =
                searchTab
                        .trim()
                        .toUpperCase();

        if ("CONTENT".equals(normalized)) {
            return "CONTENT";
        }

        if ("GOODS".equals(normalized)) {
            return "GOODS";
        }

        return "ALL";
    }

    private String makeSearchTitle(
            SearchVO searchVO) {

        boolean hasKeyword =
                searchVO != null
                        && searchVO.hasKeyword();

        boolean hasFilter =
                searchVO != null
                        && searchVO.hasFilter();

        if (hasKeyword && hasFilter) {

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