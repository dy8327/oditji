package com.project.oditji.search.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.project.oditji.search.vo.CachedContentVO;

/** 스냅샷 JSON helper의 없는 키·명시적 null·빈 배열 잔여 조건을 보완합니다. */
class SearchContentSnapshotServiceFinalConditionCoverageTest {

    private SearchContentSnapshotService service;

    @BeforeEach
    void setUp() {
        service = new SearchContentSnapshotService(mock(SearchContentPolicyService.class));
    }

    @Test
    void restoreHelpersShouldLeaveValuesUntouchedWhenKeysAreMissing() {
        CachedContentVO content = new CachedContentVO();
        JSONObject empty = new JSONObject();
        invoke("restoreTmdbId", empty, content);
        invoke("restoreAgeRatingRetryState", empty, content);
        invoke("restoreRuntimeData", empty, content);
        invoke("restoreScoreData", empty, content);
        assertNull(content.getTmdbId());
        assertNull(content.getAgeRatingRetryCount());
        assertNull(content.getRuntime());
        assertNull(content.getTmdbScore());
    }

    @Test
    void platformReaderShouldSkipJsonNullBlankAndPreserveValidKeys() {
        JSONObject json = new JSONObject().put("platformKeys",
                new JSONArray().put(JSONObject.NULL).put(" ").put("netflix"));
        List<?> values = invoke("readPlatformKeys", json);
        assertEquals(List.of("netflix"), values);
    }

    @Test
    void nullableStringShouldReturnNullForMissingExplicitNullAndBlank() {
        JSONObject json = new JSONObject()
                .put("nil", JSONObject.NULL)
                .put("blank", " ")
                .put("value", "text");
        assertNull(invoke("nullableString", json, "missing"));
        assertNull(invoke("nullableString", json, "nil"));
        assertNull(invoke("nullableString", json, "blank"));
        assertEquals("text", invoke("nullableString", json, "value"));
    }

    @Test
    void toJsonShouldCreateEmptyPlatformArrayWhenPlatformListIsNull() {
        CachedContentVO content = new CachedContentVO();
        content.setTmdbId(1L);
        content.setPlatformKeys(null);
        JSONObject json = invoke("toJson", content);
        assertEquals(0, json.getJSONArray("platformKeys").length());
        assertEquals(1L, json.getLong("tmdbId"));
    }

    @SuppressWarnings("unchecked")
    private <T> T invoke(String method, Object... args) {
        return (T) ReflectionTestUtils.invokeMethod(service, method, args);
    }
}
