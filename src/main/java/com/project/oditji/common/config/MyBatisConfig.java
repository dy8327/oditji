package com.project.oditji.common.config;

import javax.sql.DataSource;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

/**
 * MyBatis에서 사용할 SqlSessionFactory와
 * SqlSessionTemplate을 생성하는 설정 클래스입니다.
 */
@Configuration
public class MyBatisConfig {

    /**
     * DataSource와 MyBatis Mapper XML을 연결한
     * SqlSessionFactory를 생성합니다.
     *
     * @param dataSource Spring이 관리하는 데이터베이스 연결 객체
     * @return 생성된 SqlSessionFactory
     * @throws Exception Mapper XML 조회 또는 Factory 생성 중 오류가 발생한 경우
     */
    @Bean
    public SqlSessionFactory sqlSessionFactory(
            DataSource dataSource) throws Exception {

        SqlSessionFactoryBean factoryBean =
                new SqlSessionFactoryBean();

        /*
         * Spring이 관리하는 DataSource를
         * MyBatis SqlSessionFactoryBean에 연결합니다.
         */
        factoryBean.setDataSource(
                dataSource
        );

        /*
         * src/main/resources/mybatis/mappers 이하의
         * 모든 Mapper XML 파일을 MyBatis에 등록합니다.
         */
        factoryBean.setMapperLocations(
                new PathMatchingResourcePatternResolver()
                        .getResources(
                                "classpath:mybatis/mappers/**/*.xml"
                        )
        );

        SqlSessionFactory factory =
                factoryBean.getObject();

        /*
         * SqlSessionFactoryBean#getObject()의 반환값은
         * 타입상 null일 수 있습니다.
         *
         * 정상적인 MyBatis 초기화 과정에서는 Factory가 생성되지만,
         * 설정 오류 등으로 생성되지 않은 경우 아래 설정 코드에서
         * NullPointerException이 발생하지 않도록 명시적으로 검증합니다.
         */
        if (factory == null) {

            throw new IllegalStateException(
                    "SqlSessionFactory를 생성하지 못했습니다."
            );
        }

        /*
         * 데이터베이스의 MEMBER_NO 같은 스네이크 케이스 컬럼을
         * Java의 memberNo 같은 카멜 케이스 필드에 자동 매핑합니다.
         */
        factory.getConfiguration()
                .setMapUnderscoreToCamelCase(
                        true
                );

        return factory;
    }

    /**
     * 생성된 SqlSessionFactory를 사용하는
     * SqlSessionTemplate을 Spring Bean으로 등록합니다.
     *
     * @param sqlSessionFactory 앞에서 생성한 MyBatis Factory
     * @return MyBatis SQL 실행에 사용할 SqlSessionTemplate
     */
    @Bean
    public SqlSessionTemplate sqlSessionTemplate(
            SqlSessionFactory sqlSessionFactory) {

        return new SqlSessionTemplate(
                sqlSessionFactory
        );
    }
}