package com.project.oditji.search.vo;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDateTime;
import java.time.Month;

import org.junit.jupiter.api.Test;

/** SearchKeywordHistoryVO의 모든 접근자 경로를 검증합니다. */
class SearchKeywordHistoryVOCoverageTest {

    @Test
    void gettersAndSettersShouldPreserveAssignedValues() {
        SearchKeywordHistoryVO vo = new SearchKeywordHistoryVO();
        LocalDateTime first = LocalDateTime.of(2026, Month.AUGUST, 10, 10, 20);
        LocalDateTime last = LocalDateTime.of(2026, Month.AUGUST, 11, 16, 30);

        vo.setSearchHistoryNo(1L);
        vo.setMemberNo(2L);
        vo.setKeyword("검색어");
        vo.setSearchCount(3);
        vo.setFirstSearchedAt(first);
        vo.setLastSearchedAt(last);

        assertEquals(1L, vo.getSearchHistoryNo());
        assertEquals(2L, vo.getMemberNo());
        assertEquals("검색어", vo.getKeyword());
        assertEquals(3, vo.getSearchCount());
        assertEquals(first, vo.getFirstSearchedAt());
        assertEquals(last, vo.getLastSearchedAt());
    }
}
