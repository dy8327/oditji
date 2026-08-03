package com.project.oditji.content.controller;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
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
import com.project.oditji.goods.service.GoodsService;
import com.project.oditji.goods.vo.GoodsVO;
import com.project.oditji.member.vo.MemberVO;
import com.project.oditji.review.service.ReviewService;
import com.project.oditji.review.vo.ContentReviewVO;
import com.project.oditji.review.vo.ReviewVO;
import com.project.oditji.search.vo.SearchResultVO;
import com.project.oditji.tmdb.dao.TmdbDAO;
import com.project.oditji.tmdb.vo.ActorVO;
import com.project.oditji.tmdb.vo.DirectorVO;
import com.project.oditji.tmdb.vo.OttPlatformVO;
import com.project.oditji.verify.service.VerifyService;

import jakarta.servlet.http.HttpSession;

/**
 * 콘텐츠 목록, 상세페이지, 인물 필모그래피 등을 처리하는 Controller입니다.
 */
@Controller
@RequestMapping("/content")
public class ContentController {

        private final ContentService contentService;
        private final ReviewService reviewService;
        private final FavoriteService favoriteService;
        private final TmdbDAO tmdbDAO;
        // [성인 콘텐츠 접근 제한 추가] 회원의 DB 성인인증 완료 여부를 확인합니다.
        private final VerifyService verifyService;
        // [관련 상품 추가] 콘텐츠 상세페이지에 연결된 상품을 조회할 때 사용합니다.
        private final GoodsService goodsService;

        private static final int RELATED_GOODS_SIZE = 8;
        private static final String PLATFORM_NETFLIX = "netflix";
        private static final String PLATFORM_TVING = "tving";
        private static final String PLATFORM_WAVVE = "wavve";
        private static final String PLATFORM_WATCHA = "watcha";
        private static final String PLATFORM_COUPANG = "coupang";
        private static final String VALUE_POPULAR = "popular";

        public ContentController(
                        ContentService contentService,
                        ReviewService reviewService,
                        FavoriteService favoriteService,
                        TmdbDAO tmdbDAO,
                        // [성인 콘텐츠 접근 제한 추가] 기존 성인인증 서비스를 주입받습니다.
                        VerifyService verifyService,
                        // [관련 상품 추가] GoodsService를 주입받습니다.
                        GoodsService goodsService) {

                this.contentService = contentService;
                this.reviewService = reviewService;
                this.favoriteService = favoriteService;
                this.tmdbDAO = tmdbDAO;
                // [성인 콘텐츠 접근 제한 추가] DB의 MEMBER.ADULT_VERIFIED 값을 확인할 때 사용합니다.
                this.verifyService = verifyService;
                this.goodsService = goodsService;
        }

        @GetMapping("/prepare")
        public String prepareDetail(
                        Long tmdbId,
                        String contentType) {

                int contentNo = contentService.prepareContentDetail(
                                tmdbId,
                                contentType);

                return "redirect:/content/contentDetail/"
                                + contentNo;
        }

