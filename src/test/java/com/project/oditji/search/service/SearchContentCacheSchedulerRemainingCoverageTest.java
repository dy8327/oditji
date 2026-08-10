package com.project.oditji.search.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
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

/**
 * 캐시 갱신 예외 시 종료 상태가 true인 분기를 직접 보완합니다.
 */
@ExtendWith(MockitoExtension.class)
class SearchContentCacheSchedulerRemainingCoverageTest {

    @Mock
    private SearchContentStore searchContentStore;

    @Mock
    private SearchContentSnapshotService snapshotService;

    @Mock
    private SearchContentCollectorService collectorService;

    private SearchContentCacheScheduler scheduler;

    @BeforeEach
    void setUp() {
        scheduler =
                new SearchContentCacheScheduler(
                        searchContentStore,
                        snapshotService,
                        collectorService);
    }

    @AfterEach
    void tearDown() {
        scheduler.shutdown();
    }

    @Test
    void refreshFailureDuringShutdownShouldUseShutdownExceptionBranchAndResetState() {
        when(searchContentStore.getAll())
                .thenReturn(List.of());
        when(collectorService.collect(
                anyList(),
                any()))
                .thenThrow(
                        new IllegalStateException(
                                "shutdown failure"));

        AtomicBoolean refreshing =
                (AtomicBoolean)
                        ReflectionTestUtils.getField(
                                scheduler,
                                "refreshing");
        AtomicBoolean shuttingDown =
                (AtomicBoolean)
                        ReflectionTestUtils.getField(
                                scheduler,
                                "shuttingDown");

        refreshing.set(true);
        shuttingDown.set(true);

        ReflectionTestUtils.invokeMethod(
                scheduler,
                "refreshContentCache");

        assertFalse(
                scheduler.isRefreshing());
    }
}
