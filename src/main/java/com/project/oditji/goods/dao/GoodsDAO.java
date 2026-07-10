package com.project.oditji.goods.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.project.oditji.goods.vo.GoodsVO;

public interface GoodsDAO {

    /**
     * 상품명 검색 결과를 페이지 단위로 조회한다.
     */
    List<GoodsVO> selectSearchGoods(
            @Param("keyword") String keyword,
            @Param("startRow") int startRow,
            @Param("endRow") int endRow
    );

    /**
     * 상품명 검색 결과 전체 개수를 조회한다.
     */
    int countSearchGoods(
            @Param("keyword") String keyword
    );
}