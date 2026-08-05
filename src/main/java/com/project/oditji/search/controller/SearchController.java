package com.project.oditji.search.controller;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.project.oditji.common.util.PlatformNameNormalizer;
import com.project.oditji.goods.service.GoodsService;
import com.project.oditji.goods.vo.GoodsVO;
import com.project.oditji.member.vo.MemberVO;
import com.project.oditji.search.service.SearchContentPageCacheService;
import com.project.oditji.search.vo.SearchResultPageVO;
import com.project.oditji.search.vo.SearchResultVO;
import com.project.oditji.search.vo.SearchVO;
import com.project.oditji.tmdb.dao.TmdbDAO;
import com.project.oditji.tmdb.vo.OttPlatformVO;
import com.project.oditji.wish.service.WishService;

import jakarta.servlet.http.HttpSession;

@Controller
public class SearchController {

    private static final int CONTENT_PAGE_SIZE = 10;
    private static final int GOODS_PAGE_SIZE = 12;

    private static final int ALL_CONTENT_PREVIEW_SIZE = 5;
    private static final int ALL_GOODS_PREVIEW_SIZE = 5;
    private static final String SEARCH_TAB_CONTENT = "CONTENT";
    private static final String SEARCH_TAB_GOODS = "GOODS";

    private final SearchContentPageCacheService searchContentPageCacheService;
    private final GoodsService goodsService;
    private final TmdbDAO tmdbDAO;
    private final WishService wishService;

    @Value("${tmdb.api.image-base-url}")
    private String imageBaseUrl;

    public SearchController(
            SearchContentPageCacheService searchContentPageCacheService,
            GoodsService goodsService,
            TmdbDAO tmdbDAO,
            WishService wishService) {

        this.searchContentPageCacheService =
                searchContentPageCacheService;

        this.goodsService =
                goodsService;

        this.tmdbDAO =
                tmdbDAO;

        this.wishService =
                wishService;
    }

