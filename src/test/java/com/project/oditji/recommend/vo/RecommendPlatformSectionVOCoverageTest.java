package com.project.oditji.recommend.vo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.project.oditji.search.vo.SearchResultVO;

/**
 * OTT별 추천 섹션 VO의 기본값과 null 목록 방어 분기를 검증합니다.
 */
class RecommendPlatformSectionVOCoverageTest {

    @Test
    void contentListShouldNeverRemainNull() {
        RecommendPlatformSectionVO section = new RecommendPlatformSectionVO();

        assertTrue(section.getContentList().isEmpty());

        section.setPlatformNo(7L);
        section.setContentList(null);

        assertEquals(7L, section.getPlatformNo());
        assertTrue(section.getContentList().isEmpty());

        SearchResultVO content = new SearchResultVO();
        List<SearchResultVO> contentList = List.of(content);

        section.setContentList(contentList);

        assertSame(contentList, section.getContentList());
    }
}
