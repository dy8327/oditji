package com.project.oditji.search.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.json.JSONArray;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.project.oditji.search.vo.CachedContentVO;

/**
 * SonarQube에 남은 SearchContentEnrichmentService의 실제 진입 가능한
 * 빈 배열 결과와 재사용 판정 조건을 보완합니다.
 */
class SearchContentEnrichmentServiceResidualClosure5Test {

    private SearchContentManualOverrideService manualOverrideService;
    private SearchContentAgeRatingResolver ageRatingResolver;
    private SearchContentEnrichmentService service;

    @BeforeEach
    void setUp() {
        manualOverrideService = mock(SearchContentManualOverrideService.class);
        ageRatingResolver = mock(SearchContentAgeRatingResolver.class);

        service = new SearchContentEnrichmentService(
                mock(TmdbApiClient.class),
                manualOverrideService,
                ageRatingResolver);
    }

    @Test
    void parserHelpersShouldReturnNullWhenNonNullArraysProduceNoNames() {
        JSONArray empty = new JSONArray();

        assertNull(ReflectionTestUtils.invokeMethod(
                service,
                "parseGenreNames",
                empty));
        assertNull(ReflectionTestUtils.invokeMethod(
                service,
                "parseMovieDirector",
                empty));
        assertNull(ReflectionTestUtils.invokeMethod(
                service,
                "parseTvCreator",
                empty));
        assertNull(ReflectionTestUtils.invokeMethod(
                service,
                "parseCastNames",
                empty));
    }

    @Test
    void reusableDetailShouldEvaluateNullEmptyAndPopulatedPlatformLists() {
        CachedContentVO content = reusableMovie();
        when(ageRatingResolver.isNormalizedAgeRating(content.getAgeRating()))
                .thenReturn(true);
        when(manualOverrideService.contains(content))
                .thenReturn(false);

        content.setPlatformKeys(null);
        assertFalse(service.hasReusableDetail(content));

        content.setPlatformKeys(List.of());
        assertFalse(service.hasReusableDetail(content));

        content.setPlatformKeys(List.of("netflix"));
        assertTrue(service.hasReusableDetail(content));
    }

    private CachedContentVO reusableMovie() {
        CachedContentVO content = new CachedContentVO();
        content.setTmdbId(1L);
        content.setContentType("MOVIE");
        content.setTitle("제목");
        content.setGenreText("드라마");
        content.setPlatformKeys(List.of("netflix"));
        content.setSearchText("제목드라마");
        content.setAgeRating("15세 이상 관람가");
        content.setRuntime(120);
        return content;
    }
}
