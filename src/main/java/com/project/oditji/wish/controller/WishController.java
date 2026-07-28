package com.project.oditji.wish.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.project.oditji.member.vo.MemberVO;
import com.project.oditji.wish.service.WishService;
import com.project.oditji.wish.vo.WishVO;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/wish")
public class WishController {

    private final WishService wishService;
    private static final Logger log = LoggerFactory.getLogger(WishController.class);

    public WishController(
            WishService wishService) {

        this.wishService = wishService;
    }

    /**
     * 상품 찜 토글.
     * favorite.js 의 공용 .fav-btn 핸들러가 data-type="goods" 일 때
     * 이 엔드포인트로 { productNo } 를 전송한다.
     */
    @PostMapping("/toggle")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> toggleWish(@RequestBody WishVO wishVO, HttpSession session) {
        MemberVO loginMember = getLoginMember(session);

        if (loginMember == null) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(createErrorResult("로그인이 필요합니다."));
        }

        try {
            wishVO.setMemberNo(loginMember.getMemberNo());

            boolean active = wishService.toggleWish(wishVO);
            Map<String, Object> result = new HashMap<String, Object>();

            result.put("active", active);
            result.put("productNo", wishVO.getProductNo());

            return ResponseEntity.ok(result);

        } catch (IllegalArgumentException e) {

            return ResponseEntity
                    .badRequest()
                    .body(createErrorResult(e.getMessage()));

      } catch (Exception e) {
                if (log.isErrorEnabled()) {
                        log.error("상품 찜 처리 중 오류 - productNo: {}", wishVO.getProductNo(), e);
                }

                return ResponseEntity
                        .status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(createErrorResult("찜 처리 중 오류가 발생했습니다."));
        }
    }

    private MemberVO getLoginMember(HttpSession session) {

        Object loginMember = session.getAttribute("loginMember");
        if (loginMember instanceof MemberVO memberVO) {
           
                return memberVO;
        }

        return null;
    }

    private Map<String, Object> createErrorResult(String message) {

        Map<String, Object> result = new HashMap<String, Object>();
        result.put("message", message);

        return result;
    }
}
