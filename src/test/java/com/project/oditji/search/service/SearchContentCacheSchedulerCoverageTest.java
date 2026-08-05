package com.project.oditji.search.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.project.oditji.search.vo.CachedContentVO;

/** 검색 캐시 초기 복원, 비동기 갱신, 중복 제출과 종료 안전성을 검증합니다. */
@ExtendWith(MockitoExtension.class)
class SearchContentCacheSchedulerCoverageTest {

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
    void initializeShouldRestoreNonEmptySnapshotWithoutRefreshWhenDisabled() {
        CachedContentVO content = content(1L);
        List<CachedContentVO> snapshot = List.of(content);
        when(snapshotService.loadSnapshot()).thenReturn(snapshot);
        ReflectionTestUtils.setField(scheduler, "initializeOnStartup", false);

        scheduler.initialize();

        verify(searchContentStore).replaceAll(snapshot);
        verify(collectorService, never()).collect(anyList(), any());
    }

    @Test
    void initializeShouldIgnoreEmptySnapshot() {
        when(snapshotService.loadSnapshot()).thenReturn(List.of());
        ReflectionTestUtils.setField(scheduler, "initializeOnStartup", false);

        scheduler.initialize();

        verify(searchContentStore, never()).replaceAll(anyList());
    }

    @Test
    void scheduledRefreshShouldSaveAndReplaceSuccessfulResult() throws Exception {
        List<CachedContentVO> previous = List.of(content(1L));
        List<CachedContentVO> refreshed = List.of(content(2L));
        when(searchContentStore.getAll()).thenReturn(previous);
        when(collectorService.collect(eq(previous), any())).thenReturn(refreshed);

        scheduler.scheduledRefresh();
        awaitIdle();

        verify(snapshotService).saveSnapshot(refreshed);
        verify(searchContentStore).replaceAll(refreshed);
    }

    @Test
    void emptyRefreshShouldKeepExistingStore() throws Exception {
        List<CachedContentVO> previous = List.of(content(3L));
        when(searchContentStore.getAll()).thenReturn(previous);
        when(collectorService.collect(eq(previous), any())).thenReturn(List.of());

        scheduler.submitRefresh();
        awaitIdle();

        verify(snapshotService, never()).saveSnapshot(anyList());
        verify(searchContentStore, never()).replaceAll(anyList());
    }

    @Test
    void collectorFailureShouldResetRefreshingState() throws Exception {
        List<CachedContentVO> previous = List.of(content(4L));
        when(searchContentStore.getAll()).thenReturn(previous);
        when(collectorService.collect(eq(previous), any()))
                .thenThrow(new IllegalStateException("network"));

        scheduler.submitRefresh();
        awaitIdle();

        assertFalse(scheduler.isRefreshing());
    }

    @Test
    void duplicateSubmissionShouldRunOnlyOneRefresh() throws Exception {
        CountDownLatch entered = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        when(searchContentStore.getAll()).thenReturn(List.of());
        when(collectorService.collect(anyList(), any())).thenAnswer(invocation -> {
            entered.countDown();
            release.await(2, TimeUnit.SECONDS);
            return List.of(content(5L));
        });

        scheduler.submitRefresh();
        assertTrue(entered.await(2, TimeUnit.SECONDS));
        assertTrue(scheduler.isRefreshing());

        scheduler.submitRefresh();
        release.countDown();
        awaitIdle();

        verify(collectorService, times(1)).collect(anyList(), any());
    }

    @Test
    void shutdownShouldPreventFutureSubmissions() throws Exception {
        scheduler.shutdown();

        scheduler.submitRefresh();
        Thread.sleep(50L);

        assertFalse(scheduler.isRefreshing());
        verify(collectorService, never()).collect(anyList(), any());
    }

    private void awaitIdle() throws Exception {
        Instant deadline = Instant.now().plus(Duration.ofSeconds(3));

        while (scheduler.isRefreshing() && Instant.now().isBefore(deadline)) {
            Thread.sleep(10L);
        }

        assertFalse(scheduler.isRefreshing());
    }

    private CachedContentVO content(Long tmdbId) {
        CachedContentVO content = new CachedContentVO();
        content.setTmdbId(tmdbId);
        content.setContentType("MOVIE");
        return content;
    }
}
