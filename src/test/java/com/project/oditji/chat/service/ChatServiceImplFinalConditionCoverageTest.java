package com.project.oditji.chat.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.verifyNoInteractions;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.project.oditji.chat.dao.ChatDAO;
import com.project.oditji.chat.vo.ChatReadStateVO;

/**
 * 읽음 상태 저장의 각 단락 조건을 독립적으로 검증합니다.
 */
@ExtendWith(MockitoExtension.class)
class ChatServiceImplFinalConditionCoverageTest {

    @Mock
    private ChatDAO chatDAO;

    private ChatServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new ChatServiceImpl(chatDAO);
    }

    @Test
    void readStateValidationShouldCoverBlankAndNullFieldsIndependently() {
        ChatReadStateVO blankRoom = validState();
        blankRoom.setRoomId("   ");
        assertFalse(service.saveChatReadState(blankRoom));

        ChatReadStateVO nullMember = validState();
        nullMember.setMemberNo(null);
        assertFalse(service.saveChatReadState(nullMember));

        ChatReadStateVO nullMessage = validState();
        nullMessage.setLastReadMessageId(null);
        assertFalse(service.saveChatReadState(nullMessage));

        ChatReadStateVO blankMessage = validState();
        blankMessage.setLastReadMessageId("   ");
        assertFalse(service.saveChatReadState(blankMessage));

        ChatReadStateVO nullEpoch = validState();
        nullEpoch.setLastReadEpochMs(null);
        assertFalse(service.saveChatReadState(nullEpoch));

        verifyNoInteractions(chatDAO);
    }

    private ChatReadStateVO validState() {
        ChatReadStateVO state = new ChatReadStateVO();
        state.setRoomId("ROOM_1");
        state.setMemberNo(10L);
        state.setLastReadMessageId("MESSAGE_1");
        state.setLastReadEpochMs(100L);
        return state;
    }
}
