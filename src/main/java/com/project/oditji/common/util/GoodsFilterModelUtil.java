package com.project.oditji.common.util;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 상품 목록과 통합 검색 화면에서 공통으로 사용하는 상품 필터 모델을 생성합니다.
 */
public final class GoodsFilterModelUtil {

    private GoodsFilterModelUtil() {
        // 인스턴스 생성 방지
    }

    public static Map<String, Object> create(
            String keyword,
            List<String> productTypes,
            List<String> availableProductTypes,
            Integer minPrice,
            Integer maxPrice,
            boolean discountOnly,
            boolean inStockOnly) {

        Map<String, Object> attributes = new LinkedHashMap<String, Object>();
        attributes.put("keyword", keyword);
        attributes.put("productTypes", productTypes);
        attributes.put("availableProductTypes", availableProductTypes);
        attributes.put("minPrice", minPrice);
        attributes.put("maxPrice", maxPrice);
        attributes.put("discountOnly", discountOnly);
        attributes.put("inStockOnly", inStockOnly);
        return attributes;
    }
}
