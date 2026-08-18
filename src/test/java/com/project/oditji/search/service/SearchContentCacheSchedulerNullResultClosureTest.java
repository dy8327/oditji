package com.project.oditji.search.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

/** 갱신 결과 null 분기를 직접 보완합니다. */
class SearchContentCacheSchedulerNullResultClosureTest {

    private SearchContentStore searchContentStore;
    private SearchContentSnapshotService snapshotService;
    private SearchContentCollectorService collectorService;
    private SearchContentCacheScheduler scheduler;

    @BeforeEach
    void setUp() {
        searchContentStore = mock(SearchContentStore.class);
        snapshotService = mock(SearchContentSnapshotService.class);
        collectorService = mock(SearchContentCollectorService.class);
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
    void nullRefreshResultShouldKeepExistingStoreAndResetRefreshingState() {
        when(searchContentStore.getAll()).thenReturn(List.of());
        when(collectorService.collect(anyList(), any())).thenReturn(null);

        ReflectionTestUtils.invokeMethod(
                scheduler,
                "refreshContentCache");

        assertFalse(scheduler.isRefreshing());
        verify(snapshotService, never()).saveSnapshot(anyList());
        verify(searchContentStore, never()).replaceAll(anyList());
    }
}
