package com.project.oditji.subscription.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.project.oditji.search.service.SearchContentPageCacheService;
import com.project.oditji.search.vo.SearchResultPageVO;
import com.project.oditji.search.vo.SearchResultVO;
import com.project.oditji.subscription.service.SubscriptionCalculatorService;
import com.project.oditji.subscription.vo.ContentWishItemVO;
import com.project.oditji.subscription.vo.SubscriptionCalculationResultVO;
import com.project.oditji.tmdb.vo.OttPlatformVO;

@RestController
@RequestMapping("/api/subscription")
public class SubscriptionApiController {

    /** 위시리스트에 담기 전 미리보기용 검색 결과 개수 (자동완성 성격이라 적게 유지) */
    private static final int SEARCH_RESULT_SIZE = 8;

    private final SearchContentPageCacheService searchContentPageCacheService;

    private final SubscriptionCalculatorService subscriptionCalculatorService;

    public SubscriptionApiController(
            SearchContentPageCacheService searchContentPageCacheService,
            SubscriptionCalculatorService subscriptionCalculatorService) {

        this.searchContentPageCacheService =
                searchContentPageCacheService;

        this.subscriptionCalculatorService =
                subscriptionCalculatorService;
    }

    /**
     * 위시리스트에 담을 콘텐츠를 검색한다. 콘텐츠의 플랫폼 목록을 함께 내려줘서,
     * 프런트가 담은 항목을 그대로 /calculate 요청 본문으로 재사용할 수 있게 한다.
     */
    @GetMapping("/search-content")
    public List<ContentWishItemVO> searchContent(
            @RequestParam(required = false) String keyword) {

        if (keyword == null
                || keyword.trim().isEmpty()) {

            return Collections.emptyList();
        }

        SearchResultPageVO page =
                searchContentPageCacheService.getContentPage(
                        keyword,
                        1,
                        SEARCH_RESULT_SIZE,
                        Collections.emptyList(),
                        Collections.emptyList(),
                        Collections.emptyList());

        List<ContentWishItemVO> wishItemList =
                new ArrayList<ContentWishItemVO>();

        if (page == null
                || page.getResultList() == null) {

            return wishItemList;
        }

        for (SearchResultVO content : page.getResultList()) {

            wishItemList.add(
                    toWishItem(content));
        }

        return wishItemList;
    }

    /** 위시리스트를 가장 저렴하게 커버하는 OTT 구독 조합을 계산한다. */
    @PostMapping("/calculate")
    public SubscriptionCalculationResultVO calculate(
            @RequestBody List<ContentWishItemVO> wishItemList) {

        return subscriptionCalculatorService.calculate(
                wishItemList);
    }

    private ContentWishItemVO toWishItem(
            SearchResultVO content) {

        ContentWishItemVO wishItem =
                new ContentWishItemVO();

        wishItem.setTmdbId(content.getTmdbId());
        wishItem.setContentType(content.getContentType());
        wishItem.setTitle(content.getTitle());
        wishItem.setPosterPath(content.getPosterPath());

        List<String> platformNameList =
                new ArrayList<String>();

        if (content.getPlatformList() != null) {

            for (OttPlatformVO platform : content.getPlatformList()) {

                if (platform != null
                        && platform.getPlatformName() != null) {

                    platformNameList.add(
                            platform.getPlatformName());
                }
            }
        }

        wishItem.setPlatformNameList(platformNameList);

        return wishItem;
    }
}
