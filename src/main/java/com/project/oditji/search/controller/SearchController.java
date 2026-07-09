package com.project.oditji.search.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.project.oditji.search.service.SearchService;
import com.project.oditji.search.vo.SearchResultPageVO;
import com.project.oditji.search.vo.SearchResultVO;
import com.project.oditji.search.vo.SearchVO;

@Controller
public class SearchController {

    private static final int DISPLAY_PAGE_SIZE = 10;
    private static final int TMDB_PAGE_SIZE = 20;
    private static final int MAX_TMDB_PAGE = 500;

    private final SearchService searchService;

    @Value("${tmdb.api.image-base-url}")
    private String imageBaseUrl;

    public SearchController(SearchService searchService) {
        this.searchService = searchService;
    }

    @GetMapping("/search")
    public String searchResult(
            SearchVO searchVO,
            Model model) {

        if (searchVO == null) {
            searchVO = new SearchVO();
        }

        String keyword = normalizeKeyword(
                searchVO.getKeyword()
        );

        searchVO.setKeyword(keyword);

        int displayPage = normalizePage(
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

        searchVO.setContentTypes(contentTypes);
        searchVO.setGenreCodes(genreCodes);
        searchVO.setProviderIds(providerIds);
        searchVO.setPage(displayPage);

        /*
         * 화면은 10개 단위이고 TMDB는 20개 단위이므로
         * 화면 페이지를 TMDB 페이지로 변환한다.
         *
         * 화면 1, 2페이지 -> TMDB 1페이지
         * 화면 3, 4페이지 -> TMDB 2페이지
         */
        int tmdbPage =
                ((displayPage - 1) / 2) + 1;

        if (tmdbPage > MAX_TMDB_PAGE) {
            tmdbPage = MAX_TMDB_PAGE;
        }

        SearchResultPageVO tmdbResultPage;

        if (keyword.isEmpty()) {

            tmdbResultPage =
                    searchService.getPopularContent(
                            tmdbPage,
                            contentTypes,
                            genreCodes,
                            providerIds
                    );

        } else {

            tmdbResultPage =
                    searchService.searchByTmdb(
                            keyword,
                            tmdbPage,
                            contentTypes,
                            genreCodes,
                            providerIds
                    );
        }

        List<SearchResultVO> displayResultList =
                createDisplayResultList(
                        tmdbResultPage.getResultList(),
                        displayPage
                );

        int totalResults =
                tmdbResultPage.getTotalResults();

        /*
         * TMDB는 최대 500페이지까지 접근하도록 제한한다.
         * 20개 × 500페이지 = 최대 10,000개
         */
        int maximumAccessibleResults =
                TMDB_PAGE_SIZE * MAX_TMDB_PAGE;

        if (totalResults > maximumAccessibleResults) {
            totalResults = maximumAccessibleResults;
        }

        int totalPages =
                calculateTotalPages(totalResults);

        SearchResultPageVO displayPageVO =
                new SearchResultPageVO();

        displayPageVO.setResultList(
                displayResultList
        );

        displayPageVO.setPage(
                displayPage
        );

        displayPageVO.setTotalPages(
                totalPages
        );

        displayPageVO.setTotalResults(
                totalResults
        );

        model.addAttribute(
                "searchVO",
                searchVO
        );

        model.addAttribute(
                "searchResults",
                displayResultList
        );

        model.addAttribute(
                "resultList",
                displayResultList
        );

        model.addAttribute(
                "pageVO",
                displayPageVO
        );

        model.addAttribute(
                "currentPage",
                displayPage
        );

        model.addAttribute(
                "totalPages",
                totalPages
        );

        model.addAttribute(
                "totalResults",
                totalResults
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
                "searchTitle",
                makeSearchTitle(searchVO)
        );

        model.addAttribute(
                "imageBaseUrl",
                imageBaseUrl
        );

        return "search/searchResult";
    }

    private List<SearchResultVO> createDisplayResultList(
            List<SearchResultVO> sourceList,
            int displayPage) {

        List<SearchResultVO> displayList =
                new ArrayList<SearchResultVO>();

        if (sourceList == null
                || sourceList.isEmpty()) {

            return displayList;
        }

        /*
         * 홀수 화면 페이지:
         * TMDB 결과의 0~9번째
         *
         * 짝수 화면 페이지:
         * TMDB 결과의 10~19번째
         */
        int startIndex =
                displayPage % 2 == 1
                        ? 0
                        : DISPLAY_PAGE_SIZE;

        if (startIndex >= sourceList.size()) {
            return displayList;
        }

        int endIndex =
                Math.min(
                        startIndex + DISPLAY_PAGE_SIZE,
                        sourceList.size()
                );

        displayList.addAll(
                sourceList.subList(
                        startIndex,
                        endIndex
                )
        );

        return displayList;
    }

    private int calculateTotalPages(
            int totalResults) {

        if (totalResults <= 0) {
            return 0;
        }

        return (int) Math.ceil(
                (double) totalResults
                        / DISPLAY_PAGE_SIZE
        );
    }

    private int normalizePage(
            int page) {

        return page <= 0 ? 1 : page;
    }

    private String normalizeKeyword(
            String keyword) {

        if (keyword == null) {
            return "";
        }

        return keyword.trim();
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

            if (!safeList.contains(normalizedValue)) {
                safeList.add(normalizedValue);
            }
        }

        return safeList;
    }

    private String makeSearchTitle(
            SearchVO searchVO) {

        boolean hasKeyword =
                searchVO != null
                        && searchVO.hasKeyword();

        boolean hasGenreFilter =
                searchVO != null
                        && searchVO.hasGenreCodes();

        boolean hasProviderFilter =
                searchVO != null
                        && searchVO.hasProviderIds();

        boolean hasContentTypeFilter =
                searchVO != null
                        && searchVO.hasContentTypes();

        boolean hasAnyFilter =
                hasGenreFilter
                        || hasProviderFilter
                        || hasContentTypeFilter;

        if (hasKeyword && hasAnyFilter) {
            return "'"
                    + searchVO.getKeyword()
                    + "' 조건 검색 결과";
        }

        if (hasKeyword) {
            return "'"
                    + searchVO.getKeyword()
                    + "' 검색 결과";
        }

        if (hasAnyFilter) {
            return "선택 조건 검색 결과";
        }

        return "지금 인기 있는 콘텐츠";
    }
}