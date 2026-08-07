package com.project.oditji.chat.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectProvider;

import com.google.api.core.ApiFuture;
import com.google.api.core.ApiFutures;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.SetOptions;
import com.google.cloud.firestore.WriteResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.project.oditji.chat.vo.ChatRoomVO;

/** Firebase 채팅 인증과 Firestore 권한 동기화의 주요 분기를 검증합니다. */
@ExtendWith(MockitoExtension.class)
class FirebaseChatServiceCoverageTest {

    private static final long MEMBER_NO = 10L;
    private static final int BUSINESS_NO = 20;

    @Mock
    private ObjectProvider<FirebaseAuth> firebaseAuthProvider;

    @Mock
    private ObjectProvider<Firestore> firestoreProvider;

    @Mock
    private FirebaseAuth firebaseAuth;

    @Mock
    private Firestore firestore;

    @Mock
    private CollectionReference roomCollection;

    @Mock
    private DocumentReference roomReference;

    @Mock
    private CollectionReference memberCollection;

    @Mock
    private DocumentReference memberReference;

    @Mock
    private WriteResult writeResult;

    @AfterEach
    void clearInterruptedFlag() {
        Thread.interrupted();
    }

    @Test
    void isEnabledShouldRequireConfigurationAuthenticationAndFirestore() {
        FirebaseChatService disabledService = service(false);
        assertFalse(disabledService.isEnabled());
        verifyNoInteractions(firebaseAuthProvider, firestoreProvider);

        FirebaseChatService enabledService = service(true);
        when(firebaseAuthProvider.getIfAvailable()).thenReturn(null);
        assertFalse(enabledService.isEnabled());
        verifyNoInteractions(firestoreProvider);

        when(firebaseAuthProvider.getIfAvailable()).thenReturn(firebaseAuth);
        when(firestoreProvider.getIfAvailable()).thenReturn(null);
        assertFalse(enabledService.isEnabled());

        when(firestoreProvider.getIfAvailable()).thenReturn(firestore);
        assertTrue(enabledService.isEnabled());
    }

    @Test
    void createCustomTokenShouldNormalizeClaimsAndDisplayName() throws Exception {
        FirebaseChatService service = service(true);
        when(firebaseAuthProvider.getIfAvailable()).thenReturn(firebaseAuth);
        when(firebaseAuth.createCustomToken(anyString(), anyMap()))
                .thenReturn("token-1", "token-2");

        String firstToken = service.createCustomToken(
                MEMBER_NO,
                BUSINESS_NO,
                " business ",
                "   ");

        String longName = "가".repeat(101);
        String secondToken = service.createCustomToken(
                MEMBER_NO + 1,
                BUSINESS_NO + 1,
                "admin",
                longName);

        assertEquals("token-1", firstToken);
        assertEquals("token-2", secondToken);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String, Object>> claimsCaptor =
                ArgumentCaptor.forClass(Map.class);

        verify(firebaseAuth, times(2)).createCustomToken(
                anyString(),
                claimsCaptor.capture());

        Map<String, Object> businessClaims = claimsCaptor.getAllValues().get(0);
        assertEquals(MEMBER_NO, businessClaims.get("memberNo"));
        assertEquals(BUSINESS_NO, businessClaims.get("businessNo"));
        assertEquals("BUSINESS", businessClaims.get("role"));
        assertEquals("ODITJI 사용자", businessClaims.get("displayName"));
        assertEquals(Boolean.TRUE, businessClaims.get("chatEnabled"));

        Map<String, Object> adminClaims = claimsCaptor.getAllValues().get(1);
        assertEquals("ADMIN", adminClaims.get("role"));
        assertEquals(100, ((String) adminClaims.get("displayName")).length());
        assertEquals("member-10", service.createUid(MEMBER_NO));
    }

    @Test
    void createCustomTokenShouldRejectUnsupportedRoleAndWrapFirebaseFailure()
            throws Exception {

        FirebaseChatService service = service(true);
        when(firebaseAuthProvider.getIfAvailable()).thenReturn(firebaseAuth);

        assertThrows(
                IllegalArgumentException.class,
                () -> service.createCustomToken(
                        MEMBER_NO,
                        BUSINESS_NO,
                        "USER",
                        "사용자"));
        assertThrows(
                IllegalArgumentException.class,
                () -> service.createCustomToken(
                        MEMBER_NO,
                        BUSINESS_NO,
                        null,
                        "사용자"));

        FirebaseAuthException firebaseException = mock(FirebaseAuthException.class);
        when(firebaseAuth.createCustomToken(anyString(), anyMap()))
                .thenThrow(firebaseException);

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> service.createCustomToken(
                        MEMBER_NO,
                        BUSINESS_NO,
                        "ADMIN",
                        "관리자"));

