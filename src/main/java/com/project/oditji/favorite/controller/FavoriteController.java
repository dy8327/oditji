package com.project.oditji.favorite.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.project.oditji.content.service.ContentService;
import com.project.oditji.common.util.PaginationUtil;
import com.project.oditji.common.vo.PageVO;
import com.project.oditji.content.vo.ContentVO;
import com.project.oditji.favorite.service.FavoriteService;
import com.project.oditji.favorite.vo.FavoriteVO;
import com.project.oditji.goods.vo.GoodsVO;
import com.project.oditji.member.vo.MemberVO;
import com.project.oditji.verify.service.VerifyService;
import com.project.oditji.wish.service.WishService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/favorite")
public class FavoriteController {

    private final FavoriteService favoriteService;
    private final ContentService contentService;
    private final WishService wishService;
    private final VerifyService verifyService;
    private static final Logger log = LoggerFactory.getLogger(FavoriteController.class);

    private static final String RESPONSE_ACTIVE = "active";
    private static final String ADULT_FAVORITE_RESTRICTION_MESSAGE = "성인인증이 필요한 콘텐츠입니다. 성인인증 후 찜해 주세요.";

    public FavoriteController(FavoriteService favoriteService, ContentService contentService,
            WishService wishService, VerifyService verifyService) {

        this.favoriteService = favoriteService;
        this.contentService = contentService;
        this.wishService = wishService;
        this.verifyService = verifyService;
    }

    @GetMapping("/list")
    public String favoriteList(
            @RequestParam(name = "contentPage", defaultValue = "1") int contentPage,
            @RequestParam(name = "goodsPage", defaultValue = "1") int goodsPage,
            @RequestParam(name = "tab", defaultValue = "content") String tab,
            HttpSession session,
            Model model) {

        MemberVO loginMember = getLoginMember(session);
        if (loginMember == null) {
            return "redirect:/member/login";
        }

        List<ContentVO> contentFavoriteList = favoriteService.selectFavoriteList(loginMember.getMemberNo());
        List<GoodsVO> goodsFavoriteList = wishService.selectWishList(loginMember.getMemberNo());

        PageVO contentPageVO = PaginationUtil.createPage(contentPage, 10, contentFavoriteList.size());
        PageVO goodsPageVO = PaginationUtil.createPage(goodsPage, 8, goodsFavoriteList.size());

        // [추가] 콘텐츠 찜과 상품 찜은 각 탭의 페이지 위치를 독립적으로 유지합니다.
        model.addAttribute("contentFavoriteList", PaginationUtil.slice(contentFavoriteList, contentPageVO));
        model.addAttribute("contentPageVO", contentPageVO);
        model.addAttribute("contentCount", contentFavoriteList.size());
        model.addAttribute("goodsFavoriteList", PaginationUtil.slice(goodsFavoriteList, goodsPageVO));
        model.addAttribute("goodsPageVO", goodsPageVO);
        model.addAttribute("goodsCount", goodsFavoriteList.size());
        model.addAttribute("totalFavoriteCount", contentFavoriteList.size() + goodsFavoriteList.size());
        model.addAttribute("activeTab", "goods".equalsIgnoreCase(tab) ? "goods" : "content");

        return "favorite/favoriteList";
    }

    @PostMapping("/toggle")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> toggleFavorite(@RequestBody FavoriteVO favoriteVO, HttpSession session) {

        MemberVO loginMember = getLoginMember(session);

        if (loginMember == null) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(createErrorResult("로그인이 필요합니다."));
        }

