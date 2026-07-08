package com.project.oditji.search.vo;

import java.util.ArrayList;
import java.util.List;

public class SearchVO {

    private String keyword;

    // MOVIE, TV
    private List<String> contentTypes;

    // ACTION, COMEDY, THRILLER 등
    private List<String> genreCodes;

    // TMDB OTT provider ID
    private List<String> providerIds;

    private int page;

    public SearchVO() {
        this.contentTypes = new ArrayList<String>();
        this.genreCodes = new ArrayList<String>();
        this.providerIds = new ArrayList<String>();
        this.page = 1;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public List<String> getContentTypes() {
        return contentTypes;
    }

    public void setContentTypes(List<String> contentTypes) {
        this.contentTypes = contentTypes;
    }

    public List<String> getGenreCodes() {
        return genreCodes;
    }

    public void setGenreCodes(List<String> genreCodes) {
        this.genreCodes = genreCodes;
    }

    public List<String> getProviderIds() {
        return providerIds;
    }

    public void setProviderIds(List<String> providerIds) {
        this.providerIds = providerIds;
    }

    public int getPage() {
        if (page <= 0) {
            return 1;
        }

        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public boolean hasKeyword() {
        return keyword != null && !keyword.trim().isEmpty();
    }

    public boolean hasContentTypes() {
        return contentTypes != null && !contentTypes.isEmpty();
    }

    public boolean hasGenreCodes() {
        return genreCodes != null && !genreCodes.isEmpty();
    }

    public boolean hasProviderIds() {
        return providerIds != null && !providerIds.isEmpty();
    }

    public boolean hasFilter() {
        return hasContentTypes()
                || hasGenreCodes()
                || hasProviderIds();
    }
}