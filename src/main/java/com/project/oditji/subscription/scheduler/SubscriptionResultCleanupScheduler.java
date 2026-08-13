package com.project.oditji.subscription.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.project.oditji.subscription.service.SubscriptionCalculatorService;

/**
 * SUBSCRIPTION_RESULT 정리 스케줄러입니다.
 *
 * 비회원이 만든 구독 조합 공유 링크는 EXPIRES_AT이 지나면 더 이상 조회되지 않게
 * (subscriptionMapper의 selectResultById에서 제외) 되어 있지만, 실제 행은 남아있으므로
 * 이 스케줄러가 주기적으로 삭제한다. 회원이 저장한 결과는 EXPIRES_AT이 NULL이라
 * 삭제 대상에서 제외되어 계속 보관된다.
 */
@Component
public class SubscriptionResultCleanupScheduler {

    private static final Logger log = LoggerFactory.getLogger(SubscriptionResultCleanupScheduler.class);

    private final SubscriptionCalculatorService subscriptionCalculatorService;

    public SubscriptionResultCleanupScheduler(
            SubscriptionCalculatorService subscriptionCalculatorService) {

        this.subscriptionCalculatorService = subscriptionCalculatorService;
    }

    /**
     * 애플리케이션 시작이 완료된 직후 만료된 비회원 공유 링크를 한 번 정리합니다.
     *
     * 개발 환경에서 서버가 새벽 4시에 꺼져 있어도
     * 다음 서버 실행 시 오래된 기록이 정리됩니다.
     */
    @EventListener(ApplicationReadyEvent.class)
    public void cleanupOnApplicationReady() {

        cleanupExpiredResults(
                "서버 시작"
        );
    }

    /**
     * 서버가 계속 실행 중인 경우
     * 매일 새벽 4시 10분에 만료된 비회원 공유 링크를 정리합니다.
     */
    @Scheduled(cron = "0 10 4 * * *")
    public void cleanupEveryDay() {

        cleanupExpiredResults(
                "일일 스케줄"
        );
    }

    /**
     * 실제 삭제를 공통 처리합니다.
     *
     * 정리 실패가 애플리케이션 전체 실행을 중단시키지 않도록
     * 스케줄러 내부에서 예외를 처리합니다.
     */
    private void cleanupExpiredResults(
            String executionType) {

        try {

            int deletedCount =
                    subscriptionCalculatorService
                            .deleteExpiredResults();

            if (deletedCount > 0) {

                log.info("[구독 조합 공유 링크 정리] {} / 삭제된 기록: {}", executionType, deletedCount);
            }

        } catch (Exception e) {

            log.error("[구독 조합 공유 링크 정리 실패] {}", executionType, e);
        }
    }
}
