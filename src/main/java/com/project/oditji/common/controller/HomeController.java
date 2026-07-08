package com.project.oditji.common.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.project.oditji.content.service.ContentService;
import com.project.oditji.content.vo.ContentVO;

// 메인 페이지 진입 (콘텐츠 리스트 모델 전달)
@Controller
public class HomeController {

    private final ContentService contentService;

    public HomeController(ContentService contentService) {
        this.contentService = contentService;
    }

    /**
     * =========================
     * 메인 페이지
     * =========================
     */
    @GetMapping("/")
    public String main(Model model) {

        // 1. 메인 콘텐츠 리스트 (200개 or 인기순)
       List<ContentVO> contentList = contentService.getMainContentList();

      model.addAttribute("contentList", contentList);

        return "index";
    }
}