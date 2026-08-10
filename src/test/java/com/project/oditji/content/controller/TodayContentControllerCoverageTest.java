package com.project.oditji.content.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.ExtendedModelMap;

import com.project.oditji.search.service.SearchContentPageCacheService;
import com.project.oditji.search.vo.SearchResultVO;

/**
 * 오늘의 콘텐츠 화면의 정상 목록과 null 목록 분기를 검증합니다.
 */
@ExtendWith(MockitoExtension.class)
class TodayContentControllerCoverageTest {

    @Mock
    private SearchContentPageCacheService searchContentPageCacheService;

    private TodayContentController controller;

    @BeforeEach
    void setUp() {
        controller = new TodayContentController(searchContentPageCacheService);
    }

    @Test
    void todayContentShouldExposeListAndCount() {
        SearchResultVO first = new SearchResultVO();
        SearchResultVO second = new SearchResultVO();
        List<SearchResultVO> contents = List.of(first, second);

        when(searchContentPageCacheService.getMainTodayContent(100))
                .thenReturn(contents);

        ExtendedModelMap model = new ExtendedModelMap();

        assertEquals(
                "content/todayContent",
                controller.todayContent(model));

        assertSame(contents, model.get("todayContentList"));
        assertEquals(2, model.get("contentCount"));
        verify(searchContentPageCacheService).getMainTodayContent(100);
    }

    @Test
    void todayContentShouldExposeZeroCountForNullList() {
        when(searchContentPageCacheService.getMainTodayContent(100))
                .thenReturn(null);

        ExtendedModelMap model = new ExtendedModelMap();

        assertEquals(
                "content/todayContent",
                controller.todayContent(model));

        assertNull(model.get("todayContentList"));
        assertEquals(0, model.get("contentCount"));
    }
}
