package com.project.oditji.search.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.session.SqlSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.project.oditji.search.vo.SearchKeywordHistoryVO;

/** SearchKeywordHistoryDAOImpl의 MyBatis 위임 경로를 모두 검증합니다. */
class SearchKeywordHistoryDAOImplCoverageTest {

    private static final String NAMESPACE =
            "com.project.oditji.search.dao.SearchKeywordHistoryDAO.";

    private SqlSession sqlSession;
    private SearchKeywordHistoryDAOImpl dao;

    @BeforeEach
    void setUp() {
        sqlSession = mock(SqlSession.class);
        dao = new SearchKeywordHistoryDAOImpl(sqlSession);
    }

    @Test
    void mergeShouldDelegateToSqlSessionUpdate() {
        SearchKeywordHistoryVO history = new SearchKeywordHistoryVO();
        when(sqlSession.update(
                NAMESPACE + "mergeSearchKeywordHistory",
                history))
                .thenReturn(1);

        assertEquals(1, dao.mergeSearchKeywordHistory(history));
        verify(sqlSession).update(
                NAMESPACE + "mergeSearchKeywordHistory",
                history);
    }

    @Test
    void deleteBeyondLimitShouldDelegateToSqlSessionDelete() {
        Map<String, Object> param = Map.of(
                "memberNo", 3L,
                "keepCount", 30);
        when(sqlSession.delete(
                NAMESPACE + "deleteSearchKeywordHistoryBeyondLimit",
                param))
                .thenReturn(2);

        assertEquals(2, dao.deleteSearchKeywordHistoryBeyondLimit(param));
        verify(sqlSession).delete(
                NAMESPACE + "deleteSearchKeywordHistoryBeyondLimit",
                param);
    }

    @Test
    void recentListShouldDelegateToSqlSessionSelectList() {
        Map<String, Object> param = Map.of(
                "memberNo", 4L,
                "limit", 10);
        SearchKeywordHistoryVO history = new SearchKeywordHistoryVO();
        List<SearchKeywordHistoryVO> expected = List.of(history);
        when(sqlSession.<SearchKeywordHistoryVO>selectList(
                NAMESPACE + "selectRecentSearchKeywordList",
                param))
                .thenReturn(expected);

        assertSame(expected, dao.selectRecentSearchKeywordList(param));
        verify(sqlSession).selectList(
                NAMESPACE + "selectRecentSearchKeywordList",
                param);
    }

    @Test
    void deleteOneShouldDelegateToSqlSessionDelete() {
        SearchKeywordHistoryVO history = new SearchKeywordHistoryVO();
        when(sqlSession.delete(
                NAMESPACE + "deleteSearchKeywordHistory",
                history))
                .thenReturn(1);

        assertEquals(1, dao.deleteSearchKeywordHistory(history));
        verify(sqlSession).delete(
                NAMESPACE + "deleteSearchKeywordHistory",
                history);
    }

    @Test
    void deleteAllShouldDelegateToSqlSessionDelete() {
        when(sqlSession.delete(
                NAMESPACE + "deleteAllSearchKeywordHistory",
                9L))
                .thenReturn(5);

        assertEquals(5, dao.deleteAllSearchKeywordHistory(9L));
        verify(sqlSession).delete(
                NAMESPACE + "deleteAllSearchKeywordHistory",
                9L);
    }
}
