package com.project.oditji.common.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.Executor;

import org.junit.jupiter.api.Test;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/** 접속 로그 비동기 실행기의 생성 및 핵심 설정값을 검증합니다. */
class AsyncConfigCoverageTest {

    @Test
    void accessLogExecutorShouldCreateInitializedThreadPool() {
        AsyncConfig config = new AsyncConfig();

        Executor executor = config.accessLogExecutor();
        ThreadPoolTaskExecutor taskExecutor =
                assertInstanceOf(ThreadPoolTaskExecutor.class, executor);

        assertEquals(1, taskExecutor.getCorePoolSize());
        assertEquals(2, taskExecutor.getMaxPoolSize());
        assertEquals("access-log-", taskExecutor.getThreadNamePrefix());
        assertTrue(taskExecutor.getThreadPoolExecutor().getQueue().remainingCapacity() >= 500);

        taskExecutor.shutdown();
    }
}
