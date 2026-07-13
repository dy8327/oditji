package com.project.oditji.content.vo;

import java.util.ArrayList;
import java.util.List;

import com.project.oditji.search.vo.SearchResultVO;

public class ContentListPageVO {

    private List<SearchResultVO> contentList;
    private int currentPage;
    private int totalPages;
    private int totalResults;

    public ContentListPageVO() {
        this.contentList = new ArrayList<SearchResultVO>();
        this.currentPage = 1;
        this.totalPages = 0;
        this.totalResults = 0;
    }

    public List<SearchResultVO> getContentList() {
        return contentList;
    }

    public void setContentList(List<SearchResultVO> contentList) {
        this.contentList = contentList == null
                ? new ArrayList<SearchResultVO>()
                : contentList;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
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

    public boolean isHasPrevious() {
        return currentPage > 1;
    }

    public boolean isHasNext() {
        return totalPages > 0 && currentPage < totalPages;
    }
}
