package com.project.oditji.content.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.project.oditji.content.service.ContentService;

/**
 * CONTENT_VIEW_HISTORY 정리 스케줄러입니다.
 *
 * 조회 이력은 개인 OTT 추천과
 * 관리자 월간 OTT 노출 가능성 통계에 사용하므로
 * 오늘을 포함한 최근 30일 데이터만 유지합니다.
 */
@Component
public class ContentViewHistoryCleanupScheduler {

    private static final Logger log = LoggerFactory.getLogger(ContentViewHistoryCleanupScheduler.class);

    private final ContentService contentService;

    public ContentViewHistoryCleanupScheduler(
            ContentService contentService) {

        this.contentService = contentService;
    }

    /**
     * 애플리케이션 시작이 완료된 직후
     * 30일이 지난 조회 이력을 한 번 정리합니다.
     *
     * 개발 환경에서 서버가 새벽 4시에 꺼져 있어도
     * 다음 서버 실행 시 오래된 기록이 정리됩니다.
     */
    @EventListener(ApplicationReadyEvent.class)
    public void cleanupOnApplicationReady() {

        cleanupExpiredHistory(
                "서버 시작"
        );
    }

    /**
     * 서버가 계속 실행 중인 경우
     * 매일 새벽 4시에 오래된 기록을 정리합니다.
     */
    @Scheduled(cron = "0 0 4 * * *")
    public void cleanupEveryDay() {

        cleanupExpiredHistory(
                "일일 스케줄"
        );
    }

    /**
     * 실제 삭제를 공통 처리합니다.
     *
     * 정리 실패가 애플리케이션 전체 실행을 중단시키지 않도록
     * 스케줄러 내부에서 예외를 처리합니다.
     */
    private void cleanupExpiredHistory(
            String executionType) {

        try {

            int deletedCount =
                    contentService
                            .deleteExpiredContentViewHistory();

            if (deletedCount > 0) {

                log.info("[콘텐츠 조회 이력 정리] {} / 삭제된 기록: {}", executionType, deletedCount);
            }

        } catch (Exception e) {

            log.error("[콘텐츠 조회 이력 정리 실패] {}", executionType, e);
        }
    }
}