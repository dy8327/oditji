package com.project.oditji.goods.service;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.project.oditji.goods.dao.GoodsDAO;
import com.project.oditji.goods.vo.GoodsVO;
import com.project.oditji.goods.vo.ProductOptionVO;

/** 상품 검색 입력 정규화와 상세 조회의 안전한 기본값을 검증합니다. */
@ExtendWith(MockitoExtension.class)
class GoodsServiceImplTest {

    @Mock
    private GoodsDAO goodsDAO;

    private GoodsServiceImpl goodsService;

    @BeforeEach
    void setUp() {
        goodsService = new GoodsServiceImpl(goodsDAO);
    }

    @Test
    void searchGoodsShouldNormalizeFiltersPagingAndPriceOrder() {
        List<GoodsVO> result = List.of(new GoodsVO());
        when(goodsDAO.selectSearchGoods(
                "키워드",
                List.of("FIGURE", "CLOTHES"),
                1000,
                5000,
                true,
                false,
                List.of("UNDER_10000", "OVER_100000"),
                List.of("IN_STOCK", "SOLD_OUT"),
                "popular",
                1,
                100)).thenReturn(result);

        assertSame(
                result,
                goodsService.searchGoods(
                        " 키워드 ",
                        java.util.Arrays.asList(
                                " FIGURE ",
                                "FIGURE",
                                null,
                                "CLOTHES",
                                " "),
                        5000,
                        1000,
                        true,
                        false,
                        java.util.Arrays.asList(
                                "UNDER_10000",
                                "INVALID",
                                "OVER_100000",
                                "UNDER_10000",
                                null),
                        java.util.Arrays.asList(
                                "IN_STOCK",
                                "INVALID",
                                "SOLD_OUT",
                                null),
                        " POPULAR ",
                        0,
                        500));
    }

    @Test
    void searchAndCountShouldReturnEmptyOrDelegateNormalizedDefaults() {
        when(goodsDAO.selectSearchGoods(
                "",
                List.of(),
                0,
                null,
                false,
                true,
                List.of(),
                List.of(),
                "all",
                13,
                24)).thenReturn(null);

        assertTrue(goodsService.searchGoods(
                null,
                null,
                -1,
                null,
                false,
                true,
                null,
                null,
                "unknown",
                2,
                12).isEmpty());

        when(goodsDAO.countSearchGoods(
                "query",
                List.of("ETC"),
                0,
                3000,
                false,
                false,
                List.of("RANGE_10000_30000"),
                List.of("IN_STOCK"))).thenReturn(7);

        org.junit.jupiter.api.Assertions.assertEquals(
                7,
                goodsService.countSearchGoods(
                        " query ",
                        List.of(" ETC ", "ETC"),
                        -100,
                        3000,
                        false,
                        false,
                        List.of("RANGE_10000_30000"),
                        List.of("IN_STOCK")));
    }

    @Test
    void recommendationAndRelatedLookupsShouldNormalizeLimits() {
        List<GoodsVO> result = List.of(new GoodsVO());
        when(goodsDAO.selectRecommendedGoods(5)).thenReturn(result);
        when(goodsDAO.selectRecommendedGoods(20)).thenReturn(null);
        when(goodsDAO.selectGoodsByContentNo(10, 5)).thenReturn(result);
        when(goodsDAO.selectGoodsByTmdbActorId(20L, 20)).thenReturn(null);

        assertSame(result, goodsService.getRecommendedGoods(0));
        assertTrue(goodsService.getRecommendedGoods(99).isEmpty());
        assertSame(result, goodsService.getGoodsByContentNo(10, -1));
        assertTrue(goodsService.getGoodsByContentNo(0, 5).isEmpty());
        assertTrue(goodsService.getGoodsByTmdbActorId(20L, 99).isEmpty());
        assertTrue(goodsService.getGoodsByTmdbActorId(0L, 5).isEmpty());

        verify(goodsDAO, never()).selectGoodsByContentNo(0, 5);
        verify(goodsDAO, never()).selectGoodsByTmdbActorId(0L, 5);
    }

    @Test
    void listAndOptionMethodsShouldReturnEmptyWhenDaoReturnsNull() {
        when(goodsDAO.selectSearchProductTypes()).thenReturn(null);
        when(goodsDAO.selectProductOptionList(1)).thenReturn(null);

        assertTrue(goodsService.getSearchProductTypes().isEmpty());
        assertTrue(goodsService.getProductOptionList(1).isEmpty());

        List<String> types = List.of("FIGURE");
        List<ProductOptionVO> options = List.of(new ProductOptionVO());
        when(goodsDAO.selectSearchProductTypes()).thenReturn(types);
        when(goodsDAO.selectProductOptionList(2)).thenReturn(options);

        assertSame(types, goodsService.getSearchProductTypes());
        assertSame(options, goodsService.getProductOptionList(2));
    }

    @Test
    void detailAssociatedDataAndClickLogShouldValidateAndDelegate() {
        GoodsVO goods = new GoodsVO();
        List<Map<String, Object>> images = List.of(Map.of("path", "a.jpg"));
        Map<String, Object> content = Map.of("title", "콘텐츠");
        Map<String, Object> actor = Map.of("name", "배우");

        when(goodsDAO.selectGoodsDetail(1)).thenReturn(goods);
        when(goodsDAO.selectGoodsImageList(1)).thenReturn(images);
        when(goodsDAO.selectGoodsContent(1)).thenReturn(content);
        when(goodsDAO.selectGoodsActor(1)).thenReturn(actor);

        assertSame(goods, goodsService.getGoodsDetail(1));
        assertNull(goodsService.getGoodsDetail(0));
        assertSame(images, goodsService.getGoodsImageList(1));
        assertSame(content, goodsService.getGoodsContent(1));
        assertSame(actor, goodsService.getGoodsActor(1));

        assertTrue(goodsService.getGoodsImageList(0).isEmpty());
        assertTrue(goodsService.getGoodsContent(0).isEmpty());
        assertTrue(goodsService.getGoodsActor(0).isEmpty());

        when(goodsDAO.selectGoodsImageList(2)).thenReturn(null);
        when(goodsDAO.selectGoodsContent(2)).thenReturn(null);
        when(goodsDAO.selectGoodsActor(2)).thenReturn(null);
        assertTrue(goodsService.getGoodsImageList(2).isEmpty());
        assertTrue(goodsService.getGoodsContent(2).isEmpty());
        assertTrue(goodsService.getGoodsActor(2).isEmpty());

        goodsService.addProductClickLog(1, 10L);
        verify(goodsDAO).insertProductClickLog(1, 10L);
    }
}
