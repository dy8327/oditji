package com.project.oditji.search.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.project.oditji.search.vo.CachedContentVO;

/** JSONL 스냅샷의 직접 복원, 손상 라인 무시, 플랫폼 배열 파싱 잔여 분기를 보완합니다. */
class SearchContentSnapshotServiceParserGapCoverageTest {

    private SearchContentPolicyService policyService;
    private SearchContentSnapshotService service;

    @BeforeEach
    void setUp() {
        policyService = mock(SearchContentPolicyService.class);
        service = new SearchContentSnapshotService(policyService);
    }

    @Test
    void fromJsonShouldRestoreNullableAndOptionalFieldsSafely() {
        JSONObject json = new JSONObject()
                .put("tmdbId", JSONObject.NULL)
                .put("contentType", "MOVIE")
                .put("title", "영화")
                .put("ageRatingRetryCount", JSONObject.NULL)
                .put("ageRatingRestrictionChecked", JSONObject.NULL)
                .put("runtime", JSONObject.NULL)
                .put("episodeCount", JSONObject.NULL)
                .put("tmdbScore", JSONObject.NULL)
                .put("popularity", JSONObject.NULL)
                .put("platformKeys", new JSONArray()
                        .put(JSONObject.NULL)
                        .put(" ")
                        .put("netflix")
                        .put("tving"));

        CachedContentVO content = ReflectionTestUtils.invokeMethod(service, "fromJson", json);

        assertNull(content.getTmdbId());
        assertEquals("MOVIE", content.getContentType());
        assertEquals("영화", content.getTitle());
        assertNull(content.getRuntime());
        assertNull(content.getTmdbScore());
        assertEquals(List.of("netflix", "tving"), content.getPlatformKeys());
    }

    @Test
    void addSnapshotLineShouldIgnoreMalformedAndExcludedLinesThenAddValidLine() {
        List<CachedContentVO> result = new ArrayList<CachedContentVO>();
        ReflectionTestUtils.invokeMethod(service, "addSnapshotLine", result, "{broken");
        assertTrue(result.isEmpty());

        when(policyService.shouldExcludeContent(any(CachedContentVO.class)))
                .thenReturn(true)
                .thenReturn(false);

        String excludedLine = "{\"tmdbId\":1,\"contentType\":\"MOVIE\",\"title\":\"X\"}";
        ReflectionTestUtils.invokeMethod(service, "addSnapshotLine", result, excludedLine);
        assertTrue(result.isEmpty());

        String validLine = "{\"tmdbId\":2,\"contentType\":\"TV\",\"title\":\"Y\","
                + "\"platformKeys\":[\"wavve\"]}";
        ReflectionTestUtils.invokeMethod(service, "addSnapshotLine", result, validLine);

        assertEquals(1, result.size());
        assertEquals(2L, result.get(0).getTmdbId());
    }

    @Test
    void platformReaderAndNullableStringShouldCoverAbsentWrongTypeNullBlankAndValues() {
        JSONObject wrongType = new JSONObject().put("platformKeys", "not-array");
        Object wrongRaw = ReflectionTestUtils.invokeMethod(service, "readPlatformKeys", wrongType);
        assertTrue(((List<?>) wrongRaw).isEmpty());

        JSONObject withArray = new JSONObject().put(
                "platformKeys",
                new JSONArray().put("netflix").put(" ").put(JSONObject.NULL));
        Object arrayRaw = ReflectionTestUtils.invokeMethod(service, "readPlatformKeys", withArray);
        assertEquals(List.of("netflix"), arrayRaw);

        JSONObject strings = new JSONObject()
                .put("nullValue", JSONObject.NULL)
                .put("blankValue", " ")
                .put("textValue", "text");

        assertNull(ReflectionTestUtils.invokeMethod(service, "nullableString", strings, "missing"));
        assertNull(ReflectionTestUtils.invokeMethod(service, "nullableString", strings, "nullValue"));
        assertNull(ReflectionTestUtils.invokeMethod(service, "nullableString", strings, "blankValue"));
        assertEquals("text", ReflectionTestUtils.invokeMethod(service, "nullableString", strings, "textValue"));
    }
}
