package com.project.oditji.admin.dao;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.SqlSessionTemplate;

/** 사업자 회원 판별의 null/0/양수 단축평가 잔여 조건을 보완합니다. */
class AdminDAOResidualConditionClosureTest {

    private SqlSessionTemplate sqlSession;
    private AdminDAO dao;

    @BeforeEach
    void setUp() {
        sqlSession = mock(SqlSessionTemplate.class);
        dao = new AdminDAO(sqlSession);
    }

    @Test
    void businessMemberCheckShouldCoverNullZeroAndPositiveCounts() {
        when(sqlSession.<Integer>selectOne("adminIsBusinessMember", 1L))
                .thenReturn(null);
        when(sqlSession.<Integer>selectOne("adminIsBusinessMember", 2L))
                .thenReturn(0);
        when(sqlSession.<Integer>selectOne("adminIsBusinessMember", 3L))
                .thenReturn(1);

        assertFalse(dao.isBusinessMember(1L));
        assertFalse(dao.isBusinessMember(2L));
        assertTrue(dao.isBusinessMember(3L));
    }
}
