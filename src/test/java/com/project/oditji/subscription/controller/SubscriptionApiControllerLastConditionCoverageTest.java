package com.project.oditji.subscription.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.project.oditji.search.service.SearchContentPageCacheService;
import com.project.oditji.search.vo.SearchResultPageVO;
import com.project.oditji.search.vo.SearchResultVO;
import com.project.oditji.subscription.service.SubscriptionCalculatorService;
import com.project.oditji.subscription.vo.ContentWishItemVO;

/** 구독 검색 API의 빈 결과·빈 플랫폼 반복문 조건을 보완합니다. */
@ExtendWith(MockitoExtension.class)
class SubscriptionApiControllerLastConditionCoverageTest {

    @Mock
    private SearchContentPageCacheService searchContentPageCacheService;

    @Mock
    private SubscriptionCalculatorService subscriptionCalculatorService;

    private SubscriptionApiController controller;

    @BeforeEach
    void setUp() {
        controller = new SubscriptionApiController(
                searchContentPageCacheService,
                subscriptionCalculatorService);
    }

    @Test
    void searchShouldHandleEmptyResultListWithoutEnteringResultLoop() {
        SearchResultPageVO page = new SearchResultPageVO();
        page.setResultList(Collections.emptyList());

        when(searchContentPageCacheService.getContentPage(
                eq("empty-results"),
                eq(1),
                eq(8),
                anyList(),
                anyList(),
                anyList()))
                .thenReturn(page);

        assertTrue(controller.searchContent("empty-results").isEmpty());
    }

    @Test
    void searchShouldHandleEmptyPlatformListWithoutEnteringPlatformLoop() {
        SearchResultVO content = new SearchResultVO();
        content.setTmdbId(10L);
        content.setContentType("MOVIE");
        content.setTitle("플랫폼 빈 목록");
        content.setPlatformList(Collections.emptyList());

        SearchResultPageVO page = new SearchResultPageVO();
        page.setResultList(List.of(content));

        when(searchContentPageCacheService.getContentPage(
                eq("empty-platforms"),
                eq(1),
                eq(8),
                anyList(),
                anyList(),
                anyList()))
                .thenReturn(page);

        List<ContentWishItemVO> result =
                controller.searchContent("empty-platforms");

        assertEquals(1, result.size());
        assertTrue(result.get(0).getPlatformNameList().isEmpty());
    }
}
