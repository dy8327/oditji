package com.project.oditji.goods.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.project.oditji.goods.vo.GoodsVO;

public interface GoodsDAO {

    List<GoodsVO> selectSearchGoods(
            @Param("keyword") String keyword,
            @Param("productTypes") List<String> productTypes,
            @Param("minPrice") Integer minPrice,
            @Param("maxPrice") Integer maxPrice,
            @Param("discountOnly") boolean discountOnly,
            @Param("inStockOnly") boolean inStockOnly,
            @Param("startRow") int startRow,
            @Param("endRow") int endRow
    );

    int countSearchGoods(
            @Param("keyword") String keyword,
            @Param("productTypes") List<String> productTypes,
            @Param("minPrice") Integer minPrice,
            @Param("maxPrice") Integer maxPrice,
            @Param("discountOnly") boolean discountOnly,
            @Param("inStockOnly") boolean inStockOnly
    );

    List<String> selectSearchProductTypes();
}
