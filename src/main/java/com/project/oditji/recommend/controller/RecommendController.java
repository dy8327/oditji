package com.project.oditji.recommend.controller;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.project.oditji.common.service.MainContentPlatformService;
import com.project.oditji.member.service.MemberPlatformService;
import com.project.oditji.member.vo.MemberVO;
import com.project.oditji.member.vo.PlatformVO;
import com.project.oditji.recommend.service.RecommendService;
import com.project.oditji.recommend.vo.RecommendOttResultVO;
import com.project.oditji.recommend.vo.RecommendPlatformSectionVO;
import com.project.oditji.search.service.SearchContentPageCacheService;
import com.project.oditji.search.vo.SearchResultPageVO;
import com.project.oditji.search.vo.SearchResultVO;

import jakarta.servlet.http.HttpSession;

@Controller
public class RecommendController {

    private static final int SECTION_CONTENT_LIMIT = 10;
    private static final int CACHE_FETCH_SIZE = 100;
    private static final int NEW_CONTENT_MONTHS = 1;

    private final SearchContentPageCacheService searchContentPageCacheService;
    private final MemberPlatformService memberPlatformService;
    private final MainContentPlatformService mainContentPlatformService;
    private final RecommendService recommendService;

    public RecommendController(
            SearchContentPageCacheService searchContentPageCacheService,
            MemberPlatformService memberPlatformService,
            MainContentPlatformService mainContentPlatformService,
            RecommendService recommendService) {

        this.searchContentPageCacheService = searchContentPageCacheService;
        this.memberPlatformService = memberPlatformService;
        this.mainContentPlatformService = mainContentPlatformService;
        this.recommendService = recommendService;
    }

    @GetMapping("/recommend")
    public String recommendContent(
            Model model,
            HttpSession session) {

        MemberVO loginMember =
                (MemberVO) session.getAttribute("loginMember");

        boolean loggedIn =
                loginMember != null;

        Long memberNo = loggedIn
                ? loginMember.getMemberNo()
                : null;

        List<PlatformVO> selectedPlatformList =
                memberNo == null
                        ? new ArrayList<PlatformVO>()
                        : memberPlatformService.findMemberPlatformList(
                                memberNo
                        );

        List<String> selectedPlatformNames =
                extractPlatformNames(
                        selectedPlatformList
                );

        boolean personalizedRecommendation =
                !selectedPlatformNames.isEmpty();

        /*
         * 최근 30일 상세 조회 이력과 현재 찜 콘텐츠를 기준으로
         * 회원에게 가장 적합한 OTT를 계산합니다.
         *
         * 비로그인 사용자는 RecommendService에서
         * 로그인 안내 상태를 담은 결과를 반환합니다.
         */
        RecommendOttResultVO ottRecommendation =
                recommendService.getOttRecommendation(
                        memberNo
                );

        /*
         * 1. 평점이 높은 콘텐츠
         */
        List<SearchResultVO> highRatedCandidateList =
                searchContentPageCacheService
                        .getMainRecommendedContent(
                                selectedPlatformNames,
                                CACHE_FETCH_SIZE
                        );

        List<SearchResultVO> highRatedContentList =
                sortAndLimitByScore(
                        highRatedCandidateList,
                        SECTION_CONTENT_LIMIT
                );

        mainContentPlatformService.attachPlatformLogos(
                highRatedContentList,
                selectedPlatformNames
        );

        /*
         * 2. 지금 인기 있는 콘텐츠
         */
        List<SearchResultVO> popularCandidateList =
                getCachedPopularContent(
                        selectedPlatformNames
                );

        List<SearchResultVO> popularContentList =
                sortAndLimitByPopularity(
                        popularCandidateList,
                        SECTION_CONTENT_LIMIT
                );

        mainContentPlatformService.attachPlatformLogos(
                popularContentList,
                selectedPlatformNames
        );

        /*
         * 3. 최근 공개된 콘텐츠
         *
         * 영화는 개봉일, TV는 최근 회차 공개일을 기준으로
         * 오늘부터 최근 한 달 이내 콘텐츠만 신작으로 표시합니다.
         *
         * 화면용 10건으로 먼저 줄이면 최근 콘텐츠가 누락될 수 있으므로
         * 원본 100건 후보 목록을 합쳐 신작 목록을 만듭니다.
         */
        List<SearchResultVO> newContentList =
                createNewContentList(
                        highRatedCandidateList,
                        popularCandidateList,
                        SECTION_CONTENT_LIMIT
                );

        mainContentPlatformService.attachPlatformLogos(
                newContentList,
                selectedPlatformNames
        );

        /*
         * 4. 회원이 선택한 OTT별 추천
         *
         * 비로그인 또는 OTT 미선택 상태에서는 만들지 않는다.
         */
        List<RecommendPlatformSectionVO> platformSectionList =
                personalizedRecommendation
                        ? createPlatformSectionList(
                                selectedPlatformList
                        )
                        : new ArrayList<RecommendPlatformSectionVO>();

        model.addAttribute(
                "loggedIn",
                loggedIn
        );

        /*
         * 추천 콘텐츠 페이지 상단의 맞춤 OTT 영역에서 사용합니다.
         */
        model.addAttribute(
                "ottRecommendation",
                ottRecommendation
        );

        model.addAttribute(
                "selectedPlatformList",
                selectedPlatformList
        );

        model.addAttribute(
                "personalizedRecommendation",
                personalizedRecommendation
        );

        model.addAttribute(
                "highRatedContentList",
                highRatedContentList
        );

        model.addAttribute(
                "popularContentList",
                popularContentList
        );

        model.addAttribute(
                "newContentList",
                newContentList
        );

        model.addAttribute(
                "platformSectionList",
                platformSectionList
        );

        return "recommend/recommendContent";
    }

