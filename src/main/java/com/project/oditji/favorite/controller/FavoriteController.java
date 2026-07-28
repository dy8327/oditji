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
import com.project.oditji.content.vo.ContentVO;
import com.project.oditji.favorite.service.FavoriteService;
import com.project.oditji.favorite.vo.FavoriteVO;
import com.project.oditji.goods.vo.GoodsVO;
import com.project.oditji.member.vo.MemberVO;
import com.project.oditji.wish.service.WishService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/favorite")
public class FavoriteController {

    private final FavoriteService favoriteService;
    private final ContentService contentService;
    private final WishService wishService;
    private static final Logger log = LoggerFactory.getLogger(FavoriteController.class);

    public FavoriteController(FavoriteService favoriteService, ContentService contentService, WishService wishService) {

        this.favoriteService = favoriteService;
        this.contentService = contentService;
        this.wishService = wishService;
    }

    @GetMapping("/list")
    public String favoriteList(HttpSession session, Model model) {

        MemberVO loginMember = getLoginMember(session);
        if (loginMember == null) {
            return "redirect:/member/login";
        }

        List<ContentVO> contentFavoriteList = favoriteService.selectFavoriteList(loginMember.getMemberNo());
        List<GoodsVO> goodsFavoriteList =wishService.selectWishList(loginMember.getMemberNo());

        model.addAttribute("contentFavoriteList", contentFavoriteList);
        model.addAttribute("contentCount", contentFavoriteList.size());
        model.addAttribute("goodsFavoriteList", goodsFavoriteList);
        model.addAttribute("goodsCount", goodsFavoriteList.size());
        model.addAttribute("totalFavoriteCount", contentFavoriteList.size() + goodsFavoriteList.size());

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

            boolean active = favoriteService.toggleFavorite(favoriteVO);
            Map<String, Object> result = new HashMap<String, Object>();

            result.put("active", active);
            result.put("contentNo", favoriteVO.getContentNo());

            return ResponseEntity.ok(result);

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
    public ResponseEntity<Map<String, Object>> toggleFavoriteByTmdb(@RequestBody FavoriteVO requestVO, HttpSession session) {

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

            favoriteVO.setMemberNo( loginMember.getMemberNo());
            favoriteVO.setContentNo((long) contentNo);

            boolean active = favoriteService.toggleFavorite(favoriteVO);
            Map<String, Object> result = new HashMap<String, Object>();

            result.put("active", active);
            result.put("contentNo", contentNo);
            result.put("tmdbId", tmdbId);
            result.put("contentType", contentType);

            return ResponseEntity.ok(result);

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
            result.put("active", false);

            return ResponseEntity.ok(result);
        }

        try {
            FavoriteVO favoriteVO = new FavoriteVO();

            favoriteVO.setMemberNo(loginMember.getMemberNo());
            favoriteVO.setTmdbId(tmdbId);
            favoriteVO.setContentType(normalizeContentType(contentType));

            boolean active = favoriteService.isFavoriteByTmdb(favoriteVO);

            result.put("login", true);
            result.put("active", active);

            return ResponseEntity.ok(result);

        } catch (IllegalArgumentException e) {

            return ResponseEntity
                    .badRequest()
                    .body(createErrorResult(e.getMessage()));
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
