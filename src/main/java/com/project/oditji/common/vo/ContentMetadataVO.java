package com.project.oditji.common.vo;

/**
 * TMDB 조회 결과, 콘텐츠 도메인, 관리자 콘텐츠 조회에서 공통으로 사용하는
 * 콘텐츠 기본 메타데이터입니다.
 *
 * 공개일 타입은 화면과 저장 용도에 따라 LocalDate 또는 Date가 사용되므로
 * 각 하위 VO에서 별도로 정의합니다.
 */
public abstract class ContentMetadataVO {

    private Long tmdbId;
    private String contentType;
    private String title;
    private String originalTitle;
    private String overview;
    private String posterPath;
    private String backdropPath;
    private String genreText;
    private Integer runtime;
    private Integer episodeCount;
    private String director;
    private String castNames;
    private String ageRating;
    private Double tmdbScore;

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
}
