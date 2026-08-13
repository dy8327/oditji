package com.project.oditji.search.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.project.oditji.search.vo.CachedContentVO;

/**
 * 콘텐츠 수집기의 정렬/필터/중복 제거 helper 조건을 보완합니다.
 */
class SearchContentCollectorServiceRemainingCoverageTest {

    private SearchContentManualOverrideService manualOverrideService;
    private SearchContentPolicyService contentPolicyService;
    private SearchContentCollectorService service;

    @BeforeEach
    void setUp() {
        manualOverrideService =
                mock(SearchContentManualOverrideService.class);
        contentPolicyService =
                mock(SearchContentPolicyService.class);

        service = new SearchContentCollectorService(
                mock(SearchContentDiscoverService.class),
                mock(SearchContentEnrichmentService.class),
                manualOverrideService,
                mock(SearchContentAgeRatingService.class),
                mock(TmdbProviderService.class),
                contentPolicyService);
    }

    @Test
    void sortAndLimitShouldFilterEveryInvalidShapeAndPrioritizeManualContent() {
        CachedContentVO excluded =
                validContent(1L, "MOVIE", "차단", 100.0);
        when(contentPolicyService.shouldExcludeContent(excluded))
                .thenReturn(true);

        CachedContentVO noId =
                validContent(null, "MOVIE", "ID없음", 90.0);

        CachedContentVO noType =
                validContent(2L, null, "유형없음", 80.0);

        CachedContentVO nullPlatforms =
                validContent(3L, "MOVIE", "플랫폼null", 70.0);
        ReflectionTestUtils.setField(
                nullPlatforms,
                "platformKeys",
                null);

        CachedContentVO emptyPlatforms =
                validContent(4L, "MOVIE", "플랫폼없음", 60.0);
        emptyPlatforms.setPlatformKeys(List.of());

        CachedContentVO normal =
                validContent(5L, "MOVIE", "일반", 50.0);

        CachedContentVO manual =
                validContent(99L, "MOVIE", "수동", null);
        when(manualOverrideService.contains(manual))
                .thenReturn(true);

        CachedContentVO second =
                validContent(6L, "TV", "두번째", 40.0);

        List<CachedContentVO> source =
                new ArrayList<CachedContentVO>(
                        Arrays.asList(
                                null,
                                excluded,
                                noId,
                                noType,
                                nullPlatforms,
                                emptyPlatforms,
                                normal,
                                manual,
                                second));

        List<CachedContentVO> result =
                invokeList(
                        "sortAndLimit",
                        source,
                        2);

        assertEquals(2, result.size());
        assertSame(manual, result.get(0));
        assertSame(normal, result.get(1));
    }

    @Test
    void filterAndContentMapShouldCoverNullSourceNullEntriesAndInvalidMetadata() {
        List<CachedContentVO> empty =
                invokeList(
                        "filterByPolicy",
                        (Object) null);
        assertTrue(empty.isEmpty());

        CachedContentVO excluded =
                validContent(1L, "MOVIE", "차단", 1.0);
        when(contentPolicyService.shouldExcludeContent(excluded))
                .thenReturn(true);

        CachedContentVO valid =
                validContent(2L, "TV", "정상", 2.0);

        List<CachedContentVO> filtered =
                invokeList(
                        "filterByPolicy",
                        Arrays.asList(
                                null,
                                excluded,
                                valid));

        assertEquals(List.of(valid), filtered);

        CachedContentVO noId =
                validContent(null, "MOVIE", "ID없음", 3.0);
        CachedContentVO noType =
                validContent(3L, null, "유형없음", 4.0);

        Map<String, CachedContentVO> contentMap =
                invokeMap(
                        "createContentMap",
                        Arrays.asList(
                                null,
                                excluded,
                                noId,
                                noType,
                                valid));

        assertEquals(1, contentMap.size());
        assertSame(
                valid,
                contentMap.get(valid.createContentKey()));
    }

    @Test
    void duplicateRemovalShouldKeepRicherCandidateAndExerciseEveryInformationField() {
        CachedContentVO sparse =
                validContent(
                        10L,
                        "MOVIE",
                        null,
                        null);

        CachedContentVO rich =
                validContent(
                        10L,
                        "MOVIE",
                        "제목",
                        100.0);
        rich.setOriginalTitle("Original");
        rich.setPosterPath("/poster.jpg");
        rich.setReleaseDate("2026-08-10");
        rich.setGenreText("드라마");

        CachedContentVO excluded =
                validContent(
                        11L,
                        "TV",
                        "차단",
                        1.0);
        when(contentPolicyService.shouldExcludeContent(excluded))
                .thenReturn(true);

        List<CachedContentVO> result =
                invokeList(
                        "removeDuplicate",
                        Arrays.asList(
                                null,
                                sparse,
                                rich,
                                excluded));

        assertEquals(1, result.size());
        assertSame(rich, result.get(0));

        Integer richScore = ReflectionTestUtils.invokeMethod(
                service,
                "basicInformationScore",
                rich);
        Integer sparseScore = ReflectionTestUtils.invokeMethod(
                service,
                "basicInformationScore",
                sparse);

        assertEquals(6, richScore.intValue());
        assertEquals(0, sparseScore.intValue());
    }

    @Test
    void publishCheckpointShouldDeliverSortedLimitedSnapshot() {
        CachedContentVO first =
                validContent(1L, "MOVIE", "첫번째", 10.0);
        CachedContentVO second =
                validContent(2L, "MOVIE", "두번째", 20.0);

        Map<String, CachedContentVO> checkpointMap =
                new LinkedHashMap<String, CachedContentVO>();
        checkpointMap.put(
                first.createContentKey(),
                first);
        checkpointMap.put(
                second.createContentKey(),
                second);

        AtomicReference<List<CachedContentVO>> captured =
                new AtomicReference<List<CachedContentVO>>();

        ReflectionTestUtils.invokeMethod(
                service,
                "publishCheckpoint",
                checkpointMap,
                1,
                (java.util.function.Consumer<List<CachedContentVO>>)
                        captured::set);

        assertEquals(1, captured.get().size());
        assertSame(second, captured.get().get(0));
    }

    @SuppressWarnings("unchecked")
    private List<CachedContentVO> invokeList(
            String methodName,
            Object... arguments) {

        return (List<CachedContentVO>)
                ReflectionTestUtils.invokeMethod(
                        service,
                        methodName,
                        arguments);
    }

    @SuppressWarnings("unchecked")
    private Map<String, CachedContentVO> invokeMap(
            String methodName,
            Object... arguments) {

        return (Map<String, CachedContentVO>)
                ReflectionTestUtils.invokeMethod(
                        service,
                        methodName,
                        arguments);
    }

    private CachedContentVO validContent(
            Long tmdbId,
            String contentType,
            String title,
            Double popularity) {

        CachedContentVO content =
                new CachedContentVO();
        content.setTmdbId(tmdbId);
        content.setContentType(contentType);
        content.setTitle(title);
        content.setPopularity(popularity);
        content.setPlatformKeys(List.of("netflix"));
        return content;
    }
}
