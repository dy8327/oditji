package com.project.oditji.search.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.project.oditji.search.vo.CachedContentVO;
import com.project.oditji.search.vo.SearchResultVO;
import com.project.oditji.tmdb.dao.TmdbDAO;

/** 검색 캐시의 목록 guard, 카테고리, 등급, OTT 필터 잔여 조건을 보완합니다. */
class SearchContentPageCacheServiceResidualClosure5Test {

    private SearchContentPageCacheService service;

    @BeforeEach
    void setUp() {
        service = new SearchContentPageCacheService(
                mock(SearchContentStore.class),
                mock(TmdbDAO.class));
    }

    @Test
    void listAndReleaseHelpersShouldCoverEveryGuardOperandAndDistinctValidation() {
        invokeVoid("applyUpcomingReleasePolicy", (Object) null);
        invokeVoid("applyUpcomingReleasePolicy", new ArrayList<SearchResultVO>());

        assertNull(invoke("parseReleaseDate", (Object) null));
        assertNull(invoke("parseReleaseDate", "   "));
        assertNull(invoke("parseReleaseDate", "not-a-date"));
        assertEquals(
                LocalDate.of(2026, Month.AUGUST, 13),
                invoke("parseReleaseDate", " 2026-08-13 "));

        Map<String, SearchResultVO> selected = new LinkedHashMap<>();
        invokeVoid("putDistinctContent", selected, null);

        SearchResultVO noId = result(null, "MOVIE");
        invokeVoid("putDistinctContent", selected, noId);

        SearchResultVO noType = result(1L, null);
        invokeVoid("putDistinctContent", selected, noType);

        SearchResultVO valid = result(2L, "MOVIE");
        invokeVoid("putDistinctContent", selected, valid);
        invokeVoid("putDistinctContent", selected, result(2L, "MOVIE"));

        assertEquals(1, selected.size());
        assertEquals(valid, selected.get("MOVIE:2"));
    }

    @Test
    void limitListShouldCoverNullEmptyNonPositiveFullAndTrimmedResults() {
        List<SearchResultVO> first = List.of(result(1L, "MOVIE"));
        List<SearchResultVO> three = List.of(
                result(1L, "MOVIE"),
                result(2L, "MOVIE"),
                result(3L, "TV"));

        assertTrue(this.<SearchResultVO>invokeList("limitList", null, 3).isEmpty());
        assertTrue(this.<SearchResultVO>invokeList("limitList", List.of(), 3).isEmpty());
        assertTrue(this.<SearchResultVO>invokeList("limitList", first, 0).isEmpty());
        assertEquals(1, this.<SearchResultVO>invokeList("limitList", first, 3).size());
        assertEquals(2, this.<SearchResultVO>invokeList("limitList", three, 2).size());
    }

    @Test
    void contentCategoryShouldCoverEveryShortCircuitPosition() {
        assertTrue(matchesCategory(content("MOVIE", "액션"), "MOVIE"));
        assertFalse(matchesCategory(content("TV", "액션"), "MOVIE"));
        assertFalse(matchesCategory(content("MOVIE", "애니메이션"), "MOVIE"));
        assertFalse(matchesCategory(content("MOVIE", "다큐멘터리"), "MOVIE"));

        assertTrue(matchesCategory(content("TV", "드라마"), "DRAMA"));
        assertFalse(matchesCategory(content("MOVIE", "드라마"), "DRAMA"));
        assertFalse(matchesCategory(content("TV", "액션"), "DRAMA"));
        assertFalse(matchesCategory(content("TV", "드라마 애니메이션"), "DRAMA"));
        assertFalse(matchesCategory(content("TV", "드라마 다큐멘터리"), "DRAMA"));
        assertFalse(matchesCategory(content("TV", "드라마 토크"), "DRAMA"));

        assertTrue(matchesCategory(content("TV", "애니메이션"), "ANIMATION"));
        assertFalse(matchesCategory(content("TV", "드라마"), "ANIMATION"));
        assertTrue(matchesCategory(content("TV", "토크"), "VARIETY"));
        assertFalse(matchesCategory(content("MOVIE", "토크"), "VARIETY"));
        assertFalse(matchesCategory(content("TV", "드라마"), "VARIETY"));
        assertTrue(matchesCategory(content("MOVIE", "다큐멘터리"), "DOCUMENTARY"));
        assertFalse(matchesCategory(content("MOVIE", "액션"), "DOCUMENTARY"));
    }

    @Test
    void ageAndProviderFiltersShouldCoverEmptyNullMatchAndMissBranches() {
        CachedContentVO unknownAge = content("MOVIE", "드라마");
        unknownAge.setAgeRating(null);
        assertTrue(matchesAge(unknownAge, List.of()));
        assertTrue(matchesAge(unknownAge, List.of("등급 정보 없음")));
        assertFalse(matchesAge(unknownAge, List.of("15세 이상 관람가")));
        assertTrue(matchesAge(null, List.of("등급 정보 없음")));

        CachedContentVO noPlatform = content("MOVIE", "드라마");
        assertFalse(matchesProviders(noPlatform, Set.of()));

        CachedContentVO netflix = content("MOVIE", "드라마");
        netflix.setPlatformKeys(List.of("netflix"));
        assertTrue(matchesProviders(netflix, Set.of()));
        assertTrue(matchesProviders(netflix, Set.of("netflix")));
        assertFalse(matchesProviders(netflix, Set.of("tving")));
    }

    private CachedContentVO content(String type, String genres) {
        CachedContentVO content = new CachedContentVO();
        content.setContentType(type);
        content.setGenreText(genres);
        return content;
    }

    private SearchResultVO result(Long tmdbId, String type) {
        SearchResultVO result = new SearchResultVO();
        result.setTmdbId(tmdbId);
        result.setContentType(type);
        return result;
    }

    private boolean matchesCategory(CachedContentVO content, String category) {
        return Boolean.TRUE.equals(invoke("matchesContentCategory", content, category));
    }

    private boolean matchesAge(CachedContentVO content, List<String> ageRatings) {
        return Boolean.TRUE.equals(invoke("matchesAgeRatings", content, ageRatings));
    }

    private boolean matchesProviders(CachedContentVO content, Set<String> providers) {
        return Boolean.TRUE.equals(invoke("matchesProviders", content, providers));
    }

    private void invokeVoid(String methodName, Object... arguments) {
        ReflectionTestUtils.invokeMethod(service, methodName, arguments);
    }

    @SuppressWarnings("unchecked")
    private <T> T invoke(String methodName, Object... arguments) {
        return (T) ReflectionTestUtils.invokeMethod(service, methodName, arguments);
    }

    @SuppressWarnings("unchecked")
    private <T> List<T> invokeList(String methodName, Object... arguments) {
        return (List<T>) ReflectionTestUtils.invokeMethod(service, methodName, arguments);
    }
}
