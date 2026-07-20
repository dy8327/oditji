package com.project.oditji.search.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.project.oditji.search.vo.SearchResultPageVO;

/**
 * 사용자 검색 요청을 JSON 공용 캐시 조회로 처리합니다.
 *
 * 과거의 search/movie, search/tv, search/person, discover,
 * combined_credits, watch/providers 실시간 호출은 모두 제거했습니다.
 */
@Service
public class SearchServiceImpl implements SearchService {

    /**
     * SearchController와 동일한 콘텐츠 페이지 크기입니다.
     */
    private static final int CONTENT_PAGE_SIZE = 10;

    private final SearchContentPageCacheService
            searchContentPageCacheService;

    public SearchServiceImpl(
            SearchContentPageCacheService
                    searchContentPageCacheService) {

        this.searchContentPageCacheService =
                searchContentPageCacheService;
    }

    /**
     * 검색어가 없는 경우에도 동일한 공용 캐시를 인기순으로 조회합니다.
     */
    @Override
    public SearchResultPageVO getPopularContent(
            int page,
            List<String> contentCategories,
            List<String> genreCodes,
            List<String> providerIds) {

        return searchContentPageCacheService
                .getContentPage(
                        "",
                        page,
                        CONTENT_PAGE_SIZE,
                        contentCategories,
                        genreCodes,
                        providerIds
                );
    }

    /**
     * 사용자 검색어와 필터를 JSON 공용 캐시에 적용합니다.
     *
     * 이 메서드는 네트워크 요청을 생성하지 않습니다.
     */
    @Override
    public SearchResultPageVO searchContent(
            String keyword,
            int page,
            List<String> contentCategories,
            List<String> genreCodes,
            List<String> providerIds) {

        return searchContentPageCacheService
                .getContentPage(
                        keyword,
                        page,
                        CONTENT_PAGE_SIZE,
                        contentCategories,
                        genreCodes,
                        providerIds
                );
    }
}
