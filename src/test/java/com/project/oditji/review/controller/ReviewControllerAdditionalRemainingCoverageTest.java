package com.project.oditji.review.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
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

/**
 * 리뷰 목록 타입 정규화와 상품 리뷰 IllegalStateException 분기를 보완합니다.
 */
@ExtendWith(MockitoExtension.class)
class ReviewControllerAdditionalRemainingCoverageTest {

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
    void myReviewListShouldFilterContentProductAndInvalidTypes() {
        login(1L);

        MyReviewVO content = review("CONTENT");
        MyReviewVO product = review("PRODUCT");

        when(reviewService.getMyReviewList(1L))
                .thenReturn(List.of(content, product));

        ExtendedModelMap contentModel = new ExtendedModelMap();

        assertEquals(
                "review/myReviewList",
                controller.myReviewList(
                        1,
                        "content",
                        session,
                        contentModel));

        @SuppressWarnings("unchecked")
        List<MyReviewVO> contentList =
                (List<MyReviewVO>)
                        contentModel.get("reviewList");

        assertEquals(1, contentList.size());
        assertEquals("CONTENT", contentModel.get("reviewType"));
        assertEquals(2, contentModel.get("allReviewCount"));
        assertEquals(1L, contentModel.get("contentReviewCount"));
        assertEquals(1L, contentModel.get("productReviewCount"));

        ExtendedModelMap productModel = new ExtendedModelMap();

        controller.myReviewList(
                1,
                "PRODUCT",
                session,
                productModel);

        @SuppressWarnings("unchecked")
        List<MyReviewVO> productList =
                (List<MyReviewVO>)
                        productModel.get("reviewList");

        assertEquals(1, productList.size());
        assertEquals("PRODUCT", productModel.get("reviewType"));

        ExtendedModelMap invalidModel = new ExtendedModelMap();

        controller.myReviewList(
                1,
                "UNKNOWN",
                session,
                invalidModel);

        @SuppressWarnings("unchecked")
        List<MyReviewVO> allList =
                (List<MyReviewVO>)
                        invalidModel.get("reviewList");

        assertEquals(2, allList.size());
        assertEquals("ALL", invalidModel.get("reviewType"));
    }

    @Test
    void productReviewIllegalStateExceptionShouldUseSameSafeRedirect() {
        login(2L);

        doThrow(new IllegalStateException("이미 작성했습니다."))
                .when(reviewService)
                .writeProductReview(
                        2L,
                        20,
                        30,
                        4.0,
                        "리뷰");

        assertEquals(
                "redirect:/order/list?page=3",
                controller.writeProductReview(
                        session,
                        20,
                        30,
                        4.0,
                        "리뷰",
                        3,
                        redirectAttributes));

        verify(redirectAttributes)
                .addFlashAttribute(
                        "reviewMessage",
                        "이미 작성했습니다.");
    }

    private void login(Long memberNo) {
        MemberVO member = new MemberVO();
        member.setMemberNo(memberNo);
        when(session.getAttribute("loginMember"))
                .thenReturn(member);
    }

    private MyReviewVO review(String type) {
        MyReviewVO review = new MyReviewVO();
        review.setReviewType(type);
        return review;
    }
}
