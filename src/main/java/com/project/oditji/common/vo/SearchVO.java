package com.project.oditji.common.vo;

public class SearchVO {

    // 검색어
    private String keyword;

    // 콘텐츠 유형 (MOVIE / TV)
    private String contentType;

    // 장르
    private String genre;

    // 정렬 조건
    // latest : 최신순
    // score : 평점순
    // view : 조회수순
    private String sort;

    // 현재 페이지
    private int page = 1;

    // 페이지당 게시물 수
    private int size = 20;

    public String getKeyword() {
        return keyword;
    }

    public String getContentType() {
        return contentType;
    }

    public String getGenre() {
        return genre;
    }

    public String getSort() {
        return sort;
    }

    public int getPage() {
        return page;
    }

    public int getSize() {
        return size;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public void setSort(String sort) {
        this.sort = sort;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public void setSize(int size) {
        this.size = size;
    }

    
}