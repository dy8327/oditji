package com.project.oditji.search.service;

import java.util.List;

import com.project.oditji.search.vo.SearchResultPageVO;

public interface SearchService {

    SearchResultPageVO getPopularContent(
            int page,
            List<String> contentCategories,
            List<String> genreCodes,
            List<String> providerIds
    );

    SearchResultPageVO searchByTmdb(
            String keyword,
            int page,
            List<String> contentCategories,
            List<String> genreCodes,
            List<String> providerIds
    );
}
