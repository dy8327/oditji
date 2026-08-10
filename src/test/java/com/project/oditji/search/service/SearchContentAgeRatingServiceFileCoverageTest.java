package com.project.oditji.search.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.test.util.ReflectionTestUtils;

import com.project.oditji.search.vo.CachedContentVO;

/**
 * 연령등급 수동 파일 로딩과 JSON 생성/저장 helper 분기를 보완합니다.
 */
class SearchContentAgeRatingServiceFileCoverageTest {

    @TempDir
    Path tempDirectory;

    private SearchContentAgeRatingService service;

    @BeforeEach
    void setUp() {
        service = new SearchContentAgeRatingService(
                mock(TmdbApiClient.class),
                mock(SearchContentPolicyService.class),
                mock(SearchContentAgeRatingResolver.class));
    }

    @Test
    void manualOverrideLoadShouldReturnEmptyWhenDisabledBlankMissingOrDirectory() {
        ReflectionTestUtils.setField(service, "manualOverrideEnabled", false);
        ReflectionTestUtils.setField(service, "manualOverridePath", "ignored");

        assertTrue(loadOverrides().isEmpty());

        ReflectionTestUtils.setField(service, "manualOverrideEnabled", true);
        ReflectionTestUtils.setField(service, "manualOverridePath", "   ");

        assertTrue(loadOverrides().isEmpty());

        ReflectionTestUtils.setField(
                service,
                "manualOverridePath",
                tempDirectory.resolve("missing.json").toString());

        assertTrue(loadOverrides().isEmpty());

        ReflectionTestUtils.setField(
                service,
                "manualOverridePath",
                tempDirectory.toString());

        assertTrue(loadOverrides().isEmpty());
    }

    @Test
    void malformedManualOverrideJsonShouldThrowClearException() throws Exception {
        Path file = tempDirectory.resolve("bad.json");
        Files.writeString(file, "{broken");

        ReflectionTestUtils.setField(service, "manualOverrideEnabled", true);
        ReflectionTestUtils.setField(service, "manualOverridePath", file.toString());

        assertThrows(
                IllegalStateException.class,
                this::loadOverrides);
    }

    @Test
    void missingCandidateJsonShouldPreserveNullsAndValues() {
        CachedContentVO content = new CachedContentVO();
        content.setTmdbId(10L);
        content.setTitle(null);
        content.setReleaseDate("2026-08-10");
        content.setPopularity(3.5);
        content.setAgeRatingRetryCount(null);
        content.setAgeRatingLastCheckedAt("2026-08-10T12:00:00");

        JSONObject json = ReflectionTestUtils.invokeMethod(
                service,
                "createMissingCandidateJson",
                content);

        assertEquals(10L, json.getLong("tmdbId"));
        assertTrue(json.isNull("title"));
        assertEquals("2026-08-10", json.getString("releaseDate"));
        assertTrue(json.isNull("retryCount"));
    }

    @Test
    void jsonWriterShouldCreateParentDirectoriesAndFile() {
        Path file = tempDirectory.resolve("nested/dir/result.json");

        ReflectionTestUtils.invokeMethod(
                service,
                "writeJsonFile",
                file.toString(),
                new JSONObject().put("value", 1));

        assertTrue(Files.exists(file));
    }

    @SuppressWarnings("unchecked")
    private Map<String, String> loadOverrides() {
        return (Map<String, String>) ReflectionTestUtils.invokeMethod(
                service,
                "loadManualOverrides");
    }
}
