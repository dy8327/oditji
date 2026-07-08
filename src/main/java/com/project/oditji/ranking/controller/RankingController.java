package com.project.oditji.ranking.controller;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.project.oditji.ranking.service.RankingService;
import com.project.oditji.search.vo.SearchResultVO;

@Controller
public class RankingController {

    private final RankingService rankingService;

    RankingController(RankingService rankingService) {
        this.rankingService = rankingService;
    }

    /*
     * 전체 랭킹 페이지
     *
     * 접속 주소:
     * GET /ranking
     */
    @GetMapping("/ranking")
    public String rankingPage(Model model) {

        int overallLimit = 20;
        int platformLimit = 10;

        List<SearchResultVO> overallRanking =
                rankingService.getOverallPopularRanking(
                        overallLimit);

        Map<String, List<SearchResultVO>> platformRankings =
                rankingService.getAllPlatformPopularRankings(
                        platformLimit);

        model.addAttribute(
                "overallRanking",
                overallRanking);

        model.addAttribute(
                "platformRankings",
                platformRankings);

        return "content/contentRanking";
    }

    /*
     * 전체 인기랭킹 JSON API
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
                .getOverallPopularRanking(limit);
    }

    /*
     * OTT별 인기랭킹 JSON API
     *
     * 예:
     * GET /api/ranking/platform?platform=Netflix
     * GET /api/ranking/platform?platform=TVING&limit=20
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
                        limit);
    }

    /*
     * 전체 OTT별 인기랭킹 JSON API
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
                .getAllPlatformPopularRankings(limit);
    }
}