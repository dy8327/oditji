package com.project.oditji.review.controller;

import java.util.List;

import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

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
            @RequestParam String reviewText) {

        MemberVO loginMember = (MemberVO) session.getAttribute("loginMember");

        if (loginMember == null) {
            return "redirect:/member/login";
        }

        reviewService.writeContentReview(
                loginMember.getMemberNo(),
                contentNo,
                rating,
                reviewText);

        return "redirect:/content/contentDetail/" + contentNo;
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
            @RequestParam String reviewText) {

        MemberVO loginMember = (MemberVO) session.getAttribute("loginMember");

        if (loginMember == null) {
            return "redirect:/member/login";
        }

        reviewService.updateContentReview(
                loginMember.getMemberNo(),
                reviewNo,
                rating,
                reviewText);

        return "redirect:/content/contentDetail/" + contentNo;
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
            @RequestParam Long reviewNo) {

        MemberVO loginMember = (MemberVO) session.getAttribute("loginMember");

        if (loginMember == null) {
            return "redirect:/member/login";
        }

        reviewService.deleteContentReview(
                loginMember.getMemberNo(),
                reviewNo);

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