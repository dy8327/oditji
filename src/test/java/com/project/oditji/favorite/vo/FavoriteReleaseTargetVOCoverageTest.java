package com.project.oditji.favorite.vo;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/** 공개 예정 찜 콘텐츠 알림 대상 VO의 접근자를 검증합니다. */
class FavoriteReleaseTargetVOCoverageTest {

    @Test
    void gettersAndSettersShouldPreserveEveryValue() {
        FavoriteReleaseTargetVO target = new FavoriteReleaseTargetVO();

        target.setMemberNo(10L);
        target.setContentNo(20L);
        target.setTmdbId(30L);
        target.setContentType("TV");
        target.setTitle("공개 예정 작품");

        assertEquals(Long.valueOf(10L), target.getMemberNo());
        assertEquals(Long.valueOf(20L), target.getContentNo());
        assertEquals(Long.valueOf(30L), target.getTmdbId());
        assertEquals("TV", target.getContentType());
        assertEquals("공개 예정 작품", target.getTitle());
    }
}
