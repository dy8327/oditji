package com.project.oditji.search.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

/** 콘텐츠 상세 보강 서비스의 문자열/JSON helper 잔여 분기를 보완합니다. */
class SearchContentEnrichmentServiceFinalBranchCoverageTest {

    private SearchContentEnrichmentService service;

    @BeforeEach
    void setUp() {
        service = new SearchContentEnrichmentService(
                mock(TmdbApiClient.class),
                mock(SearchContentManualOverrideService.class),
                mock(SearchContentAgeRatingResolver.class));
    }

    @Test
    void textHelpersShouldCoverNullBlankAndPresentValues() {
        assertFalse((Boolean) invoke("hasText", new Object[] { null }));
        assertFalse((Boolean) invoke("hasText", "   "));
        assertTrue((Boolean) invoke("hasText", " value "));

        assertEquals("", invoke("normalizeSearchText", new Object[] { null }));
        assertEquals("닥터하얀마피아", invoke("normalizeSearchText", " 닥터: 하얀 마피아! "));
        assertEquals("", invoke("safeText", new Object[] { null }));
        assertEquals("text", invoke("safeText", "text"));

        assertEquals("first", invoke("firstNonBlank", "first", "second"));
        assertEquals("second", invoke("firstNonBlank", null, "second"));
        assertEquals("second", invoke("firstNonBlank", "   ", "second"));
    }

    @Test
    void nullableJsonHelpersShouldCoverMissingNullBlankZeroAndPresentValues() {
        JSONObject json = new JSONObject();
        json.put("nullValue", JSONObject.NULL);
        json.put("blank", "   ");
        json.put("text", "value");
        json.put("zero", 0);
        json.put("negative", -1);
        json.put("positive", 12);
        json.put("score", 8.25);

        assertNull(invoke("nullableString", json, "missing"));
        assertNull(invoke("nullableString", json, "nullValue"));
        assertNull(invoke("nullableString", json, "blank"));
        assertEquals("value", invoke("nullableString", json, "text"));

        assertNull(invoke("nullablePositiveInteger", json, "missing"));
        assertNull(invoke("nullablePositiveInteger", json, "nullValue"));
        assertNull(invoke("nullablePositiveInteger", json, "zero"));
        assertNull(invoke("nullablePositiveInteger", json, "negative"));
        assertEquals(Integer.valueOf(12), (Integer) invoke("nullablePositiveInteger", json, "positive"));

        assertNull(invoke("nullableDouble", json, "missing"));
        assertNull(invoke("nullableDouble", json, "nullValue"));
        assertEquals(Double.valueOf(8.25), (Double) invoke("nullableDouble", json, "score"));
    }

    @Test
    void tvLastAirDateShouldPreferLastEpisodeAndFallbackToLastAirDate() {
        assertNull(invoke("resolveTvLastAirDate", new Object[] { null }));

        JSONObject fallbackOnly = new JSONObject();
        fallbackOnly.put("last_air_date", "2026-08-01");
        assertEquals("2026-08-01", invoke("resolveTvLastAirDate", fallbackOnly));

        JSONObject blankEpisode = new JSONObject();
        blankEpisode.put("last_episode_to_air", new JSONObject().put("air_date", "   "));
        blankEpisode.put("last_air_date", "2026-08-02");
        assertEquals("2026-08-02", invoke("resolveTvLastAirDate", blankEpisode));

        JSONObject episodePreferred = new JSONObject();
        episodePreferred.put("last_episode_to_air", new JSONObject().put("air_date", "2026-08-10"));
        episodePreferred.put("last_air_date", "2026-08-02");
        assertEquals("2026-08-10", invoke("resolveTvLastAirDate", episodePreferred));
    }

    @Test
    void firstPositiveIntegerShouldCoverNullNoPositiveAndFirstPositive() {
        assertNull(invoke("parseFirstPositiveInteger", new Object[] { null }));
        assertNull(invoke("parseFirstPositiveInteger", new JSONArray().put(0).put(-1)));
        assertEquals(Integer.valueOf(7),
                (Integer) invoke("parseFirstPositiveInteger", new JSONArray().put(0).put(7).put(9)));
    }

    @Test
    void peopleParsersShouldSkipNullWrongRoleBlankAndDuplicates() {
        JSONArray crew = new JSONArray();
        crew.put(JSONObject.NULL);
        crew.put(new JSONObject().put("job", "Writer").put("name", "작가"));
        crew.put(new JSONObject().put("job", "Director").put("name", "   "));
        crew.put(new JSONObject().put("job", "Director").put("name", "감독A"));
        crew.put(new JSONObject().put("job", "director").put("name", "감독A"));
        assertEquals("감독A", invoke("parseMovieDirector", crew));
        assertNull(invoke("parseMovieDirector", new Object[] { null }));

        JSONArray creators = new JSONArray();
        creators.put(JSONObject.NULL);
        creators.put(new JSONObject().put("name", "   "));
        creators.put(new JSONObject().put("name", "크리에이터A"));
        creators.put(new JSONObject().put("name", "크리에이터A"));
        assertEquals("크리에이터A", invoke("parseTvCreator", creators));
        assertNull(invoke("parseTvCreator", new Object[] { null }));

        JSONArray cast = new JSONArray();
        cast.put(JSONObject.NULL);
        cast.put(new JSONObject().put("name", "   "));
        cast.put(new JSONObject().put("name", "배우A"));
        cast.put(new JSONObject().put("name", "배우A"));
        cast.put(new JSONObject().put("name", "배우B"));
        cast.put(new JSONObject().put("name", "배우C"));
        cast.put(new JSONObject().put("name", "배우D"));
        cast.put(new JSONObject().put("name", "배우E"));
        cast.put(new JSONObject().put("name", "배우F"));
        assertEquals("배우A, 배우B, 배우C, 배우D, 배우E", invoke("parseCastNames", cast));
        assertNull(invoke("parseCastNames", new Object[] { null }));
    }

    private Object invoke(String methodName, Object... arguments) {
        return ReflectionTestUtils.invokeMethod(service, methodName, arguments);
    }
}
