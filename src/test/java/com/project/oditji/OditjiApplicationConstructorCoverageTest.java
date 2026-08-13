package com.project.oditji;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

/** 애플리케이션 기본 생성자까지 실행해 진입점 클래스의 남은 라인을 보완합니다. */
class OditjiApplicationConstructorCoverageTest {

    @Test
    void applicationShouldBeConstructible() {
        assertNotNull(new OditjiApplication());
    }
}
