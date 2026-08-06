package com.project.oditji.common.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.project.oditji.common.util.PlatformSelectionUtil;
import com.project.oditji.member.service.MemberPlatformService;
import com.project.oditji.member.vo.MemberVO;
import com.project.oditji.member.vo.PlatformVO;
import com.project.oditji.search.service.SearchContentPageCacheService;
import com.project.oditji.search.vo.SearchResultVO;

import jakarta.servlet.http.HttpSession;

@Controller
public class HomeController {

    private static final int MAIN_POPULAR_LIMIT = 10;
    private static final int MAIN_SLIDER_LIMIT = 20;

    private final SearchContentPageCacheService
            searchContentPageCacheService;

    private final MemberPlatformService
            memberPlatformService;

    public HomeController(
            SearchContentPageCacheService
                    searchContentPageCacheService,
            MemberPlatformService memberPlatformService) {

        this.searchContentPageCacheService =
                searchContentPageCacheService;

        this.memberPlatformService =
                memberPlatformService;
    }

    @GetMapping("/")
    public String home(
            Model model,
            HttpSession session) {

        /*
         * 메인 우측 인기 콘텐츠
         *
         * TMDB API를 호출하지 않고
         * JSONL에서 적재된 공용 검색 콘텐츠를
         * 인기도 순으로 조회한다.
         */
        List<SearchResultVO> popularContentList =
                searchContentPageCacheService
                        .getMainPopularContent(
                                MAIN_POPULAR_LIMIT
                        );

        /*
         * 오늘의 콘텐츠
         *
         * 1순위: 최근 30일 이내 공개작
         * 2순위: 최근 90일 이내 공개작으로 보충
         * 3순위: 전체 인기 콘텐츠로 보충
         *
         * 모든 단계에서 인기도 내림차순,
         * 평점 내림차순으로 정렬한다.
         */
        List<SearchResultVO> todayContentList =
                searchContentPageCacheService
                        .getMainTodayContent(
                                MAIN_SLIDER_LIMIT
                        );

        MemberVO loginMember =
                (MemberVO) session.getAttribute(
                        "loginMember"
                );

        Long memberNo =
                loginMember == null
                        ? null
                        : loginMember.getMemberNo();

        List<PlatformVO> selectedPlatformList =
                memberNo == null
                        ? new ArrayList<PlatformVO>()
                        : memberPlatformService
                                .findMemberPlatformList(
                                        memberNo
                                );

        List<String> selectedPlatformNames =
                PlatformSelectionUtil.extractPlatformNames(
                        selectedPlatformList
                );

        /*
         * 추천 콘텐츠
         *
         * 로그인 회원이 OTT를 선택한 경우:
         * 선택 OTT에 해당하는 JSONL 콘텐츠만 추천한다.
         *
         * 비로그인 또는 OTT 미선택:
         * ODITJI 지원 OTT 전체를 기준으로 추천한다.
         */
        List<SearchResultVO> recommendedContentList =
                searchContentPageCacheService
                        .getMainRecommendedContent(
                                selectedPlatformNames,
                                MAIN_SLIDER_LIMIT
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

        model.addAttribute(
                "personalizedRecommendation",
                !selectedPlatformNames.isEmpty()
        );

        return "index";
    }


}
