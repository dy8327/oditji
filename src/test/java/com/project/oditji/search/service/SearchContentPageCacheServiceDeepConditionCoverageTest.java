package com.project.oditji.search.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import java.time.LocalDate;
import java.time.Month;
import java.util.Arrays;
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

/** 페이지 캐시 서비스의 검색 필터 short-circuit와 정규화 잔여 조건을 보완합니다. */
class SearchContentPageCacheServiceDeepConditionCoverageTest {

    private SearchContentPageCacheService service;

    @BeforeEach
    void setUp() {
        service = new SearchContentPageCacheService(
                mock(SearchContentStore.class),
                mock(TmdbDAO.class));
    }

    @Test
    void searchFilterShouldStopAtEachFailedConditionAndEventuallyMatch() {
        List<String> empty = List.of();
        Set<String> noPlatforms = Set.of();

        assertFalse(matches(null, "", empty, empty, empty, noPlatforms));

        CachedContentVO noId = baseContent();
        noId.setTmdbId(null);
        assertFalse(matches(noId, "", empty, empty, empty, noPlatforms));

        CachedContentVO noType = baseContent();
        noType.setContentType(null);
        assertFalse(matches(noType, "", empty, empty, empty, noPlatforms));

        CachedContentVO keywordFail = baseContent();
        keywordFail.setSearchText("다른검색어");
        assertFalse(matches(keywordFail, "없는키워드", empty, empty, empty, noPlatforms));

        CachedContentVO categoryFail = baseContent();
        assertFalse(matches(categoryFail, "", List.of("ANIMATION"), empty, empty, noPlatforms));

        CachedContentVO genreFail = baseContent();
        assertFalse(matches(genreFail, "", empty, List.of("HORROR"), empty, noPlatforms));

        CachedContentVO ageFail = baseContent();
        assertFalse(matches(ageFail, "", empty, empty, List.of("청소년 관람불가"), noPlatforms));

        CachedContentVO platformFail = baseContent();
        assertFalse(matches(platformFail, "", empty, empty, empty, Set.of("tving")));

        assertTrue(matches(baseContent(), "", empty, empty, empty, Set.of("netflix")));
    }

    @Test
    void keywordMatchingShouldCoverEmptyStoredSearchTextAndFallbackFields() {
        CachedContentVO content = baseContent();
        assertTrue((Boolean) invoke("matchesKeyword", content, ""));

        content.setSearchText(null);
        content.setTitle("닥터 하얀 마피아");
        content.setOriginalTitle(null);
        content.setDirector("감독A");
        content.setCastNames("배우A");
        assertTrue((Boolean) invoke("matchesKeyword", content, "닥터하얀마피아"));
        assertTrue((Boolean) invoke("matchesKeyword", content, "감독a"));
        assertFalse((Boolean) invoke("matchesKeyword", content, "없는사람"));

        content.setSearchText("   ");
        assertTrue((Boolean) invoke("matchesKeyword", content, "배우a"));

        content.setSearchText("cachedtext");
        assertTrue((Boolean) invoke("matchesKeyword", content, "cached"));
    }

    @Test
    void contentCategoryMatchingShouldCoverMovieDramaAnimationVarietyAndDocumentaryBranches() {
        CachedContentVO content = baseContent();

        content.setContentType("TV");
        content.setGenreText("드라마");
        assertFalse((Boolean) invoke("matchesContentCategory", content, "MOVIE"));

        content.setContentType("MOVIE");
        content.setGenreText("애니메이션");
        assertFalse((Boolean) invoke("matchesContentCategory", content, "MOVIE"));

        content.setGenreText("다큐멘터리");
        assertFalse((Boolean) invoke("matchesContentCategory", content, "MOVIE"));

        content.setGenreText("액션");
        assertTrue((Boolean) invoke("matchesContentCategory", content, "MOVIE"));

        content.setContentType("MOVIE");
        content.setGenreText("드라마");
        assertFalse((Boolean) invoke("matchesContentCategory", content, "DRAMA"));

        content.setContentType("TV");
        content.setGenreText("코미디");
        assertFalse((Boolean) invoke("matchesContentCategory", content, "DRAMA"));

        content.setGenreText("드라마 애니메이션");
        assertFalse((Boolean) invoke("matchesContentCategory", content, "DRAMA"));

        content.setGenreText("드라마 다큐멘터리");
        assertFalse((Boolean) invoke("matchesContentCategory", content, "DRAMA"));

        content.setGenreText("드라마 리얼리티");
        assertFalse((Boolean) invoke("matchesContentCategory", content, "DRAMA"));

        content.setGenreText("드라마");
        assertTrue((Boolean) invoke("matchesContentCategory", content, "DRAMA"));

        content.setGenreText("애니메이션");
        assertTrue((Boolean) invoke("matchesContentCategory", content, "ANIMATION"));
        content.setGenreText("코미디");
        assertFalse((Boolean) invoke("matchesContentCategory", content, "ANIMATION"));

        content.setContentType("MOVIE");
        content.setGenreText("리얼리티");
        assertFalse((Boolean) invoke("matchesContentCategory", content, "VARIETY"));
        content.setContentType("TV");
        assertTrue((Boolean) invoke("matchesContentCategory", content, "VARIETY"));
        content.setGenreText("토크");
        assertTrue((Boolean) invoke("matchesContentCategory", content, "VARIETY"));

        content.setGenreText("다큐멘터리");
        assertTrue((Boolean) invoke("matchesContentCategory", content, "DOCUMENTARY"));
        content.setGenreText("드라마");
        assertFalse((Boolean) invoke("matchesContentCategory", content, "DOCUMENTARY"));
    }