    /**
     * JSON 공용 검색 캐시에서 인기 콘텐츠를 조회합니다.
     *
     * platformValues에는 Netflix, TVING 등의 플랫폼 이름이 전달되며,
     * SearchContentPageCacheService에서 내부 플랫폼 키로 정규화합니다.
     */
    private List<SearchResultVO> getCachedPopularContent(
            List<String> platformValues) {

        SearchResultPageVO pageVO =
                searchContentPageCacheService.getContentPage(
                        "",
                        1,
                        CACHE_FETCH_SIZE,
                        Collections.emptyList(),
                        Collections.emptyList(),
                        platformValues
                );

        return pageVO == null
                ? new ArrayList<SearchResultVO>()
                : safeList(
                        pageVO.getResultList()
                );
    }

    private List<String> extractPlatformNames(
            List<PlatformVO> platformList) {

        List<String> platformNames =
                new ArrayList<String>();

        if (platformList == null) {
            return platformNames;
        }

        for (PlatformVO platform : platformList) {

            if (platform == null
                    || platform.getPlatformName() == null
                    || platform.getPlatformName().isBlank()) {
                continue;
            }

            if (!platformNames.contains(
                    platform.getPlatformName())) {

                platformNames.add(
                        platform.getPlatformName()
                );
            }
        }

        return platformNames;
    }

    private List<SearchResultVO> safeList(
            List<SearchResultVO> sourceList) {

        return sourceList == null
                ? new ArrayList<SearchResultVO>()
                : new ArrayList<SearchResultVO>(
                        sourceList
                );
    }

    private List<SearchResultVO> sortAndLimitByScore(
            List<SearchResultVO> sourceList,
            int limit) {

        List<SearchResultVO> resultList =
                distinctContentList(sourceList);

        resultList.sort(
                Comparator
                        .comparing(
                                SearchResultVO::getTmdbScore,
                                Comparator.nullsLast(
                                        Comparator.reverseOrder()
                                )
                        )
                        .thenComparing(
                                SearchResultVO::getPopularity,
                                Comparator.nullsLast(
                                        Comparator.reverseOrder()
                                )
                        )
        );

        return limitList(resultList, limit);
    }

    private List<SearchResultVO> sortAndLimitByPopularity(
            List<SearchResultVO> sourceList,
            int limit) {

        List<SearchResultVO> resultList =
                distinctContentList(sourceList);

        resultList.sort(
                Comparator
                        .comparing(
                                SearchResultVO::getPopularity,
                                Comparator.nullsLast(
                                        Comparator.reverseOrder()
                                )
                        )
                        .thenComparing(
                                SearchResultVO::getTmdbScore,
                                Comparator.nullsLast(
                                        Comparator.reverseOrder()
                                )
                        )
        );

        return limitList(resultList, limit);
    }

