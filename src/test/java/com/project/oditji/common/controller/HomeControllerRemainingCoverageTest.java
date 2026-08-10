package com.project.oditji.common.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
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
import com.project.oditji.search.service.SearchContentPageCacheService;
import com.project.oditji.search.vo.SearchResultVO;

/**
 * 메인 화면의 인기 콘텐츠 10개 초과 분리와 memberNo null 분기를 보완합니다.
 */
@ExtendWith(MockitoExtension.class)
class HomeControllerRemainingCoverageTest {

    @Mock
    private SearchContentPageCacheService pageCacheService;

    @Mock
    private MemberPlatformService memberPlatformService;

    private HomeController controller;

    @BeforeEach
    void setUp() {
        controller =
                new HomeController(
                        pageCacheService,
                        memberPlatformService);
    }

    @Test
    void popularPoolOverTenShouldSplitTopTenAndRemainingSection() {
        List<SearchResultVO> pool =
                new ArrayList<SearchResultVO>();

        for (int index = 0;
                index < 12;
                index++) {
            pool.add(
                    new SearchResultVO());
        }

        when(pageCacheService.getMainPopularContent(30))
                .thenReturn(pool);
        stubOtherSections();

        ExtendedModelMap model =
                new ExtendedModelMap();

        assertEquals(
                "index",
                controller.home(
                        model,
                        new MockHttpSession()));

        @SuppressWarnings("unchecked")
        List<SearchResultVO> top =
                (List<SearchResultVO>)
                        model.get(
                                "popularContentList");

        @SuppressWarnings("unchecked")
        List<SearchResultVO> rest =
                (List<SearchResultVO>)
                        model.get(
                                "popularSectionContentList");

        assertEquals(10, top.size());
        assertEquals(2, rest.size());
        assertSame(pool.get(0), top.get(0));
        assertSame(pool.get(10), rest.get(0));
    }

    @Test
    void loginMemberWithNullMemberNoShouldBehaveLikeGuest() {
        MemberVO member =
                new MemberVO();

        MockHttpSession session =
                new MockHttpSession();
        session.setAttribute(
                "loginMember",
                member);

        when(pageCacheService.getMainPopularContent(30))
                .thenReturn(List.of());
        stubOtherSections();

        ExtendedModelMap model =
                new ExtendedModelMap();

        controller.home(
                model,
                session);

        assertEquals(
                List.of(),
                model.get(
                        "selectedPlatformList"));
        assertFalse(
                (Boolean)
                        model.get(
                                "personalizedRecommendation"));

        verify(
                memberPlatformService,
                never())
                .findMemberPlatformList(null);
    }

    private void stubOtherSections() {
        when(pageCacheService.getMainTodayContent(20))
                .thenReturn(List.of());
        when(pageCacheService.getMainNewContent(20))
                .thenReturn(List.of());
        when(pageCacheService.getMainRecommendedContent(
                List.of(),
                20))
                .thenReturn(List.of());
    }
}
