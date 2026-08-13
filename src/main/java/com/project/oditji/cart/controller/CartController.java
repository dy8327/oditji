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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.project.oditji.cart.service.CartService;
import com.project.oditji.common.util.ApiResponseUtil;
import com.project.oditji.common.util.LoginMemberUtil;
import com.project.oditji.common.util.PaginationUtil;
import com.project.oditji.common.vo.PageVO;
import com.project.oditji.cart.vo.CartItemVO;
import com.project.oditji.cart.vo.CartRequestVO;
import com.project.oditji.member.vo.MemberVO;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/cart")
public class CartController {

    private static final String RESPONSE_CART_COUNT = "cartCount";
    private static final String RESPONSE_SUCCESS = "success";

    private final CartService cartService;
    private static final Logger log = LoggerFactory.getLogger(CartController.class);

    public CartController(
            CartService cartService) {

        this.cartService = cartService;
    }

    // 장바구니 목록 화면
    @GetMapping
    public String cart(
            @RequestParam(name = "page", defaultValue = "1") int page,
            HttpSession session,
            Model model) {

        MemberVO loginMember = LoginMemberUtil.getLoginMember(session);

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

        PageVO pageVO = PaginationUtil.createPage(page, 5, cartItemList.size());

        // [추가] 장바구니 합계는 전체 상품 기준으로 유지하고, 화면 목록만 페이지별로 표시합니다.
        model.addAttribute("cartItemList", PaginationUtil.slice(cartItemList, pageVO));
        model.addAttribute("cartTotalCount", cartItemList.size());
        model.addAttribute("pageVO", pageVO);
        model.addAttribute("totalPrice", totalPrice);

        return "cart/cart";
    }

    // 상품 상세에서 장바구니 담기
    @PostMapping("/add")
    @ResponseBody
    public Map<String, Object> addCartItem(@RequestBody CartRequestVO requestVO, HttpSession session) {

        MemberVO loginMember = LoginMemberUtil.getLoginMember(session);

        if (loginMember == null) {
            return ApiResponseUtil.loginRequired();
        }

        try {
            int cartCount = cartService.addCartItem(
                    loginMember.getMemberNo(),
                    requestVO.getProductNo(),
                    requestVO.getOptionNo(),
                    requestVO.getQuantity());

            Map<String, Object> response = ApiResponseUtil.success("장바구니에 상품을 담았습니다.");
            response.put(RESPONSE_CART_COUNT, cartCount);

            return response;

        } catch (IllegalArgumentException e) {

            return ApiResponseUtil.failure(e.getMessage());

        } catch (Exception e) {
            if (log.isErrorEnabled()) {
                log.error("장바구니 상품 추가 중 오류 - productNo: {}", requestVO.getProductNo(), e);
            }
            return ApiResponseUtil.failure("장바구니 처리 중 오류가 발생했습니다.");
        }
    }

    // 장바구니 상품 수량 변경
    @PostMapping("/update")
    @ResponseBody
    public Map<String, Object> updateCartItem(@RequestBody CartRequestVO requestVO, HttpSession session) {

        MemberVO loginMember = LoginMemberUtil.getLoginMember(session);

        if (loginMember == null) {
            return ApiResponseUtil.loginRequired();
        }

        try {
            cartService.updateCartItemQuantity(
                    loginMember.getMemberNo(),
                    requestVO.getCartItemNo(),
                    requestVO.getQuantity());

            return ApiResponseUtil.success("수량이 변경되었습니다.");

        } catch (IllegalArgumentException e) {

            return ApiResponseUtil.failure(e.getMessage());

        } catch (Exception e) {
            if (log.isErrorEnabled()) {
                log.error("장바구니 수량 변경 중 오류 - cartItemNo: {}", requestVO.getCartItemNo(), e);
            }
            return ApiResponseUtil.failure("수량 변경 중 오류가 발생했습니다.");
        }
    }

    // 장바구니 상품 개별 삭제
    @PostMapping("/delete")
    @ResponseBody
    public Map<String, Object> deleteCartItem(@RequestBody CartRequestVO requestVO, HttpSession session) {

        MemberVO loginMember = LoginMemberUtil.getLoginMember(session);

        if (loginMember == null) {
            return ApiResponseUtil.loginRequired();
        }

        return executeDelete(
                loginMember,
                () -> cartService.deleteCartItem(
                        loginMember.getMemberNo(),
                        requestVO.getCartItemNo()),
                "장바구니에서 상품을 삭제했습니다.",
                "상품 삭제 중 오류가 발생했습니다.",
                "장바구니 상품 삭제 중 오류 - cartItemNo: "
                        + requestVO.getCartItemNo());
    }

    // 선택한 장바구니 상품 삭제
    @PostMapping("/delete-selected")
    @ResponseBody
    public Map<String, Object> deleteSelectedCartItems(@RequestBody CartRequestVO requestVO, HttpSession session) {
        MemberVO loginMember = LoginMemberUtil.getLoginMember(session);

        if (loginMember == null) {
            return ApiResponseUtil.loginRequired();
        }

        return executeDelete(
                loginMember,
                () -> cartService.deleteSelectedCartItems(
                        loginMember.getMemberNo(),
                        requestVO.getCartItemNos()),
                "선택한 상품을 삭제했습니다.",
                "선택 상품 삭제 중 오류가 발생했습니다.",
                "선택한 장바구니 상품 삭제 중 오류");
    }

    // 헤더나 다른 화면에서 사용할 장바구니 개수
    @GetMapping("/count")
    @ResponseBody
    public Map<String, Object> countCartItems(HttpSession session) {
        MemberVO loginMember = LoginMemberUtil.getLoginMember(session);
        Map<String, Object> response = new LinkedHashMap<String, Object>();

        if (loginMember == null) {
            response.put(RESPONSE_SUCCESS, true);
            response.put(RESPONSE_CART_COUNT, 0);

            return response;
        }

        response.put(RESPONSE_SUCCESS, true);
        response.put(RESPONSE_CART_COUNT, cartService.countCartItems(loginMember.getMemberNo()));

        return response;
    }

    private Map<String, Object> executeDelete(
            MemberVO loginMember,
            Runnable deleteAction,
            String successMessage,
            String errorMessage,
            String logMessage) {

        return ApiResponseUtil.execute(
                deleteAction,
                successMessage,
                errorMessage,
                log,
                logMessage,
                response -> response.put(
                        RESPONSE_CART_COUNT,
                        cartService.countCartItems(
                                loginMember.getMemberNo())));
    }

}