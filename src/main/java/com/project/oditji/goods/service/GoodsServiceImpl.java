package com.project.oditji.goods.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.project.oditji.goods.dao.GoodsDAO;
import com.project.oditji.goods.vo.GoodsVO;

@Service
@Transactional(readOnly = true)
public class GoodsServiceImpl implements GoodsService {

    private final GoodsDAO goodsDAO;

    public GoodsServiceImpl(GoodsDAO goodsDAO) {
        this.goodsDAO = goodsDAO;
    }

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
            int page,
            int pageSize) {

        String normalizedKeyword = normalizeKeyword(keyword);
        List<String> normalizedProductTypes = normalizeProductTypes(productTypes);
        Integer normalizedMinPrice = normalizePrice(minPrice);
        Integer normalizedMaxPrice = normalizePrice(maxPrice);
        List<String> normalizedPriceRanges = normalizePriceRanges(priceRanges);
        List<String> normalizedStockStatus = normalizeStockStatus(stockStatus);

        if (normalizedMinPrice != null
                && normalizedMaxPrice != null
                && normalizedMinPrice > normalizedMaxPrice) {

            int temporaryPrice = normalizedMinPrice;
            normalizedMinPrice = normalizedMaxPrice;
            normalizedMaxPrice = temporaryPrice;
        }

        String normalizedType = normalizeListType(type);
        int normalizedPage = normalizePage(page);
        int normalizedPageSize = normalizePageSize(pageSize);
        int startRow = (normalizedPage - 1) * normalizedPageSize + 1;
        int endRow = normalizedPage * normalizedPageSize;

        List<GoodsVO> resultList = goodsDAO.selectSearchGoods(
                normalizedKeyword,
                normalizedProductTypes,
                normalizedMinPrice,
                normalizedMaxPrice,
                discountOnly,
                inStockOnly,
                normalizedPriceRanges,
                normalizedStockStatus,
                normalizedType,
                startRow,
                endRow
        );

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

        String normalizedKeyword = normalizeKeyword(keyword);
        List<String> normalizedProductTypes = normalizeProductTypes(productTypes);
        Integer normalizedMinPrice = normalizePrice(minPrice);
        Integer normalizedMaxPrice = normalizePrice(maxPrice);
        List<String> normalizedPriceRanges = normalizePriceRanges(priceRanges);
        List<String> normalizedStockStatus = normalizeStockStatus(stockStatus);

        if (normalizedMinPrice != null
                && normalizedMaxPrice != null
                && normalizedMinPrice > normalizedMaxPrice) {

            int temporaryPrice = normalizedMinPrice;
            normalizedMinPrice = normalizedMaxPrice;
            normalizedMaxPrice = temporaryPrice;
        }

        return goodsDAO.countSearchGoods(
                normalizedKeyword,
                normalizedProductTypes,
                normalizedMinPrice,
                normalizedMaxPrice,
                discountOnly,
                inStockOnly,
                normalizedPriceRanges,
                normalizedStockStatus
        );
    }

    @Override
    public List<GoodsVO> getRecommendedGoods(int limit) {

        int normalizedLimit = limit <= 0 ? 5 : Math.min(limit, 20);

        List<GoodsVO> resultList =
                goodsDAO.selectRecommendedGoods(normalizedLimit);

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

        List<GoodsVO> resultList =
                goodsDAO.selectGoodsByContentNo(contentNo, normalizedLimit);

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

        List<GoodsVO> resultList =
                goodsDAO.selectGoodsByTmdbActorId(tmdbActorId, normalizedLimit);

        return resultList == null
                ? new ArrayList<GoodsVO>()
                : resultList;
    }

    @Override
    public List<String> getSearchProductTypes() {

        List<String> productTypes =
                goodsDAO.selectSearchProductTypes();

        return productTypes == null
                ? new ArrayList<String>()
                : productTypes;
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

        List<Map<String, Object>> imageList =
                goodsDAO.selectGoodsImageList(productNo);

        return imageList == null
                ? Collections.emptyList()
                : imageList;
    }

    @Override
    public Map<String, Object> getGoodsContent(int productNo) {

        if (productNo <= 0) {
            return null;
        }

        return goodsDAO.selectGoodsContent(productNo);
    }

    @Override
    public Map<String, Object> getGoodsActor(int productNo) {

        if (productNo <= 0) {
            return null;
        }

        return goodsDAO.selectGoodsActor(productNo);
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

        String normalized = type.trim().toLowerCase();

        return "popular".equals(normalized)
                ? "popular"
                : "all";
    }

    private String normalizeKeyword(String keyword) {
        return keyword == null ? "" : keyword.trim();
    }

    private List<String> normalizeProductTypes(List<String> sourceList) {

        List<String> normalizedList = new ArrayList<String>();

        if (sourceList == null) {
            return normalizedList;
        }

        for (String productType : sourceList) {

            if (productType == null) {
                continue;
            }

            String normalized = productType.trim();

            if (!normalized.isEmpty()
                    && !normalizedList.contains(normalized)) {

                normalizedList.add(normalized);
            }
        }

        return normalizedList;
    }

    private static final List<String> VALID_PRICE_RANGES = java.util.Arrays.asList(
            "UNDER_10000",
            "RANGE_10000_30000",
            "RANGE_30000_50000",
            "RANGE_50000_100000",
            "OVER_100000"
    );

    private static final List<String> VALID_STOCK_STATUS = java.util.Arrays.asList(
            "IN_STOCK",
            "SOLD_OUT"
    );

    private List<String> normalizePriceRanges(List<String> sourceList) {

        List<String> normalizedList = new ArrayList<String>();

        if (sourceList == null) {
            return normalizedList;
        }

        for (String priceRange : sourceList) {

            if (priceRange == null) {
                continue;
            }

            String normalized = priceRange.trim();

            if (VALID_PRICE_RANGES.contains(normalized)
                    && !normalizedList.contains(normalized)) {

                normalizedList.add(normalized);
            }
        }

        return normalizedList;
    }

    private List<String> normalizeStockStatus(List<String> sourceList) {

        List<String> normalizedList = new ArrayList<String>();

        if (sourceList == null) {
            return normalizedList;
        }

        for (String status : sourceList) {

            if (status == null) {
                continue;
            }

            String normalized = status.trim();

            if (VALID_STOCK_STATUS.contains(normalized)
                    && !normalizedList.contains(normalized)) {

                normalizedList.add(normalized);
            }
        }

        return normalizedList;
    }

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
}