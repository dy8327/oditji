package com.project.oditji.search.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

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
 * 검색 페이지 캐시의 keyword/category/genre/provider/match 정보 단축평가를 집중 보완합니다.
 */
class SearchContentPageCacheServiceMoreConditionCoverageTest {

    private SearchContentPageCacheService service;

    @BeforeEach
    void setUp() {
        service = new SearchContentPageCacheService(
                mock(SearchContentStore.class),
                mock(TmdbDAO.class));
    }

    @Test
    void completeSearchFilterPredicateShouldRejectMissingCoreFieldsAndAcceptValidContent() {
        List<String> empty = List.of();
        Set<String> selected = Set.of();

        assertFalse(matchesFilters(
                null,
                "",
                empty,
                empty,
                empty,
                selected));

        CachedContentVO noId = baseContent();
        noId.setTmdbId(null);

        assertFalse(matchesFilters(
                noId,
                "",
                empty,
                empty,
                empty,
                selected));

        CachedContentVO noType = baseContent();
        noType.setContentType(null);

        assertFalse(matchesFilters(
                noType,
                "",
                empty,
                empty,
                empty,
                selected));

        assertTrue(matchesFilters(
                baseContent(),
                "",
                empty,
                empty,
                empty,
                selected));
    }

    @Test
    void keywordMatchingShouldCoverEmptyStoredTextFallbackMatchAndMiss() {
        CachedContentVO content = baseContent();

        assertTrue(matchesKeyword(content, ""));

        content.setSearchText(null);
        content.setTitle("Hello World");
        content.setOriginalTitle(null);
        content.setDirector(null);
        content.setCastNames(null);

        assertTrue(matchesKeyword(content, "helloworld"));
        assertFalse(matchesKeyword(content, "missing"));

        content.setSearchText(" ");
        content.setDirector("Kim Director");

        assertTrue(matchesKeyword(content, "kimdirector"));
    }

    @Test
    void categoryAndGenreMatchingShouldCoverEmptyMultiChoiceAndSpecialGenreAliases() {
        CachedContentVO movie = baseContent();
        movie.setContentType("MOVIE");
        movie.setGenreText("액션·모험");

        assertTrue(matchesCategories(movie, List.of()));
        assertTrue(matchesCategories(
                movie,
                List.of("DRAMA", "MOVIE")));
        assertFalse(matchesCategories(
                movie,
                List.of("DRAMA")));

        assertTrue(matchesGenres(
                movie,
                List.of("ACTION")));
        assertFalse(matchesGenres(
                movie,
                List.of("HORROR")));

        movie.setGenreText("SF·판타지");
        assertTrue(matchesGenres(
                movie,
                List.of("SCI_FI")));
        assertTrue(matchesGenres(
                movie,
                List.of("FANTASY")));

        movie.setGenreText("연속극");
        assertTrue(matchesGenres(
                movie,
                List.of("ROMANCE")));
    }

    @Test
    void ageRatingAndProviderPredicatesShouldCoverNullContentEmptySelectionsAndMatches() {
        assertTrue(matchesAge(
                null,
                List.of("등급 정보 없음")));

        CachedContentVO content = baseContent();
        content.setAgeRating("15세");

        assertTrue(matchesAge(
                content,
                List.of()));
        assertTrue(matchesAge(
                content,
                List.of("15세 이상 관람가")));
        assertFalse(matchesAge(
                content,
                List.of("12세 이상 관람가")));

        content.setPlatformKeys(List.of());

        assertFalse(matchesProviders(
                content,
                Set.of()));

        content.setPlatformKeys(
                List.of("netflix", "tving"));

        assertTrue(matchesProviders(
                content,
                Set.of()));
        assertTrue(matchesProviders(
                content,
                Set.of("tving")));
        assertFalse(matchesProviders(
                content,
                Set.of("wavve")));
    }

    @Test
    void matchInformationShouldCoverEmptyTitleDirectorCastAndNoPersonMatches() {
        CachedContentVO content = baseContent();
        SearchResultVO result = new SearchResultVO();

        applyMatch(result, content, "");
        assertEquals("TITLE", result.getMatchType());

        content.setTitle("찾는 제목");
        applyMatch(result, content, "찾는제목");
        assertEquals("TITLE", result.getMatchType());

        content.setTitle("다른 제목");
        content.setDirector("찾는 감독");
        applyMatch(result, content, "찾는감독");

        assertEquals("PERSON", result.getMatchType());
        assertEquals("감독", result.getMatchedPersonRole());

        content.setDirector(null);
        content.setCastNames("찾는 배우");
        applyMatch(result, content, "찾는배우");

        assertEquals("PERSON", result.getMatchType());
        assertEquals("배우", result.getMatchedPersonRole());

        SearchResultVO noMatch = new SearchResultVO();
        content.setCastNames(null);
        applyMatch(noMatch, content, "없음");

        assertEquals("TITLE", noMatch.getMatchType());
        assertNull(noMatch.getMatchedPersonRole());
    }

