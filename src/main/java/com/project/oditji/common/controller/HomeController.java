package com.project.oditji.common.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.project.oditji.search.vo.SearchResultVO;
import com.project.oditji.tmdb.service.TmdbService;

// 메인 페이지 진입
@Controller
public class HomeController {

    private final TmdbService tmdbService;

    public HomeController(TmdbService tmdbService) {
        this.tmdbService = tmdbService;
    }

    /**
     * =========================
     * 메인 페이지
     * =========================
     */
    @GetMapping("/")
    public String main(Model model) {

        List<SearchResultVO> popularContentList =
                tmdbService.getMainPopularContent();

        List<SearchResultVO> todayContentList =
                tmdbService.getMainTodayContent();

        List<SearchResultVO> recommendedContentList =
                tmdbService.getMainRecommendedContent();

        model.addAttribute(
                "popularContentList",
                popularContentList
        );

        model.addAttribute(
                "todayContentList",
                todayContentList
        );

        model.addAttribute(
                "recommendedContentList",
                recommendedContentList
        );

        return "index";
    }
}