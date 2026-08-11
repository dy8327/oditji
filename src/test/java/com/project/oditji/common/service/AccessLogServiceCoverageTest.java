package com.project.oditji.common.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.project.oditji.common.dao.AccessLogDAO;
import com.project.oditji.common.vo.AccessLogVO;

/** 비동기 접속 로그 저장 서비스의 성공 및 예외 흡수 경로를 검증합니다. */
class AccessLogServiceCoverageTest {

    private AccessLogDAO accessLogDAO;
    private AccessLogService service;

    @BeforeEach
    void setUp() {
        accessLogDAO = mock(AccessLogDAO.class);
        service = new AccessLogService(accessLogDAO);
    }

    @Test
    void saveShouldDelegateToDao() {
        AccessLogVO accessLog = new AccessLogVO();

        service.saveAccessLogAsync(accessLog);

        verify(accessLogDAO).insertAccessLog(accessLog);
    }

    @Test
    void saveShouldSwallowDaoException() {
        AccessLogVO accessLog = new AccessLogVO();
        doThrow(new IllegalStateException("db failure"))
                .when(accessLogDAO)
                .insertAccessLog(accessLog);

        assertDoesNotThrow(
                () -> service.saveAccessLogAsync(accessLog));
        verify(accessLogDAO).insertAccessLog(accessLog);
    }
}
