package com.project.oditji.favorite.controller;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.project.oditji.content.vo.ContentVO;
import com.project.oditji.favorite.service.FavoriteService;
import com.project.oditji.favorite.vo.FavoriteVO;
import com.project.oditji.member.vo.MemberVO;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/favorite")
public class FavoriteController {

    private final FavoriteService favoriteService;

    public FavoriteController(FavoriteService favoriteService) {
        this.favoriteService = favoriteService;
    }

    // 찜 목록
    @GetMapping("/list")
    public String favoriteList(HttpSession session,
                               Model model) {

        MemberVO loginMember =
                (MemberVO) session.getAttribute("loginMember");

        if (loginMember == null) {
            return "redirect:/member/login";
        }

        List<ContentVO> contentFavoriteList =
                favoriteService.selectFavoriteList(loginMember.getMemberNo());

        model.addAttribute("contentFavoriteList", contentFavoriteList);
        model.addAttribute("contentCount", contentFavoriteList.size());
        model.addAttribute("totalFavoriteCount", contentFavoriteList.size());

        // 상품 찜은 아직 미구현
        model.addAttribute("goodsFavoriteList", Collections.emptyList());
        model.addAttribute("goodsCount", 0);

        return "favorite/favoriteList";
    }

    // 찜 토글
    @PostMapping("/toggle")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> toggleFavorite(
            @RequestBody FavoriteVO favoriteVO,
            HttpSession session) {

        MemberVO loginMember =
                (MemberVO) session.getAttribute("loginMember");

        if (loginMember == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        favoriteVO.setMemberNo(loginMember.getMemberNo());

        boolean active = favoriteService.toggleFavorite(favoriteVO);

        Map<String, Object> result = new HashMap<>();
        result.put("active", active);

        return ResponseEntity.ok(result);
    }

}