package com.project.oditji.search.vo;

import java.util.ArrayList;
import java.util.List;

import com.project.oditji.common.vo.ContentMetadataVO;
import com.project.oditji.tmdb.vo.OttPlatformVO;

/**
 * JSONL 검색 캐시와 콘텐츠 추천 화면에서 사용하는 검색 결과입니다.
 * 콘텐츠 공통 메타데이터는 ContentMetadataVO에서 상속합니다.
 */
public class SearchResultVO extends ContentMetadataVO {

    private Long contentNo;
    private String releaseDate;

    /**
     * TV 콘텐츠의 최근 회차 공개일입니다.
     * 추천 신작 영역에서 TV 카드 날짜와 정렬 기준으로 사용합니다.
     */
    private String lastAirDate;

    private Integer viewCount;
    private Double popularity;
    private String matchType;
    private String matchedPersonName;
    private String matchedPersonRole;

    /**
     * 콘텐츠 상세 페이지의 관련 콘텐츠 카드에 표시할 추천 이유입니다.
     */
    private String recommendationReason;

    /**
     * JSP에서 추천 근거별 색상 클래스를 적용할 때 사용하는 유형입니다.
     */
    private String recommendationReasonType;

    /**
     * 메인 신규 콘텐츠에서 오늘 이후 3일 이내 공개 예정인 콘텐츠인지 여부입니다.
     * 화면에서 "예정작" 뱃지를 표시할 때 사용합니다.
     */
    private boolean upcoming;

    private List<OttPlatformVO> platformList = new ArrayList<OttPlatformVO>();

    public Long getContentNo() {
        return contentNo;
    }

    public void setContentNo(Long contentNo) {
        this.contentNo = contentNo;
    }

    @Override
    public String getTitle() {
        String title = super.getTitle();
        return title == null || title.trim().isEmpty() ? "제목 없음" : title;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(String releaseDate) {
        this.releaseDate = releaseDate;
    }

    public String getLastAirDate() {
        return lastAirDate;
    }

    public void setLastAirDate(String lastAirDate) {
        this.lastAirDate = lastAirDate;
    }

    public Integer getViewCount() {
        return viewCount;
    }

    public void setViewCount(Integer viewCount) {
        this.viewCount = viewCount;
    }

    public Double getPopularity() {
        return popularity;
    }

    public void setPopularity(Double popularity) {
        this.popularity = popularity;
    }

    public String getMatchType() {
        return matchType;
    }

    public void setMatchType(String matchType) {
        this.matchType = matchType;
    }

    public String getMatchedPersonName() {
        return matchedPersonName;
    }

    public void setMatchedPersonName(String matchedPersonName) {
        this.matchedPersonName = matchedPersonName;
    }

    public String getMatchedPersonRole() {
        return matchedPersonRole;
    }

    public void setMatchedPersonRole(String matchedPersonRole) {
        this.matchedPersonRole = matchedPersonRole;
    }

    public String getRecommendationReason() {
        return recommendationReason;
    }

    public void setRecommendationReason(String recommendationReason) {
        this.recommendationReason = recommendationReason;
    }

    public String getRecommendationReasonType() {
        return recommendationReasonType;
    }

    public void setRecommendationReasonType(String recommendationReasonType) {
        this.recommendationReasonType = recommendationReasonType;
    }

    public boolean isUpcoming() {
        return upcoming;
    }

    public void setUpcoming(boolean upcoming) {
        this.upcoming = upcoming;
    }

    public List<OttPlatformVO> getPlatformList() {
        return platformList;
    }

    public void setPlatformList(List<OttPlatformVO> platformList) {
        this.platformList = platformList == null
                ? new ArrayList<OttPlatformVO>()
                : platformList;
    }
}
