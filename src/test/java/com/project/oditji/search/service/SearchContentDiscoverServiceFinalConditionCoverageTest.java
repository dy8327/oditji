package com.project.oditji.search.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.project.oditji.search.vo.CachedContentVO;

/** SearchContentDiscoverService의 남은 null/blank/중복 단락 조건을 검증합니다. */
class SearchContentDiscoverServiceFinalConditionCoverageTest {

    private SearchContentPolicyService policyService;
    private SearchContentDiscoverService service;

    @BeforeEach
    void setUp() {
        policyService = mock(SearchContentPolicyService.class);
        service = new SearchContentDiscoverService(mock(TmdbApiClient.class), policyService);
    }

    @Test
    void appendItemsShouldCoverNullEmptyNullItemExcludedAndInvalidId() {
        List<CachedContentVO> result = new ArrayList<CachedContentVO>();
        assertFalse(invokeBoolean("appendDiscoverItems", result, null, "MOVIE", 5));
        assertFalse(invokeBoolean("appendDiscoverItems", result, new JSONArray(), "MOVIE", 5));

        JSONArray items = new JSONArray();
        items.put(JSONObject.NULL);
        items.put(new JSONObject().put("adult", true).put("id", 1));
        items.put(new JSONObject().put("id", 0).put("title", "invalid"));
        assertTrue(invokeBoolean("appendDiscoverItems", result, items, "MOVIE", 5));
        assertTrue(result.isEmpty());
    }

    @Test
    void providerAndGenreHelpersShouldSkipInvalidValuesAndDuplicates() {
        Set<Integer> providerIds = new LinkedHashSet<Integer>();
        providerIds.add(null);
        providerIds.add(0);
        providerIds.add(-1);
        providerIds.add(8);
        providerIds.add(337);
        assertEquals("8|337", invoke("joinProviderIds", providerIds));

        assertNull(invoke("convertGenreIdsToText", null, "MOVIE"));
        assertNull(invoke("convertGenreIdsToText", new JSONArray().put(999999), "MOVIE"));
        assertEquals("액션", invoke("convertGenreIdsToText", new JSONArray().put(28).put(28), "MOVIE"));
    }

    @Test
    void nullableJsonHelpersShouldCoverMissingNullBlankZeroAndValidValues() {
        JSONObject json = new JSONObject();
        assertNull(invoke("nullableString", json, "name"));
        json.put("name", JSONObject.NULL);
        assertNull(invoke("nullableString", json, "name"));
        json.put("name", "   ");
        assertNull(invoke("nullableString", json, "name"));
        json.put("name", "title");
        assertEquals("title", invoke("nullableString", json, "name"));

        assertNull(invoke("nullableLong", json, "id"));
        json.put("id", JSONObject.NULL);
        assertNull(invoke("nullableLong", json, "id"));
        json.put("id", 0);
        assertNull(invoke("nullableLong", json, "id"));
        json.put("id", 10);
        assertEquals(Long.valueOf(10L), (Long) invoke("nullableLong", json, "id"));

        assertNull(invoke("nullableDouble", json, "score"));
        json.put("score", JSONObject.NULL);
        assertNull(invoke("nullableDouble", json, "score"));
        json.put("score", 7.5);
        assertEquals(Double.valueOf(7.5), (Double) invoke("nullableDouble", json, "score"));
    }

    @Test
    void duplicateRemovalShouldIgnoreIncompleteEntriesAndKeepLatestDuplicate() {
        CachedContentVO nullId = new CachedContentVO();
        nullId.setContentType("MOVIE");

        CachedContentVO nullType = new CachedContentVO();
        nullType.setTmdbId(1L);

        CachedContentVO first = content(2L, "MOVIE", "first");
        CachedContentVO second = content(2L, "MOVIE", "second");

        List<CachedContentVO> source = new ArrayList<CachedContentVO>();
        source.add(null);
        source.add(nullId);
        source.add(nullType);
        source.add(first);
        source.add(second);

        @SuppressWarnings("unchecked")
        List<CachedContentVO> result = (List<CachedContentVO>) invoke("removeDuplicate", source);
        assertEquals(1, result.size());
        assertEquals("second", result.get(0).getTitle());
    }

    @Test
    void exclusionShouldShortCircuitAdultAndDelegateNormalContentToPolicy() {
        JSONObject adult = new JSONObject().put("adult", true);
        assertTrue(invokeBoolean("shouldExcludeContent", adult));

        JSONObject normal = new JSONObject()
                .put("title", "title")
                .put("original_title", "original");
        when(policyService.shouldExcludeContent(null, null, "title", "original")).thenReturn(true);
        assertTrue(invokeBoolean("shouldExcludeContent", normal));
    }

    private CachedContentVO content(Long id, String type, String title) {
        CachedContentVO content = new CachedContentVO();
        content.setTmdbId(id);
        content.setContentType(type);
        content.setTitle(title);
        return content;
    }

    private Object invoke(String methodName, Object... args) {
        return ReflectionTestUtils.invokeMethod(service, methodName, args);
    }

    private boolean invokeBoolean(String methodName, Object... args) {
        return Boolean.TRUE.equals(invoke(methodName, args));
    }
}
