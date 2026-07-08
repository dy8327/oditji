import { db } from "./firebase-config.js";

import {
    collection,
    query,
    orderBy,
    limit,
    onSnapshot
} from "https://www.gstatic.com/firebasejs/12.15.0/firebase-firestore.js";

document.addEventListener("DOMContentLoaded", function() {

    const contextPath = document.getElementById("contextPath").value;
    const businessNo = document.getElementById("businessNo").value;

    const joinButtons = document.querySelectorAll(".join-btn");
    const enterButtons = document.querySelectorAll(".enter-btn");
    const roomCards = document.querySelectorAll(".room-card");

    joinButtons.forEach(function(button) {

        button.addEventListener("click", function() {

            const roomId = button.dataset.roomId;

            joinRoom(contextPath, roomId, businessNo);

        });

    });

    enterButtons.forEach(function(button) {

        button.addEventListener("click", function() {

            const roomId = button.dataset.roomId;

            enterRoom(contextPath, roomId);

        });

    });

    roomCards.forEach(function(card) {

        const roomId = card.dataset.roomId;

        listenLastMessage(roomId);

    });

});


function joinRoom(contextPath, roomId, businessNo) {

    fetch(contextPath + "/chat/api/join", {
        method: "POST",
        headers: {
            "Content-Type": "application/x-www-form-urlencoded"
        },
        body:
            "roomId=" + encodeURIComponent(roomId) +
            "&businessNo=" + encodeURIComponent(businessNo)
    })
    .then(function(response) {
        return response.json();
    })
    .then(function(data) {

        if (data.message) {
            alert(data.message);
        }

        if (data.success) {
            location.href = contextPath + "/chat/room/" + roomId;
        }

    })
    .catch(function(error) {

        console.error(error);
        alert("채팅방 참가 중 오류가 발생했습니다.");

    });

}


function enterRoom(contextPath, roomId) {

    location.href = contextPath + "/chat/room/" + roomId;

}


function listenLastMessage(roomId) {

    const messageRef = collection(
        db,
        "chatRooms",
        roomId,
        "messages"
    );

    const lastMessageQuery = query(
        messageRef,
        orderBy("sendTime", "desc"),
        limit(1)
    );

    onSnapshot(lastMessageQuery, function(snapshot) {

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

        snapshot.forEach(function(doc) {

            const message = doc.data();

            if (message.type === "SYSTEM") {

                lastMessageElement.textContent = message.message;

            } else {

                lastMessageElement.textContent =
                    message.senderName + " : " + message.message;

            }

            lastTimeElement.textContent = formatLastTime(message.sendTime);

        });

    }, function(error) {

        console.error(error);

    });

}


function formatLastTime(timestamp) {

    if (!timestamp) {
        return "";
    }

    const date = timestamp.toDate();

    const now = new Date();

    const isToday =
        date.getFullYear() === now.getFullYear() &&
        date.getMonth() === now.getMonth() &&
        date.getDate() === now.getDate();

    if (isToday) {

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