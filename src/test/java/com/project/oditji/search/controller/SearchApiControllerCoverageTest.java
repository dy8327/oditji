package com.project.oditji.search.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.project.oditji.member.vo.MemberVO;
import com.project.oditji.search.service.SearchContentPageCacheService;
import com.project.oditji.search.service.SearchKeywordHistoryService;
import com.project.oditji.search.vo.SearchKeywordResponseVO;
import com.project.oditji.search.vo.SearchResultVO;

import jakarta.servlet.http.HttpSession;

/** 검색 자동완성 및 최근 검색어 REST API의 로그인/비로그인 분기를 검증합니다. */
class SearchApiControllerCoverageTest {

    private SearchContentPageCacheService searchContentPageCacheService;
    private SearchKeywordHistoryService searchKeywordHistoryService;
    private SearchApiController controller;
    private HttpSession session;

    @BeforeEach
    void setUp() {
        searchContentPageCacheService = mock(SearchContentPageCacheService.class);
        searchKeywordHistoryService = mock(SearchKeywordHistoryService.class);
        controller = new SearchApiController(
                searchContentPageCacheService,
                searchKeywordHistoryService);
        session = mock(HttpSession.class);
    }

    @Test
    void autocompleteShouldDelegateWithPreviewLimit() {
        SearchResultVO result = new SearchResultVO();
        List<SearchResultVO> expected = List.of(result);
        when(searchContentPageCacheService.getAutocompletePreview("오징어", 6))
                .thenReturn(expected);

        List<SearchResultVO> actual = controller.autocomplete("오징어");

        assertSame(expected, actual);
        verify(searchContentPageCacheService)
                .getAutocompletePreview("오징어", 6);
    }

    @Test
    void recentKeywordsShouldPassNullForAnonymousSession() {
        when(session.getAttribute("loginMember")).thenReturn(null);
        List<String> expected = List.of("검색어");
        when(searchKeywordHistoryService.getRecentSearchKeywordList(null, 10))
                .thenReturn(expected);

        List<String> actual = controller.recentKeywords(session);

        assertSame(expected, actual);
        verify(searchKeywordHistoryService)
                .getRecentSearchKeywordList(null, 10);
    }

    @Test
    void recentKeywordsShouldUseLoggedInMemberNumber() {
        loginAs(7L);
        List<String> expected = List.of("넷플릭스", "티빙");
        when(searchKeywordHistoryService.getRecentSearchKeywordList(7L, 10))
                .thenReturn(expected);

        List<String> actual = controller.recentKeywords(session);

        assertSame(expected, actual);
        verify(searchKeywordHistoryService)
                .getRecentSearchKeywordList(7L, 10);
    }

    @Test
    void deleteRecentKeywordShouldRejectAnonymousUser() {
        when(session.getAttribute("loginMember")).thenReturn(null);

        SearchKeywordResponseVO response =
                controller.deleteRecentKeyword("검색어", session);

        assertFalse(response.isSuccess());
        assertEquals("로그인 후 이용할 수 있습니다.", response.getMessage());
        verifyNoInteractions(searchKeywordHistoryService);
    }

    @Test
    void deleteRecentKeywordShouldDeleteForLoggedInUser() {
        loginAs(11L);

        SearchKeywordResponseVO response =
                controller.deleteRecentKeyword("  최근 검색어  ", session);

        assertTrue(response.isSuccess());
        assertEquals("최근 검색어를 삭제했습니다.", response.getMessage());
        verify(searchKeywordHistoryService)
                .deleteSearchKeyword(11L, "  최근 검색어  ");
    }

    @Test
    void deleteAllRecentKeywordsShouldRejectAnonymousUser() {
        when(session.getAttribute("loginMember")).thenReturn("not-a-member");

        SearchKeywordResponseVO response =
                controller.deleteAllRecentKeywords(session);

        assertFalse(response.isSuccess());
        assertEquals("로그인 후 이용할 수 있습니다.", response.getMessage());
        verifyNoInteractions(searchKeywordHistoryService);
    }

    @Test
    void deleteAllRecentKeywordsShouldDeleteForLoggedInUser() {
        loginAs(21L);

        SearchKeywordResponseVO response =
                controller.deleteAllRecentKeywords(session);

        assertTrue(response.isSuccess());
        assertEquals("최근 검색어를 모두 삭제했습니다.", response.getMessage());
        verify(searchKeywordHistoryService)
                .deleteAllSearchKeyword(21L);
    }

    private void loginAs(Long memberNo) {
        MemberVO member = new MemberVO();
        member.setMemberNo(memberNo);
        when(session.getAttribute("loginMember")).thenReturn(member);
    }
}
