package com.project.oditji.common.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.ui.ExtendedModelMap;

import com.project.oditji.member.service.MemberPlatformService;
import com.project.oditji.member.vo.MemberVO;
import com.project.oditji.member.vo.PlatformVO;
import com.project.oditji.search.service.SearchContentPageCacheService;
import com.project.oditji.search.vo.SearchResultVO;

/** 메인 화면의 게스트·회원별 추천 OTT 처리와 모델 구성을 검증합니다. */
@ExtendWith(MockitoExtension.class)
class HomeControllerCoverageTest {

    @Mock
    private SearchContentPageCacheService searchContentPageCacheService;

    @Mock
    private MemberPlatformService memberPlatformService;

    private HomeController controller;

    @BeforeEach
    void setUp() {
        controller = new HomeController(
                searchContentPageCacheService,
                memberPlatformService);
    }

    @Test
    void guestShouldLoadGeneralRecommendationsWithoutMemberPlatformLookup() {
        List<SearchResultVO> popular = List.of(new SearchResultVO());
        List<SearchResultVO> today = List.of(new SearchResultVO());
        List<SearchResultVO> recommended = List.of(new SearchResultVO());
        when(searchContentPageCacheService.getMainPopularContent(5)).thenReturn(popular);
        when(searchContentPageCacheService.getMainTodayContent(20)).thenReturn(today);
        when(searchContentPageCacheService.getMainRecommendedContent(List.of(), 20))
                .thenReturn(recommended);

        ExtendedModelMap model = new ExtendedModelMap();
        String view = controller.home(model, new MockHttpSession());

        assertEquals("index", view);
        assertEquals(popular, model.get("popularContentList"));
        assertEquals(today, model.get("todayContentList"));
        assertEquals(recommended, model.get("recommendedContentList"));
        assertEquals(List.of(), model.get("selectedPlatformList"));
        assertFalse((Boolean) model.get("personalizedRecommendation"));
    }

    @Test
    void loggedInMemberShouldDeduplicateAndIgnoreInvalidPlatformNames() {
        MemberVO member = new MemberVO();
        member.setMemberNo(7L);
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("loginMember", member);

        PlatformVO netflix = platform("Netflix");
        PlatformVO tving = platform("TVING");
        PlatformVO blank = platform("  ");
        List<PlatformVO> selected = Arrays.asList(
                netflix,
                blank,
                null,
                platform("Netflix"),
                tving);
        when(memberPlatformService.findMemberPlatformList(7L)).thenReturn(selected);
        when(searchContentPageCacheService.getMainPopularContent(5)).thenReturn(List.of());
        when(searchContentPageCacheService.getMainTodayContent(20)).thenReturn(List.of());
        when(searchContentPageCacheService.getMainRecommendedContent(
                List.of("Netflix", "TVING"),
                20))
                .thenReturn(List.of());

        ExtendedModelMap model = new ExtendedModelMap();
        String view = controller.home(model, session);

        assertEquals("index", view);
        assertEquals(selected, model.get("selectedPlatformList"));
        assertTrue((Boolean) model.get("personalizedRecommendation"));
        verify(searchContentPageCacheService).getMainRecommendedContent(
                List.of("Netflix", "TVING"),
                20);
    }

    @Test
    void nullMemberPlatformResultShouldFallBackToGeneralRecommendations() {
        MemberVO member = new MemberVO();
        member.setMemberNo(8L);
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("loginMember", member);
        when(memberPlatformService.findMemberPlatformList(8L)).thenReturn(null);
        when(searchContentPageCacheService.getMainPopularContent(5)).thenReturn(List.of());
        when(searchContentPageCacheService.getMainTodayContent(20)).thenReturn(List.of());
        when(searchContentPageCacheService.getMainRecommendedContent(List.of(), 20))
                .thenReturn(List.of());

        ExtendedModelMap model = new ExtendedModelMap();
        controller.home(model, session);

        assertNull(model.get("selectedPlatformList"));
        assertFalse((Boolean) model.get("personalizedRecommendation"));
    }

    private PlatformVO platform(String name) {
        PlatformVO platform = new PlatformVO();
        platform.setPlatformName(name);
        return platform;
    }
}
