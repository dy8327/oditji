package com.project.oditji.goods.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.project.oditji.common.util.FilterValueNormalizer;
import com.project.oditji.goods.dao.GoodsDAO;
import com.project.oditji.goods.vo.GoodsVO;
import com.project.oditji.goods.vo.ProductOptionVO;

@Service
@Transactional(readOnly = true)
public class GoodsServiceImpl implements GoodsService {

    private static final String POPULAR = "popular";

    private final GoodsDAO goodsDAO;

    public GoodsServiceImpl(GoodsDAO goodsDAO) {
        this.goodsDAO = goodsDAO;
    }

    private static final List<String> VALID_SORTS = Arrays.asList(
            POPULAR,
            "latest",
            "price_asc",
            "price_desc",
            "title");

    @Override
    public List<GoodsVO> searchGoods(
            String keyword,
            List<String> productTypes,
            Integer minPrice,
            Integer maxPrice,
            boolean discountOnly,
            boolean inStockOnly,
            List<String> priceRanges,
            List<String> stockStatus,
            String type,
            String sort,
            int page,
            int pageSize) {

        GoodsTextFilters filters = normalizeTextFilters(
                keyword, productTypes, priceRanges, stockStatus);
        PriceBounds prices = normalizePriceBounds(minPrice, maxPrice);
        String normalizedType = normalizeListType(type);
        String normalizedSort = normalizeSort(sort);
        int normalizedPage = normalizePage(page);
        int normalizedPageSize = normalizePageSize(pageSize);
        int startRow = (normalizedPage - 1) * normalizedPageSize + 1;
        int endRow = normalizedPage * normalizedPageSize;

        List<GoodsVO> resultList = goodsDAO.selectSearchGoods(
                filters.keyword(),
                filters.productTypes(),
                prices.minPrice(),
                prices.maxPrice(),
                discountOnly,
                inStockOnly,
                filters.priceRanges(),
                filters.stockStatus(),
                normalizedType,
                normalizedSort,
                startRow,
                endRow);

        return resultList == null
                ? new ArrayList<GoodsVO>()
                : resultList;
    }

    @Override
    public int countSearchGoods(
            String keyword,
            List<String> productTypes,
            Integer minPrice,
            Integer maxPrice,
            boolean discountOnly,
            boolean inStockOnly,
            List<String> priceRanges,
            List<String> stockStatus) {

        GoodsTextFilters filters = normalizeTextFilters(
                keyword, productTypes, priceRanges, stockStatus);
        PriceBounds prices = normalizePriceBounds(minPrice, maxPrice);

        return goodsDAO.countSearchGoods(
                filters.keyword(),
                filters.productTypes(),
                prices.minPrice(),
                prices.maxPrice(),
                discountOnly,
                inStockOnly,
                filters.priceRanges(),
                filters.stockStatus());
    }

    @Override
    public List<GoodsVO> getRecommendedGoods(int limit) {

        int normalizedLimit = limit <= 0 ? 5 : Math.min(limit, 20);

        List<GoodsVO> resultList = goodsDAO.selectRecommendedGoods(normalizedLimit);

        return resultList == null
                ? new ArrayList<GoodsVO>()
                : resultList;
    }

    @Override
    public List<GoodsVO> getGoodsByContentNo(int contentNo, int limit) {

        if (contentNo <= 0) {
            return new ArrayList<GoodsVO>();
        }

        int normalizedLimit = limit <= 0 ? 5 : Math.min(limit, 20);

        List<GoodsVO> resultList = goodsDAO.selectGoodsByContentNo(contentNo, normalizedLimit);

        return resultList == null
                ? new ArrayList<GoodsVO>()
                : resultList;
    }

    @Override
    public List<GoodsVO> getGoodsByTmdbActorId(long tmdbActorId, int limit) {

        if (tmdbActorId <= 0) {
            return new ArrayList<GoodsVO>();
        }

        int normalizedLimit = limit <= 0 ? 5 : Math.min(limit, 20);

        List<GoodsVO> resultList = goodsDAO.selectGoodsByTmdbActorId(tmdbActorId, normalizedLimit);

        return resultList == null
                ? new ArrayList<GoodsVO>()
                : resultList;
    }

