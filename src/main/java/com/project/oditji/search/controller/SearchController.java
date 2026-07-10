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

    /*
     * 화면에 표시할 콘텐츠 개수
     */
    private static final int DISPLAY_PAGE_SIZE = 10;

    /*
     * TMDB API 최대 접근 페이지
     */
    private static final int MAX_TMDB_PAGE = 500;

    private final SearchService searchService;

    @Value("${tmdb.api.image-base-url}")
    private String imageBaseUrl;

    public SearchController(
            SearchService searchService) {

        this.searchService = searchService;
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

        searchVO.setKeyword(keyword);
        searchVO.setPage(displayPage);
        searchVO.setContentTypes(contentTypes);
        searchVO.setGenreCodes(genreCodes);
        searchVO.setProviderIds(providerIds);

        /*
         * 현재 화면 페이지까지 필요한 결과 개수.
         *
         * 1페이지: 10개 필요
         * 2페이지: 20개 필요
         * 3페이지: 30개 필요
         */
        int requiredResultCount =
                displayPage * DISPLAY_PAGE_SIZE;

        /*
         * 다음 페이지 존재 여부 확인을 위해
         * 한 개를 추가로 수집한다.
         */
        int collectionTarget =
                requiredResultCount + 1;

        List<SearchResultVO> collectedResultList =
                new ArrayList<SearchResultVO>();

        int tmdbPage = 1;

        int tmdbTotalPages = 0;
        int tmdbTotalResults = 0;

        /*
         * 지원 OTT가 있는 콘텐츠가 화면 페이지에 필요한 만큼
         * 모일 때까지 TMDB 페이지를 순서대로 조회한다.
         */
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
                tmdbTotalPages =
                        Math.min(
                                partialPage.getTotalPages(),
                                MAX_TMDB_PAGE
                        );

                tmdbTotalResults =
                        partialPage.getTotalResults();
            }

            List<SearchResultVO> partialResultList =
                    partialPage.getResultList();

            if (partialResultList != null
                    && !partialResultList.isEmpty()) {

                addUniqueResults(
                        collectedResultList,
                        partialResultList
                );
            }

            /*
             * TMDB가 알려준 마지막 페이지까지 도달하면 종료
             */
            if (partialPage.getTotalPages() <= tmdbPage) {
                break;
            }

            tmdbPage++;
        }

        int startIndex =
                (displayPage - 1)
                        * DISPLAY_PAGE_SIZE;

        int endIndex =
                Math.min(
                        startIndex + DISPLAY_PAGE_SIZE,
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

        boolean hasNextPage =
                collectedResultList.size()
                        > endIndex;

        /*
         * 지원 OTT 필터 적용 후의 실제 전체 개수는
         * 모든 TMDB 페이지를 검사해야 알 수 있다.
         *
         * 현재 페이지 기준으로 다음 페이지가 있으면
         * 최소 현재 페이지 + 1까지 표시한다.
         */
        int filteredTotalPages;

        if (hasNextPage) {
            filteredTotalPages =
                    displayPage + 1;
        } else {
            filteredTotalPages =
                    displayPage;
        }

        /*
         * 첫 페이지인데 결과가 없으면 페이지도 0으로 처리
         */
        if (displayPage == 1
                && displayResultList.isEmpty()) {

            filteredTotalPages = 0;
        }

        SearchResultPageVO displayPageVO =
                new SearchResultPageVO();

        displayPageVO.setResultList(
                displayResultList
        );

        displayPageVO.setPage(
                displayPage
        );

        displayPageVO.setTotalPages(
                filteredTotalPages
        );

        /*
         * 전체 TMDB 결과 수는 OTT 필터링 전 개수이므로
         * 참고값으로만 사용한다.
         */
        displayPageVO.setTotalResults(
                tmdbTotalResults
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
                filteredTotalPages
        );

        model.addAttribute(
                "totalResults",
                tmdbTotalResults
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

        /*
         * 필요하다면 화면에서 안내용으로 사용 가능
         */
        model.addAttribute(
                "tmdbTotalPages",
                tmdbTotalPages
        );

        return "search/searchResult";
    }

    /**
     * 검색어 유무에 따라 SearchService를 호출한다.
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
     * 콘텐츠 타입과 TMDB ID가 같은 결과는 중복으로 추가하지 않는다.
     */
    private void addUniqueResults(
            List<SearchResultVO> targetList,
            List<SearchResultVO> sourceList) {

        for (SearchResultVO sourceVO : sourceList) {

            if (sourceVO == null
                    || sourceVO.getTmdbId() == null
                    || sourceVO.getContentType() == null) {

                continue;
            }

            boolean duplicated = false;

            for (SearchResultVO targetVO : targetList) {

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
                        sourceVO.getContentType().equalsIgnoreCase(
                                targetVO.getContentType()
                        );

                if (sameTmdbId
                        && sameContentType) {

                    duplicated = true;
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

        if (hasKeyword
                && hasAnyFilter) {

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