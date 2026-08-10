package com.project.oditji.search.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
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
import com.project.oditji.tmdb.vo.OttPlatformVO;

/**
 * 페이지 캐시 서비스의 단순 helper와 검색 조건 단축평가 분기를 추가로 보완합니다.
 */
class SearchContentPageCacheServiceFinalCoverageTest {

    private SearchContentPageCacheService service;

    @BeforeEach
    void setUp() {
        service = new SearchContentPageCacheService(
                mock(SearchContentStore.class),
                mock(TmdbDAO.class));
    }

    @Test
    void releaseDateAndLimitHelpersShouldCoverNullBlankInvalidAndBoundaryValues() {
        assertNull(invoke(
                "parseReleaseDate",
                (Object) null));
        assertNull(invoke(
                "parseReleaseDate",
                " "));
        assertNull(invoke(
                "parseReleaseDate",
                "not-date"));

        assertEquals(
                LocalDate.of(2026, 8, 10),
                invoke(
                        "parseReleaseDate",
                        " 2026-08-10 "));

        assertTrue(
                invokeList(
                        "limitList",
                        null,
                        10)
                        .isEmpty());

        assertTrue(
                invokeList(
                        "limitList",
                        List.of(),
                        10)
                        .isEmpty());

        assertTrue(
                invokeList(
                        "limitList",
                        List.of(new SearchResultVO()),
                        0)
                        .isEmpty());

        SearchResultVO first = new SearchResultVO();
        SearchResultVO second = new SearchResultVO();

        List<SearchResultVO> source =
                List.of(first, second);

        List<SearchResultVO> copied =
                invokeList(
                        "limitList",
                        source,
                        2);

        assertEquals(2, copied.size());

        List<SearchResultVO> limited =
                invokeList(
                        "limitList",
                        source,
                        1);

        assertEquals(1, limited.size());
        assertSame(first, limited.get(0));
    }

    @Test
    void distinctHelperShouldIgnoreMalformedContentAndKeepFirstDuplicate() {
        Map<String, SearchResultVO> selected =
                new LinkedHashMap<String, SearchResultVO>();

        invokeVoid(
                "putDistinctContent",
                selected,
                null);

        SearchResultVO noId = new SearchResultVO();
        noId.setContentType("MOVIE");
        invokeVoid(
                "putDistinctContent",
                selected,
                noId);

        SearchResultVO noType = new SearchResultVO();
        noType.setTmdbId(1L);
        invokeVoid(
                "putDistinctContent",
                selected,
                noType);

        SearchResultVO first = result(2L, "MOVIE");
        SearchResultVO duplicate = result(2L, "MOVIE");

        invokeVoid(
                "putDistinctContent",
                selected,
                first);
        invokeVoid(
                "putDistinctContent",
                selected,
                duplicate);

        assertEquals(1, selected.size());
        assertSame(
                first,
                selected.get("MOVIE:2"));
    }

    @Test
    void contentListSortAndTypeNormalizationShouldCoverAllAllowedAndFallbackValues() {
        assertEquals(
                "popular",
                invoke(
                        "normalizeContentListSort",
                        null,
                        "all"));

        assertEquals(
                "latest",
                invoke(
                        "normalizeContentListSort",
                        " ",
                        "new"));

        assertEquals(
                "popular",
                invoke(
                        "normalizeContentListSort",
                        "POPULAR",
                        "all"));

        assertEquals(
                "rating",
                invoke(
                        "normalizeContentListSort",
                        "rating",
                        "all"));

        assertEquals(
                "latest",
                invoke(
                        "normalizeContentListSort",
                        "latest",
                        "all"));

        assertEquals(
                "title",
                invoke(
                        "normalizeContentListSort",
                        "title",
                        "all"));

        assertEquals(
                "latest",
                invoke(
                        "normalizeContentListSort",
                        "wrong",
                        "new"));

        assertEquals(
                "all",
                invoke(
                        "normalizeContentListType",
                        (Object) null));
        assertEquals(
                "popular",
                invoke(
                        "normalizeContentListType",
                        " Popular "));
        assertEquals(
                "new",
                invoke(
                        "normalizeContentListType",
                        "new"));
        assertEquals(
                "all",
                invoke(
                        "normalizeContentListType",
                        "movie"));
    }

    @Test
    void categoryMatchingShouldCoverMovieDramaAnimationVarietyDocumentaryAndFalsePaths() {
        CachedContentVO movie = content("MOVIE", "액션");
        assertTrue(matchCategory(movie, "MOVIE"));

        CachedContentVO animatedMovie =
                content("MOVIE", "애니메이션");
        assertFalse(matchCategory(animatedMovie, "MOVIE"));
        assertTrue(matchCategory(animatedMovie, "ANIMATION"));

        CachedContentVO documentaryMovie =
                content("MOVIE", "다큐멘터리");
        assertFalse(matchCategory(documentaryMovie, "MOVIE"));
        assertTrue(matchCategory(documentaryMovie, "DOCUMENTARY"));

        CachedContentVO drama =
                content("TV", "드라마");
        assertTrue(matchCategory(drama, "DRAMA"));

        CachedContentVO varietyDrama =
                content("TV", "드라마 토크");
        assertFalse(matchCategory(varietyDrama, "DRAMA"));
        assertTrue(matchCategory(varietyDrama, "VARIETY"));

        assertFalse(matchCategory(movie, "UNKNOWN"));
    }

