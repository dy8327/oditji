package com.project.oditji.subscription.scheduler;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.project.oditji.subscription.service.SubscriptionCalculatorService;

/** 만료 구독 결과 정리 스케줄러의 시작/정기 실행 및 예외 방어 분기를 검증합니다. */
@ExtendWith(MockitoExtension.class)
class SubscriptionResultCleanupSchedulerCoverageTest {

    @Mock
    private SubscriptionCalculatorService subscriptionCalculatorService;

    private SubscriptionResultCleanupScheduler scheduler;

    @BeforeEach
    void setUp() {
        scheduler = new SubscriptionResultCleanupScheduler(subscriptionCalculatorService);
    }

    @Test
    void cleanupOnApplicationReadyShouldDeleteExpiredResultsWhenRowsExist() {
        when(subscriptionCalculatorService.deleteExpiredResults()).thenReturn(2);

        scheduler.cleanupOnApplicationReady();

        verify(subscriptionCalculatorService).deleteExpiredResults();
    }

    @Test
    void cleanupEveryDayShouldAlsoHandleZeroDeletedRows() {
        when(subscriptionCalculatorService.deleteExpiredResults()).thenReturn(0);

        scheduler.cleanupEveryDay();

        verify(subscriptionCalculatorService).deleteExpiredResults();
    }

    @Test
    void cleanupShouldSwallowServiceExceptionSoSchedulerKeepsRunning() {
        when(subscriptionCalculatorService.deleteExpiredResults())
                .thenThrow(new IllegalStateException("cleanup-test"));

        scheduler.cleanupEveryDay();

        verify(subscriptionCalculatorService).deleteExpiredResults();
    }
}
