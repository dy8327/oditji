package com.project.oditji.business.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.project.oditji.business.dao.BusinessDAO;

/*
 * =========================================================
 * 이벤트 종료 상태 자동 전환 스케줄러
 *
 * EVENT.END_DATE가 지났는데도 STATUS가 'APPROVED'로
 * 남아있는 이벤트를 매일 배치로 'END'로 전환한다.
 *
 * (참고) businessmapper.xml의 상품검색/중복연결 검증 쿼리는
 * END_DATE를 직접 확인하도록 이미 방어 처리되어 있어
 * 이 배치가 지연되더라도 상품 재사용이 막히지는 않는다.
 * 이 스케줄러는 화면/목록에 표시되는 STATUS 값 자체를
 * 실제 상태와 맞추기 위한 용도이다.
 * =========================================================
 */
@Component
public class EventStatusScheduler {

    private static final Logger log = LoggerFactory.getLogger(EventStatusScheduler.class);

    private final BusinessDAO businessDAO;

    public EventStatusScheduler(BusinessDAO businessDAO) {
        this.businessDAO = businessDAO;
    }

    // 매일 00:05에 실행
    @Scheduled(cron = "0 5 0 * * *")
    public void endExpiredEvents() {

        int updatedCount = businessDAO.updateExpiredEventStatus();

        if (updatedCount > 0) {
            log.info("[EventStatusScheduler] 종료 처리된 이벤트 수: {}", updatedCount);
        }
    }
}