        try {
            favoriteVO.setMemberNo(loginMember.getMemberNo());

            // 이미 찜한 콘텐츠의 해제는 허용하고, 신규 찜일 때만 성인인증을 검사합니다.
            validateAdultFavoriteAccess(favoriteVO, loginMember);

            boolean active = favoriteService.toggleFavorite(favoriteVO);
            Map<String, Object> result = new HashMap<String, Object>();

            result.put(RESPONSE_ACTIVE, active);
            result.put("contentNo", favoriteVO.getContentNo());

            return ResponseEntity.ok(result);

        } catch (AdultFavoriteRestrictedException e) {

            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(createErrorResult(e.getMessage()));

        } catch (IllegalArgumentException e) {

            return ResponseEntity
                    .badRequest()
                    .body(createErrorResult(e.getMessage()));

        } catch (Exception e) {
            if (log.isErrorEnabled()) {
                log.error("콘텐츠 찜 처리 중 오류 - contentNo: {}", favoriteVO.getContentNo(), e);
            }

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResult("찜 처리 중 오류가 발생했습니다."));
        }
    }

    @PostMapping("/toggle-by-tmdb")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> toggleFavoriteByTmdb(@RequestBody FavoriteVO requestVO,
            HttpSession session) {

        MemberVO loginMember = getLoginMember(session);

        if (loginMember == null) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(createErrorResult("로그인이 필요합니다."));
        }

        try {
            Long tmdbId = requestVO.getTmdbId();
            String contentType = normalizeContentType(requestVO.getContentType());

            if (tmdbId == null || tmdbId <= 0) {
                throw new IllegalArgumentException("TMDB 콘텐츠 번호가 올바르지 않습니다.");
            }

            int contentNo = contentService.prepareContentDetail(tmdbId, contentType);
            FavoriteVO favoriteVO = new FavoriteVO();

            favoriteVO.setMemberNo(loginMember.getMemberNo());
            favoriteVO.setContentNo((long) contentNo);

            // 목록 화면에서도 신규 찜일 때 동일한 성인인증 제한을 적용합니다.
            validateAdultFavoriteAccess(favoriteVO, loginMember);

            boolean active = favoriteService.toggleFavorite(favoriteVO);
            Map<String, Object> result = new HashMap<String, Object>();

            result.put(RESPONSE_ACTIVE, active);
            result.put("contentNo", contentNo);
            result.put("tmdbId", tmdbId);
            result.put("contentType", contentType);

            return ResponseEntity.ok(result);

        } catch (AdultFavoriteRestrictedException e) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(createErrorResult(e.getMessage()));

        } catch (IllegalArgumentException e) {
            return ResponseEntity
                    .badRequest()
                    .body(createErrorResult(e.getMessage()));

        } catch (IllegalStateException e) {
            if (log.isErrorEnabled()) {
                log.error("TMDB 콘텐츠 저장 후 찜 처리 실패 - tmdbId: {}, contentType: {}",
                        requestVO.getTmdbId(), requestVO.getContentType(), e);
            }

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResult("콘텐츠 저장 및 찜 처리 중 오류가 발생했습니다."));

        } catch (Exception e) {
            if (log.isErrorEnabled()) {
                log.error("TMDB 콘텐츠 찜 처리 중 오류 - tmdbId: {}, contentType: {}",
                        requestVO.getTmdbId(), requestVO.getContentType(), e);
            }

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResult("콘텐츠 저장 및 찜 처리 중 오류가 발생했습니다."));
        }
    }

    @GetMapping("/status-by-tmdb")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> favoriteStatusByTmdb(@RequestParam("tmdbId") Long tmdbId,
            @RequestParam("contentType") String contentType, HttpSession session) {

        MemberVO loginMember = getLoginMember(session);
        Map<String, Object> result = new HashMap<String, Object>();

        if (loginMember == null) {
            result.put("login", false);
            result.put(RESPONSE_ACTIVE, false);

            return ResponseEntity.ok(result);
        }

        try {
            FavoriteVO favoriteVO = new FavoriteVO();

            favoriteVO.setMemberNo(loginMember.getMemberNo());
            favoriteVO.setTmdbId(tmdbId);
            favoriteVO.setContentType(normalizeContentType(contentType));

            boolean active = favoriteService.isFavoriteByTmdb(favoriteVO);

            result.put("login", true);
            result.put(RESPONSE_ACTIVE, active);

            return ResponseEntity.ok(result);

        } catch (IllegalArgumentException e) {

            return ResponseEntity
                    .badRequest()
                    .body(createErrorResult(e.getMessage()));
        }
    }

    /**
     * 성인인증이 필요한 콘텐츠의 신규 찜인지 확인합니다.
     * 이미 찜한 콘텐츠는 찜 해제 요청이므로 제한하지 않습니다.
     */
    private void validateAdultFavoriteAccess(FavoriteVO favoriteVO, MemberVO loginMember) {

        if (favoriteVO == null || favoriteVO.getContentNo() == null) {
            return;
        }

        if (favoriteService.isFavorite(favoriteVO)) {
            return;
        }

        int contentNo = Math.toIntExact(favoriteVO.getContentNo());
        ContentVO content = contentService.getContentDetail(contentNo);

        if (content == null) {
            throw new IllegalArgumentException("존재하지 않는 콘텐츠입니다.");
        }

        if (!isAdultRestrictedContent(content.getAgeRating())) {
            return;
        }

        if (!verifyService.isAdultVerified(loginMember.getMemberNo())) {
            throw new AdultFavoriteRestrictedException(ADULT_FAVORITE_RESTRICTION_MESSAGE);
        }
    }

    /**
     * 콘텐츠 상세페이지의 성인 제한 기준과 동일하게 판별합니다.
     * 청소년 관람불가, 등급 정보 없음, 값 없음과 해외 미등급 표기를 제한합니다.
     */
    private boolean isAdultRestrictedContent(String ageRating) {

        if (ageRating == null || ageRating.isBlank()) {
            return true;
        }

        String normalizedAgeRating = ageRating
                .replaceAll("\\s+", "")
                .toLowerCase(Locale.ROOT);

        return normalizedAgeRating.contains("청소년관람불가")
                || normalizedAgeRating.contains("19세")
                || normalizedAgeRating.contains("등급정보없음")
                || normalizedAgeRating.contains("notrated")
                || normalizedAgeRating.contains("unrated")
                || "nr".equals(normalizedAgeRating);
    }

    /**
     * 성인인증이 필요한 신규 찜 요청을 403 응답으로 구분하기 위한 내부 예외입니다.
     */
    private static class AdultFavoriteRestrictedException extends RuntimeException {

        private static final long serialVersionUID = 1L;

        AdultFavoriteRestrictedException(String message) {
            super(message);
        }
    }

    private MemberVO getLoginMember(HttpSession session) {

        Object loginMember = session.getAttribute("loginMember");

        if (loginMember instanceof MemberVO memberVO) {
            return memberVO;
        }

        return null;
    }

    private String normalizeContentType(String contentType) {

        if (contentType == null || contentType.isBlank()) {

            throw new IllegalArgumentException("콘텐츠 유형이 없습니다.");
        }

        String normalized = contentType.trim().toUpperCase(Locale.ROOT);
        if (!"MOVIE".equals(normalized) && !"TV".equals(normalized)) {

            throw new IllegalArgumentException("올바르지 않은 콘텐츠 유형입니다.");
        }

        return normalized;
    }

    private Map<String, Object> createErrorResult(String message) {

        Map<String, Object> result = new HashMap<String, Object>();
        result.put("message", message);

        return result;
    }
}