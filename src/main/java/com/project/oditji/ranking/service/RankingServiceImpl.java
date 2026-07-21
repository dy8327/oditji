package com.project.oditji.ranking.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.project.oditji.search.service.SearchContentPageCacheService;
import com.project.oditji.search.vo.SearchResultVO;

/**
 * 랭킹 화면에서 사용할 콘텐츠를 JSONL 공용 캐시에서 조회합니다.
 *
 * 기존 구현처럼 사용자 요청 시 TMDB Discover API와
 * Watch Providers API를 호출하지 않습니다.
 *
 * 데이터 흐름:
 * SearchContentStore
 * -> SearchContentPageCacheService
 * -> RankingServiceImpl
 * -> RankingController
 */
@Service
public class RankingServiceImpl implements RankingService {

    private static final int DEFAULT_LIMIT = 10;
    private static final int MAX_LIMIT = 100;

    /**
     * Map의 키는 contentRanking.jsp에서 조회하는 이름과 동일하게 유지합니다.
     *
     * 쿠팡플레이를 포함하여 ODITJI 지원 OTT 6개를 모두 제공합니다.
     */
    private static final List<String> SUPPORTED_PLATFORM_LIST =
            List.of(
                    "Netflix",
                    "TVING",
                    "wavve",
                    "Disney Plus",
                    "Watcha",
                    "Coupangplay"
            );

    private final SearchContentPageCacheService
            searchContentPageCacheService;

    public RankingServiceImpl(
            SearchContentPageCacheService
                    searchContentPageCacheService) {

        this.searchContentPageCacheService =
                searchContentPageCacheService;
    }

    /**
     * 전체 JSONL 콘텐츠 중 지원 OTT가 하나 이상 있는 콘텐츠를
     * 인기도 우선, 평점 보조 순으로 정렬하여 반환합니다.
     */
    @Override
    public List<SearchResultVO> getOverallPopularRanking(
            int limit) {

        int normalizedLimit =
                normalizeLimit(limit);

        return searchContentPageCacheService
                .getMainRecommendedContent(
                        Collections.emptyList(),
                        normalizedLimit
                );
    }

    /**
     * 전달된 OTT 이름을 프로젝트 내부에서 사용하는 이름으로 정규화한 뒤
     * 해당 OTT가 포함된 JSONL 콘텐츠만 랭킹에 사용합니다.
     */
    @Override
    public List<SearchResultVO> getPlatformPopularRanking(
            String platformName,
            int limit) {

        int normalizedLimit =
                normalizeLimit(limit);

        String normalizedPlatformName =
                normalizePlatformName(
                        platformName
                );

        if (normalizedPlatformName == null) {
            return new ArrayList<SearchResultVO>();
        }

        return searchContentPageCacheService
                .getMainRecommendedContent(
                        Collections.singletonList(
                                normalizedPlatformName
                        ),
                        normalizedLimit
                );
    }

    /**
     * ODITJI 지원 OTT 6개를 순서대로 조회하여
     * JSP에서 사용할 플랫폼별 랭킹 Map을 생성합니다.
     */
    @Override
    public Map<String, List<SearchResultVO>>
            getAllPlatformPopularRankings(
                    int limit) {

        int normalizedLimit =
                normalizeLimit(limit);

        Map<String, List<SearchResultVO>> rankingMap =
                new LinkedHashMap<
                        String,
                        List<SearchResultVO>>();

        for (String platformName
                : SUPPORTED_PLATFORM_LIST) {

            rankingMap.put(
                    platformName,
                    getPlatformPopularRanking(
                            platformName,
                            normalizedLimit
                    )
            );
        }

        return rankingMap;
    }

    /**
     * 화면이나 API에서 다양한 표기로 전달된 OTT 이름을
     * SearchContentPageCacheService가 인식할 수 있는 이름으로 통일합니다.
     */
    private String normalizePlatformName(
            String platformName) {

        if (platformName == null
                || platformName.isBlank()) {

            return null;
        }

        String normalized =
                platformName.trim()
                        .toLowerCase(Locale.ROOT)
                        .replaceAll(
                                "[^a-z0-9가-힣]",
                                ""
                        );

        if (normalized.contains("netflix")
                || normalized.contains("넷플릭스")) {

            return "Netflix";
        }

        if (normalized.contains("tving")
                || normalized.contains("티빙")) {

            return "TVING";
        }

        if (normalized.contains("wavve")
                || normalized.contains("웨이브")) {

            return "wavve";
        }

        if (normalized.contains("disney")
                || normalized.contains("디즈니")) {

            return "Disney Plus";
        }

        if (normalized.contains("watcha")
                || normalized.contains("왓챠")) {

            return "Watcha";
        }

        if (normalized.contains("coupang")
                || normalized.contains("쿠팡")) {

            return "Coupangplay";
        }

        return null;
    }

    /**
     * 비정상적인 limit 값으로 과도한 목록을 반환하지 않도록 보정합니다.
     */
    private int normalizeLimit(
            int limit) {

        if (limit <= 0) {
            return DEFAULT_LIMIT;
        }

        return Math.min(
                limit,
                MAX_LIMIT
        );
    }
}
