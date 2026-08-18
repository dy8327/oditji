package com.project.oditji.subscription.vo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

/** 보고 싶은 콘텐츠 VO의 플랫폼/로고 목록 null 정규화 분기를 보완합니다. */
class ContentWishItemVOFinalConditionCoverageTest {

    @Test
    void platformNameListShouldNormalizeNullAndKeepProvidedList() {
        ContentWishItemVO item = new ContentWishItemVO();

        item.setPlatformNameList(null);
        assertTrue(item.getPlatformNameList().isEmpty());

        List<String> platformNames = new ArrayList<String>();
        platformNames.add("Netflix");
        item.setPlatformNameList(platformNames);

        assertSame(platformNames, item.getPlatformNameList());
        assertEquals(List.of("Netflix"), item.getPlatformNameList());
    }

    @Test
    void platformLogoListShouldNormalizeNullAndKeepProvidedList() {
        ContentWishItemVO item = new ContentWishItemVO();

        assertTrue(item.getPlatformLogoList().isEmpty());

        item.setPlatformLogoList(null);
        assertTrue(item.getPlatformLogoList().isEmpty());

        List<String> logoList = new ArrayList<String>();
        logoList.add("/netflix.png");
        item.setPlatformLogoList(logoList);

        assertSame(logoList, item.getPlatformLogoList());
        assertEquals(List.of("/netflix.png"), item.getPlatformLogoList());
    }
}
