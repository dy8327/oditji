package com.project.oditji.admin.vo;

import java.util.Date;

/**
 * 콘텐츠 관리 VO (테이블: CONTENT, 제공 OTT는 CONTENT_PLATFORM 조인 집계)
 */
public class ContentManageVO {

    private Long contentNo;
    private Long tmdbId;
    private String contentType;   // MOVIE, TV
    private String title;
    private String originalTitle;
    private String overview;
    private String posterPath;
    private String backdropPath;
    private Date releaseDate;
    private String genreText;
    private Integer runtime;
    private Integer episodeCount;
    private String director;
    private String castNames;
    private String ageRating;
    private Double tmdbScore;
    private Long viewCount;
    private String platformNames; // CONTENT_PLATFORM + OTT_PLATFORM LISTAGG 집계 (조회 전용)
    private Date createdAt;
    private Date updatedAt;

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
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getOriginalTitle() {
        return originalTitle;
    }

    public void setOriginalTitle(String originalTitle) {
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

    public void setPosterPath(String posterPath) {
        this.posterPath = posterPath;
    }

    public String getBackdropPath() {
        return backdropPath;
    }

    public void setBackdropPath(String backdropPath) {
        this.backdropPath = backdropPath;
    }

    public Date getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(Date releaseDate) {
        this.releaseDate = releaseDate;
    }

    public String getGenreText() {
        return genreText;
    }

    public void setGenreText(String genreText) {
        this.genreText = genreText;
    }

    public Integer getRuntime() {
        return runtime;
    }

    public void setRuntime(Integer runtime) {
        this.runtime = runtime;
    }

    public Integer getEpisodeCount() {
        return episodeCount;
    }

    public void setEpisodeCount(Integer episodeCount) {
        this.episodeCount = episodeCount;
    }

    public String getDirector() {
        return director;
    }

    public void setDirector(String director) {
        this.director = director;
    }

    public String getCastNames() {
        return castNames;
    }

    public void setCastNames(String castNames) {
        this.castNames = castNames;
    }

    public String getAgeRating() {
        return ageRating;
    }

    public void setAgeRating(String ageRating) {
        this.ageRating = ageRating;
    }

    public Double getTmdbScore() {
        return tmdbScore;
    }

    public void setTmdbScore(Double tmdbScore) {
        this.tmdbScore = tmdbScore;
    }

    public Long getViewCount() {
        return viewCount;
    }

    public void setViewCount(Long viewCount) {
        this.viewCount = viewCount;
    }

    public String getPlatformNames() {
        return platformNames;
    }

    public void setPlatformNames(String platformNames) {
        this.platformNames = platformNames;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }
}
