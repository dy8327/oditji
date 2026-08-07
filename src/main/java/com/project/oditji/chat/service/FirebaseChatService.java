package com.project.oditji.chat.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.google.api.core.ApiFuture;
import com.google.api.core.ApiFutures;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.FieldValue;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.SetOptions;
import com.google.cloud.firestore.WriteResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.project.oditji.chat.vo.ChatRoomVO;

/**
 * Firebase Custom Token 발급과 Firestore 채팅 권한 문서 동기화를 담당합니다.
 *
 * 브라우저는 messages 문서만 직접 다루고, chatRooms 및 members 권한 문서는
 * Firebase Admin SDK를 사용하는 이 서비스만 생성·수정·삭제합니다.
 */
@Service
public class FirebaseChatService {

    private static final String COLLECTION_CHAT_ROOMS = "chatRooms";
    private static final String COLLECTION_MEMBERS = "members";
    private static final String ROOM_TYPE_PUBLIC = "PUBLIC";
    private static final String STATUS_ACTIVE = "ACTIVE";
    private static final String STATUS_INACTIVE = "INACTIVE";
    private static final int FIREBASE_WAIT_SECONDS = 15;

    private final boolean configuredEnabled;
    private final ObjectProvider<FirebaseAuth> firebaseAuthProvider;
    private final ObjectProvider<Firestore> firestoreProvider;

    public FirebaseChatService(
            @Value("${firebase.chat.enabled:false}") boolean configuredEnabled,
            ObjectProvider<FirebaseAuth> firebaseAuthProvider,
            ObjectProvider<Firestore> firestoreProvider) {

        this.configuredEnabled = configuredEnabled;
        this.firebaseAuthProvider = firebaseAuthProvider;
        this.firestoreProvider = firestoreProvider;
    }

    /**
     * Firebase Admin SDK가 실제로 활성화되어 있는지 확인합니다.
     */
    public boolean isEnabled() {
        return configuredEnabled
                && firebaseAuthProvider.getIfAvailable() != null
                && firestoreProvider.getIfAvailable() != null;
    }

    /**
     * ODITJI 회원 한 명을 Firebase 사용자 UID 및 Custom Claim으로 변환합니다.
     */
    public String createCustomToken(
            long memberNo,
            int businessNo,
            String role,
            String displayName) {

        FirebaseAuth firebaseAuth = requireFirebaseAuth();
        String normalizedRole = normalizeRole(role);

        if (!"ADMIN".equals(normalizedRole)
                && !"BUSINESS".equals(normalizedRole)) {
            throw new IllegalArgumentException(
                    "Firebase 채팅 토큰을 발급할 수 없는 역할입니다.");
        }

        String uid = createUid(memberNo);

        Map<String, Object> claims = new HashMap<>();
        claims.put("memberNo", memberNo);
        claims.put("businessNo", businessNo);
        claims.put("role", normalizedRole);
        claims.put("displayName", normalizeDisplayName(displayName));
        claims.put("chatEnabled", true);

        try {
            return firebaseAuth.createCustomToken(uid, claims);
        } catch (FirebaseAuthException exception) {
            throw new IllegalStateException(
                    "Firebase 채팅 인증 토큰 발급에 실패했습니다.",
                    exception);
        }
    }

    /**
     * 현재 활성 채팅방과 로그인 사용자의 자유방 참가 상태를 Firestore에 맞춥니다.
     *
     * 기존 운영 전 메시지만 존재하고 chatRooms 메타 문서가 없는 경우도
     * 첫 인증 시 이 메서드가 부모 문서와 참가자 문서를 자동 생성합니다.
     */
    public void synchronizeCurrentUser(
            List<ChatRoomVO> roomList,
            List<ChatRoomVO> joinedRoomList,
            long memberNo,
            int businessNo,
            String role,
            String displayName) {

        Firestore firestore = requireFirestore();
        String uid = createUid(memberNo);
        String normalizedRole = normalizeRole(role);
        Set<String> joinedRoomIds = createRoomIdSet(joinedRoomList);
        List<ApiFuture<WriteResult>> writes = new ArrayList<>();

        if (roomList == null) {
            waitForWrites(writes);
            return;
        }

        for (ChatRoomVO room : roomList) {

            if (room == null || isBlank(room.getRoomId())) {
                continue;
            }

            DocumentReference roomReference = getRoomReference(
                    firestore,
                    room.getRoomId());

            writes.add(roomReference.set(
                    createRoomData(room, STATUS_ACTIVE),
                    SetOptions.merge()));

            if (!ROOM_TYPE_PUBLIC.equals(room.getRoomType())) {
                continue;
            }

            DocumentReference memberReference = roomReference
                    .collection(COLLECTION_MEMBERS)
                    .document(uid);

            if ("BUSINESS".equals(normalizedRole)
                    && joinedRoomIds.contains(room.getRoomId())) {

                writes.add(memberReference.set(
                        createMemberData(
                                memberNo,
                                businessNo,
                                normalizedRole,
                                displayName),
                        SetOptions.merge()));
            } else {
                writes.add(memberReference.delete());
            }
        }

        waitForWrites(writes);
    }

    /**
     * 새로 생성되거나 변경된 채팅방의 권한 메타데이터를 저장합니다.
     */
    public void synchronizeRoom(ChatRoomVO room) {

        if (room == null || isBlank(room.getRoomId())) {
            return;
        }

        Firestore firestore = requireFirestore();
        ApiFuture<WriteResult> write = getRoomReference(
                firestore,
                room.getRoomId())
                .set(
                        createRoomData(room, STATUS_ACTIVE),
                        SetOptions.merge());

        waitForWrite(write);
    }

