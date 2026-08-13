package com.project.oditji.common.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import org.junit.jupiter.api.Test;

class DateTimeUtilCoverageTest {

    @Test
    void formatShouldSupportDateAndDateTimeValues() {
        assertEquals(
                "2026-08-05",
                DateTimeUtil.format(LocalDate.of(2026, Month.AUGUST, 5), "yyyy-MM-dd"));
        assertEquals(
                "2026-08-05 16:20",
                DateTimeUtil.format(
                        LocalDateTime.of(2026, Month.AUGUST, 5, 16, 20),
                        "yyyy-MM-dd HH:mm"));
        assertEquals("", DateTimeUtil.format(null, "yyyy-MM-dd"));
    }

    @Test
    void epochConversionShouldUseKoreaTimeAndHandleNull() {
        LocalDateTime koreaEpoch = LocalDateTime.of(1970, Month.JANUARY, 1, 9, 0);

        assertEquals(0L, DateTimeUtil.toEpochMilli(null));
        assertEquals(0L, DateTimeUtil.toEpochMilli(koreaEpoch));
    }

    @Test
    void toLocalDateTimeShouldSupportJavaTimeTypes() {
        LocalDateTime expected = LocalDateTime.of(2026, Month.AUGUST, 5, 16, 20);
        Instant instant = expected.atZone(DateTimeUtil.KOREA_ZONE).toInstant();

        assertEquals(expected, DateTimeUtil.toLocalDateTime(expected));
        assertEquals(
                LocalDate.of(2026, Month.AUGUST, 5).atStartOfDay(),
                DateTimeUtil.toLocalDateTime(LocalDate.of(2026, Month.AUGUST, 5)));
        assertEquals(
                expected,
                DateTimeUtil.toLocalDateTime(
                        OffsetDateTime.of(expected, ZoneOffset.ofHours(9))));
        assertEquals(
                expected,
                DateTimeUtil.toLocalDateTime(
                        ZonedDateTime.of(expected, DateTimeUtil.KOREA_ZONE)));
        assertEquals(expected, DateTimeUtil.toLocalDateTime(instant));
        assertNull(DateTimeUtil.toLocalDateTime(null));
    }

    @Test
    void unsupportedValuesShouldFailClearly() {
        assertThrows(
                IllegalArgumentException.class,
                () -> DateTimeUtil.format("2026-08-05", "yyyy-MM-dd"));
        assertThrows(
                IllegalArgumentException.class,
                () -> DateTimeUtil.toLocalDateTime("2026-08-05"));
    }
}
