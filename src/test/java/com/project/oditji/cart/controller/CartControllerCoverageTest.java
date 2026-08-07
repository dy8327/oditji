package com.project.oditji.cart.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.ExtendedModelMap;

import com.project.oditji.cart.service.CartService;
import com.project.oditji.cart.vo.CartItemVO;
import com.project.oditji.cart.vo.CartRequestVO;
import com.project.oditji.member.vo.MemberVO;

import jakarta.servlet.http.HttpSession;

/** 장바구니 화면, 추가, 수량 변경, 삭제 및 개수 조회의 분기를 검증합니다. */
@ExtendWith(MockitoExtension.class)
class CartControllerCoverageTest {

        @Mock
        private CartService cartService;

        @Mock
        private HttpSession session;

        private CartController controller;

        @BeforeEach
        void setUp() {
                controller = new CartController(cartService);
        }

        @Test
        void cartShouldRedirectAnonymousMember() {
                assertEquals(
                                "redirect:/member/login?redirect=/cart",
                                controller.cart(1, session, new ExtendedModelMap()));
                verify(cartService, never()).getCartItemList(anyLong());
        }

        @Test
        void cartShouldRejectInvalidSessionMemberNumbers() {
                when(session.getAttribute("loginMember"))
                                .thenReturn("invalid")
                                .thenReturn(member(null))
                                .thenReturn(member(0L))
                                .thenReturn(member(-1L));

                assertEquals(
                                "redirect:/member/login?redirect=/cart",
                                controller.cart(1, session, new ExtendedModelMap()));
                assertEquals(
                                "redirect:/member/login?redirect=/cart",
                                controller.cart(1, session, new ExtendedModelMap()));
                assertEquals(
                                "redirect:/member/login?redirect=/cart",
                                controller.cart(1, session, new ExtendedModelMap()));
                assertEquals(
                                "redirect:/member/login?redirect=/cart",
                                controller.cart(1, session, new ExtendedModelMap()));
        }

        @Test
        void cartShouldExposeItemsAndSumOnlyAvailablePrices() {
                login(1L);
                CartItemVO available = item(10000, 10, 2, 5, "APPROVED");
                CartItemVO soldOut = item(20000, 0, 1, 0, "APPROVED");
                CartItemVO waiting = item(30000, 0, 1, 10, "WAITING");
                List<CartItemVO> items = List.of(available, soldOut, waiting);
                when(cartService.getCartItemList(1L)).thenReturn(items);
                ExtendedModelMap model = new ExtendedModelMap();

                assertEquals("cart/cart", controller.cart(1, session, model));
                assertSame(items, model.get("cartItemList"));
                assertEquals(18000L, model.get("totalPrice"));
        }

        @Test
        void addShouldRequireLogin() {
                Map<String, Object> response = controller.addCartItem(request(), session);

                assertFalse((Boolean) response.get("success"));
                assertTrue((Boolean) response.get("loginRequired"));
                assertEquals("로그인이 필요한 서비스입니다.", response.get("message"));
        }

        @Test
        void addShouldReturnCartCountOnSuccess() {
                login(2L);
                CartRequestVO request = request();
                when(cartService.addCartItem(2L, 10, 20L, 3)).thenReturn(4);

                Map<String, Object> response = controller.addCartItem(request, session);

                assertTrue((Boolean) response.get("success"));
                assertEquals(4, response.get("cartCount"));
                assertEquals("장바구니에 상품을 담았습니다.", response.get("message"));
        }

        @Test
        void addShouldMapValidationAndUnexpectedFailures() {
                login(3L);
                CartRequestVO invalid = request();
                when(cartService.addCartItem(3L, 10, 20L, 3))
                                .thenThrow(new IllegalArgumentException("재고가 부족합니다."))
                                .thenThrow(new IllegalStateException("db"));

                Map<String, Object> validation = controller.addCartItem(invalid, session);
                assertFalse((Boolean) validation.get("success"));
                assertEquals("재고가 부족합니다.", validation.get("message"));

                CartRequestVO failed = request();

                Map<String, Object> error = controller.addCartItem(failed, session);
                assertFalse((Boolean) error.get("success"));
                assertEquals("장바구니 처리 중 오류가 발생했습니다.", error.get("message"));
        }

        @Test
        void updateShouldRequireLoginAndReturnSuccess() {
                CartRequestVO request = request();

                Map<String, Object> anonymous = controller.updateCartItem(request, session);
                assertTrue((Boolean) anonymous.get("loginRequired"));

                login(4L);
                Map<String, Object> success = controller.updateCartItem(request, session);

                assertTrue((Boolean) success.get("success"));
                assertEquals("수량이 변경되었습니다.", success.get("message"));
                verify(cartService).updateCartItemQuantity(4L, 30L, 3);
        }

