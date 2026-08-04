package com.project.oditji.common.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.project.oditji.common.vo.PageVO;

/**
 * 관리자 목록에서 사용하는 공통 페이징 계산 기능을 검증합니다.
 */
class PaginationUtilTest {

    @Test
    void buildShouldCreateFirstPageBlock() {

        PageVO result = PaginationUtil.build(1, 52, 10, 5); 

        assertEquals(1, result.getCurrentPage());
        assertEquals(10, result.getPageSize());
        assertEquals(52, result.getTotalCount());
        assertEquals(6, result.getTotalPage());
        assertEquals(1, result.getStartPage());
        assertEquals(5, result.getEndPage());
        assertFalse(result.isPrev());
        assertTrue(result.isNext());
    }

    @Test
    void buildShouldCorrectPageGreaterThanTotalPage() {

        PageVO result = PaginationUtil.build(999, 52, 10, 5);

        assertEquals(6, result.getCurrentPage());
        assertEquals(6, result.getStartPage());
        assertEquals(6, result.getEndPage());
        assertTrue(result.isPrev());
        assertFalse(result.isNext());
    }

    @Test
    void buildShouldReturnOnePageWhenTotalCountIsZero() {

        PageVO result = PaginationUtil.build(0, 0, 10, 5);

        assertEquals(1, result.getCurrentPage());
        assertEquals(1, result.getTotalPage());
        assertEquals(1, result.getStartPage());
        assertEquals(1, result.getEndPage());
        assertFalse(result.isPrev());
        assertFalse(result.isNext());
    }

    @Test
    void offsetShouldCalculateSkippedRowCount() {
        assertEquals(20, PaginationUtil.offset(3, 10));
    }

    @Test
    void offsetShouldCorrectInvalidPageNumber() {
        assertEquals(0, PaginationUtil.offset(-1, 10));
    }
}
