package com.project.oditji.chat.vo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * 채팅 알림 컨텍스트 VO의 생성자와 roomList null 방어 양쪽 분기를 검증합니다.
 */
class ChatNotificationContextVOCoverageTest {

    @Test
    void defaultConstructorShouldStartWithEmptyRoomListAndRoundTripScalars() {
        ChatNotificationContextVO context =
                new ChatNotificationContextVO();

        context.setEnabled(true);
        context.setMemberNo(10L);
        context.setBusinessNo(20);
        context.setRole("BUSINESS");

        assertTrue(context.isEnabled());
        assertEquals(10L, context.getMemberNo());
        assertEquals(20, context.getBusinessNo());
        assertEquals("BUSINESS", context.getRole());
        assertTrue(context.getRoomList().isEmpty());
    }

    @Test
    void constructorShouldConvertNullRoomsToEmptyAndKeepSuppliedRooms() {
        ChatNotificationContextVO nullRooms =
                new ChatNotificationContextVO(
                        true,
                        1L,
                        2,
                        "BUSINESS",
                        null);

        assertTrue(
                nullRooms.getRoomList()
                        .isEmpty());

        ChatNotificationRoomVO room =
                new ChatNotificationRoomVO();
        List<ChatNotificationRoomVO> rooms =
                List.of(room);

        ChatNotificationContextVO supplied =
                new ChatNotificationContextVO(
                        true,
                        1L,
                        2,
                        "BUSINESS",
                        rooms);

        assertSame(
                rooms,
                supplied.getRoomList());
    }

    @Test
    void setterShouldConvertNullToEmptyAndKeepNonNullList() {
        ChatNotificationContextVO context =
                new ChatNotificationContextVO();

        context.setRoomList(null);
        assertTrue(
                context.getRoomList()
                        .isEmpty());

        List<ChatNotificationRoomVO> rooms =
                List.of(
                        new ChatNotificationRoomVO());

        context.setRoomList(rooms);

        assertSame(
                rooms,
                context.getRoomList());
    }
}
