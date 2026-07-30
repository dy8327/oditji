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
            List<String> priceRanges,
            List<String> stockStatus,
            String type,
            int page,
            int pageSize
    );

    /**
     * 기존 호출부 호환용 메서드입니다.
     * priceRanges, stockStatus를 전달하지 않으면 해당 조건 없이 조회합니다.
     */
    default List<GoodsVO> searchGoods(
            String keyword,
            List<String> productTypes,
            Integer minPrice,
            Integer maxPrice,
            boolean discountOnly,
            boolean inStockOnly,
            String type,
            int page,
            int pageSize) {

        return searchGoods(
                keyword,
                productTypes,
                minPrice,
                maxPrice,
                discountOnly,
                inStockOnly,
                null,
                null,
                type,
                page,
                pageSize
        );
    }

    /**
     * 기존 호출부 호환용 메서드입니다.
     * 정렬 유형을 전달하지 않으면 전체 상품 최신순으로 조회합니다.
     */
    default List<GoodsVO> searchGoods(
            String keyword,
            List<String> productTypes,
            Integer minPrice,
            Integer maxPrice,
            boolean discountOnly,
            boolean inStockOnly,
            int page,
            int pageSize) {

        return searchGoods(
                keyword,
                productTypes,
                minPrice,
                maxPrice,
                discountOnly,
                inStockOnly,
                null,
                null,
                "all",
                page,
                pageSize
        );
    }

    int countSearchGoods(
            String keyword,
            List<String> productTypes,
            Integer minPrice,
            Integer maxPrice,
            boolean discountOnly,
            boolean inStockOnly,
            List<String> priceRanges,
            List<String> stockStatus
    );

    /**
     * 기존 호출부 호환용 메서드입니다.
     * priceRanges, stockStatus를 전달하지 않으면 해당 조건 없이 조회합니다.
     */
    default int countSearchGoods(
            String keyword,
            List<String> productTypes,
            Integer minPrice,
            Integer maxPrice,
            boolean discountOnly,
            boolean inStockOnly) {

        return countSearchGoods(
                keyword,
                productTypes,
                minPrice,
                maxPrice,
                discountOnly,
                inStockOnly,
                null,
                null
        );
    }

    List<GoodsVO> getRecommendedGoods(int limit);

    /** 콘텐츠 상세페이지의 "관련 상품" 목록을 조회합니다. */
    List<GoodsVO> getGoodsByContentNo(int contentNo, int limit);

    /** 인물 필모그래피 페이지의 "관련 상품" 목록을 조회합니다. */
    List<GoodsVO> getGoodsByTmdbActorId(long tmdbActorId, int limit);

    List<String> getSearchProductTypes();

    GoodsVO getGoodsDetail(int productNo);

    List<Map<String, Object>> getGoodsImageList(int productNo);

    Map<String, Object> getGoodsContent(int productNo);

    Map<String, Object> getGoodsActor(int productNo);

    /* 상품 상세 클릭 로그 저장 */
    void addProductClickLog(int productNo, Long memberNo);
}