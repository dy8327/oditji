package com.project.oditji.favorite.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.project.oditji.favorite.service.FavoriteService;

/**
 * 찜한 콘텐츠의 공개일이 다가오면 회원에게 알림을 보내는 스케줄러입니다.
 *
 * 매일 아침 한 번, 오늘 또는 내일(하루 전) 개봉·공개하는 콘텐츠를 찜한
 * 회원 전원에게 업무 알림(NOTIFICATION)을 생성합니다.
 */
@Component
public class FavoriteReleaseNotificationScheduler {

    private static final Logger log =
            LoggerFactory.getLogger(FavoriteReleaseNotificationScheduler.class);

    private final FavoriteService favoriteService;

    public FavoriteReleaseNotificationScheduler(
            FavoriteService favoriteService) {

        this.favoriteService = favoriteService;
    }

    /**
     * 애플리케이션 시작이 완료된 직후 한 번 실행합니다.
     *
     * 개발 환경에서 서버가 매일 아침 9시에 꺼져 있어도
     * 다음 서버 실행 시 오늘 보내야 할 알림이 누락되지 않습니다.
     */
    @EventListener(ApplicationReadyEvent.class)
    public void notifyOnApplicationReady() {

        notifyUpcomingReleases(
                "서버 시작"
        );
    }

    /**
     * 서버가 계속 실행 중인 경우 매일 아침 9시에 알림을 발송합니다.
     */
    @Scheduled(cron = "0 0 9 * * *")
    public void notifyEveryDay() {

        notifyUpcomingReleases(
                "일일 스케줄"
        );
    }

    /**
     * 실제 알림 발송을 공통 처리합니다.
     *
     * 발송 실패가 애플리케이션 전체 실행을 중단시키지 않도록
     * 스케줄러 내부에서 예외를 처리합니다.
     */
    private void notifyUpcomingReleases(
            String executionType) {

        try {

            int notifiedCount =
                    favoriteService
                            .notifyUpcomingReleases();

            if (notifiedCount > 0) {

                log.info(
                        "[찜한 콘텐츠 공개 알림] {} / 발송된 알림: {}",
                        executionType,
                        notifiedCount
                );
            }

        } catch (Exception e) {

            log.error(
                    "[찜한 콘텐츠 공개 알림 발송 실패] {}",
                    executionType,
                    e
            );
        }
    }
}