    @Test
    void ageAndProviderHelpersShouldCoverEmptySelectedAndMatchMissBranches() {
        CachedContentVO content =
                content("MOVIE", "드라마");
        content.setAgeRating("15세");
        content.setPlatformKeys(
                List.of("netflix"));

        assertTrue(
                matchProviders(
                        content,
                        Set.of()));

        content.setPlatformKeys(List.of());

        assertFalse(
                matchProviders(
                        content,
                        Set.of()));

        content.setPlatformKeys(
                List.of("netflix", "tving"));

        assertTrue(
                matchProviders(
                        content,
                        Set.of("tving")));

        assertFalse(
                matchProviders(
                        content,
                        Set.of("wavve")));

        assertEquals(
                "15세 이상 관람가",
                invoke(
                        "normalizeAgeRating",
                        "15세"));

        assertEquals(
                "등급 정보 없음",
                invoke(
                        "normalizeAgeRating",
                        "NR"));

        assertEquals(
                "원본",
                invoke(
                        "normalizeAgeRating",
                        " 원본 "));
    }

    @Test
    void providerAndPlatformMapHelpersShouldCoverNullBlankLegacySupportedAndUnknownValues() {
        @SuppressWarnings("unchecked")
        Set<String> nullKeys =
                (Set<String>) invoke(
                        "providerIdsToKeys",
                        (Object) null);

        assertTrue(nullKeys.isEmpty());

        @SuppressWarnings("unchecked")
        Set<String> keys =
                (Set<String>) invoke(
                        "providerIdsToKeys",
                        Arrays.asList(
                                null,
                                " ",
                                "8",
                                "tving",
                                "unsupported"));

        assertEquals(
                Set.of("netflix", "tving"),
                keys);

        @SuppressWarnings("unchecked")
        Map<String, OttPlatformVO> emptyMap =
                (Map<String, OttPlatformVO>) invoke(
                        "createPlatformMap",
                        (Object) null);

        assertTrue(emptyMap.isEmpty());

        OttPlatformVO noName =
                new OttPlatformVO();

        OttPlatformVO netflix =
                new OttPlatformVO();
        netflix.setPlatformName("Netflix");

        @SuppressWarnings("unchecked")
        Map<String, OttPlatformVO> map =
                (Map<String, OttPlatformVO>) invoke(
                        "createPlatformMap",
                        Arrays.asList(
                                null,
                                noName,
                                netflix));

        assertEquals(1, map.size());
        assertSame(
                netflix,
                map.get("netflix"));
    }

    @Test
    void upperCaseNormalizerShouldSkipNullBlankAndDuplicates() {
        @SuppressWarnings("unchecked")
        List<String> empty =
                (List<String>) invoke(
                        "normalizeUpperCaseList",
                        (Object) null);

        assertTrue(empty.isEmpty());

        @SuppressWarnings("unchecked")
        List<String> normalized =
                (List<String>) invoke(
                        "normalizeUpperCaseList",
                        Arrays.asList(
                                null,
                                " ",
                                "movie",
                                " MOVIE ",
                                "tv"));

        assertEquals(
                List.of("MOVIE", "TV"),
                normalized);
    }

    private boolean matchCategory(
            CachedContentVO content,
            String category) {

        Boolean result = invoke(
                "matchesContentCategory",
                content,
                category);

        return Boolean.TRUE.equals(result);
    }

    private boolean matchProviders(
            CachedContentVO content,
            Set<String> selected) {

        Boolean result = invoke(
                "matchesProviders",
                content,
                selected);

        return Boolean.TRUE.equals(result);
    }

    private CachedContentVO content(
            String type,
            String genre) {

        CachedContentVO content =
                new CachedContentVO();
        content.setContentType(type);
        content.setGenreText(genre);
        content.setPlatformKeys(
                new ArrayList<String>());
        return content;
    }

    private SearchResultVO result(
            Long tmdbId,
            String type) {

        SearchResultVO result =
                new SearchResultVO();
        result.setTmdbId(tmdbId);
        result.setContentType(type);
        return result;
    }

    @SuppressWarnings("unchecked")
    private <T> T invoke(
            String methodName,
            Object... arguments) {

        return (T)
                ReflectionTestUtils.invokeMethod(
                        service,
                        methodName,
                        arguments);
    }

    @SuppressWarnings("unchecked")
    private List<SearchResultVO> invokeList(
            String methodName,
            Object... arguments) {

        return (List<SearchResultVO>)
                ReflectionTestUtils.invokeMethod(
                        service,
                        methodName,
                        arguments);
    }

    private void invokeVoid(
            String methodName,
            Object... arguments) {

        ReflectionTestUtils.invokeMethod(
                service,
                methodName,
                arguments);
    }
}
