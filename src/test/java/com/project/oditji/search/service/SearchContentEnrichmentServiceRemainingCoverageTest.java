package com.project.oditji.search.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

import com.project.oditji.search.vo.CachedContentVO;

/**
 * 콘텐츠 상세 보강 서비스의 JSON 파싱 및 null 방어 helper 분기를 보완합니다.
 */
class SearchContentEnrichmentServiceRemainingCoverageTest {

    private SearchContentEnrichmentService service;

    @BeforeEach
    void setUp() {
        service = new SearchContentEnrichmentService(
                Mockito.mock(TmdbApiClient.class),
                Mockito.mock(SearchContentManualOverrideService.class),
                Mockito.mock(SearchContentAgeRatingResolver.class));
    }

    @Test
    void copyReusableDetailShouldHandleNullArgumentsAndNullPlatformBackingField() {
        CachedContentVO source = new CachedContentVO();
        source.setTitle("제목");
        ReflectionTestUtils.setField(
                source,
                "platformKeys",
                null);

        CachedContentVO target = new CachedContentVO();

        service.copyReusableDetail(null, target);
        service.copyReusableDetail(source, null);
        service.copyReusableDetail(source, target);

        assertEquals("제목", target.getTitle());
        assertTrue(target.getPlatformKeys().isEmpty());
    }

    @Test
    void emptyBatchShouldCoverWorkerNormalizationAtBothBounds() {
        TmdbProviderRegistry registry =
                Mockito.mock(TmdbProviderRegistry.class);

        ReflectionTestUtils.setField(service, "workerCount", 0);
        assertTrue(service.enrichBatch(
                List.of(),
                registry).isEmpty());

        ReflectionTestUtils.setField(service, "workerCount", 99);
        assertTrue(service.enrichBatch(
                List.of(),
                registry).isEmpty());
    }

    @Test
    void genreParserShouldSkipNullBlankAndDuplicateNames() {
        JSONArray genres = new JSONArray()
                .put(JSONObject.NULL)
                .put(new JSONObject().put("name", " "))
                .put(new JSONObject().put("name", "액션"))
                .put(new JSONObject().put("name", "액션"))
                .put(new JSONObject().put("name", "드라마"));

        String parsed = ReflectionTestUtils.invokeMethod(
                service,
                "parseGenreNames",
                genres);

        assertEquals("액션, 드라마", parsed);

        String nullResult = ReflectionTestUtils.invokeMethod(
                service,
                "parseGenreNames",
                (Object) null);
        assertNull(nullResult);
    }

    @Test
    void directorCreatorAndCastParsersShouldCoverInvalidDuplicateAndLimitBranches() {
        JSONArray crew = new JSONArray()
                .put(JSONObject.NULL)
                .put(new JSONObject()
                        .put("job", "Producer")
                        .put("name", "제작자"))
                .put(new JSONObject()
                        .put("job", "Director")
                        .put("name", " "))
                .put(new JSONObject()
                        .put("job", "director")
                        .put("name", "감독"))
                .put(new JSONObject()
                        .put("job", "Director")
                        .put("name", "감독"));

        assertEquals(
                "감독",
                ReflectionTestUtils.invokeMethod(
                        service,
                        "parseMovieDirector",
                        crew));

        JSONArray creators = new JSONArray()
                .put(JSONObject.NULL)
                .put(new JSONObject().put("name", " "))
                .put(new JSONObject().put("name", "크리에이터"))
                .put(new JSONObject().put("name", "크리에이터"));

        assertEquals(
                "크리에이터",
                ReflectionTestUtils.invokeMethod(
                        service,
                        "parseTvCreator",
                        creators));

        JSONArray cast = new JSONArray()
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
                ReflectionTestUtils.invokeMethod(
                        service,
                        "parseCastNames",
                        cast));

