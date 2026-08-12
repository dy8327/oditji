package com.project.oditji.holiday.service;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.InetSocketAddress;
import java.util.Map;

import com.sun.net.httpserver.HttpServer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.test.util.ReflectionTestUtils;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import tools.jackson.databind.json.JsonMapper;

/** 공휴일 서비스 WARN 로그 guard의 비활성(false) 분기를 검증합니다. */
class HolidayServiceImplLoggingGuardCoverageTest {

    private HolidayServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new HolidayServiceImpl(JsonMapper.builder().build());
    }

    @Test
    void apiFailureShouldRemainSafeWhenWarnLoggingIsDisabled() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);
        server.start();
        int closedPort = server.getAddress().getPort();
        server.stop(0);

        ReflectionTestUtils.setField(
                service,
                "baseUrl",
                "http://localhost:" + closedPort);
        ReflectionTestUtils.setField(service, "serviceKey", "test-key");

        Map<Integer, String> result = withWarnLoggingDisabled(
                () -> service.getHolidaysByMonth(2026, 12));

        assertTrue(result.isEmpty());
    }

    @Test
    void errorResultCodeShouldRemainSafeWhenWarnLoggingIsDisabled() {
        String body = """
                {
                  "response": {
                    "header": {
                      "resultCode": "30",
                      "resultMsg": "SERVICE KEY ERROR"
                    }
                  }
                }
                """;

        Map<Integer, String> result = withWarnLoggingDisabled(
                () -> parse(body));

        assertTrue(result.isEmpty());
    }

    @Test
    void invalidHolidayDateShouldRemainSafeWhenWarnLoggingIsDisabled() {
        String body = """
                {
                  "response": {
                    "header": {"resultCode": "00"},
                    "body": {
                      "items": {
                        "item": {
                          "isHoliday": "Y",
                          "locdate": "not-a-date",
                          "dateName": "잘못된 날짜"
                        }
                      }
                    }
                  }
                }
                """;

        Map<Integer, String> result = withWarnLoggingDisabled(
                () -> parse(body));

        assertTrue(result.isEmpty());
    }

    @SuppressWarnings("unchecked")
    private Map<Integer, String> parse(String body) {
        return (Map<Integer, String>) ReflectionTestUtils.invokeMethod(
                service,
                "parseHolidayResponse",
                body);
    }

    private <T> T withWarnLoggingDisabled(java.util.function.Supplier<T> action) {
        Logger logger = (Logger) LoggerFactory.getLogger(HolidayServiceImpl.class);
        Level originalLevel = logger.getLevel();

        try {
            logger.setLevel(Level.OFF);
            return action.get();
        } finally {
            logger.setLevel(originalLevel);
        }
    }
}
