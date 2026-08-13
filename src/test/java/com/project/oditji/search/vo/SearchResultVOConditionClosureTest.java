package com.project.oditji.search.vo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.project.oditji.tmdb.vo.OttPlatformVO;

/** SearchResultVO의 title/platformList 잔여 조건을 보완합니다. */
class SearchResultVOConditionClosureTest {

    @Test
    void titleShouldCoverNullBlankAndPresentValues() {
        SearchResultVO result = new SearchResultVO();

        assertEquals("제목 없음", result.getTitle());

        result.setTitle("   ");
        assertEquals("제목 없음", result.getTitle());

        result.setTitle("테스트 콘텐츠");
        assertEquals("테스트 콘텐츠", result.getTitle());
    }

    @Test
    void platformListShouldReplaceNullAndKeepProvidedList() {
        SearchResultVO result = new SearchResultVO();

        result.setPlatformList(null);
        assertNotNull(result.getPlatformList());
        assertTrue(result.getPlatformList().isEmpty());

        List<OttPlatformVO> platforms = List.of(new OttPlatformVO());
        result.setPlatformList(platforms);
        assertSame(platforms, result.getPlatformList());
    }
}