    /**
     * 자유방 참가자의 Firestore 접근 권한 문서를 생성합니다.
     */
    public void addRoomMember(
            String roomId,
            long memberNo,
            int businessNo,
            String role,
            String displayName) {

        Firestore firestore = requireFirestore();
        DocumentReference memberReference = getRoomReference(
                firestore,
                roomId)
                .collection(COLLECTION_MEMBERS)
                .document(createUid(memberNo));

        waitForWrite(memberReference.set(
                createMemberData(
                        memberNo,
                        businessNo,
                        normalizeRole(role),
                        displayName),
                SetOptions.merge()));
    }

    /**
     * 자유방 나가기 전에 Firestore 접근 권한을 먼저 제거합니다.
     */
    public void removeRoomMember(String roomId, long memberNo) {

        Firestore firestore = requireFirestore();
        DocumentReference memberReference = getRoomReference(
                firestore,
                roomId)
                .collection(COLLECTION_MEMBERS)
                .document(createUid(memberNo));

        waitForWrite(memberReference.delete());
    }

    /**
     * 비활성화할 채팅방을 Firestore 규칙에서도 즉시 접근 불가 상태로 만듭니다.
     */
    public void deactivateRoom(String roomId) {

        if (isBlank(roomId)) {
            return;
        }

        Firestore firestore = requireFirestore();
        Map<String, Object> data = new HashMap<>();
        data.put("status", STATUS_INACTIVE);
        data.put("updatedAt", FieldValue.serverTimestamp());

        waitForWrite(getRoomReference(firestore, roomId)
                .set(data, SetOptions.merge()));
    }

    public String createUid(long memberNo) {
        return "member-" + memberNo;
    }

    private FirebaseAuth requireFirebaseAuth() {

        if (!configuredEnabled) {
            throw new IllegalStateException(
                    "Firebase 채팅 인증이 비활성화되어 있습니다.");
        }

        FirebaseAuth firebaseAuth = firebaseAuthProvider.getIfAvailable();

        if (firebaseAuth == null) {
            throw new IllegalStateException(
                    "Firebase Authentication 초기화 정보를 찾을 수 없습니다.");
        }

        return firebaseAuth;
    }

    private Firestore requireFirestore() {

        if (!configuredEnabled) {
            throw new IllegalStateException(
                    "Firebase 채팅 인증이 비활성화되어 있습니다.");
        }

        Firestore firestore = firestoreProvider.getIfAvailable();

        if (firestore == null) {
            throw new IllegalStateException(
                    "Firestore Admin 초기화 정보를 찾을 수 없습니다.");
        }

        return firestore;
    }

    private DocumentReference getRoomReference(
            Firestore firestore,
            String roomId) {

        if (isBlank(roomId)) {
            throw new IllegalArgumentException(
                    "Firestore에 동기화할 채팅방 ID가 없습니다.");
        }

        return firestore
                .collection(COLLECTION_CHAT_ROOMS)
                .document(roomId.trim());
    }

    private Map<String, Object> createRoomData(
            ChatRoomVO room,
            String status) {

        Map<String, Object> data = new HashMap<>();
        data.put("roomId", room.getRoomId());
        data.put("roomName", valueOrEmpty(room.getRoomName()));
        data.put("roomType", valueOrEmpty(room.getRoomType()));
        data.put("status", status);
        data.put("maxMember", room.getMaxMember());
        data.put("updatedAt", FieldValue.serverTimestamp());

        return data;
    }

    private Map<String, Object> createMemberData(
            long memberNo,
            int businessNo,
            String role,
            String displayName) {

        Map<String, Object> data = new HashMap<>();
        data.put("memberNo", memberNo);
        data.put("businessNo", businessNo);
        data.put("role", role);
        data.put("displayName", normalizeDisplayName(displayName));
        data.put("active", true);
        data.put("updatedAt", FieldValue.serverTimestamp());

        return data;
    }

    private Set<String> createRoomIdSet(List<ChatRoomVO> roomList) {

        Set<String> roomIds = new HashSet<>();

        if (roomList == null) {
            return roomIds;
        }

        for (ChatRoomVO room : roomList) {
            if (room != null && !isBlank(room.getRoomId())) {
                roomIds.add(room.getRoomId());
            }
        }

        return roomIds;
    }

    private void waitForWrites(List<ApiFuture<WriteResult>> writes) {

        if (writes.isEmpty()) {
            return;
        }

        try {
            ApiFutures.allAsList(writes)
                    .get(FIREBASE_WAIT_SECONDS, TimeUnit.SECONDS);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(
                    "Firestore 권한 문서 동기화가 중단되었습니다.",
                    exception);
        } catch (Exception exception) {
            throw new IllegalStateException(
                    "Firestore 권한 문서 동기화에 실패했습니다.",
                    exception);
        }
    }

    private void waitForWrite(ApiFuture<WriteResult> write) {

        try {
            write.get(FIREBASE_WAIT_SECONDS, TimeUnit.SECONDS);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(
                    "Firestore 권한 문서 처리가 중단되었습니다.",
                    exception);
        } catch (Exception exception) {
            throw new IllegalStateException(
                    "Firestore 권한 문서 처리에 실패했습니다.",
                    exception);
        }
    }

    private String normalizeRole(String role) {
        return valueOrEmpty(role).trim().toUpperCase(Locale.ROOT);
    }

    private String normalizeDisplayName(String displayName) {

        String normalized = valueOrEmpty(displayName).trim();

        if (normalized.isEmpty()) {
            return "ODITJI 사용자";
        }

        return normalized.length() <= 100
                ? normalized
                : normalized.substring(0, 100);
    }

    private String valueOrEmpty(String value) {
        return value == null ? "" : value;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
