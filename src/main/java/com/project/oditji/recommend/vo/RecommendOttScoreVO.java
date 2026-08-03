package com.project.oditji.recommend.vo;

import java.util.ArrayList;
import java.util.List;

import com.project.oditji.common.vo.PlatformBaseVO;

/**
 * 회원의 관심 기록을 기준으로 계산한 OTT별 추천 점수 정보입니다.
 */
public class RecommendOttScoreVO extends PlatformBaseVO {

    private Integer platformNo;
    private int viewScore;
    private int favoriteScore;
    private int totalScore;
    private int interestContentCount;
    private int favoriteContentCount;
    private List<RecommendOttContentVO> favoriteContentList = new ArrayList<RecommendOttContentVO>();
    private List<RecommendOttContentVO> viewedContentList = new ArrayList<RecommendOttContentVO>();

    public Integer getPlatformNo() {
        return platformNo;
    }

    public void setPlatformNo(Integer platformNo) {
        this.platformNo = platformNo;
    }

    public int getViewScore() {
        return viewScore;
    }

    public void setViewScore(int viewScore) {
        this.viewScore = viewScore;
    }

    public int getFavoriteScore() {
        return favoriteScore;
    }

    public void setFavoriteScore(int favoriteScore) {
        this.favoriteScore = favoriteScore;
    }

    public int getTotalScore() {
        return totalScore;
    }

    public void setTotalScore(int totalScore) {
        this.totalScore = totalScore;
    }

    public int getInterestContentCount() {
        return interestContentCount;
    }

    public void setInterestContentCount(int interestContentCount) {
        this.interestContentCount = interestContentCount;
    }

    public int getFavoriteContentCount() {
        return favoriteContentCount;
    }

    public void setFavoriteContentCount(int favoriteContentCount) {
        this.favoriteContentCount = favoriteContentCount;
    }

    public List<RecommendOttContentVO> getFavoriteContentList() {
        return favoriteContentList;
    }

    public void setFavoriteContentList(List<RecommendOttContentVO> favoriteContentList) {
        this.favoriteContentList = favoriteContentList == null
                ? new ArrayList<RecommendOttContentVO>()
                : favoriteContentList;
    }

    public List<RecommendOttContentVO> getViewedContentList() {
        return viewedContentList;
    }

    public void setViewedContentList(List<RecommendOttContentVO> viewedContentList) {
        this.viewedContentList = viewedContentList == null
                ? new ArrayList<RecommendOttContentVO>()
                : viewedContentList;
    }
}
