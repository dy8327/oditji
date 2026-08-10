package com.project.oditji.search.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.project.oditji.search.vo.CachedContentVO;

/**
 * 상세 보강 재사용 판정의 각 필수조건과 영화/TV 추가 조건을 보완합니다.
 */
class SearchContentEnrichmentServiceFinalCoverageTest {

    private SearchContentManualOverrideService manualOverrideService;
    private SearchContentAgeRatingResolver ageRatingResolver;
    private SearchContentEnrichmentService service;

    @BeforeEach
    void setUp() {
        manualOverrideService =
                mock(SearchContentManualOverrideService.class);
        ageRatingResolver =
                mock(SearchContentAgeRatingResolver.class);

        service = new SearchContentEnrichmentService(
                mock(TmdbApiClient.class),
                manualOverrideService,
                ageRatingResolver);
    }

    @Test
    void reusableDetailShouldRejectEveryMissingRequiredField() {
        assertFalse(service.hasReusableDetail(null));

        CachedContentVO content = validMovie();

        content.setTmdbId(null);
        assertFalse(service.hasReusableDetail(content));

        content = validMovie();
        content.setContentType(" ");
        assertFalse(service.hasReusableDetail(content));

        content = validMovie();
        content.setTitle(null);
        assertFalse(service.hasReusableDetail(content));

        content = validMovie();
        content.setGenreText(" ");
        assertFalse(service.hasReusableDetail(content));

        content = validMovie();
        content.setPlatformKeys(null);
        assertFalse(service.hasReusableDetail(content));

        content = validMovie();
        content.setPlatformKeys(List.of());
        assertFalse(service.hasReusableDetail(content));

        content = validMovie();
        content.setSearchText(" ");
        assertFalse(service.hasReusableDetail(content));
    }

    @Test
    void reusableDetailShouldRejectUnknownAgeAndIncompleteTvFields() {
        CachedContentVO movie = validMovie();

        when(ageRatingResolver.isNormalizedAgeRating(
                movie.getAgeRating()))
                .thenReturn(false);

        assertFalse(
                service.hasReusableDetail(movie));

        CachedContentVO noEpisodes = validTv();
        when(ageRatingResolver.isNormalizedAgeRating(
                noEpisodes.getAgeRating()))
                .thenReturn(true);
        noEpisodes.setEpisodeCount(null);

        assertFalse(
                service.hasReusableDetail(noEpisodes));

        CachedContentVO noDate = validTv();
        when(ageRatingResolver.isNormalizedAgeRating(
                noDate.getAgeRating()))
                .thenReturn(true);
        noDate.setLastAirDate(" ");

        assertFalse(
                service.hasReusableDetail(noDate));
    }

    @Test
    void reusableMovieShouldRequireRuntimeOnlyForManualOverride() {
        CachedContentVO normal = validMovie();

        when(ageRatingResolver.isNormalizedAgeRating(
                normal.getAgeRating()))
                .thenReturn(true);

        when(manualOverrideService.contains(normal))
                .thenReturn(false);

        normal.setRuntime(null);

        assertTrue(
                service.hasReusableDetail(normal));

        CachedContentVO manualMissingRuntime =
                validMovie();

        when(ageRatingResolver.isNormalizedAgeRating(
                manualMissingRuntime.getAgeRating()))
                .thenReturn(true);

        when(manualOverrideService.contains(
                manualMissingRuntime))
                .thenReturn(true);

        manualMissingRuntime.setRuntime(null);

        assertFalse(
                service.hasReusableDetail(
                        manualMissingRuntime));

        manualMissingRuntime.setRuntime(120);

        assertTrue(
                service.hasReusableDetail(
                        manualMissingRuntime));
    }

    @Test
    void completeTvDetailShouldBeReusable() {
        CachedContentVO tv = validTv();

        when(ageRatingResolver.isNormalizedAgeRating(
                tv.getAgeRating()))
                .thenReturn(true);

        assertTrue(
                service.hasReusableDetail(tv));
    }

    private CachedContentVO validMovie() {
        CachedContentVO content = base("MOVIE");
        content.setRuntime(100);
        return content;
    }

    private CachedContentVO validTv() {
        CachedContentVO content = base("TV");
        content.setEpisodeCount(12);
        content.setLastAirDate("2026-08-01");
        return content;
    }

    private CachedContentVO base(String type) {
        CachedContentVO content = new CachedContentVO();
        content.setTmdbId(1L);
        content.setContentType(type);
        content.setTitle("제목");
        content.setGenreText("드라마");
        content.setPlatformKeys(List.of("netflix"));
        content.setSearchText("제목드라마");
        content.setAgeRating("15세 이상 관람가");
        return content;
    }
}
