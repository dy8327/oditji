package com.project.oditji.recommend.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.ExtendedModelMap;

import com.project.oditji.common.service.MainContentPlatformService;
import com.project.oditji.member.service.MemberPlatformService;
import com.project.oditji.member.vo.MemberVO;
import com.project.oditji.member.vo.PlatformVO;
import com.project.oditji.recommend.service.RecommendService;
import com.project.oditji.recommend.vo.RecommendOttResultVO;
import com.project.oditji.recommend.vo.RecommendPlatformSectionVO;
import com.project.oditji.search.service.SearchContentPageCacheService;
import com.project.oditji.search.service.SearchContentStore;
import com.project.oditji.search.vo.CachedContentVO;
import com.project.oditji.search.vo.SearchResultPageVO;
import com.project.oditji.search.vo.SearchResultVO;

import jakarta.servlet.http.HttpSession;

/** 추천 화면의 정렬, 신작, 플랫폼 섹션과 연령등급 보강을 검증합니다. */
@ExtendWith(MockitoExtension.class)
class RecommendControllerCoverageTest {

    @Mock
    private SearchContentPageCacheService pageCacheService;

    @Mock
    private SearchContentStore searchContentStore;

    @Mock
    private MemberPlatformService memberPlatformService;

    @Mock
    private MainContentPlatformService mainContentPlatformService;

    @Mock
    private RecommendService recommendService;

    @Mock
    private HttpSession session;

    private RecommendController controller;

    @BeforeEach
    void setUp() {
        controller = new RecommendController(
                pageCacheService,
                searchContentStore,
                memberPlatformService,
                mainContentPlatformService,
                recommendService);
    }

    @Test
    void anonymousPageShouldExposeEmptySafeDefaults() {
        RecommendOttResultVO recommendation = new RecommendOttResultVO();
        when(recommendService.getOttRecommendation(null)).thenReturn(recommendation);
        when(pageCacheService.getMainRecommendedContent(List.of(), 100)).thenReturn(null);
        when(pageCacheService.getContentPage(
                eq(""), eq(1), eq(100), anyList(), anyList(), eq(List.of())))
                .thenReturn(null);
        ExtendedModelMap model = new ExtendedModelMap();

        assertEquals("recommend/recommendContent", controller.recommendContent(model, session));
        assertEquals(Boolean.FALSE, model.get("loggedIn"));
        assertEquals(Boolean.FALSE, model.get("personalizedRecommendation"));
        assertSame(recommendation, model.get("ottRecommendation"));
        assertTrue(list(model, "selectedPlatformList").isEmpty());
        assertTrue(list(model, "highRatedContentList").isEmpty());
        assertTrue(list(model, "popularContentList").isEmpty());
        assertTrue(list(model, "newContentList").isEmpty());
        assertTrue(list(model, "platformSectionList").isEmpty());
        verify(memberPlatformService, never()).findMemberPlatformList(anyLong());
    }

    @Test
    void nullMemberPlatformListShouldDisablePersonalizedSections() {
        MemberVO member = member(7L);
        when(session.getAttribute("loginMember")).thenReturn(member);
        when(memberPlatformService.findMemberPlatformList(7L)).thenReturn(null);
        when(recommendService.getOttRecommendation(7L)).thenReturn(new RecommendOttResultVO());
        when(pageCacheService.getMainRecommendedContent(List.of(), 100)).thenReturn(List.of());
        SearchResultPageVO emptyPage = page(List.of());
        when(pageCacheService.getContentPage(
                eq(""), eq(1), eq(100), anyList(), anyList(), eq(List.of())))
                .thenReturn(emptyPage);
        ExtendedModelMap model = new ExtendedModelMap();

        controller.recommendContent(model, session);

        assertEquals(Boolean.TRUE, model.get("loggedIn"));
        assertEquals(Boolean.FALSE, model.get("personalizedRecommendation"));
        assertEquals(null, model.get("selectedPlatformList"));
        assertTrue(list(model, "platformSectionList").isEmpty());
    }

