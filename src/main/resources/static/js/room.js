import {
    sendMessage,
    updateMessage,
    deleteMessage,
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

    /*
     * 메시지 수정은 최초 전송 시각(sendTime)을 기준으로
     * 5분 이내에만 허용합니다.
     */
    const EDIT_LIMIT_MS = 5 * 60 * 1000;

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
     * 메시지가 최초 전송된 뒤 5분 이내인지 확인합니다.
     * 수정 여부와 관계없이 최초 sendTime을 기준으로 계산하므로
     * 한 번 수정했다고 해서 수정 가능 시간이 다시 늘어나지 않습니다.
     *
     * @param {Object} message 확인할 메시지
     * @returns {boolean} 5분 이내 수정 가능 여부
     */
    function isWithinEditLimit(message) {

        if (!message
                || !message.sendTime
                || typeof message.sendTime.toDate !== "function") {
            return false;
        }

        const sentAt = message.sendTime.toDate();

        if (Number.isNaN(sentAt.getTime())) {
            return false;
        }

        const elapsedTime = Date.now() - sentAt.getTime();

        return elapsedTime >= 0
            && elapsedTime <= EDIT_LIMIT_MS;
    }

    /**
     * 자유방에서 현재 로그인한 사업자가 직접 작성한 메시지를 수정합니다.
     * sendTime은 유지하고 내용과 수정 여부만 Firestore에서 갱신합니다.
     *
     * @param {Object} message 수정할 메시지 객체
     */
    async function handleEditMessage(message) {

        if (roomType !== "PUBLIC") {
            alert("자유방 메시지만 수정할 수 있습니다.");
            return;
        }

        if (!message || !message.id) {
            alert("수정할 메시지 정보를 확인할 수 없습니다.");
            return;
        }

        if (Number(message.senderBusinessNo) !== businessNo
                || message.type !== "CHAT") {
            alert("본인이 작성한 메시지만 수정할 수 있습니다.");
            return;
        }

        if (!isWithinEditLimit(message)) {
            alert("메시지는 전송 후 5분 이내에만 수정할 수 있습니다.");
            return;
        }

        const currentMessage = String(message.message || "");
        const editedMessage = window.prompt(
            "수정할 메시지를 입력하세요.",
            currentMessage
        );

        if (editedMessage === null) {
            return;
        }

        const trimmedMessage = editedMessage.trim();

        if (!trimmedMessage) {
            alert("메시지 내용을 입력해주세요.");
            return;
        }

        if (trimmedMessage === currentMessage.trim()) {
            return;
        }

        await updateMessage(
            roomId,
            message.id,
            trimmedMessage
        );
    }

    /**
     * 자유방에서 현재 로그인한 사업자가 직접 작성한 메시지를 삭제합니다.
     * 공지방에서는 호출하지 않으며, 삭제 전 사용자 확인을 한 번 거칩니다.
     * 삭제가 완료되면 Firestore 실시간 구독이 자동으로 다시 렌더링합니다.
     *
     * @param {Object} message 삭제할 메시지 객체
     */
    async function handleDeleteMessage(message) {

        if (roomType !== "PUBLIC") {
            alert("자유방 메시지만 삭제할 수 있습니다.");
            return;
        }

        if (!message || !message.id) {
            alert("삭제할 메시지 정보를 확인할 수 없습니다.");
            return;
        }

        if (Number(message.senderBusinessNo) !== businessNo
                || message.type !== "CHAT") {
            alert("본인이 작성한 메시지만 삭제할 수 있습니다.");
            return;
        }

        if (!confirm("이 메시지를 삭제하시겠습니까?")) {
            return;
        }

        await deleteMessage(roomId, message.id);
    }

    /**
     * 열려 있는 메시지 더보기 메뉴를 닫습니다.
     *
     * @param {HTMLElement|null} exceptMenu 닫지 않을 메뉴
     */
    function closeMessageMenus(exceptMenu = null) {

        document
            .querySelectorAll(".message-action-menu.open")
            .forEach(function(menu) {

                if (menu === exceptMenu) {
                    return;
                }

                menu.classList.remove("open");

                const actionArea = menu.closest(".message-actions");

                if (actionArea) {
                    const moreButton = actionArea.querySelector(
                        ".message-more-btn"
                    );

                    if (moreButton) {
                        moreButton.setAttribute(
                            "aria-expanded",
                            "false"
                        );
                    }
                }
            });
    }

    /**
     * Firestore Timestamp를 날짜·시·분 단위 비교값으로 변환합니다.
     * 같은 시각 표시라도 날짜가 다르면 서로 다른 메시지 묶음으로 처리합니다.
     *
     * @param {Object} timestamp Firestore Timestamp
     * @returns {string|null} yyyy-MM-dd HH:mm 형태의 비교값
     */
    function getMinuteKey(timestamp) {

        if (!timestamp || typeof timestamp.toDate !== "function") {
            return null;
        }

        const date = timestamp.toDate();

        if (Number.isNaN(date.getTime())) {
            return null;
        }

        const year = date.getFullYear();
        const month = String(date.getMonth() + 1).padStart(2, "0");
        const day = String(date.getDate()).padStart(2, "0");
        const hour = String(date.getHours()).padStart(2, "0");
        const minute = String(date.getMinutes()).padStart(2, "0");

        return `${year}-${month}-${day} ${hour}:${minute}`;
    }

    /**
     * 두 메시지가 카카오톡처럼 하나의 연속 묶음으로 표시될 수 있는지 확인합니다.
     * 연속된 일반 메시지 중 발신자와 날짜·시·분이 모두 같을 때만 묶습니다.
     * 시스템 메시지가 사이에 있거나 전송 시간이 아직 확정되지 않은 메시지는 묶지 않습니다.
     *
     * @param {Object|null} firstMessage 앞쪽 메시지
     * @param {Object|null} secondMessage 뒤쪽 메시지
     * @returns {boolean} 같은 묶음 여부
     */
    function isSameMessageGroup(firstMessage, secondMessage) {

        if (!firstMessage || !secondMessage) {
            return false;
        }

        if (firstMessage.type === "SYSTEM" || secondMessage.type === "SYSTEM") {
            return false;
        }

        const firstMinuteKey = getMinuteKey(firstMessage.sendTime);
        const secondMinuteKey = getMinuteKey(secondMessage.sendTime);

        if (!firstMinuteKey || !secondMinuteKey) {
            return false;
        }

        return Number(firstMessage.senderBusinessNo)
                === Number(secondMessage.senderBusinessNo)
            && String(firstMessage.senderName || "")
                === String(secondMessage.senderName || "")
            && firstMinuteKey === secondMinuteKey;
    }

    /**
     * 메시지 위치에 맞는 묶음 클래스를 반환합니다.
     * 이 클래스는 말풍선 간격과 모서리 형태를 조정하는 데 사용합니다.
     *
     * @param {boolean} sameAsPrevious 이전 메시지와 같은 묶음인지 여부
     * @param {boolean} sameAsNext 다음 메시지와 같은 묶음인지 여부
     * @returns {string} 메시지 묶음 위치 클래스
     */
    function getGroupPositionClass(sameAsPrevious, sameAsNext) {

        if (!sameAsPrevious && !sameAsNext) {
            return "group-single";
        }

        if (!sameAsPrevious) {
            return "group-start";
        }

        if (!sameAsNext) {
            return "group-end";
        }

        return "group-middle";
    }

    /**
     * 자유방의 내 일반 메시지인지 확인합니다.
     *
     * @param {Object} message 확인할 메시지
     * @returns {boolean} 수정/삭제 메뉴 표시 가능 여부
     */
    function canManageMessage(message) {

        return roomType === "PUBLIC"
            && Number(message.senderBusinessNo) === businessNo
            && message.type === "CHAT"
            && Boolean(message.id);
    }

    /**
     * 내 메시지에 표시할 ⋮ 메뉴를 생성합니다.
     * 수정과 삭제는 자유방의 본인 일반 메시지에만 제공되며, 수정은 전송 후 5분 이내에만 가능합니다.
     *
     * @param {Object} message 대상 메시지
     * @returns {HTMLElement} 메시지 메뉴 영역
     */
    function createMessageActions(message) {

        const actionArea = document.createElement("div");
        actionArea.className = "message-actions";

        const moreButton = document.createElement("button");
        moreButton.type = "button";
        moreButton.className = "message-more-btn";
        moreButton.textContent = "⋮";
        moreButton.setAttribute("aria-label", "메시지 메뉴");
        moreButton.setAttribute("aria-haspopup", "true");
        moreButton.setAttribute("aria-expanded", "false");
        moreButton.title = "메시지 메뉴";

        const actionMenu = document.createElement("div");
        actionMenu.className = "message-action-menu";
        actionMenu.setAttribute("role", "menu");

        const editButton = document.createElement("button");
        editButton.type = "button";
        editButton.className = "message-action-item";
        editButton.textContent = "수정";
        editButton.setAttribute("role", "menuitem");

        /*
         * 수정은 최초 전송 후 5분 이내에만 허용합니다.
         * 5분이 지난 메시지는 메뉴는 유지하되 수정 버튼을 비활성화하여
         * 사용자가 삭제 기능은 계속 이용할 수 있도록 합니다.
         */
        const editAllowed = isWithinEditLimit(message);

        if (!editAllowed) {
            editButton.disabled = true;
            editButton.title = "전송 후 5분이 지나 수정할 수 없습니다.";
            editButton.setAttribute("aria-disabled", "true");
        }

        const deleteButton = document.createElement("button");
        deleteButton.type = "button";
        deleteButton.className =
            "message-action-item message-action-delete";
        deleteButton.textContent = "삭제";
        deleteButton.setAttribute("role", "menuitem");

        moreButton.addEventListener("click", function(event) {

            event.stopPropagation();

            const willOpen = !actionMenu.classList.contains("open");

            closeMessageMenus(actionMenu);
            actionMenu.classList.toggle("open", willOpen);
            moreButton.setAttribute(
                "aria-expanded",
                String(willOpen)
            );
        });

        actionMenu.addEventListener("click", function(event) {
            event.stopPropagation();
        });

        editButton.addEventListener("click", async function() {

            if (editButton.disabled) {
                return;
            }

            closeMessageMenus();
            await handleEditMessage(message);
        });

        deleteButton.addEventListener("click", async function() {
            closeMessageMenus();
            await handleDeleteMessage(message);
        });

        actionMenu.appendChild(editButton);
        actionMenu.appendChild(deleteButton);

        actionArea.appendChild(moreButton);
        actionArea.appendChild(actionMenu);

        return actionArea;
    }

    /**
     * Firestore에서 받은 메시지를 화면에 출력합니다.
     * 같은 발신자가 같은 분에 연속으로 보낸 메시지는 하나의 묶음으로 붙여 표시하고,
     * 묶음의 마지막 메시지에만 전송 시간을 표시합니다.
     * 수정된 메시지는 시간 표시 여부와 관계없이 [수정] 표기를 유지합니다.
     *
     * @param {Array<Object>} messageList 시간순으로 정렬된 메시지 목록
     */
    function renderMessages(messageList) {

        if (!messageArea || !emptyMessage) {
            return;
        }

        closeMessageMenus();
        messageArea.innerHTML = "";

        if (!messageList || messageList.length === 0) {
            messageArea.appendChild(emptyMessage);
            return;
        }

        messageList.forEach(function(message, index) {

            if (message.type === "SYSTEM") {
                const systemDiv = document.createElement("div");
                systemDiv.className = "system-message";
                systemDiv.textContent = message.message;
                messageArea.appendChild(systemDiv);
                return;
            }

            const previousMessage = index > 0
                ? messageList[index - 1]
                : null;
            const nextMessage = index < messageList.length - 1
                ? messageList[index + 1]
                : null;

            const sameAsPrevious = isSameMessageGroup(
                previousMessage,
                message
            );
            const sameAsNext = isSameMessageGroup(
                message,
                nextMessage
            );

            const mine = Number(message.senderBusinessNo) === businessNo;
            const noticeMessage = message.type === "NOTICE";
            const row = document.createElement("div");

            row.className = mine
                ? "message-row mine"
                : "message-row other";

            row.classList.add(
                getGroupPositionClass(sameAsPrevious, sameAsNext)
            );

            if (noticeMessage) {
                row.classList.add("notice-message-row");
            }

            /*
             * 같은 묶음의 첫 메시지에만 발신자명을 표시합니다.
             * 내 메시지는 기존 화면과 동일하게 발신자 영역을 CSS에서 숨깁니다.
             */
            if (!sameAsPrevious) {
                const sender = document.createElement("div");
                sender.className = "sender";
                sender.textContent = noticeMessage
                    ? "[관리자] " + message.senderName
                    : message.senderName;
                row.appendChild(sender);
            }

            const messageLine = document.createElement("div");
            messageLine.className = "message-line";

            const bubble = document.createElement("div");
            bubble.className = "bubble";
            bubble.textContent = message.message;
            messageLine.appendChild(bubble);

            /*
             * 수정 여부와 전송 시간을 하나의 메타 영역으로 묶습니다.
             * 수정된 메시지는 묶음 중간에 있어 시간이 생략되더라도 [수정]은 남습니다.
             */
            const messageMeta = document.createElement("div");
            messageMeta.className = "message-meta";

            if (message.edited === true) {
                const edited = document.createElement("span");
                edited.className = "edited-indicator";
                edited.textContent = "[수정]";
                messageMeta.appendChild(edited);
            }

            if (!sameAsNext) {
                const timeText = formatTime(message.sendTime);

                if (timeText) {
                    const time = document.createElement("span");
                    time.className = "time";
                    time.textContent = timeText;
                    messageMeta.appendChild(time);
                }
            }

            if (messageMeta.childElementCount > 0) {
                messageLine.appendChild(messageMeta);
            }

            if (canManageMessage(message)) {
                messageLine.appendChild(
                    createMessageActions(message)
                );
            }

            row.appendChild(messageLine);
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

    document.addEventListener("click", function() {
        closeMessageMenus();
    });

    document.addEventListener("keydown", function(event) {

        if (event.key === "Escape") {
            closeMessageMenus();
        }
    });

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
