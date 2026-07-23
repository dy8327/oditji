import { db } from "./firebase-config.js";

import {
    collection,
    addDoc,
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
                type: noticeRoom ? "NOTICE" : "CHAT"
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
