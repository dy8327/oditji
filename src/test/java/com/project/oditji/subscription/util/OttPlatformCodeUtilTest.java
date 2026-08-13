package com.project.oditji.subscription.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

/** 콘텐츠 플랫폼명(영문)과 OTT_DISCOUNT_INFO.PLATFORM_CODE 간 매칭을 검증합니다. */
class OttPlatformCodeUtilTest {

    @Test
    void shouldMatchExactContentPlatformNames() {
        assertEquals("NETFLIX", OttPlatformCodeUtil.fromContentPlatformName("Netflix"));
        assertEquals("TVING", OttPlatformCodeUtil.fromContentPlatformName("TVING"));
        assertEquals("WAVVE", OttPlatformCodeUtil.fromContentPlatformName("wavve"));
        assertEquals("DISNEY", OttPlatformCodeUtil.fromContentPlatformName("Disney Plus"));
        assertEquals("WATCHA", OttPlatformCodeUtil.fromContentPlatformName("Watcha"));
        assertEquals("COUPANG", OttPlatformCodeUtil.fromContentPlatformName("Coupangplay"));
    }

    @Test
    void shouldIgnoreCaseSpacingAndPlusSign() {
        assertEquals("DISNEY", OttPlatformCodeUtil.fromContentPlatformName("disney+"));
        assertEquals("DISNEY", OttPlatformCodeUtil.fromContentPlatformName("  DISNEY PLUS  "));
        assertEquals("NETFLIX", OttPlatformCodeUtil.fromContentPlatformName("netflix"));
    }

    @Test
    void shouldReturnNullForUnknownOrNullName() {
        assertNull(OttPlatformCodeUtil.fromContentPlatformName("Apple TV+"));
        assertNull(OttPlatformCodeUtil.fromContentPlatformName(null));
    }
}
