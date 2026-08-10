package com.project.oditji.common.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.when;

import javax.sql.DataSource;

import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mybatis.spring.SqlSessionFactoryBean;

/**
 * SqlSessionFactoryBean#getObject()가 null인 방어 분기를 직접 검증합니다.
 */
class MyBatisConfigNullFactoryCoverageTest {

    @Test
    void nullFactoryShouldThrowConfiguredIllegalStateException() {
        try (MockedConstruction<SqlSessionFactoryBean> construction =
                mockConstruction(
                        SqlSessionFactoryBean.class,
                        (mock, context) ->
                                when(mock.getObject())
                                        .thenReturn(null))) {

            MyBatisConfig config = new MyBatisConfig();
            DataSource dataSource =
                    mock(DataSource.class);

            IllegalStateException exception =
                    assertThrows(
                            IllegalStateException.class,
                            () -> config.sqlSessionFactory(dataSource));

            assertEquals(
                    "SqlSessionFactory를 생성하지 못했습니다.",
                    exception.getMessage());
        }
    }
}