        @GetMapping("/list")
        public String list(
                        @RequestParam(defaultValue = "all") String type,
                        @RequestParam(required = false) String sort,
                        @RequestParam(defaultValue = "1") int page,
                        @RequestParam(required = false) List<String> contentCategories,
                        @RequestParam(required = false) List<String> genreCodes,
                        @RequestParam(required = false) List<String> providerIds,
                        @RequestParam(required = false) List<String> ageRatings,
                        Model model) {

                String normalizedType = normalizeListType(type);

                String normalizedSort = normalizeListSort(
                                sort,
                                normalizedType);

                int safePage = page <= 0 ? 1 : page;

                List<String> safeCategories = safeList(contentCategories);

                List<String> safeGenres = safeList(genreCodes);

                List<String> safeProviders = safeList(providerIds);

                List<String> safeAgeRatings = safeList(ageRatings);

                ContentListPageVO pageVO = contentService.getContentListByType(
                                normalizedType,
                                normalizedSort,
                                safePage,
                                safeCategories,
                                safeGenres,
                                safeProviders,
                                safeAgeRatings);

                List<SearchResultVO> recommendedList = contentService.getContentRecommendedList(
                                safeCategories,
                                safeGenres,
                                safeProviders,
                                safeAgeRatings);

                Map<String, String> ottLogoMap = createOttLogoMap(
                                tmdbDAO.selectActivePlatformList());

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
                                "sort",
                                normalizedSort);

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
                                "ageRatings",
                                safeAgeRatings);

                model.addAttribute(
                                "pageTitle",
                                makePageTitle(normalizedType));

                model.addAttribute(
                                "ottLogoMap",
                                ottLogoMap);

                return "content/contentList";
        }

        /**
         * 콘텐츠 상세페이지를 표시합니다.
         *
         * 로그인 회원이 상세페이지에 진입한 경우
         * CONTENT_VIEW_HISTORY에 오늘 조회 이력을 저장합니다.
         *
         * 비로그인 사용자의 조회는 개인 OTT 추천 대상이 아니므로
         * 조회 이력 테이블에는 저장하지 않습니다.
         */
        @GetMapping("/contentDetail/{contentNo}")
        public String detail(
                        @PathVariable int contentNo,
                        HttpSession session,
                        Model model) {

                ContentVO content = contentService.getContentDetail(contentNo);

                if (content == null) {
                        throw new IllegalArgumentException(
                                        "존재하지 않는 콘텐츠입니다.");
                }

                MemberVO loginMember = (MemberVO) session.getAttribute(
                                "loginMember");

                Long loginMemberNo = loginMember == null
                                ? null
                                : loginMember.getMemberNo();

                /*
                 * [성인 콘텐츠 접근 제한 추가]
                 * 청소년 관람불가 또는 등급 정보가 없는 콘텐츠는
                 * 상세정보를 조회하기 전에 성인인증 페이지로 이동시킵니다.
                 *
                 * 비로그인 사용자는 성인인증 페이지에서 로그인한 뒤
                 * 다시 현재 콘텐츠 상세페이지로 돌아오게 됩니다.
                 */
                if (isAdultRestrictedContent(content.getAgeRating())) {

                        boolean adultVerified = loginMemberNo != null
                                        && verifyService.isAdultVerified(loginMemberNo);

                        if (!adultVerified) {

                                String returnUrl = "/content/contentDetail/"
                                                + contentNo;

                                String encodedReturnUrl = UriUtils.encodeQueryParam(
                                                returnUrl,
                                                StandardCharsets.UTF_8);

                                return "redirect:/verify/adult?returnUrl="
                                                + encodedReturnUrl;
                        }
                }

                /*
                 * 콘텐츠가 실제로 존재하는 것이 확인된 뒤
                 * 로그인 회원의 조회 이력을 저장합니다.
                 *
                 * 같은 날 동일 콘텐츠를 다시 조회하면
                 * 새로운 행이 아니라 VIEW_COUNT가 증가합니다.
                 */
                if (loginMemberNo != null) {

                        contentService.recordContentViewHistory(
                                        loginMemberNo,
                                        contentNo);
                }

                List<ActorVO> actorList = contentService.getActorListByContentNo(
                                contentNo);

                List<DirectorVO> directorList = contentService.getDirectorListByContentNo(
                                contentNo);

                List<OttPlatformVO> ottList = contentService.getOttPlatformListByContentNo(
                                contentNo);

                List<SearchResultVO> relatedContentList = contentService.getRelatedContentList(
                                contentNo);

                // [관련 상품 추가] 이 콘텐츠에 연결된 승인 완료 상품 목록입니다.
                List<GoodsVO> goodsList = goodsService.getGoodsByContentNo(
                                contentNo,
                                RELATED_GOODS_SIZE);

                List<ContentReviewVO> reviewList = reviewService.getContentReviewList(
                                contentNo);

                Double avgRating = reviewService.getAvgRating(
                                contentNo);

                int reviewCount = reviewService.getReviewCount(
                                contentNo);

                ReviewVO myReview = reviewService.getMyReview(
                                loginMemberNo,
                                contentNo);

                Set<Integer> reportedReviewSet = reviewService.getReportedReviewSet(
                                loginMemberNo);

                boolean favoriteActive = false;

                if (loginMemberNo != null) {

                        FavoriteVO favoriteVO = new FavoriteVO();

                        favoriteVO.setMemberNo(
                                        loginMemberNo);

                        favoriteVO.setContentNo(
                                        (long) contentNo);

                        favoriteActive = favoriteService.isFavorite(
                                        favoriteVO);
                }

                model.addAttribute(
                                "content",
                                content);

                model.addAttribute(
                                "actorList",
                                actorList);

                model.addAttribute(
                                "directorList",
                                directorList);

                model.addAttribute(
                                "ottList",
                                ottList);

                model.addAttribute(
                                "relatedContentList",
                                relatedContentList);

                model.addAttribute(
                                "goodsList",
                                goodsList);

                model.addAttribute(
                                "reviewList",
                                reviewList);

                model.addAttribute(
                                "avgRating",
                                avgRating);

                model.addAttribute(
                                "reviewCount",
                                reviewCount);

                model.addAttribute(
                                "myReview",
                                myReview);

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
                        @RequestParam("platformName") String platformName,
                        @RequestParam("title") String title) {

                String safePlatformName = platformName == null
                                ? ""
                                : platformName.trim();

                String safeTitle = title == null
                                ? ""
                                : title.trim();

                String encodedTitle = UriUtils.encodeQueryParam(
                                safeTitle,
                                StandardCharsets.UTF_8);

                String normalizedPlatformName = safePlatformName
                                .replace(" ", "")
                                .toLowerCase(Locale.ROOT);

                String redirectUrl;

                switch (normalizedPlatformName) {

                        case PLATFORM_NETFLIX:

                                redirectUrl = "https://www.netflix.com/search?q="
                                                + encodedTitle;

                                break;

                        case PLATFORM_TVING:

                                redirectUrl = "https://www.tving.com/search?keyword="
                                                + encodedTitle;

                                break;

                        case PLATFORM_WAVVE:

                                redirectUrl = "https://www.wavve.com/search?searchWord="
                                                + encodedTitle;

                                break;

                        case PLATFORM_WATCHA:

                                redirectUrl = "https://watcha.com/search?query="
                                                + encodedTitle;

                                break;

                        case "coupangplay":
                        case PLATFORM_COUPANG:

                                redirectUrl = "https://www.coupangplay.com/query"
                                                + "?src=page_search&keyword="
                                                + encodedTitle;

                                break;

                        /*
                         * Disney+는 검색어 전달 URL이 안정적이지 않아
                         * 공식 홈페이지로 이동합니다.
                         */
                        case "disney+":
                        case "disneyplus":

                                redirectUrl = "https://www.disneyplus.com/";

                                break;

                        default:

                                String fallbackKeyword = safeTitle
                                                + " "
                                                + safePlatformName;

                                redirectUrl = "https://www.google.com/search?q="
                                                + UriUtils.encodeQueryParam(
                                                                fallbackKeyword,
                                                                StandardCharsets.UTF_8);

                                break;
                }

                RedirectView redirectView = new RedirectView(
                                redirectUrl);

                redirectView.setExposeModelAttributes(
                                false);

                return redirectView;
        }

        @GetMapping("/person/{tmdbPersonId}")
        public String personFilmography(
                        @PathVariable Long tmdbPersonId,
                        @RequestParam(defaultValue = "ACTOR") String role,
                        Model model) {

                PersonFilmographyVO person = contentService.getPersonFilmography(
                                tmdbPersonId,
                                role);

                model.addAttribute(
                                "person",
                                person);

                // [관련 상품 추가] 이 인물이 배우로 연결된 승인 완료 상품 목록입니다.
                // 상품 등록 시 관련 배우가 지정되어 있을 때만 값이 채워지며,
                // 없으면 빈 목록이 반환되어 JSP에서 관련 상품 탭을 감춥니다.
                List<GoodsVO> relatedGoodsList = goodsService.getGoodsByTmdbActorId(
                                tmdbPersonId,
                                RELATED_GOODS_SIZE);

                model.addAttribute(
                                "relatedGoodsList",
                                relatedGoodsList);

                return "content/personFilmography";
        }

        /**
         * OTT_PLATFORM 테이블에서 조회한 플랫폼 목록을
         * JSP에서 사용하기 편한 로고 URL Map으로 변환합니다.
         */
        private Map<String, String> createOttLogoMap(
                        List<OttPlatformVO> platformList) {

                Map<String, String> logoMap = new LinkedHashMap<String, String>();

                if (platformList == null) {
                        return logoMap;
                }

                for (OttPlatformVO platform : platformList) {

                        if (platform == null
                                        || platform.getPlatformName() == null
                                        || platform.getLogoImage() == null
                                        || platform.getLogoImage().isBlank()) {

                                continue;
                        }

                        String platformKey = normalizePlatformName(
                                        platform.getPlatformName());

                        if (!platformKey.isEmpty()) {

                                logoMap.put(
                                                platformKey,
                                                platform.getLogoImage());
                        }
                }

                return logoMap;
        }

        /**
         * DB의 플랫폼 이름 표기가 조금 달라도
         * JSP에서 사용하는 공통 키로 맞춥니다.
         */
        private String normalizePlatformName(
                        String platformName) {

                if (platformName == null) {
                        return "";
                }

                String normalized = platformName
                                .trim()
                                .toLowerCase(Locale.ROOT)
                                .replaceAll(
                                                "[^a-z0-9]",
                                                "");

                if (normalized.contains(PLATFORM_NETFLIX)) {
                        return PLATFORM_NETFLIX;
                }

                if (normalized.contains(PLATFORM_TVING)) {
                        return PLATFORM_TVING;
                }

                if (normalized.contains(PLATFORM_WAVVE)) {
                        return PLATFORM_WAVVE;
                }

                if (normalized.contains("disney")) {
                        return "disney";
                }

                if (normalized.contains(PLATFORM_WATCHA)) {
                        return PLATFORM_WATCHA;
                }

                if (normalized.contains(PLATFORM_COUPANG)) {
                        return PLATFORM_COUPANG;
                }

                return normalized;
        }

        private String normalizeListType(
                        String type) {

                String value = type == null
                                ? "all"
                                : type.trim()
                                                .toLowerCase(
                                                                Locale.ROOT);

                if (VALUE_POPULAR.equals(value)
                                || "new".equals(value)) {

                        return value;
                }

                return "all";
        }

        /**
         * 콘텐츠 목록에서 허용하는 정렬값만 사용합니다.
         * 신규 탭은 정렬값이 없을 때 최신순을 기본으로 사용하고,
         * 나머지 탭은 인기순을 기본으로 사용합니다.
         */
        private String normalizeListSort(
                        String sort,
                        String type) {

                String defaultSort = "new".equals(type)
                                ? "latest"
                                : VALUE_POPULAR;

                if (sort == null
                                || sort.isBlank()) {

                        return defaultSort;
                }

                String normalized = sort.trim()
                                .toLowerCase(Locale.ROOT);

                if (VALUE_POPULAR.equals(normalized)
                                || "rating".equals(normalized)
                                || "latest".equals(normalized)
                                || "title".equals(normalized)) {

                        return normalized;
                }

                return defaultSort;
        }

        private List<String> safeList(
                        List<String> values) {

                return values == null
                                ? new ArrayList<String>()
                                : values;
        }

        private String makePageTitle(
                        String type) {

                if (VALUE_POPULAR.equals(type)) {
                        return "인기 콘텐츠";
                }

                if ("new".equals(type)) {
                        return "신규 콘텐츠";
                }

                return "영화·시리즈";
        }

        /**
         * [성인 콘텐츠 접근 제한 추가]
         * 성인인증이 필요한 관람등급인지 판별합니다.
         *
         * DB에 값이 없거나 "등급 정보 없음"으로 저장된 경우도
         * 안전을 위해 성인인증 대상으로 처리합니다.
         */
        private boolean isAdultRestrictedContent(
                        String ageRating) {

                if (ageRating == null
                                || ageRating.isBlank()) {

                        return true;
                }

                String normalizedAgeRating = ageRating
                                .replaceAll("\s+", "")
                                .toLowerCase(Locale.ROOT);

                return normalizedAgeRating.contains("청소년관람불가")
                                || normalizedAgeRating.contains("19세")
                                || normalizedAgeRating.contains("등급정보없음")
                                || normalizedAgeRating.contains("notrated")
                                || normalizedAgeRating.contains("unrated")
                                || "nr".equals(normalizedAgeRating);
        }

}