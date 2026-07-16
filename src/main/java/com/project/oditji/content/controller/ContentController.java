package com.project.oditji.content.controller;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.view.RedirectView;
import org.springframework.web.util.UriUtils;

import com.project.oditji.content.service.ContentService;
import com.project.oditji.content.vo.ContentListPageVO;
import com.project.oditji.content.vo.ContentVO;
import com.project.oditji.content.vo.PersonFilmographyVO;
import com.project.oditji.favorite.service.FavoriteService;
import com.project.oditji.favorite.vo.FavoriteVO;
import com.project.oditji.member.vo.MemberVO;
import com.project.oditji.review.service.ReviewService;
import com.project.oditji.review.vo.ContentReviewVO;
import com.project.oditji.review.vo.ReviewVO;
import com.project.oditji.search.vo.SearchResultVO;
import com.project.oditji.tmdb.vo.ActorVO;
import com.project.oditji.tmdb.vo.DirectorVO;
import com.project.oditji.tmdb.vo.OttPlatformVO;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/content")
public class ContentController {

    private final ContentService contentService;
    private final ReviewService reviewService;
    private final FavoriteService favoriteService;

    public ContentController(
            ContentService contentService,
            ReviewService reviewService,
            FavoriteService favoriteService) {

        this.contentService = contentService;
        this.reviewService = reviewService;
        this.favoriteService = favoriteService;
    }

    @GetMapping("/prepare")
    public String prepareDetail(
            Long tmdbId,
            String contentType) {

        int contentNo =
                contentService.prepareContentDetail(
                        tmdbId,
                        contentType);

        return "redirect:/content/contentDetail/"
                + contentNo;
    }

    @GetMapping("/list")
    public String list(
            @RequestParam(defaultValue = "all")
            String type,
            @RequestParam(defaultValue = "1")
            int page,
            @RequestParam(required = false)
            List<String> contentCategories,
            @RequestParam(required = false)
            List<String> genreCodes,
            @RequestParam(required = false)
            List<String> providerIds,
            Model model) {

        String normalizedType =
                normalizeListType(type);

        int safePage =
                page <= 0 ? 1 : page;

        List<String> safeCategories =
                safeList(contentCategories);

        List<String> safeGenres =
                safeList(genreCodes);

        List<String> safeProviders =
                safeList(providerIds);

        ContentListPageVO pageVO =
                contentService.getContentListByType(
                        normalizedType,
                        safePage,
                        safeCategories,
                        safeGenres,
                        safeProviders);

        List<SearchResultVO> recommendedList =
                contentService.getContentRecommendedList(
                        safeProviders);

        model.addAttribute(
                "contentList",
                pageVO.getContentList());

        model.addAttribute(
                "recommendedList",
                recommendedList);

        model.addAttribute(
                "pageVO",
                pageVO);

        model.addAttribute(
                "type",
                normalizedType);

        model.addAttribute(
                "page",
                pageVO.getCurrentPage());

        model.addAttribute(
                "totalPage",
                pageVO.getTotalPages());

        model.addAttribute(
                "totalCount",
                pageVO.getTotalResults());

        model.addAttribute(
                "contentCategories",
                safeCategories);

        model.addAttribute(
                "genreCodes",
                safeGenres);

        model.addAttribute(
                "providerIds",
                safeProviders);

        model.addAttribute(
                "pageTitle",
                makePageTitle(normalizedType));

        return "content/contentList";
    }

