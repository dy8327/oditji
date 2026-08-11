package com.project.oditji.search.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.project.oditji.content.vo.ContentVO;
import com.project.oditji.search.vo.CachedContentVO;
import com.project.oditji.search.vo.SearchResultVO;
import com.project.oditji.tmdb.dao.TmdbDAO;

/** 검색 페이지 캐시의 긴 AND/OR 단락 조건을 각 실패 지점별로 검증합니다. */
class SearchContentPageCacheServiceShortCircuitCoverageTest {

    private SearchContentPageCacheService service;

    @BeforeEach
    void setUp() {
        service = new SearchContentPageCacheService(
                mock(SearchContentStore.class),
                mock(TmdbDAO.class));
    }

    @Test
    void matchesSearchFiltersShouldCoverEveryShortCircuitPosition() {
        List<String> empty = List.of();
        Set<String> noProviders = Set.of();

        assertFalse(matches(null, "", empty, empty, empty, noProviders));

        CachedContentVO noId = baseContent();
        noId.setTmdbId(null);
        assertFalse(matches(noId, "", empty, empty, empty, noProviders));

        CachedContentVO noType = baseContent();
        noType.setContentType(null);
        assertFalse(matches(noType, "", empty, empty, empty, noProviders));

        CachedContentVO keywordMiss = baseContent();
        keywordMiss.setSearchText("다른검색어");
        assertFalse(matches(keywordMiss, "없는검색어", empty, empty, empty, noProviders));

        CachedContentVO categoryMiss = baseContent();
        categoryMiss.setContentType("TV");
        assertFalse(matches(
                categoryMiss,
                "",
                List.of("MOVIE"),
                empty,
                empty,
                noProviders));

        CachedContentVO genreMiss = baseContent();
        genreMiss.setGenreText("드라마");
        assertFalse(matches(
                genreMiss,
                "",
                empty,
                List.of("ACTION"),
                empty,
                noProviders));

        CachedContentVO ageMiss = baseContent();
        ageMiss.setAgeRating("12세 이상 관람가");
        assertFalse(matches(
                ageMiss,
                "",
                empty,
                empty,
                List.of("15세 이상 관람가"),
                noProviders));

        CachedContentVO providerMiss = baseContent();
        providerMiss.setPlatformKeys(List.of("wavve"));
        assertFalse(matches(
                providerMiss,
                "",
                empty,
                empty,
                empty,
                Set.of("netflix")));

        CachedContentVO matched = baseContent();
        matched.setPlatformKeys(List.of("netflix"));
        assertTrue(matches(
                matched,
                "",
                empty,
                empty,
                empty,
                Set.of("netflix")));
    }

    @Test
    void appendHelpersShouldCoverNullSourcesLimitReachedAndLoopBreak() {
        Map<String, SearchResultVO> selected = new LinkedHashMap<String, SearchResultVO>();

        ReflectionTestUtils.invokeMethod(
                service,
                "appendReleasedContent",
                selected,
                null,
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 12, 31),
                2);

        SearchResultVO existing = result(1L, "MOVIE", "2026-05-01");
        selected.put("MOVIE:1", existing);

        ReflectionTestUtils.invokeMethod(
                service,
                "appendReleasedContent",
                selected,
                List.of(result(2L, "MOVIE", "2026-05-02")),
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 12, 31),
                1);

        ReflectionTestUtils.invokeMethod(
                service,
                "appendAllContent",
                new LinkedHashMap<String, SearchResultVO>(),
                null,
                2);

        Map<String, SearchResultVO> oneLimit = new LinkedHashMap<String, SearchResultVO>();
        List<SearchResultVO> source = new ArrayList<SearchResultVO>();
        source.add(result(3L, "MOVIE", null));
        source.add(result(4L, "MOVIE", null));

        ReflectionTestUtils.invokeMethod(
                service,
                "appendAllContent",
                oneLimit,
                source,
                1);

        assertTrue(oneLimit.size() == 1);
    }

    @Test
    void relatedReasonShouldCoverCandidateGenrePresentButCurrentGenreEmpty() {
        ContentVO current = new ContentVO();
        current.setGenreText("");
        current.setDirector("");
        current.setCastNames("");

        SearchResultVO candidate = new SearchResultVO();
        candidate.setGenreText("액션");

        String reason = ReflectionTestUtils.invokeMethod(
                service,
                "createRelatedRecommendationReason",
                current,
                candidate,
                Set.of(),
                "",
                Set.of(),
                Set.of());

        assertTrue(reason.contains("같은 콘텐츠 분류"));
    }

    private boolean matches(
            CachedContentVO content,
            String keyword,
            List<String> categories,
            List<String> genres,
            List<String> ages,
            Set<String> providers) {

        Boolean result = ReflectionTestUtils.invokeMethod(
                service,
                "matchesSearchFilters",
                content,
                keyword,
                categories,
                genres,
                ages,
                providers);

        return Boolean.TRUE.equals(result);
    }

    private CachedContentVO baseContent() {
        CachedContentVO content = new CachedContentVO();
        content.setTmdbId(100L);
        content.setContentType("MOVIE");
        content.setTitle("테스트");
        content.setGenreText("액션");
        content.setAgeRating("15세 이상 관람가");
        content.setSearchText("테스트");
        content.setPlatformKeys(List.of("netflix"));
        return content;
    }

    private SearchResultVO result(Long tmdbId, String type, String releaseDate) {
        SearchResultVO result = new SearchResultVO();
        result.setTmdbId(tmdbId);
        result.setContentType(type);
        result.setReleaseDate(releaseDate);
        return result;
    }
}
