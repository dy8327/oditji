package com.project.oditji.search.vo;

import java.util.ArrayList;
import java.util.List;

public class SearchVO {

    private String keyword;
    private List<String> contentCategories;
    private List<String> genreCodes;
    private List<String> providerIds;
    private String searchTab;
    private int contentPage;
    private int goodsPage;

    public SearchVO() {
        this.contentCategories = new ArrayList<String>();
        this.genreCodes = new ArrayList<String>();
        this.providerIds = new ArrayList<String>();
        this.searchTab = "ALL";
        this.contentPage = 1;
        this.goodsPage = 1;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public List<String> getContentCategories() {
        return contentCategories;
    }

    public void setContentCategories(List<String> contentCategories) {
        this.contentCategories = contentCategories == null
                ? new ArrayList<String>()
                : contentCategories;
    }

    public List<String> getGenreCodes() {
        return genreCodes;
    }

    public void setGenreCodes(List<String> genreCodes) {
        this.genreCodes = genreCodes == null
                ? new ArrayList<String>()
                : genreCodes;
    }

    public List<String> getProviderIds() {
        return providerIds;
    }

    public void setProviderIds(List<String> providerIds) {
        this.providerIds = providerIds == null
                ? new ArrayList<String>()
                : providerIds;
    }

    public String getSearchTab() {
        return searchTab;
    }

    public void setSearchTab(String searchTab) {
        this.searchTab = searchTab;
    }

    public int getContentPage() {
        return contentPage <= 0 ? 1 : contentPage;
    }

    public void setContentPage(int contentPage) {
        this.contentPage = contentPage;
    }

    public int getGoodsPage() {
        return goodsPage <= 0 ? 1 : goodsPage;
    }

    public void setGoodsPage(int goodsPage) {
        this.goodsPage = goodsPage;
    }

    // 기존 page 파라미터 호환용
    public int getPage() {
        return getContentPage();
    }

    public void setPage(int page) {
        this.contentPage = page;
    }

    public boolean hasKeyword() {
        return keyword != null && !keyword.trim().isEmpty();
    }

    public boolean hasContentCategories() {
        return contentCategories != null && !contentCategories.isEmpty();
    }

    public boolean hasGenreCodes() {
        return genreCodes != null && !genreCodes.isEmpty();
    }

    public boolean hasProviderIds() {
        return providerIds != null && !providerIds.isEmpty();
    }

    public boolean hasFilter() {
        return hasContentCategories()
                || hasGenreCodes()
                || hasProviderIds();
    }
}
