package com.project.oditji.common.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

/**
 * 목록형 요청 파라미터의 공백, 중복 및 허용값을 일관되게 정리합니다.
 */
public final class FilterValueNormalizer {

    private FilterValueNormalizer() {
        // 인스턴스 생성 방지
    }

    public static List<String> distinctTrimmed(List<String> sourceList) {
        return normalize(sourceList, null, false);
    }

    public static List<String> distinctAllowed(
            List<String> sourceList,
            Collection<String> allowedValues) {
        return normalize(sourceList, allowedValues, false);
    }

    public static List<String> distinctUpperCaseAllowed(
            List<String> sourceList,
            Collection<String> allowedValues) {
        return normalize(sourceList, allowedValues, true);
    }

    private static List<String> normalize(
            List<String> sourceList,
            Collection<String> allowedValues,
            boolean upperCase) {

        List<String> normalizedList = new ArrayList<String>();

        if (sourceList == null) {
            return normalizedList;
        }

        for (String value : sourceList) {
            if (value == null) {
                continue;
            }

            String normalized = value.trim();
            if (upperCase) {
                normalized = normalized.toUpperCase(Locale.ROOT);
            }

            boolean allowed = allowedValues == null
                    || allowedValues.contains(normalized);

            if (!normalized.isEmpty()
                    && allowed
                    && !normalizedList.contains(normalized)) {
                normalizedList.add(normalized);
            }
        }

        return normalizedList;
    }
}
