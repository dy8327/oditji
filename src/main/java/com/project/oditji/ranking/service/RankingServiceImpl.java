package com.project.oditji.ranking.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
 * 기존처럼 사용자 요청 시 TMDB API를 다시 호출하지 않고,
 * JSONL 메모리 캐시에 저장된 인기도와 평점을 함께 사용합니다.
 *
 * 랭킹 계산 기준:
 * 1. TMDB 평점 6.0 이상만 사용
 * 2. 인기도를 로그 정규화하여 0~100점으로 변환
 * 3. 평점을 0~100점으로 변환
 * 4. 인기도 60% + 평점 40%로 최종 점수 계산
 * 5. 최종 점수가 같으면 인기도, 평점 순으로 정렬
 */
@Service
public class RankingServiceImpl implements RankingService {

    private static final int DEFAULT_LIMIT = 10;
    private static final int MAX_LIMIT = 100;
    private static final String PLATFORM_WAVVE = "wavve";

    /**
     * 현재 JSONL 최대 적재량보다 넉넉하게 조회합니다.
     *
     * getMainRecommendedContent()는 전달한 개수만큼 반환하므로,
     * 50,000건을 요청하면 현재 저장된 약 33,000건 전체를 후보로
     * 받아서 복합 점수를 계산할 수 있습니다.
     */
    private static final int RANKING_CANDIDATE_LIMIT = 50000;

    /**
     * 랭킹에 포함할 최소 TMDB 평점입니다.
     *
     * 평점이 지나치게 낮은 콘텐츠가 높은 인기도만으로
     * 상위 랭킹에 노출되는 것을 방지합니다.
     */
    private static final double MINIMUM_TMDB_SCORE = 6.0;

    /**
     * 최종 랭킹 점수에서 인기도가 차지하는 비중입니다.
     */
    private static final double POPULARITY_WEIGHT = 0.60;

    /**
     * 최종 랭킹 점수에서 TMDB 평점이 차지하는 비중입니다.
     */
    private static final double RATING_WEIGHT = 0.40;

