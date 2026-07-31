package com.project.oditji.search.vo;

import java.util.ArrayList;
import java.util.List;

/**
 * 검색용 공용 메모리와 JSONL 스냅샷에서 사용하는 콘텐츠 VO입니다.
 *
 * 검색 결과와 상세 진입 시 필요한 핵심 TMDB 정보를 보관합니다.
 */
public class CachedContentVO {

    private Long tmdbId;
    private String contentType;
    private String title;
    private String originalTitle;
    private String posterPath;
    private String releaseDate;

    /**
     * TV 콘텐츠의 가장 최근 방영 회차 공개일입니다.
     * 영화 콘텐츠에서는 null을 유지합니다.
     */
    private String lastAirDate;

    private String genreText;
    private String ageRating;

    /**
     * 최초 상세 보강 이후 "등급 정보 없음" 콘텐츠만 대상으로 수행한
     * 추가 등급 재조회 횟수입니다.
     *
     * 기존 JSONL에는 이 값이 없으므로 null 또는 0은 아직 추가 재조회를
     * 수행하지 않은 상태로 처리합니다.
     */
    private Integer ageRatingRetryCount;

    /**
     * TMDB 등급 정보를 마지막으로 확인한 시각입니다.
     * ISO-8601 문자열로 저장하여 기존 JSONL과의 호환성을 유지합니다.
     */
    private String ageRatingLastCheckedAt;

    private Double tmdbScore;
    private Double popularity;

    private Integer episodeCount;
    private Integer runtime;
    private String director;
    private String castNames;

    private List<String> platformKeys;
    private String searchText;

    public CachedContentVO() {
        this.platformKeys = new ArrayList<String>();
    }

    /**
     * 콘텐츠 유형과 TMDB ID를 조합하여 공용 저장소 키를 만듭니다.
     */
    public String createContentKey() {

        if (tmdbId == null || contentType == null) {
            return "";
        }

        return contentType.trim().toUpperCase()
                + "_"
                + tmdbId;
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

    public String getLastAirDate() {
        return lastAirDate;
    }

    public void setLastAirDate(String lastAirDate) {
        this.lastAirDate = lastAirDate;
    }

    public String getGenreText() {
        return genreText;
    }

    public void setGenreText(String genreText) {
        this.genreText = genreText;
    }

    public String getAgeRating() {
        return ageRating;
    }

    public void setAgeRating(String ageRating) {
        this.ageRating = ageRating;
    }

    public Integer getAgeRatingRetryCount() {
        return ageRatingRetryCount;
    }

    public void setAgeRatingRetryCount(Integer ageRatingRetryCount) {
        this.ageRatingRetryCount = ageRatingRetryCount;
    }

    public String getAgeRatingLastCheckedAt() {
        return ageRatingLastCheckedAt;
    }

    public void setAgeRatingLastCheckedAt(String ageRatingLastCheckedAt) {
        this.ageRatingLastCheckedAt = ageRatingLastCheckedAt;
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

    public List<String> getPlatformKeys() {
        return platformKeys;
    }

    public void setPlatformKeys(List<String> platformKeys) {

        this.platformKeys = platformKeys == null
                ? new ArrayList<String>()
                : new ArrayList<String>(platformKeys);
    }

    public String getSearchText() {
        return searchText;
    }

    public void setSearchText(String searchText) {
        this.searchText = searchText;
    }

    public Integer getRuntime() {
        return runtime;
    }

    public void setRuntime(Integer runtime) {
        this.runtime = runtime;
    }
}
