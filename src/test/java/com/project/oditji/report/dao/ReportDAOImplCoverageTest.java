package com.project.oditji.report.dao;

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

import com.project.oditji.report.vo.ReportVO;

/** ReportDAOImpl의 MyBatis 위임 경로를 모두 검증합니다. */
class ReportDAOImplCoverageTest {

    private static final String NAMESPACE = "com.project.oditji.report.dao.ReportDAO.";

    private SqlSession sqlSession;
    private ReportDAOImpl dao;

    @BeforeEach
    void setUp() {
        sqlSession = mock(SqlSession.class);
        dao = new ReportDAOImpl(sqlSession);
    }

    @Test
    void insertReportShouldDelegateToSqlSession() {
        ReportVO report = new ReportVO();
        when(sqlSession.insert(NAMESPACE + "insertReport", report)).thenReturn(1);

        assertEquals(1, dao.insertReport(report));
        verify(sqlSession).insert(NAMESPACE + "insertReport", report);
    }

    @Test
    void countReportShouldDelegateToSqlSession() {
        Map<String, Object> param = Map.of("memberNo", 1L);
        when(sqlSession.selectOne(NAMESPACE + "countReport", param)).thenReturn(2);

        assertEquals(2, dao.countReport(param));
        verify(sqlSession).selectOne(NAMESPACE + "countReport", param);
    }

    @Test
    void reportedProductReviewListShouldDelegateToSqlSession() {
        List<Integer> expected = List.of(3, 7);
        when(sqlSession.<Integer>selectList(NAMESPACE + "selectReportedProductReviewNoList", 10L))
                .thenReturn(expected);

        assertSame(expected, dao.selectReportedProductReviewNoList(10L));
        verify(sqlSession).selectList(NAMESPACE + "selectReportedProductReviewNoList", 10L);
    }
}
