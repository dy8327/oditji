package com.project.oditji.search.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.test.util.ReflectionTestUtils;

import com.project.oditji.search.vo.CachedContentVO;

/**
 * 수동 OTT 보완 서비스의 잘못된 내부 key, 디렉터리 경로와 null 플랫폼 목록 분기를 보완합니다.
 */
class SearchContentManualOverrideServiceRemainingCoverageTest {

    @TempDir
    Path tempDirectory;

    private SearchContentManualOverrideService service;

    @BeforeEach
    void setUp() {
        TmdbProviderService providerService =
                mock(TmdbProviderService.class);

        when(providerService.normalizePlatformName(anyString()))
                .thenAnswer(invocation ->
                        invocation.getArgument(
                                0,
                                String.class)
                                .trim()
                                .toLowerCase(Locale.ROOT));

        service =
                new SearchContentManualOverrideService(
                        providerService);
    }

    @Test
    void candidateCreationShouldSkipMalformedAndNonNumericInternalKeys() {
        Map<String, Set<String>> values =
                new LinkedHashMap<String, Set<String>>();

        values.put(
                "BROKEN",
                Set.of("netflix"));
        values.put(
                "MOVIE:not-number",
                Set.of("netflix"));
        values.put(
                "TV:55",
                Set.of("tving"));

        overrideReference().set(values);

        List<CachedContentVO> candidates =
                service.createCandidates();

        assertEquals(1, candidates.size());
        assertEquals(
                "TV",
                candidates.get(0)
                        .getContentType());
        assertEquals(
                55L,
                candidates.get(0)
                        .getTmdbId());
    }

    @Test
    void applyShouldHandleNullExistingPlatformListAndMergeManualValue() {
        overrideReference().set(
                Map.of(
                        "MOVIE:10",
                        Set.of("netflix")));

        CachedContentVO content =
                new CachedContentVO();
        content.setContentType("MOVIE");
        content.setTmdbId(10L);
        content.setPlatformKeys(null);

        service.apply(content);

        assertEquals(
                List.of("netflix"),
                content.getPlatformKeys());
    }

    @Test
    void containsShouldCoverNullIdBlankTypeAndPositiveMatch() {
        overrideReference().set(
                Map.of(
                        "TV:20",
                        Set.of("tving")));

        CachedContentVO noId =
                new CachedContentVO();
        noId.setContentType("TV");

        CachedContentVO blankType =
                new CachedContentVO();
        blankType.setTmdbId(20L);
        blankType.setContentType(" ");

        CachedContentVO valid =
                new CachedContentVO();
        valid.setTmdbId(20L);
        valid.setContentType("TV");

        assertFalse(service.contains(noId));
        assertFalse(service.contains(blankType));
        assertTrue(service.contains(valid));
    }

    @Test
    void existingDirectoryOverridePathShouldBeIgnoredBecauseItIsNotRegularFile() {
        ReflectionTestUtils.setField(
                service,
                "enabled",
                true);
        ReflectionTestUtils.setField(
                service,
                "overridePath",
                tempDirectory.toString());

        service.reload();

        assertTrue(
                service.createCandidates()
                        .isEmpty());
    }

    @Test
    void missingSectionAndUnsupportedOrNonArrayEntriesShouldLeaveTargetEmpty() {
        Map<String, Set<String>> target =
                new LinkedHashMap<String, Set<String>>();

        ReflectionTestUtils.invokeMethod(
                service,
                "readSection",
                new JSONObject(),
                "MOVIE",
                target);

        JSONObject section =
                new JSONObject()
                        .put(
                                "unsupported",
                                new org.json.JSONArray()
                                        .put(1L))
                        .put(
                                "netflix",
                                "not-array");

        ReflectionTestUtils.invokeMethod(
                service,
                "addManualPlatformOverrides",
                section,
                "unsupported",
                "MOVIE",
                target);

        ReflectionTestUtils.invokeMethod(
                service,
                "addManualPlatformOverrides",
                section,
                "netflix",
                "MOVIE",
                target);

        assertTrue(target.isEmpty());
    }

    @SuppressWarnings("unchecked")
    private AtomicReference<Map<String, Set<String>>> overrideReference() {
        return (AtomicReference<Map<String, Set<String>>>)
                ReflectionTestUtils.getField(
                        service,
                        "overrideMap");
    }
}
