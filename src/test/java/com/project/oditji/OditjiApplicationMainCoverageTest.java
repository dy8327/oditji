package com.project.oditji;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * 애플리케이션 진입점이 Spring Boot 실행기로 위임되는지 검증합니다.
 */
class OditjiApplicationMainCoverageTest {

    @Test
    void mainShouldDelegateToSpringApplication() {
        String[] arguments = {"--spring.main.banner-mode=off"};
        ConfigurableApplicationContext context =
                mock(ConfigurableApplicationContext.class);

        try (MockedStatic<SpringApplication> springApplication =
                mockStatic(SpringApplication.class)) {

            springApplication
                    .when(() -> SpringApplication.run(
                            OditjiApplication.class,
                            arguments))
                    .thenReturn(context);

            OditjiApplication.main(arguments);

            springApplication.verify(() -> SpringApplication.run(
                    OditjiApplication.class,
                    arguments));
        }
    }
}
