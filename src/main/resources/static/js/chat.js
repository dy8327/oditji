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
 * 메시지 전송
 */
export async function sendMessage(
    roomId,
    businessNo,
    senderName,
    message
) {

    if (!message || message.trim() === "") {
        return;
    }

    try {

        await addDoc(

            collection(db, "chatRooms", roomId, "messages"),

            {

                senderBusinessNo: businessNo,

                senderName: senderName,

                message: message.trim(),

                sendTime: serverTimestamp(),

                type: "CHAT"

            }

        );

    } catch (e) {

        console.error(e);

        alert("메시지 전송 실패");

    }

}


/**
 * 시스템 메시지
 */
export async function sendSystemMessage(
    roomId,
    message
) {

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

    } catch (e) {

        console.error(e);

    }

}


/**
 * 실시간 메시지 수신
 */
export function listenMessages(
    roomId,
    callback
) {

    const q = query(

        collection(db, "chatRooms", roomId, "messages"),

        orderBy("sendTime", "asc")

    );

    return onSnapshot(q, (snapshot) => {

        const messageList = [];

        snapshot.forEach((doc) => {

            messageList.push({

                id: doc.id,

                ...doc.data()

            });

        });

        callback(messageList);

    });

}


/**
 * 날짜 포맷
 */
export function formatTime(timestamp) {

    if (!timestamp) {

        return "";

    }

    const date = timestamp.toDate();

    return date.toLocaleTimeString("ko-KR", {

        hour: "2-digit",

        minute: "2-digit"

    });

}


/**
 * 날짜 포맷 (년월일)
 */
export function formatDate(timestamp) {

    if (!timestamp) {

        return "";

    }

    const date = timestamp.toDate();

    return date.toLocaleDateString("ko-KR");

}