        @Test
        void updateShouldMapValidationAndUnexpectedFailures() {
                login(5L);
                CartRequestVO request = request();
                doThrow(new IllegalArgumentException("수량 오류"))
                                .when(cartService)
                                .updateCartItemQuantity(5L, 30L, 3);

                Map<String, Object> validation = controller.updateCartItem(request, session);
                assertEquals("수량 오류", validation.get("message"));

                doThrow(new IllegalStateException("db"))
                                .when(cartService)
                                .updateCartItemQuantity(5L, 30L, 3);

                Map<String, Object> error = controller.updateCartItem(request, session);
                assertEquals("수량 변경 중 오류가 발생했습니다.", error.get("message"));
        }

        @Test
        void deleteShouldRequireLoginAndReturnUpdatedCount() {
                CartRequestVO request = request();

                assertTrue((Boolean) controller.deleteCartItem(request, session).get("loginRequired"));

                login(6L);
                when(cartService.countCartItems(6L)).thenReturn(2);

                Map<String, Object> success = controller.deleteCartItem(request, session);

                assertTrue((Boolean) success.get("success"));
                assertEquals(2, success.get("cartCount"));
                verify(cartService).deleteCartItem(6L, 30L);
        }

        @Test
        void deleteShouldMapValidationAndUnexpectedFailures() {
                login(7L);
                CartRequestVO request = request();
                doThrow(new IllegalArgumentException("삭제할 수 없습니다."))
                                .when(cartService)
                                .deleteCartItem(7L, 30L);

                assertEquals(
                                "삭제할 수 없습니다.",
                                controller.deleteCartItem(request, session).get("message"));

                doThrow(new IllegalStateException("db"))
                                .when(cartService)
                                .deleteCartItem(7L, 30L);

                assertEquals(
                                "상품 삭제 중 오류가 발생했습니다.",
                                controller.deleteCartItem(request, session).get("message"));
        }

        @Test
        void deleteSelectedShouldRequireLoginAndReturnUpdatedCount() {
                CartRequestVO request = request();

                assertTrue((Boolean) controller.deleteSelectedCartItems(request, session)
                                .get("loginRequired"));

                login(8L);
                when(cartService.countCartItems(8L)).thenReturn(1);

                Map<String, Object> response = controller.deleteSelectedCartItems(request, session);

                assertTrue((Boolean) response.get("success"));
                assertEquals(1, response.get("cartCount"));
                verify(cartService).deleteSelectedCartItems(8L, List.of(30L, 31L));
        }

        @Test
        void deleteSelectedShouldMapValidationAndUnexpectedFailures() {
                login(9L);
                CartRequestVO request = request();
                doThrow(new IllegalArgumentException("선택 항목이 없습니다."))
                                .when(cartService)
                                .deleteSelectedCartItems(9L, List.of(30L, 31L));

                assertEquals(
                                "선택 항목이 없습니다.",
                                controller.deleteSelectedCartItems(request, session).get("message"));

                doThrow(new IllegalStateException("db"))
                                .when(cartService)
                                .deleteSelectedCartItems(9L, List.of(30L, 31L));

                assertEquals(
                                "선택 상품 삭제 중 오류가 발생했습니다.",
                                controller.deleteSelectedCartItems(request, session).get("message"));
        }

        @Test
        void countShouldReturnZeroForAnonymousAndServiceCountForMember() {
                Map<String, Object> anonymous = controller.countCartItems(session);
                assertEquals(Boolean.TRUE, anonymous.get("success"));
                assertEquals(0, anonymous.get("cartCount"));

                login(10L);
                when(cartService.countCartItems(10L)).thenReturn(5);

                Map<String, Object> member = controller.countCartItems(session);
                assertEquals(Boolean.TRUE, member.get("success"));
                assertEquals(5, member.get("cartCount"));
        }

        private void login(Long memberNo) {
                when(session.getAttribute("loginMember")).thenReturn(member(memberNo));
        }

        private MemberVO member(Long memberNo) {
                MemberVO member = new MemberVO();
                member.setMemberNo(memberNo);
                return member;
        }

        private CartRequestVO request() {
                CartRequestVO request = new CartRequestVO();
                request.setProductNo(10);
                request.setOptionNo(20L);
                request.setQuantity(3);
                request.setCartItemNo(30L);
                request.setCartItemNos(List.of(30L, 31L));
                return request;
        }

        private CartItemVO item(
                        int price,
                        int discountRate,
                        int quantity,
                        int stock,
                        String status) {

                CartItemVO item = new CartItemVO();
                item.setPrice(price);
                item.setDiscountRate(discountRate);
                item.setQuantity(quantity);
                item.setStock(stock);
                item.setStatus(status);
                return item;
        }
}