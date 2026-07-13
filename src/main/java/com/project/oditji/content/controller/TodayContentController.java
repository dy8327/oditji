package com.project.oditji.content.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.project.oditji.common.service.MainContentPlatformService;
import com.project.oditji.search.vo.SearchResultVO;
import com.project.oditji.tmdb.service.TmdbService;

@Controller
public class TodayContentController {

    private final TmdbService tmdbService;
    private final MainContentPlatformService mainContentPlatformService;

    public TodayContentController(
            TmdbService tmdbService,
            MainContentPlatformService mainContentPlatformService) {

        this.tmdbService = tmdbService;
        this.mainContentPlatformService = mainContentPlatformService;
    }

    @GetMapping("/content/today")
    public String todayContent(Model model) {

        List<SearchResultVO> todayContentList =
                tmdbService.getMainTodayContent();

        mainContentPlatformService.attachPlatformLogos(
                todayContentList,
                new ArrayList<String>()
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
