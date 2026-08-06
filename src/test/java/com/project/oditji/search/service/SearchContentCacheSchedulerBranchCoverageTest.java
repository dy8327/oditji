package com.project.oditji.search.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.function.Consumer;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.project.oditji.search.vo.CachedContentVO;

/** 검색 캐시 스케줄러의 시작 갱신과 null 결과 분기를 추가로 검증합니다. */
@ExtendWith(MockitoExtension.class)
class SearchContentCacheSchedulerBranchCoverageTest {

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
    void initializeShouldRestoreSnapshotAndRefreshWhenStartupOptionIsEnabled() {

        List<CachedContentVO> snapshot = List.of(content(1L));
        List<CachedContentVO> refreshed = List.of(content(2L));
        when(snapshotService.loadSnapshot()).thenReturn(snapshot);
        when(searchContentStore.getAll()).thenReturn(snapshot);
        when(collectorService.collect(eq(snapshot), any())).thenAnswer(invocation -> {
            Consumer<List<CachedContentVO>> checkpoint = invocation.getArgument(1);
            checkpoint.accept(refreshed);
            return refreshed;
        });
        ReflectionTestUtils.setField(scheduler, "initializeOnStartup", true);

        scheduler.initialize();

        verify(searchContentStore, timeout(3_000L)).replaceAll(refreshed);
        scheduler.shutdown();

        verify(searchContentStore, times(2)).replaceAll(anyList());
        verify(snapshotService, times(2)).saveSnapshot(refreshed);
    }

    @Test
    void nullRefreshResultShouldKeepExistingSnapshotAndResetState() {

        List<CachedContentVO> previous = List.of(content(3L));
        when(searchContentStore.getAll()).thenReturn(previous);
        when(collectorService.collect(eq(previous), any())).thenReturn(null);

        scheduler.submitRefresh();

        verify(collectorService, timeout(3_000L)).collect(eq(previous), any());
        scheduler.shutdown();

        assertFalse(scheduler.isRefreshing());
        verify(snapshotService, never()).saveSnapshot(anyList());
        verify(searchContentStore, never()).replaceAll(anyList());
    }

    @Test
    void repeatedShutdownAndRefreshRequestsShouldRemainSafe() {
        scheduler.shutdown();
        scheduler.shutdown();

        scheduler.scheduledRefresh();
        scheduler.submitRefresh();

        assertFalse(scheduler.isRefreshing());
        verify(collectorService, never()).collect(anyList(), any());
    }

    private CachedContentVO content(Long tmdbId) {
        CachedContentVO content = new CachedContentVO();
        content.setTmdbId(tmdbId);
        content.setContentType("MOVIE");
        return content;
    }
}