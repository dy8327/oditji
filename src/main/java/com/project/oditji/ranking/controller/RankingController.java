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

/**
 * 전체 및 OTT별 인기 랭킹 화면과 JSON API를 제공합니다.
 *
 * 실제 데이터 조회는 RankingService에서 처리하며,
 * 현재 구현은 TMDB 실시간 호출이 아닌 JSONL 공용 캐시를 사용합니다.
 */
@Controller
public class RankingController {

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

        int overallLimit = 20;
        int platformLimit = 10;

        List<SearchResultVO> overallRanking =
                rankingService
                        .getOverallPopularRanking(
                                overallLimit
                        );

        Map<String, List<SearchResultVO>>
                platformRankings =
                rankingService
                        .getAllPlatformPopularRankings(
                                platformLimit
                        );

        model.addAttribute(
                "overallRanking",
                overallRanking
        );

        model.addAttribute(
                "platformRankings",
                platformRankings
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
}
