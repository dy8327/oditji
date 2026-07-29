import { db } from "./firebase-config.js";

import {
    collection,
    addDoc,
    doc,
    deleteDoc,
    updateDoc,
    query,
    orderBy,
    onSnapshot,
    serverTimestamp
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

    const noticeRoom = roomType === "NOTICE";

    if (noticeRoom && !isAdmin) {
        await showAlert("공지방에서는 관리자만 메시지를 작성할 수 있습니다.", "warning");
        return false;
    }

    try {

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

    try {

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
 * 시스템 안내 메시지를 저장합니다.
 */
export async function sendSystemMessage(roomId, message) {

    try {

        await addDoc(
            collection(db, "chatRooms", roomId, "messages"),
            {
                senderMemberNo: 0,
                senderBusinessNo: 0,
                senderName: "SYSTEM",
                senderRole: "SYSTEM",
                message: message,
                sendTime: serverTimestamp(),
                type: "SYSTEM"
            }
        );

    } catch (error) {
        console.error("시스템 메시지 전송 실패:", error);
    }
}

/**
 * 방의 전체 메시지를 시간순으로 실시간 수신합니다.
 */
export function listenMessages(roomId, callback) {

    const messageQuery = query(
        collection(db, "chatRooms", roomId, "messages"),
        orderBy("sendTime", "asc")
    );

    return onSnapshot(
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
