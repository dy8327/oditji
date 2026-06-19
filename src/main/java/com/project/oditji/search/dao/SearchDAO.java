package com.project.oditji.search.dao;

import java.util.List;

import com.project.oditji.common.vo.SearchVO;
import com.project.oditji.search.vo.SearchResultVO;

public interface SearchDAO {

    // 검색 결과 조회
    List<SearchResultVO> searchContent(SearchVO searchVO);

    // 검색 결과 총 개수
    int getSearchCount(SearchVO searchVO);

}