package com.project.oditji.goods.service;

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

/** 콘텐츠/배우 연관 상품 조회의 limit 삼항연산자 반대쪽 분기를 보완합니다. */
@ExtendWith(MockitoExtension.class)
class GoodsServiceImplLimitOperandClosureTest {

    @Mock
    private GoodsDAO goodsDAO;

    private GoodsServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new GoodsServiceImpl(goodsDAO);
    }

    @Test
    void contentGoodsShouldKeepPositiveLimitBelowMaximum() {
        List<GoodsVO> expected = List.of(new GoodsVO());
        when(goodsDAO.selectGoodsByContentNo(10, 8)).thenReturn(expected);

        assertSame(expected, service.getGoodsByContentNo(10, 8));
    }

    @Test
    void actorGoodsShouldUseDefaultLimitWhenNonPositive() {
        List<GoodsVO> expected = List.of(new GoodsVO());
        when(goodsDAO.selectGoodsByTmdbActorId(20L, 5)).thenReturn(expected);

        assertSame(expected, service.getGoodsByTmdbActorId(20L, 0));
    }
}
