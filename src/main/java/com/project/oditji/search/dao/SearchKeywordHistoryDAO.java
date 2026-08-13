package com.project.oditji.search.dao;

import java.util.List;
import java.util.Map;

import com.project.oditji.search.vo.SearchKeywordHistoryVO;

/**
 * 로그인 회원의 검색어 이력 관련 DB 접근 인터페이스입니다.
 */
public interface SearchKeywordHistoryDAO {

    /**
     * 로그인 회원의 검색어 이력을 저장합니다.
     *
     * 같은 회원이 이미 검색한 검색어(대소문자 무시)면
     * SEARCH_COUNT를 증가시키고 LAST_SEARCHED_AT을 갱신하며,
     * 처음 검색한 검색어면 새 행을 추가합니다.
     */
    int mergeSearchKeywordHistory(
            SearchKeywordHistoryVO historyVO);

    /**
     * 회원이 보관할 수 있는 최근 검색어 개수를 초과한
     * 오래된 검색어 이력을 삭제합니다.
     *
     * param: memberNo(Long), keepCount(int)
     */
    int deleteSearchKeywordHistoryBeyondLimit(
            Map<String, Object> param);

    /**
     * 로그인 회원의 최근 검색어를
     * 마지막 검색 시각(LAST_SEARCHED_AT) 내림차순으로 조회합니다.
     *
     * param: memberNo(Long), limit(int)
     */
    List<SearchKeywordHistoryVO> selectRecentSearchKeywordList(
            Map<String, Object> param);

    /**
     * 로그인 회원의 검색어 이력 중 하나를 삭제합니다.
     */
    int deleteSearchKeywordHistory(
            SearchKeywordHistoryVO historyVO);

    /**
     * 로그인 회원의 검색어 이력을 모두 삭제합니다.
     */
    int deleteAllSearchKeywordHistory(
            Long memberNo);
}
