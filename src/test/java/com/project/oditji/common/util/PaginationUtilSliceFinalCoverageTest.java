package com.project.oditji.common.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.project.oditji.common.vo.PageVO;

/** PaginationUtil.slice의 null/empty/out-of-range/부분 페이지 분기를 검증합니다. */
class PaginationUtilSliceFinalCoverageTest {

    @Test
    void sliceShouldCoverNullEmptyOutOfRangeAndPartialPage() {
        PageVO firstPage = PaginationUtil.createPage(1, 2, 3);
        assertTrue(PaginationUtil.slice(null, firstPage).isEmpty());
        assertTrue(PaginationUtil.slice(List.of(), firstPage).isEmpty());

        PageVO outOfRange = new PageVO();
        outOfRange.setCurrentPage(5);
        outOfRange.setPageSize(2);
        assertTrue(PaginationUtil.slice(List.of("A", "B", "C"), outOfRange).isEmpty());

        PageVO secondPage = new PageVO();
        secondPage.setCurrentPage(2);
        secondPage.setPageSize(2);
        assertEquals(List.of("C"), PaginationUtil.slice(List.of("A", "B", "C"), secondPage));
    }
}
