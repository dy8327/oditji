package com.project.oditji.search.dao;

import java.util.List;

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.project.oditji.common.vo.SearchVO;
import com.project.oditji.search.vo.SearchResultVO;

@Repository
public class SearchDAOImpl implements SearchDAO {

    @Autowired
    private SqlSessionTemplate sqlSession;

    private static final String NAMESPACE =
            "com.project.oditji.search.dao.SearchDAO.";

    @Override
    public List<SearchResultVO> searchContent(SearchVO searchVO) {
        return sqlSession.selectList(
                NAMESPACE + "searchContent",
                searchVO);
    }

    @Override
    public int getSearchCount(SearchVO searchVO) {
        return sqlSession.selectOne(
                NAMESPACE + "getSearchCount",
                searchVO);
    }

}