    @Test
    void providerKeyAndPlatformListHelpersShouldCoverNullLegacySupportedAndUnknownValues() {
        assertEquals("", resolveProvider(null));
        assertEquals("", resolveProvider(" "));
        assertEquals("netflix", resolveProvider("8"));
        assertEquals("coupang", resolveProvider("283"));
        assertEquals("tving", resolveProvider("TVING"));
        assertEquals("", resolveProvider("unsupported"));

        @SuppressWarnings("unchecked")
        List<OttPlatformVO> empty =
                (List<OttPlatformVO>)
                        ReflectionTestUtils.invokeMethod(
                                service,
                                "createPlatformList",
                                null,
                                Map.of());

        assertTrue(empty.isEmpty());

        OttPlatformVO netflix = new OttPlatformVO();
        netflix.setPlatformName("Netflix");

        Map<String, OttPlatformVO> map =
                new LinkedHashMap<String, OttPlatformVO>();
        map.put("netflix", netflix);

        @SuppressWarnings("unchecked")
        List<OttPlatformVO> values =
                (List<OttPlatformVO>)
                        ReflectionTestUtils.invokeMethod(
                                service,
                                "createPlatformList",
                                Arrays.asList(
                                        "missing",
                                        "netflix"),
                                map);

        assertEquals(1, values.size());
        assertSame(netflix, values.get(0));
    }

    @Test
    void ageListAndUtilityNormalizersShouldRemoveBlankDuplicatesAndSupportUnknownGenreCode() {
        @SuppressWarnings("unchecked")
        List<String> ages =
                (List<String>)
                        ReflectionTestUtils.invokeMethod(
                                service,
                                "normalizeAgeRatingList",
                                Arrays.asList(
                                        null,
                                        " ",
                                        "15세",
                                        "15세 이상 관람가",
                                        "NR"));

        assertEquals(
                List.of(
                        "15세 이상 관람가",
                        "등급 정보 없음"),
                ages);

        assertEquals(
                "",
                ReflectionTestUtils.invokeMethod(
                        service,
                        "safeText",
                        (Object) null));

        assertEquals(
                "CUSTOM",
                ReflectionTestUtils.invokeMethod(
                        service,
                        "displayGenreName",
                        "CUSTOM"));
    }

    private CachedContentVO baseContent() {
        CachedContentVO content = new CachedContentVO();
        content.setTmdbId(1L);
        content.setContentType("MOVIE");
        content.setTitle("제목");
        content.setGenreText("드라마");
        content.setAgeRating("15세 이상 관람가");
        content.setPlatformKeys(
                new ArrayList<String>(
                        List.of("netflix")));
        content.setSearchText("제목");
        return content;
    }

    private boolean matchesFilters(
            CachedContentVO content,
            String keyword,
            List<String> categories,
            List<String> genres,
            List<String> ages,
            Set<String> platforms) {

        Boolean result = ReflectionTestUtils.invokeMethod(
                service,
                "matchesSearchFilters",
                content,
                keyword,
                categories,
                genres,
                ages,
                platforms);

        return Boolean.TRUE.equals(result);
    }

    private boolean matchesKeyword(
            CachedContentVO content,
            String keyword) {

        Boolean result = ReflectionTestUtils.invokeMethod(
                service,
                "matchesKeyword",
                content,
                keyword);

        return Boolean.TRUE.equals(result);
    }

    private boolean matchesCategories(
            CachedContentVO content,
            List<String> categories) {

        Boolean result = ReflectionTestUtils.invokeMethod(
                service,
                "matchesContentCategories",
                content,
                categories);

        return Boolean.TRUE.equals(result);
    }

    private boolean matchesGenres(
            CachedContentVO content,
            List<String> genres) {

        Boolean result = ReflectionTestUtils.invokeMethod(
                service,
                "matchesGenreCodes",
                content,
                genres);

        return Boolean.TRUE.equals(result);
    }

    private boolean matchesAge(
            CachedContentVO content,
            List<String> ages) {

        Boolean result = ReflectionTestUtils.invokeMethod(
                service,
                "matchesAgeRatings",
                content,
                ages);

        return Boolean.TRUE.equals(result);
    }

    private boolean matchesProviders(
            CachedContentVO content,
            Set<String> platforms) {

        Boolean result = ReflectionTestUtils.invokeMethod(
                service,
                "matchesProviders",
                content,
                platforms);

        return Boolean.TRUE.equals(result);
    }

    private void applyMatch(
            SearchResultVO result,
            CachedContentVO content,
            String keyword) {

        ReflectionTestUtils.invokeMethod(
                service,
                "applyMatchInformation",
                result,
                content,
                keyword);
    }

    private String resolveProvider(String value) {
        return ReflectionTestUtils.invokeMethod(
                service,
                "resolveProviderKey",
                value);
    }
}
