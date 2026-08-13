package com.project.oditji.search.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.project.oditji.search.vo.CachedContentVO;

/** 배치 보강 결과의 null/empty 플랫폼 단축평가 분기를 직접 보완합니다. */
class SearchContentCollectorServicePlatformOperandCoverageTest {

    private SearchContentDiscoverService discoverService;
    private SearchContentEnrichmentService enrichmentService;
    private SearchContentManualOverrideService manualOverrideService;
    private SearchContentAgeRatingService ageRatingService;
    private TmdbProviderService providerService;
    private SearchContentPolicyService contentPolicyService;
    private SearchContentCollectorService service;
    private TmdbProviderRegistry registry;

    @BeforeEach
    void setUp() {
        discoverService = mock(SearchContentDiscoverService.class);
        enrichmentService = mock(SearchContentEnrichmentService.class);
        manualOverrideService = mock(SearchContentManualOverrideService.class);
        ageRatingService = mock(SearchContentAgeRatingService.class);
        providerService = mock(TmdbProviderService.class);
        contentPolicyService = mock(SearchContentPolicyService.class);

        service = new SearchContentCollectorService(
                discoverService,
                enrichmentService,
                manualOverrideService,
                ageRatingService,
                providerService,
                contentPolicyService);

        ReflectionTestUtils.setField(service, "maxSize", 100);
        ReflectionTestUtils.setField(service, "movieRatio", 1.0);
        ReflectionTestUtils.setField(service, "batchSize", 50);

        registry = new TmdbProviderRegistry(
                Map.of(8, "netflix"),
                Map.of());
        when(providerService.loadRegistry()).thenReturn(registry);
    }

    @Test
    void collectShouldRejectNullAndEmptyPlatformsBeforeAcceptingValidContent() {
        CachedContentVO candidate = content(1L, List.of());

        when(discoverService.collectMovieCandidates(
                100,
                registry.getMovieProviderIds()))
                .thenReturn(List.of(candidate));
        when(discoverService.collectTvCandidates(
                0,
                registry.getTvProviderIds()))
                .thenReturn(List.of());
        when(discoverService.collectSupplementCandidates())
                .thenReturn(List.of());
        when(manualOverrideService.createCandidates())
                .thenReturn(List.of());

        CachedContentVO nullPlatforms = content(2L, null);
        CachedContentVO emptyPlatforms = content(3L, List.of());
        CachedContentVO valid = content(4L, List.of("netflix"));

        when(enrichmentService.enrichBatch(anyList(), same(registry)))
                .thenReturn(Arrays.asList(
                        nullPlatforms,
                        emptyPlatforms,
                        valid));

        List<CachedContentVO> result = service.collect(List.of());

        assertEquals(1, result.size());
        assertSame(valid, result.get(0));
    }

    private CachedContentVO content(Long tmdbId, List<String> platformKeys) {
        CachedContentVO content = new CachedContentVO();
        content.setTmdbId(tmdbId);
        content.setContentType("MOVIE");
        content.setTitle("콘텐츠 " + tmdbId);
        content.setPopularity(10.0 + tmdbId);
        content.setPlatformKeys(platformKeys);
        return content;
    }
}
