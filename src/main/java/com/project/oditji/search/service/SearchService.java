package com.project.oditji.search.service;

import java.util.List;

import com.project.oditji.search.vo.SearchResultPageVO;

/**
 * 검색 콘텐츠 조회 서비스입니다.
 *
 * 사용자 검색 요청에서는 TMDB API를 직접 호출하지 않고,
 * SearchContentStore에 적재된 JSON 공용 캐시만 조회합니다.
 */
public interface SearchService {

    /**
     * 검색어 없이 인기순 콘텐츠를 조회합니다.
     */
    SearchResultPageVO getPopularContent(
            int page,
            List<String> contentCategories,
            List<String> genreCodes,
            List<String> providerIds
    );

    /**
     * 검색어와 필터를 적용하여 공용 캐시에서 콘텐츠를 조회합니다.
     */
    SearchResultPageVO searchContent(
            String keyword,
            int page,
            List<String> contentCategories,
            List<String> genreCodes,
            List<String> providerIds
    );

    /**
     * 기존 호출부 호환용 메서드입니다.
     *
     * 이름에는 Tmdb가 남아 있지만 실제 TMDB API는 호출하지 않고
     * searchContent()로 위임합니다.
     *
     * 기존 참조가 모두 제거된 뒤 이 메서드도 삭제할 수 있습니다.
     */
    default SearchResultPageVO searchByTmdb(
            String keyword,
            int page,
            List<String> contentCategories,
            List<String> genreCodes,
            List<String> providerIds
    ) {

        return searchContent(
                keyword,
                page,
                contentCategories,
                genreCodes,
                providerIds
        );
    }
}
