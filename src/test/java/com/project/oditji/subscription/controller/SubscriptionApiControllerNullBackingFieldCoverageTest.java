package com.project.oditji.subscription.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.project.oditji.search.service.SearchContentPageCacheService;
import com.project.oditji.search.vo.SearchResultPageVO;
import com.project.oditji.search.vo.SearchResultVO;
import com.project.oditji.subscription.service.SubscriptionCalculatorService;
import com.project.oditji.subscription.vo.ContentWishItemVO;

/**
 * VO setter가 null을 빈 목록으로 치환해서 일반 setter 호출로는 도달하지 않는
 * 구독 검색 컨트롤러의 null 방어 분기를 직접 검증합니다.
 */
class SubscriptionApiControllerNullBackingFieldCoverageTest {

    private SearchContentPageCacheService searchContentPageCacheService;
    private SubscriptionApiController controller;

    @BeforeEach
    void setUp() {
        searchContentPageCacheService = mock(SearchContentPageCacheService.class);
        controller = new SubscriptionApiController(
                searchContentPageCacheService,
                mock(SubscriptionCalculatorService.class));
    }

    @Test
    void searchShouldHandleNullResultListBackingField() {
        SearchResultPageVO page = new SearchResultPageVO();

        /*
         * SearchResultPageVO#setResultList(null)은 빈 ArrayList로 바꾸므로
         * 실제 null 방어 코드 자체를 검증하기 위해 backing field를 직접 설정합니다.
         */
        ReflectionTestUtils.setField(page, "resultList", null);

        when(searchContentPageCacheService.getContentPage(
                eq("null-result-backing"),
                eq(1),
                eq(8),
                anyList(),
                anyList(),
                anyList()))
                .thenReturn(page);

        assertTrue(controller.searchContent("null-result-backing").isEmpty());
    }

    @Test
    void searchShouldHandleNullPlatformListBackingField() {
        SearchResultVO content = new SearchResultVO();
        content.setTmdbId(10L);
        content.setContentType("MOVIE");
        content.setTitle("플랫폼 null backing field");

        /* SearchResultVO#setPlatformList(null) 역시 빈 목록으로 정규화됩니다. */
        ReflectionTestUtils.setField(content, "platformList", null);

        SearchResultPageVO page = new SearchResultPageVO();
        page.setResultList(List.of(content));

        when(searchContentPageCacheService.getContentPage(
                eq("null-platform-backing"),
                eq(1),
                eq(8),
                anyList(),
                anyList(),
                anyList()))
                .thenReturn(page);

        List<ContentWishItemVO> result =
                controller.searchContent("null-platform-backing");

        assertEquals(1, result.size());
        assertTrue(result.get(0).getPlatformNameList().isEmpty());
    }
}