    @GetMapping("/search")
    public String searchResult(
            SearchVO searchVO,
            HttpSession session,
            Model model) {

        if (searchVO == null) {
            searchVO = new SearchVO();
        }

        String keyword =
                normalizeKeyword(
                        searchVO.getKeyword()
                );

        int contentPage =
                normalizePage(
                        searchVO.getContentPage()
                );

        int goodsPage =
                normalizePage(
                        searchVO.getGoodsPage()
                );

        List<String> contentCategories =
                normalizeContentCategories(
                        searchVO.getContentCategories()
                );

        List<String> genreCodes =
                createSafeList(
                        searchVO.getGenreCodes()
                );

        List<String> providerIds =
                createSafeList(
                        searchVO.getProviderIds()
                );

        List<String> ageRatings =
                createSafeList(
                        searchVO.getAgeRatings()
                );

        List<String> productTypes =
                createSafeList(
                        searchVO.getProductTypes()
                );

        Integer minPrice =
                normalizePrice(
                        searchVO.getMinPrice()
                );

        Integer maxPrice =
                normalizePrice(
                        searchVO.getMaxPrice()
                );

        if (minPrice != null
                && maxPrice != null
                && minPrice > maxPrice) {

            int temporaryPrice = minPrice;

            minPrice = maxPrice;
            maxPrice = temporaryPrice;
        }

        boolean discountOnly =
                searchVO.isDiscountOnly();

        boolean inStockOnly =
                searchVO.isInStockOnly();

        String searchTab =
                normalizeSearchTab(
                        searchVO.getSearchTab()
                );

        /*
         * 검색어가 없어도 상품을 조회한다.
         *
         * keyword가 빈 문자열이면 goodsMapper.xml에서
         * 상품명 LIKE 조건을 적용하지 않고
         * ON_SALE 상품 전체를 조회한다.
         */
        int goodsTotalCount =
                goodsService.countSearchGoods(
                        keyword,
                        productTypes,
                        minPrice,
                        maxPrice,
                        discountOnly,
                        inStockOnly
                );

        int goodsTotalPages =
                calculateTotalPages(
                        goodsTotalCount,
                        GOODS_PAGE_SIZE
                );

        if (goodsTotalPages > 0
                && goodsPage > goodsTotalPages) {

            goodsPage = goodsTotalPages;
        }

        searchVO.setKeyword(keyword);
        searchVO.setContentPage(contentPage);
        searchVO.setGoodsPage(goodsPage);

        searchVO.setContentCategories(
                contentCategories
        );

        searchVO.setGenreCodes(
                genreCodes
        );

        searchVO.setProviderIds(
                providerIds
        );

        searchVO.setAgeRatings(
                ageRatings
        );

        searchVO.setProductTypes(
                productTypes
        );

        searchVO.setMinPrice(
                minPrice
        );

        searchVO.setMaxPrice(
                maxPrice
        );

        searchVO.setDiscountOnly(
                discountOnly
        );

        searchVO.setInStockOnly(
                inStockOnly
        );

        searchVO.setSearchTab(
                searchTab
        );

        /*
         * 콘텐츠 조회
         *
         * 검색어가 비어 있으면 인기 콘텐츠가 조회된다.
         */
        SearchResultPageVO contentPageVO =
                searchContentPageCacheService.getContentPage(
                        keyword,
                        contentPage,
                        CONTENT_PAGE_SIZE,
                        contentCategories,
                        genreCodes,
                        providerIds,
                        ageRatings
                );

        List<SearchResultVO> contentResults =
                contentPageVO.getResultList() == null
                        ? new ArrayList<SearchResultVO>()
                        : contentPageVO.getResultList();

        /*
         * 전체 탭용 콘텐츠 미리보기
         */
        List<SearchResultVO> allContentResults =
                searchContentPageCacheService.getFirstPagePreview(
                        keyword,
                        ALL_CONTENT_PREVIEW_SIZE,
                        CONTENT_PAGE_SIZE,
                        contentCategories,
                        genreCodes,
                        providerIds,
                        ageRatings
                );

        if (allContentResults == null) {
            allContentResults =
                    new ArrayList<SearchResultVO>();
        }

        /*
         * 상품 탭용 상품 목록
         *
         * 검색어가 없어도 ON_SALE 상품을 조회한다.
         */
        List<GoodsVO> goodsResults =
                new ArrayList<GoodsVO>();

        if (goodsTotalCount > 0) {

            goodsResults =
                    goodsService.searchGoods(
                            keyword,
                            productTypes,
                            minPrice,
                            maxPrice,
                            discountOnly,
                            inStockOnly,
                            goodsPage,
                            GOODS_PAGE_SIZE
                    );
        }

        if (goodsResults == null) {
            goodsResults =
                    new ArrayList<GoodsVO>();
        }

        /*
         * 전체 탭용 상품 미리보기
         *
         * 검색어가 없어도 최신 판매 상품 5개를 조회한다.
         */
        List<GoodsVO> allGoodsResults =
                new ArrayList<GoodsVO>();

        if (goodsTotalCount > 0) {

            allGoodsResults =
                    goodsService.searchGoods(
                            keyword,
                            productTypes,
                            minPrice,
                            maxPrice,
                            discountOnly,
                            inStockOnly,
                            1,
                            ALL_GOODS_PREVIEW_SIZE
                    );
        }

        if (allGoodsResults == null) {
            allGoodsResults =
                    new ArrayList<GoodsVO>();
        }

        List<String> availableProductTypes =
                goodsService.getSearchProductTypes();

        if (availableProductTypes == null) {
            availableProductTypes =
                    new ArrayList<String>();
        }

        /*
         * 검색 조건에 일치하는 전체 건수
         *
         * JSONL 검색은 전체 데이터를 메모리에서 필터링한 뒤
         * SearchResultPageVO.totalResults에 전체 일치 건수를 저장한다.
         * 따라서 현재 페이지에 표시된 카드 수가 아니라
         * 실제 검색 결과 전체 건수를 화면에 전달한다.
         */
        int contentTotalCount =
                contentPageVO.getTotalResults();

        int combinedTotalCount =
                contentTotalCount
                        + goodsTotalCount;

        /*
         * 활성 OTT 플랫폼 로고
         */
        Map<String, String> ottLogoMap =
                createOttLogoMap(
                        tmdbDAO.selectActivePlatformList()
                );

        /*
         * 로그인 회원이 찜한 상품 번호 목록
         *
         * goodsList.jsp의 상품 카드와 동일한 찜(♥) 버튼을 검색 결과에서도
         * 그대로 재사용하기 위해, goodsList 화면과 같은 방식으로
         * wishedProductNoSet을 모델에 담아 전달합니다.
         */
        MemberVO loginMember =
                (MemberVO) session.getAttribute("loginMember");

        Long loginMemberNo =
                loginMember == null
                        ? null
                        : loginMember.getMemberNo();

        Set<Integer> wishedProductNoSet =
                wishService.getWishedProductNoSet(loginMemberNo);

        model.addAttribute(
                "searchVO",
                searchVO
        );

        model.addAttribute(
                "wishedProductNoSet",
                wishedProductNoSet
        );

        model.addAttribute(
                "searchResults",
                contentResults
        );

        model.addAttribute(
                "resultList",
                contentResults
        );

        model.addAttribute(
                "contentResults",
                contentResults
        );

        model.addAttribute(
                "goodsResults",
                goodsResults
        );

        model.addAttribute(
                "allContentResults",
                allContentResults
        );

        model.addAttribute(
                "allGoodsResults",
                allGoodsResults
        );

        model.addAttribute(
                "contentPageVO",
                contentPageVO
        );

        model.addAttribute(
                "pageVO",
                contentPageVO
        );

        model.addAttribute(
                "contentCurrentPage",
                contentPage
        );

        model.addAttribute(
                "goodsCurrentPage",
                goodsPage
        );

        model.addAttribute(
                "currentPage",
                contentPage
        );

        model.addAttribute(
                "contentTotalPages",
                contentPageVO.getTotalPages()
        );

        model.addAttribute(
                "goodsTotalPages",
                goodsTotalPages
        );

        model.addAttribute(
                "totalPages",
                contentPageVO.getTotalPages()
        );

        model.addAttribute(
                "contentTotalCount",
                contentTotalCount
        );

        model.addAttribute(
                "goodsTotalCount",
                goodsTotalCount
        );

        model.addAttribute(
                "combinedTotalCount",
                combinedTotalCount
        );

        model.addAttribute(
                "totalResults",
                contentTotalCount
        );

        model.addAttribute(
                "keyword",
                keyword
        );

        model.addAttribute(
                "contentCategories",
                contentCategories
        );

        model.addAttribute(
                "genreCodes",
                genreCodes
        );

        model.addAttribute(
                "providerIds",
                providerIds
        );

        model.addAttribute(
                "ageRatings",
                ageRatings
        );

        model.addAttribute(
                "productTypes",
                productTypes
        );

        model.addAttribute(
                "availableProductTypes",
                availableProductTypes
        );

        model.addAttribute(
                "minPrice",
                minPrice
        );

        model.addAttribute(
                "maxPrice",
                maxPrice
        );

        model.addAttribute(
                "discountOnly",
                discountOnly
        );

        model.addAttribute(
                "inStockOnly",
                inStockOnly
        );

        model.addAttribute(
                "searchTab",
                searchTab
        );

        model.addAttribute(
                "searchTitle",
                makeSearchTitle(searchVO)
        );

        model.addAttribute(
                "imageBaseUrl",
                imageBaseUrl
        );

        model.addAttribute(
                "ottLogoMap",
                ottLogoMap
        );

        return "search/searchResult";
    }

