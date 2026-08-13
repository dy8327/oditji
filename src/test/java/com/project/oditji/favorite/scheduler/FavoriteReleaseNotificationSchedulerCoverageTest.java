package com.project.oditji.favorite.scheduler;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.project.oditji.favorite.service.FavoriteService;

/**
 * 찜 콘텐츠 공개 알림 스케줄러의 시작/정기 실행, 무발송, 예외 안전 처리 분기를 검증합니다.
 */
@ExtendWith(MockitoExtension.class)
class FavoriteReleaseNotificationSchedulerCoverageTest {

    @Mock
    private FavoriteService favoriteService;

    private FavoriteReleaseNotificationScheduler scheduler;

    @BeforeEach
    void setUp() {
        scheduler = new FavoriteReleaseNotificationScheduler(favoriteService);
    }

    @Test
    void applicationReadyAndDailyScheduleShouldInvokeFavoriteNotificationService() {
        when(favoriteService.notifyUpcomingReleases())
                .thenReturn(2)
                .thenReturn(0);

        scheduler.notifyOnApplicationReady();
        scheduler.notifyEveryDay();

        verify(favoriteService, times(2)).notifyUpcomingReleases();
    }

    @Test
    void schedulerShouldSwallowNotificationFailure() {
        when(favoriteService.notifyUpcomingReleases())
                .thenThrow(new IllegalStateException("notification failure"));

        assertDoesNotThrow(scheduler::notifyEveryDay);
        verify(favoriteService).notifyUpcomingReleases();
    }
}
