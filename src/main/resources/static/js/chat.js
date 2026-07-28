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
 * 공지방에서는 관리자만 호출할 수 있도록 room.jsp에서도 제한하지만,
 * Firebase Authentication 적용 전 단계이므로 이 함수에서도 한 번 더 검사합니다.
 */
export async function sendMessage(
    roomId,
    businessNo,
    senderName,
    message,
    roomType,
    isAdmin
) {

    if (!message || message.trim() === "") {
        return false;
    }

    const noticeRoom = roomType === "NOTICE";

    if (noticeRoom && !isAdmin) {
        alert("공지방에서는 관리자만 메시지를 작성할 수 있습니다.");
        return false;
    }

    try {

        await addDoc(
            collection(db, "chatRooms", roomId, "messages"),
            {
                senderBusinessNo: Number(businessNo),
                senderName: senderName,
                message: message.trim(),
                sendTime: serverTimestamp(),
                type: noticeRoom ? "NOTICE" : "CHAT",
                edited: false
            }
        );

        return true;

    } catch (error) {

        console.error("메시지 전송 실패:", error);
        alert("메시지 전송에 실패했습니다.");
        return false;
    }
}

/**
 * Firestore의 특정 채팅 메시지 내용을 수정합니다.
 *
 * sendTime은 변경하지 않기 때문에 기존 시간순 정렬 및
 * 같은 시간 메시지 묶음 기준은 그대로 유지됩니다.
 * 수정 여부를 화면에 계속 표시할 수 있도록 edited와 editedAt을 저장합니다.
 *
 * @param {string} roomId 채팅방 ID
 * @param {string} messageId 수정할 Firestore 메시지 문서 ID
 * @param {string} newMessage 수정할 메시지 내용
 * @returns {Promise<boolean>} 수정 성공 여부
 */
export async function updateMessage(roomId, messageId, newMessage) {

    if (!roomId || !roomId.trim() || !messageId || !messageId.trim()) {
        alert("수정할 메시지 정보를 확인할 수 없습니다.");
        return false;
    }

    if (!newMessage || newMessage.trim() === "") {
        alert("메시지 내용을 입력해주세요.");
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
        alert("메시지 수정에 실패했습니다.");
        return false;
    }
}

/**
 * Firestore에서 특정 채팅 메시지를 완전히 삭제합니다.
 *
 * 실제 삭제 메뉴 노출 여부는 room.js에서
 * "자유방 + 본인이 작성한 일반 메시지" 조건으로 제한합니다.
 * 현재 개발용 Firestore 규칙에서는 delete가 허용되어 있으므로
 * 문서 ID를 이용해 해당 메시지 문서를 직접 삭제합니다.
 *
 * @param {string} roomId 채팅방 ID
 * @param {string} messageId 삭제할 Firestore 메시지 문서 ID
 * @returns {Promise<boolean>} 삭제 성공 여부
 */
export async function deleteMessage(roomId, messageId) {

    if (!roomId || !roomId.trim() || !messageId || !messageId.trim()) {
        alert("삭제할 메시지 정보를 확인할 수 없습니다.");
        return false;
    }

    try {

        await deleteDoc(
            doc(db, "chatRooms", roomId, "messages", messageId)
        );

        return true;

    } catch (error) {

        console.error("메시지 삭제 실패:", error);
        alert("메시지 삭제에 실패했습니다.");
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
                senderBusinessNo: 0,
                senderName: "SYSTEM",
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
