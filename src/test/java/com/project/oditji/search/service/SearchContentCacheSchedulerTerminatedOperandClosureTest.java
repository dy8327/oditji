package com.project.oditji.search.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

/** 작업 제출 거절 조건 중 isTerminated()가 참인 마지막 OR 피연산자를 보완합니다. */
class SearchContentCacheSchedulerTerminatedOperandClosureTest {

    private SearchContentCacheScheduler scheduler;
    private ExecutorService originalExecutor;

    @BeforeEach
    void setUp() {
        scheduler = new SearchContentCacheScheduler(
                mock(SearchContentStore.class),
                mock(SearchContentSnapshotService.class),
                mock(SearchContentCollectorService.class));
        originalExecutor = (ExecutorService) ReflectionTestUtils.getField(
                scheduler,
                "refreshExecutor");
    }

    @AfterEach
    void tearDown() {
        if (originalExecutor != null) {
            originalExecutor.shutdownNow();
        }
    }

    @Test
    void rejectedSubmissionShouldStopAtTerminatedExecutorOperand() {
        ExecutorService executor = mock(ExecutorService.class);
        when(executor.submit(any(Runnable.class)))
                .thenThrow(new RejectedExecutionException("terminated"));
        when(executor.isShutdown()).thenReturn(false);
        when(executor.isTerminated()).thenReturn(true);

        ReflectionTestUtils.setField(
                scheduler,
                "refreshExecutor",
                executor);

        AtomicBoolean shuttingDown = (AtomicBoolean) ReflectionTestUtils.getField(
                scheduler,
                "shuttingDown");
        shuttingDown.set(false);

        scheduler.submitRefresh();

        assertFalse(scheduler.isRefreshing());
    }
}
