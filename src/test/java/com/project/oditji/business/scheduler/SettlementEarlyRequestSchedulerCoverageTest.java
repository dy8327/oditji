package com.project.oditji.business.scheduler;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;

import com.project.oditji.business.service.BusinessService;

/** 사전 정산 자동 확정 스케줄러가 서비스 로직을 한 번 위임하는지 검증합니다. */
class SettlementEarlyRequestSchedulerCoverageTest {

    @Test
    void finalizeEarlySettlementRequestsShouldDelegateToBusinessService() {
        BusinessService businessService = mock(BusinessService.class);
        SettlementEarlyRequestScheduler scheduler =
                new SettlementEarlyRequestScheduler(businessService);

        scheduler.finalizeEarlySettlementRequests();

        verify(businessService).finalizeEarlySettlementRequests();
    }
}
