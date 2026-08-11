package com.project.oditji.search.service;

import java.util.List;

/**
 * 로그인 회원의 검색어 이력 관련 비즈니스 로직 인터페이스입니다.
 */
public interface SearchKeywordHistoryService {

    /**
     * 로그인 회원의 검색어 이력을 저장합니다.
     *
     * 비로그인 사용자는 Controller에서 호출하지 않습니다.
     */
    void recordSearchKeyword(
            Long memberNo,
            String keyword);

    /**
     * 로그인 회원의 최근 검색어 목록을
     * 최근 검색 순서로 조회합니다.
     */
    List<String> getRecentSearchKeywordList(
            Long memberNo,
            int limit);

    /**
     * 로그인 회원의 검색어 이력 중 하나를 삭제합니다.
     */
    void deleteSearchKeyword(
            Long memberNo,
            String keyword);

    /**
     * 로그인 회원의 검색어 이력을 모두 삭제합니다.
     */
    void deleteAllSearchKeyword(
            Long memberNo);
}
