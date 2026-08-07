package com.project.oditji.chat.vo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/** Firebase 채팅 토큰 응답 VO의 생성자와 접근자를 검증합니다. */
class FirebaseChatTokenVOCoverageTest {

    @Test
    void defaultConstructorAndSettersShouldStoreEveryField() {
        FirebaseChatTokenVO token = new FirebaseChatTokenVO();

        assertFalse(token.isSuccess());
        assertFalse(token.isEnabled());
        assertNull(token.getToken());
        assertNull(token.getUid());
        assertNull(token.getMessage());

        token.setSuccess(true);
        token.setEnabled(true);
        token.setToken("custom-token");
        token.setUid("member-10");
        token.setMessage("정상 발급");

        assertTrue(token.isSuccess());
        assertTrue(token.isEnabled());
        assertEquals("custom-token", token.getToken());
        assertEquals("member-10", token.getUid());
        assertEquals("정상 발급", token.getMessage());
    }

    @Test
    void fullConstructorShouldInitializeEveryField() {
        FirebaseChatTokenVO token = new FirebaseChatTokenVO(
                true,
                false,
                "token",
                "member-7",
                "message");

        assertTrue(token.isSuccess());
        assertFalse(token.isEnabled());
        assertEquals("token", token.getToken());
        assertEquals("member-7", token.getUid());
        assertEquals("message", token.getMessage());
    }
}
