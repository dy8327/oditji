package com.project.oditji.search.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.project.oditji.search.vo.CachedContentVO;

/** JSONL 스냅샷 저장·복원·손상 데이터 처리 분기를 검증합니다. */
@ExtendWith(MockitoExtension.class)
class SearchContentSnapshotServiceTest {

    @TempDir
    Path tempDirectory;

    @Mock
    private SearchContentPolicyService contentPolicyService;

    private SearchContentSnapshotService service;
    private Path snapshotPath;

    @BeforeEach
    void setUp() {
        service = new SearchContentSnapshotService(contentPolicyService);
        snapshotPath = tempDirectory.resolve("cache/search-content-cache.jsonl");
        ReflectionTestUtils.setField(service, "snapshotEnabled", true);
        ReflectionTestUtils.setField(
                service,
                "snapshotPath",
                snapshotPath.toString());
        lenient().when(contentPolicyService.shouldExcludeContent(any()))
                .thenReturn(false);
    }

    @Test
    void disabledOrMissingSnapshotShouldReturnEmptyList() {
        ReflectionTestUtils.setField(service, "snapshotEnabled", false);
        assertTrue(service.loadSnapshot().isEmpty());

        service.saveSnapshot(List.of(createContent(1L, "비저장")));
        assertFalse(Files.exists(snapshotPath));

        ReflectionTestUtils.setField(service, "snapshotEnabled", true);
        assertTrue(service.loadSnapshot().isEmpty());
    }

    @Test
    void saveAndLoadShouldPreserveAllSupportedFields() {
        CachedContentVO source = createContent(10L, "스냅샷 콘텐츠");

        service.saveSnapshot(List.of(source));
        List<CachedContentVO> loaded = service.loadSnapshot();

        assertEquals(1, loaded.size());
        CachedContentVO restored = loaded.get(0);
        assertEquals(10L, restored.getTmdbId());
        assertEquals("MOVIE", restored.getContentType());
        assertEquals("스냅샷 콘텐츠", restored.getTitle());
        assertEquals("원제", restored.getOriginalTitle());
        assertEquals("2026-08-01", restored.getReleaseDate());
        assertEquals("2026-08-02", restored.getLastAirDate());
        assertEquals(120, restored.getRuntime());
        assertEquals(12, restored.getEpisodeCount());
        assertEquals(2, restored.getAgeRatingRetryCount());
        assertEquals(Boolean.TRUE,
                restored.getAgeRatingRestrictionChecked());
        assertEquals(8.5, restored.getTmdbScore());
        assertEquals(99.5, restored.getPopularity());
        assertEquals(List.of("netflix", "tving"),
                restored.getPlatformKeys());
    }

    @Test
    void saveShouldIgnoreNullBlockedAndBlankPlatformRows() throws IOException {
        CachedContentVO allowed = createContent(1L, "허용");
        allowed.setPlatformKeys(java.util.Arrays.asList(
                "netflix",
                null,
                " "));
        CachedContentVO blocked = createContent(2L, "차단");

        when(contentPolicyService.shouldExcludeContent(blocked))
                .thenReturn(true);

        service.saveSnapshot(java.util.Arrays.asList(
                null,
                allowed,
                blocked));

        List<String> lines = Files.readAllLines(
                snapshotPath,
                StandardCharsets.UTF_8);
        assertEquals(1, lines.size());
        assertTrue(lines.get(0).contains("\"netflix\""));
        assertFalse(lines.get(0).contains("차단"));
    }

    @Test
    void loadShouldSkipBlankMalformedAndPolicyExcludedLines() throws IOException {
        Files.createDirectories(snapshotPath.getParent());
        String valid = "{\"tmdbId\":1,\"contentType\":\"MOVIE\","
                + "\"title\":\"정상\",\"platformKeys\":[\"netflix\"]}";
        String blocked = "{\"tmdbId\":2,\"contentType\":\"MOVIE\","
                + "\"title\":\"차단\",\"platformKeys\":[]}";
        Files.writeString(
                snapshotPath,
                System.lineSeparator()
                        + valid
                        + System.lineSeparator()
                        + "{invalid-json"
                        + System.lineSeparator()
                        + blocked,
                StandardCharsets.UTF_8);

        when(contentPolicyService.shouldExcludeContent(any()))
                .thenAnswer(invocation -> {
                    CachedContentVO value = invocation.getArgument(0);
                    return "차단".equals(value.getTitle());
                });

        List<CachedContentVO> loaded = service.loadSnapshot();
        assertEquals(1, loaded.size());
        assertEquals("정상", loaded.get(0).getTitle());
    }

    @Test
    void legacyJsonWithoutOptionalFieldsShouldRestoreNullAndEmptyValues()
            throws IOException {
        Files.createDirectories(snapshotPath.getParent());
        Files.writeString(
                snapshotPath,
                "{\"tmdbId\":3,\"contentType\":\"TV\","
                        + "\"title\":\"레거시\"}",
                StandardCharsets.UTF_8);

        CachedContentVO loaded = service.loadSnapshot().get(0);
        assertEquals(3L, loaded.getTmdbId());
        assertNull(loaded.getLastAirDate());
        assertNull(loaded.getRuntime());
        assertNull(loaded.getTmdbScore());
        assertTrue(loaded.getPlatformKeys().isEmpty());
    }

    @Test
    void invalidTargetPathShouldWrapIoFailure() throws IOException {
        Path blockingParent = tempDirectory.resolve("blocking-parent");
        Files.writeString(
                blockingParent,
                "not-a-directory",
                StandardCharsets.UTF_8);
        Path invalidTarget = blockingParent.resolve("cache.jsonl");
        ReflectionTestUtils.setField(
                service,
                "snapshotPath",
                invalidTarget.toString());

        assertThrows(
                IllegalStateException.class,
                () -> service.saveSnapshot(List.of(createContent(1L, "실패"))));
    }

    private CachedContentVO createContent(Long id, String title) {
        CachedContentVO content = new CachedContentVO();
        content.setTmdbId(id);
        content.setContentType("MOVIE");
        content.setTitle(title);
        content.setOriginalTitle("원제");
        content.setPosterPath("/poster.jpg");
        content.setReleaseDate("2026-08-01");
        content.setLastAirDate("2026-08-02");
        content.setGenreText("액션, 드라마");
        content.setAgeRating("15세 이상 관람가");
        content.setAgeRatingRetryCount(2);
        content.setAgeRatingLastCheckedAt("2026-08-04T10:00:00");
        content.setAgeRatingRestrictionChecked(Boolean.TRUE);
        content.setRuntime(120);
        content.setEpisodeCount(12);
        content.setDirector("감독");
        content.setCastNames("배우");
        content.setTmdbScore(8.5);
        content.setPopularity(99.5);
        content.setSearchText("검색어");
        content.setPlatformKeys(List.of("netflix", "tving"));
        return content;
    }
}