    @Test
    void personalizedPageShouldSortDeduplicateFilterAndBuildSections() {
        LocalDate today = LocalDate.now();
        MemberVO member = member(8L);
        when(session.getAttribute("loginMember")).thenReturn(member);

        PlatformVO netflix = platform(1L, "Netflix", "netflix.png");
        PlatformVO tving = platform(2L, "TVING", "tving.png");
        when(memberPlatformService.findMemberPlatformList(8L))
                .thenReturn(List.of(netflix, tving));

        RecommendOttResultVO recommendation = new RecommendOttResultVO();
        when(recommendService.getOttRecommendation(8L)).thenReturn(recommendation);

        SearchResultVO movie = content(1L, "MOVIE", 8.0, 50.0, today.minusDays(5).toString(), null);
        SearchResultVO movieDuplicate = content(1L, "MOVIE", 10.0, 999.0, today.toString(), null);
        SearchResultVO tv = content(2L, "TV", 9.0, 40.0, null, today.minusDays(2).toString());
        SearchResultVO oldMovie = content(3L, "MOVIE", 7.0, 100.0, today.minusMonths(2).toString(), null);
        SearchResultVO futureMovie = content(4L, "MOVIE", 6.0, 80.0, today.plusDays(1).toString(), null);
        SearchResultVO invalidDate = content(5L, "MOVIE", null, null, "invalid", null);
        SearchResultVO invalidKey = new SearchResultVO();

        when(pageCacheService.getMainRecommendedContent(List.of("Netflix", "TVING"), 100))
                .thenReturn(List.of(movie, movieDuplicate, tv, oldMovie, futureMovie, invalidDate, invalidKey));

        SearchResultVO newest = content(7L, "MOVIE", 5.0, null, today.toString(), null);
        SearchResultVO sectionMovie = content(9L, "MOVIE", 7.5, 77.0, today.minusDays(1).toString(), null);

        when(pageCacheService.getContentPage(
                eq(""), eq(1), eq(100), anyList(), anyList(), anyList()))
                .thenAnswer(invocation -> {
                    List<String> providers = invocation.getArgument(5);
                    if (providers.equals(List.of("Netflix", "TVING"))) {
                        return page(List.of(oldMovie, newest, movie));
                    }
                    if (providers.equals(List.of("Netflix"))) {
                        return page(List.of(sectionMovie));
                    }
                    return page(List.of());
                });

        when(searchContentStore.findByTmdbIdAndContentType(anyLong(), anyString()))
                .thenAnswer(invocation -> {
                    Long tmdbId = invocation.getArgument(0);
                    if (Long.valueOf(1L).equals(tmdbId)) {
                        return cached("15세 이상 관람가");
                    }
                    if (Long.valueOf(2L).equals(tmdbId)) {
                        return cached(" ");
                    }
                    if (Long.valueOf(3L).equals(tmdbId)) {
                        return cached("12세 이상 관람가");
                    }
                    if (Long.valueOf(9L).equals(tmdbId)) {
                        return cached("전체 관람가");
                    }
                    return null;
                });

        ExtendedModelMap model = new ExtendedModelMap();
        assertEquals("recommend/recommendContent", controller.recommendContent(model, session));

        assertEquals(Boolean.TRUE, model.get("loggedIn"));
        assertEquals(Boolean.TRUE, model.get("personalizedRecommendation"));
        assertSame(recommendation, model.get("ottRecommendation"));

        List<SearchResultVO> highRated = resultList(model, "highRatedContentList");
        assertEquals(List.of(tv, movie, oldMovie, futureMovie, invalidDate), highRated);
        assertEquals("등급 정보 없음", tv.getAgeRating());
        assertEquals("15세 이상 관람가", movie.getAgeRating());

        List<SearchResultVO> popular = resultList(model, "popularContentList");
        assertEquals(List.of(oldMovie, movie, newest), popular);

        List<SearchResultVO> newContents = resultList(model, "newContentList");
        assertEquals(List.of(newest, tv, movie), newContents);
        assertFalse(newContents.contains(oldMovie));
        assertFalse(newContents.contains(futureMovie));
        assertFalse(newContents.contains(invalidDate));

        @SuppressWarnings("unchecked")
        List<RecommendPlatformSectionVO> sections =
                (List<RecommendPlatformSectionVO>) model.get("platformSectionList");
        assertEquals(1, sections.size());
        assertEquals(1L, sections.get(0).getPlatformNo());
        assertEquals("Netflix", sections.get(0).getPlatformName());
        assertEquals("netflix.png", sections.get(0).getLogoImage());
        assertEquals(List.of(sectionMovie), sections.get(0).getContentList());
        assertEquals("전체 관람가", sectionMovie.getAgeRating());

        verify(mainContentPlatformService, atLeastOnce())
                .attachPlatformLogos(anyList(), anyList());
    }

