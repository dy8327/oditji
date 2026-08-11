package com.project.oditji.common.interceptor;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import com.project.oditji.common.dao.AccessLogDAO;
import com.project.oditji.common.vo.AccessLogVO;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

/** AccessLogInterceptor의 WARN 비활성화 분기를 검증합니다. */
class AccessLogInterceptorWarnDisabledCoverageTest {

    @Test
    void daoFailureShouldStillContinueWhenWarnLoggingIsDisabled() {
        AccessLogDAO accessLogDAO = mock(AccessLogDAO.class);
        doThrow(new IllegalStateException("db"))
                .when(accessLogDAO)
                .insertAccessLog(any(AccessLogVO.class));

        AccessLogInterceptor interceptor = new AccessLogInterceptor(accessLogDAO);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod("GET");
        request.setRequestURI("/coverage/access-log");
        request.setRemoteAddr("127.0.0.1");

        Logger logger = (Logger) LoggerFactory.getLogger(AccessLogInterceptor.class);
        Level originalLevel = logger.getLevel();
        try {
            logger.setLevel(Level.OFF);
            assertTrue(interceptor.preHandle(
                    request,
                    new MockHttpServletResponse(),
                    new Object()));
        } finally {
            logger.setLevel(originalLevel);
        }
    }
}
