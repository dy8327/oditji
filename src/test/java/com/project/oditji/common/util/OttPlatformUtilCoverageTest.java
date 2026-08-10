package com.project.oditji.common.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import com.project.oditji.tmdb.vo.OttPlatformVO;

/**
 * OTT 로고 변환 유틸의 null/name/logo 조건과 key 변환 분기를 검증합니다.
 */
class OttPlatformUtilCoverageTest {

    @Test
    void logoMapShouldSkipEveryUnusablePlatformShape() {
        OttPlatformVO noName = platform(null, "/a.png");
        OttPlatformVO noLogo = platform("Netflix", null);
        OttPlatformVO blankLogo = platform("TVING", "   ");
        OttPlatformVO valid = platform("Disney Plus", "/disney.png");

        Map<String, String> result =
                OttPlatformUtil.createLogoMap(
                        Arrays.asList(
                                null,
                                noName,
                                noLogo,
                                blankLogo,
                                valid));

        assertEquals(1, result.size());
        assertEquals("/disney.png", result.get("disney"));
    }

    @Test
    void nullListShouldReturnMutableEmptyMap() {
        Map<String, String> result =
                OttPlatformUtil.createLogoMap(null);

        assertTrue(result.isEmpty());

        result.put("x", "y");

        assertEquals("y", result.get("x"));
    }

    @Test
    void blankPlatformNameShouldPassLogoValidationButBeDroppedByEmptyKeyCheck() {
        Map<String, String> result =
                OttPlatformUtil.createLogoMap(
                        List.of(
                                platform("   ", "/blank-name.png")));

        assertTrue(result.isEmpty());
    }

    private OttPlatformVO platform(String name, String logo) {
        OttPlatformVO platform = new OttPlatformVO();
        platform.setPlatformName(name);
        platform.setLogoImage(logo);
        return platform;
    }
}
