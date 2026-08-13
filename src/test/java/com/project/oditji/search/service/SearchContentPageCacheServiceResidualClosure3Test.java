package com.project.oditji.search.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

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

/** 검색 페이지 캐시의 텍스트/플랫폼/매칭 helper 잔여 단축평가를 보완합니다. */
class SearchContentPageCacheServiceResidualClosure3Test {

    private SearchContentPageCacheService service;

    @BeforeEach
    void setUp() {
        service = new SearchContentPageCacheService(
                mock(SearchContentStore.class),
                mock(TmdbDAO.class));
    }

    @Test
    void matchInformationShouldCoverEmptyTitleDirectorCastAndMissPaths() {
        CachedContentVO content = new CachedContentVO();
        content.setTitle("테스트 작품");
        content.setOriginalTitle("Test Work");
        content.setDirector("홍길동");
        content.setCastNames("배우 하나, 배우 둘");

        SearchResultVO emptyKeyword = new SearchResultVO();
        invokeVoid("applyMatchInformation", emptyKeyword, content, "");
        assertEquals("TITLE", emptyKeyword.getMatchType());

        SearchResultVO title = new SearchResultVO();
        invokeVoid("applyMatchInformation", title, content, "테스트작품");
        assertEquals("TITLE", title.getMatchType());

        SearchResultVO director = new SearchResultVO();
        invokeVoid("applyMatchInformation", director, content, "홍길동");
        assertEquals("PERSON", director.getMatchType());
        assertEquals("감독", director.getMatchedPersonRole());

        SearchResultVO cast = new SearchResultVO();
        invokeVoid("applyMatchInformation", cast, content, "배우둘");
        assertEquals("PERSON", cast.getMatchType());
        assertEquals("배우", cast.getMatchedPersonRole());

        SearchResultVO miss = new SearchResultVO();
        invokeVoid("applyMatchInformation", miss, content, "없는검색어");
        assertEquals("TITLE", miss.getMatchType());
    }

    @Test
    void providerHelpersShouldCoverNullBlankLegacySupportedAndUnsupportedValues() {
        assertEquals("", invoke("resolveProviderKey", (Object) null));
        assertEquals("", invoke("resolveProviderKey", "   "));
        assertEquals("netflix", invoke("resolveProviderKey", "8"));
        assertEquals("netflix", invoke("resolveProviderKey", " Netflix "));
        assertEquals("", invoke("resolveProviderKey", "unsupported-provider"));

        Set<String> empty = invoke("providerIdsToKeys", (Object) null);
        assertTrue(empty.isEmpty());

        Set<String> keys = invoke(
                "providerIdsToKeys",
                List.of("8", "netflix", "unsupported-provider"));
        assertEquals(Set.of("netflix"), keys);
    }

    @Test
    void platformAndTextHelpersShouldCoverInvalidRowsAndFallbacks() {
        OttPlatformVO nullName = new OttPlatformVO();
        OttPlatformVO netflix = new OttPlatformVO();
        netflix.setPlatformName("Netflix");

        Map<String, OttPlatformVO> map = invoke(
                "createPlatformMap",
                java.util.Arrays.asList(null, nullName, netflix));
        assertEquals(netflix, map.get("netflix"));

        List<OttPlatformVO> emptyPlatforms = invoke(
                "createPlatformList",
                null,
                map);
        assertTrue(emptyPlatforms.isEmpty());

        assertEquals("", invoke("normalizeSearchText", (Object) null));
        assertEquals("abc123", invoke("normalizeSearchText", "ＡＢＣ-123"));
        assertEquals("", invoke("safeText", (Object) null));
        assertEquals("value", invoke("safeText", "value"));
        assertEquals("액션", invoke("displayGenreName", "ACTION"));
        assertEquals("CUSTOM", invoke("displayGenreName", "CUSTOM"));

        List<String> normalized = invoke(
                "normalizeUpperCaseList",
                java.util.Arrays.asList(null, " ", " drama ", "DRAMA", "movie"));
        assertEquals(List.of("DRAMA", "MOVIE"), normalized);
        assertFalse(normalized.contains(""));
    }

    private void invokeVoid(String method, Object... args) {
        ReflectionTestUtils.invokeMethod(service, method, args);
    }

    @SuppressWarnings("unchecked")
    private <T> T invoke(String method, Object... args) {
        return (T) ReflectionTestUtils.invokeMethod(service, method, args);
    }
}
