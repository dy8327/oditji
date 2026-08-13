package com.project.oditji.common.dao;

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.stereotype.Repository;

import com.project.oditji.common.vo.AccessLogVO;

@Repository
public class AccessLogDAO {

    private final SqlSessionTemplate sqlSession;

    public AccessLogDAO(SqlSessionTemplate sqlSession) {
        this.sqlSession = sqlSession;
    }

    public int insertAccessLog(AccessLogVO accessLog) {
        return sqlSession.insert("insertAccessLog", accessLog);
    }
}
