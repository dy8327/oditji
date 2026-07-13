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
import com.project.oditji.recommend.vo.RecommendPlatformSectionVO;
import com.project.oditji.search.vo.SearchResultVO;
import com.project.oditji.tmdb.service.TmdbService;

import jakarta.servlet.http.HttpSession;

@Controller
public class RecommendController {

    private static final int SECTION_CONTENT_LIMIT = 10;

    private final TmdbService tmdbService;
    private final MemberPlatformService memberPlatformService;
    private final MainContentPlatformService mainContentPlatformService;

    public RecommendController(
            TmdbService tmdbService,
            MemberPlatformService memberPlatformService,
            MainContentPlatformService mainContentPlatformService) {

        this.tmdbService = tmdbService;
        this.memberPlatformService = memberPlatformService;
        this.mainContentPlatformService = mainContentPlatformService;
    }

    @GetMapping("/recommend")
    public String recommendContent(
            Model model,
            HttpSession session) {

        MemberVO loginMember =
                (MemberVO) session.getAttribute("loginMember");

        Long memberNo = loginMember == null
                ? null
                : loginMember.getMemberNo();

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
         * 1. 평점이 높은 콘텐츠
         */
        List<SearchResultVO> highRatedContentList =
                safeList(
                        tmdbService.getMainRecommendedContent(
                                selectedPlatformNames
                        )
                );

        highRatedContentList =
                sortAndLimitByScore(
                        highRatedContentList,
                        SECTION_CONTENT_LIMIT
                );

        mainContentPlatformService.attachPlatformLogos(
                highRatedContentList,
                selectedPlatformNames
        );

        /*
         * 2. 지금 인기 있는 콘텐츠
         */
        List<SearchResultVO> popularContentList =
                safeList(
                        tmdbService.getPopularKrOttContent(
                                1,
                                selectedPlatformNames,
                                Collections.emptyList(),
                                Collections.emptyList()
                        )
                );

        popularContentList =
                sortAndLimitByPopularity(
                        popularContentList,
                        SECTION_CONTENT_LIMIT
                );

        mainContentPlatformService.attachPlatformLogos(
                popularContentList,
                selectedPlatformNames
        );

        /*
         * 3. 최근 공개된 콘텐츠
         *
         * 추천 목록과 인기 목록을 합친 뒤,
         * 공개일이 오늘 이하인 콘텐츠만 최신순으로 정렬한다.
         */
        List<SearchResultVO> newContentList =
                createNewContentList(
                        highRatedContentList,
                        popularContentList,
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

        LocalDate today = LocalDate.now();

        resultList.removeIf(content ->
                parseReleaseDate(
                        content.getReleaseDate()
                ) == null
                || parseReleaseDate(
                        content.getReleaseDate()
                ).isAfter(today)
        );

        resultList.sort((first, second) -> {

            LocalDate firstDate =
                    parseReleaseDate(
                            first.getReleaseDate()
                    );

            LocalDate secondDate =
                    parseReleaseDate(
                            second.getReleaseDate()
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

        return limitList(resultList, limit);
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
                    safeList(
                            tmdbService.getPopularKrOttContent(
                                    1,
                                    onePlatformNameList,
                                    Collections.emptyList(),
                                    Collections.emptyList()
                            )
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
