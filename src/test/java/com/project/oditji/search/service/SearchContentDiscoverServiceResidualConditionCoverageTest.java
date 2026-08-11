package com.project.oditji.search.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
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

/** Discover 수집기의 null/empty/중복/JSON 변환 잔여 조건을 보완합니다. */
class SearchContentDiscoverServiceResidualConditionCoverageTest {

    private TmdbApiClient apiClient;
    private SearchContentPolicyService policyService;
    private SearchContentDiscoverService service;

    @BeforeEach
    void setUp() {
        apiClient = mock(TmdbApiClient.class);
        policyService = mock(SearchContentPolicyService.class);
        service = new SearchContentDiscoverService(apiClient, policyService);
        ReflectionTestUtils.setField(service, "startYear", 1950);
        ReflectionTestUtils.setField(service, "supplementEnabled", true);
        ReflectionTestUtils.setField(service, "supplementYears", 1);
        ReflectionTestUtils.setField(service, "supplementPagesPerYear", 1);
        ReflectionTestUtils.setField(service, "supplementMaxCandidatesPerType", 10);
    }

    @Test
    void publicCollectorsShouldCoverInvalidTargetProviderAndDisabledSupplement() {
        assertTrue(service.collectMovieCandidates(0, Set.of(8)).isEmpty());
        assertTrue(service.collectMovieCandidates(1, null).isEmpty());
        assertTrue(service.collectMovieCandidates(1, Set.of()).isEmpty());

        ReflectionTestUtils.setField(service, "supplementEnabled", false);
        assertTrue(service.collectSupplementCandidates().isEmpty());

        ReflectionTestUtils.setField(service, "supplementEnabled", true);
        ReflectionTestUtils.setField(service, "supplementMaxCandidatesPerType", 0);
        assertTrue(service.collectSupplementCandidates().isEmpty());
        verify(apiClient, never()).get(any(String.class));
    }

    @Test
    void providerJoinShouldIgnoreNullZeroAndNegativeIds() {
        Set<Integer> providerIds = new LinkedHashSet<Integer>();
        providerIds.add(null);
        providerIds.add(0);
        providerIds.add(-1);
        providerIds.add(8);
        providerIds.add(1883);

        String joined = ReflectionTestUtils.invokeMethod(service, "joinProviderIds", providerIds);
        assertEquals("8|1883", joined);
    }

    @Test
    void appendItemsShouldCoverNullEmptyMaxReachedNullItemAndExcludedItem() {
        List<CachedContentVO> result = new ArrayList<CachedContentVO>();

        assertFalse(invokeBoolean("appendDiscoverItems", result, null, "MOVIE", 10));
        assertFalse(invokeBoolean("appendDiscoverItems", result, new JSONArray(), "MOVIE", 10));
        assertTrue(invokeBoolean("appendDiscoverItems", result, new JSONArray().put(new JSONObject()), "MOVIE", 0));

        ReflectionTestUtils.invokeMethod(service, "appendDiscoverItem", result, null, "MOVIE");
        assertTrue(result.isEmpty());

        JSONObject adult = new JSONObject().put("adult", true).put("id", 1);
        ReflectionTestUtils.invokeMethod(service, "appendDiscoverItem", result, adult, "MOVIE");
        assertTrue(result.isEmpty());
        verify(policyService, never()).shouldExcludeContent(isNull(), isNull(), any(), any());

        JSONObject excluded = new JSONObject().put("adult", false).put("id", 2).put("title", "blocked");
        when(policyService.shouldExcludeContent(null, null, "blocked", null)).thenReturn(true);
        ReflectionTestUtils.invokeMethod(service, "appendDiscoverItem", result, excluded, "MOVIE");
        assertTrue(result.isEmpty());
    }

    @Test
    void nullableJsonHelpersShouldCoverMissingNullBlankZeroAndValues() {
        JSONObject json = new JSONObject();
        assertNull(invoke("nullableString", json, "name"));
        json.put("name", JSONObject.NULL);
        assertNull(invoke("nullableString", json, "name"));
        json.put("name", "   ");
        assertNull(invoke("nullableString", json, "name"));
        json.put("name", "제목");
        assertEquals("제목", invoke("nullableString", json, "name"));

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
        assertEquals(7.5, (Double) invoke("nullableDouble", json, "score"), 0.0001);
    }

    @Test
    void genreConversionFirstNonBlankAndDuplicateRemovalShouldCoverBothBranches() {
        assertNull(invoke("convertGenreIdsToText", null, "MOVIE"));
        assertNull(invoke("convertGenreIdsToText", new JSONArray().put(-1), "MOVIE"));

        String movieGenres = invoke("convertGenreIdsToText", new JSONArray().put(28).put(28), "MOVIE");
        assertEquals("액션", movieGenres);

        String tvGenres = invoke("convertGenreIdsToText", new JSONArray().put(18), "TV");
        assertEquals("드라마", tvGenres);

        assertEquals("first", invoke("firstNonBlank", "first", "second"));
        assertEquals("second", invoke("firstNonBlank", null, "second"));
        assertEquals("second", invoke("firstNonBlank", "   ", "second"));

        CachedContentVO valid = content(1L, "MOVIE");
        CachedContentVO duplicate = content(1L, "MOVIE");
        CachedContentVO noId = content(null, "MOVIE");
        CachedContentVO noType = content(2L, null);
        List<CachedContentVO> source = new ArrayList<CachedContentVO>();
        source.add(null);
        source.add(noId);
        source.add(noType);
        source.add(valid);
        source.add(duplicate);

        List<CachedContentVO> deduplicated = ReflectionTestUtils.invokeMethod(service, "removeDuplicate", source);
        assertEquals(1, deduplicated.size());
    }

    private CachedContentVO content(Long id, String type) {
        CachedContentVO content = new CachedContentVO();
        content.setTmdbId(id);
        content.setContentType(type);
        return content;
    }

    private boolean invokeBoolean(String method, Object... arguments) {
        Boolean result = ReflectionTestUtils.invokeMethod(service, method, arguments);
        return Boolean.TRUE.equals(result);
    }

    @SuppressWarnings("unchecked")
    private <T> T invoke(String method, Object... arguments) {
        return (T) ReflectionTestUtils.invokeMethod(service, method, arguments);
    }
}
