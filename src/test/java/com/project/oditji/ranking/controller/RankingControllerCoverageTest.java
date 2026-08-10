package com.project.oditji.ranking.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.ExtendedModelMap;

import com.project.oditji.member.service.MemberPlatformService;
import com.project.oditji.member.vo.PlatformVO;
import com.project.oditji.ranking.service.RankingService;
import com.project.oditji.search.vo.SearchResultVO;

/**
 * 랭킹 화면의 플랫폼 로고 null/정상 목록 분기와 JSON API 위임을 검증합니다.
 */
@ExtendWith(MockitoExtension.class)
class RankingControllerCoverageTest {

    @Mock
    private RankingService rankingService;

    @Mock
    private MemberPlatformService memberPlatformService;

    private RankingController controller;

    @BeforeEach
    void setUp() {
        controller = new RankingController(
                rankingService,
                memberPlatformService);
    }

    @Test
    void rankingPageShouldBuildSevenPanelsWhenPlatformListIsNull() {
        SearchResultVO overall =
                new SearchResultVO();

        when(rankingService.getOverallPopularRanking(20))
                .thenReturn(List.of(overall));
        when(rankingService.getAllPlatformPopularRankings(10))
                .thenReturn(Map.of());
        when(memberPlatformService.findPlatformList())
                .thenReturn(null);

        ExtendedModelMap model =
                new ExtendedModelMap();

        assertEquals(
                "content/contentRanking",
                controller.rankingPage(model));

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> panels =
                (List<Map<String, Object>>)
                        model.get("rankingPanels");

        assertEquals(7, panels.size());
        assertEquals("overall", panels.get(0).get("tabId"));
        assertEquals(Boolean.TRUE, panels.get(0).get("active"));
        assertSame(
                List.of(overall).get(0),
                ((List<?>) panels.get(0)
                        .get("rankingList")).get(0));
    }

    @Test
    void rankingPageShouldApplyPlatformLogoAndPlatformRanking() {
        PlatformVO netflix =
                new PlatformVO();
        netflix.setPlatformName("Netflix");
        netflix.setLogoImage("/netflix.png");

        SearchResultVO item =
                new SearchResultVO();

        when(rankingService.getOverallPopularRanking(20))
                .thenReturn(List.of());
        when(rankingService.getAllPlatformPopularRankings(10))
                .thenReturn(
                        Map.of(
                                "Netflix",
                                List.of(item)));
        when(memberPlatformService.findPlatformList())
                .thenReturn(List.of(netflix));

        ExtendedModelMap model =
                new ExtendedModelMap();

        controller.rankingPage(model);

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> panels =
                (List<Map<String, Object>>)
                        model.get("rankingPanels");

        Map<String, Object> netflixPanel =
                panels.get(1);

        assertEquals(
                "/netflix.png",
                netflixPanel.get("tabLogoImage"));
        assertEquals(
                "넷플릭스 인기 콘텐츠",
                netflixPanel.get("title"));
        assertEquals(
                1,
                ((List<?>) netflixPanel
                        .get("rankingList")).size());
    }

    @Test
    void rankingApisShouldDelegateArgumentsAndReturnSameResults() {
        List<SearchResultVO> overall =
                List.of(new SearchResultVO());
        List<SearchResultVO> platform =
                List.of(new SearchResultVO());
        Map<String, List<SearchResultVO>> all =
                Map.of(
                        "Netflix",
                        platform);

        when(rankingService.getOverallPopularRanking(7))
                .thenReturn(overall);
        when(rankingService.getPlatformPopularRanking(
                "Netflix",
                8))
                .thenReturn(platform);
        when(rankingService.getAllPlatformPopularRankings(9))
                .thenReturn(all);

        assertSame(
                overall,
                controller.overallRankingApi(7));
        assertSame(
                platform,
                controller.platformRankingApi(
                        "Netflix",
                        8));
        assertSame(
                all,
                controller.allPlatformRankingApi(9));

        verify(rankingService)
                .getOverallPopularRanking(7);
        verify(rankingService)
                .getPlatformPopularRanking(
                        "Netflix",
                        8);
        verify(rankingService)
                .getAllPlatformPopularRankings(9);
    }
}
