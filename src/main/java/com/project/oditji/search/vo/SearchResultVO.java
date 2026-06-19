package com.project.oditji.search.vo;

import java.util.Date;

public class SearchResultVO {

    // 콘텐츠 번호
    private Long contentNo;

    // TMDB ID
    private Long tmdbId;

    // 영화 / 드라마
    private String contentType;

    // 제목
    private String title;

    // 원제
    private String originalTitle;

    // 포스터 이미지 경로
    private String posterPath;

    // 개봉일
    private Date releaseDate;

    // 장르
    private String genreText;

    // TMDB 평점
    private Double tmdbScore;

    // 조회수
    private Integer viewCount;

    public Long getContentNo() {
        return contentNo;
    }

    public Long getTmdbId() {
        return tmdbId;
    }

    public String getContentType() {
        return contentType;
    }

    public String getTitle() {
        return title;
    }

    public String getOriginalTitle() {
        return originalTitle;
    }

    public String getPosterPath() {
        return posterPath;
    }

    public Date getReleaseDate() {
        return releaseDate;
    }

    public String getGenreText() {
        return genreText;
    }

    public Double getTmdbScore() {
        return tmdbScore;
    }

    public Integer getViewCount() {
        return viewCount;
    }

    public void setContentNo(Long contentNo) {
        this.contentNo = contentNo;
    }

    public void setTmdbId(Long tmdbId) {
        this.tmdbId = tmdbId;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setOriginalTitle(String originalTitle) {
        this.originalTitle = originalTitle;
    }

    public void setPosterPath(String posterPath) {
        this.posterPath = posterPath;
    }

    public void setReleaseDate(Date releaseDate) {
        this.releaseDate = releaseDate;
    }

    public void setGenreText(String genreText) {
        this.genreText = genreText;
    }

    public void setTmdbScore(Double tmdbScore) {
        this.tmdbScore = tmdbScore;
    }

    public void setViewCount(Integer viewCount) {
        this.viewCount = viewCount;
    }

    
}