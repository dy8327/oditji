package com.project.oditji.ranking.service;

import java.util.List;
import java.util.Map;

import com.project.oditji.search.vo.SearchResultVO;

/**
 * JSONL 공용 콘텐츠 저장소를 기준으로
 * 전체 및 OTT별 인기 랭킹을 조회하는 서비스입니다.
 */
public interface RankingService {

    /**
     * ODITJI 지원 OTT에서 시청 가능한 영화와 TV를 통합하여
     * JSONL에 저장된 인기도와 평점을 기준으로 반환합니다.
     *
     * 정렬 우선순위:
     * 1. 인기도 높은 순
     * 2. 평점 높은 순
     */
    List<SearchResultVO> getOverallPopularRanking(
            int limit);

    /**
     * 특정 OTT에서 제공되는 영화와 TV를 통합하여
     * JSONL에 저장된 인기도와 평점을 기준으로 반환합니다.
     */
    List<SearchResultVO> getPlatformPopularRanking(
            String platformName,
            int limit);

    /**
     * ODITJI에서 지원하는 6개 OTT별 인기 랭킹을 반환합니다.
     */
    Map<String, List<SearchResultVO>>
            getAllPlatformPopularRankings(
                    int limit);
}