    /**
     * 활성 OTT 플랫폼 목록을 플랫폼명-로고 주소 Map으로 변환합니다.
     */
    private Map<String, String> createOttLogoMap(
            List<OttPlatformVO> platformList) {

        Map<String, String> logoMap =
                new LinkedHashMap<String, String>();

        if (platformList == null) {
            return logoMap;
        }

        for (OttPlatformVO platform : platformList) {

            if (platform == null
                    || platform.getPlatformName() == null
                    || platform.getLogoImage() == null
                    || platform.getLogoImage().isBlank()) {

                continue;
            }

            String platformKey =
                    PlatformNameNormalizer.toKey(
                            platform.getPlatformName()
                    );

            if (!platformKey.isEmpty()) {

                logoMap.put(
                        platformKey,
                        platform.getLogoImage()
                );
            }
        }

        return logoMap;
    }

    /**
     * 콘텐츠 분류 필터에서 허용하는 값만 남깁니다.
     *
     * 시스템 기본 언어에 따라 대문자 변환 결과가 달라지지 않도록
     * Locale.ROOT를 명시합니다.
     */
    private List<String> normalizeContentCategories(
            List<String> sourceList) {

        List<String> safeList =
                new ArrayList<String>();

        if (sourceList == null) {
            return safeList;
        }

        for (String value : sourceList) {

            if (value == null) {
                continue;
            }

            String normalized =
                    value.trim().toUpperCase(Locale.ROOT);

            if (("MOVIE".equals(normalized)
                    || "DRAMA".equals(normalized)
                    || "ANIMATION".equals(normalized)
                    || "VARIETY".equals(normalized)
                    || "DOCUMENTARY".equals(normalized))
                    && !safeList.contains(normalized)) {

                safeList.add(normalized);
            }
        }

        return safeList;
    }

