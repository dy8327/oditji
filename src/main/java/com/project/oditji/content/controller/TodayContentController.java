package com.project.oditji.content.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.project.oditji.search.service.SearchContentPageCacheService;
import com.project.oditji.search.vo.SearchResultVO;

@Controller
public class TodayContentController {

    /*
     * 오늘의 콘텐츠 더보기 화면에 표시할 최대 개수입니다.
     *
     * SearchContentPageCacheService의 기준:
     * 1. 최근 30일 공개 콘텐츠
     * 2. 부족하면 최근 90일 콘텐츠
     * 3. 그래도 부족하면 전체 인기 콘텐츠
     */
    private static final int TODAY_CONTENT_LIMIT = 100;

    private final SearchContentPageCacheService
            searchContentPageCacheService;

    public TodayContentController(
            SearchContentPageCacheService
                    searchContentPageCacheService) {

        this.searchContentPageCacheService =
                searchContentPageCacheService;
    }

    @GetMapping("/content/today")
    public String todayContent(
            Model model) {

        /*
         * TMDB API를 직접 호출하지 않고
         * JSONL에서 서버 공용 메모리로 적재된 콘텐츠를 조회합니다.
         */
        List<SearchResultVO> todayContentList =
                searchContentPageCacheService
                        .getMainTodayContent(
                                TODAY_CONTENT_LIMIT
                        );

        model.addAttribute(
                "todayContentList",
                todayContentList
        );

        model.addAttribute(
                "contentCount",
                todayContentList == null
                        ? 0
                        : todayContentList.size()
        );

        return "content/todayContent";
    }
}