    @Test
    void categoryAndGenreListMatchersShouldCoverEmptyMatchAndNoMatchPaths() {
        CachedContentVO content = baseContent();
        content.setGenreText("액션·모험 SF·판타지 연속극");

        assertTrue((Boolean) invoke("matchesContentCategories", content, List.of()));
        assertTrue((Boolean) invoke("matchesContentCategories", content, List.of("MOVIE", "ANIMATION")));
        assertFalse((Boolean) invoke("matchesContentCategories", content, List.of("DOCUMENTARY")));

        assertTrue((Boolean) invoke("matchesGenreCodes", content, List.of()));
        assertTrue((Boolean) invoke("matchesGenreCodes", content, List.of("ACTION")));
        assertTrue((Boolean) invoke("matchesGenreCodes", content, List.of("SCI_FI")));
        assertTrue((Boolean) invoke("matchesGenreCodes", content, List.of("FANTASY")));
        assertTrue((Boolean) invoke("matchesGenreCodes", content, List.of("ROMANCE")));
        assertFalse((Boolean) invoke("matchesGenreCodes", content, List.of("HORROR")));
    }

    @Test
    void ageRatingNormalizationShouldCoverNullAliasesDuplicatesAndUnknownCustomValues() {
        assertEquals("등급 정보 없음", invoke("normalizeAgeRating", new Object[] { null }));
        assertEquals("등급 정보 없음", invoke("normalizeAgeRating", "   "));
        assertEquals("등급 정보 없음", invoke("normalizeAgeRating", "Not Rated"));
        assertEquals("등급 정보 없음", invoke("normalizeAgeRating", "unrated"));
        assertEquals("등급 정보 없음", invoke("normalizeAgeRating", "NR"));
        assertEquals("청소년 관람불가", invoke("normalizeAgeRating", "19세 이상"));
        assertEquals("청소년 관람불가", invoke("normalizeAgeRating", "18세"));
        assertEquals("15세 이상 관람가", invoke("normalizeAgeRating", "15세"));
        assertEquals("12세 이상 관람가", invoke("normalizeAgeRating", "12세"));
        assertEquals("7세 이상 관람가", invoke("normalizeAgeRating", "7세"));
        assertEquals("전체 관람가", invoke("normalizeAgeRating", "전체관람가"));
        assertEquals("CUSTOM", invoke("normalizeAgeRating", " CUSTOM "));

        List<String> normalized = castStringList(invoke(
                "normalizeAgeRatingList",
                Arrays.asList(null, " ", "15세", "15세 이상 관람가", "NR", "전체")));
        assertEquals(List.of("15세 이상 관람가", "등급 정보 없음", "전체 관람가"), normalized);

        assertTrue((Boolean) invoke("matchesAgeRatings", baseContent(), List.of()));
        assertTrue((Boolean) invoke("matchesAgeRatings", null, List.of("등급 정보 없음")));
        CachedContentVO content = baseContent();
        content.setAgeRating(null);
        assertTrue((Boolean) invoke("matchesAgeRatings", content, List.of("등급 정보 없음")));
        assertFalse((Boolean) invoke("matchesAgeRatings", content, List.of("15세 이상 관람가")));
    }

    @Test
    void providerNormalizationAndMatchingShouldCoverLegacySupportedUnsupportedAndNullInputs() {
        assertTrue(castStringSet(invoke("providerIdsToKeys", new Object[] { null })).isEmpty());
        Set<String> keys = castStringSet(invoke(
                "providerIdsToKeys",
                Arrays.asList(null, " ", "8", " Netflix ", "283", "unsupported")));
        assertTrue(keys.contains("netflix"));
        assertTrue(keys.contains("coupang"));
        assertFalse(keys.contains("unsupported"));

        assertEquals("", invoke("resolveProviderKey", new Object[] { null }));
        assertEquals("", invoke("resolveProviderKey", "   "));
        assertEquals("netflix", invoke("resolveProviderKey", "8"));
        assertEquals("tving", invoke("resolveProviderKey", " TVING "));
        assertEquals("", invoke("resolveProviderKey", "unknown"));

        CachedContentVO content = baseContent();
        assertTrue((Boolean) invoke("matchesProviders", content, Set.of()));
        assertTrue((Boolean) invoke("matchesProviders", content, Set.of("netflix")));
        assertFalse((Boolean) invoke("matchesProviders", content, Set.of("tving")));
        content.setPlatformKeys(List.of());
        assertFalse((Boolean) invoke("matchesProviders", content, Set.of()));
    }

