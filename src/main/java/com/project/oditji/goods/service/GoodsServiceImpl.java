package com.project.oditji.goods.service;

import java.util.ArrayList;
import java.util.List;

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
            int page,
            int pageSize) {

        String normalizedKeyword =
                normalizeKeyword(keyword);

        List<String> normalizedProductTypes =
                normalizeProductTypes(productTypes);

        Integer normalizedMinPrice =
                normalizePrice(minPrice);

        Integer normalizedMaxPrice =
                normalizePrice(maxPrice);

        if (normalizedMinPrice != null
                && normalizedMaxPrice != null
                && normalizedMinPrice > normalizedMaxPrice) {

            int temporaryPrice = normalizedMinPrice;

            normalizedMinPrice = normalizedMaxPrice;
            normalizedMaxPrice = temporaryPrice;
        }

        int normalizedPage = normalizePage(page);

        int normalizedPageSize =
                normalizePageSize(pageSize);

        int startRow =
                (normalizedPage - 1)
                * normalizedPageSize
                + 1;

        int endRow =
                normalizedPage
                * normalizedPageSize;

        List<GoodsVO> resultList =
                goodsDAO.selectSearchGoods(
                        normalizedKeyword,
                        normalizedProductTypes,
                        normalizedMinPrice,
                        normalizedMaxPrice,
                        discountOnly,
                        inStockOnly,
                        startRow,
                        endRow
                );

        if (resultList == null) {
            return new ArrayList<GoodsVO>();
        }

        return resultList;
    }

    @Override
    public int countSearchGoods(
            String keyword,
            List<String> productTypes,
            Integer minPrice,
            Integer maxPrice,
            boolean discountOnly,
            boolean inStockOnly) {

        String normalizedKeyword =
                normalizeKeyword(keyword);

        List<String> normalizedProductTypes =
                normalizeProductTypes(productTypes);

        Integer normalizedMinPrice =
                normalizePrice(minPrice);

        Integer normalizedMaxPrice =
                normalizePrice(maxPrice);

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
                inStockOnly
        );
    }

    @Override
    public List<GoodsVO> getRecommendedGoods(int limit) {

        int normalizedLimit = limit;

        if (normalizedLimit <= 0) {
            normalizedLimit = 5;
        }

        normalizedLimit = Math.min(
                normalizedLimit,
                20
        );

        List<GoodsVO> recommendedGoodsList =
                goodsDAO.selectRecommendedGoods(
                        normalizedLimit
                );

        if (recommendedGoodsList == null) {
            return new ArrayList<GoodsVO>();
        }

        return recommendedGoodsList;
    }

    @Override
    public List<String> getSearchProductTypes() {

        List<String> productTypes =
                goodsDAO.selectSearchProductTypes();

        if (productTypes == null) {
            return new ArrayList<String>();
        }

        return productTypes;
    }

    private String normalizeKeyword(String keyword) {

        if (keyword == null) {
            return "";
        }

        return keyword.trim();
    }

    private List<String> normalizeProductTypes(
            List<String> sourceList) {

        List<String> normalizedList =
                new ArrayList<String>();

        if (sourceList == null) {
            return normalizedList;
        }

        for (String productType : sourceList) {

            if (productType == null) {
                continue;
            }

            String normalized =
                    productType.trim();

            if (!normalized.isEmpty()
                    && !normalizedList.contains(
                            normalized
                    )) {

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

        if (page <= 0) {
            return 1;
        }

        return page;
    }

    private int normalizePageSize(int pageSize) {

        if (pageSize <= 0) {
            return 10;
        }

        return Math.min(pageSize, 100);
    }
}