package com.project.oditji.content.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.project.oditji.content.service.ContentService;
import com.project.oditji.content.vo.ContentVO;

@Controller
@RequestMapping("/content")
public class ContentController {

    private final ContentService contentService;

    public ContentController(ContentService contentService) {
        this.contentService = contentService;
    }

    @GetMapping("/prepare")
    public String prepareDetail(Long tmdbId, String contentType) {

        int contentNo = contentService.prepareContentDetail(tmdbId, contentType);

        return "redirect:/content/contentDetail/" + contentNo;
    }

    @GetMapping("/list")
    public String list(@RequestParam(defaultValue = "all") String type,
                    @RequestParam(defaultValue = "1") int page,
                    Model model) {

        List<ContentVO> contentList =
            contentService.getContentListByType(type, page);

        model.addAttribute("contentList", contentList);
        model.addAttribute("type", type);
        model.addAttribute("page", page);

        return "content/contentList";
    }

    @GetMapping("/contentDetail/{contentNo}")
    public String detail(@PathVariable int contentNo, Model model) {

        ContentVO content = contentService.getContentDetail(contentNo);

        model.addAttribute("content", content);

        return "content/contentDetail";
    }
}