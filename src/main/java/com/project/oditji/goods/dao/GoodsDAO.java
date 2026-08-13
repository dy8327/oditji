package com.project.oditji.goods.dao;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

import com.project.oditji.goods.vo.GoodsVO;
import com.project.oditji.goods.vo.ProductOptionVO;

public interface GoodsDAO {

        @SuppressWarnings("java:S107")
        List<GoodsVO> selectSearchGoods(
                        @Param("keyword") String keyword,
                        @Param("productTypes") List<String> productTypes,
                        @Param("minPrice") Integer minPrice,
                        @Param("maxPrice") Integer maxPrice,
                        @Param("discountOnly") boolean discountOnly,
                        @Param("inStockOnly") boolean inStockOnly,
                        @Param("priceRanges") List<String> priceRanges,
                        @Param("stockStatus") List<String> stockStatus,
                        @Param("type") String type,
                        @Param("sort") String sort,
                        @Param("startRow") int startRow,
                        @Param("endRow") int endRow);

        @SuppressWarnings("java:S107")
        int countSearchGoods(
                        @Param("keyword") String keyword,
                        @Param("productTypes") List<String> productTypes,
                        @Param("minPrice") Integer minPrice,
                        @Param("maxPrice") Integer maxPrice,
                        @Param("discountOnly") boolean discountOnly,
                        @Param("inStockOnly") boolean inStockOnly,
                        @Param("priceRanges") List<String> priceRanges,
                        @Param("stockStatus") List<String> stockStatus);

        List<GoodsVO> selectRecommendedGoods(
                        @Param("limit") int limit);

        /**
         * 콘텐츠 상세페이지의 "관련 상품" 영역에서 사용합니다.
         * 해당 콘텐츠(CONTENT_NO)에 연결된 승인 완료(APPROVED) 상품만 조회합니다.
         */
        List<GoodsVO> selectGoodsByContentNo(
                        @Param("contentNo") int contentNo,
                        @Param("limit") int limit);

        /**
         * 인물 필모그래피 페이지의 "관련 상품" 영역에서 사용합니다.
         * 해당 TMDB 인물 ID(TMDB_ACTOR_ID)로 등록된 배우와 연결된
         * 승인 완료(APPROVED) 상품만 조회합니다.
         */
        List<GoodsVO> selectGoodsByTmdbActorId(
                        @Param("tmdbActorId") long tmdbActorId,
                        @Param("limit") int limit);

        List<String> selectSearchProductTypes();

        GoodsVO selectGoodsDetail(
                        @Param("productNo") int productNo);

        List<Map<String, Object>> selectGoodsImageList(
                        @Param("productNo") int productNo);

        Map<String, Object> selectGoodsContent(
                        @Param("productNo") int productNo);

        Map<String, Object> selectGoodsActor(
                        @Param("productNo") int productNo);

        // [상품 옵션 기능 추가] 상품 상세 옵션 목록 조회
        List<ProductOptionVO> selectProductOptionList(@Param("productNo") int productNo);

        int insertProductClickLog(
                        @Param("productNo") int productNo,
                        @Param("memberNo") Long memberNo);
}