    @Test
    void platformMapListAndUpperCaseNormalizationShouldCoverNullInvalidAndDuplicateValues() {
        assertTrue(castPlatformMap(invoke("createPlatformMap", new Object[] { null })).isEmpty());

        OttPlatformVO nullName = new OttPlatformVO();
        nullName.setPlatformName(null);
        OttPlatformVO netflix = new OttPlatformVO();
        netflix.setPlatformName("Netflix");
        OttPlatformVO unknown = new OttPlatformVO();
        unknown.setPlatformName("Unknown Platform");

        Map<String, OttPlatformVO> platformMap = castPlatformMap(
                invoke("createPlatformMap", Arrays.asList(null, nullName, netflix, unknown)));
        assertEquals(netflix, platformMap.get("netflix"));

        assertTrue(castPlatformList(invoke("createPlatformList", null, platformMap)).isEmpty());
        List<OttPlatformVO> platformList = castPlatformList(
                invoke("createPlatformList", Arrays.asList("netflix", "missing"), platformMap));
        assertEquals(1, platformList.size());

        assertTrue(castStringList(invoke("normalizeUpperCaseList", new Object[] { null })).isEmpty());
        assertEquals(
                List.of("MOVIE", "TV"),
                castStringList(invoke("normalizeUpperCaseList", Arrays.asList(null, " ", " movie ", "MOVIE", "tv"))));
    }

    @Test
    void releaseDateLimitAndRelatedValueHelpersShouldCoverBoundaryBranches() {
        assertNull(invoke("parseReleaseDate", new Object[] { null }));
        assertNull(invoke("parseReleaseDate", "   "));
        assertNull(invoke("parseReleaseDate", "invalid"));
        assertEquals(LocalDate.of(2026, Month.AUGUST, 11), invoke("parseReleaseDate", " 2026-08-11 "));

        assertTrue(castSearchList(invoke("limitList", null, 2)).isEmpty());
        assertTrue(castSearchList(invoke("limitList", List.of(), 2)).isEmpty());
        assertTrue(castSearchList(invoke("limitList", List.of(new SearchResultVO()), 0)).isEmpty());

        List<SearchResultVO> one = List.of(new SearchResultVO());
        assertEquals(1, castSearchList(invoke("limitList", one, 2)).size());
        List<SearchResultVO> three = List.of(new SearchResultVO(), new SearchResultVO(), new SearchResultVO());
        assertEquals(2, castSearchList(invoke("limitList", three, 2)).size());

        assertEquals("", invoke("normalizeRelatedValue", new Object[] { null }));
        assertEquals("tv", invoke("normalizeRelatedValue", " TV "));
        assertEquals("100_tv", invoke("buildTmdbKey", 100L, " TV "));
        assertEquals("", invoke("normalizeSearchText", new Object[] { null }));
        assertEquals("abc123", invoke("normalizeSearchText", " A-B C!123 "));
        assertEquals("", invoke("safeText", new Object[] { null }));
        assertEquals("text", invoke("safeText", "text"));
        assertEquals("알수없음", invoke("displayGenreName", "알수없음"));
    }

    private boolean matches(
            CachedContentVO content,
            String keyword,
            List<String> categories,
            List<String> genres,
            List<String> ages,
            Set<String> platforms) {

        return (Boolean) invoke(
                "matchesSearchFilters",
                content,
                keyword,
                categories,
                genres,
                ages,
                platforms);
    }

    private CachedContentVO baseContent() {
        CachedContentVO content = new CachedContentVO();
        content.setTmdbId(100L);
        content.setContentType("MOVIE");
        content.setTitle("영화 제목");
        content.setOriginalTitle("Original");
        content.setDirector("감독");
        content.setCastNames("배우");
        content.setSearchText("영화제목original감독배우");
        content.setGenreText("액션");
        content.setAgeRating("15세 이상 관람가");
        content.setPlatformKeys(List.of("netflix"));
        return content;
    }

    private Object invoke(String methodName, Object... arguments) {
        return ReflectionTestUtils.invokeMethod(service, methodName, arguments);
    }

    @SuppressWarnings("unchecked")
    private List<String> castStringList(Object value) {
        return (List<String>) value;
    }

    @SuppressWarnings("unchecked")
    private Set<String> castStringSet(Object value) {
        return (Set<String>) value;
    }

    @SuppressWarnings("unchecked")
    private Map<String, OttPlatformVO> castPlatformMap(Object value) {
        return (Map<String, OttPlatformVO>) value;
    }

    @SuppressWarnings("unchecked")
    private List<OttPlatformVO> castPlatformList(Object value) {
        return (List<OttPlatformVO>) value;
    }

    @SuppressWarnings("unchecked")
    private List<SearchResultVO> castSearchList(Object value) {
        return (List<SearchResultVO>) value;
    }
}
