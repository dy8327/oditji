package com.project.oditji.common.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * 이용약관 및 개인정보 처리방침 화면의
 * View 경로 반환을 검증합니다.
 */
class LegalControllerTest {

    private LegalController controller;

    @BeforeEach
    void setUp() {
        controller = new LegalController();
    }

    @Test
    void termsShouldReturnTermsView() {
        String view = controller.terms();

        assertEquals("legal/terms", view);
    }

    @Test
    void privacyShouldReturnPrivacyView() {
        String view = controller.privacy();

        assertEquals("legal/privacy", view);
    }
}