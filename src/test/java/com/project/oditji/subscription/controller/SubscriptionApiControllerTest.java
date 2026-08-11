package com.project.oditji.subscription.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
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
import com.project.oditji.subscription.vo.SubscriptionCalculationResultVO;
import com.project.oditji.tmdb.vo.OttPlatformVO;

/** 구독 계산기 검색·계산 API의 입력 정규화와 매핑을 검증합니다. */
@ExtendWith(MockitoExtension.class)
class SubscriptionApiControllerTest {

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
    void blankKeywordShouldReturnEmptyListWithoutQuerying() {
        List<ContentWishItemVO> result = controller.searchContent("   ");

        assertTrue(result.isEmpty());
    }

    @Test
    void nullKeywordShouldReturnEmptyListWithoutQuerying() {
        List<ContentWishItemVO> result = controller.searchContent(null);

        assertTrue(result.isEmpty());
    }

    @Test
    void searchShouldMapContentAndPlatformNamesFromPageResult() {
        SearchResultVO content = new SearchResultVO();
        content.setTmdbId(100L);
        content.setContentType("MOVIE");
        content.setTitle("테스트 영화");
        content.setPosterPath("/poster.jpg");

        OttPlatformVO netflix = new OttPlatformVO();
        netflix.setPlatformName("Netflix");
        OttPlatformVO tving = new OttPlatformVO();
        tving.setPlatformName("TVING");
        content.setPlatformList(List.of(netflix, tving));

        SearchResultPageVO page = new SearchResultPageVO();
        page.setResultList(List.of(content));

        when(searchContentPageCacheService.getContentPage(
                "인터스텔라",
                1,
                8,
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList()))
                .thenReturn(page);

        List<ContentWishItemVO> result = controller.searchContent("인터스텔라");

        assertEquals(1, result.size());
        ContentWishItemVO wishItem = result.get(0);
        assertEquals(100L, wishItem.getTmdbId());
        assertEquals("MOVIE", wishItem.getContentType());
        assertEquals("테스트 영화", wishItem.getTitle());
        assertEquals("/poster.jpg", wishItem.getPosterPath());
        assertEquals(List.of("Netflix", "TVING"), wishItem.getPlatformNameList());
    }

    @Test
    void nullPageResultShouldReturnEmptyList() {
        when(searchContentPageCacheService.getContentPage(
                "없는검색어",
                1,
                8,
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList()))
                .thenReturn(null);

        List<ContentWishItemVO> result = controller.searchContent("없는검색어");

        assertTrue(result.isEmpty());
    }

    @Test
    void calculateShouldDelegateToService() {
        List<ContentWishItemVO> wishItemList = List.of(new ContentWishItemVO());
        SubscriptionCalculationResultVO expected = new SubscriptionCalculationResultVO();
        when(subscriptionCalculatorService.calculate(wishItemList))
                .thenReturn(expected);

        SubscriptionCalculationResultVO result = controller.calculate(wishItemList);

        assertSame(expected, result);
        verify(subscriptionCalculatorService).calculate(wishItemList);
    }
}
