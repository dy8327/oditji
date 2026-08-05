package com.project.oditji.search.service;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.project.oditji.search.vo.CachedContentVO;

import jakarta.annotation.PreDestroy;

/**
 * 검색 콘텐츠 공용 캐시의 초기 복원과 비동기 갱신을 담당합니다.
 *
 * Spring Boot DevTools 재시작 중에는 기존 ApplicationContext가 종료되면서
 * 실행기가 먼저 종료될 수 있으므로, 종료 상태와 작업 제출 경쟁 상태를
 * 안전하게 처리합니다.
 */
@Service
public class SearchContentCacheScheduler {

    private static final Logger log =
            LoggerFactory.getLogger(SearchContentCacheScheduler.class);

    private final SearchContentStore searchContentStore;
    private final SearchContentSnapshotService snapshotService;
    private final SearchContentCollectorService collectorService;

    /**
     * 동일 시점에 갱신 작업이 중복 실행되는 것을 방지합니다.
     */
    private final AtomicBoolean refreshing =
            new AtomicBoolean(false);

    /**
     * ApplicationContext 종료가 시작된 뒤 새 작업이 제출되는 것을 방지합니다.
     */
    private final AtomicBoolean shuttingDown =
            new AtomicBoolean(false);

    /**
     * 검색 콘텐츠 갱신 전용 단일 스레드 실행기입니다.
     */
    private final ExecutorService refreshExecutor =
            Executors.newSingleThreadExecutor(
                    runnable -> {

                        Thread thread =
                                new Thread(
                                        runnable,
                                        "search-content-refresh"
                                );

                        thread.setDaemon(true);

                        return thread;
                    }
            );

    @Value("${search.content-cache.initialize-on-startup:true}")
    private boolean initializeOnStartup;

    public SearchContentCacheScheduler(
            SearchContentStore searchContentStore,
            SearchContentSnapshotService snapshotService,
            SearchContentCollectorService collectorService) {

        this.searchContentStore =
                searchContentStore;

        this.snapshotService =
                snapshotService;

        this.collectorService =
                collectorService;
    }

    /**
     * 애플리케이션 시작이 완료되면 저장된 스냅샷을 먼저 복원하고,
     * 설정이 활성화된 경우 최신 데이터 갱신을 요청합니다.
     */
    @EventListener(ApplicationReadyEvent.class)
    public void initialize() {

        List<CachedContentVO> snapshot =
                snapshotService.loadSnapshot();

        if (!snapshot.isEmpty()) {

            searchContentStore.replaceAll(snapshot);
        }

        if (initializeOnStartup) {
            submitRefresh();
        }
    }

    /**
     * 설정된 주기에 따라 검색 콘텐츠 갱신을 요청합니다.
     */
    @Scheduled(
            fixedDelayString =
                    "${search.content-cache.refresh-delay-ms:172800000}",
            initialDelayString =
                    "${search.content-cache.scheduler-initial-delay-ms:60000}"
    )
    public void scheduledRefresh() {
        submitRefresh();
    }

    /**
     * 검색 콘텐츠 갱신 작업을 전용 실행기에 제출합니다.
     *
     * DevTools 재시작 과정에서 실행기 종료와 작업 제출이 동시에 발생하더라도
     * RejectedExecutionException이 애플리케이션 시작 과정으로 전파되지 않도록
     * 종료 상태를 확인하고 제출 거절을 안전하게 처리합니다.
     */
    public void submitRefresh() {

        if (shuttingDown.get()) {

            log.debug("애플리케이션 종료 중이므로 검색 콘텐츠 갱신 요청을 건너뜁니다.");

            return;
        }

        if (!refreshing.compareAndSet(
                false,
                true
        )) {

            return;
        }

        try {

            refreshExecutor.submit(this::refreshContentCache);

        } catch (RejectedExecutionException e) {

            refreshing.set(false);

            if (shuttingDown.get()
                    || refreshExecutor.isShutdown()
                    || refreshExecutor.isTerminated()) {

                log.debug("검색 콘텐츠 갱신 실행기가 종료되어 작업 제출을 건너뜁니다.");

                return;
            }

            log.error("검색 콘텐츠 갱신 작업 제출 실패", e);
        }
    }

    /**
     * 기존 캐시를 기준으로 콘텐츠를 수집하고 스냅샷과 공용 저장소를 갱신합니다.
     */
    private void refreshContentCache() {

        try {

            List<CachedContentVO> previous =
                    searchContentStore.getAll();

            List<CachedContentVO> refreshed =
                    collectorService.collect(
                            previous,
                            snapshotService::saveSnapshot
                    );

            if (refreshed == null
                    || refreshed.isEmpty()) {

                log.warn("검색 콘텐츠 갱신 결과가 비어 있어 기존 데이터를 유지합니다.");

                return;
            }

            snapshotService.saveSnapshot(
                    refreshed
            );

            searchContentStore.replaceAll(
                    refreshed
            );

        } catch (Exception e) {

            if (shuttingDown.get()
                    || Thread.currentThread().isInterrupted()) {

                log.debug("애플리케이션 종료로 검색 콘텐츠 갱신을 중단합니다.");

            } else {

                log.error("검색 콘텐츠 공용 저장소 갱신 실패", e);
            }

        } finally {

            refreshing.set(false);
        }
    }

    public boolean isRefreshing() {
        return refreshing.get();
    }

    /**
     * ApplicationContext 종료 시 실행기를 중지하고 이후 작업 제출을 차단합니다.
     */
    @PreDestroy
    public void shutdown() {

        shuttingDown.set(true);
        refreshing.set(false);

        refreshExecutor.shutdownNow();
    }
}