    /**
     * null, 빈 문자열, 중복값을 제거한 안전한 필터 목록을 만듭니다.
     */
    private List<String> createSafeList(
            List<String> sourceList) {

        List<String> safeList =
                new ArrayList<String>();

        if (sourceList == null) {
            return safeList;
        }

        for (String value : sourceList) {

            if (value == null) {
                continue;
            }

            String normalizedValue =
                    value.trim();

            if (!normalizedValue.isEmpty()
                    && !safeList.contains(normalizedValue)) {

                safeList.add(normalizedValue);
            }
        }

        return safeList;
    }

    /**
     * 가격 필터는 0원 미만이 되지 않도록 보정합니다.
     */
    private Integer normalizePrice(
            Integer price) {

        if (price == null) {
            return null;
        }

        return Math.max(price, 0);
    }

    /**
     * 전체 검색 건수와 페이지 크기로 전체 페이지 수를 계산합니다.
     */
    private int calculateTotalPages(
            int totalCount,
            int pageSize) {

        if (totalCount <= 0
                || pageSize <= 0) {

            return 0;
        }

        return (totalCount + pageSize - 1)
                / pageSize;
    }

    /**
     * 페이지 번호가 1보다 작으면 첫 페이지로 보정합니다.
     */
    private int normalizePage(
            int page) {

        return page <= 0
                ? 1
                : page;
    }

    /**
     * 검색어 앞뒤의 공백을 제거하고 null을 빈 문자열로 바꿉니다.
     */
    private String normalizeKeyword(
            String keyword) {

        return keyword == null
                ? ""
                : keyword.trim();
    }

    /**
     * 전체, 콘텐츠, 상품 탭 이외의 값은 전체 탭으로 처리합니다.
     *
     * 시스템 기본 언어에 영향을 받지 않도록 Locale.ROOT를 사용합니다.
     */
    private String normalizeSearchTab(
            String searchTab) {

        if (searchTab == null) {
            return "ALL";
        }

        String normalized =
                searchTab
                        .trim()
                        .toUpperCase(Locale.ROOT);

        if (SEARCH_TAB_CONTENT.equals(normalized)) {
            return SEARCH_TAB_CONTENT;
        }

        if (SEARCH_TAB_GOODS.equals(normalized)) {
            return SEARCH_TAB_GOODS;
        }

        return "ALL";
    }

    /**
     * 검색어와 필터 선택 상태에 맞는 화면 제목을 생성합니다.
     */
    private String makeSearchTitle(
            SearchVO searchVO) {

        boolean hasKeyword =
                searchVO != null
                        && searchVO.hasKeyword();

        boolean hasFilter =
                searchVO != null
                        && searchVO.hasFilter();

        if (hasKeyword && hasFilter) {

            return "'"
                    + searchVO.getKeyword()
                    + "' 조건 검색 결과";
        }

        if (hasKeyword) {

            return "'"
                    + searchVO.getKeyword()
                    + "' 검색 결과";
        }

        if (hasFilter) {
            return "선택 조건 검색 결과";
        }

        String searchTab =
                searchVO == null
                        ? "ALL"
                        : normalizeSearchTab(
                                searchVO.getSearchTab()
                        );

        if (SEARCH_TAB_CONTENT.equals(searchTab)) {
            return "지금 인기 있는 콘텐츠";
        }

        if (SEARCH_TAB_GOODS.equals(searchTab)) {
            return "현재 판매 중인 상품";
        }

        return "지금 인기 있는 콘텐츠와 상품";
    }
}