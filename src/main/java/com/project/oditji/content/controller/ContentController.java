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
import com.project.oditji.content.vo.PersonFilmographyVO;
import com.project.oditji.tmdb.vo.ActorVO;
import com.project.oditji.tmdb.vo.DirectorVO;

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

        if (content == null) {
            throw new IllegalArgumentException("존재하지 않는 콘텐츠입니다.");
        }

        List<ActorVO> actorList =
                contentService.getActorListByContentNo(contentNo);

        List<DirectorVO> directorList =
                contentService.getDirectorListByContentNo(contentNo);

        model.addAttribute("content", content);
        model.addAttribute("actorList", actorList);
        model.addAttribute("directorList", directorList);

        return "content/contentDetail";
    }

    @GetMapping("/person/{tmdbPersonId}")
    public String personFilmography(
            @PathVariable Long tmdbPersonId,
            @RequestParam(defaultValue = "ACTOR") String role,
            Model model) {

        PersonFilmographyVO person =
                contentService.getPersonFilmography(tmdbPersonId, role);

        model.addAttribute("person", person);

        return "content/personFilmography";
    }
}
