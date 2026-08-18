import { db, ensureFirebaseChatAuth } from "./firebase-config.js?v=2";

import {
    collection,
    addDoc,
    doc,
    deleteDoc,
    updateDoc,
    query,
    where,
    orderBy,
    onSnapshot,
    serverTimestamp,
    Timestamp
} from "https://www.gstatic.com/firebasejs/12.15.0/firebase-firestore.js";

/**
 * Firestore에 채팅 메시지를 저장합니다.
 *
 * 기존 senderBusinessNo는 과거 메시지 및 화면 권한 호환을 위해 유지하고,
 * 읽음 처리와 향후 사용자 유형 확장을 위해 senderMemberNo와 senderRole을 추가합니다.
 */
export async function sendMessage(
    roomId,
    memberNo,
    businessNo,
    senderName,
    message,
    roomType,
    role,
    isAdmin
) {

    if (!message || message.trim() === "") {
        return false;
    }

    if (message.trim().length > 2000) {
        await showAlert("메시지는 2,000자 이내로 입력해주세요.", "warning");
        return false;
    }

    const noticeRoom = roomType === "NOTICE";

    if (noticeRoom && !isAdmin) {
        await showAlert("공지방에서는 관리자만 메시지를 작성할 수 있습니다.", "warning");
        return false;
    }

    try {

        await ensureFirebaseChatAuth();

        await addDoc(
            collection(db, "chatRooms", roomId, "messages"),
            {
                senderMemberNo: Number(memberNo),
                senderBusinessNo: Number(businessNo),
                senderName: senderName,
                senderRole: role || (isAdmin ? "ADMIN" : "BUSINESS"),
                message: message.trim(),
                sendTime: serverTimestamp(),
                type: noticeRoom ? "NOTICE" : "CHAT",
                edited: false
            }
        );

        return true;

    } catch (error) {

        console.error("메시지 전송 실패:", error);
        await showAlert("메시지 전송에 실패했습니다.", "error");
        return false;
    }
}

/**
 * Firestore의 특정 채팅 메시지 내용을 수정합니다.
 *
 * sendTime은 변경하지 않기 때문에 기존 시간순 정렬 및
 * 같은 시간 메시지 묶음 기준은 그대로 유지됩니다.
 * 수정 여부를 화면에 계속 표시할 수 있도록 edited와 editedAt을 저장합니다.
 */
export async function updateMessage(roomId, messageId, newMessage) {

    if (!roomId || !roomId.trim() || !messageId || !messageId.trim()) {
        await showAlert("수정할 메시지 정보를 확인할 수 없습니다.", "warning");
        return false;
    }

    if (!newMessage || newMessage.trim() === "") {
        await showAlert("메시지 내용을 입력해주세요.", "warning");
        return false;
    }

    if (newMessage.trim().length > 2000) {
        await showAlert("메시지는 2,000자 이내로 입력해주세요.", "warning");
        return false;
    }

    try {

        await ensureFirebaseChatAuth();

        await updateDoc(
            doc(db, "chatRooms", roomId, "messages", messageId),
            {
                message: newMessage.trim(),
                edited: true,
                editedAt: serverTimestamp()
            }
        );

        return true;

    } catch (error) {

        console.error("메시지 수정 실패:", error);
        await showAlert("메시지 수정에 실패했습니다.", "error");
        return false;
    }
}

/**
 * Firestore에서 특정 채팅 메시지를 완전히 삭제합니다.
 */
export async function deleteMessage(roomId, messageId) {

    if (!roomId || !roomId.trim() || !messageId || !messageId.trim()) {
        await showAlert("삭제할 메시지 정보를 확인할 수 없습니다.", "warning");
        return false;
    }

    try {

        await ensureFirebaseChatAuth();

        await deleteDoc(
            doc(db, "chatRooms", roomId, "messages", messageId)
        );

        return true;

    } catch (error) {

        console.error("메시지 삭제 실패:", error);
        await showAlert("메시지 삭제에 실패했습니다.", "error");
        return false;
    }
}

/**
 * 방의 메시지를 시간순으로 실시간 수신합니다.
 *
 * 자유방은 Oracle CHAT_ROOM_MEMBER.JOIN_DATE를 epoch millisecond로 전달받아
 * Firestore 쿼리 자체를 참가 시각 이후로 제한합니다. 화면에서만 과거 메시지를
 * 숨기는 방식이 아니므로 참가 전 메시지는 클라이언트로 조회하지 않습니다.
 * 공지방처럼 시작 시각이 0 이하이면 기존과 동일하게 전체 메시지를 조회합니다.
 *
 * @param {string} roomId 채팅방 ID
 * @param {Function} callback 메시지 목록 수신 콜백
 * @param {number} startEpochMs 조회 시작 시각(epoch millisecond)
 * @returns {Function} Firestore 실시간 구독 해제 함수
 */
export function listenMessages(roomId, callback, startEpochMs = 0) {

    let unsubscribe = null;
    let cancelled = false;

    ensureFirebaseChatAuth()
        .then(function() {

            if (cancelled) {
                return;
            }

            const messageCollection = collection(
                db,
                "chatRooms",
                roomId,
                "messages"
            );
            const normalizedStartEpochMs = Number(startEpochMs) || 0;

            const messageQuery = normalizedStartEpochMs > 0
                ? query(
                    messageCollection,
                    where(
                        "sendTime",
                        ">=",
                        Timestamp.fromMillis(normalizedStartEpochMs)
                    ),
                    orderBy("sendTime", "asc")
                )
                : query(
                    messageCollection,
                    orderBy("sendTime", "asc")
                );

            unsubscribe = onSnapshot(
                messageQuery,
                snapshot => {

                    const messageList = [];

                    snapshot.forEach(documentSnapshot => {
                        messageList.push({
                            id: documentSnapshot.id,
                            ...documentSnapshot.data()
                        });
                    });

                    callback(messageList);
                },
                error => {
                    console.error("메시지 실시간 조회 실패:", error);
                }
            );
        })
        .catch(function(error) {
            console.error("Firebase 채팅 인증 실패:", error);
        });

    return function() {
        cancelled = true;

        if (typeof unsubscribe === "function") {
            unsubscribe();
        }
    };
}

/**
 * Firestore Timestamp를 epoch millisecond로 변환합니다.
 */
export function getTimestampMillis(timestamp) {

    if (!timestamp || typeof timestamp.toMillis !== "function") {
        return 0;
    }

    return timestamp.toMillis();
}

/**
 * 메시지 시간을 시:분 형식으로 표시합니다.
 */
export function formatTime(timestamp) {

    if (!timestamp || typeof timestamp.toDate !== "function") {
        return "";
    }

    return timestamp.toDate().toLocaleTimeString("ko-KR", {
        hour: "2-digit",
        minute: "2-digit"
    });
}

/**
 * 메시지 날짜를 연월일 형식으로 표시합니다.
 */
export function formatDate(timestamp) {

    if (!timestamp || typeof timestamp.toDate !== "function") {
        return "";
    }

    return timestamp.toDate().toLocaleDateString("ko-KR");
}
