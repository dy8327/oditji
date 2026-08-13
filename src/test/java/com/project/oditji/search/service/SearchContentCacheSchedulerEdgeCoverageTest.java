package com.project.oditji.search.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

/** 캐시 스케줄러의 실행기 제출 거절 및 인터럽트 예외 분기를 검증합니다. */
@ExtendWith(MockitoExtension.class)
class SearchContentCacheSchedulerEdgeCoverageTest {

    @Mock
    private SearchContentStore searchContentStore;

    @Mock
    private SearchContentSnapshotService snapshotService;

    @Mock
    private SearchContentCollectorService collectorService;

    private SearchContentCacheScheduler scheduler;

    @BeforeEach
    void setUp() {
        scheduler = new SearchContentCacheScheduler(
                searchContentStore,
                snapshotService,
                collectorService);
    }

    @AfterEach
    void tearDown() {
        scheduler.shutdown();
    }

    @Test
    void rejectedSubmissionFromAlreadyShutdownExecutorShouldResetRefreshing() {
        scheduler.shutdown();
        AtomicBoolean shuttingDown = (AtomicBoolean) ReflectionTestUtils.getField(scheduler, "shuttingDown");
        shuttingDown.set(false);

        scheduler.submitRefresh();

        assertFalse(scheduler.isRefreshing());
    }

    @Test
    void interruptedCollectorFailureShouldResetRefreshingWithoutReplacingStore() {
        when(searchContentStore.getAll()).thenReturn(List.of());
        when(collectorService.collect(anyList(), any())).thenAnswer(invocation -> {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("interrupted");
        });

        scheduler.submitRefresh();

        verify(collectorService, timeout(3_000L)).collect(anyList(), any());
        assertEventuallyIdle();
    }

    private void assertEventuallyIdle() {
        long deadline = System.nanoTime() + 3_000_000_000L;
        while (scheduler.isRefreshing() && System.nanoTime() < deadline) {
            Thread.onSpinWait();
        }
        assertFalse(scheduler.isRefreshing());
    }
}
