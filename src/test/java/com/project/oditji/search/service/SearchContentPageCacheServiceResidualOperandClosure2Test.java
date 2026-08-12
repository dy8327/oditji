package com.project.oditji.search.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.project.oditji.search.vo.CachedContentVO;
import com.project.oditji.tmdb.dao.TmdbDAO;
import com.project.oditji.tmdb.vo.OttPlatformVO;

/** 검색 페이지 캐시의 빈 플랫폼명과 필터 helper의 잔여 피연산자를 보완합니다. */
class SearchContentPageCacheServiceResidualOperandClosure2Test {

    private SearchContentPageCacheService service;

    @BeforeEach
    void setUp() {
        service = new SearchContentPageCacheService(
                mock(SearchContentStore.class),
                mock(TmdbDAO.class));
    }

    @Test
    void platformMapShouldSkipBlankNormalizedPlatformKey() {
        OttPlatformVO blank = new OttPlatformVO();
        blank.setPlatformName("   ");
        OttPlatformVO netflix = new OttPlatformVO();
        netflix.setPlatformName("Netflix");

        @SuppressWarnings("unchecked")
        Map<String, OttPlatformVO> result = (Map<String, OttPlatformVO>) ReflectionTestUtils.invokeMethod(
                service,
                "createPlatformMap",
                Arrays.asList(blank, netflix));

        assertEquals(1, result.size());
        assertEquals(netflix, result.get("netflix"));
    }

    @Test
    void ageRatingListShouldCoverEmptyNormalizedValueAndLaterUniqueValue() {
        @SuppressWarnings("unchecked")
        List<String> result = (List<String>) ReflectionTestUtils.invokeMethod(
                service,
                "normalizeAgeRatingList",
                Arrays.asList("   ", "UNKNOWN-CUSTOM", "UNKNOWN-CUSTOM", "15세"));

        assertEquals(2, result.size());
        assertEquals("UNKNOWN-CUSTOM", result.get(0));
        assertEquals("15세 이상 관람가", result.get(1));
    }

    @Test
    void contentCategoryShouldCoverMovieWithDocumentaryAndTvWithoutVariety() {
        CachedContentVO movieDocumentary = new CachedContentVO();
        movieDocumentary.setContentType("MOVIE");
        movieDocumentary.setGenreText("다큐멘터리");

        Boolean movie = ReflectionTestUtils.invokeMethod(
                service,
                "matchesContentCategory",
                movieDocumentary,
                "MOVIE");
        Boolean documentary = ReflectionTestUtils.invokeMethod(
                service,
                "matchesContentCategory",
                movieDocumentary,
                "DOCUMENTARY");

        CachedContentVO tvDrama = new CachedContentVO();
        tvDrama.setContentType("TV");
        tvDrama.setGenreText("드라마");

        Boolean variety = ReflectionTestUtils.invokeMethod(
                service,
                "matchesContentCategory",
                tvDrama,
                "VARIETY");

        assertFalse(Boolean.TRUE.equals(movie));
        assertTrue(Boolean.TRUE.equals(documentary));
        assertFalse(Boolean.TRUE.equals(variety));
    }
}
