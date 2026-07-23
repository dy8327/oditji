package com.project.oditji.recommend.vo;

import java.util.ArrayList;
import java.util.List;

/**
 * 회원의 관심 기록을 기준으로 계산한
 * OTT별 추천 점수 정보를 담는 VO입니다.
 *
 * 조회 점수와 찜 점수를 분리해서 보관하므로
 * 추천 화면에서 어떤 행동이 추천에 영향을 주었는지
 * 설명하는 데 사용할 수 있습니다.
 */
public class RecommendOttScoreVO {

    private Integer platformNo;
    private String platformName;
    private String logoImage;
    private String siteUrl;

    /*
     * 최근 30일 콘텐츠 상세 조회를 기반으로 계산된 점수입니다.
     *
     * 최근 7일:
     * 하루 최대 3회 × 2점
     *
     * 최근 8~30일:
     * 하루 최대 3회 × 1점
     */
    private int viewScore;

    /*
     * 현재 찜 중인 콘텐츠를 기준으로 계산된 점수입니다.
     * 찜 콘텐츠 한 개당 3점입니다.
     */
    private int favoriteScore;

    /*
     * 조회 점수와 찜 점수를 합산한 최종 점수입니다.
     */
    private int totalScore;

    /*
     * 해당 OTT가 제공하는 회원의 서로 다른 관심 콘텐츠 수입니다.
     *
     * 최근 조회 콘텐츠와 찜 콘텐츠의 합집합을 기준으로 합니다.
     */
    private int interestContentCount;

    /*
     * 해당 OTT가 제공하는 회원의 찜 콘텐츠 수입니다.
     */
    private int favoriteContentCount;

    /*
     * 해당 OTT에서 볼 수 있는 회원의 찜 콘텐츠입니다.
     * 화면에는 최대 2개까지만 표시합니다.
     */
    private List<RecommendOttContentVO> favoriteContentList =
            new ArrayList<RecommendOttContentVO>();

    /*
     * 해당 OTT에서 볼 수 있는 최근 7일 조회 콘텐츠입니다.
     * 방문 횟수 순으로 최대 2개까지만 표시합니다.
     */
    private List<RecommendOttContentVO> viewedContentList =
            new ArrayList<RecommendOttContentVO>();
    
    public Integer getPlatformNo() {
        return platformNo;
    }

    public void setPlatformNo(
            Integer platformNo) {

        this.platformNo = platformNo;
    }

    public String getPlatformName() {
        return platformName;
    }

    public void setPlatformName(
            String platformName) {

        this.platformName = platformName;
    }

    public String getLogoImage() {
        return logoImage;
    }

    public void setLogoImage(
            String logoImage) {

        this.logoImage = logoImage;
    }

    public String getSiteUrl() {
        return siteUrl;
    }

    public void setSiteUrl(
            String siteUrl) {

        this.siteUrl = siteUrl;
    }

    public int getViewScore() {
        return viewScore;
    }

    public void setViewScore(
            int viewScore) {

        this.viewScore = viewScore;
    }

    public int getFavoriteScore() {
        return favoriteScore;
    }

    public void setFavoriteScore(
            int favoriteScore) {

        this.favoriteScore = favoriteScore;
    }

    public int getTotalScore() {
        return totalScore;
    }

    public void setTotalScore(
            int totalScore) {

        this.totalScore = totalScore;
    }

    public int getInterestContentCount() {
        return interestContentCount;
    }

    public void setInterestContentCount(
            int interestContentCount) {

        this.interestContentCount =
                interestContentCount;
    }

    public int getFavoriteContentCount() {
        return favoriteContentCount;
    }

    public void setFavoriteContentCount(
            int favoriteContentCount) {

        this.favoriteContentCount =
                favoriteContentCount;
    }

    public List<RecommendOttContentVO> getFavoriteContentList() {
        return favoriteContentList;
    }

    public void setFavoriteContentList(
            List<RecommendOttContentVO> favoriteContentList) {

        this.favoriteContentList =
                favoriteContentList == null
                        ? new ArrayList<RecommendOttContentVO>()
                        : favoriteContentList;
    }

    public List<RecommendOttContentVO> getViewedContentList() {
        return viewedContentList;
    }

    public void setViewedContentList(
            List<RecommendOttContentVO> viewedContentList) {

        this.viewedContentList =
                viewedContentList == null
                        ? new ArrayList<RecommendOttContentVO>()
                        : viewedContentList;
    }
}
