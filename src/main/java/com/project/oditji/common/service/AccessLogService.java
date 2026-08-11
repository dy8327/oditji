package com.project.oditji.common.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.project.oditji.common.dao.AccessLogDAO;
import com.project.oditji.common.vo.AccessLogVO;

@Service
public class AccessLogService {

    private static final Logger log =
            LoggerFactory.getLogger(AccessLogService.class);

    private final AccessLogDAO accessLogDAO;

    public AccessLogService(AccessLogDAO accessLogDAO) {
        this.accessLogDAO = accessLogDAO;
    }

    @Async("accessLogExecutor")
    public void saveAccessLogAsync(AccessLogVO accessLog) {

        try {
            accessLogDAO.insertAccessLog(accessLog);
        } catch (Exception e) {
            log.warn("비동기 접속 로그 저장 실패", e);
        }
    }
}