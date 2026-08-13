package com.project.oditji.common.config;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;

import javax.sql.DataSource;

import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.defaults.DefaultSqlSessionFactory;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.SqlSessionTemplate;

/**
 * MyBatis 설정의 SqlSessionTemplate 생성 경로를 직접 검증합니다.
 */
class MyBatisConfigRemainingCoverageTest {

    @Test
    void sqlSessionTemplateShouldUseProvidedFactory() {
        MyBatisConfig config =
                new MyBatisConfig();

        DataSource dataSource =
                mock(DataSource.class);

        Environment environment =
                new Environment(
                        "coverage-test",
                        new JdbcTransactionFactory(),
                        dataSource);

        Configuration configuration =
                new Configuration(environment);

        SqlSessionFactory factory =
                new DefaultSqlSessionFactory(
                        configuration);

        SqlSessionTemplate template =
                config.sqlSessionTemplate(
                        factory);

        assertSame(
                factory,
                template.getSqlSessionFactory());
    }
}
