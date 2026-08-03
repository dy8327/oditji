package com.project.oditji.search.service;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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

@Service
public class SearchContentCacheScheduler {

    private static final Logger log = LoggerFactory.getLogger(SearchContentCacheScheduler.class);

    private final SearchContentStore searchContentStore;
    private final SearchContentSnapshotService snapshotService;
    private final SearchContentCollectorService collectorService;

    private final AtomicBoolean refreshing =
            new AtomicBoolean(false);

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

    @EventListener(ApplicationReadyEvent.class)
    public void initialize() {

        List<CachedContentVO> snapshot =
                snapshotService.loadSnapshot();

        if (!snapshot.isEmpty()) {

            searchContentStore.replaceAll(snapshot);

            log.info("검색 콘텐츠 스냅샷 복원 완료: {}건", snapshot.size());
        }

        if (initializeOnStartup) {
            submitRefresh();
        }
    }

    @Scheduled(
            fixedDelayString =
                    "${search.content-cache.refresh-delay-ms:172800000}",
            initialDelayString =
                    "${search.content-cache.scheduler-initial-delay-ms:60000}"
    )
    public void scheduledRefresh() {
        submitRefresh();
    }

    public void submitRefresh() {

        if (!refreshing.compareAndSet(
                false,
                true
        )) {

            return;
        }

        refreshExecutor.submit(
                () -> {

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

                        log.info("검색 콘텐츠 공용 저장소 갱신 완료: {}건", refreshed.size());

                    } catch (Exception e) {

                        log.error("검색 콘텐츠 공용 저장소 갱신 실패", e);

                    } finally {

                        refreshing.set(false);
                    }
                }
        );
    }

    public boolean isRefreshing() {
        return refreshing.get();
    }

    @PreDestroy
    public void shutdown() {
        refreshExecutor.shutdownNow();
    }
}