    @GetMapping("/contentDetail/{contentNo}")
    public String detail(
            @PathVariable int contentNo,
            HttpSession session,
            Model model) {

        ContentVO content =
                contentService.getContentDetail(contentNo);

        if (content == null) {
            throw new IllegalArgumentException(
                    "존재하지 않는 콘텐츠입니다.");
        }

        List<ActorVO> actorList =
                contentService.getActorListByContentNo(contentNo);

        List<DirectorVO> directorList =
                contentService.getDirectorListByContentNo(contentNo);

        List<OttPlatformVO> ottList =
                contentService.getOttPlatformListByContentNo(contentNo);

        List<ContentVO> relatedContentList =
                contentService.getRelatedContentList(contentNo);

        List<ContentReviewVO> reviewList =
                reviewService.getContentReviewList(contentNo);

        Double avgRating =
                reviewService.getAvgRating(contentNo);

        int reviewCount =
                reviewService.getReviewCount(contentNo);

        MemberVO loginMember =
                (MemberVO) session.getAttribute("loginMember");

        Long loginMemberNo =
                loginMember == null
                        ? null
                        : loginMember.getMemberNo();

        ReviewVO myReview =
                reviewService.getMyReview(
                        loginMemberNo,
                        contentNo);

        Set<Integer> reportedReviewSet =
                reviewService.getReportedReviewSet(
                        loginMemberNo);

        boolean favoriteActive = false;

        if (loginMemberNo != null) {

            FavoriteVO favoriteVO =
                    new FavoriteVO();

            favoriteVO.setMemberNo(loginMemberNo);
            favoriteVO.setContentNo((long) contentNo);

            favoriteActive =
                    favoriteService.isFavorite(favoriteVO);
        }

        model.addAttribute("content", content);
        model.addAttribute("actorList", actorList);
        model.addAttribute("directorList", directorList);
        model.addAttribute("ottList", ottList);
        model.addAttribute(
                "relatedContentList",
                relatedContentList);

        model.addAttribute("reviewList", reviewList);
        model.addAttribute("avgRating", avgRating);
        model.addAttribute("reviewCount", reviewCount);
        model.addAttribute("myReview", myReview);
        model.addAttribute(
                "reportedReviewSet",
                reportedReviewSet);

        model.addAttribute(
                "favoriteActive",
                favoriteActive);

        model.addAttribute(
                "loginRequired",
                loginMember == null);

        return "content/contentDetail";
    }

    @GetMapping("/ott-search")
    public RedirectView redirectOttSearch(
            @RequestParam("platformName")
            String platformName,
            @RequestParam("title")
            String title) {

        String safePlatformName =
                platformName == null
                        ? ""
                        : platformName.trim();

        String safeTitle =
                title == null
                        ? ""
                        : title.trim();

        String encodedTitle =
                UriUtils.encodeQueryParam(
                        safeTitle,
                        StandardCharsets.UTF_8);

        String normalizedPlatformName =
                safePlatformName
                        .replace(" ", "")
                        .toLowerCase(Locale.ROOT);

        String redirectUrl;

        switch (normalizedPlatformName) {
            case "netflix":
                redirectUrl =
                        "https://www.netflix.com/search?q="
                        + encodedTitle;
                break;

            case "tving":
                redirectUrl =
                        "https://www.tving.com/search/all?keyword="
                        + encodedTitle;
                break;

            case "wavve":
                redirectUrl =
                        "https://www.wavve.com/search?searchWord="
                        + encodedTitle;
                break;

            case "disney+":
            case "disneyplus":
                redirectUrl =
                        "https://www.disneyplus.com/ko-kr/browse/search?q="
                        + encodedTitle;
                break;

            case "watcha":
                redirectUrl =
                        "https://watcha.com/search?query="
                        + encodedTitle;
                break;

            case "coupangplay":
            case "coupang":
                redirectUrl =
                        "https://www.coupangplay.com/search?q="
                        + encodedTitle;
                break;

            default:
                String fallbackKeyword =
                        safeTitle + " " + safePlatformName;

                redirectUrl =
                        "https://www.google.com/search?q="
                        + UriUtils.encodeQueryParam(
                                fallbackKeyword,
                                StandardCharsets.UTF_8);
                break;
        }

        RedirectView redirectView =
                new RedirectView(redirectUrl);

        redirectView.setExposeModelAttributes(false);

        return redirectView;
    }

    @GetMapping("/person/{tmdbPersonId}")
    public String personFilmography(
            @PathVariable Long tmdbPersonId,
            @RequestParam(defaultValue = "ACTOR")
            String role,
            Model model) {

        PersonFilmographyVO person =
                contentService.getPersonFilmography(
                        tmdbPersonId,
                        role);

        model.addAttribute(
                "person",
                person);

        return "content/personFilmography";
    }

    private String normalizeListType(
            String type) {

        String value =
                type == null
                        ? "all"
                        : type.trim()
                                .toLowerCase(Locale.ROOT);

        if ("popular".equals(value)
                || "new".equals(value)) {

            return value;
        }

        return "all";
    }

    private List<String> safeList(
            List<String> values) {

        return values == null
                ? new ArrayList<String>()
                : values;
    }

    private String makePageTitle(
            String type) {

        if ("popular".equals(type)) {
            return "인기 콘텐츠";
        }

        if ("new".equals(type)) {
            return "신규 콘텐츠";
        }

        return "영화·시리즈";
    }
}