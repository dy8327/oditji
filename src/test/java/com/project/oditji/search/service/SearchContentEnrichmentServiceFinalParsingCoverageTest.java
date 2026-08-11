package com.project.oditji.search.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.project.oditji.search.vo.CachedContentVO;

/** 검색 상세 보강 서비스의 JSON parsing helper 잔여 조건을 보완합니다. */
class SearchContentEnrichmentServiceFinalParsingCoverageTest {

    private SearchContentEnrichmentService service;

    @BeforeEach
    void setUp() {
        service = new SearchContentEnrichmentService(
                mock(TmdbApiClient.class),
                mock(SearchContentManualOverrideService.class),
                mock(SearchContentAgeRatingResolver.class));
    }

    @Test
    void genreDirectorCreatorAndCastParsersShouldIgnoreNullObjectsBlankAndDuplicates() {
        assertNull(invoke("parseGenreNames", (Object) null));
        JSONArray genres = new JSONArray()
                .put(JSONObject.NULL)
                .put(new JSONObject().put("name", " "))
                .put(new JSONObject().put("name", "드라마"))
                .put(new JSONObject().put("name", "드라마"));
        assertEquals("드라마", invoke("parseGenreNames", genres));

        assertNull(invoke("parseMovieDirector", (Object) null));
        JSONArray crew = new JSONArray()
                .put(JSONObject.NULL)
                .put(new JSONObject().put("job", "Writer").put("name", "작가"))
                .put(new JSONObject().put("job", "director").put("name", "감독"))
                .put(new JSONObject().put("job", "Director").put("name", "감독"));
        assertEquals("감독", invoke("parseMovieDirector", crew));

        JSONArray creators = new JSONArray()
                .put(JSONObject.NULL)
                .put(new JSONObject().put("name", " "))
                .put(new JSONObject().put("name", "크리에이터"));
        assertEquals("크리에이터", invoke("parseTvCreator", creators));

        JSONArray cast = new JSONArray();
        for (int index = 0; index < 7; index++) {
            cast.put(new JSONObject().put("name", "배우" + index));
        }
        String names = invoke("parseCastNames", cast);
        assertEquals(5, names.split(", ").length);
    }

    @Test
    void primitiveHelpersShouldCoverMissingNullZeroPositiveAndFallbackValues() {
        JSONObject json = new JSONObject()
                .put("nil", JSONObject.NULL)
                .put("blank", " ")
                .put("zero", 0)
                .put("positive", 12)
                .put("score", 8.5);
        assertNull(invoke("nullableString", json, "missing"));
        assertNull(invoke("nullableString", json, "nil"));
        assertNull(invoke("nullableString", json, "blank"));
        assertNull(invoke("nullablePositiveInteger", json, "zero"));
        assertEquals(
                12,
                ((Integer) invoke("nullablePositiveInteger", json, "positive")).intValue());
        assertEquals(
                8.5,
                ((Double) invoke("nullableDouble", json, "score")).doubleValue());

        assertNull(invoke("parseFirstPositiveInteger", (Object) null));
        assertNull(invoke("parseFirstPositiveInteger", new JSONArray().put(0).put(-1)));
        assertEquals(
                7,
                ((Integer) invoke(
                        "parseFirstPositiveInteger",
                        new JSONArray().put(0).put(7).put(9))).intValue());

        assertEquals("second", invoke("firstNonBlank", null, "second"));
        assertEquals("second", invoke("firstNonBlank", " ", "second"));
        assertEquals("first", invoke("firstNonBlank", "first", "second"));
        assertEquals("", invoke("normalizeSearchText", (Object) null));
        assertEquals("", invoke("safeText", (Object) null));
    }

    @Test
    void tvLastAirDateShouldPreferLastEpisodeAndFallbackToLastAirDate() {
        assertNull(invoke("resolveTvLastAirDate", (Object) null));
        JSONObject detail = new JSONObject().put("last_air_date", "2026-08-01");
        assertEquals("2026-08-01", invoke("resolveTvLastAirDate", detail));

        detail.put("last_episode_to_air", new JSONObject().put("air_date", "2026-08-11"));
        assertEquals("2026-08-11", invoke("resolveTvLastAirDate", detail));
    }

    @Test
    void searchTextShouldSafelyCombineNullableContentFields() {
        CachedContentVO content = new CachedContentVO();
        content.setTitle(" 닥터 X ");
        content.setOriginalTitle(null);
        content.setDirector("홍 길동");
        content.setCastNames(null);
        String searchText = service.createSearchText(content);
        assertTrue(searchText.contains("닥터x"));
        assertTrue(searchText.contains("홍길동"));
    }

    @SuppressWarnings("unchecked")
    private <T> T invoke(String method, Object... args) {
        return (T) ReflectionTestUtils.invokeMethod(service, method, args);
    }
}
