package com.project.oditji.chat.vo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Date;

import org.junit.jupiter.api.Test;

/**
 * 채팅방 참여자 VO의 생성자, 접근자와 문자열 표현을 검증합니다.
 */
class ChatRoomMemberVOCoverageTest {

    @Test
    void fullConstructorAndAccessorsShouldExposeParticipantData() {
        Date joinedAt = new Date(1234L);
        ChatRoomMemberVO member = new ChatRoomMemberVO(
                "room-1",
                20,
                joinedAt,
                "공지방 참여자",
                "Y");

        assertEquals("room-1", member.getRoomId());
        assertEquals(20, member.getBusinessNo());
        assertEquals(joinedAt, member.getJoinDate());
        assertEquals("공지방 참여자", member.getDescription());
        assertEquals("Y", member.getIsDefault());
        assertTrue(member.toString().contains("room-1"));
        assertTrue(member.toString().contains("businessNo=20"));
    }

    @Test
    void defaultConstructorSettersShouldUpdateParticipantData() {
        ChatRoomMemberVO member = new ChatRoomMemberVO();
        Date joinedAt = new Date(5678L);

        member.setRoomId("room-2");
        member.setBusinessNo(30);
        member.setJoinDate(joinedAt);
        member.setDescription("자유방 참여자");
        member.setIsDefault("N");

        assertEquals("room-2", member.getRoomId());
        assertEquals(30, member.getBusinessNo());
        assertEquals(joinedAt, member.getJoinDate());
        assertEquals("자유방 참여자", member.getDescription());
        assertEquals("N", member.getIsDefault());
    }
}
