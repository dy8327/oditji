package com.project.oditji.search.vo;

/**
 * 최근 검색어 삭제 API의 공통 응답입니다.
 */
public class SearchKeywordResponseVO {

    private final boolean success;
    private final String message;

    public SearchKeywordResponseVO(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }
}
