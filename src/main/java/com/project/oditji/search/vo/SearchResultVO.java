package com.project.oditji.search.vo;

import java.util.ArrayList;
import java.util.List;

import com.project.oditji.tmdb.vo.OttPlatformVO;

public class SearchResultVO {

    // DB 콘텐츠 번호
    private Long contentNo;

    // TMDB 콘텐츠 ID
    private Long tmdbId;

    // MOVIE / TV
    private String contentType;

    private String title;
    private String originalTitle;
    private String overview;
    private String posterPath;
    private String backdropPath;

    // 영화 개봉일 또는 TV 첫 방영일
    private String releaseDate;

    private String genreText;
    private Double tmdbScore;
    private Integer viewCount;

    // TMDB 인기 점수
    // RankingServiceImpl, TmdbServiceImpl에서 사용
    private Double popularity;

    // TMDB 제공처와 DB OTT_PLATFORM을 매칭한 플랫폼 정보
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
        if (title == null || title.trim().isEmpty()) {
            return "제목 없음";
        }

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

    public String getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(String releaseDate) {
        this.releaseDate = releaseDate;
    }

    public String getGenreText() {
        return genreText;
    }

    public void setGenreText(String genreText) {
        this.genreText = genreText;
    }

    public Double getTmdbScore() {
        return tmdbScore;
    }

    public void setTmdbScore(Double tmdbScore) {
        this.tmdbScore = tmdbScore;
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

    public List<OttPlatformVO> getPlatformList() {
        return platformList;
    }

    public void setPlatformList(List<OttPlatformVO> platformList) {
        if (platformList == null) {
            this.platformList = new ArrayList<OttPlatformVO>();
            return;
        }

        this.platformList = platformList;
    }
}