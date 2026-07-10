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
     * 콘텐츠 탭에서 한 페이지에 표시할 콘텐츠 수
     */
    private static final int CONTENT_PAGE_SIZE = 10;

    /*
     * 전체 탭에서 미리 보여줄 콘텐츠 수
     */
    private static final int ALL_CONTENT_PREVIEW_SIZE = 5;

    /*
     * 전체 탭에서 미리 보여줄 상품 수
     */
    private static final int ALL_GOODS_PREVIEW_SIZE = 5;

    /*
     * 상품 검색 시 조회할 최대 상품 수
     */
    private static final int GOODS_SEARCH_SIZE = 20;

    /*
     * TMDB API 최대 접근 페이지
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

        int displayPage =
                normalizePage(
                        searchVO.getPage()
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

        searchVO.setKeyword(keyword);
        searchVO.setPage(displayPage);
        searchVO.setContentTypes(contentTypes);
        searchVO.setGenreCodes(genreCodes);
        searchVO.setProviderIds(providerIds);
        searchVO.setSearchTab(searchTab);

        /*
         * =====================================================
         * 콘텐츠 탭용 현재 페이지 결과
         * =====================================================
         */
        SearchResultPageVO contentPageVO =
                collectContentPage(
                        keyword,
                        displayPage,
                        contentTypes,
                        genreCodes,
                        providerIds
                );

        List<SearchResultVO> contentResults =
                contentPageVO.getResultList();

        if (contentResults == null) {
            contentResults =
                    new ArrayList<SearchResultVO>();
        }

        /*
         * =====================================================
         * 전체 탭용 콘텐츠 미리보기
         *
         * 현재 콘텐츠 탭 페이지와 상관없이
         * 항상 1페이지 상위 5개를 사용한다.
         * =====================================================
         */
        SearchResultPageVO firstContentPageVO;

        if (displayPage == 1) {
            firstContentPageVO = contentPageVO;
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

        List<SearchResultVO> firstContentResultList =
                firstContentPageVO.getResultList();

        List<SearchResultVO> allContentResults =
                createContentPreviewList(
                        firstContentResultList,
                        ALL_CONTENT_PREVIEW_SIZE
                );

        /*
         * =====================================================
         * 상품 검색
         * =====================================================
         */
        List<GoodsVO> goodsResults =
                new ArrayList<GoodsVO>();

        int goodsTotalCount = 0;

        if (keyword != null
                && !keyword.isEmpty()) {

            List<GoodsVO> searchedGoodsList =
                    goodsService.searchGoods(
                            keyword,
                            1,
                            GOODS_SEARCH_SIZE
                    );

            if (searchedGoodsList != null) {
                goodsResults.addAll(
                        searchedGoodsList
                );
            }

            goodsTotalCount =
                    goodsService.countSearchGoods(
                            keyword
                    );
        }

        /*
         * 전체 탭용 상품 상위 5개
         */
        List<GoodsVO> allGoodsResults =
                createGoodsPreviewList(
                        goodsResults,
                        ALL_GOODS_PREVIEW_SIZE
                );

        int contentTotalCount =
                contentPageVO.getTotalResults();

        int combinedTotalCount =
                contentTotalCount
                        + goodsTotalCount;

        /*
         * =====================================================
         * 기존 화면 호환 모델
         * =====================================================
         */
        model.addAttribute(
                "searchResults",
                contentResults
        );

        model.addAttribute(
                "resultList",
                contentResults
        );

        model.addAttribute(
                "totalResults",
                contentTotalCount
        );

        /*
         * =====================================================
         * 통합검색 결과 모델
         * =====================================================
         */
        model.addAttribute(
                "contentResults",
                contentResults
        );

        model.addAttribute(
                "goodsResults",
                goodsResults
        );

        /*
         * 전체 탭 전용 결과
         */
        model.addAttribute(
                "allContentResults",
                allContentResults
        );

        model.addAttribute(
                "allGoodsResults",
                allGoodsResults
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

        /*
         * =====================================================
         * 페이지 정보
         * =====================================================
         */
        model.addAttribute(
                "pageVO",
                contentPageVO
        );

        model.addAttribute(
                "currentPage",
                displayPage
        );

        model.addAttribute(
                "totalPages",
                contentPageVO.getTotalPages()
        );

        /*
         * =====================================================
         * 검색 조건
         * =====================================================
         */
        model.addAttribute(
                "searchVO",
                searchVO
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
     * 현재 화면 페이지에 필요한 콘텐츠를 수집한다.
     *
     * 지원 OTT 콘텐츠만 남기기 때문에
     * 화면에 필요한 개수가 모일 때까지 TMDB 페이지를 순차 조회한다.
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

        /*
         * 다음 페이지 존재 여부를 확인하기 위해
         * 한 개를 추가로 수집한다.
         */
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

            /*
             * 첫 번째 응답의 전체 페이지와 전체 결과 수를 보관한다.
             */
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

        /*
         * TMDB가 제공한 전체 페이지 수를
         * 페이지네이션의 마지막 페이지로 사용한다.
         *
         * 지원 OTT 필터 전 전체 페이지이므로
         * 뒤쪽 페이지에서 결과가 적거나 없을 수 있다.
         */
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

    /**
     * 검색어 유무에 따라 콘텐츠 검색 메서드를 호출한다.
     */
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

    /**
     * 전체 탭에서 사용할 콘텐츠 상위 결과를 만든다.
     */
    private List<SearchResultVO> createContentPreviewList(
            List<SearchResultVO> sourceList,
            int limit) {

        List<SearchResultVO> previewList =
                new ArrayList<SearchResultVO>();

        if (sourceList == null
                || sourceList.isEmpty()
                || limit <= 0) {

            return previewList;
        }

        int endIndex =
                Math.min(
                        limit,
                        sourceList.size()
                );

        previewList.addAll(
                sourceList.subList(
                        0,
                        endIndex
                )
        );

        return previewList;
    }

    /**
     * 전체 탭에서 사용할 상품 상위 결과를 만든다.
     */
    private List<GoodsVO> createGoodsPreviewList(
            List<GoodsVO> sourceList,
            int limit) {

        List<GoodsVO> previewList =
                new ArrayList<GoodsVO>();

        if (sourceList == null
                || sourceList.isEmpty()
                || limit <= 0) {

            return previewList;
        }

        int endIndex =
                Math.min(
                        limit,
                        sourceList.size()
                );

        previewList.addAll(
                sourceList.subList(
                        0,
                        endIndex
                )
        );

        return previewList;
    }

    /**
     * TMDB ID와 콘텐츠 타입이 같은 결과는 중복으로 추가하지 않는다.
     */
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

            boolean duplicated = false;

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

                    duplicated = true;

                    /*
                     * 같은 콘텐츠가 제목 검색과 인물 검색에
                     * 모두 포함된 경우 인물 검색 정보를 유지한다.
                     */
                    if ("PERSON".equals(
                            sourceVO.getMatchType()
                    )) {

                        targetVO.setMatchType(
                                sourceVO.getMatchType()
                        );

                        targetVO.setMatchedPersonName(
                                sourceVO.getMatchedPersonName()
                        );

                        targetVO.setMatchedPersonRole(
                                sourceVO.getMatchedPersonRole()
                        );
                    }

                    break;
                }
            }

            if (!duplicated) {
                targetList.add(sourceVO);
            }
        }
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