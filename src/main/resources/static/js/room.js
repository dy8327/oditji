import {
    sendMessage,
    listenMessages,
    formatTime
} from "./chat.js";

/**
 * 채팅방 상세 화면의 서버 전달값과 DOM 요소를 초기화합니다.
 * JSP는 값만 data-* 속성으로 전달하고 실제 동작은 이 파일에서 처리합니다.
 */
document.addEventListener("DOMContentLoaded", function() {

    const pageData = document.getElementById("chatPageData");

    if (!pageData) {
        console.error("채팅방 페이지 정보를 찾을 수 없습니다.");
        return;
    }

    const contextPath = pageData.dataset.contextPath || "";
    const roomId = pageData.dataset.roomId || "";
    const roomType = pageData.dataset.roomType || "PUBLIC";
    const businessNo = Number(pageData.dataset.businessNo);
    const businessName = pageData.dataset.businessName || "";
    const isAdmin = pageData.dataset.admin === "true";

    const messageArea = document.getElementById("messageArea");
    const emptyMessage = document.getElementById("emptyMessage");
    const messageInput = document.getElementById("messageInput");
    const sendBtn = document.getElementById("sendBtn");
    const leaveBtn = document.getElementById("leaveBtn");
    const roomListBtn = document.getElementById("roomListBtn");

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

        if (!messageArea || !emptyMessage) {
            return;
        }

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
            const noticeMessage = message.type === "NOTICE";
            const row = document.createElement("div");

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
     * 자유방 참가 기록을 삭제하고 채팅방 목록으로 이동합니다.
     * 사업자 번호는 서버 세션에서 확인하므로 요청 본문에는 roomId만 전달합니다.
     */
    function leaveRoom() {

        if (!confirm("채팅방에서 나가시겠습니까?")) {
            return;
        }

        if (!roomId.trim()) {
            alert("채팅방 정보를 확인할 수 없습니다.");
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

            try {
                return JSON.parse(responseText);
            } catch (error) {
                throw new Error("서버 응답이 JSON 형식이 아닙니다: " + responseText);
            }
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

    if (roomListBtn) {
        roomListBtn.addEventListener("click", function() {
            location.href = contextPath + "/chat/list";
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

    if (!roomId.trim()) {
        console.error("채팅방 ID가 없어 메시지 구독을 시작할 수 없습니다.");
        return;
    }

    listenMessages(roomId, renderMessages);
});
