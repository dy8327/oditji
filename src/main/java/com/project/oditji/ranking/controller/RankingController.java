package com.project.oditji.ranking.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.project.oditji.ranking.service.RankingService;
import com.project.oditji.search.vo.SearchResultVO;

/**
 * 전체 및 OTT별 인기 랭킹 화면과 JSON API를 제공합니다.
 *
 * 실제 데이터 조회는 RankingService에서 처리하며,
 * 현재 구현은 TMDB 실시간 호출이 아닌 JSONL 공용 캐시를 사용합니다.
 */
@Controller
public class RankingController {

    private static final int OVERALL_RANKING_LIMIT = 20;
    private static final int PLATFORM_RANKING_LIMIT = 10;

    private static final int TAB_ID_INDEX = 0;
    private static final int DISPLAY_NAME_INDEX = 1;
    private static final int PLATFORM_KEY_INDEX = 2;

    private static final String[][] PLATFORM_DEFINITIONS = {
        {"netflix", "Netflix", "Netflix"},
        {"tving", "TVING", "TVING"},
        {"wavve", "wavve", "wavve"},
        {"disney", "Disney+", "Disney Plus"},
        {"watcha", "Watcha", "Watcha"},
        {"coupang", "Coupang Play", "Coupangplay"}
    };

    private final RankingService rankingService;

    public RankingController(
            RankingService rankingService) {

        this.rankingService =
                rankingService;
    }

    /**
     * 전체 랭킹 페이지를 표시합니다.
     *
     * 접속 주소:
     * GET /ranking
     */
    @GetMapping("/ranking")
    public String rankingPage(
            Model model) {

        List<SearchResultVO> overallRanking =
                rankingService
                        .getOverallPopularRanking(
                                OVERALL_RANKING_LIMIT
                        );

        Map<String, List<SearchResultVO>>
                platformRankings =
                rankingService
                        .getAllPlatformPopularRankings(
                                PLATFORM_RANKING_LIMIT
                        );

        List<Map<String, Object>> rankingPanels =
                createRankingPanels(
                        overallRanking,
                        platformRankings
                );

        model.addAttribute(
                "rankingPanels",
                rankingPanels
        );

        return "content/contentRanking";
    }

    /**
     * 전체 인기 랭킹을 JSON으로 반환합니다.
     *
     * 예:
     * GET /api/ranking/overall
     * GET /api/ranking/overall?limit=20
     */
    @GetMapping("/api/ranking/overall")
    @ResponseBody
    public List<SearchResultVO> overallRankingApi(
            @RequestParam(
                    name = "limit",
                    defaultValue = "10")
            int limit) {

        return rankingService
                .getOverallPopularRanking(
                        limit
                );
    }

    /**
     * 특정 OTT의 인기 랭킹을 JSON으로 반환합니다.
     *
     * 예:
     * GET /api/ranking/platform?platform=Netflix
     * GET /api/ranking/platform?platform=Coupangplay&limit=20
     */
    @GetMapping("/api/ranking/platform")
    @ResponseBody
    public List<SearchResultVO> platformRankingApi(
            @RequestParam("platform")
            String platformName,
            @RequestParam(
                    name = "limit",
                    defaultValue = "10")
            int limit) {

        return rankingService
                .getPlatformPopularRanking(
                        platformName,
                        limit
                );
    }

    /**
     * 지원 OTT 6개의 인기 랭킹을 JSON Map으로 반환합니다.
     *
     * 예:
     * GET /api/ranking/platform/all
     */
    @GetMapping("/api/ranking/platform/all")
    @ResponseBody
    public Map<String, List<SearchResultVO>>
            allPlatformRankingApi(
                    @RequestParam(
                            name = "limit",
                            defaultValue = "10")
                    int limit) {

        return rankingService
                .getAllPlatformPopularRankings(
                        limit
                );
    }

    /**
     * JSP가 동일한 패널 마크업을 한 번만 사용하도록
     * 전체 랭킹과 OTT별 랭킹을 공통 화면 데이터로 변환합니다.
     */
    private List<Map<String, Object>> createRankingPanels(
            List<SearchResultVO> overallRanking,
            Map<String, List<SearchResultVO>> platformRankings) {

        List<Map<String, Object>> rankingPanels =
                new ArrayList<Map<String, Object>>();

        rankingPanels.add(
                createRankingPanel(
                        "overall",
                        "전체",
                        "전체 인기 콘텐츠",
                        "지원 OTT에서 제공되는 영화와 TV 통합 인기순입니다.",
                        overallRanking,
                        "전체 인기 랭킹을 불러오지 못했습니다.",
                        true
                )
        );

        for (String[] platformDefinition : PLATFORM_DEFINITIONS) {
            rankingPanels.add(
                    createPlatformRankingPanel(
                            platformDefinition[TAB_ID_INDEX],
                            platformDefinition[DISPLAY_NAME_INDEX],
                            platformDefinition[PLATFORM_KEY_INDEX],
                            platformRankings
                    )
            );
        }

        return rankingPanels;
    }

    /**
     * OTT별 패널에 필요한 제목, 설명, 빈 목록 문구를 생성합니다.
     */
    private Map<String, Object> createPlatformRankingPanel(
            String tabId,
            String displayName,
            String platformKey,
            Map<String, List<SearchResultVO>> platformRankings) {

        List<SearchResultVO> rankingList =
                platformRankings.getOrDefault(
                        platformKey,
                        Collections.emptyList()
                );

        return createRankingPanel(
                tabId,
                displayName,
                displayName + " 인기 콘텐츠",
                "한국 " + displayName
                        + " 정액제 제공 콘텐츠 기준입니다.",
                rankingList,
                displayName
                        + " 인기 랭킹을 불러오지 못했습니다.",
                false
        );
    }

    /**
     * 랭킹 패널 한 개를 JSP에서 사용할 Map 형태로 구성합니다.
     */
    private Map<String, Object> createRankingPanel(
            String tabId,
            String tabLabel,
            String title,
            String description,
            List<SearchResultVO> rankingList,
            String emptyMessage,
            boolean active) {

        Map<String, Object> rankingPanel =
                new LinkedHashMap<String, Object>();

        rankingPanel.put("tabId", tabId);
        rankingPanel.put("tabLabel", tabLabel);
        rankingPanel.put("title", title);
        rankingPanel.put("description", description);
        rankingPanel.put("rankingList", rankingList);
        rankingPanel.put("emptyMessage", emptyMessage);
        rankingPanel.put("active", active);

        return rankingPanel;
    }
}
