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
import com.project.oditji.tmdb.vo.OttPlatformVO;

import jakarta.servlet.http.HttpSession;

@Controller
public class HomeController {

    private static final int MAIN_POPULAR_LIMIT = 10;
    private static final int MAIN_POPULAR_SECTION_EXTRA_LIMIT = 20;
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
         * 메인 우측 인기 콘텐츠 + 인기 콘텐츠 섹션
         *
         * TMDB API를 호출하지 않고
         * JSONL에서 적재된 공용 검색 콘텐츠를
         * 인기도 순으로 조회한다.
         *
         * 실시간 인기 콘텐츠(랭킹 TOP10)와
         * 그 아래 "인기 콘텐츠" 슬라이더가
         * 완전히 같은 10개를 중복 노출하지 않도록,
         * 넉넉히 (TOP10 + 여분) 조회한 뒤 구간을 나눠 사용한다.
         */
        List<SearchResultVO> popularContentPool =
                searchContentPageCacheService
                        .getMainPopularContent(
                                MAIN_POPULAR_LIMIT
                                        + MAIN_POPULAR_SECTION_EXTRA_LIMIT
                        );

        List<SearchResultVO> popularContentList =
                popularContentPool.size() <= MAIN_POPULAR_LIMIT
                        ? popularContentPool
                        : new ArrayList<SearchResultVO>(
                                popularContentPool.subList(
                                        0,
                                        MAIN_POPULAR_LIMIT
                                )
                        );

        List<SearchResultVO> popularSectionContentList =
                popularContentPool.size() <= MAIN_POPULAR_LIMIT
                        ? new ArrayList<SearchResultVO>()
                        : new ArrayList<SearchResultVO>(
                                popularContentPool.subList(
                                        MAIN_POPULAR_LIMIT,
                                        popularContentPool.size()
                                )
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

        /*
         * 신규 콘텐츠
         *
         * 공개일(releaseDate) 내림차순으로 정렬한
         * 최신 콘텐츠를 노출한다. "오늘의 콘텐츠"처럼
         * 최근 30/90일로 필터링해 보충하지 않고,
         * 전체 콘텐츠를 최신순으로 정렬해 그대로 사용한다.
         */
        List<SearchResultVO> newContentList =
                searchContentPageCacheService
                        .getMainNewContent(
                                MAIN_SLIDER_LIMIT
                        );

        /*
         * 브랜드 소개 슬라이드(오른쪽 여백)에 노출할 신뢰 지표.
         *
         * 지원 OTT는 텍스트 개수 대신 실제 로고를 그대로 노출해
         * 어떤 플랫폼을 통합 검색하는지 한눈에 보여준다.
         *
         * 콘텐츠 수는 실제 값을 그대로 노출하면 갱신될 때마다
         * 문구가 자잘하게 흔들려 보이므로, 100 단위로 내림한 뒤
         * "N,000+" 형태로 표시한다.
         */
        List<OttPlatformVO> featuredPlatformList =
                searchContentPageCacheService
                        .getFeaturedPlatformList();

        int rawContentCount =
                searchContentPageCacheService
                        .getTotalContentCount();

        int roundedContentCount =
                rawContentCount >= 100
                        ? (rawContentCount / 100) * 100
                        : rawContentCount;

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
                "popularSectionContentList",
                popularSectionContentList
        );

        model.addAttribute(
                "todayContentList",
                todayContentList
        );

        model.addAttribute(
                "newContentList",
                newContentList
        );

        model.addAttribute(
                "featuredPlatformList",
                featuredPlatformList
        );

        model.addAttribute(
                "roundedContentCount",
                roundedContentCount
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
