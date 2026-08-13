package com.project.oditji.search.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.Arrays;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.project.oditji.search.vo.CachedContentVO;

/** 수집기 콘텐츠 맵의 null 소스와 정보 점수 short-circuit 조합을 보완합니다. */
class SearchContentCollectorServiceNullMapCoverageTest {

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
    void contentMapShouldReturnEmptyForNullSource() {
        Map<?, ?> result = ReflectionTestUtils.invokeMethod(
                service,
                "createContentMap",
                (Object) null);

        assertTrue(result.isEmpty());
    }

    @Test
    void basicInformationScoreShouldEvaluateNonNullBlankAndNonBlankPairs() {
        CachedContentVO content = new CachedContentVO();
        content.setTitle("제목");
        content.setOriginalTitle("   ");
        content.setPosterPath("/poster.jpg");
        content.setReleaseDate("   ");
        content.setGenreText("드라마");
        content.setPopularity(null);

        Integer score = ReflectionTestUtils.invokeMethod(
                service,
                "basicInformationScore",
                content);

        assertEquals(3, score.intValue());
    }

    @Test
    void contentMapShouldSkipNullIdAndNullTypeAfterPolicyCheck() {
        CachedContentVO noId = new CachedContentVO();
        noId.setContentType("MOVIE");

        CachedContentVO noType = new CachedContentVO();
        noType.setTmdbId(2L);

        Map<?, ?> result = ReflectionTestUtils.invokeMethod(
                service,
                "createContentMap",
                Arrays.asList(null, noId, noType));

        assertTrue(result.isEmpty());
    }
}
