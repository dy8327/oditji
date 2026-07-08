package com.project.oditji.search.vo;

import java.util.ArrayList;
import java.util.List;

public class SearchResultPageVO {

    private List<SearchResultVO> resultList;

    private int page;
    private int totalPages;
    private int totalResults;

    public SearchResultPageVO() {
        this.resultList = new ArrayList<SearchResultVO>();
        this.page = 1;
        this.totalPages = 0;
        this.totalResults = 0;
    }

    public List<SearchResultVO> getResultList() {
        return resultList;
    }

    public void setResultList(List<SearchResultVO> resultList) {
        if (resultList == null) {
            this.resultList = new ArrayList<SearchResultVO>();
        } else {
            this.resultList = resultList;
        }
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    public int getTotalResults() {
        return totalResults;
    }

    public void setTotalResults(int totalResults) {
        this.totalResults = totalResults;
    }
}