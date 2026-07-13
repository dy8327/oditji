package com.project.oditji.goods.service;

import java.util.List;

import com.project.oditji.goods.vo.GoodsVO;

public interface GoodsService {

    List<GoodsVO> searchGoods(
            String keyword,
            List<String> productTypes,
            Integer minPrice,
            Integer maxPrice,
            boolean discountOnly,
            boolean inStockOnly,
            int page,
            int pageSize
    );

    int countSearchGoods(
            String keyword,
            List<String> productTypes,
            Integer minPrice,
            Integer maxPrice,
            boolean discountOnly,
            boolean inStockOnly
    );

    List<String> getSearchProductTypes();
}
