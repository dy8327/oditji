package com.project.oditji.content.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.project.oditji.content.dao.ContentDAO;
import com.project.oditji.content.vo.ContentVO;
import com.project.oditji.search.service.SearchContentPageCacheService;
import com.project.oditji.search.service.SearchContentStore;
import com.project.oditji.search.vo.SearchResultVO;
import com.project.oditji.tmdb.service.TmdbService;

/** 최근 본 콘텐츠와 출시 캘린더 Service 위임 신규 로직을 검증합니다. */
@ExtendWith(MockitoExtension.class)
class ContentServiceImplReleaseRecentCoverageTest {

    @Mock
    private ContentDAO contentDAO;
    @Mock
    private TmdbService tmdbService;
    @Mock
    private SearchContentPageCacheService searchContentPageCacheService;
    @Mock
    private SearchContentStore searchContentStore;

    private ContentServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new ContentServiceImpl(
                contentDAO,
                tmdbService,
                searchContentPageCacheService,
                searchContentStore);
    }

    @Test
    void recentlyViewedShouldRejectInvalidMemberAndLimitBeforeDaoLookup() {
        assertTrue(service.getRecentlyViewedContentList(null, 5).isEmpty());
        assertTrue(service.getRecentlyViewedContentList(0L, 5).isEmpty());
        assertTrue(service.getRecentlyViewedContentList(-1L, 5).isEmpty());
        assertTrue(service.getRecentlyViewedContentList(1L, 0).isEmpty());
        assertTrue(service.getRecentlyViewedContentList(1L, -1).isEmpty());

        verify(contentDAO, never()).selectRecentViewedContentList(any());
        verify(searchContentPageCacheService, never())
                .getMainRecentlyViewedContent(any());
    }

    @Test
    void recentlyViewedShouldReturnEmptyForNullAndEmptyDaoLists() {
        when(contentDAO.selectRecentViewedContentList(any()))
                .thenReturn(null)
                .thenReturn(List.of());

        assertTrue(service.getRecentlyViewedContentList(7L, 4).isEmpty());
        assertTrue(service.getRecentlyViewedContentList(7L, 4).isEmpty());

        verify(searchContentPageCacheService, never())
                .getMainRecentlyViewedContent(any());
    }

    @Test
    void recentlyViewedShouldPassMemberAndLimitAndDelegateCacheConversion() {
        ContentVO first = new ContentVO();
        ContentVO second = new ContentVO();
        List<ContentVO> recent = List.of(first, second);
        List<SearchResultVO> expected = List.of(new SearchResultVO());
        when(contentDAO.selectRecentViewedContentList(any())).thenReturn(recent);
        when(searchContentPageCacheService.getMainRecentlyViewedContent(recent))
                .thenReturn(expected);

        List<SearchResultVO> result = service.getRecentlyViewedContentList(9L, 6);

        assertSame(expected, result);
        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String, Object>> captor =
                ArgumentCaptor.forClass(Map.class);
        verify(contentDAO).selectRecentViewedContentList(captor.capture());
        assertEquals(Long.valueOf(9L), captor.getValue().get("memberNo"));
        assertEquals(6, captor.getValue().get("limit"));
        verify(searchContentPageCacheService).getMainRecentlyViewedContent(recent);
    }

    @Test
    void releaseCalendarShouldDelegateYearAndMonthWithoutChangingValues() {
        List<SearchResultVO> expected = List.of(new SearchResultVO());
        when(searchContentPageCacheService.getReleaseCalendarContent(2026, 8))
                .thenReturn(expected);

        assertSame(expected, service.getReleaseCalendarContent(2026, 8));
    }
}
