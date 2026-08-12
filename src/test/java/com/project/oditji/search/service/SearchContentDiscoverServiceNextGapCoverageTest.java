package com.project.oditji.search.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
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

/** Discover 수집기의 루프/변환 잔여 경계 조건을 추가 보완합니다. */
class SearchContentDiscoverServiceNextGapCoverageTest {

    private TmdbApiClient apiClient;
    private SearchContentPolicyService policyService;
    private SearchContentDiscoverService service;

    @BeforeEach
    void setUp() {
        apiClient = mock(TmdbApiClient.class);
        policyService = mock(SearchContentPolicyService.class);
        service = new SearchContentDiscoverService(apiClient, policyService);

        when(apiClient.getBaseUrl()).thenReturn("https://api.test/3");
        when(apiClient.getLanguage()).thenReturn("ko-KR");
        when(apiClient.getRegion()).thenReturn("KR");
        when(apiClient.encode(anyString()))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void appendItemsShouldSkipLoopWhenMaximumIsAlreadyReached() {
        List<CachedContentVO> result = new ArrayList<CachedContentVO>();
        result.add(content(1L, "MOVIE"));
        JSONArray items = new JSONArray().put(movieJson(2L, "두 번째"));

        Boolean appended = ReflectionTestUtils.invokeMethod(
                service,
                "appendDiscoverItems",
                result,
                items,
                "MOVIE",
                1);

        assertEquals(Boolean.TRUE, appended);
        assertEquals(1, result.size());
    }

    @Test
    void appendItemShouldIgnoreConvertedContentWithoutPositiveId() {
        List<CachedContentVO> result = new ArrayList<CachedContentVO>();
        JSONObject withoutId = new JSONObject()
                .put("adult", false)
                .put("title", "ID 없음");

        ReflectionTestUtils.invokeMethod(
                service,
                "appendDiscoverItem",
                result,
                withoutId,
                "MOVIE");

        assertTrue(result.isEmpty());
    }

    @Test
    void exclusionHelperShouldReachPolicyWithMissingDisplayTitles() {
        when(policyService.shouldExcludeContent(null, null, null, null))
                .thenReturn(false);

        Boolean excluded = ReflectionTestUtils.invokeMethod(
                service,
                "shouldExcludeContent",
                new JSONObject().put("adult", false));

        assertEquals(Boolean.FALSE, excluded);
    }

    @Test
    void conversionShouldCoverMovieFallbackAndTvFallbackFields() {
        JSONObject movie = new JSONObject()
                .put("id", 10L)
                .put("original_title", "원제")
                .put("release_date", "2026-08-01")
                .put("genre_ids", new JSONArray().put(28));
        CachedContentVO movieContent = ReflectionTestUtils.invokeMethod(
                service,
                "convertDiscoverItem",
                movie,
                "MOVIE");
        assertEquals("원제", movieContent.getTitle());

        JSONObject tv = new JSONObject()
                .put("id", 20L)
                .put("original_name", "TV 원제")
                .put("first_air_date", "2026-08-02")
                .put("genre_ids", new JSONArray().put(18));
        CachedContentVO tvContent = ReflectionTestUtils.invokeMethod(
                service,
                "convertDiscoverItem",
                tv,
                "TV");
        assertEquals("TV 원제", tvContent.getTitle());
    }

    @Test
    void duplicateRemovalShouldRejectBlankTypeOnlyAfterNonNullChecks() {
        CachedContentVO blankType = content(1L, "");
        CachedContentVO valid = content(2L, "MOVIE");

        List<CachedContentVO> result = ReflectionTestUtils.invokeMethod(
                service,
                "removeDuplicate",
                List.of(blankType, valid));

        assertEquals(2, result.size());
        assertFalse(result.isEmpty());
    }

    @Test
    void nullableHelpersShouldCoverPresentButNonConvertibleValues() {
        JSONObject json = new JSONObject()
                .put("text", new JSONObject())
                .put("id", "not-number")
                .put("score", "not-number");

        String text = ReflectionTestUtils.invokeMethod(service, "nullableString", json, "text");
        Long id = ReflectionTestUtils.invokeMethod(service, "nullableLong", json, "id");
        Double score = ReflectionTestUtils.invokeMethod(service, "nullableDouble", json, "score");

        assertFalse(text.isBlank());
        assertNull(id);
        assertTrue(score.isNaN());
    }

    private CachedContentVO content(Long id, String type) {
        CachedContentVO content = new CachedContentVO();
        content.setTmdbId(id);
        content.setContentType(type);
        return content;
    }

    private JSONObject movieJson(long id, String title) {
        return new JSONObject()
                .put("id", id)
                .put("adult", false)
                .put("title", title);
    }
}
