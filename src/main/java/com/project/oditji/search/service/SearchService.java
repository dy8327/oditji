package com.project.oditji.search.service;

import java.util.List;

import com.project.oditji.search.vo.SearchResultPageVO;

public interface SearchService {

    /**
     * 검색어가 없을 때 인기 콘텐츠 또는 필터 검색
     */
    SearchResultPageVO getPopularContent(
            int page,
            List<String> contentTypes,
            List<String> genreCodes,
            List<String> providerIds
    );

    /**
     * 검색어가 있을 때 TMDB 검색
     */
    SearchResultPageVO searchByTmdb(
            String keyword,
            int page,
            List<String> contentTypes,
            List<String> genreCodes,
            List<String> providerIds
    );
}