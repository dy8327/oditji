package com.project.oditji;

import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.Test;
import org.springframework.boot.builder.SpringApplicationBuilder;

/** WAR 배포용 애플리케이션 초기화 구성을 검증합니다. */
class ServletInitializerCoverageTest {

    @Test
    void configureShouldReturnTheSuppliedBuilder() {
        SpringApplicationBuilder builder = new SpringApplicationBuilder();

        SpringApplicationBuilder configured = new ServletInitializer().configure(builder);

        assertSame(builder, configured);
    }
}
