package com.project.oditji.search.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.project.oditji.search.vo.CachedContentVO;

/** 기존 스냅샷 상세정보 재사용, 복사와 검색문자열 생성 로직을 검증합니다. */
@ExtendWith(MockitoExtension.class)
class SearchContentEnrichmentServiceTest {

    @Mock
    private TmdbApiClient apiClient;

    @Mock
    private SearchContentManualOverrideService manualOverrideService;

    private SearchContentEnrichmentService enrichmentService;

    @BeforeEach
    void setUp() {
        enrichmentService = new SearchContentEnrichmentService(
                apiClient,
                manualOverrideService,
                new SearchContentAgeRatingResolver());
    }

    @Test
    void reusableDetailShouldRequireEveryCommonField() {
        assertFalse(enrichmentService.hasReusableDetail(null));

        CachedContentVO content = completeMovie();
        assertTrue(enrichmentService.hasReusableDetail(content));

        content.setTmdbId(null);
        assertFalse(enrichmentService.hasReusableDetail(content));
        content = completeMovie();
        content.setContentType(" ");
        assertFalse(enrichmentService.hasReusableDetail(content));
        content = completeMovie();
        content.setTitle(null);
        assertFalse(enrichmentService.hasReusableDetail(content));
        content = completeMovie();
        content.setGenreText(" ");
        assertFalse(enrichmentService.hasReusableDetail(content));
        content = completeMovie();
        content.setPlatformKeys(List.of());
        assertFalse(enrichmentService.hasReusableDetail(content));
        content = completeMovie();
        content.setSearchText(null);
        assertFalse(enrichmentService.hasReusableDetail(content));
        content = completeMovie();
        content.setAgeRating("R");
        assertFalse(enrichmentService.hasReusableDetail(content));
    }

    @Test
    void reusableTvShouldRequireEpisodeCountAndLastAirDate() {
        CachedContentVO tv = completeMovie();
        tv.setContentType("TV");
        tv.setEpisodeCount(null);
        tv.setLastAirDate("2026-08-01");
        assertFalse(enrichmentService.hasReusableDetail(tv));

        tv.setEpisodeCount(12);
        tv.setLastAirDate(null);
        assertFalse(enrichmentService.hasReusableDetail(tv));

        tv.setLastAirDate("2026-08-01");
        assertTrue(enrichmentService.hasReusableDetail(tv));
    }

    @Test
    void manualMovieShouldRequireRuntime() {
        CachedContentVO movie = completeMovie();
        movie.setRuntime(null);
        when(manualOverrideService.contains(movie)).thenReturn(true);
        assertFalse(enrichmentService.hasReusableDetail(movie));

        movie.setRuntime(120);
        assertTrue(enrichmentService.hasReusableDetail(movie));

        CachedContentVO normalMovie = completeMovie();
        normalMovie.setRuntime(null);
        when(manualOverrideService.contains(normalMovie)).thenReturn(false);
        assertTrue(enrichmentService.hasReusableDetail(normalMovie));
    }

    @Test
    void copyReusableDetailShouldCopyValuesAndDefensivelyCopyPlatforms() {
        CachedContentVO source = completeMovie();
        source.setOriginalTitle("Original");
        source.setPosterPath("poster.jpg");
        source.setReleaseDate("2026-01-01");
        source.setLastAirDate("2026-02-01");
        source.setTmdbScore(8.5);
        source.setPopularity(100.0);
        source.setRuntime(120);
        source.setEpisodeCount(10);
        source.setDirector("감독");
        source.setCastNames("배우1, 배우2");
        source.setAgeRatingRestrictionChecked(Boolean.TRUE);

        CachedContentVO target = new CachedContentVO();
        enrichmentService.copyReusableDetail(source, target);

        assertEquals(source.getTitle(), target.getTitle());
        assertEquals(source.getOriginalTitle(), target.getOriginalTitle());
        assertEquals(source.getPosterPath(), target.getPosterPath());
        assertEquals(source.getReleaseDate(), target.getReleaseDate());
        assertEquals(source.getLastAirDate(), target.getLastAirDate());
        assertEquals(source.getGenreText(), target.getGenreText());
        assertEquals(source.getTmdbScore(), target.getTmdbScore());
        assertEquals(source.getPopularity(), target.getPopularity());
        assertEquals(source.getRuntime(), target.getRuntime());
        assertEquals(source.getEpisodeCount(), target.getEpisodeCount());
        assertEquals(source.getDirector(), target.getDirector());
        assertEquals(source.getCastNames(), target.getCastNames());
        assertEquals(source.getAgeRating(), target.getAgeRating());
        assertEquals(Boolean.TRUE, target.getAgeRatingRestrictionChecked());
        assertEquals(source.getPlatformKeys(), target.getPlatformKeys());
        assertNotSame(source.getPlatformKeys(), target.getPlatformKeys());

        source.setPlatformKeys(null);
        enrichmentService.copyReusableDetail(source, target);
        assertTrue(target.getPlatformKeys().isEmpty());

        enrichmentService.copyReusableDetail(null, target);
        enrichmentService.copyReusableDetail(source, null);
    }

    @Test
    void searchTextShouldNormalizeCaseSpacesSymbolsAndUnicodeWidth() {
        CachedContentVO content = new CachedContentVO();
        content.setTitle("  ODITJI: 콘텐츠 ");
        content.setOriginalTitle("ＯＴＴ Movie");
        content.setDirector("Kim Director");
        content.setCastNames("Actor-1, Actor 2");

        assertEquals(
                "oditji콘텐츠ottmoviekimdirectoractor1actor2",
                enrichmentService.createSearchText(content));

        CachedContentVO empty = new CachedContentVO();
        assertEquals("", enrichmentService.createSearchText(empty));
    }

    private CachedContentVO completeMovie() {
        CachedContentVO content = new CachedContentVO();
        content.setTmdbId(1L);
        content.setContentType("MOVIE");
        content.setTitle("영화");
        content.setGenreText("드라마");
        content.setPlatformKeys(List.of("netflix"));
        content.setSearchText("영화드라마");
        content.setAgeRating("15세 이상 관람가");
        return content;
    }
}
