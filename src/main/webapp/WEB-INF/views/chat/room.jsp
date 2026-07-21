<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>

<!DOCTYPE html>

<html>

<head>

<meta charset="UTF-8">

<title>${room.roomName}</title>

<link rel="stylesheet" href="${pageContext.request.contextPath}/css/chat-room.css">
</head>

<body>

<div class="chat-container">

    <div class="chat-header">

        <h2>${room.roomName}</h2>

        <p>${room.roomDescription}</p>

        <div class="top-btn-area">

            <button type="button"
                    onclick="location.href='${pageContext.request.contextPath}/chat/list'">
                목록으로
            </button>

        </div>

    </div>

    <div class="chat-info">

        <span>
            참여 사업자 :
            ${businessName}
        </span>

        <span>
            ROOM ID :
            ${room.roomId}
        </span>

    </div>

    <div id="messageArea"
         class="message-area">

        <div id="emptyMessage"
             class="empty-message">

            아직 메시지가 없습니다.

        </div>

    </div>

    <div class="input-area">

        <textarea id="messageInput"
                  placeholder="메시지를 입력하세요."></textarea>

        <button type="button"
                class="send-btn"
                id="sendBtn">

            전송

        </button>

        <button type="button"
                class="leave-btn"
                id="leaveBtn">

            나가기

        </button>

    </div>

</div>


<script type="module">

import {
    sendMessage,
    listenMessages,
    formatTime
} from "${pageContext.request.contextPath}/js/chat.js";

const contextPath = "${pageContext.request.contextPath}";

const roomId = "${room.roomId}";

const businessNo = Number("${businessNo}");

const businessName = "${businessName}";

const messageArea = document.getElementById("messageArea");

const emptyMessage = document.getElementById("emptyMessage");

const messageInput = document.getElementById("messageInput");

const sendBtn = document.getElementById("sendBtn");

const leaveBtn = document.getElementById("leaveBtn");


/*
    메시지 전송
*/
async function handleSendMessage() {

    const message = messageInput.value;

    if (!message || message.trim() === "") {
        return;
    }

    await sendMessage(
        roomId,
        businessNo,
        businessName,
        message
    );

    messageInput.value = "";
    messageInput.focus();

}


/*
    메시지 화면 출력
*/
function renderMessages(messageList) {

    messageArea.innerHTML = "";

    if (!messageList || messageList.length === 0) {

        messageArea.appendChild(emptyMessage);
        return;

    }

    messageList.forEach(message => {

        if (message.type === "SYSTEM") {

            const systemDiv = document.createElement("div");
            systemDiv.className = "system-message";
            systemDiv.textContent = message.message;

            messageArea.appendChild(systemDiv);

            return;

        }

        const isMine =
            Number(message.senderBusinessNo) === businessNo;

        const row = document.createElement("div");
        row.className = isMine
            ? "message-row mine"
            : "message-row other";

        const sender = document.createElement("div");
        sender.className = "sender";
        sender.textContent = message.senderName;

        const bubble = document.createElement("div");
        bubble.className = "bubble";
        bubble.textContent = message.message;

        const time = document.createElement("div");
        time.className = "time";
        time.textContent = formatTime(message.sendTime);

        row.appendChild(sender);
        row.appendChild(bubble);
        row.appendChild(time);

        messageArea.appendChild(row);

    });

    scrollToBottom();

}


/*
    스크롤 맨 아래로 이동
*/
function scrollToBottom() {

    messageArea.scrollTop = messageArea.scrollHeight;

}


/*
    채팅방 나가기
*/
function leaveRoom() {

    if (!confirm("채팅방에서 나가시겠습니까?")) {
        return;
    }

    fetch(contextPath + "/chat/api/leave", {
        method: "POST",
        headers: {
            "Content-Type": "application/x-www-form-urlencoded"
        },
        body:
            "roomId=" + encodeURIComponent(roomId) +
            "&businessNo=" + encodeURIComponent(businessNo)
    })
    .then(response => response.json())
    .then(data => {

        if (data.message) {
            alert(data.message);
        } else {
            alert("채팅방 나가기 처리가 완료되었습니다.");
        }

        if (data.success) {
            location.href = contextPath + "/chat/list";
        }

    })
    .catch(error => {

        console.error(error);
        alert("채팅방 나가기 중 오류가 발생했습니다.");

    });

}


/*
    이벤트 연결
*/
sendBtn.addEventListener("click", handleSendMessage);

messageInput.addEventListener("keydown", function(event) {

    if (event.key === "Enter" && !event.shiftKey) {

        event.preventDefault();
        handleSendMessage();

    }

});

leaveBtn.addEventListener("click", leaveRoom);


/*
    Firebase 실시간 메시지 수신 시작
*/
listenMessages(roomId, renderMessages);

</script>

</body>

</html>