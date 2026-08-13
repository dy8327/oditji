package com.project.oditji.search.vo;

import java.time.LocalDateTime;

/**
 * 로그인 회원의 검색어 이력을 담는 VO입니다.
 *
 * SEARCH_KEYWORD_HISTORY 테이블은
 * 회원 + 검색어별로 한 행만 유지합니다.
 *
 * 같은 회원이 같은 검색어로 다시 검색하면
 * 새로운 행을 추가하지 않고 SEARCH_COUNT를 증가시키고
 * LAST_SEARCHED_AT을 갱신합니다.
 */
public class SearchKeywordHistoryVO {

    private Long searchHistoryNo;
    private Long memberNo;
    private String keyword;
    private int searchCount;
    private LocalDateTime firstSearchedAt;
    private LocalDateTime lastSearchedAt;

    public Long getSearchHistoryNo() {
        return searchHistoryNo;
    }

    public void setSearchHistoryNo(
            Long searchHistoryNo) {

        this.searchHistoryNo = searchHistoryNo;
    }

    public Long getMemberNo() {
        return memberNo;
    }

    public void setMemberNo(
            Long memberNo) {

        this.memberNo = memberNo;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(
            String keyword) {

        this.keyword = keyword;
    }

    public int getSearchCount() {
        return searchCount;
    }

    public void setSearchCount(
            int searchCount) {

        this.searchCount = searchCount;
    }

    public LocalDateTime getFirstSearchedAt() {
        return firstSearchedAt;
    }

    public void setFirstSearchedAt(
            LocalDateTime firstSearchedAt) {

        this.firstSearchedAt = firstSearchedAt;
    }

    public LocalDateTime getLastSearchedAt() {
        return lastSearchedAt;
    }

    public void setLastSearchedAt(
            LocalDateTime lastSearchedAt) {

        this.lastSearchedAt = lastSearchedAt;
    }
}
