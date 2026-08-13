package com.project.oditji.recommend.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.ExtendedModelMap;

import com.project.oditji.common.service.MainContentPlatformService;
import com.project.oditji.common.util.DateTimeUtil;
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

/**
 * RecommendControllerCoverageTest 이후 남아 있는 정렬/제한/날짜/플랫폼 조건을 보완합니다.
 */
@ExtendWith(MockitoExtension.class)
class RecommendControllerRemainingCoverageTest {

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
    void loggedInMemberWithoutMemberNoShouldUseSafeEmptyPlatformSelection() {
        MemberVO member = new MemberVO();
        RecommendOttResultVO recommendation = new RecommendOttResultVO();
        SearchResultPageVO page = mock(SearchResultPageVO.class);

        when(session.getAttribute("loginMember")).thenReturn(member);
        when(recommendService.getOttRecommendation(null)).thenReturn(recommendation);
        when(pageCacheService.getMainRecommendedContent(List.of(), 100)).thenReturn(null);
        when(pageCacheService.getContentPage(
                eq(""), eq(1), eq(100), anyList(), anyList(), eq(List.of())))
                .thenReturn(page);
        when(page.getResultList()).thenReturn(null);

        ExtendedModelMap model = new ExtendedModelMap();

        assertEquals(
                "recommend/recommendContent",
                controller.recommendContent(model, session));

        assertEquals(Boolean.TRUE, model.get("loggedIn"));
        assertEquals(Boolean.FALSE, model.get("personalizedRecommendation"));
        assertSame(recommendation, model.get("ottRecommendation"));
        assertTrue(resultList(model, "selectedPlatformList").isEmpty());
        assertTrue(resultList(model, "popularContentList").isEmpty());
        verify(memberPlatformService, never()).findMemberPlatformList(null);
    }

    @Test
    void largeSameDateCandidatesShouldCoverLimitsAndNullablePopularityTieBreakers() {
        LocalDate today = LocalDate.now(DateTimeUtil.KOREA_ZONE);
        List<SearchResultVO> candidates = new ArrayList<SearchResultVO>();

        candidates.add(content(1L, 7.0, null, today));
        candidates.add(content(2L, 9.0, 100.0, today));
        candidates.add(content(3L, 9.0, 200.0, today));
        candidates.add(content(4L, 8.0, 300.0, today));
        candidates.add(content(5L, 8.5, 300.0, today));
        candidates.add(content(6L, 6.0, 60.0, today));
        candidates.add(content(7L, 5.0, 50.0, today));
        candidates.add(content(8L, 4.0, 40.0, today));
        candidates.add(content(9L, 3.0, 30.0, today));
        candidates.add(content(10L, 2.0, 20.0, today));
        candidates.add(content(11L, 1.0, 10.0, today));
        candidates.add(content(12L, 0.5, null, today));

        when(pageCacheService.getMainRecommendedContent(List.of(), 100))
                .thenReturn(candidates);
        when(pageCacheService.getContentPage(
                eq(""), eq(1), eq(100), anyList(), anyList(), eq(List.of())))
                .thenReturn(page(candidates));

        ExtendedModelMap model = new ExtendedModelMap();

        controller.recommendContent(model, session);

        List<SearchResultVO> highRated =
                searchResultList(model, "highRatedContentList");
        List<SearchResultVO> popular =
                searchResultList(model, "popularContentList");
        List<SearchResultVO> newContents =
                searchResultList(model, "newContentList");

        assertEquals(10, highRated.size());
        assertEquals(10, popular.size());
        assertEquals(10, newContents.size());

        assertEquals(3L, highRated.get(0).getTmdbId());
        assertEquals(2L, highRated.get(1).getTmdbId());

        assertEquals(5L, popular.get(0).getTmdbId());
        assertEquals(4L, popular.get(1).getTmdbId());

        assertEquals(4L, newContents.get(0).getTmdbId());
        assertEquals(5L, newContents.get(1).getTmdbId());
        assertEquals(11L, newContents.get(9).getTmdbId());
    }

