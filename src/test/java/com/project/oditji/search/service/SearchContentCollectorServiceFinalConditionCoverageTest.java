package com.project.oditji.search.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.project.oditji.search.vo.CachedContentVO;

/**
 * 수집기 helper의 AND/OR 단락 조건에서 남기 쉬운 false 결과를 검증합니다.
 */
class SearchContentCollectorServiceFinalConditionCoverageTest {

    private SearchContentCollectorService service;

    @BeforeEach
    void setUp() {
        service = new SearchContentCollectorService(
                mock(SearchContentDiscoverService.class),
                mock(SearchContentEnrichmentService.class),
                mock(SearchContentManualOverrideService.class),
                mock(SearchContentAgeRatingService.class),
                mock(TmdbProviderService.class),
                mock(SearchContentPolicyService.class));
    }

    @Test
    void basicInformationScoreShouldEvaluateBlankSecondOperands() {
        CachedContentVO blank = new CachedContentVO();
        blank.setTitle("   ");
        blank.setOriginalTitle("   ");
        blank.setPosterPath("   ");
        blank.setReleaseDate("   ");
        blank.setGenreText("   ");
        blank.setPopularity(null);

        Integer score = ReflectionTestUtils.invokeMethod(
                service,
                "basicInformationScore",
                blank);

        assertEquals(0, score.intValue());
    }

    @Test
    void duplicateRemovalShouldKeepExistingWhenLaterCandidateIsNotRicher() {
        CachedContentVO rich = content(10L, "MOVIE", "제목", 10.0);
        rich.setOriginalTitle("Original");
        rich.setPosterPath("/poster.jpg");
        rich.setReleaseDate("2026-08-11");
        rich.setGenreText("드라마");

        CachedContentVO poor = content(10L, "MOVIE", "제목", null);

        List<CachedContentVO> result = ReflectionTestUtils.invokeMethod(
                service,
                "removeDuplicate",
                List.of(rich, poor));

        assertEquals(1, result.size());
        assertSame(rich, result.get(0));
    }

    private CachedContentVO content(
            Long tmdbId,
            String contentType,
            String title,
            Double popularity) {

        CachedContentVO content = new CachedContentVO();
        content.setTmdbId(tmdbId);
        content.setContentType(contentType);
        content.setTitle(title);
        content.setPopularity(popularity);
        content.setPlatformKeys(List.of("netflix"));
        return content;
    }
}
