package com.project.oditji.report.dao;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.session.SqlSession;
import org.springframework.stereotype.Repository;

import com.project.oditji.report.vo.ReportVO;

@Repository
public class ReportDAOImpl implements ReportDAO {

    private final SqlSession sqlSession;

    private static final String NAMESPACE = "com.project.oditji.report.dao.ReportDAO.";

    public ReportDAOImpl(SqlSession sqlSession) {
        this.sqlSession = sqlSession;
    }

    @Override
    public int insertReport(ReportVO report) {
        return sqlSession.insert(NAMESPACE + "insertReport", report);
    }

    @Override
    public int countReport(Map<String, Object> param) {
        return sqlSession.selectOne(NAMESPACE + "countReport", param);
    }

    @Override
    public List<Integer> selectReportedProductReviewNoList(Long memberNo) {
        return sqlSession.selectList(
                NAMESPACE + "selectReportedProductReviewNoList",
                memberNo);
    }
}
