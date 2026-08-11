package com.project.oditji.chat.vo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

/** ChatResponseVO의 기본/전체 생성자와 모든 접근자를 검증합니다. */
class ChatResponseVOCompleteCoverageTest {

    @Test
    void constructorsAndAccessorsShouldExposeAllValues() {
        ChatResponseVO empty = new ChatResponseVO();
        assertFalse(empty.isSuccess());
        assertEquals(0, empty.getCode());
        assertNull(empty.getMessage());

        empty.setSuccess(true);
        empty.setCode(200);
        empty.setMessage("성공");

        assertTrue(empty.isSuccess());
        assertEquals(200, empty.getCode());
        assertEquals("성공", empty.getMessage());

        ChatResponseVO created = new ChatResponseVO(false, 400, "실패");
        assertFalse(created.isSuccess());
        assertEquals(400, created.getCode());
        assertEquals("실패", created.getMessage());
    }
}
