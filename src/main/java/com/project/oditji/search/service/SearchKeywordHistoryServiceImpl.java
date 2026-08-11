package com.project.oditji.search.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.project.oditji.search.dao.SearchKeywordHistoryDAO;
import com.project.oditji.search.vo.SearchKeywordHistoryVO;

/**
 * 로그인 회원의 검색어 이력 관련 비즈니스 로직 구현체입니다.
 *
 * 회원 1인당 최근 검색어를 최대 MAX_KEEP_COUNT건까지만 유지하여
 * SEARCH_KEYWORD_HISTORY가 무한정 쌓이지 않게 합니다.
 */
@Service
public class SearchKeywordHistoryServiceImpl implements SearchKeywordHistoryService {

    /**
     * 검색어 미리보기 드롭다운에 노출할 최대 개수보다 여유 있게 보관합니다.
     */
    private static final int MAX_KEEP_COUNT = 30;

    private static final int MAX_KEYWORD_LENGTH = 100;

    private final SearchKeywordHistoryDAO searchKeywordHistoryDAO;

    public SearchKeywordHistoryServiceImpl(
            SearchKeywordHistoryDAO searchKeywordHistoryDAO) {

        this.searchKeywordHistoryDAO = searchKeywordHistoryDAO;
    }

    @Override
    @Transactional
    public void recordSearchKeyword(
            Long memberNo,
            String keyword) {

        String normalizedKeyword =
                normalizeKeyword(keyword);

        if (memberNo == null
                || memberNo <= 0
                || normalizedKeyword.isEmpty()) {

            return;
        }

        SearchKeywordHistoryVO historyVO =
                new SearchKeywordHistoryVO();

        historyVO.setMemberNo(memberNo);
        historyVO.setKeyword(normalizedKeyword);

        searchKeywordHistoryDAO.mergeSearchKeywordHistory(
                historyVO
        );

        Map<String, Object> param =
                new HashMap<String, Object>();

        param.put("memberNo", memberNo);
        param.put("keepCount", MAX_KEEP_COUNT);

        searchKeywordHistoryDAO
                .deleteSearchKeywordHistoryBeyondLimit(
                        param
                );
    }

    @Override
    public List<String> getRecentSearchKeywordList(
            Long memberNo,
            int limit) {

        if (memberNo == null
                || memberNo <= 0
                || limit <= 0) {

            return Collections.emptyList();
        }

        Map<String, Object> param =
                new HashMap<String, Object>();

        param.put("memberNo", memberNo);
        param.put("limit", limit);

        List<SearchKeywordHistoryVO> historyList =
                searchKeywordHistoryDAO
                        .selectRecentSearchKeywordList(
                                param
                        );

        if (historyList == null
                || historyList.isEmpty()) {

            return Collections.emptyList();
        }

        List<String> keywordList =
                new ArrayList<String>();

        for (SearchKeywordHistoryVO historyVO
                : historyList) {

            keywordList.add(
                    historyVO.getKeyword()
            );
        }

        return keywordList;
    }

    @Override
    @Transactional
    public void deleteSearchKeyword(
            Long memberNo,
            String keyword) {

        String normalizedKeyword =
                normalizeKeyword(keyword);

        if (memberNo == null
                || memberNo <= 0
                || normalizedKeyword.isEmpty()) {

            return;
        }

        SearchKeywordHistoryVO historyVO =
                new SearchKeywordHistoryVO();

        historyVO.setMemberNo(memberNo);
        historyVO.setKeyword(normalizedKeyword);

        searchKeywordHistoryDAO.deleteSearchKeywordHistory(
                historyVO
        );
    }

    @Override
    @Transactional
    public void deleteAllSearchKeyword(
            Long memberNo) {

        if (memberNo == null
                || memberNo <= 0) {

            return;
        }

        searchKeywordHistoryDAO.deleteAllSearchKeywordHistory(
                memberNo
        );
    }

    /**
     * 검색어 앞뒤 공백을 제거하고 지나치게 긴 검색어는 잘라냅니다.
     */
    private String normalizeKeyword(
            String keyword) {

        if (keyword == null) {
            return "";
        }

        String trimmed =
                keyword.trim();

        if (trimmed.length() > MAX_KEYWORD_LENGTH) {

            trimmed =
                    trimmed.substring(
                            0,
                            MAX_KEYWORD_LENGTH
                    );
        }

        return trimmed;
    }
}