    /**
     * Map의 키는 contentRanking.jsp에서 조회하는 이름과
     * 동일하게 유지합니다.
     */
    private static final List<String> SUPPORTED_PLATFORM_LIST =
            List.of(
                    "Netflix",
                    "TVING",
                    PLATFORM_WAVVE,
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
     * 전체 JSONL 콘텐츠를 대상으로
     * 인기도와 평점을 합산한 랭킹을 반환합니다.
     */
    @Override
    public List<SearchResultVO> getOverallPopularRanking(
            int limit) {

        int normalizedLimit =
                normalizeLimit(limit);

        List<SearchResultVO> candidateList =
                searchContentPageCacheService
                        .getMainRecommendedContent(
                                Collections.emptyList(),
                                RANKING_CANDIDATE_LIMIT
                        );

        return createWeightedRanking(
                candidateList,
                normalizedLimit
        );
    }

    /**
     * 전달된 OTT에서 제공되는 콘텐츠만 대상으로
     * 인기도와 평점을 합산한 랭킹을 반환합니다.
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

        List<SearchResultVO> candidateList =
                searchContentPageCacheService
                        .getMainRecommendedContent(
                                Collections.singletonList(
                                        normalizedPlatformName
                                ),
                                RANKING_CANDIDATE_LIMIT
                        );

        return createWeightedRanking(
                candidateList,
                normalizedLimit
        );
    }

    /**
     * 지원 OTT 6개의 랭킹을 순서대로 생성합니다.
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
     * 인기도와 평점을 동일한 0~100 범위로 환산한 뒤
     * 각각의 가중치를 적용하여 복합 랭킹을 만듭니다.
     */
    private List<SearchResultVO> createWeightedRanking(
            List<SearchResultVO> sourceList,
            int limit) {

        if (sourceList == null
                || sourceList.isEmpty()) {

            return new ArrayList<SearchResultVO>();
        }

        /*
         * 원본 캐시 목록을 직접 수정하지 않도록
         * 새로운 목록에 랭킹 후보를 담습니다.
         */
        List<SearchResultVO> filteredList =
                new ArrayList<SearchResultVO>();

        /*
         * TMDB 평점 6.0 미만 콘텐츠는 랭킹 후보에서 제외합니다.
         */
        for (SearchResultVO content : sourceList) {

            if (content == null) {
                continue;
            }

            double tmdbScore =
                    safeDouble(
                            content.getTmdbScore()
                    );

            if (tmdbScore < MINIMUM_TMDB_SCORE) {
                continue;
            }

            filteredList.add(content);
        }

        if (filteredList.isEmpty()) {
            return filteredList;
        }

        /*
         * 현재 후보 목록에서 가장 높은 popularity 값을 구합니다.
         *
         * 이 최댓값을 기준으로 각 콘텐츠의 인기도를
         * 0~100점 범위로 정규화합니다.
         */
        double maxPopularity =
                filteredList.stream()
                        .map(
                                SearchResultVO::getPopularity
                        )
                        .filter(
                                value ->
                                        value != null
                                        && value > 0.0
                        )
                        .mapToDouble(
                                Double::doubleValue
                        )
                        .max()
                        .orElse(0.0);

        /*
         * 최종 복합 랭킹 점수 기준으로 내림차순 정렬합니다.
         *
         * 복합 점수가 같은 경우:
         * 1. 인기도가 높은 콘텐츠
         * 2. 평점이 높은 콘텐츠
         * 순서로 배치합니다.
         */
        Comparator<SearchResultVO> rankingComparator =
                Comparator
                        .comparingDouble(
                                (SearchResultVO content) ->
                                        calculateRankingScore(
                                                content,
                                                maxPopularity
                                        )
                        )
                        .reversed()
                        .thenComparing(
                                SearchResultVO::getPopularity,
                                Comparator.nullsLast(
                                        Comparator.reverseOrder()
                                )
                        )
                        .thenComparing(
                                SearchResultVO::getTmdbScore,
                                Comparator.nullsLast(
                                        Comparator.reverseOrder()
                                )
                        );

        filteredList.sort(rankingComparator);

        if (filteredList.size() <= limit) {
            return filteredList;
        }

        return new ArrayList<SearchResultVO>(
                filteredList.subList(
                        0,
                        limit
                )
        );
    }

    /**
     * 콘텐츠 한 건의 최종 랭킹 점수를 계산합니다.
     *
     * 계산식:
     *
     * 인기도 정규화 점수 × 0.6
     * + 평점 환산 점수 × 0.4
     *
     * TMDB popularity는 값의 편차가 매우 크므로
     * Math.log1p()를 사용해 차이를 완화한 뒤,
     * 현재 후보 목록의 최대 인기도를 기준으로 정규화합니다.
     */
    private double calculateRankingScore(
            SearchResultVO content,
            double maxPopularity) {

        double popularity =
                Math.max(
                        safeDouble(
                                content.getPopularity()
                        ),
                        0.0
                );

        /*
         * TMDB 평점은 원래 0~10 범위이므로
         * 비정상적인 값이 들어와도 0~10으로 제한합니다.
         */
        double tmdbScore =
                Math.max(
                        0.0,
                        Math.min(
                                safeDouble(
                                        content.getTmdbScore()
                                ),
                                10.0
                        )
                );

        double popularityScore = 0.0;

        /*
         * 인기도를 로그 정규화합니다.
         *
         * 단순히 popularity / maxPopularity를 사용하면
         * 인기도가 매우 높은 일부 콘텐츠 때문에
         * 나머지 콘텐츠의 점수가 지나치게 낮아질 수 있습니다.
         *
         * log1p를 사용하면 인기도 차이를 유지하면서도
         * 극단적인 수치 차이를 완화할 수 있습니다.
         */
        if (maxPopularity > 0.0
                && popularity > 0.0) {

            popularityScore =
                    Math.log1p(popularity)
                    / Math.log1p(maxPopularity)
                    * 100.0;
        }

        /*
         * TMDB 평점 0~10을 0~100점으로 환산합니다.
         */
        double ratingScore =
                tmdbScore * 10.0;

        /*
         * 최종 복합 점수를 반환합니다.
         */
        return popularityScore
                * POPULARITY_WEIGHT
                + ratingScore
                * RATING_WEIGHT;
    }

    /**
     * null 숫자를 안전하게 0으로 처리합니다.
     */
    private double safeDouble(
            Double value) {

        return value == null
                ? 0.0
                : value;
    }

    /**
     * 다양한 OTT 표기를 프로젝트 내부 표기로 통일합니다.
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

        if (normalized.contains(PLATFORM_WAVVE)
                || normalized.contains("웨이브")) {

            return PLATFORM_WAVVE;
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
     * 비정상적인 limit 값으로 과도한 목록을 반환하지 않도록
     * 반환 개수를 보정합니다.
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