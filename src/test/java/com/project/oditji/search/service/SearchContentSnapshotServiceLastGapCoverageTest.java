package com.project.oditji.search.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Map;

import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.test.util.ReflectionTestUtils;

/** JSONL 스냅샷의 null 목록·부모 없는 경로·원자 이동 fallback 잔여 분기를 보완합니다. */
class SearchContentSnapshotServiceLastGapCoverageTest {

    @TempDir
    Path tempDirectory;

    private SearchContentSnapshotService service;

    @BeforeEach
    void setUp() {
        service = new SearchContentSnapshotService(
                mock(SearchContentPolicyService.class));
        ReflectionTestUtils.setField(
                service,
                "snapshotEnabled",
                true);
    }

    @Test
    void saveShouldHandleNullContentsWhenTargetPathHasNoParent() throws Exception {
        String fileName =
                "snapshot-last-gap-"
                        + System.nanoTime()
                        + ".jsonl";
        Path target = Path.of(fileName);
        Path temporary = Path.of(fileName + ".tmp");

        ReflectionTestUtils.setField(
                service,
                "snapshotPath",
                fileName);

        try {
            service.saveSnapshot(null);

            assertTrue(Files.exists(target));
            assertEquals(0L, Files.size(target));
        } finally {
            Files.deleteIfExists(temporary);
            Files.deleteIfExists(target);
        }
    }

    @Test
    void saveShouldHandleEmptyContentsWithoutEnteringContentLoop() throws Exception {
        Path target = tempDirectory.resolve("empty-list.jsonl");
        ReflectionTestUtils.setField(
                service,
                "snapshotPath",
                target.toString());

        service.saveSnapshot(Collections.emptyList());

        assertTrue(Files.exists(target));
        assertEquals(0L, Files.size(target));
    }

    @Test
    void nullableStringShouldCoverNullValueReturnedByOptString() {
        JSONObject nullReturningJson = new JSONObject() {
            @Override
            public boolean has(String key) {
                return true;
            }

            @Override
            public boolean isNull(String key) {
                return false;
            }

            @Override
            public String optString(
                    String key,
                    String fallback) {
                return null;
            }
        };

        String value = ReflectionTestUtils.invokeMethod(
                service,
                "nullableString",
                nullReturningJson,
                "synthetic-key");

        assertNull(value);
    }

    @Test
    void moveAtomicallyShouldFallbackWhenMovingAcrossFileSystems()
            throws Exception {

        Path source = tempDirectory.resolve("source.tmp");
        Files.writeString(
                source,
                "snapshot-data",
                StandardCharsets.UTF_8);

        Path zipPath = tempDirectory.resolve("fallback.zip");
        URI zipUri = URI.create("jar:" + zipPath.toUri());

        try (FileSystem zipFileSystem =
                     FileSystems.newFileSystem(
                             zipUri,
                             Map.of("create", "true"))) {

            Path target = zipFileSystem.getPath("/snapshot.jsonl");

            ReflectionTestUtils.invokeMethod(
                    service,
                    "moveAtomically",
                    source,
                    target);

            assertFalse(Files.exists(source));
            assertTrue(Files.exists(target));
            assertEquals(
                    "snapshot-data",
                    Files.readString(
                            target,
                            StandardCharsets.UTF_8));
        }
    }
}
