package com.project.oditji.review.controller;

import java.util.List;

import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.project.oditji.member.vo.MemberVO;
import com.project.oditji.review.service.ReviewService;
import com.project.oditji.review.vo.MyReviewVO;

@Controller
@RequestMapping("/review")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    /**
     * 마이페이지 내가 작성한 리뷰 목록
     */
    @GetMapping("/myReviewList")
    public String myReviewList(
            HttpSession session,
            Model model) {

        MemberVO loginMember = (MemberVO) session.getAttribute("loginMember");

        if (loginMember == null) {
            return "redirect:/member/login";
        }

        List<MyReviewVO> reviewList = reviewService.getMyReviewList(
                loginMember.getMemberNo());

        model.addAttribute(
                "reviewList",
                reviewList);

        return "review/myReviewList";
    }

    /**
     * 콘텐츠 리뷰 작성
     */
    @PostMapping("/write")
    public String writeContentReview(
            HttpSession session,
            @RequestParam int contentNo,
            @RequestParam double rating,
            @RequestParam String reviewText,
            // [추가] 체크하지 않은 경우 N으로 처리한다.
            @RequestParam(defaultValue = "N") String spoilerYn,
            // [수정] 동일 콘텐츠 중복 작성 안내 메시지를 상세 페이지로 전달한다.
            RedirectAttributes redirectAttributes) {

        MemberVO loginMember = (MemberVO) session.getAttribute("loginMember");

        if (loginMember == null) {
            return "redirect:/member/login";
        }

        try {
            reviewService.writeContentReview(
                    loginMember.getMemberNo(),
                    contentNo,
                    rating,
                    reviewText,
                    // [추가] 사용자가 선택한 스포일러 포함 여부
                    spoilerYn);
        } catch (IllegalStateException e) {
            // [수정] 서비스의 중복 리뷰 예외를 사용자가 확인할 수 있는 알림 문구로 변경한다.
            redirectAttributes.addFlashAttribute(
                    "reviewAlertMessage",
                    "계정 하나당 리뷰 1개만 작성이 가능합니다");
        }

        return "redirect:/content/contentDetail/" + contentNo + "#reviewSection";
    }

    /**
     * 콘텐츠 리뷰 수정
     */
    @PostMapping("/update")
    public String updateContentReview(
            HttpSession session,
            @RequestParam Long reviewNo,
            @RequestParam int contentNo,
            @RequestParam double rating,
            @RequestParam String reviewText,
            // [추가] 체크하지 않은 경우 N으로 처리한다.
            @RequestParam(defaultValue = "N") String spoilerYn) {

        MemberVO loginMember = (MemberVO) session.getAttribute("loginMember");

        if (loginMember == null) {
            return "redirect:/member/login";
        }

        reviewService.updateContentReview(
                loginMember.getMemberNo(),
                reviewNo,
                // [추가] 수정 리뷰의 줄거리 비교에 사용
                contentNo,
                rating,
                reviewText,
                // [추가] 사용자가 선택한 스포일러 포함 여부
                spoilerYn);

        return "redirect:/content/contentDetail/" + contentNo + "#reviewSection";
    }

    /**
     * 상품 리뷰 작성
     */
    @PostMapping("/writeProductReview")
    public String writeProductReview(
            HttpSession session,
            @RequestParam int productNo,
            @RequestParam int orderItemNo,
            @RequestParam double rating,
            @RequestParam String content) {

        MemberVO loginMember = (MemberVO) session.getAttribute("loginMember");

        if (loginMember == null) {
            return "redirect:/member/login";
        }

        reviewService.writeProductReview(
                loginMember.getMemberNo(),
                productNo,
                orderItemNo,
                rating,
                content);

        return "redirect:/order/list";
    }

    /**
     * 콘텐츠 리뷰 삭제
     */
    @PostMapping("/deleteContentReview")
    public String deleteContentReview(
            HttpSession session,
            @RequestParam Long reviewNo,
            // [수정] 콘텐츠 상세 페이지에서 삭제한 경우 동일 페이지로 돌아가기 위해 사용한다.
            @RequestParam(required = false) Integer contentNo) {

        MemberVO loginMember = (MemberVO) session.getAttribute("loginMember");

        if (loginMember == null) {
            return "redirect:/member/login";
        }

        reviewService.deleteContentReview(
                loginMember.getMemberNo(),
                reviewNo);

        // [수정] 콘텐츠 상세 페이지에서 삭제한 경우 리뷰 영역으로 즉시 돌아간다.
        if (contentNo != null) {
            return "redirect:/content/contentDetail/" + contentNo + "#reviewSection";
        }

        return "redirect:/review/myReviewList";
    }

    /**
     * 상품 리뷰 삭제
     */
    @PostMapping("/deleteProductReview")
    public String deleteProductReview(
            HttpSession session,
            @RequestParam Long reviewNo) {

        MemberVO loginMember = (MemberVO) session.getAttribute("loginMember");

        if (loginMember == null) {
            return "redirect:/member/login";
        }

        reviewService.deleteProductReview(
                loginMember.getMemberNo(),
                reviewNo);

        return "redirect:/review/myReviewList";
    }

}