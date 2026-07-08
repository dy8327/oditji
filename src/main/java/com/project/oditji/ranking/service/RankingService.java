package com.project.oditji.ranking.service;

import java.util.List;
import java.util.Map;

import com.project.oditji.search.vo.SearchResultVO;

public interface RankingService {

    /*
     * 한국에서 지원 OTT로 시청할 수 있는 영화와 TV를 통합하여
     * TMDB popularity 기준으로 반환합니다.
     */
    List<SearchResultVO> getOverallPopularRanking(int limit);

    /*
     * 특정 OTT에서 한국 기준으로 제공되는 영화와 TV를 통합하여
     * TMDB popularity 기준으로 반환합니다.
     */
    List<SearchResultVO> getPlatformPopularRanking(
            String platformName,
            int limit);

    /*
     * ODITJI에서 지원하는 모든 OTT별 랭킹을 반환합니다.
     */
    Map<String, List<SearchResultVO>> getAllPlatformPopularRankings(
            int limit);
}