        assertNull(ReflectionTestUtils.invokeMethod(
                service,
                "parseMovieDirector",
                (Object) null));
        assertNull(ReflectionTestUtils.invokeMethod(
                service,
                "parseTvCreator",
                (Object) null));
        assertNull(ReflectionTestUtils.invokeMethod(
                service,
                "parseCastNames",
                (Object) null));
    }

    @Test
    void platformHelperShouldIgnoreNullUnsupportedBlankAndDuplicateProviders() {
        Map<Integer, String> supported = Map.of(
                8,
                "Netflix",
                9,
                " ");

        JSONArray providers = new JSONArray()
                .put(JSONObject.NULL)
                .put(new JSONObject().put("provider_id", 999))
                .put(new JSONObject().put("provider_id", 9))
                .put(new JSONObject().put("provider_id", 8))
                .put(new JSONObject().put("provider_id", 8));

        Set<String> keys = new LinkedHashSet<String>();

        ReflectionTestUtils.invokeMethod(
                service,
                "addPlatformKeysByProviderId",
                providers,
                supported,
                keys);

        assertEquals(
                Set.of("Netflix"),
                keys);

        ReflectionTestUtils.invokeMethod(
                service,
                "addPlatformKeysByProviderId",
                null,
                supported,
                keys);

        assertEquals(1, keys.size());
    }

    @Test
    void textHelpersShouldCoverNullBlankAndFallbackValues() {
        assertEquals(
                "",
                ReflectionTestUtils.invokeMethod(
                        service,
                        "normalizeSearchText",
                        (Object) null));

        assertEquals(
                "",
                ReflectionTestUtils.invokeMethod(
                        service,
                        "safeText",
                        (Object) null));

        assertEquals(
                "값",
                ReflectionTestUtils.invokeMethod(
                        service,
                        "safeText",
                        "값"));

        assertEquals(
                "first",
                ReflectionTestUtils.invokeMethod(
                        service,
                        "firstNonBlank",
                        "first",
                        "second"));

        assertEquals(
                "second",
                ReflectionTestUtils.invokeMethod(
                        service,
                        "firstNonBlank",
                        " ",
                        "second"));

        assertEquals(
                "second",
                ReflectionTestUtils.invokeMethod(
                        service,
                        "firstNonBlank",
                        null,
                        "second"));
    }

    @Test
    void nullableJsonHelpersShouldCoverMissingNullBlankZeroAndPositiveValues() {
        JSONObject json = new JSONObject()
                .put("nullValue", JSONObject.NULL)
                .put("blankValue", " ")
                .put("textValue", "text")
                .put("zero", 0)
                .put("positive", 7)
                .put("doubleValue", 3.5);

        assertNull(ReflectionTestUtils.invokeMethod(
                service,
                "nullableString",
                json,
                "missing"));
        assertNull(ReflectionTestUtils.invokeMethod(
                service,
                "nullableString",
                json,
                "nullValue"));
        assertNull(ReflectionTestUtils.invokeMethod(
                service,
                "nullableString",
                json,
                "blankValue"));
        assertEquals(
                "text",
                ReflectionTestUtils.invokeMethod(
                        service,
                        "nullableString",
                        json,
                        "textValue"));

        assertNull(ReflectionTestUtils.invokeMethod(
                service,
                "nullablePositiveInteger",
                json,
                "missing"));
        assertNull(ReflectionTestUtils.invokeMethod(
                service,
                "nullablePositiveInteger",
                json,
                "nullValue"));
        assertNull(ReflectionTestUtils.invokeMethod(
                service,
                "nullablePositiveInteger",
                json,
                "zero"));
        Integer positiveInteger = ReflectionTestUtils.invokeMethod(
                service,
                "nullablePositiveInteger",
                json,
                "positive");

        assertEquals(7, positiveInteger.intValue());

        assertNull(ReflectionTestUtils.invokeMethod(
                service,
                "nullableDouble",
                json,
                "missing"));
        assertNull(ReflectionTestUtils.invokeMethod(
                service,
                "nullableDouble",
                json,
                "nullValue"));
        assertEquals(
                3.5,
                (Double) ReflectionTestUtils.invokeMethod(
                        service,
                        "nullableDouble",
                        json,
                        "doubleValue"),
                0.001);
    }

    @Test
    void tvDateAndRuntimeHelpersShouldCoverEveryFallback() {
        assertNull(ReflectionTestUtils.invokeMethod(
                service,
                "resolveTvLastAirDate",
                (Object) null));

        JSONObject fallbackDetail = new JSONObject()
                .put("last_air_date", "2026-08-01");

        assertEquals(
                "2026-08-01",
                ReflectionTestUtils.invokeMethod(
                        service,
                        "resolveTvLastAirDate",
                        fallbackDetail));

        JSONObject blankEpisode = new JSONObject()
                .put(
                        "last_episode_to_air",
                        new JSONObject()
                                .put("air_date", " "))
                .put("last_air_date", "2026-08-02");

        assertEquals(
                "2026-08-02",
                ReflectionTestUtils.invokeMethod(
                        service,
                        "resolveTvLastAirDate",
                        blankEpisode));

        JSONObject episodeDetail = new JSONObject()
                .put(
                        "last_episode_to_air",
                        new JSONObject()
                                .put(
                                        "air_date",
                                        "2026-08-03"))
                .put("last_air_date", "2026-07-01");

        assertEquals(
                "2026-08-03",
                ReflectionTestUtils.invokeMethod(
                        service,
                        "resolveTvLastAirDate",
                        episodeDetail));

        assertNull(ReflectionTestUtils.invokeMethod(
                service,
                "parseFirstPositiveInteger",
                (Object) null));

        JSONArray runtimes = new JSONArray()
                .put(0)
                .put(-1)
                .put(45)
                .put(50);

        Integer firstPositiveRuntime = ReflectionTestUtils.invokeMethod(
                service,
                "parseFirstPositiveInteger",
                runtimes);

        assertEquals(45, firstPositiveRuntime.intValue());

        JSONArray noPositive = new JSONArray()
                .put(0)
                .put(-1);

        assertNull(ReflectionTestUtils.invokeMethod(
                service,
                "parseFirstPositiveInteger",
                noPositive));
    }
}
