package com.project.oditji.common.util;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;

/**
 * 프로젝트 전역 날짜·시간 처리 유틸리티입니다.
 *
 * Oracle DATE 컬럼은 시간대 정보가 없으므로 애플리케이션 내부에서는
 * LocalDate 또는 LocalDateTime으로 전달하고, 현재 시각과 epoch 변환이
 * 필요한 지점에서만 한국 표준시를 명시합니다.
 */
public final class DateTimeUtil {

    public static final ZoneId KOREA_ZONE = ZoneId.of("Asia/Seoul");

    private DateTimeUtil() {
    }

    /**
     * JSP EL 함수에서 LocalDate/LocalDateTime 값을 지정 형식으로 출력합니다.
     *
     * @param value 날짜 또는 날짜·시간 값
     * @param pattern DateTimeFormatter 패턴
     * @return 형식화된 문자열. 값이 null이면 빈 문자열
     */
    public static String format(Object value, String pattern) {
        if (value == null) {
            return "";
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);

        if (value instanceof TemporalAccessor temporal) {
            return formatter.format(temporal);
        }

        throw new IllegalArgumentException(
                "지원하지 않는 날짜·시간 형식입니다: " + value.getClass().getName());
    }

    /**
     * 시간대가 없는 LocalDateTime을 한국 표준시 기준 epoch millisecond로 변환합니다.
     */
    public static long toEpochMilli(LocalDateTime value) {
        if (value == null) {
            return 0L;
        }

        return value.atZone(KOREA_ZONE).toInstant().toEpochMilli();
    }

    /**
     * 지원하는 java.time 값을 한국 표준시 기준 LocalDateTime으로 변환합니다.
     */
    public static LocalDateTime toLocalDateTime(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof LocalDateTime localDateTime) {
            return localDateTime;
        }
        if (value instanceof LocalDate localDate) {
            return localDate.atStartOfDay();
        }
        if (value instanceof OffsetDateTime offsetDateTime) {
            return offsetDateTime.atZoneSameInstant(KOREA_ZONE).toLocalDateTime();
        }
        if (value instanceof ZonedDateTime zonedDateTime) {
            return zonedDateTime.withZoneSameInstant(KOREA_ZONE).toLocalDateTime();
        }
        if (value instanceof Instant instant) {
            return LocalDateTime.ofInstant(instant, KOREA_ZONE);
        }

        throw new IllegalArgumentException(
                "지원하지 않는 날짜·시간 형식입니다: " + value.getClass().getName());
    }
}
