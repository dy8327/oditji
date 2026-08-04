package com.project.oditji.search.vo;

import java.util.ArrayList;
import java.util.List;

import com.project.oditji.common.vo.ContentMetadataVO;

/**
 * 검색용 공용 메모리와 JSONL 스냅샷에서 사용하는 콘텐츠 VO입니다.
 */
public class CachedContentVO extends ContentMetadataVO {

    private String releaseDate;
    private String lastAirDate;
    private Integer ageRatingRetryCount;
    private String ageRatingLastCheckedAt;
    private Boolean ageRatingRestrictionChecked;
    private Double popularity;
    private List<String> platformKeys;
    private String searchText;

    public CachedContentVO() {
        this.platformKeys = new ArrayList<String>();
    }

    public String createContentKey() {
        if (getTmdbId() == null || getContentType() == null) {
            return "";
        }

        return getContentType().trim().toUpperCase()
                + "_"
                + getTmdbId();
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

    public Boolean getAgeRatingRestrictionChecked() {
        return ageRatingRestrictionChecked;
    }

    public void setAgeRatingRestrictionChecked(Boolean ageRatingRestrictionChecked) {
        this.ageRatingRestrictionChecked = ageRatingRestrictionChecked;
    }

    public Double getPopularity() {
        return popularity;
    }

    public void setPopularity(Double popularity) {
        this.popularity = popularity;
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
}
