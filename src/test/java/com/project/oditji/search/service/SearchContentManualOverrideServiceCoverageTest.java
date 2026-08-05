package com.project.oditji.search.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.project.oditji.search.vo.CachedContentVO;

/** 수동 OTT 보완 JSON의 로딩, 후보 생성, 병합 및 오류 분기를 검증합니다. */
@ExtendWith(MockitoExtension.class)
class SearchContentManualOverrideServiceCoverageTest {

    @Mock
    private TmdbProviderService providerService;

    @TempDir
    Path tempDir;

    private SearchContentManualOverrideService service;

    @BeforeEach
    void setUp() {
        service = new SearchContentManualOverrideService(providerService);
    }

    @Test
    void disabledOrMissingPathShouldKeepOverridesEmpty() {
        ReflectionTestUtils.setField(service, "enabled", false);
        ReflectionTestUtils.setField(service, "overridePath", "");
        service.reload();
        assertTrue(service.createCandidates().isEmpty());

        ReflectionTestUtils.setField(service, "enabled", true);
        ReflectionTestUtils.setField(service, "overridePath", "   ");
        service.reload();
        assertTrue(service.createCandidates().isEmpty());

        ReflectionTestUtils.setField(
                service,
                "overridePath",
                tempDir.resolve("missing.json").toString());
        service.reload();
        assertTrue(service.createCandidates().isEmpty());
    }

    @Test
    void validJsonShouldCreateUniqueMovieAndTvCandidates() throws Exception {
        prepareNormalizer();
        Path json = writeJson("""
                {
                  "MOVIE": {
                    "Netflix": [101, 101, 0, -1, "bad"],
                    "Coupang Play": [102],
                    "Unsupported": [999]
                  },
                  "TV": {
                    "TVING": [201],
                    "wavve": [202]
                  }
                }
                """);
        enable(json);

        service.reload();
        List<CachedContentVO> candidates = service.createCandidates();
        Set<String> keys = candidates.stream()
                .map(value -> value.getContentType() + ":" + value.getTmdbId())
                .collect(Collectors.toSet());

        assertEquals(Set.of("MOVIE:101", "MOVIE:102", "TV:201", "TV:202"), keys);
    }

    @Test
    void applyShouldMergeWithoutDuplicatesAndContainsShouldMatch() throws Exception {
        prepareNormalizer();
        Path json = writeJson("""
                {"MOVIE":{"Netflix":[101],"Watcha":[101]}}
                """);
        enable(json);
        service.reload();

        CachedContentVO content = content("MOVIE", 101L);
        content.setPlatformKeys(List.of("netflix", "wavve"));

        service.apply(content);

        assertEquals(List.of("netflix", "wavve", "watcha"), content.getPlatformKeys());
        assertTrue(service.contains(content));
        assertFalse(service.contains(content("TV", 101L)));
    }

    @Test
    void applyAndContainsShouldIgnoreIncompleteOrUnknownContent() throws Exception {
        prepareNormalizer();
        Path json = writeJson("{\"MOVIE\":{\"Netflix\":[101]}}");
        enable(json);
        service.reload();

        service.apply(null);
        service.apply(new CachedContentVO());

        CachedContentVO noType = new CachedContentVO();
        noType.setTmdbId(101L);
        service.apply(noType);

        CachedContentVO unknown = content("MOVIE", 999L);
        service.apply(unknown);
        assertTrue(unknown.getPlatformKeys() == null || unknown.getPlatformKeys().isEmpty());
        assertFalse(service.contains(null));
        assertFalse(service.contains(new CachedContentVO()));
    }

    @Test
    void malformedJsonShouldThrowClearException() throws Exception {
        Path json = writeJson("{not-json");
        enable(json);

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                service::reload);

        assertTrue(exception.getMessage().contains("JSON 형식"));
    }

    @Test
    void nonObjectSectionsAndNonArrayPlatformValuesShouldBeIgnored() throws Exception {
        prepareNormalizer();
        Path json = writeJson("""
                {
                  "MOVIE": [],
                  "TV": {
                    "Netflix": "not-an-array",
                    "Disney Plus": [303]
                  }
                }
                """);
        enable(json);

        service.reload();

        List<CachedContentVO> candidates = service.createCandidates();
        assertEquals(1, candidates.size());
        assertEquals("TV", candidates.get(0).getContentType());
        assertEquals(303L, candidates.get(0).getTmdbId());
    }

    private void enable(Path path) {
        ReflectionTestUtils.setField(service, "enabled", true);
        ReflectionTestUtils.setField(service, "overridePath", path.toString());
    }

    private Path writeJson(String value) throws Exception {
        Path path = tempDir.resolve("manual-platform-overrides.json");
        Files.writeString(path, value, StandardCharsets.UTF_8);
        return path;
    }

    private CachedContentVO content(String type, Long tmdbId) {
        CachedContentVO content = new CachedContentVO();
        content.setContentType(type);
        content.setTmdbId(tmdbId);
        return content;
    }

    private void prepareNormalizer() {
        when(providerService.normalizePlatformName(anyString()))
                .thenAnswer(invocation -> {
                    String value = invocation.getArgument(0, String.class)
                            .replace(" ", "")
                            .toLowerCase(Locale.ROOT);
                    return switch (value) {
                        case "netflix" -> "netflix";
                        case "tving" -> "tving";
                        case "wavve" -> "wavve";
                        case "disneyplus" -> "disney";
                        case "watcha" -> "watcha";
                        case "coupangplay" -> "coupang";
                        default -> "";
                    };
                });
    }
}
