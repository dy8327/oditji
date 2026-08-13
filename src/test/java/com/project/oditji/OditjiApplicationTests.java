package com.project.oditji;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 외부 DB와 로컬 파일 경로에 의존하지 않고
 * 애플리케이션 시작 클래스의 핵심 설정을 검증합니다.
 */
class OditjiApplicationTests {

    @Test
    void applicationConfigurationShouldBeDeclared() {
        assertNotNull(
                OditjiApplication.class.getAnnotation(
                        SpringBootApplication.class));
        assertNotNull(
                OditjiApplication.class.getAnnotation(
                        MapperScan.class));
        assertNotNull(
                OditjiApplication.class.getAnnotation(
                        EnableCaching.class));
        assertNotNull(
                OditjiApplication.class.getAnnotation(
                        EnableScheduling.class));
    }
}
