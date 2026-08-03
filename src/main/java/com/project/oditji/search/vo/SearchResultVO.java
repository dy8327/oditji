package com.project.oditji.search.vo;

import java.util.ArrayList;
import java.util.List;

import com.project.oditji.tmdb.vo.OttPlatformVO;

public class SearchResultVO {

    private Long contentNo;
    private Long tmdbId;
    private String contentType;

    private String title;
    private String originalTitle;
    private String overview;
    private String posterPath;
    private String backdropPath;
    private String releaseDate;

    /**
     * TV 콘텐츠의 최근 회차 공개일입니다.
     * 추천 신작 영역에서 TV 카드 날짜와 정렬 기준으로 사용합니다.
     */
    private String lastAirDate;

    private String genreText;
    private String ageRating;

    private Double tmdbScore;
    private Integer viewCount;
    private Double popularity;

    private Integer episodeCount;
    private String director;
    private String castNames;

    private String matchType;
    private String matchedPersonName;
    private String matchedPersonRole;

    /**
     * 콘텐츠 상세 페이지의 관련 콘텐츠 카드에 표시할 추천 이유입니다.
     * 현재 콘텐츠와 후보 콘텐츠의 장르, 감독, 출연진을 비교하여
     * SearchContentPageCacheService에서 동적으로 생성합니다.
     */
    private String recommendationReason;

    /**
     * 추천 이유의 대표 유형입니다.
     * JSP에서는 이 값을 기준으로 장르·감독·출연진 등
     * 추천 근거별 색상 클래스를 적용합니다.
     */
    private String recommendationReasonType;

    private List<OttPlatformVO> platformList =
            new ArrayList<OttPlatformVO>();

    public Long getContentNo() {
        return contentNo;
    }

    public void setContentNo(Long contentNo) {
        this.contentNo = contentNo;
    }

    public Long getTmdbId() {
        return tmdbId;
    }

    public void setTmdbId(Long tmdbId) {
        this.tmdbId = tmdbId;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getTitle() {

        return title == null
                || title.trim().isEmpty()
                ? "제목 없음"
                : title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getOriginalTitle() {
        return originalTitle;
    }

    public void setOriginalTitle(
            String originalTitle) {

        this.originalTitle = originalTitle;
    }

    public String getOverview() {
        return overview;
    }

    public void setOverview(String overview) {
        this.overview = overview;
    }

    public String getPosterPath() {
        return posterPath;
    }

    public void setPosterPath(
            String posterPath) {

        this.posterPath = posterPath;
    }

    public String getBackdropPath() {
        return backdropPath;
    }

    public void setBackdropPath(
            String backdropPath) {

        this.backdropPath = backdropPath;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(
            String releaseDate) {

        this.releaseDate = releaseDate;
    }

    public String getLastAirDate() {
        return lastAirDate;
    }

    public void setLastAirDate(
            String lastAirDate) {

        this.lastAirDate = lastAirDate;
    }

    public String getGenreText() {
        return genreText;
    }

    public void setGenreText(
            String genreText) {

        this.genreText = genreText;
    }

    public String getAgeRating() {
        return ageRating;
    }

    public void setAgeRating(
            String ageRating) {

        this.ageRating = ageRating;
    }

    public Double getTmdbScore() {
        return tmdbScore;
    }

    public void setTmdbScore(
            Double tmdbScore) {

        this.tmdbScore = tmdbScore;
    }

    public Integer getViewCount() {
        return viewCount;
    }

    public void setViewCount(
            Integer viewCount) {

        this.viewCount = viewCount;
    }

    public Double getPopularity() {
        return popularity;
    }

    public void setPopularity(
            Double popularity) {

        this.popularity = popularity;
    }

    public Integer getEpisodeCount() {
        return episodeCount;
    }

    public void setEpisodeCount(
            Integer episodeCount) {

        this.episodeCount = episodeCount;
    }

    public String getDirector() {
        return director;
    }

    public void setDirector(
            String director) {

        this.director = director;
    }

    public String getCastNames() {
        return castNames;
    }

    public void setCastNames(
            String castNames) {

        this.castNames = castNames;
    }

    public String getMatchType() {
        return matchType;
    }

    public void setMatchType(
            String matchType) {

        this.matchType = matchType;
    }

    public String getMatchedPersonName() {
        return matchedPersonName;
    }

    public void setMatchedPersonName(
            String matchedPersonName) {

        this.matchedPersonName =
                matchedPersonName;
    }

    public String getMatchedPersonRole() {
        return matchedPersonRole;
    }

    public void setMatchedPersonRole(
            String matchedPersonRole) {

        this.matchedPersonRole =
                matchedPersonRole;
    }

    public String getRecommendationReason() {
        return recommendationReason;
    }

    public void setRecommendationReason(
            String recommendationReason) {

        this.recommendationReason =
                recommendationReason;
    }

    public String getRecommendationReasonType() {
        return recommendationReasonType;
    }

    public void setRecommendationReasonType(
            String recommendationReasonType) {

        this.recommendationReasonType =
                recommendationReasonType;
    }

    public List<OttPlatformVO> getPlatformList() {
        return platformList;
    }

    public void setPlatformList(
            List<OttPlatformVO> platformList) {

        this.platformList =
                platformList == null
                        ? new ArrayList<OttPlatformVO>()
                        : platformList;
    }
}