        assertEquals(firebaseException, exception.getCause());
    }

    @Test
    void createCustomTokenShouldFailWhenFirebaseIsDisabledOrMissing() {
        FirebaseChatService disabledService = service(false);

        assertThrows(
                IllegalStateException.class,
                () -> disabledService.createCustomToken(
                        MEMBER_NO,
                        BUSINESS_NO,
                        "ADMIN",
                        "관리자"));

        FirebaseChatService missingAuthService = service(true);
        when(firebaseAuthProvider.getIfAvailable()).thenReturn(null);

        assertThrows(
                IllegalStateException.class,
                () -> missingAuthService.createCustomToken(
                        MEMBER_NO,
                        BUSINESS_NO,
                        "ADMIN",
                        "관리자"));
    }

    @Test
    void synchronizeCurrentUserShouldMergeRoomsAndManagePublicMemberships() {
        FirebaseChatService service = service(true);
        stubFirestoreHierarchy();
        ApiFuture<WriteResult> success = ApiFutures.immediateFuture(writeResult);
        when(roomReference.set(anyMap(), any(SetOptions.class))).thenReturn(success);
        when(memberReference.set(anyMap(), any(SetOptions.class))).thenReturn(success);
        when(memberReference.delete()).thenReturn(success);

        ChatRoomVO joinedPublic = room(" ROOM_1 ", "자유방 1", "PUBLIC", 30);
        ChatRoomVO notJoinedPublic = room("ROOM_2", null, "PUBLIC", 40);
        ChatRoomVO notice = room("ROOM_3", "공지방", "NOTICE", 100);
        ChatRoomVO blankId = room("   ", "빈 방", "PUBLIC", 10);

        List<ChatRoomVO> roomList = new ArrayList<>();
        roomList.add(null);
        roomList.add(joinedPublic);
        roomList.add(notJoinedPublic);
        roomList.add(notice);
        roomList.add(blankId);

        List<ChatRoomVO> joinedRoomList = new ArrayList<>();
        joinedRoomList.add(null);
        joinedRoomList.add(joinedPublic);
        joinedRoomList.add(blankId);

        service.synchronizeCurrentUser(
                roomList,
                joinedRoomList,
                MEMBER_NO,
                BUSINESS_NO,
                " business ",
                " 사업자 ");

        verify(roomCollection).document("ROOM_1");
        verify(roomCollection).document("ROOM_2");
        verify(roomCollection).document("ROOM_3");
        verify(roomReference, times(3)).set(anyMap(), any(SetOptions.class));
        verify(memberReference).set(anyMap(), any(SetOptions.class));
        verify(memberReference).delete();

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String, Object>> memberDataCaptor =
                ArgumentCaptor.forClass(Map.class);
        verify(memberReference).set(
                memberDataCaptor.capture(),
                any(SetOptions.class));

        Map<String, Object> memberData = memberDataCaptor.getValue();
        assertEquals(MEMBER_NO, memberData.get("memberNo"));
        assertEquals(BUSINESS_NO, memberData.get("businessNo"));
        assertEquals("BUSINESS", memberData.get("role"));
        assertEquals("사업자", memberData.get("displayName"));
        assertEquals(Boolean.TRUE, memberData.get("active"));
        assertNotNull(memberData.get("updatedAt"));
    }

    @Test
    void synchronizeCurrentUserShouldHandleNullListsAndAdminMembershipDeletion() {
        FirebaseChatService service = service(true);
        when(firestoreProvider.getIfAvailable()).thenReturn(firestore);

        service.synchronizeCurrentUser(
                null,
                null,
                MEMBER_NO,
                BUSINESS_NO,
                "ADMIN",
                null);

        verifyNoInteractions(firestore);

        stubFirestoreHierarchy();
        ApiFuture<WriteResult> success = ApiFutures.immediateFuture(writeResult);
        when(roomReference.set(anyMap(), any(SetOptions.class))).thenReturn(success);
        when(memberReference.delete()).thenReturn(success);

        ChatRoomVO publicRoom = room("ROOM_4", null, "PUBLIC", 50);
        service.synchronizeCurrentUser(
                List.of(publicRoom),
                null,
                MEMBER_NO,
                BUSINESS_NO,
                "ADMIN",
                null);

        verify(memberReference).delete();
        verify(memberReference, never()).set(anyMap(), any(SetOptions.class));
    }

    @Test
    void synchronizeRoomShouldIgnoreMissingRoomAndWriteActiveMetadata() {
        FirebaseChatService service = service(true);

        service.synchronizeRoom(null);
        ChatRoomVO blankRoom = room(" ", "빈 방", "PUBLIC", 10);
        service.synchronizeRoom(blankRoom);
        verifyNoInteractions(firestoreProvider);

        stubFirestoreHierarchy();
        when(roomReference.set(anyMap(), any(SetOptions.class)))
                .thenReturn(ApiFutures.immediateFuture(writeResult));

        ChatRoomVO room = room(" ROOM_5 ", null, null, 25);
        service.synchronizeRoom(room);

        verify(roomCollection).document("ROOM_5");

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String, Object>> roomDataCaptor =
                ArgumentCaptor.forClass(Map.class);
        verify(roomReference).set(
                roomDataCaptor.capture(),
                any(SetOptions.class));

        Map<String, Object> roomData = roomDataCaptor.getValue();
        assertEquals(" ROOM_5 ", roomData.get("roomId"));
        assertEquals("", roomData.get("roomName"));
        assertEquals("", roomData.get("roomType"));
        assertEquals("ACTIVE", roomData.get("status"));
        assertEquals(25, roomData.get("maxMember"));
        assertNotNull(roomData.get("updatedAt"));
    }

    @Test
    void addAndRemoveRoomMemberShouldUseNormalizedMemberDocument() {
        FirebaseChatService service = service(true);
        stubFirestoreHierarchy();
        ApiFuture<WriteResult> success = ApiFutures.immediateFuture(writeResult);
        when(memberReference.set(anyMap(), any(SetOptions.class))).thenReturn(success);
        when(memberReference.delete()).thenReturn(success);

        service.addRoomMember(
                " ROOM_6 ",
                MEMBER_NO,
                BUSINESS_NO,
                " business ",
                "   ");
        service.removeRoomMember(" ROOM_6 ", MEMBER_NO);

        verify(roomCollection, times(2)).document("ROOM_6");
        verify(memberCollection, times(2)).document("member-10");
        verify(memberReference).delete();

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String, Object>> memberDataCaptor =
                ArgumentCaptor.forClass(Map.class);
        verify(memberReference).set(
                memberDataCaptor.capture(),
                any(SetOptions.class));

        assertEquals("BUSINESS", memberDataCaptor.getValue().get("role"));
        assertEquals("ODITJI 사용자", memberDataCaptor.getValue().get("displayName"));
    }

    @Test
    void deactivateRoomShouldIgnoreBlankIdAndWriteInactiveStatus() {
        FirebaseChatService service = service(true);

        service.deactivateRoom(null);
        service.deactivateRoom("   ");
        verifyNoInteractions(firestoreProvider);

        stubFirestoreHierarchy();
        when(roomReference.set(anyMap(), any(SetOptions.class)))
                .thenReturn(ApiFutures.immediateFuture(writeResult));

        service.deactivateRoom(" ROOM_7 ");

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String, Object>> dataCaptor =
                ArgumentCaptor.forClass(Map.class);
        verify(roomReference).set(dataCaptor.capture(), any(SetOptions.class));
        assertEquals("INACTIVE", dataCaptor.getValue().get("status"));
        assertNotNull(dataCaptor.getValue().get("updatedAt"));
    }

    @Test
    void firestoreOperationsShouldRejectDisabledMissingAndBlankRoomId() {
        FirebaseChatService disabledService = service(false);
        ChatRoomVO room = room("ROOM_8", "방", "PUBLIC", 10);

        List<ChatRoomVO> roomList = List.of(room);
        List<ChatRoomVO> emptyJoinedRoomList = List.of();

        assertThrows(
                IllegalStateException.class,
                () -> disabledService.synchronizeCurrentUser(
                        roomList,
                        emptyJoinedRoomList,
                        MEMBER_NO,
                        BUSINESS_NO,
                        "BUSINESS",
                        "사업자"));

        FirebaseChatService missingFirestoreService = service(true);
        when(firestoreProvider.getIfAvailable()).thenReturn(null);
        assertThrows(
                IllegalStateException.class,
                () -> missingFirestoreService.addRoomMember(
                        "ROOM_8",
                        MEMBER_NO,
                        BUSINESS_NO,
                        "BUSINESS",
                        "사업자"));

        when(firestoreProvider.getIfAvailable()).thenReturn(firestore);
        assertThrows(
                IllegalArgumentException.class,
                () -> missingFirestoreService.addRoomMember(
                        "   ",
                        MEMBER_NO,
                        BUSINESS_NO,
                        "BUSINESS",
                        "사업자"));
    }

    @Test
    void singleWriteFailureShouldBeWrapped() throws Exception {
        FirebaseChatService service = service(true);
        stubFirestoreHierarchy();

        @SuppressWarnings("unchecked")
        ApiFuture<WriteResult> failedFuture = mock(ApiFuture.class);
        when(memberReference.delete()).thenReturn(failedFuture);
        when(failedFuture.get(anyLong(), eq(TimeUnit.SECONDS)))
                .thenThrow(new ExecutionException(new IllegalStateException("write failed")));

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> service.removeRoomMember("ROOM_9", MEMBER_NO));

        assertTrue(exception.getMessage().contains("Firestore 권한 문서 처리에 실패"));
    }

    @Test
    void interruptedSingleWriteShouldRestoreInterruptFlag() throws Exception {
        FirebaseChatService service = service(true);
        stubFirestoreHierarchy();

        @SuppressWarnings("unchecked")
        ApiFuture<WriteResult> interruptedFuture = mock(ApiFuture.class);
        when(memberReference.delete()).thenReturn(interruptedFuture);
        when(interruptedFuture.get(anyLong(), eq(TimeUnit.SECONDS)))
                .thenThrow(new InterruptedException("interrupted"));

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> service.removeRoomMember("ROOM_10", MEMBER_NO));

        assertTrue(exception.getMessage().contains("Firestore 권한 문서 처리가 중단"));
        assertTrue(Thread.currentThread().isInterrupted());
    }

    @Test
    void multipleWriteFailureShouldBeWrapped() {
        FirebaseChatService service = service(true);
        stubFirestoreHierarchy();

        when(roomReference.set(anyMap(), any(SetOptions.class)))
                .thenReturn(ApiFutures.immediateFailedFuture(
                        new IllegalStateException("room write failed")));
        when(memberReference.delete())
                .thenReturn(ApiFutures.immediateFuture(writeResult));

        ChatRoomVO room = room("ROOM_11", "자유방", "PUBLIC", 10);

        List<ChatRoomVO> roomList = List.of(room);

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> service.synchronizeCurrentUser(
                        roomList,
                        null,
                        MEMBER_NO,
                        BUSINESS_NO,
                        "ADMIN",
                        "관리자"));

        assertTrue(exception.getMessage().contains("Firestore 권한 문서 동기화에 실패"));
    }

    private FirebaseChatService service(boolean enabled) {
        return new FirebaseChatService(
                enabled,
                firebaseAuthProvider,
                firestoreProvider);
    }

    private void stubFirestoreHierarchy() {
        org.mockito.Mockito.lenient()
                .when(firestoreProvider.getIfAvailable())
                .thenReturn(firestore);
        org.mockito.Mockito.lenient()
                .when(firestore.collection("chatRooms"))
                .thenReturn(roomCollection);
        org.mockito.Mockito.lenient()
                .when(roomCollection.document(anyString()))
                .thenReturn(roomReference);
        org.mockito.Mockito.lenient()
                .when(roomReference.collection("members"))
                .thenReturn(memberCollection);
        org.mockito.Mockito.lenient()
                .when(memberCollection.document(anyString()))
                .thenReturn(memberReference);
    }

    private ChatRoomVO room(
            String roomId,
            String roomName,
            String roomType,
            int maxMember) {

        ChatRoomVO room = new ChatRoomVO();
        room.setRoomId(roomId);
        room.setRoomName(roomName);
        room.setRoomType(roomType);
        room.setMaxMember(maxMember);
        return room;
    }
}
