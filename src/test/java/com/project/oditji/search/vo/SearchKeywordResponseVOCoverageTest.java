package com.project.oditji.search.vo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/** 최근 검색어 API 응답 VO의 성공/실패 값을 검증합니다. */
class SearchKeywordResponseVOCoverageTest {

    @Test
    void constructorShouldExposeSuccessAndMessageValues() {
        SearchKeywordResponseVO success =
                new SearchKeywordResponseVO(true, "완료");
        SearchKeywordResponseVO failure =
                new SearchKeywordResponseVO(false, "실패");

        assertTrue(success.isSuccess());
        assertEquals("완료", success.getMessage());
        assertFalse(failure.isSuccess());
        assertEquals("실패", failure.getMessage());
    }
}
