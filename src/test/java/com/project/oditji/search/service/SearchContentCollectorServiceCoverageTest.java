package com.project.oditji.search.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.project.oditji.search.vo.CachedContentVO;

/** 기존 스냅샷 재사용, 후보 중복 제거, 배치 보강과 체크포인트 구성을 검증합니다. */
@ExtendWith(MockitoExtension.class)
class SearchContentCollectorServiceCoverageTest {

    @Mock
    private SearchContentDiscoverService discoverService;

    @Mock
    private SearchContentEnrichmentService enrichmentService;

    @Mock
    private SearchContentManualOverrideService manualOverrideService;

    @Mock
    private SearchContentAgeRatingService ageRatingService;

    @Mock
    private TmdbProviderService providerService;

    @Mock
    private SearchContentPolicyService contentPolicyService;

    private SearchContentCollectorService service;
    private TmdbProviderRegistry registry;

    @BeforeEach
    void setUp() {
        service = new SearchContentCollectorService(
                discoverService,
                enrichmentService,
                manualOverrideService,
                ageRatingService,
                providerService,
                contentPolicyService);
        ReflectionTestUtils.setField(service, "maxSize", 10);
        ReflectionTestUtils.setField(service, "movieRatio", 0.5);
        ReflectionTestUtils.setField(service, "batchSize", 1);

        registry = new TmdbProviderRegistry(
                Map.of(8, "netflix"),
                Map.of(337, "disney"));
        lenient().when(providerService.loadRegistry()).thenReturn(registry);
        lenient().when(contentPolicyService.shouldExcludeContent(any()))
                .thenAnswer(invocation -> {
                    CachedContentVO content = invocation.getArgument(0);
                    return content != null && "차단".equals(content.getTitle());
                });
        lenient().when(manualOverrideService.contains(any()))
                .thenAnswer(invocation -> {
                    CachedContentVO content = invocation.getArgument(0);
                    return content != null && content.getTmdbId() != null
                            && content.getTmdbId() == 99L;
                });
    }

    @Test
    void collectShouldReusePreviousEnrichPendingFilterAndPublishCheckpoints() {
        CachedContentVO previousReusable = content(
                1L,
                "MOVIE",
                "이전 상세",
                30.0,
                List.of("netflix"));
        previousReusable.setAgeRatingRetryCount(1);
        previousReusable.setAgeRatingLastCheckedAt("2026-08-01T10:00:00");
        CachedContentVO previousBlocked = content(
                2L,
                "MOVIE",
                "차단",
                40.0,
                List.of("netflix"));

        CachedContentVO duplicateSparse = content(
                3L,
                "MOVIE",
                null,
                90.0,
                List.of());
        CachedContentVO duplicateRich = content(
                3L,
                "MOVIE",
                "풍부한 후보",
                80.0,
                List.of());
        duplicateRich.setOriginalTitle("Rich Original");
        duplicateRich.setPosterPath("/poster.jpg");
        duplicateRich.setReleaseDate("2026-08-01");
        duplicateRich.setGenreText("드라마");

        CachedContentVO reusableCandidate = content(
                1L,
                "MOVIE",
                "새 후보",
                50.0,
                List.of());
        CachedContentVO pendingCandidate = content(
                4L,
                "TV",
                "신규 후보",
                60.0,
                List.of());
        CachedContentVO blockedCandidate = content(
                5L,
                "TV",
                "차단",
                70.0,
                List.of());
        CachedContentVO manualCandidate = content(
                99L,
                "MOVIE",
                null,
                null,
                List.of());
        CachedContentVO invalid = new CachedContentVO();

        when(discoverService.collectMovieCandidates(50, registry.getMovieProviderIds()))
                .thenReturn(List.of(
                        duplicateSparse,
                        duplicateRich,
                        reusableCandidate,
                        blockedCandidate,
                        invalid));
        when(discoverService.collectTvCandidates(50, registry.getTvProviderIds()))
                .thenReturn(List.of(pendingCandidate));
        when(discoverService.collectSupplementCandidates())
                .thenReturn(Arrays.asList(duplicateSparse, null));
        when(manualOverrideService.createCandidates())
                .thenReturn(List.of(manualCandidate));

        when(enrichmentService.hasReusableDetail(previousReusable)).thenReturn(true);
        when(enrichmentService.hasReusableDetail(null)).thenReturn(false);
        doAnswer(invocation -> {
            CachedContentVO source = invocation.getArgument(0);
            CachedContentVO target = invocation.getArgument(1);
            target.setTitle(source.getTitle());
            target.setPlatformKeys(source.getPlatformKeys());
            return null;
        }).when(enrichmentService).copyReusableDetail(
                same(previousReusable),
                same(reusableCandidate));
        when(enrichmentService.createSearchText(reusableCandidate))
                .thenReturn("이전상세");

        CachedContentVO enrichedPending = content(
                4L,
                "TV",
                "신규 보강",
                60.0,
                List.of("disney"));
        CachedContentVO enrichedManual = content(
                99L,
                "MOVIE",
                "수동 보강",
                null,
                List.of("netflix"));
        CachedContentVO enrichedDuplicate = content(
                3L,
                "MOVIE",
                "풍부한 후보",
                80.0,
                List.of("netflix"));
        CachedContentVO noPlatform = content(
                6L,
                "MOVIE",
                "제공처 없음",
                10.0,
                List.of());
        CachedContentVO excluded = content(
                7L,
                "TV",
                "차단",
                20.0,
                List.of("disney"));

        when(enrichmentService.enrichBatch(anyList(), same(registry)))
                .thenAnswer(invocation -> {
                    List<CachedContentVO> batch = invocation.getArgument(0);
                    List<CachedContentVO> enriched = new ArrayList<CachedContentVO>();
                    for (CachedContentVO candidate : batch) {
                        if (candidate.getTmdbId() == 99L) {
                            enriched.add(enrichedManual);
                        } else if (candidate.getTmdbId() == 4L) {
                            enriched.add(enrichedPending);
                        } else if (candidate.getTmdbId() == 3L) {
                            enriched.add(enrichedDuplicate);
                        }
                    }
                    enriched.add(noPlatform);
                    enriched.add(excluded);
                    enriched.add(null);
                    return enriched;
                });

        List<List<CachedContentVO>> checkpoints = new ArrayList<List<CachedContentVO>>();
        List<CachedContentVO> result = service.collect(
                Arrays.asList(previousReusable, previousBlocked, null),
                checkpoint -> checkpoints.add(new ArrayList<CachedContentVO>(checkpoint)));

        assertEquals(4, result.size());
        assertEquals(99L, result.get(0).getTmdbId());
        assertTrue(result.stream().anyMatch(item -> item.getTmdbId() == 1L));
        assertTrue(result.stream().anyMatch(item -> item.getTmdbId() == 3L));
        assertTrue(result.stream().anyMatch(item -> item.getTmdbId() == 4L));
        assertFalse(result.stream().anyMatch(item -> item.getTmdbId() == 5L));
        assertFalse(result.stream().anyMatch(item -> item.getTmdbId() == 6L));
        assertEquals(1, reusableCandidate.getAgeRatingRetryCount());
        assertEquals("2026-08-01T10:00:00", reusableCandidate.getAgeRatingLastCheckedAt());
        assertEquals("이전상세", reusableCandidate.getSearchText());
        assertFalse(checkpoints.isEmpty());

        verify(manualOverrideService).reload();
        verify(ageRatingService).reload();
        verify(ageRatingService, times(2)).applyManualOverrides(anyList());
        verify(ageRatingService).recheckUnknownAgeRatings(anyList());
        verify(ageRatingService).recheckRestrictedAgeRatings(anyList());
        verify(ageRatingService).markLookupCompleted(enrichedPending);
        verify(ageRatingService).markLookupCompleted(enrichedManual);
        verify(ageRatingService).markLookupCompleted(enrichedDuplicate);
        verify(ageRatingService).writeMissingCandidates(result);
    }

