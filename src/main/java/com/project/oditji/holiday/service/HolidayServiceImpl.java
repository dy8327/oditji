package com.project.oditji.holiday.service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * 한국천문연구원(공공데이터포털) 특일 정보제공 서비스(SpcdeInfoService)의
 * "공휴일 정보 조회(getRestDeInfo)" 오퍼레이션을 호출해 공휴일 정보를 제공합니다.
 *
 * 서비스 결과는 연·월 단위로 메모리에 캐싱합니다. 특일 정보는 연 1회 갱신되는
 * 정적인 데이터이므로, 같은 달을 반복 조회할 때마다 외부 API를 다시 호출할
 * 필요가 없습니다(서버가 떠 있는 동안 유지되는 단순 캐시입니다).
 */
@Service
public class HolidayServiceImpl implements HolidayService {

    private static final Logger log = LoggerFactory.getLogger(HolidayServiceImpl.class);

    private static final DateTimeFormatter LOCDATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyyMMdd");

    private static final String HOLIDAY_FLAG_YES = "Y";
    private static final String RESULT_CODE_SUCCESS = "00";

    private final RestClient restClient;
    private final JsonMapper jsonMapper;

    /**
     * 공공데이터포털 특일 정보제공 서비스(SpcdeInfoService) 기본 URL입니다.
     * application.properties의 holiday.api.base-url로 설정합니다.
     */
    @Value("${holiday.api.base-url}")
    private String baseUrl;

    /**
     * 공공데이터포털에서 발급받은 서비스키(디코딩 키)입니다.
     * RestClient가 쿼리 파라미터로 넣을 때 URL 인코딩을 한 번 더 수행하므로,
     * 포털에서 "Encoding" 처리된 키가 아닌 "Decoding" 키를 사용해야 합니다.
     */
    @Value("${holiday.api.service-key}")
    private String serviceKey;

    private final Map<YearMonth, Map<Integer, String>> holidayCache =
            new ConcurrentHashMap<>();

    public HolidayServiceImpl(JsonMapper jsonMapper) {
        this.restClient = RestClient.create();
        this.jsonMapper = jsonMapper;
    }

    @Override
    public Map<Integer, String> getHolidaysByMonth(int year, int month) {

        YearMonth targetYearMonth = YearMonth.of(year, month);

        return holidayCache.computeIfAbsent(
                targetYearMonth,
                key -> fetchHolidaysByMonth(
                        key.getYear(),
                        key.getMonthValue()));
    }

    /**
     * 실제 API 호출을 담당합니다.
     *
     * 서비스키 미설정, 네트워크 오류, 응답 형식 오류 등 어떤 이유로든 조회에
     * 실패해도 예외를 밖으로 던지지 않고 빈 맵을 반환해, 공휴일 표시 실패가
     * 캘린더 화면 전체를 깨뜨리지 않도록 합니다.
     */
    private Map<Integer, String> fetchHolidaysByMonth(int year, int month) {

        try {

            String responseBody =
                    restClient.get()
                            .uri(baseUrl
                                    + "/getRestDeInfo"
                                    + "?solYear={solYear}"
                                    + "&solMonth={solMonth}"
                                    + "&ServiceKey={serviceKey}"
                                    + "&_type=json"
                                    + "&numOfRows=50",
                                    year,
                                    String.format("%02d", month),
                                    serviceKey)
                            .retrieve()
                            .body(String.class);

            return parseHolidayResponse(responseBody);

        } catch (Exception e) {

            if (log.isWarnEnabled()) {

                log.warn(
                        "[공휴일 정보 조회 실패] {}년 {}월 - 공휴일 표시 없이 캘린더를 보여줍니다.",
                        year,
                        month,
                        e);
            }

            return Collections.emptyMap();
        }
    }

    private Map<Integer, String> parseHolidayResponse(String responseBody) {

        if (responseBody == null
                || responseBody.isBlank()) {

            return Collections.emptyMap();
        }

        JsonNode root = jsonMapper.readTree(responseBody);

        JsonNode header = root.path("response").path("header");
        String resultCode = header.path("resultCode").asString("");

        if (!RESULT_CODE_SUCCESS.equals(resultCode)) {

            if (log.isWarnEnabled()) {

                log.warn(
                        "[공휴일 정보 조회 실패] resultCode={}, resultMsg={}",
                        resultCode,
                        header.path("resultMsg").asString(""));
            }

            return Collections.emptyMap();
        }

        JsonNode itemNode = root.path("response")
                .path("body")
                .path("items")
                .path("item");

        // 일(day) 기준 오름차순으로 정렬해, 화면(공휴일 요약 문구 등)에서
        // 별도 정렬 없이 바로 사용할 수 있도록 합니다.
        Map<Integer, String> holidayByDay = new TreeMap<>();

        // 공공데이터포털 API 특성상 결과가 1건이면 item이 배열이 아닌
        // 단일 객체로 내려오는 경우가 있어 두 형태를 모두 처리합니다.
        if (itemNode.isArray()) {

            for (JsonNode item : itemNode) {
                addHolidayIfPresent(holidayByDay, item);
            }

        } else if (itemNode.isObject()) {

            addHolidayIfPresent(holidayByDay, itemNode);
        }

        return holidayByDay;
    }

    private void addHolidayIfPresent(
            Map<Integer, String> holidayByDay,
            JsonNode item) {

        String isHoliday = item.path("isHoliday").asString(HOLIDAY_FLAG_YES);

        if (!HOLIDAY_FLAG_YES.equals(isHoliday)) {
            return;
        }

        String locdate = item.path("locdate").asString(null);
        String dateName = item.path("dateName").asString(null);

        if (locdate == null
                || dateName == null
                || dateName.isBlank()) {

            return;
        }

        try {

            LocalDate date = LocalDate.parse(locdate, LOCDATE_FORMATTER);

            // 같은 날짜에 항목이 두 개 이상 내려오는 경우 먼저 들어온 명칭을 사용합니다.
            holidayByDay.putIfAbsent(date.getDayOfMonth(), dateName);

        } catch (DateTimeParseException e) {

            if (log.isWarnEnabled()) {
                log.warn("[공휴일 정보 조회] 날짜 형식 파싱 실패: {}", locdate);
            }
        }
    }
}
