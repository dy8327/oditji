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

/** 검색 캐시의 정규화·필터·플랫폼 helper 잔여 조건을 보완합니다. */
class SearchContentPageCacheServiceFinalNormalizationCoverageTest {

    private SearchContentPageCacheService service;

    @BeforeEach
    void setUp() {
        service = new SearchContentPageCacheService(
                mock(SearchContentStore.class),
                mock(TmdbDAO.class));
    }

    @Test
    void releaseDateAndLimitHelpersShouldCoverBlankInvalidAndBoundaryValues() {
        assertNull(invoke("parseReleaseDate", (Object) null));
        assertNull(invoke("parseReleaseDate", "   "));
        assertNull(invoke("parseReleaseDate", "2026-99-99"));
        assertEquals(LocalDate.of(2026, Month.AUGUST, 11),
                invoke("parseReleaseDate", " 2026-08-11 "));

        List<SearchResultVO> values = List.of(new SearchResultVO(), new SearchResultVO());
        List<?> nullLimited = invoke("limitList", null, 2);
        List<?> zeroLimited = invoke("limitList", values, 0);
        List<?> sameLimited = invoke("limitList", values, 2);
        List<?> shortLimited = invoke("limitList", values, 1);

        assertTrue(nullLimited.isEmpty());
        assertTrue(zeroLimited.isEmpty());
        assertEquals(2, sameLimited.size());
        assertEquals(1, shortLimited.size());
    }

    @Test
    void ageRatingNormalizationShouldCoverEveryCanonicalBucketAndDeduplication() {
        assertEquals("등급 정보 없음", invoke("normalizeAgeRating", (Object) null));
        assertEquals("등급 정보 없음", invoke("normalizeAgeRating", " NR "));
        assertEquals("등급 정보 없음", invoke("normalizeAgeRating", "unrated"));
        assertEquals("청소년 관람불가", invoke("normalizeAgeRating", "19세 이상"));
        assertEquals("15세 이상 관람가", invoke("normalizeAgeRating", "15세"));
        assertEquals("12세 이상 관람가", invoke("normalizeAgeRating", "12세"));
        assertEquals("7세 이상 관람가", invoke("normalizeAgeRating", "7세"));
        assertEquals("전체 관람가", invoke("normalizeAgeRating", "전체"));
        assertEquals("PG", invoke("normalizeAgeRating", " PG "));

        List<String> source = Arrays.asList(null, " ", "15세", "15세 이상 관람가", "NR");
        List<?> normalized = invoke("normalizeAgeRatingList", source);
        assertEquals(2, normalized.size());
        assertTrue(normalized.contains("15세 이상 관람가"));
        assertTrue(normalized.contains("등급 정보 없음"));
    }

    @Test
    void providerHelpersShouldCoverLegacySupportedUnsupportedNullAndUnknownPlatforms() {
        assertEquals("", invoke("resolveProviderKey", (Object) null));
        assertEquals("", invoke("resolveProviderKey", " "));
        assertEquals("netflix", invoke("resolveProviderKey", "8"));
        assertEquals("coupang", invoke("resolveProviderKey", "283"));
        assertEquals("netflix", invoke("resolveProviderKey", "Netflix"));
        assertEquals("", invoke("resolveProviderKey", "unsupported-provider"));

        Set<?> empty = invoke("providerIdsToKeys", (Object) null);
        Set<?> keys = invoke("providerIdsToKeys", Arrays.asList(null, " ", "8", "Netflix", "999999"));
        assertTrue(empty.isEmpty());
        assertEquals(Set.of("netflix"), keys);
    }

    @Test
    void categoryAndGenreHelpersShouldCoverMovieDramaAnimationVarietyDocumentaryAndAliases() {
        CachedContentVO content = new CachedContentVO();
        content.setContentType("MOVIE");
        content.setGenreText("액션·모험, SF·판타지, 연속극");

        assertTrue(booleanValue(invoke("matchesContentCategory", content, "MOVIE")));
        assertTrue(booleanValue(invoke("matchesGenreCodes", content, List.of("ACTION"))));
        assertTrue(booleanValue(invoke("matchesGenreCodes", content, List.of("SCI_FI"))));
        assertTrue(booleanValue(invoke("matchesGenreCodes", content, List.of("FANTASY"))));
        assertTrue(booleanValue(invoke("matchesGenreCodes", content, List.of("ROMANCE"))));
        assertFalse(booleanValue(invoke("matchesGenreCodes", content, List.of("HORROR"))));

        content.setContentType("TV");
        content.setGenreText("드라마");
        assertTrue(booleanValue(invoke("matchesContentCategory", content, "DRAMA")));
        content.setGenreText("애니메이션");
        assertTrue(booleanValue(invoke("matchesContentCategory", content, "ANIMATION")));
        content.setGenreText("리얼리티");
        assertTrue(booleanValue(invoke("matchesContentCategory", content, "VARIETY")));
        content.setGenreText("다큐멘터리");
        assertTrue(booleanValue(invoke("matchesContentCategory", content, "DOCUMENTARY")));
    }

    @Test
    void platformMapAndMatchInformationShouldIgnoreInvalidPlatformAndResolvePersonMatches() {
        OttPlatformVO netflix = new OttPlatformVO();
        netflix.setPlatformName("Netflix");
        OttPlatformVO unnamed = new OttPlatformVO();

        Map<?, ?> emptyMap = invoke("createPlatformMap", (Object) null);
        Map<?, ?> map = invoke("createPlatformMap", Arrays.asList(null, unnamed, netflix));
        assertTrue(emptyMap.isEmpty());
        assertEquals(1, map.size());

        List<?> noPlatforms = invoke("createPlatformList", null, map);
        List<?> platforms = invoke("createPlatformList", List.of("missing", "netflix"), map);
        assertTrue(noPlatforms.isEmpty());
        assertEquals(1, platforms.size());

        CachedContentVO content = new CachedContentVO();
        content.setTitle("작품");
        content.setOriginalTitle("Original");
        content.setDirector("홍길동");
        content.setCastNames("배우A, 배우B");

        SearchResultVO result = new SearchResultVO();
        invoke("applyMatchInformation", result, content, "홍길동");
        assertEquals("PERSON", result.getMatchType());
        assertEquals("감독", result.getMatchedPersonRole());

        SearchResultVO castResult = new SearchResultVO();
        invoke("applyMatchInformation", castResult, content, "배우a");
        assertEquals("PERSON", castResult.getMatchType());
        assertEquals("배우", castResult.getMatchedPersonRole());
    }


    private boolean booleanValue(Object value) {
        return Boolean.TRUE.equals(value);
    }

    @SuppressWarnings("unchecked")
    private <T> T invoke(String method, Object... args) {
        return (T) ReflectionTestUtils.invokeMethod(service, method, args);
    }
}
