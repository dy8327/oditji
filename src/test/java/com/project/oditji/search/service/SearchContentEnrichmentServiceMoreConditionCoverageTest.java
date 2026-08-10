package com.project.oditji.search.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * SearchContentEnrichmentService의 parsing helper 단축평가를 추가 보완합니다.
 */
class SearchContentEnrichmentServiceMoreConditionCoverageTest {

    private SearchContentEnrichmentService service;

    @BeforeEach
    void setUp() {
        service = new SearchContentEnrichmentService(
                mock(TmdbApiClient.class),
                mock(SearchContentManualOverrideService.class),
                mock(SearchContentAgeRatingResolver.class));
    }

    @Test
    void genreParserShouldCoverNullInvalidBlankDuplicateAndValidNames() {
        assertNull(
                invoke(
                        "parseGenreNames",
                        (Object) null));

        JSONArray genres =
                new JSONArray()
                        .put(JSONObject.NULL)
                        .put(new JSONObject().put("name", " "))
                        .put(new JSONObject().put("name", "드라마"))
                        .put(new JSONObject().put("name", "드라마"))
                        .put(new JSONObject().put("name", "액션"));

        assertEquals(
                "드라마, 액션",
                invoke(
                        "parseGenreNames",
                        genres));
    }

    @Test
    void directorAndCreatorParsersShouldCoverNullWrongJobBlankDuplicateAndValidRows() {
        JSONArray crew =
                new JSONArray()
                        .put(JSONObject.NULL)
                        .put(new JSONObject()
                                .put("job", "Producer")
                                .put("name", "제작자"))
                        .put(new JSONObject()
                                .put("job", "Director")
                                .put("name", " "))
                        .put(new JSONObject()
                                .put("job", "Director")
                                .put("name", "감독A"))
                        .put(new JSONObject()
                                .put("job", "director")
                                .put("name", "감독A"));

        assertEquals(
                "감독A",
                invoke(
                        "parseMovieDirector",
                        crew));

        JSONArray creators =
                new JSONArray()
                        .put(JSONObject.NULL)
                        .put(new JSONObject().put("name", " "))
                        .put(new JSONObject().put("name", "작가A"))
                        .put(new JSONObject().put("name", "작가A"))
                        .put(new JSONObject().put("name", "작가B"));

        assertEquals(
                "작가A, 작가B",
                invoke(
                        "parseTvCreator",
                        creators));
    }

    @Test
    void castParserShouldStopAtFiveUniqueNonBlankNames() {
        JSONArray cast =
                new JSONArray()
                        .put(JSONObject.NULL)
                        .put(new JSONObject().put("name", " "))
                        .put(new JSONObject().put("name", "A"))
                        .put(new JSONObject().put("name", "A"))
                        .put(new JSONObject().put("name", "B"))
                        .put(new JSONObject().put("name", "C"))
                        .put(new JSONObject().put("name", "D"))
                        .put(new JSONObject().put("name", "E"))
                        .put(new JSONObject().put("name", "F"));

        assertEquals(
                "A, B, C, D, E",
                invoke(
                        "parseCastNames",
                        cast));
    }

    @Test
    void providerAdderShouldCoverNullMapMissBlankAndValidPlatformKeys() {
        Set<String> keys =
                new LinkedHashSet<String>();

        invokeVoid(
                "addPlatformKeysByProviderId",
                null,
                Map.of(8, "netflix"),
                keys);

        assertTrue(keys.isEmpty());

        JSONArray providers =
                new JSONArray()
                        .put(JSONObject.NULL)
                        .put(new JSONObject().put("provider_id", 0))
                        .put(new JSONObject().put("provider_id", 9))
                        .put(new JSONObject().put("provider_id", 8));

        invokeVoid(
                "addPlatformKeysByProviderId",
                providers,
                Map.of(
                        9,
                        " ",
                        8,
                        "netflix"),
                keys);

        assertEquals(
                Set.of("netflix"),
                keys);
    }

    @Test
    void scalarHelpersShouldCoverNullBlankZeroNegativeAndPositiveValues() {
        assertNull(
                invoke(
                        "firstNonBlank",
                        (Object) null,
                        (Object) null));

        assertEquals(
                "second",
                invoke(
                        "firstNonBlank",
                        " ",
                        "second"));

        JSONObject json =
                new JSONObject()
                        .put("nil", JSONObject.NULL)
                        .put("blank", " ")
                        .put("zero", 0)
                        .put("negative", -1)
                        .put("positive", 7)
                        .put("score", 8.5);

        assertNull(
                invoke(
                        "nullableString",
                        json,
                        "nil"));
        assertNull(
                invoke(
                        "nullableString",
                        json,
                        "blank"));

        assertNull(
                invoke(
                        "nullablePositiveInteger",
                        json,
                        "zero"));
        assertNull(
                invoke(
                        "nullablePositiveInteger",
                        json,
                        "negative"));
        assertEquals(
                7,
                ((Integer)
                        invoke(
                                "nullablePositiveInteger",
                                json,
                                "positive"))
                        .intValue());

        assertEquals(
                8.5,
                (Double)
                        invoke(
                                "nullableDouble",
                                json,
                                "score"));
    }

    @SuppressWarnings("unchecked")
    private <T> T invoke(
            String methodName,
            Object... arguments) {

        return (T)
                ReflectionTestUtils.invokeMethod(
                        service,
                        methodName,
                        arguments);
    }

    private void invokeVoid(
            String methodName,
            Object... arguments) {

        ReflectionTestUtils.invokeMethod(
                service,
                methodName,
                arguments);
    }
}
