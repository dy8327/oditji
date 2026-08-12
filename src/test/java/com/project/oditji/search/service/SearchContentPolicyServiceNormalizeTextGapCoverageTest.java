package com.project.oditji.search.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

/** 검색 노출 정책 문자열 정규화의 null 반환 분기를 보완합니다. */
class SearchContentPolicyServiceNormalizeTextGapCoverageTest {

    @Test
    void normalizeTextShouldCoverNullAndNfkcLowerCasePaths() {
        SearchContentPolicyService service = new SearchContentPolicyService();

        String nullValue = ReflectionTestUtils.invokeMethod(
                service,
                "normalizeText",
                (Object) null);

        String normalized = ReflectionTestUtils.invokeMethod(
                service,
                "normalizeText",
                "ＡＢＣ");

        assertEquals("", nullValue);
        assertEquals("abc", normalized);
    }
}
