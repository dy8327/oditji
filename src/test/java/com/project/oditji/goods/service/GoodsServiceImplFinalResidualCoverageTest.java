package com.project.oditji.goods.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.project.oditji.goods.dao.GoodsDAO;
import com.project.oditji.goods.vo.GoodsVO;

/** 상품 서비스의 정상 limit/정상 결과 반환 잔여 경로를 보완합니다. */
@ExtendWith(MockitoExtension.class)
class GoodsServiceImplFinalResidualCoverageTest {

    @Mock
    private GoodsDAO goodsDAO;

    private GoodsServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new GoodsServiceImpl(goodsDAO);
    }

    @Test
    void recommendedGoodsShouldKeepNormalPositiveLimitAndReturnDaoList() {
        List<GoodsVO> expected = List.of(new GoodsVO());
        when(goodsDAO.selectRecommendedGoods(8))
                .thenReturn(expected);

        assertSame(expected, service.getRecommendedGoods(8));
    }

    @Test
    void searchGoodsShouldReturnNonNullDaoResultWithNormalPaging() {
        List<GoodsVO> expected = List.of(new GoodsVO());
        when(goodsDAO.selectSearchGoods(
                "keyword",
                List.of(),
                1000,
                5000,
                false,
                false,
                List.of(),
                List.of(),
                "popular",
                "popular",
                13,
                24))
                .thenReturn(expected);

        assertEquals(
                expected,
                service.searchGoods(
                        " keyword ",
                        List.of(),
                        1000,
                        5000,
                        false,
                        false,
                        List.of(),
                        List.of(),
                        "popular",
                        "popular",
                        2,
                        12));
    }
}
