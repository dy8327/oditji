package com.project.oditji.review.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.project.oditji.member.vo.MemberVO;
import com.project.oditji.review.service.ReviewService;
import com.project.oditji.review.vo.MyReviewVO;

import jakarta.servlet.http.HttpSession;

/** 콘텐츠·상품 리뷰 목록, 작성, 수정, 삭제와 예외 리다이렉트를 검증합니다. */
@ExtendWith(MockitoExtension.class)
class ReviewControllerCoverageTest {

        @Mock
        private ReviewService reviewService;

        @Mock
        private HttpSession session;

        @Mock
        private RedirectAttributes redirectAttributes;

        private ReviewController controller;

        @BeforeEach
        void setUp() {
                controller = new ReviewController(reviewService);
        }

        @Test
        void myReviewListShouldRequireLoginAndExposeList() {
                assertEquals(
                                "redirect:/member/login",
                                controller.myReviewList(1, "ALL", session, new ExtendedModelMap()));

                login(1L);
                List<MyReviewVO> reviews = List.of(new MyReviewVO());
                when(reviewService.getMyReviewList(1L)).thenReturn(reviews);
                ExtendedModelMap model = new ExtendedModelMap();

                assertEquals("review/myReviewList", controller.myReviewList(1, "ALL", session, model));
                assertSame(reviews, model.get("reviewList"));
        }

        @Test
        void writeContentShouldRequireLoginAndDelegateSuccess() {
                assertEquals(
                                "redirect:/member/login",
                                controller.writeContentReview(
                                                session, 10, 4.5, "좋아요", "N", redirectAttributes));

                login(2L);
                assertEquals(
                                "redirect:/content/contentDetail/10#reviewSection",
                                controller.writeContentReview(
                                                session, 10, 4.5, "좋아요", "Y", redirectAttributes));
                verify(reviewService).writeContentReview(2L, 10, 4.5, "좋아요", "Y");
        }

        @Test
        void duplicateContentReviewShouldExposeAlertAndReturnDetail() {
                login(3L);
                doThrow(new IllegalStateException("duplicate"))
                                .when(reviewService)
                                .writeContentReview(3L, 11, 3.0, "중복", "N");

                assertEquals(
                                "redirect:/content/contentDetail/11#reviewSection",
                                controller.writeContentReview(
                                                session, 11, 3.0, "중복", "N", redirectAttributes));
                verify(redirectAttributes).addFlashAttribute(
                                "reviewAlertMessage",
                                "계정 하나당 리뷰 1개만 작성이 가능합니다");
        }

        @Test
        void updateContentShouldRequireLoginAndDelegate() {
                assertEquals(
                                "redirect:/member/login",
                                controller.updateContentReview(
                                                session, 5L, 12, 4.0, "수정", "N"));

                login(4L);
                assertEquals(
                                "redirect:/content/contentDetail/12#reviewSection",
                                controller.updateContentReview(
                                                session, 5L, 12, 4.0, "수정", "Y"));
                verify(reviewService).updateContentReview(
                                4L, 5L, 12, 4.0, "수정", "Y");
        }

        @Test
        void writeProductShouldRequireLoginAndKeepPositivePage() {
                assertEquals(
                                "redirect:/member/login",
                                controller.writeProductReview(
                                                session, 20, 30, 5.0, "상품 리뷰", 2, redirectAttributes));

                login(5L);
                assertEquals(
                                "redirect:/order/list?page=2",
                                controller.writeProductReview(
                                                session, 20, 30, 5.0, "상품 리뷰", 2, redirectAttributes));
                verify(reviewService).writeProductReview(
                                5L, 20, 30, 5.0, "상품 리뷰");
        }

        @Test
        void writeProductShouldNormalizePageAndExposeFailureMessage() {
                login(6L);

                assertEquals(
                                "redirect:/order/list?page=1",
                                controller.writeProductReview(
                                                session, 21, 31, 4.0, "정상", -2, redirectAttributes));

                doThrow(new IllegalArgumentException("구매한 상품만 작성할 수 있습니다."))
                                .when(reviewService)
                                .writeProductReview(6L, 22, 32, 3.0, "실패");

                assertEquals(
                                "redirect:/order/list?page=1",
                                controller.writeProductReview(
                                                session, 22, 32, 3.0, "실패", 0, redirectAttributes));
                verify(redirectAttributes).addFlashAttribute(
                                "reviewMessage",
                                "구매한 상품만 작성할 수 있습니다.");
        }

        @Test
        void deleteContentShouldRequireLoginAndReturnRequestedLocation() {
                assertEquals(
                                "redirect:/member/login",
                                controller.deleteContentReview(session, 40L, 10));

                login(7L);
                assertEquals(
                                "redirect:/content/contentDetail/10#reviewSection",
                                controller.deleteContentReview(session, 40L, 10));
                verify(reviewService).deleteContentReview(7L, 40L);

                assertEquals(
                                "redirect:/review/myReviewList",
                                controller.deleteContentReview(session, 41L, null));
                verify(reviewService).deleteContentReview(7L, 41L);
        }

        @Test
        void deleteProductShouldRequireLoginAndReturnRequestedLocation() {
                assertEquals(
                                "redirect:/member/login",
                                controller.deleteProductReview(session, 50L, 20));

                login(8L);
                assertEquals(
                                "redirect:/goods/goodsDetail/20#reviewSection",
                                controller.deleteProductReview(session, 50L, 20));
                verify(reviewService).deleteProductReview(8L, 50L);

                assertEquals(
                                "redirect:/review/myReviewList",
                                controller.deleteProductReview(session, 51L, null));
                verify(reviewService).deleteProductReview(8L, 51L);
        }

        private void login(Long memberNo) {
                MemberVO member = new MemberVO();
                member.setMemberNo(memberNo);
                when(session.getAttribute("loginMember")).thenReturn(member);
        }
}