    @Test
    void invalidAndDuplicatePlatformEntriesShouldBeIgnoredForProviderNames() {
        MemberVO member = member(9L);
        when(session.getAttribute("loginMember")).thenReturn(member);
        PlatformVO blank = platform(1L, " ", null);
        PlatformVO netflix = platform(2L, "Netflix", null);
        PlatformVO duplicate = platform(3L, "Netflix", null);
        when(memberPlatformService.findMemberPlatformList(9L))
                .thenReturn(java.util.Arrays.asList(null, blank, netflix, duplicate));
        when(recommendService.getOttRecommendation(9L)).thenReturn(new RecommendOttResultVO());
        when(pageCacheService.getMainRecommendedContent(List.of("Netflix"), 100)).thenReturn(List.of());
        when(pageCacheService.getContentPage(
                eq(""), eq(1), eq(100), anyList(), anyList(), anyList()))
                .thenReturn(page(List.of()));
        ExtendedModelMap model = new ExtendedModelMap();

        controller.recommendContent(model, session);

        assertEquals(Boolean.TRUE, model.get("personalizedRecommendation"));
        assertTrue(resultList(model, "platformSectionList").isEmpty());
        verify(pageCacheService).getMainRecommendedContent(List.of("Netflix"), 100);
    }

    private MemberVO member(Long memberNo) {
        MemberVO member = new MemberVO();
        member.setMemberNo(memberNo);
        return member;
    }

    private PlatformVO platform(Long no, String name, String logo) {
        PlatformVO platform = new PlatformVO();
        platform.setPlatformNo(no);
        platform.setPlatformName(name);
        platform.setLogoImage(logo);
        return platform;
    }

    private SearchResultVO content(
            Long tmdbId,
            String type,
            Double score,
            Double popularity,
            String releaseDate,
            String lastAirDate) {

        SearchResultVO content = new SearchResultVO();
        content.setTmdbId(tmdbId);
        content.setContentType(type);
        content.setTmdbScore(score);
        content.setPopularity(popularity);
        content.setReleaseDate(releaseDate);
        content.setLastAirDate(lastAirDate);
        return content;
    }

    private CachedContentVO cached(String ageRating) {
        CachedContentVO cached = new CachedContentVO();
        cached.setAgeRating(ageRating);
        return cached;
    }

    private SearchResultPageVO page(List<SearchResultVO> list) {
        SearchResultPageVO page = new SearchResultPageVO();
        page.setResultList(list);
        return page;
    }

    @SuppressWarnings("unchecked")
    private <T> List<T> list(ExtendedModelMap model, String name) {
        return (List<T>) model.get(name);
    }

    @SuppressWarnings("unchecked")
    private List<SearchResultVO> resultList(ExtendedModelMap model, String name) {
        return (List<SearchResultVO>) model.get(name);
    }
}
