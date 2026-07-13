package com.project.oditji.common.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.project.oditji.common.service.MainContentPlatformService;
import com.project.oditji.member.service.MemberPlatformService;
import com.project.oditji.member.vo.MemberVO;
import com.project.oditji.member.vo.PlatformVO;
import com.project.oditji.search.vo.SearchResultVO;
import com.project.oditji.tmdb.service.TmdbService;

import jakarta.servlet.http.HttpSession;

@Controller
public class HomeController {

    private final TmdbService tmdbService;
    private final MemberPlatformService memberPlatformService;
    private final MainContentPlatformService mainContentPlatformService;

    public HomeController(
            TmdbService tmdbService,
            MemberPlatformService memberPlatformService,
            MainContentPlatformService mainContentPlatformService) {

        this.tmdbService = tmdbService;
        this.memberPlatformService = memberPlatformService;
        this.mainContentPlatformService = mainContentPlatformService;
    }

    @GetMapping("/")
    public String main(
            Model model,
            HttpSession session) {

        /*
         * 메인 우측 인기 콘텐츠
         */
        List<SearchResultVO> popularContentList =
                tmdbService.getMainPopularContent();

        /*
         * 오늘의 콘텐츠
         */
        List<SearchResultVO> todayContentList =
                tmdbService.getMainTodayContent();

        /*
         * 로그인 회원 확인
         */
        MemberVO loginMember =
                (MemberVO) session.getAttribute("loginMember");

        Long memberNo = loginMember == null
                ? null
                : loginMember.getMemberNo();

        /*
         * 회원이 선택한 OTT 조회
         *
         * 비로그인 회원은 빈 목록으로 처리한다.
         */
        List<PlatformVO> selectedPlatformList =
                memberNo == null
                        ? new ArrayList<PlatformVO>()
                        : memberPlatformService.findMemberPlatformList(
                                memberNo
                        );

        /*
         * TmdbService에 전달할 플랫폼 이름 목록
         */
        List<String> selectedPlatformNames =
                new ArrayList<String>();

        for (PlatformVO platform : selectedPlatformList) {

            if (platform == null
                    || platform.getPlatformName() == null
                    || platform.getPlatformName().isBlank()) {
                continue;
            }

            selectedPlatformNames.add(
                    platform.getPlatformName()
            );
        }

        /*
         * 추천 콘텐츠
         *
         * 로그인 + OTT 선택:
         * 회원이 선택한 OTT 기준
         *
         * 비로그인 또는 OTT 미선택:
         * 지원 OTT 전체 기준
         */
        List<SearchResultVO> recommendedContentList =
                tmdbService.getMainRecommendedContent(
                        selectedPlatformNames
                );

        /*
         * 오늘의 콘텐츠 OTT 로고
         *
         * 빈 플랫폼 목록을 전달하면
         * ODITJI 지원 OTT 전체 중 실제 제공되는 OTT를 표시한다.
         */
        mainContentPlatformService.attachPlatformLogos(
                todayContentList,
                new ArrayList<String>()
        );

        /*
         * 추천 콘텐츠 OTT 로고
         *
         * 회원이 선택한 OTT가 있다면
         * 실제 제공 OTT와 회원 선택 OTT의 교집합만 표시한다.
         */
        mainContentPlatformService.attachPlatformLogos(
                recommendedContentList,
                selectedPlatformNames
        );

        model.addAttribute(
                "popularContentList",
                popularContentList
        );

        model.addAttribute(
                "todayContentList",
                todayContentList
        );

        model.addAttribute(
                "recommendedContentList",
                recommendedContentList
        );

        model.addAttribute(
                "selectedPlatformList",
                selectedPlatformList
        );

        /*
         * index.jsp의 추천 설명 문구 분기용
         */
        model.addAttribute(
                "personalizedRecommendation",
                !selectedPlatformNames.isEmpty()
        );

        return "index";
    }
}