    @Override
    public List<String> getSearchProductTypes() {

        List<String> productTypes = goodsDAO.selectSearchProductTypes();

        return productTypes == null
                ? new ArrayList<String>()
                : productTypes;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductOptionVO> getProductOptionList(int productNo) {
        List<ProductOptionVO> list = goodsDAO.selectProductOptionList(productNo);
        return list == null ? Collections.emptyList() : list;
    }

    @Override
    public GoodsVO getGoodsDetail(int productNo) {

        if (productNo <= 0) {
            return null;
        }

        return goodsDAO.selectGoodsDetail(productNo);
    }

    @Override
    public List<Map<String, Object>> getGoodsImageList(int productNo) {

        if (productNo <= 0) {
            return Collections.emptyList();
        }

        List<Map<String, Object>> imageList = goodsDAO.selectGoodsImageList(productNo);

        return imageList == null
                ? Collections.emptyList()
                : imageList;
    }

    @Override
    public Map<String, Object> getGoodsContent(int productNo) {

        if (productNo <= 0) {
            return Collections.emptyMap();
        }

        Map<String, Object> content = goodsDAO.selectGoodsContent(productNo);

        return content == null
                ? Collections.emptyMap()
                : content;
    }

    @Override
    public Map<String, Object> getGoodsActor(int productNo) {

        if (productNo <= 0) {
            return Collections.emptyMap();
        }

        Map<String, Object> actor = goodsDAO.selectGoodsActor(productNo);

        return actor == null
                ? Collections.emptyMap()
                : actor;
    }

    /* 상품 상세 클릭 로그 저장 */
    @Override
    public void addProductClickLog(int productNo, Long memberNo) {
        goodsDAO.insertProductClickLog(productNo, memberNo);
    }

    /** 상품 목록 정렬 유형을 안전한 값으로 정규화합니다. */
    private String normalizeListType(String type) {

        if (type == null) {
            return "all";
        }

        String normalized = type.trim().toLowerCase(Locale.ROOT);

        return POPULAR.equals(normalized)
                ? POPULAR
                : "all";
    }

    /**
     * [추가] 목록 상단 정렬 select(contentList.jsp와 통일한 정렬 UI)에서 전달된 값을
     * 허용된 값(popular/latest/price_asc/price_desc/title)으로만 정규화합니다.
     * 허용되지 않은 값이거나 비어 있으면 기본값(popular)을 사용합니다.
     */
    private String normalizeSort(String sort) {

        if (sort == null) {
            return POPULAR;
        }

        String normalized = sort.trim().toLowerCase(Locale.ROOT);

        return VALID_SORTS.contains(normalized)
                ? normalized
                : POPULAR;
    }

    private GoodsTextFilters normalizeTextFilters(
            String keyword,
            List<String> productTypes,
            List<String> priceRanges,
            List<String> stockStatus) {

        return new GoodsTextFilters(
                keyword == null ? "" : keyword.trim(),
                FilterValueNormalizer.distinctTrimmed(productTypes),
                FilterValueNormalizer.distinctAllowed(
                        priceRanges, VALID_PRICE_RANGES),
                FilterValueNormalizer.distinctAllowed(
                        stockStatus, VALID_STOCK_STATUS));
    }

    private PriceBounds normalizePriceBounds(
            Integer minPrice,
            Integer maxPrice) {

        Integer normalizedMinPrice = normalizePrice(minPrice);
        Integer normalizedMaxPrice = normalizePrice(maxPrice);

        if (normalizedMinPrice != null
                && normalizedMaxPrice != null
                && normalizedMinPrice > normalizedMaxPrice) {

            return new PriceBounds(normalizedMaxPrice, normalizedMinPrice);
        }

        return new PriceBounds(normalizedMinPrice, normalizedMaxPrice);
    }

    private static final List<String> VALID_PRICE_RANGES = Arrays.asList(
            "UNDER_10000",
            "RANGE_10000_30000",
            "RANGE_30000_50000",
            "RANGE_50000_100000",
            "OVER_100000");

    private static final List<String> VALID_STOCK_STATUS = Arrays.asList(
            "IN_STOCK",
            "SOLD_OUT");

    private Integer normalizePrice(Integer price) {

        if (price == null) {
            return null;
        }

        return Math.max(price, 0);
    }

    private int normalizePage(int page) {
        return page <= 0 ? 1 : page;
    }

    private int normalizePageSize(int pageSize) {

        if (pageSize <= 0) {
            return 12;
        }

        return Math.min(pageSize, 100);
    }
    private record GoodsTextFilters(
            String keyword,
            List<String> productTypes,
            List<String> priceRanges,
            List<String> stockStatus) {
    }

    private record PriceBounds(
            Integer minPrice,
            Integer maxPrice) {
    }

}