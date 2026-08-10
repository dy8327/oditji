package com.project.oditji.search.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.project.oditji.search.vo.CachedContentVO;

/**
 * Discover 서비스의 helper null/blank/invalid ID와 중복 제거 조건을 보완합니다.
 */
class SearchContentDiscoverServiceRemainingCoverageTest {

    private TmdbApiClient apiClient;
    private SearchContentPolicyService contentPolicyService;
    private SearchContentDiscoverService service;

    @BeforeEach
    void setUp() {
        apiClient =
                mock(TmdbApiClient.class);
        contentPolicyService =
                mock(SearchContentPolicyService.class);
        service =
                new SearchContentDiscoverService(
                        apiClient,
                        contentPolicyService);
    }

    @Test
    void providerJoinShouldIgnoreNullZeroAndNegativeIds() {
        Set<Integer> providers =
                new LinkedHashSet<Integer>(
                        Arrays.asList(
                                null,
                                0,
                                -1,
                                8,
                                337));

        String result =
                ReflectionTestUtils.invokeMethod(
                        service,
                        "joinProviderIds",
                        providers);

        assertEquals(
                "8|337",
                result);
    }

    @Test
    void appendDiscoverItemsShouldRejectNullAndEmptyArraysAndRespectMaximum() {
        List<CachedContentVO> result =
                new ArrayList<CachedContentVO>();

        Boolean nullItems =
                ReflectionTestUtils.invokeMethod(
                        service,
                        "appendDiscoverItems",
                        result,
                        null,
                        "MOVIE",
                        2);

        Boolean emptyItems =
                ReflectionTestUtils.invokeMethod(
                        service,
                        "appendDiscoverItems",
                        result,
                        new JSONArray(),
                        "MOVIE",
                        2);

        assertNotEquals(
                Boolean.TRUE,
                nullItems);
        assertNotEquals(
                Boolean.TRUE,
                emptyItems);

        JSONArray items =
                new JSONArray()
                        .put(JSONObject.NULL)
                        .put(
                                new JSONObject()
                                        .put("id", 1L)
                                        .put("title", "A"))
                        .put(
                                new JSONObject()
                                        .put("id", 2L)
                                        .put("title", "B"))
                        .put(
                                new JSONObject()
                                        .put("id", 3L)
                                        .put("title", "C"));

        Boolean appended =
                ReflectionTestUtils.invokeMethod(
                        service,
                        "appendDiscoverItems",
                        result,
                        items,
                        "MOVIE",
                        2);

        assertEquals(
                Boolean.TRUE,
                appended);
        assertEquals(2, result.size());
    }

    @Test
    void contentExclusionShouldShortCircuitAdultAndUseTitleFallbackForPolicy() {
        JSONObject adult =
                new JSONObject()
                        .put("adult", true)
                        .put("title", "성인");

        Boolean adultExcluded =
                ReflectionTestUtils.invokeMethod(
                        service,
                        "shouldExcludeContent",
                        adult);

        assertEquals(
                Boolean.TRUE,
                adultExcluded);

        verify(
                contentPolicyService,
                never())
                .shouldExcludeContent(
                        null,
                        null,
                        "성인",
                        null);

        JSONObject fallback =
                new JSONObject()
                        .put("adult", false)
                        .put("title", " ")
                        .put(
                                "original_title",
                                "원제");

        when(contentPolicyService.shouldExcludeContent(
                null,
                null,
                " ",
                "원제"))
                .thenReturn(false);

        Boolean normalExcluded =
                ReflectionTestUtils.invokeMethod(
                        service,
                        "shouldExcludeContent",
                        fallback);

        assertNotEquals(
                Boolean.TRUE,
                normalExcluded);
    }

    @Test
    void duplicateRemovalShouldIgnoreInvalidRowsAndKeepLastDuplicate() {
        CachedContentVO first =
                content(
                        1L,
                        "MOVIE",
                        "first");
        CachedContentVO second =
                content(
                        1L,
                        "MOVIE",
                        "second");

        CachedContentVO noId =
                content(
                        null,
                        "MOVIE",
                        "no-id");

        CachedContentVO noType =
                content(
                        2L,
                        null,
                        "no-type");

        @SuppressWarnings("unchecked")
        List<CachedContentVO> result =
                (List<CachedContentVO>)
                        ReflectionTestUtils.invokeMethod(
                                service,
                                "removeDuplicate",
                                Arrays.asList(
                                        null,
                                        first,
                                        noId,
                                        noType,
                                        second));

        assertEquals(1, result.size());
        assertEquals(
                "second",
                result.get(0).getTitle());
    }

    @Test
    void nullableHelpersShouldCoverMissingNullBlankZeroAndPositiveValues() {
        JSONObject json =
                new JSONObject()
                        .put("nil", JSONObject.NULL)
                        .put("blank", " ")
                        .put("text", "value")
                        .put("zero", 0)
                        .put("id", 7L)
                        .put("score", 8.8);

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
                "value",
                invokeString(
                        json,
                        "text"));

        assertNull(
                invokeLong(
                        json,
                        "missing"));
        assertNull(
                invokeLong(
                        json,
                        "nil"));
        assertNull(
                invokeLong(
                        json,
                        "zero"));
        assertEquals(
                7L,
                invokeLong(
                        json,
                        "id"));

        assertNull(
                invokeDouble(
                        json,
                        "missing"));
        assertNull(
                invokeDouble(
                        json,
                        "nil"));
        assertEquals(
                8.8,
                invokeDouble(
                        json,
                        "score"));
    }

    @Test
    void firstNonBlankAndGenreConversionShouldCoverBothFallbackSides() {
        assertEquals(
                "first",
                invokeFirstNonBlank(
                        "first",
                        "second"));
        assertEquals(
                "second",
                invokeFirstNonBlank(
                        null,
                        "second"));
        assertEquals(
                "second",
                invokeFirstNonBlank(
                        " ",
                        "second"));

        assertNull(
                invokeGenreText(
                        null,
                        "MOVIE"));

        JSONArray genres =
                new JSONArray()
                        .put(28)
                        .put(28)
                        .put(999999);

        assertEquals(
                "액션",
                invokeGenreText(
                        genres,
                        "MOVIE"));
    }

    private CachedContentVO content(
            Long tmdbId,
            String type,
            String title) {

        CachedContentVO content =
                new CachedContentVO();
        content.setTmdbId(tmdbId);
        content.setContentType(type);
        content.setTitle(title);
        return content;
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

    private Long invokeLong(
            JSONObject json,
            String key) {

        return ReflectionTestUtils.invokeMethod(
                service,
                "nullableLong",
                json,
                key);
    }

    private Double invokeDouble(
            JSONObject json,
            String key) {

        return ReflectionTestUtils.invokeMethod(
                service,
                "nullableDouble",
                json,
                key);
    }

    private String invokeFirstNonBlank(
            String first,
            String second) {

        return ReflectionTestUtils.invokeMethod(
                service,
                "firstNonBlank",
                first,
                second);
    }

    private String invokeGenreText(
            JSONArray genres,
            String type) {

        return ReflectionTestUtils.invokeMethod(
                service,
                "convertGenreIdsToText",
                genres,
                type);
    }
}
