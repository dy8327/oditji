package com.project.oditji.search.dao;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.session.SqlSession;
import org.springframework.stereotype.Repository;

import com.project.oditji.search.vo.SearchKeywordHistoryVO;

/**
 * SearchKeywordHistoryDAO 구현체입니다.
 *
 * MyBatis SqlSession을 이용해
 * 로그인 회원의 검색어 이력을 처리합니다.
 */
@Repository
public class SearchKeywordHistoryDAOImpl implements SearchKeywordHistoryDAO {

    private final SqlSession sqlSession;

    private static final String NAMESPACE =
            "com.project.oditji.search.dao.SearchKeywordHistoryDAO.";

    public SearchKeywordHistoryDAOImpl(
            SqlSession sqlSession) {

        this.sqlSession = sqlSession;
    }

    @Override
    public int mergeSearchKeywordHistory(
            SearchKeywordHistoryVO historyVO) {

        return sqlSession.update(
                NAMESPACE + "mergeSearchKeywordHistory",
                historyVO
        );
    }

    @Override
    public int deleteSearchKeywordHistoryBeyondLimit(
            Map<String, Object> param) {

        return sqlSession.delete(
                NAMESPACE + "deleteSearchKeywordHistoryBeyondLimit",
                param
        );
    }

    @Override
    public List<SearchKeywordHistoryVO> selectRecentSearchKeywordList(
            Map<String, Object> param) {

        return sqlSession.selectList(
                NAMESPACE + "selectRecentSearchKeywordList",
                param
        );
    }

    @Override
    public int deleteSearchKeywordHistory(
            SearchKeywordHistoryVO historyVO) {

        return sqlSession.delete(
                NAMESPACE + "deleteSearchKeywordHistory",
                historyVO
        );
    }

    @Override
    public int deleteAllSearchKeywordHistory(
            Long memberNo) {

        return sqlSession.delete(
                NAMESPACE + "deleteAllSearchKeywordHistory",
                memberNo
        );
    }
}
