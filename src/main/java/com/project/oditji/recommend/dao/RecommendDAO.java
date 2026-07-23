package com.project.oditji.recommend.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.project.oditji.recommend.vo.RecommendOttContentVO;
import com.project.oditji.recommend.vo.RecommendOttScoreVO;

/**
 * OTT 관심도 점수 계산에 필요한 데이터를 조회하는 DAO입니다.
 *
 * MyBatis Mapper 인터페이스 방식으로 동작하므로
 * 별도의 RecommendDAOImpl은 필요하지 않습니다.
 */
@Mapper
public interface RecommendDAO {

    /**
     * 회원의 최근 조회 이력과 현재 찜 콘텐츠를 이용해
     * OTT별 관심도 점수를 계산합니다.
     *
     * 결과는 총점이 높은 OTT부터 반환합니다.
     */
    List<RecommendOttScoreVO> selectOttInterestScoreList(
            Long memberNo);

    /**
     * 회원의 서로 다른 관심 콘텐츠 수를 계산합니다.
     *
     * 최근 30일 조회 콘텐츠와 현재 찜 콘텐츠를
     * 중복 없이 합산합니다.
     */
    int countDistinctInterestContent(
            Long memberNo);

    /**
     * 최종 추천 대상으로 선택된 OTT들이 제공하는
     * 회원의 관심 콘텐츠 수를 중복 없이 계산합니다.
     *
     * 공동 추천일 때 같은 콘텐츠가 두 OTT 모두에 있어도
     * 한 번만 집계하기 위해 플랫폼 번호 목록을 전달합니다.
     */
    int countDistinctInterestContentByPlatforms(
            @Param("memberNo") Long memberNo,
            @Param("platformNoList")
            List<Integer> platformNoList);

    /**
     * 최종 추천 대상으로 선택된 OTT들이 제공하는
     * 회원의 찜 콘텐츠 수를 중복 없이 계산합니다.
     *
     * 공동 추천된 두 OTT에 같은 찜 콘텐츠가 있더라도
     * 화면에는 한 개로 표시되도록 합집합 기준으로 집계합니다.
     */
    int countDistinctFavoriteContentByPlatforms(
            @Param("memberNo") Long memberNo,
            @Param("platformNoList")
            List<Integer> platformNoList);

    /**
     * 추천 OTT에서 볼 수 있는 회원의 찜 콘텐츠를
     * 최근 찜한 순서로 최대 2개 조회합니다.
     */
    List<RecommendOttContentVO> selectFavoriteContentList(
            @Param("memberNo") Long memberNo,
            @Param("platformNo") Integer platformNo);

    /**
     * 추천 OTT에서 볼 수 있는 최근 7일 조회 콘텐츠를
     * 방문 횟수 순으로 최대 2개 조회합니다.
     *
     * 방문 횟수가 같으면 콘텐츠 전체 인기 조회 수가
     * 높은 콘텐츠를 먼저 반환합니다.
     */
    List<RecommendOttContentVO> selectRecentViewedContentList(
            @Param("memberNo") Long memberNo,
            @Param("platformNo") Integer platformNo);
}