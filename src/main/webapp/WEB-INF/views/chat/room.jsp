<%@ page language="java"
    contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>

<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>${room.roomName}</title>
<link rel="stylesheet"
      href="${pageContext.request.contextPath}/css/chat-room.css">
</head>
<body>

<div class="chat-container ${isNoticeRoom ? 'notice-room-container' : ''}">

    <div class="chat-header ${isNoticeRoom ? 'notice-header' : ''}">

        <div class="chat-title-line">
            <h2><c:out value="${room.roomName}" /></h2>

            <span class="header-room-badge">
                <c:choose>
                    <c:when test="${isNoticeRoom}">공지방</c:when>
                    <c:otherwise>자유방</c:otherwise>
                </c:choose>
            </span>
        </div>

        <p>
            <c:choose>
                <c:when test="${empty room.roomDescription}">
                    <c:choose>
                        <c:when test="${isNoticeRoom}">
                            관리자 공지 전용 채팅방입니다.
                        </c:when>
                        <c:otherwise>
                            자유롭게 대화할 수 있는 사업자 채팅방입니다.
                        </c:otherwise>
                    </c:choose>
                </c:when>
                <c:otherwise>
                    <c:out value="${room.roomDescription}" />
                </c:otherwise>
            </c:choose>
        </p>

        <div class="top-btn-area">
            <button type="button"
                    onclick="location.href='${pageContext.request.contextPath}/chat/list'">
                목록으로
            </button>
        </div>

    </div>

    <div class="chat-info">
        <span>접속 사업자: <c:out value="${businessName}" /></span>
        <span>ROOM ID: <c:out value="${room.roomId}" /></span>
    </div>

    <c:if test="${isNoticeRoom and not isAdmin}">
        <div class="readonly-notice">
            이 방은 관리자 공지 전용입니다. 사업자는 공지를 읽을 수만 있습니다.
        </div>
    </c:if>

    <div id="messageArea"
         class="message-area">
        <div id="emptyMessage"
             class="empty-message">
            아직 메시지가 없습니다.
        </div>
    </div>

    <c:choose>

        <c:when test="${isNoticeRoom and not isAdmin}">
            <div class="readonly-footer">
                관리자만 공지 메시지를 작성할 수 있습니다.
            </div>
        </c:when>

        <c:otherwise>
            <div class="input-area">

                <textarea id="messageInput"
                          placeholder="${isNoticeRoom ? '공지 내용을 입력하세요.' : '메시지를 입력하세요.'}"></textarea>

                <button type="button"
                        class="send-btn"
                        id="sendBtn">
                    전송
                </button>

                <c:if test="${not isNoticeRoom}">
                    <button type="button"
                            class="leave-btn"
                            id="leaveBtn">
                        나가기
                    </button>
                </c:if>

            </div>
        </c:otherwise>

    </c:choose>

</div>

<script type="module">

import {
    sendMessage,
    listenMessages,
    formatTime
} from "${pageContext.request.contextPath}/js/chat.js";

const contextPath = "${pageContext.request.contextPath}";
const roomId = "${room.roomId}";
const roomType = "${room.roomType}";
const businessNo = Number("${businessNo}");
const businessName = "${businessName}";
const isAdmin = "${isAdmin}" === "true";

const messageArea = document.getElementById("messageArea");
const emptyMessage = document.getElementById("emptyMessage");
const messageInput = document.getElementById("messageInput");
const sendBtn = document.getElementById("sendBtn");
const leaveBtn = document.getElementById("leaveBtn");

/**
 * 현재 방의 권한을 확인한 후 메시지를 전송합니다.
 */
async function handleSendMessage() {

    if (!messageInput) {
        return;
    }

    const message = messageInput.value;

    if (!message || message.trim() === "") {
        return;
    }

    const sent = await sendMessage(
        roomId,
        businessNo,
        businessName,
        message,
        roomType,
        isAdmin
    );

    if (!sent) {
        return;
    }

    messageInput.value = "";
    messageInput.focus();
}

/**
 * Firestore에서 받은 메시지를 화면에 출력합니다.
 */
function renderMessages(messageList) {

    messageArea.innerHTML = "";

    if (!messageList || messageList.length === 0) {
        messageArea.appendChild(emptyMessage);
        return;
    }

    messageList.forEach(function(message) {

        if (message.type === "SYSTEM") {
            const systemDiv = document.createElement("div");
            systemDiv.className = "system-message";
            systemDiv.textContent = message.message;
            messageArea.appendChild(systemDiv);
            return;
        }

        const mine = Number(message.senderBusinessNo) === businessNo;
        const row = document.createElement("div");
        const noticeMessage = message.type === "NOTICE";

        row.className = mine
            ? "message-row mine"
            : "message-row other";

        if (noticeMessage) {
            row.classList.add("notice-message-row");
        }

        const sender = document.createElement("div");
        sender.className = "sender";
        sender.textContent = noticeMessage
            ? "[관리자] " + message.senderName
            : message.senderName;

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

    messageArea.scrollTop = messageArea.scrollHeight;
}

/**
 * 자유방 참가 기록을 삭제하고 목록으로 이동합니다.
 * 사업자 번호는 서버 세션에서 확인하므로 요청에 포함하지 않습니다.
 */
function leaveRoom() {

    if (!confirm("채팅방에서 나가시겠습니까?")) {
        return;
    }

    fetch(contextPath + "/chat/api/leave", {
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
                "채팅방 나가기 요청 실패: "
                + response.status
                + " / "
                + responseText
            );
        }

        return JSON.parse(responseText);
    })
    .then(function(data) {

        alert(data.message || "채팅방 나가기 처리가 완료되었습니다.");

        if (data.success) {
            location.href = contextPath + "/chat/list";
        }
    })
    .catch(function(error) {
        console.error("채팅방 나가기 중 오류:", error);
        alert("채팅방 나가기 중 오류가 발생했습니다.");
    });
}

if (sendBtn && messageInput) {

    sendBtn.addEventListener("click", handleSendMessage);

    messageInput.addEventListener("keydown", function(event) {

        if (event.key === "Enter" && !event.shiftKey) {
            event.preventDefault();
            handleSendMessage();
        }
    });
}

if (leaveBtn) {
    leaveBtn.addEventListener("click", leaveRoom);
}

listenMessages(roomId, renderMessages);

</script>

</body>
</html>
