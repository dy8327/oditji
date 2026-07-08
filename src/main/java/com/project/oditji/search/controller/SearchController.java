package com.project.oditji.search.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.project.oditji.search.service.SearchService;
import com.project.oditji.search.vo.SearchResultPageVO;
import com.project.oditji.search.vo.SearchVO;

@Controller
public class SearchController {

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

        String keyword = searchVO.getKeyword();

        if (keyword != null) {
            keyword = keyword.trim();
            searchVO.setKeyword(keyword);
        }

        SearchResultPageVO resultPage;

        if (keyword == null || keyword.isEmpty()) {

            resultPage = searchService.getPopularContent(
                    searchVO.getPage(),
                    searchVO.getContentTypes(),
                    searchVO.getGenreCodes(),
                    searchVO.getProviderIds()
            );

        } else {

            resultPage = searchService.searchByTmdb(
                    keyword,
                    searchVO.getPage(),
                    searchVO.getContentTypes(),
                    searchVO.getGenreCodes(),
                    searchVO.getProviderIds()
            );
        }

        model.addAttribute(
                "searchVO",
                searchVO
        );

        model.addAttribute(
                "searchResults",
                resultPage.getResultList()
        );

        // 새 JSP에서 resultList를 사용해도 되도록 같이 전달
        model.addAttribute(
                "resultList",
                resultPage.getResultList()
        );

        model.addAttribute(
                "currentPage",
                resultPage.getPage()
        );

        model.addAttribute(
                "totalPages",
                resultPage.getTotalPages()
        );

        model.addAttribute(
                "totalResults",
                resultPage.getTotalResults()
        );

        model.addAttribute(
                "keyword",
                keyword == null ? "" : keyword
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

    private String makeSearchTitle(SearchVO searchVO) {

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