package com.project.oditji.goods.service;

import java.util.List;

import com.project.oditji.goods.vo.GoodsVO;

public interface GoodsService {

    /**
     * 검색어와 페이지 조건에 맞는 상품을 조회한다.
     */
    List<GoodsVO> searchGoods(
            String keyword,
            int page,
            int pageSize
    );

    /**
     * 검색어에 해당하는 전체 상품 개수를 조회한다.
     */
    int countSearchGoods(
            String keyword
    );
}