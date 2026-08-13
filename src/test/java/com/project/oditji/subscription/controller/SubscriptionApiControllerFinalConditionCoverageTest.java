package com.project.oditji.subscription.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.util.Arrays;
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
import com.project.oditji.tmdb.vo.OttPlatformVO;

/** 구독 검색 API의 null 결과 목록과 플랫폼 short-circuit 조건을 보완합니다. */
@ExtendWith(MockitoExtension.class)
class SubscriptionApiControllerFinalConditionCoverageTest {

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
    void searchShouldReturnEmptyWhenPageHasNullResultList() {
        SearchResultPageVO page = new SearchResultPageVO();
        page.setResultList(null);

        when(searchContentPageCacheService.getContentPage(
                eq("null-list"),
                eq(1),
                eq(8),
                anyList(),
                anyList(),
                anyList()))
                .thenReturn(page);

        assertTrue(controller.searchContent("null-list").isEmpty());
    }

    @Test
    void searchShouldCoverNullPlatformListNullPlatformAndNullPlatformName() {
        SearchResultVO withoutPlatforms = new SearchResultVO();
        withoutPlatforms.setTmdbId(1L);
        withoutPlatforms.setContentType("MOVIE");
        withoutPlatforms.setTitle("플랫폼 없음");
        withoutPlatforms.setPlatformList(null);

        OttPlatformVO unnamed = new OttPlatformVO();
        unnamed.setPlatformName(null);

        OttPlatformVO netflix = new OttPlatformVO();
        netflix.setPlatformName("Netflix");

        SearchResultVO mixedPlatforms = new SearchResultVO();
        mixedPlatforms.setTmdbId(2L);
        mixedPlatforms.setContentType("TV");
        mixedPlatforms.setTitle("혼합 플랫폼");
        mixedPlatforms.setPlatformList(Arrays.asList(null, unnamed, netflix));

        SearchResultPageVO page = new SearchResultPageVO();
        page.setResultList(List.of(withoutPlatforms, mixedPlatforms));

        when(searchContentPageCacheService.getContentPage(
                eq("platform-branches"),
                eq(1),
                eq(8),
                anyList(),
                anyList(),
                anyList()))
                .thenReturn(page);

        List<ContentWishItemVO> result = controller.searchContent("platform-branches");

        assertEquals(2, result.size());
        assertTrue(result.get(0).getPlatformNameList().isEmpty());
        assertEquals(List.of("Netflix"), result.get(1).getPlatformNameList());
    }
}
