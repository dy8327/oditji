package com.project.oditji.search.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.project.oditji.goods.service.GoodsService;
import com.project.oditji.goods.vo.GoodsVO;
import com.project.oditji.search.service.SearchService;
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

    /*
     * TMDB 최대 접근 페이지
     */
    private static final int MAX_TMDB_PAGE = 500;

    private final SearchService searchService;
    private final GoodsService goodsService;

    @Value("${tmdb.api.image-base-url}")
    private String imageBaseUrl;

    public SearchController(
            SearchService searchService,
            GoodsService goodsService) {

        this.searchService = searchService;
        this.goodsService = goodsService;
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

                goodsPage = goodsTotalPages;
            }
        }

        /*
         * 정규화된 검색 조건을 SearchVO에 다시 설정한다.
         */
        searchVO.setKeyword(keyword);
        searchVO.setContentPage(contentPage);
        searchVO.setGoodsPage(goodsPage);
        searchVO.setContentTypes(contentTypes);
        searchVO.setGenreCodes(genreCodes);
        searchVO.setProviderIds(providerIds);
        searchVO.setSearchTab(searchTab);

        /*
         * =====================================================
         * 콘텐츠 탭 현재 페이지
         * =====================================================
         */
        SearchResultPageVO contentPageVO =
                collectContentPage(
                        keyword,
                        contentPage,
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
         * 콘텐츠 탭이 몇 페이지이든 항상 1페이지 상위 5개
         * =====================================================
         */
        SearchResultPageVO firstContentPageVO;

        if (contentPage == 1) {

            firstContentPageVO =
                    contentPageVO;

        } else {

            firstContentPageVO =
                    collectContentPage(
                            keyword,
                            1,
                            contentTypes,
                            genreCodes,
                            providerIds
                    );
        }

        List<SearchResultVO> allContentResults =
                limitContentList(
                        firstContentPageVO.getResultList(),
                        ALL_CONTENT_PREVIEW_SIZE
                );

        /*
         * =====================================================
         * 상품 탭 현재 페이지
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
         * 상품 탭이 몇 페이지이든 항상 1페이지 상위 5개
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
         * 모델 데이터
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
                makeSearchTitle(searchVO)
        );

        model.addAttribute(
                "imageBaseUrl",
                imageBaseUrl
        );

        return "search/searchResult";
    }

    /**
     * 콘텐츠 페이지에 필요한 결과를 수집한다.
     */
    private SearchResultPageVO collectContentPage(
            String keyword,
            int displayPage,
            List<String> contentTypes,
            List<String> genreCodes,
            List<String> providerIds) {

        int requiredResultCount =
                displayPage
                        * CONTENT_PAGE_SIZE;

        int collectionTarget =
                requiredResultCount + 1;

        List<SearchResultVO> collectedResultList =
                new ArrayList<SearchResultVO>();

        int tmdbPage = 1;
        int sourceTotalPages = 0;
        int sourceTotalResults = 0;

        while (tmdbPage <= MAX_TMDB_PAGE
                && collectedResultList.size()
                        < collectionTarget) {

            SearchResultPageVO partialPage =
                    requestSearchPage(
                            keyword,
                            tmdbPage,
                            contentTypes,
                            genreCodes,
                            providerIds
                    );

            if (partialPage == null) {
                break;
            }

            if (tmdbPage == 1) {

                sourceTotalPages =
                        Math.min(
                                partialPage.getTotalPages(),
                                MAX_TMDB_PAGE
                        );

                sourceTotalResults =
                        partialPage.getTotalResults();
            }

            addUniqueResults(
                    collectedResultList,
                    partialPage.getResultList()
            );

            if (partialPage.getTotalPages()
                    <= tmdbPage) {

                break;
            }

            tmdbPage++;
        }

        int startIndex =
                (displayPage - 1)
                        * CONTENT_PAGE_SIZE;

        int endIndex =
                Math.min(
                        startIndex
                                + CONTENT_PAGE_SIZE,
                        collectedResultList.size()
                );

        List<SearchResultVO> displayResultList =
                new ArrayList<SearchResultVO>();

        if (startIndex
                < collectedResultList.size()) {

            displayResultList.addAll(
                    collectedResultList.subList(
                            startIndex,
                            endIndex
                    )
            );
        }

        int totalPages =
                sourceTotalPages;

        if (displayPage == 1
                && displayResultList.isEmpty()) {

            totalPages = 0;
        }

        SearchResultPageVO pageVO =
                new SearchResultPageVO();

        pageVO.setResultList(
                displayResultList
        );

        pageVO.setPage(
                displayPage
        );

        pageVO.setTotalPages(
                totalPages
        );

        pageVO.setTotalResults(
                sourceTotalResults
        );

        return pageVO;
    }

    private SearchResultPageVO requestSearchPage(
            String keyword,
            int tmdbPage,
            List<String> contentTypes,
            List<String> genreCodes,
            List<String> providerIds) {

        if (keyword == null
                || keyword.isEmpty()) {

            return searchService.getPopularContent(
                    tmdbPage,
                    contentTypes,
                    genreCodes,
                    providerIds
            );
        }

        return searchService.searchByTmdb(
                keyword,
                tmdbPage,
                contentTypes,
                genreCodes,
                providerIds
        );
    }

    private void addUniqueResults(
            List<SearchResultVO> targetList,
            List<SearchResultVO> sourceList) {

        if (sourceList == null) {
            return;
        }

        for (SearchResultVO sourceVO
                : sourceList) {

            if (sourceVO == null
                    || sourceVO.getTmdbId() == null
                    || sourceVO.getContentType() == null) {

                continue;
            }

            SearchResultVO duplicatedVO =
                    findDuplicatedResult(
                            targetList,
                            sourceVO
                    );

            if (duplicatedVO == null) {

                targetList.add(sourceVO);
                continue;
            }

            /*
             * 동일 콘텐츠가 제목 검색과 인물 검색에 함께 포함되면
             * 배우/감독 검색 정보를 보존한다.
             */
            if ("PERSON".equals(
                    sourceVO.getMatchType()
            )) {

                duplicatedVO.setMatchType(
                        sourceVO.getMatchType()
                );

                duplicatedVO.setMatchedPersonName(
                        sourceVO.getMatchedPersonName()
                );

                duplicatedVO.setMatchedPersonRole(
                        sourceVO.getMatchedPersonRole()
                );
            }
        }
    }

    private SearchResultVO findDuplicatedResult(
            List<SearchResultVO> targetList,
            SearchResultVO sourceVO) {

        for (SearchResultVO targetVO
                : targetList) {

            if (targetVO == null
                    || targetVO.getTmdbId() == null
                    || targetVO.getContentType() == null) {

                continue;
            }

            boolean sameTmdbId =
                    sourceVO.getTmdbId().equals(
                            targetVO.getTmdbId()
                    );

            boolean sameContentType =
                    sourceVO.getContentType()
                            .equalsIgnoreCase(
                                    targetVO.getContentType()
                            );

            if (sameTmdbId
                    && sameContentType) {

                return targetVO;
            }
        }

        return null;
    }

    private List<SearchResultVO> createSafeContentList(
            List<SearchResultVO> sourceList) {

        if (sourceList == null) {
            return new ArrayList<SearchResultVO>();
        }

        return sourceList;
    }

    private List<SearchResultVO> limitContentList(
            List<SearchResultVO> sourceList,
            int limit) {

        List<SearchResultVO> resultList =
                new ArrayList<SearchResultVO>();

        if (sourceList == null
                || sourceList.isEmpty()
                || limit <= 0) {

            return resultList;
        }

        int endIndex =
                Math.min(
                        sourceList.size(),
                        limit
                );

        resultList.addAll(
                sourceList.subList(
                        0,
                        endIndex
                )
        );

        return resultList;
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

        if ("CONTENT".equals(normalizedTab)) {
            return "CONTENT";
        }

        if ("GOODS".equals(normalizedTab)) {
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