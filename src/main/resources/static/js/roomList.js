import { db } from "./firebase-config.js";

import {
    collection,
    query,
    orderBy,
    limit,
    onSnapshot
} from "https://www.gstatic.com/firebasejs/12.15.0/firebase-firestore.js";

/**
 * 채팅방 목록의 입장 버튼과 최근 메시지 구독을 연결합니다.
 */
document.addEventListener("DOMContentLoaded", function() {

    const contextPathElement = document.getElementById("contextPath");

    if (!contextPathElement) {
        return;
    }

    const contextPath = contextPathElement.value;
    const createRoomBtn = document.getElementById("createRoomBtn");
    const actionButtons = document.querySelectorAll(".room-action-btn");
    const roomCards = document.querySelectorAll(".room-card");

    if (createRoomBtn) {
        createRoomBtn.addEventListener("click", function() {
            location.href = contextPath + "/chat/create";
        });
    }

    actionButtons.forEach(function(button) {

        button.addEventListener("click", function() {

            const roomId = button.dataset.roomId;
            const roomType = button.dataset.roomType;

            if (roomType === "NOTICE") {
                enterRoom(contextPath, roomId);
                return;
            }

            joinAndEnterRoom(contextPath, roomId, button);
        });
    });

    roomCards.forEach(function(card) {
        listenLastMessage(card.dataset.roomId);
    });
});

/**
 * 자유방 참가 API를 호출한 후 참가 성공 또는 기존 참가 상태이면 입장합니다.
 * 사업자 번호는 서버 세션에서 확인하므로 요청 본문으로 보내지 않습니다.
 */
function joinAndEnterRoom(contextPath, roomId, button) {

    button.disabled = true;

    fetch(contextPath + "/chat/api/join", {
        method: "POST",
        headers: {
            "Content-Type": "application/x-www-form-urlencoded; charset=UTF-8"
        },
        body: "roomId=" + encodeURIComponent(roomId)
    })
    .then(async function(response) {

        const responseText = await response.text();

        if (!response.ok) {
            throw new Error(
                "채팅방 참가 요청 실패: "
                + response.status
                + " / "
                + responseText
            );
        }

        return JSON.parse(responseText);
    })
    .then(function(data) {

        if (data.success) {
            enterRoom(contextPath, roomId);
            return;
        }

        alert(data.message || "채팅방 참가에 실패했습니다.");
    })
    .catch(function(error) {
        console.error("채팅방 참가 중 오류:", error);
        alert("채팅방 참가 중 오류가 발생했습니다.");
    })
    .finally(function() {
        button.disabled = false;
    });
}

/**
 * 채팅방 상세 화면으로 이동합니다.
 */
function enterRoom(contextPath, roomId) {
    location.href = contextPath + "/chat/room/" + encodeURIComponent(roomId);
}

/**
 * 채팅방별 마지막 메시지를 Firestore에서 실시간으로 조회합니다.
 */
function listenLastMessage(roomId) {

    const lastMessageQuery = query(
        collection(db, "chatRooms", roomId, "messages"),
        orderBy("sendTime", "desc"),
        limit(1)
    );

    onSnapshot(
        lastMessageQuery,
        function(snapshot) {

            const lastMessageElement =
                document.getElementById("lastMessage-" + roomId);

            const lastTimeElement =
                document.getElementById("lastTime-" + roomId);

            if (!lastMessageElement || !lastTimeElement) {
                return;
            }

            if (snapshot.empty) {
                lastMessageElement.textContent = "마지막 메시지가 없습니다.";
                lastTimeElement.textContent = "";
                return;
            }

            const message = snapshot.docs[0].data();

            if (message.type === "SYSTEM") {
                lastMessageElement.textContent = message.message;
            } else if (message.type === "NOTICE") {
                lastMessageElement.textContent = "[공지] " + message.message;
            } else {
                lastMessageElement.textContent =
                    message.senderName + " : " + message.message;
            }

            lastTimeElement.textContent = formatLastTime(message.sendTime);
        },
        function(error) {
            console.error("최근 메시지 조회 실패:", error);
        }
    );
}

/**
 * 오늘 작성된 메시지는 시간을, 이전 메시지는 월/일을 표시합니다.
 */
function formatLastTime(timestamp) {

    if (!timestamp || typeof timestamp.toDate !== "function") {
        return "";
    }

    const date = timestamp.toDate();
    const now = new Date();

    const today =
        date.getFullYear() === now.getFullYear()
        && date.getMonth() === now.getMonth()
        && date.getDate() === now.getDate();

    if (today) {
        return date.toLocaleTimeString("ko-KR", {
            hour: "2-digit",
            minute: "2-digit"
        });
    }

    return date.toLocaleDateString("ko-KR", {
        month: "2-digit",
        day: "2-digit"
    });
}
