package com.project.oditji.recommend.service;

import com.project.oditji.recommend.vo.RecommendOttResultVO;

/**
 * 회원 맞춤 OTT 추천 서비스 인터페이스입니다.
 */
public interface RecommendService {

    /**
     * 회원의 최근 콘텐츠 조회와 찜 데이터를 분석해
     * 최종 OTT 추천 결과를 반환합니다.
     */
    RecommendOttResultVO getOttRecommendation(
            Long memberNo);
}