package com.project.oditji.search.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

/** 지원 문자 스크립트 판별 OR 조건의 양쪽 분기를 직접 보완합니다. */
class SearchContentPolicyServiceScriptBranchClosureTest {

    private SearchContentPolicyService service;

    @BeforeEach
    void setUp() {
        service = new SearchContentPolicyService();
    }

    @Test
    void supportedScriptShouldCoverHangulLatinAndUnsupportedBranches() {
        assertEquals(Boolean.TRUE, invokeSupported('한'));
        assertEquals(Boolean.TRUE, invokeSupported('A'));
        assertEquals(Boolean.FALSE, invokeSupported('Я'));
    }

    private Boolean invokeSupported(char value) {
        return ReflectionTestUtils.invokeMethod(
                service,
                "isSupportedTitleScript",
                (int) value);
    }
}
