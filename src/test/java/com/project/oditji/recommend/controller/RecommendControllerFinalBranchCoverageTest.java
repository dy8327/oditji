package com.project.oditji.recommend.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.project.oditji.common.service.MainContentPlatformService;
import com.project.oditji.member.service.MemberPlatformService;
import com.project.oditji.member.vo.PlatformVO;
import com.project.oditji.recommend.service.RecommendService;
import com.project.oditji.recommend.vo.RecommendPlatformSectionVO;
import com.project.oditji.search.service.SearchContentPageCacheService;
import com.project.oditji.search.service.SearchContentStore;
import com.project.oditji.search.vo.CachedContentVO;
import com.project.oditji.search.vo.SearchResultVO;

/** 추천 컨트롤러의 private helper 잔여 조건을 직접 보완합니다. */
class RecommendControllerFinalBranchCoverageTest {

    private SearchContentStore searchContentStore;
    private RecommendController controller;

    @BeforeEach
    void setUp() {
        searchContentStore = mock(SearchContentStore.class);
        controller = new RecommendController(
                mock(SearchContentPageCacheService.class),
                searchContentStore,
                mock(MemberPlatformService.class),
                mock(MainContentPlatformService.class),
                mock(RecommendService.class));
    }

    @Test
    void recentDateAndReleaseDateParsingShouldCoverNullBlankInvalidMovieAndTv() {
        assertNull(invoke("resolveRecentContentDate", new Object[] { null }));

        SearchResultVO tv = result(1L, "TV");
        tv.setReleaseDate("2026-08-01");
        tv.setLastAirDate("2026-08-10");
        assertEquals(LocalDate.of(2026, Month.AUGUST, 10),
                invoke("resolveRecentContentDate", tv));

        SearchResultVO movie = result(2L, "MOVIE");
        movie.setReleaseDate("2026-08-11");
        assertEquals(LocalDate.of(2026, Month.AUGUST, 11),
                invoke("resolveRecentContentDate", movie));

        assertNull(invoke("parseReleaseDate", new Object[] { null }));
        assertNull(invoke("parseReleaseDate", "   "));
        assertNull(invoke("parseReleaseDate", "not-a-date"));
    }

    @Test
    void platformSectionHelpersShouldCoverNullBlankAndEmptyInputs() {
        List<RecommendPlatformSectionVO> nullSections = castList(
                invoke("createPlatformSectionList", new Object[] { null }));
        assertTrue(nullSections.isEmpty());

        assertNull(invoke("createPlatformSection", new Object[] { null }));

        PlatformVO nullName = new PlatformVO();
        nullName.setPlatformName(null);
        assertNull(invoke("createPlatformSection", nullName));

        PlatformVO blankName = new PlatformVO();
        blankName.setPlatformName("   ");
        assertNull(invoke("createPlatformSection", blankName));
    }

    @Test
    void ageRatingAttachmentShouldCoverInvalidItemsAndAllFallbacks() {
        invoke("attachAgeRatings", new Object[] { null });
        invoke("attachAgeRatings", List.of());

        SearchResultVO noId = result(null, "MOVIE");
        SearchResultVO noType = result(2L, null);
        SearchResultVO blankType = result(3L, "   ");
        SearchResultVO missingCached = result(4L, "MOVIE");
        SearchResultVO nullRating = result(5L, "TV");
        SearchResultVO blankRating = result(6L, "MOVIE");
        SearchResultVO validRating = result(7L, "TV");

        CachedContentVO cachedNull = cached(5L, "TV", null);
        CachedContentVO cachedBlank = cached(6L, "MOVIE", "   ");
        CachedContentVO cachedValid = cached(7L, "TV", "15세 이상 관람가");

        when(searchContentStore.findByTmdbIdAndContentType(4L, "MOVIE")).thenReturn(null);
        when(searchContentStore.findByTmdbIdAndContentType(5L, "TV")).thenReturn(cachedNull);
        when(searchContentStore.findByTmdbIdAndContentType(6L, "MOVIE")).thenReturn(cachedBlank);
        when(searchContentStore.findByTmdbIdAndContentType(7L, "TV")).thenReturn(cachedValid);

        List<SearchResultVO> contents = Arrays.asList(
                null,
                noId,
                noType,
                blankType,
                missingCached,
                nullRating,
                blankRating,
                validRating);

        invoke("attachAgeRatings", contents);

        assertEquals("등급 정보 없음", missingCached.getAgeRating());
        assertEquals("등급 정보 없음", nullRating.getAgeRating());
        assertEquals("등급 정보 없음", blankRating.getAgeRating());
        assertEquals("15세 이상 관람가", validRating.getAgeRating());
    }

    @Test
    void distinctLimitAndNullableComparatorShouldCoverAllBranches() {
        List<SearchResultVO> nullDistinct = castSearchList(
                invoke("distinctContentList", new Object[] { null }));
        assertTrue(nullDistinct.isEmpty());

        SearchResultVO noId = result(null, "MOVIE");
        SearchResultVO noType = result(1L, null);
        SearchResultVO first = result(10L, "MOVIE");
        SearchResultVO duplicate = result(10L, "MOVIE");
        List<SearchResultVO> distinct = castSearchList(
                invoke("distinctContentList", Arrays.asList(null, noId, noType, first, duplicate)));
        assertEquals(1, distinct.size());
        assertEquals(first, distinct.get(0));

        List<SearchResultVO> emptyLimit = castSearchList(
                invoke("limitList", new Object[] { null, 2 }));
        assertTrue(emptyLimit.isEmpty());

        List<SearchResultVO> source = new ArrayList<SearchResultVO>();
        source.add(first);
        assertEquals(source, castSearchList(invoke("limitList", source, 2)));
        source.add(result(11L, "TV"));
        source.add(result(12L, "MOVIE"));
        assertEquals(2, castSearchList(invoke("limitList", source, 2)).size());

        assertEquals(0, (Integer) invoke("compareNullableDoubleDescending", new Object[] { null, null }));
        assertEquals(1, (Integer) invoke("compareNullableDoubleDescending", new Object[] { null, 1.0 }));
        assertEquals(-1, (Integer) invoke("compareNullableDoubleDescending", new Object[] { 1.0, null }));
        assertTrue((Integer) invoke("compareNullableDoubleDescending", 3.0, 1.0) < 0);
    }

    private SearchResultVO result(Long tmdbId, String contentType) {
        SearchResultVO result = new SearchResultVO();
        result.setTmdbId(tmdbId);
        result.setContentType(contentType);
        return result;
    }

    private CachedContentVO cached(Long tmdbId, String contentType, String ageRating) {
        CachedContentVO content = new CachedContentVO();
        content.setTmdbId(tmdbId);
        content.setContentType(contentType);
        content.setAgeRating(ageRating);
        return content;
    }

    private Object invoke(String methodName, Object... arguments) {
        return ReflectionTestUtils.invokeMethod(controller, methodName, arguments);
    }

    @SuppressWarnings("unchecked")
    private List<RecommendPlatformSectionVO> castList(Object value) {
        return (List<RecommendPlatformSectionVO>) value;
    }

    @SuppressWarnings("unchecked")
    private List<SearchResultVO> castSearchList(Object value) {
        return (List<SearchResultVO>) value;
    }
}
