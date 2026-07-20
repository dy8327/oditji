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
    private String genreText;

    private Double tmdbScore;
    private Integer viewCount;
    private Double popularity;

    private Integer episodeCount;
    private String director;
    private String castNames;

    private String matchType;
    private String matchedPersonName;
    private String matchedPersonRole;

    private List<OttPlatformVO> platformList =
            new ArrayList<OttPlatformVO>();

    public SearchResultVO() {
    }

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

    public String getGenreText() {
        return genreText;
    }

    public void setGenreText(
            String genreText) {

        this.genreText = genreText;
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