    @Test
    void collectCompatibilityMethodShouldHandleNullAndNoCandidates() {
        when(discoverService.collectMovieCandidates(50, registry.getMovieProviderIds()))
                .thenReturn(List.of());
        when(discoverService.collectTvCandidates(50, registry.getTvProviderIds()))
                .thenReturn(List.of());
        when(discoverService.collectSupplementCandidates()).thenReturn(List.of());
        when(manualOverrideService.createCandidates()).thenReturn(List.of());

        List<CachedContentVO> result = service.collect(null);

        assertTrue(result.isEmpty());
        verify(enrichmentService, never()).enrichBatch(anyList(), same(registry));
        verify(ageRatingService).writeMissingCandidates(result);
    }

    @Test
    void maximumSizeAndRatioShouldBeNormalized() {
        ReflectionTestUtils.setField(service, "maxSize", 100);
        ReflectionTestUtils.setField(service, "movieRatio", 2.0);
        ReflectionTestUtils.setField(service, "batchSize", 5000);

        List<CachedContentVO> many = new ArrayList<CachedContentVO>();
        for (long id = 1; id <= 110; id++) {
            many.add(content(id, "MOVIE", "후보 " + id, (double) id, List.of()));
        }

        when(discoverService.collectMovieCandidates(100, registry.getMovieProviderIds()))
                .thenReturn(many);
        when(discoverService.collectTvCandidates(0, registry.getTvProviderIds()))
                .thenReturn(List.of());
        when(discoverService.collectSupplementCandidates()).thenReturn(List.of());
        when(manualOverrideService.createCandidates()).thenReturn(List.of());
        AtomicInteger batchSize = new AtomicInteger();
        when(enrichmentService.enrichBatch(anyList(), same(registry)))
                .thenAnswer(invocation -> {
                    List<CachedContentVO> batch = invocation.getArgument(0);
                    batchSize.set(batch.size());
                    for (CachedContentVO item : batch) {
                        item.setPlatformKeys(List.of("netflix"));
                    }
                    return batch;
                });

        List<CachedContentVO> result = service.collect(List.of());

        assertEquals(100, result.size());
        assertEquals(100, batchSize.get());
    }

    private CachedContentVO content(
            Long tmdbId,
            String contentType,
            String title,
            Double popularity,
            List<String> platforms) {
        CachedContentVO content = new CachedContentVO();
        content.setTmdbId(tmdbId);
        content.setContentType(contentType);
        content.setTitle(title);
        content.setPopularity(popularity);
        content.setPlatformKeys(platforms);
        return content;
    }
}
