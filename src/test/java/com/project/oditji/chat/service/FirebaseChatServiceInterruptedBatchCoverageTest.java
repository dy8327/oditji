package com.project.oditji.chat.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.SetOptions;
import com.google.cloud.firestore.WriteResult;
import com.google.firebase.auth.FirebaseAuth;
import com.project.oditji.chat.vo.ChatRoomVO;

/**
 * 여러 Firestore 쓰기를 기다리는 중 인터럽트되는 예외 처리 라인을 검증합니다.
 */
class FirebaseChatServiceInterruptedBatchCoverageTest {

    @AfterEach
    void clearInterruptFlag() {
        Thread.interrupted();
    }

    @Test
    void synchronizeCurrentUserShouldRestoreInterruptFlagWhenBatchWaitIsInterrupted() {
        @SuppressWarnings("unchecked")
        ObjectProvider<FirebaseAuth> authProvider = mock(ObjectProvider.class);
        @SuppressWarnings("unchecked")
        ObjectProvider<Firestore> firestoreProvider = mock(ObjectProvider.class);
        Firestore firestore = mock(Firestore.class);
        CollectionReference roomCollection = mock(CollectionReference.class);
        DocumentReference roomReference = mock(DocumentReference.class);
        @SuppressWarnings("unchecked")
        ApiFuture<WriteResult> pendingWrite = mock(ApiFuture.class);

        when(firestoreProvider.getIfAvailable()).thenReturn(firestore);
        when(firestore.collection("chatRooms")).thenReturn(roomCollection);
        when(roomCollection.document(anyString())).thenReturn(roomReference);
        when(roomReference.set(anyMap(), any(SetOptions.class))).thenReturn(pendingWrite);

        FirebaseChatService service = new FirebaseChatService(
                true,
                authProvider,
                firestoreProvider);

        ChatRoomVO noticeRoom = new ChatRoomVO();
        noticeRoom.setRoomId("ROOM_INTERRUPT");
        noticeRoom.setRoomName("공지방");
        noticeRoom.setRoomType("NOTICE");
        noticeRoom.setMaxMember(100);

        Thread.currentThread().interrupt();

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> service.synchronizeCurrentUser(
                        List.of(noticeRoom),
                        List.of(),
                        10L,
                        20,
                        "ADMIN",
                        "관리자"));

        assertTrue(exception.getMessage().contains("동기화가 중단"));
        assertTrue(Thread.currentThread().isInterrupted());
    }
}
