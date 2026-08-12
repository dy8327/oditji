package com.project.oditji.business.scheduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.project.oditji.business.service.BusinessService;

/**
 * =========================================================
 * [사전 정산 요청 자동 확정 추가]
 * 다음 달 정산을 미리 요청한 건을 정산월 도래 후 실제 정산 요청으로 확정한다.
 *
 * - 매일 00:05에 실행한다.
 * - 월 1일 실행 시점을 놓쳐도 이후 실행에서 이미 도래한 PRE_REQUESTED 건을 처리한다.
 * - 실제 확정 로직은 BusinessService에 두어 트랜잭션 경계를 유지한다.
 * =========================================================
 */
@Component
public class SettlementEarlyRequestScheduler {

    private final BusinessService businessService;

    public SettlementEarlyRequestScheduler(BusinessService businessService) {
        this.businessService = businessService;
    }

    @Scheduled(cron = "0 5 0 * * *", zone = "Asia/Seoul")
    public void finalizeEarlySettlementRequests() {
        businessService.finalizeEarlySettlementRequests();
    }
}