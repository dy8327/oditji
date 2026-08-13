package com.project.oditji.event.dao;

import java.util.List;

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.stereotype.Repository;

import com.project.oditji.event.vo.EventVO;

@Repository
public class EventDAOImpl implements EventDAO {

    private static final String NAMESPACE =
            "com.project.oditji.event.dao.EventDAO";

    private final SqlSessionTemplate sqlSession;

    public EventDAOImpl(SqlSessionTemplate sqlSession) {
        this.sqlSession = sqlSession;
    }

    @Override
    public List<EventVO> selectEventList(String period) {
        return sqlSession.selectList(
                NAMESPACE + ".selectEventList",
                period
        );
    }

    @Override
    public EventVO selectEventDetail(Long eventNo) {
        return sqlSession.selectOne(
                NAMESPACE + ".selectEventDetail",
                eventNo
        );
    }
}
