package com.project.oditji.goods.service;

import java.util.List;
import java.util.Map;

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

    List<GoodsVO> getRecommendedGoods(int limit);

    List<String> getSearchProductTypes();

    GoodsVO getGoodsDetail(int productNo);

    List<Map<String, Object>> getGoodsImageList(int productNo);

    Map<String, Object> getGoodsContent(int productNo);

    Map<String, Object> getGoodsActor(int productNo);
}