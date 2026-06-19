package com.project.oditji.search.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.project.oditji.common.vo.SearchVO;
import com.project.oditji.search.dao.SearchDAO;
import com.project.oditji.search.vo.SearchResultVO;

@Service
public class SearchServiceImpl implements SearchService {

    @Autowired
    private SearchDAO searchDAO;

    /**
     * 통합검색
     */
    @Override
    public List<SearchResultVO> searchContent(SearchVO searchVO) {

        return searchDAO.searchContent(searchVO);
    }

    /**
     * 검색 결과 개수 조회
     */
    @Override
    public int getSearchCount(SearchVO searchVO) {

        return searchDAO.getSearchCount(searchVO);
    }

}