package com.project.oditji.cart.controller;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.project.oditji.cart.service.CartService;
import com.project.oditji.cart.vo.CartItemVO;
import com.project.oditji.cart.vo.CartRequestVO;
import com.project.oditji.member.vo.MemberVO;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/cart")
public class CartController {

    private final CartService cartService;
    private static final Logger log = LoggerFactory.getLogger(CartController.class);

    public CartController(
            CartService cartService) {

        this.cartService = cartService;
    }

    // 장바구니 목록 화면
    @GetMapping
    public String cart(HttpSession session, Model model) {

        MemberVO loginMember = getLoginMember(session);

        if (loginMember == null) {
            return "redirect:/member/login?redirect=/cart";
        }

        List<CartItemVO> cartItemList = cartService.getCartItemList(loginMember.getMemberNo());

        long totalPrice = 0L;

        for (CartItemVO item : cartItemList) {
            if (item.isAvailable()) {
                totalPrice += item.getItemTotalPrice();
            }
        }

        model.addAttribute("cartItemList", cartItemList);
        model.addAttribute("totalPrice", totalPrice);

        return "cart/cart";
    }

    // 상품 상세에서 장바구니 담기
    @PostMapping("/add")
    @ResponseBody
    public Map<String, Object> addCartItem(@RequestBody CartRequestVO requestVO, HttpSession session) {

        MemberVO loginMember = getLoginMember(session);

        if (loginMember == null) {
            return loginRequiredResponse();
        }

        try {
            int cartCount = cartService.addCartItem(
                    loginMember.getMemberNo(),
                    requestVO.getProductNo(),
                    requestVO.getOptionNo(),
                    requestVO.getQuantity());

            Map<String, Object> response = successResponse("장바구니에 상품을 담았습니다.");
            response.put("cartCount", cartCount);

            return response;

        } catch (IllegalArgumentException e) {

            return failResponse(e.getMessage());

        } catch (Exception e) {
            if (log.isErrorEnabled()) {
                log.error("장바구니 상품 추가 중 오류 - productNo: {}", requestVO.getProductNo(), e);
            }
            return failResponse("장바구니 처리 중 오류가 발생했습니다.");
        }
    }

    // 장바구니 상품 수량 변경
    @PostMapping("/update")
    @ResponseBody
    public Map<String, Object> updateCartItem(@RequestBody CartRequestVO requestVO, HttpSession session) {

        MemberVO loginMember = getLoginMember(session);

        if (loginMember == null) {
            return loginRequiredResponse();
        }

        try {
            cartService.updateCartItemQuantity(
                    loginMember.getMemberNo(),
                    requestVO.getCartItemNo(),
                    requestVO.getQuantity());

            return successResponse("수량이 변경되었습니다.");

        } catch (IllegalArgumentException e) {

            return failResponse(e.getMessage());

        } catch (Exception e) {
            if (log.isErrorEnabled()) {
                log.error("장바구니 수량 변경 중 오류 - cartItemNo: {}", requestVO.getCartItemNo(), e);
            }
            return failResponse("수량 변경 중 오류가 발생했습니다.");
        }
    }

    // 장바구니 상품 개별 삭제
    @PostMapping("/delete")
    @ResponseBody
    public Map<String, Object> deleteCartItem(@RequestBody CartRequestVO requestVO, HttpSession session) {

        MemberVO loginMember = getLoginMember(session);

        if (loginMember == null) {
            return loginRequiredResponse();
        }

        try {
            cartService.deleteCartItem(loginMember.getMemberNo(), requestVO.getCartItemNo());

            Map<String, Object> response = successResponse("장바구니에서 상품을 삭제했습니다.");
            response.put("cartCount", cartService.countCartItems(loginMember.getMemberNo()));

            return response;

        } catch (IllegalArgumentException e) {

            return failResponse(e.getMessage());

        } catch (Exception e) {
            if (log.isErrorEnabled()) {
                log.error("장바구니 상품 삭제 중 오류 - cartItemNo: {}", requestVO.getCartItemNo(), e);
            }
            return failResponse("상품 삭제 중 오류가 발생했습니다.");
        }
    }

    // 선택한 장바구니 상품 삭제
    @PostMapping("/delete-selected")
    @ResponseBody
    public Map<String, Object> deleteSelectedCartItems(@RequestBody CartRequestVO requestVO, HttpSession session) {
        MemberVO loginMember = getLoginMember(session);

        if (loginMember == null) {
            return loginRequiredResponse();
        }

        try {
            cartService.deleteSelectedCartItems(loginMember.getMemberNo(), requestVO.getCartItemNos());

            Map<String, Object> response = successResponse("선택한 상품을 삭제했습니다.");
            response.put("cartCount", cartService.countCartItems(loginMember.getMemberNo()));

            return response;

        } catch (IllegalArgumentException e) {

            return failResponse(e.getMessage());

        } catch (Exception e) {
            if (log.isErrorEnabled()) {
                log.error("선택한 장바구니 상품 삭제 중 오류", e);
            }
            return failResponse("선택 상품 삭제 중 오류가 발생했습니다.");
        }
    }

    // 헤더나 다른 화면에서 사용할 장바구니 개수
    @GetMapping("/count")
    @ResponseBody
    public Map<String, Object> countCartItems(HttpSession session) {
        MemberVO loginMember = getLoginMember(session);
        Map<String, Object> response = new LinkedHashMap<String, Object>();

        if (loginMember == null) {
            response.put("success", true);
            response.put("cartCount", 0);

            return response;
        }

        response.put("success", true);
        response.put("cartCount", cartService.countCartItems(loginMember.getMemberNo()));

        return response;
    }

    private MemberVO getLoginMember(HttpSession session) {
        Object sessionMember = session.getAttribute("loginMember");
        if (!(sessionMember instanceof MemberVO)) {
            return null;
        }

        MemberVO loginMember = (MemberVO) sessionMember;
        if (loginMember.getMemberNo() == null || loginMember.getMemberNo() <= 0) {

            return null;
        }

        return loginMember;
    }

    private Map<String, Object> successResponse(String message) {
        Map<String, Object> response = new LinkedHashMap<String, Object>();

        response.put("success", true);
        response.put("message", message);

        return response;
    }

    private Map<String, Object> failResponse(String message) {
        Map<String, Object> response = new LinkedHashMap<String, Object>();

        response.put("success", false);
        response.put("message", message);

        return response;
    }

    private Map<String, Object> loginRequiredResponse() {

        Map<String, Object> response = failResponse("로그인이 필요한 서비스입니다.");
        response.put("loginRequired", true);

        return response;
    }
}