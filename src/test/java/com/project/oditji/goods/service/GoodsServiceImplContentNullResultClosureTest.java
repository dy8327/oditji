package com.project.oditji.goods.service;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.project.oditji.goods.dao.GoodsDAO;

/** 정상 콘텐츠 번호 조회에서 DAO가 null을 반환하는 잔여 결과 분기를 보완합니다. */
class GoodsServiceImplContentNullResultClosureTest {

    private GoodsDAO goodsDAO;
    private GoodsServiceImpl service;

    @BeforeEach
    void setUp() {
        goodsDAO = mock(GoodsDAO.class);
        service = new GoodsServiceImpl(goodsDAO);
    }

    @Test
    void contentGoodsShouldReturnEmptyListWhenDaoReturnsNull() {
        when(goodsDAO.selectGoodsByContentNo(10, 8)).thenReturn(null);

        assertTrue(service.getGoodsByContentNo(10, 8).isEmpty());

        verify(goodsDAO).selectGoodsByContentNo(10, 8);
    }
}
