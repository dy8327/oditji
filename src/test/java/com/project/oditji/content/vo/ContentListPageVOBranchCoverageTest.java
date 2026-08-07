package com.project.oditji.content.vo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.project.oditji.search.vo.SearchResultVO;

/** 콘텐츠 목록 페이지 VO의 null 보정 및 이전/다음 페이지 조건을 모두 검증합니다. */
class ContentListPageVOBranchCoverageTest {

    @Test
    void constructorAndNullListShouldUseSafeDefaults() {
        ContentListPageVO page = new ContentListPageVO();

        assertTrue(page.getContentList().isEmpty());
        assertEquals(1, page.getCurrentPage());
        assertEquals(0, page.getTotalPages());
        assertEquals(0, page.getTotalResults());
        assertFalse(page.isHasPrevious());
        assertFalse(page.isHasNext());

        page.setContentList(null);
        assertTrue(page.getContentList().isEmpty());
    }

    @Test
    void nonNullListAndPageFlagsShouldCoverAllConditions() {
        ContentListPageVO page = new ContentListPageVO();
        List<SearchResultVO> contentList = List.of(new SearchResultVO());
        page.setContentList(contentList);
        assertSame(contentList, page.getContentList());

        page.setCurrentPage(2);
        page.setTotalPages(3);
        page.setTotalResults(21);
        assertTrue(page.isHasPrevious());
        assertTrue(page.isHasNext());
        assertEquals(21, page.getTotalResults());

        page.setCurrentPage(3);
        assertFalse(page.isHasNext());

        page.setCurrentPage(1);
        page.setTotalPages(0);
        assertFalse(page.isHasPrevious());
        assertFalse(page.isHasNext());
    }
}