    /**
     * 추천 화면의 신작 콘텐츠 목록을 생성합니다.
     *
     * 영화는 최초 개봉일을 사용하고,
     * TV는 마지막으로 방영된 회차의 공개일을 사용합니다.
     * 기준일이 최근 한 달 범위를 벗어나거나 미래인 콘텐츠는 제외합니다.
     */
    private List<SearchResultVO> createNewContentList(
            List<SearchResultVO> highRatedContentList,
            List<SearchResultVO> popularContentList,
            int limit) {

        List<SearchResultVO> mergedList =
                new ArrayList<SearchResultVO>();

        mergedList.addAll(
                safeList(highRatedContentList)
        );

        mergedList.addAll(
                safeList(popularContentList)
        );

        List<SearchResultVO> resultList =
                distinctContentList(mergedList);

        LocalDate today =
                LocalDate.now();

        LocalDate startDate =
                today.minusMonths(
                        NEW_CONTENT_MONTHS
                );

        resultList.removeIf(content -> {

            LocalDate recentDate =
                    resolveRecentContentDate(
                            content
                    );

            return recentDate == null
                    || recentDate.isBefore(startDate)
                    || recentDate.isAfter(today);
        });

        resultList.sort((first, second) -> {

            LocalDate firstDate =
                    resolveRecentContentDate(
                            first
                    );

            LocalDate secondDate =
                    resolveRecentContentDate(
                            second
                    );

            int dateCompare =
                    secondDate.compareTo(firstDate);

            if (dateCompare != 0) {
                return dateCompare;
            }

            return compareNullableDoubleDescending(
                    first.getPopularity(),
                    second.getPopularity()
            );
        });

        return limitList(
                resultList,
                limit
        );
    }

    /**
     * 신작 판단에 사용할 날짜를 콘텐츠 유형별로 반환합니다.
     *
     * 영화는 개봉일, TV는 최근 회차 공개일을 사용합니다.
     * 최근 회차 날짜가 아직 없는 TV는 신작 목록에서 제외하기 위해 null을 반환합니다.
     */
    private LocalDate resolveRecentContentDate(
            SearchResultVO content) {

        if (content == null) {
            return null;
        }

        String dateText =
                "TV".equalsIgnoreCase(
                        content.getContentType()
                )
                        ? content.getLastAirDate()
                        : content.getReleaseDate();

        return parseReleaseDate(
                dateText
        );
    }

    private List<RecommendPlatformSectionVO> createPlatformSectionList(
            List<PlatformVO> selectedPlatformList) {

        List<RecommendPlatformSectionVO> sectionList =
                new ArrayList<RecommendPlatformSectionVO>();

        if (selectedPlatformList == null) {
            return sectionList;
        }

        for (PlatformVO platform : selectedPlatformList) {

            if (platform == null
                    || platform.getPlatformName() == null
                    || platform.getPlatformName().isBlank()) {
                continue;
            }

            List<String> onePlatformNameList =
                    Collections.singletonList(
                            platform.getPlatformName()
                    );

            List<SearchResultVO> contentList =
                    getCachedPopularContent(
                            onePlatformNameList
                    );

            contentList =
                    sortAndLimitByPopularity(
                            contentList,
                            SECTION_CONTENT_LIMIT
                    );

            mainContentPlatformService.attachPlatformLogos(
                    contentList,
                    onePlatformNameList
            );

            if (contentList.isEmpty()) {
                continue;
            }

            RecommendPlatformSectionVO section =
                    new RecommendPlatformSectionVO();

            section.setPlatformNo(
                    platform.getPlatformNo()
            );

            section.setPlatformName(
                    platform.getPlatformName()
            );

            section.setLogoImage(
                    platform.getLogoImage()
            );

            section.setContentList(
                    contentList
            );

            sectionList.add(section);
        }

        return sectionList;
    }

    private List<SearchResultVO> distinctContentList(
            List<SearchResultVO> sourceList) {

        Map<String, SearchResultVO> uniqueMap =
                new LinkedHashMap<String, SearchResultVO>();

        if (sourceList == null) {
            return new ArrayList<SearchResultVO>();
        }

        for (SearchResultVO content : sourceList) {

            if (content == null
                    || content.getTmdbId() == null
                    || content.getContentType() == null) {
                continue;
            }

            String key =
                    content.getContentType()
                    + ":"
                    + content.getTmdbId();

            uniqueMap.putIfAbsent(
                    key,
                    content
            );
        }

        return new ArrayList<SearchResultVO>(
                uniqueMap.values()
        );
    }

    private List<SearchResultVO> limitList(
            List<SearchResultVO> sourceList,
            int limit) {

        if (sourceList == null
                || sourceList.isEmpty()) {
            return new ArrayList<SearchResultVO>();
        }

        if (sourceList.size() <= limit) {
            return sourceList;
        }

        return new ArrayList<SearchResultVO>(
                sourceList.subList(
                        0,
                        limit
                )
        );
    }

    private LocalDate parseReleaseDate(
            String releaseDate) {

        if (releaseDate == null
                || releaseDate.isBlank()) {
            return null;
        }

        try {

            return LocalDate.parse(
                    releaseDate.trim()
            );

        } catch (Exception e) {

            return null;
        }
    }

    private int compareNullableDoubleDescending(
            Double first,
            Double second) {

        if (first == null && second == null) {
            return 0;
        }

        if (first == null) {
            return 1;
        }

        if (second == null) {
            return -1;
        }

        return Double.compare(
                second,
                first
        );
    }
}