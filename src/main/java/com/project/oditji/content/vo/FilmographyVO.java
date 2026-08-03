package com.project.oditji.content.vo;

public class FilmographyVO {

    private Long tmdbId;
    private String contentType;
    private String title;
    private String originalTitle;
    private String posterPath;
    private String releaseDate;
    private String participationName;
    private String participationCategory;
    private Double tmdbScore;
    private Double popularity;

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

    public String getPosterPath() {
        return posterPath;
    }

    public void setPosterPath(String posterPath) {
        this.posterPath = posterPath;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(String releaseDate) {
        this.releaseDate = releaseDate;
    }

    public String getParticipationName() {
        return participationName;
    }

    public void setParticipationName(String participationName) {
        this.participationName = participationName;
    }

    public String getParticipationCategory() {
        return participationCategory;
    }

    public void setParticipationCategory(String participationCategory) {
        this.participationCategory = participationCategory;
    }

    public Double getTmdbScore() {
        return tmdbScore;
    }

    public void setTmdbScore(Double tmdbScore) {
        this.tmdbScore = tmdbScore;
    }

    public Double getPopularity() {
        return popularity;
    }

    public void setPopularity(Double popularity) {
        this.popularity = popularity;
    }
}