    @Test
    void invalidContentShapesAndBlankDatesShouldCoverGuardConditions() {
        LocalDate today = LocalDate.now(DateTimeUtil.KOREA_ZONE);

        SearchResultVO noTmdbId = new SearchResultVO();
        noTmdbId.setContentType("MOVIE");
        noTmdbId.setReleaseDate(today.toString());

        SearchResultVO noContentType = new SearchResultVO();
        noContentType.setTmdbId(3L);
        noContentType.setReleaseDate(today.toString());

        SearchResultVO blankContentType =
                content(4L, null, 10.0, null);
        blankContentType.setContentType(" ");

        SearchResultVO nullRecentDate =
                content(5L, 9.0, 9.0, today);
        nullRecentDate.setContentType("TV");
        nullRecentDate.setReleaseDate(null);
        nullRecentDate.setLastAirDate(null);

        SearchResultVO blankReleaseDate =
                content(6L, 8.0, 8.0, today);
        blankReleaseDate.setReleaseDate("   ");

        SearchResultVO valid =
                content(7L, 7.0, 7.0, today);

        List<SearchResultVO> candidates = Arrays.asList(
                null,
                noTmdbId,
                noContentType,
                blankContentType,
                nullRecentDate,
                blankReleaseDate,
                valid);

        when(pageCacheService.getMainRecommendedContent(List.of(), 100))
                .thenReturn(candidates);
        when(pageCacheService.getContentPage(
                eq(""), eq(1), eq(100), anyList(), anyList(), eq(List.of())))
                .thenReturn(page(candidates));

        when(searchContentStore.findByTmdbIdAndContentType(5L, "TV"))
                .thenReturn(cached(null));
        when(searchContentStore.findByTmdbIdAndContentType(6L, "MOVIE"))
                .thenReturn(cached(" "));
        when(searchContentStore.findByTmdbIdAndContentType(7L, "MOVIE"))
                .thenReturn(cached("15세 이상 관람가"));

        ExtendedModelMap model = new ExtendedModelMap();

        controller.recommendContent(model, session);

        List<SearchResultVO> highRated =
                searchResultList(model, "highRatedContentList");
        List<SearchResultVO> newContents =
                searchResultList(model, "newContentList");

        assertEquals(4, highRated.size());
        assertEquals(1, newContents.size());
        assertSame(valid, newContents.get(0));

        assertNull(blankContentType.getAgeRating());
        assertEquals("등급 정보 없음", nullRecentDate.getAgeRating());
        assertEquals("등급 정보 없음", blankReleaseDate.getAgeRating());
        assertEquals("15세 이상 관람가", valid.getAgeRating());
    }

    @Test
    void platformSectionShouldIgnoreNullPlatformNameAndBuildValidSection() {
        LocalDate today = LocalDate.now(DateTimeUtil.KOREA_ZONE);
        MemberVO member = member(77L);

        PlatformVO nullName = platform(1L, null, null);
        PlatformVO netflix = platform(2L, "Netflix", "netflix.png");
        SearchResultVO sectionContent =
                content(100L, 8.0, 88.0, today);

        when(session.getAttribute("loginMember")).thenReturn(member);
        when(memberPlatformService.findMemberPlatformList(77L))
                .thenReturn(Arrays.asList(nullName, netflix));
        when(pageCacheService.getMainRecommendedContent(List.of("Netflix"), 100))
                .thenReturn(List.of());
        when(pageCacheService.getContentPage(
                eq(""), eq(1), eq(100), anyList(), anyList(), eq(List.of("Netflix"))))
                .thenReturn(
                        page(List.of(sectionContent)),
                        page(List.of(sectionContent)));

        ExtendedModelMap model = new ExtendedModelMap();

        controller.recommendContent(model, session);

        List<RecommendPlatformSectionVO> sections =
                platformSectionList(model);

        assertEquals(Boolean.TRUE, model.get("personalizedRecommendation"));
        assertEquals(1, sections.size());
        assertEquals(2L, sections.get(0).getPlatformNo());
        assertEquals("Netflix", sections.get(0).getPlatformName());
        assertEquals("netflix.png", sections.get(0).getLogoImage());
        assertEquals(List.of(sectionContent), sections.get(0).getContentList());
    }

    private MemberVO member(Long memberNo) {
        MemberVO member = new MemberVO();
        member.setMemberNo(memberNo);
        return member;
    }

    private PlatformVO platform(
            Long platformNo,
            String platformName,
            String logoImage) {

        PlatformVO platform = new PlatformVO();
        platform.setPlatformNo(platformNo);
        platform.setPlatformName(platformName);
        platform.setLogoImage(logoImage);
        return platform;
    }

    private SearchResultVO content(
            Long tmdbId,
            Double score,
            Double popularity,
            LocalDate releaseDate) {

        SearchResultVO content = new SearchResultVO();
        content.setTmdbId(tmdbId);
        content.setContentType("MOVIE");
        content.setTmdbScore(score);
        content.setPopularity(popularity);
        content.setReleaseDate(
                releaseDate == null
                        ? null
                        : releaseDate.toString());
        return content;
    }

    private SearchResultPageVO page(
            List<SearchResultVO> resultList) {

        SearchResultPageVO page = new SearchResultPageVO();
        page.setResultList(resultList);
        return page;
    }

    private CachedContentVO cached(
            String ageRating) {

        CachedContentVO cached = new CachedContentVO();
        cached.setAgeRating(ageRating);
        return cached;
    }

    @SuppressWarnings("unchecked")
    private <T> List<T> resultList(
            ExtendedModelMap model,
            String attributeName) {

        return (List<T>) model.get(attributeName);
    }

    @SuppressWarnings("unchecked")
    private List<SearchResultVO> searchResultList(
            ExtendedModelMap model,
            String attributeName) {

        return (List<SearchResultVO>) model.get(attributeName);
    }

    @SuppressWarnings("unchecked")
    private List<RecommendPlatformSectionVO> platformSectionList(
            ExtendedModelMap model) {

        return (List<RecommendPlatformSectionVO>) model.get(
                "platformSectionList");
    }
}
