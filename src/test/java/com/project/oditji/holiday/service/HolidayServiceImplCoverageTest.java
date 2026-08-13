package com.project.oditji.holiday.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import tools.jackson.databind.json.JsonMapper;

/** 공휴일 API 응답 파싱, 캐시, 실패 보호 분기를 검증합니다. */
class HolidayServiceImplCoverageTest {

    private HttpServer server;
    private AtomicReference<String> responseBody;
    private AtomicInteger requestCount;
    private HolidayServiceImpl service;

    @BeforeEach
    void setUp() throws IOException {
        responseBody = new AtomicReference<>(successArrayResponse());
        requestCount = new AtomicInteger();

        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/getRestDeInfo", this::writeResponse);
        server.start();

        service = new HolidayServiceImpl(JsonMapper.builder().build());
        ReflectionTestUtils.setField(
                service,
                "baseUrl",
                "http://localhost:" + server.getAddress().getPort());
        ReflectionTestUtils.setField(service, "serviceKey", "test-service-key");
    }

    @AfterEach
    void tearDown() {
        if (server != null) {
            server.stop(0);
        }
    }

    @Test
    void getHolidaysByMonthShouldParseArrayFilterInvalidItemsAndUseCache() {
        Map<Integer, String> holidays = service.getHolidaysByMonth(2026, 8);

        assertEquals(3, holidays.size());
        assertEquals("광복절", holidays.get(15));
        assertEquals("대체공휴일", holidays.get(17));
        assertEquals("기본 공휴일", holidays.get(18));
        assertEquals(1, requestCount.get());

        responseBody.set(successSingleObjectResponse());

        Map<Integer, String> cached = service.getHolidaysByMonth(2026, 8);

        assertEquals(holidays, cached);
        assertEquals(1, requestCount.get());
    }

    @Test
    void getHolidaysByMonthShouldParseSingleObjectResponse() {
        responseBody.set(successSingleObjectResponse());

        Map<Integer, String> holidays = service.getHolidaysByMonth(2026, 9);

        assertEquals(Map.of(28, "단일 공휴일"), holidays);
        assertEquals(1, requestCount.get());
    }

    @Test
    void parseHolidayResponseShouldReturnEmptyForNullBlankErrorAndMissingItem() {
        assertTrue(parse(null).isEmpty());
        assertTrue(parse("   ").isEmpty());
        assertTrue(parse(errorResponse()).isEmpty());
        assertTrue(parse(successWithoutUsableItem()).isEmpty());
    }

    @Test
    void getHolidaysByMonthShouldReturnEmptyWhenJsonParsingFails() {
        responseBody.set("{not-json");

        Map<Integer, String> holidays = service.getHolidaysByMonth(2026, 10);

        assertTrue(holidays.isEmpty());
        assertEquals(1, requestCount.get());
    }

    @Test
    void getHolidaysByMonthShouldReturnEmptyWhenApiConnectionFails() {
        server.stop(0);
        server = null;

        Map<Integer, String> holidays = service.getHolidaysByMonth(2026, 11);

        assertTrue(holidays.isEmpty());
    }

    @SuppressWarnings("unchecked")
    private Map<Integer, String> parse(String body) {
        return (Map<Integer, String>) ReflectionTestUtils.invokeMethod(
                service,
                "parseHolidayResponse",
                body);
    }

    private void writeResponse(HttpExchange exchange) throws IOException {
        requestCount.incrementAndGet();
        byte[] body = responseBody.get().getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(200, body.length);
        exchange.getResponseBody().write(body);
        exchange.close();
    }

    private static String successArrayResponse() {
        return """
                {
                  "response": {
                    "header": {"resultCode": "00", "resultMsg": "NORMAL SERVICE."},
                    "body": {
                      "items": {
                        "item": [
                          {"isHoliday": "Y", "locdate": "20260815", "dateName": "광복절"},
                          {"isHoliday": "N", "locdate": "20260816", "dateName": "평일"},
                          {"isHoliday": "Y", "locdate": "20260817", "dateName": "대체공휴일"},
                          {"locdate": "20260818", "dateName": "기본 공휴일"},
                          {"isHoliday": "Y", "dateName": "날짜 없음"},
                          {"isHoliday": "Y", "locdate": "20260819"},
                          {"isHoliday": "Y", "locdate": "20260820", "dateName": "   "},
                          {"isHoliday": "Y", "locdate": "잘못된날짜", "dateName": "파싱 실패"},
                          {"isHoliday": "Y", "locdate": "20260815", "dateName": "중복 명칭"}
                        ]
                      }
                    }
                  }
                }
                """;
    }

    private static String successSingleObjectResponse() {
        return """
                {
                  "response": {
                    "header": {"resultCode": "00"},
                    "body": {
                      "items": {
                        "item": {"isHoliday": "Y", "locdate": "20260928", "dateName": "단일 공휴일"}
                      }
                    }
                  }
                }
                """;
    }

    private static String errorResponse() {
        return """
                {
                  "response": {
                    "header": {"resultCode": "30", "resultMsg": "SERVICE KEY IS NOT REGISTERED"}
                  }
                }
                """;
    }

    private static String successWithoutUsableItem() {
        return """
                {
                  "response": {
                    "header": {"resultCode": "00"},
                    "body": {"items": {"item": null}}
                  }
                }
                """;
    }
}
