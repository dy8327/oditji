package com.project.oditji.tmdb.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;

import java.time.LocalDate;
import java.time.Month;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.project.oditji.tmdb.dao.TmdbDAO;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/** TMDB 서비스의 날짜·등급·nullable helper 잔여 조건을 보완합니다. */
class TmdbServiceImplFinalPureHelperCoverageTest {

    private JsonMapper mapper;
    private TmdbServiceImpl service;

    @BeforeEach
    void setUp() {
        mapper = JsonMapper.builder().build();
        service = new TmdbServiceImpl(mock(TmdbDAO.class), mapper);
    }

    @Test
    void dateAndLengthHelpersShouldCoverNullBlankInvalidTrimAndTruncation() {
        assertNull(invoke("parseDate", (Object) null));
        assertNull(invoke("parseDate", " "));
        assertNull(invoke("parseDate", "invalid"));
        assertEquals(LocalDate.of(2026, Month.AUGUST, 11), invoke("parseDate", "2026-08-11"));

        assertNull(invoke("limitLength", null, 3));
        assertEquals("abc", invoke("limitLength", " abc ", 5));
        assertEquals("abc", invoke("limitLength", "abcdef", 3));
    }

    @Test
    void koreanAndUsAgeRatingConvertersShouldCoverBlankUnsupportedAndMappedValues() {
        assertNull(invoke("convertAgeRating", "KR", null, false));
        assertNull(invoke("convertAgeRating", "KR", " ", false));
        assertNull(invoke("convertAgeRating", "JP", "PG12", false));
        assertEquals("ALL", invoke("convertAgeRating", "KR", "전체 관람가", false));
        assertEquals("ALL", invoke("convertAgeRating", "KR", "7세", false));
        assertEquals("12", invoke("convertAgeRating", "KR", "12", false));
        assertEquals("15", invoke("convertAgeRating", "KR", "15", false));
        assertEquals("18", invoke("convertAgeRating", "KR", "청소년 관람불가", false));
        assertEquals("UNKNOWN", invoke("convertAgeRating", "KR", "UNKNOWN", false));
        assertEquals("15", invoke("convertAgeRating", "US", "TV-14", true));
        assertEquals("15", invoke("convertAgeRating", "US", "PG-13", false));
        assertEquals("UNKNOWN", invoke("convertAgeRating", "US", "XYZ", false));
    }

    @Test
    void nullableHelpersShouldCoverMissingNullZeroPositiveAndDoubleValues() throws Exception {
        JsonNode missing = mapper.readTree("{}").path("missing");
        JsonNode nil = mapper.readTree("null");
        JsonNode zero = mapper.readTree("0");
        JsonNode positive = mapper.readTree("12");
        JsonNode score = mapper.readTree("8.5");

        assertNull(invoke("nullableInt", (Object) null));
        assertNull(invoke("nullableInt", missing));
        assertNull(invoke("nullableInt", nil));
        assertNull(invoke("nullableInt", zero));
        assertEquals(12, ((Integer) invoke("nullableInt", positive)).intValue());
        assertNull(invoke("nullableDouble", (Object) null));
        assertNull(invoke("nullableDouble", missing));
        assertNull(invoke("nullableDouble", nil));
        assertEquals(8.5, ((Double) invoke("nullableDouble", score)).doubleValue());
    }

    @Test
    void firstNonBlankShouldCoverNullBlankAndFirstValue() {
        assertEquals("second", invoke("firstNonBlank", null, "second"));
        assertEquals("second", invoke("firstNonBlank", " ", "second"));
        assertEquals("first", invoke("firstNonBlank", "first", "second"));
    }

    @SuppressWarnings("unchecked")
    private <T> T invoke(String method, Object... args) {
        return (T) ReflectionTestUtils.invokeMethod(service, method, args);
    }
}
