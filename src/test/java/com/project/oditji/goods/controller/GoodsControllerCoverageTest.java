package com.project.oditji.goods.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.web.server.ResponseStatusException;

import com.project.oditji.goods.service.GoodsService;
import com.project.oditji.goods.vo.GoodsVO;
import com.project.oditji.member.vo.MemberVO;
import com.project.oditji.report.service.ReportService;
import com.project.oditji.review.service.ReviewService;
import com.project.oditji.wish.service.WishService;
import com.project.oditji.wish.vo.WishVO;

/** 상품 목록 유형, 카테고리, 상세 추천 및 찜 분기를 검증합니다. */
@ExtendWith(MockitoExtension.class)
class GoodsControllerCoverageTest {

    @Mock
    private GoodsService goodsService;
    @Mock
    private ReviewService reviewService;
    @Mock
    private ReportService reportService;
    @Mock
    private WishService wishService;

    private GoodsController controller;

    @BeforeEach
    void setUp() {
        controller = new GoodsController(
                goodsService,
                reviewService,
                reportService,
                wishService);
    }

    @Test
    void categoryWithoutSingleSupportedTypeShouldRedirect() {
        assertEquals(
                "redirect:/goods/list?type=all",
                callList("category", null, 1, new ExtendedModelMap()));
        assertEquals(
                "redirect:/goods/list?type=all",
                callList("category", List.of("BOOK", "OST"), 1, new ExtendedModelMap()));
        assertEquals(
                "redirect:/goods/list?type=all",
                callList("category", List.of("UNKNOWN"), 1, new ExtendedModelMap()));

        verifyNoInteractions(goodsService, reviewService, reportService, wishService);
    }

    @Test
    void allSupportedCategoryNamesShouldBeExposedToModel() {
        Map<String, String> expected = Map.ofEntries(
                Map.entry("BOOK", "도서"),
                Map.entry("CLOTHES", "의상"),
                Map.entry("SHOES", "신발"),
                Map.entry("OST", "OST"),
                Map.entry("PROP", "소품"),
                Map.entry("GOODS", "굿즈"),
                Map.entry("FIGURE", "피규어"),
                Map.entry("POSTER", "포스터"),
                Map.entry("ETC", "기타"));

        for (Map.Entry<String, String> entry : expected.entrySet()) {
            ExtendedModelMap model = new ExtendedModelMap();
            String view = callList(
                    "category",
                    List.of(entry.getKey()),
                    1,
                    model);

            assertEquals("goods/goodsList", view);
            assertEquals(entry.getValue(), model.get("selectedCategoryName"));
            assertEquals("category", model.get("type"));
        }
    }

    @Test
    void listShouldNormalizeTypeAndPageAndClampToLastPage() {
        when(goodsService.countSearchGoods(
                "keyword", null, null, null,
                false, false, null, null))
                .thenReturn(21);
        when(goodsService.searchGoods(
                "keyword", null, null, null,
                false, false, null, null,
                "all", 3, 10))
                .thenReturn(List.of());

        ExtendedModelMap model = new ExtendedModelMap();
        String view = controller.goodsList(
                "keyword",
                null,
                null,
                null,
                false,
                false,
                null,
                null,
                "invalid",
                99,
                new MockHttpSession(),
                model);

        assertEquals("goods/goodsList", view);
        assertEquals("all", model.get("type"));
        assertEquals(3, model.get("page"));
        assertEquals(3, model.get("totalPage"));
        assertEquals(21, model.get("totalCount"));
    }

    @Test
    void listShouldNormalizeNonPositivePageAndLoadMemberWishes() {
        MemberVO member = new MemberVO();
        member.setMemberNo(50L);
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("loginMember", member);
        when(wishService.getWishedProductNoSet(50L)).thenReturn(Set.of(1, 2));

        ExtendedModelMap model = new ExtendedModelMap();
        String view = controller.goodsList(
                "",
                List.of("BOOK"),
                1000,
                50000,
                true,
                true,
                List.of("10000_30000"),
                List.of("IN_STOCK"),
                "popular",
                0,
                session,
                model);

        assertEquals("goods/goodsList", view);
        assertEquals(1, model.get("page"));
        assertEquals("popular", model.get("type"));
        assertEquals(Set.of(1, 2), model.get("wishedProductNoSet"));
    }

    @Test
    void detailShouldReturnNotFoundForMissingGoods() {
        when(goodsService.getGoodsDetail(10)).thenReturn(null);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> controller.detail(
                        10,
                        new MockHttpSession(),
                        new ExtendedModelMap()));

        assertEquals(404, exception.getStatusCode().value());
    }

    @Test
    void detailShouldFilterCurrentProductAndLimitRecommendationsForGuest() {
        GoodsVO goods = goods(10);
        when(goodsService.getGoodsDetail(10)).thenReturn(goods);
        when(goodsService.getRecommendedGoods(6)).thenReturn(List.of(
                goods(10), goods(11), goods(12), goods(13),
                goods(14), goods(15), goods(16)));
        when(goodsService.getGoodsImageList(10)).thenReturn(List.of());
        when(goodsService.getGoodsContent(10)).thenReturn(Map.of());
        when(goodsService.getGoodsActor(10)).thenReturn(Map.of());
        when(reviewService.getProductReviewList(10)).thenReturn(List.of());
        when(reportService.getReportedProductReviewSet(null)).thenReturn(Set.of());

        ExtendedModelMap model = new ExtendedModelMap();
        String view = controller.detail(10, new MockHttpSession(), model);

        assertEquals("goods/goodsDetail", view);
        assertEquals(Boolean.FALSE, model.get("wishActive"));
        @SuppressWarnings("unchecked")
        List<GoodsVO> recommended = (List<GoodsVO>) model.get("recommendedGoodsList");
        assertEquals(5, recommended.size());
        assertFalse(recommended.stream().anyMatch(value -> value.getProductNo() == 10));
        verify(goodsService).addProductClickLog(10, null);
    }

    @Test
    void detailShouldResolveWishForLoggedInMember() {
        GoodsVO goods = goods(20);
        when(goodsService.getGoodsDetail(20)).thenReturn(goods);
        when(goodsService.getRecommendedGoods(6)).thenReturn(new ArrayList<>());
        when(wishService.isWished(any(WishVO.class))).thenReturn(true);

        MemberVO member = new MemberVO();
        member.setMemberNo(70L);
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("loginMember", member);
        ExtendedModelMap model = new ExtendedModelMap();

        String view = controller.detail(20, session, model);

        assertEquals("goods/goodsDetail", view);
        assertEquals(Boolean.TRUE, model.get("wishActive"));
        verify(goodsService).addProductClickLog(20, 70L);
        verify(wishService).isWished(any(WishVO.class));
    }

    private String callList(
            String type,
            List<String> productTypes,
            int page,
            ExtendedModelMap model) {
        return controller.goodsList(
                "",
                productTypes,
                null,
                null,
                false,
                false,
                null,
                null,
                type,
                page,
                new MockHttpSession(),
                model);
    }

    private GoodsVO goods(int productNo) {
        GoodsVO goods = new GoodsVO();
        goods.setProductNo(productNo);
        return goods;
    }
}
