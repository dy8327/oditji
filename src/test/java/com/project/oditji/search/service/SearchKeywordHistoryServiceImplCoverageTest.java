package com.project.oditji.search.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.project.oditji.search.dao.SearchKeywordHistoryDAO;
import com.project.oditji.search.vo.SearchKeywordHistoryVO;

/** 최근 검색어 서비스의 입력 검증, 정규화, 조회 및 삭제 분기를 검증합니다. */
class SearchKeywordHistoryServiceImplCoverageTest {

    private SearchKeywordHistoryDAO searchKeywordHistoryDAO;
    private SearchKeywordHistoryServiceImpl service;

    @BeforeEach
    void setUp() {
        searchKeywordHistoryDAO = mock(SearchKeywordHistoryDAO.class);
        service = new SearchKeywordHistoryServiceImpl(searchKeywordHistoryDAO);
    }

    @Test
    void recordShouldIgnoreInvalidMemberAndKeywordValues() {
        service.recordSearchKeyword(null, "keyword");
        service.recordSearchKeyword(0L, "keyword");
        service.recordSearchKeyword(-1L, "keyword");
        service.recordSearchKeyword(1L, null);
        service.recordSearchKeyword(1L, "   ");

        verifyNoInteractions(searchKeywordHistoryDAO);
    }

    @Test
    void recordShouldTrimKeywordAndDeleteRowsBeyondKeepLimit() {
        service.recordSearchKeyword(7L, "  넷플릭스  ");

        ArgumentCaptor<SearchKeywordHistoryVO> historyCaptor =
                ArgumentCaptor.forClass(SearchKeywordHistoryVO.class);
        verify(searchKeywordHistoryDAO)
                .mergeSearchKeywordHistory(historyCaptor.capture());

        SearchKeywordHistoryVO history = historyCaptor.getValue();
        assertEquals(7L, history.getMemberNo());
        assertEquals("넷플릭스", history.getKeyword());

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String, Object>> mapCaptor =
                ArgumentCaptor.forClass(Map.class);
        verify(searchKeywordHistoryDAO)
                .deleteSearchKeywordHistoryBeyondLimit(mapCaptor.capture());
        assertEquals(7L, mapCaptor.getValue().get("memberNo"));
        assertEquals(30, mapCaptor.getValue().get("keepCount"));
    }

    @Test
    void recordShouldTruncateKeywordToOneHundredCharacters() {
        String longKeyword = "a".repeat(120);

        service.recordSearchKeyword(8L, longKeyword);

        ArgumentCaptor<SearchKeywordHistoryVO> captor =
                ArgumentCaptor.forClass(SearchKeywordHistoryVO.class);
        verify(searchKeywordHistoryDAO)
                .mergeSearchKeywordHistory(captor.capture());
        assertEquals(100, captor.getValue().getKeyword().length());
        assertEquals("a".repeat(100), captor.getValue().getKeyword());
    }

    @Test
    void recentListShouldReturnEmptyForInvalidArguments() {
        assertTrue(service.getRecentSearchKeywordList(null, 10).isEmpty());
        assertTrue(service.getRecentSearchKeywordList(0L, 10).isEmpty());
        assertTrue(service.getRecentSearchKeywordList(-1L, 10).isEmpty());
        assertTrue(service.getRecentSearchKeywordList(1L, 0).isEmpty());
        assertTrue(service.getRecentSearchKeywordList(1L, -1).isEmpty());

        verifyNoInteractions(searchKeywordHistoryDAO);
    }

    @Test
    void recentListShouldReturnEmptyWhenDaoReturnsNullOrEmptyList() {
        when(searchKeywordHistoryDAO.selectRecentSearchKeywordList(any()))
                .thenReturn(null)
                .thenReturn(List.of());

        assertTrue(service.getRecentSearchKeywordList(3L, 4).isEmpty());
        assertTrue(service.getRecentSearchKeywordList(3L, 4).isEmpty());
    }

    @Test
    void recentListShouldMapHistoryObjectsToKeywords() {
        SearchKeywordHistoryVO first = history("넷플릭스");
        SearchKeywordHistoryVO second = history("티빙");
        List<SearchKeywordHistoryVO> historyList = List.of(first, second);
        when(searchKeywordHistoryDAO.selectRecentSearchKeywordList(any()))
                .thenReturn(historyList);

        List<String> result =
                service.getRecentSearchKeywordList(5L, 2);

        assertEquals(List.of("넷플릭스", "티빙"), result);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String, Object>> captor =
                ArgumentCaptor.forClass(Map.class);
        verify(searchKeywordHistoryDAO)
                .selectRecentSearchKeywordList(captor.capture());
        assertEquals(5L, captor.getValue().get("memberNo"));
        assertEquals(2, captor.getValue().get("limit"));
    }

    @Test
    void deleteOneShouldIgnoreInvalidInputs() {
        service.deleteSearchKeyword(null, "keyword");
        service.deleteSearchKeyword(0L, "keyword");
        service.deleteSearchKeyword(-1L, "keyword");
        service.deleteSearchKeyword(1L, null);
        service.deleteSearchKeyword(1L, "   ");

        verify(searchKeywordHistoryDAO, never())
                .deleteSearchKeywordHistory(any());
    }

    @Test
    void deleteOneShouldNormalizeAndDelegate() {
        service.deleteSearchKeyword(12L, "  검색어  ");

        ArgumentCaptor<SearchKeywordHistoryVO> captor =
                ArgumentCaptor.forClass(SearchKeywordHistoryVO.class);
        verify(searchKeywordHistoryDAO)
                .deleteSearchKeywordHistory(captor.capture());
        assertEquals(12L, captor.getValue().getMemberNo());
        assertEquals("검색어", captor.getValue().getKeyword());
    }

    @Test
    void deleteAllShouldIgnoreInvalidMemberAndDelegateForValidMember() {
        service.deleteAllSearchKeyword(null);
        service.deleteAllSearchKeyword(0L);
        service.deleteAllSearchKeyword(-1L);
        verify(searchKeywordHistoryDAO, never())
                .deleteAllSearchKeywordHistory(any());

        service.deleteAllSearchKeyword(15L);

        verify(searchKeywordHistoryDAO)
                .deleteAllSearchKeywordHistory(15L);
    }

    private SearchKeywordHistoryVO history(String keyword) {
        SearchKeywordHistoryVO history = new SearchKeywordHistoryVO();
        history.setKeyword(keyword);
        return history;
    }
}
