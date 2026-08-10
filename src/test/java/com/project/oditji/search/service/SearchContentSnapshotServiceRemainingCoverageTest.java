package com.project.oditji.search.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.test.util.ReflectionTestUtils;

import com.project.oditji.search.vo.CachedContentVO;

/**
 * JSONL 스냅샷 복원 helper의 명시적 null 키와 플랫폼 배열 조건을 보완합니다.
 */
class SearchContentSnapshotServiceRemainingCoverageTest {

    @TempDir
    Path tempDirectory;

    private SearchContentSnapshotService service;

    @BeforeEach
    void setUp() {
        service =
                new SearchContentSnapshotService(
                        mock(SearchContentPolicyService.class));

        ReflectionTestUtils.setField(
                service,
                "snapshotEnabled",
                true);
        ReflectionTestUtils.setField(
                service,
                "snapshotPath",
                tempDirectory.resolve(
                        "snapshot.jsonl")
                        .toString());
    }

    @Test
    void loadSnapshotShouldWrapDirectoryReadFailure() {
        ReflectionTestUtils.setField(
                service,
                "snapshotPath",
                tempDirectory.toString());

        assertThrows(
                IllegalStateException.class,
                () -> service.loadSnapshot());
    }

    @Test
    void restoreHelpersShouldCoverExplicitJsonNullAndPresentValues() {
        CachedContentVO content =
                new CachedContentVO();

        JSONObject nulls =
                new JSONObject()
                        .put("tmdbId", JSONObject.NULL)
                        .put("ageRatingRetryCount", JSONObject.NULL)
                        .put("ageRatingRestrictionChecked", JSONObject.NULL)
                        .put("runtime", JSONObject.NULL)
                        .put("episodeCount", JSONObject.NULL)
                        .put("tmdbScore", JSONObject.NULL)
                        .put("popularity", JSONObject.NULL);

        ReflectionTestUtils.invokeMethod(
                service,
                "restoreTmdbId",
                nulls,
                content);
        ReflectionTestUtils.invokeMethod(
                service,
                "restoreAgeRatingRetryState",
                nulls,
                content);
        ReflectionTestUtils.invokeMethod(
                service,
                "restoreRuntimeData",
                nulls,
                content);
        ReflectionTestUtils.invokeMethod(
                service,
                "restoreScoreData",
                nulls,
                content);

        assertNull(content.getTmdbId());
        assertNull(content.getAgeRatingRetryCount());
        assertNull(content.getAgeRatingRestrictionChecked());
        assertNull(content.getRuntime());
        assertNull(content.getEpisodeCount());
        assertNull(content.getTmdbScore());
        assertNull(content.getPopularity());

        JSONObject values =
                new JSONObject()
                        .put("tmdbId", 1L)
                        .put("ageRatingRetryCount", 3)
                        .put("ageRatingRestrictionChecked", false)
                        .put("runtime", 100)
                        .put("episodeCount", 12)
                        .put("tmdbScore", 7.7)
                        .put("popularity", 88.8);

        ReflectionTestUtils.invokeMethod(
                service,
                "restoreTmdbId",
                values,
                content);
        ReflectionTestUtils.invokeMethod(
                service,
                "restoreAgeRatingRetryState",
                values,
                content);
        ReflectionTestUtils.invokeMethod(
                service,
                "restoreRuntimeData",
                values,
                content);
        ReflectionTestUtils.invokeMethod(
                service,
                "restoreScoreData",
                values,
                content);

        assertEquals(1L, content.getTmdbId());
        assertEquals(3, content.getAgeRatingRetryCount());
        assertEquals(Boolean.FALSE, content.getAgeRatingRestrictionChecked());
        assertEquals(100, content.getRuntime());
        assertEquals(12, content.getEpisodeCount());
        assertEquals(7.7, content.getTmdbScore());
        assertEquals(88.8, content.getPopularity());
    }

    @Test
    void platformReaderShouldHandleNullArrayNullBlankAndNormalKeys() {
        JSONObject noArray =
                new JSONObject()
                        .put("platformKeys", "invalid");

        List<String> empty =
                invokePlatformKeys(noArray);
        assertTrue(empty.isEmpty());

        JSONObject withArray =
                new JSONObject()
                        .put(
                                "platformKeys",
                                new JSONArray()
                                        .put(JSONObject.NULL)
                                        .put(" ")
                                        .put("netflix")
                                        .put("tving"));

        assertEquals(
                List.of(
                        "netflix",
                        "tving"),
                invokePlatformKeys(withArray));
    }

    @Test
    void nullableStringShouldCoverMissingExplicitNullBlankAndValue() {
        JSONObject json =
                new JSONObject()
                        .put("nil", JSONObject.NULL)
                        .put("blank", " ")
                        .put("value", "text");

        assertNull(
                invokeString(
                        json,
                        "missing"));
        assertNull(
                invokeString(
                        json,
                        "nil"));
        assertNull(
                invokeString(
                        json,
                        "blank"));
        assertEquals(
                "text",
                invokeString(
                        json,
                        "value"));
    }

    @Test
    void toJsonShouldIgnoreNullAndBlankPlatformKeys() {
        CachedContentVO content =
                new CachedContentVO();

        content.setTmdbId(7L);
        content.setPlatformKeys(
                Arrays.asList(
                        null,
                        " ",
                        "netflix"));

        JSONObject json =
                ReflectionTestUtils.invokeMethod(
                        service,
                        "toJson",
                        content);

        JSONArray platforms =
                json.getJSONArray(
                        "platformKeys");

        assertEquals(1, platforms.length());
        assertEquals(
                "netflix",
                platforms.getString(0));

        assertFalse(
                json.isNull("tmdbId"));
        assertTrue(
                json.isNull("title"));
    }

    @SuppressWarnings("unchecked")
    private List<String> invokePlatformKeys(
            JSONObject json) {

        return (List<String>)
                ReflectionTestUtils.invokeMethod(
                        service,
                        "readPlatformKeys",
                        json);
    }

    private String invokeString(
            JSONObject json,
            String key) {

        return ReflectionTestUtils.invokeMethod(
                service,
                "nullableString",
                json,
                key);
    }
}
