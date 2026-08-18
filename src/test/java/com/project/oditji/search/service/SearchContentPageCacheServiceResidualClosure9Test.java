package com.project.oditji.search.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.Month;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.project.oditji.search.vo.CachedContentVO;
import com.project.oditji.search.vo.SearchResultPageVO;
import com.project.oditji.search.vo.SearchResultVO;
import com.project.oditji.tmdb.dao.TmdbDAO;

/** SonarQube에 남은 페이징·루프·등급 정규화 조건을 정확히 보완합니다. */
class SearchContentPageCacheServiceResidualClosure9Test {

    private SearchContentStore searchContentStore;
    private TmdbDAO tmdbDAO;
    private SearchContentPageCacheService service;

    @BeforeEach
    void setUp() {
        searchContentStore = mock(SearchContentStore.class);
        tmdbDAO = mock(TmdbDAO.class);
        service = new SearchContentPageCacheService(searchContentStore, tmdbDAO);

        when(tmdbDAO.selectActivePlatformList()).thenReturn(List.of());
    }

    @Test
    void appendReleasedContentShouldStopWhenLimitBecomesFullInsideLoop() {
        LocalDate startDate = LocalDate.of(2026, Month.AUGUST, 1);
        LocalDate endDate = LocalDate.of(2026, Month.AUGUST, 31);

        SearchResultVO first = result(1L, "2026-08-10");
        SearchResultVO second = result(2L, "2026-08-11");
        Map<String, SearchResultVO> selectedMap = new LinkedHashMap<>();

        ReflectionTestUtils.invokeMethod(
                service,
                "appendReleasedContent",
                selectedMap,
                List.of(first, second),
                startDate,
                endDate,
                1);

        assertEquals(1, selectedMap.size());
        assertEquals(first, selectedMap.get("MOVIE:1"));
    }

    @Test
    void contentPageShouldClampOversizedPageToLastAvailablePage() {
        when(searchContentStore.getAll()).thenReturn(List.of(
                cached(10L, "첫 번째"),
                cached(11L, "두 번째")));

        SearchResultPageVO page = service.getContentPage(
                "",
                99,
                1,
                List.of(),
                List.of(),
                List.of(),
                List.of());

        assertEquals(2, page.getPage());
        assertEquals(2, page.getTotalPages());
        assertEquals(2, page.getTotalResults());
        assertEquals(1, page.getResultList().size());
    }

    @Test
    void ageRatingListShouldCoverEmptyNormalizedValueAndDuplicateValue() {
        String trimToEmptyButNotBlank = String.valueOf((char) 0);

        List<?> normalized = ReflectionTestUtils.invokeMethod(
                service,
                "normalizeAgeRatingList",
                List.of(
                        trimToEmptyButNotBlank,
                        "15세 이상 관람가",
                        "15세 이상 관람가"));

        assertEquals(List.of("15세 이상 관람가"), normalized);
    }

    private CachedContentVO cached(Long tmdbId, String title) {
        CachedContentVO content = new CachedContentVO();
        content.setTmdbId(tmdbId);
        content.setContentType("MOVIE");
        content.setTitle(title);
        content.setPlatformKeys(List.of("netflix"));
        return content;
    }

    private SearchResultVO result(Long tmdbId, String releaseDate) {
        SearchResultVO result = new SearchResultVO();
        result.setTmdbId(tmdbId);
        result.setContentType("MOVIE");
        result.setReleaseDate(releaseDate);
        return result;
    }
}
