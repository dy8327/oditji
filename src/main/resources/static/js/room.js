import {
    sendMessage,
    updateMessage,
    deleteMessage,
    listenMessages,
    formatTime,
    formatDate,
    getTimestampMillis
} from "./chat.js?v=5";

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
    const memberNo = Number(pageData.dataset.memberNo);
    const businessNo = Number(pageData.dataset.businessNo);
    const businessName = pageData.dataset.businessName || "";
    const role = pageData.dataset.role || "BUSINESS";
    const isAdmin = pageData.dataset.admin === "true";

    /*
     * roomList.jsp 의 중앙 패널 iframe(embed=1) 안에서 열렸는지 여부입니다.
     * 임베드 상태에서는 "목록으로"/"나가기" 후 이동을 부모 창(roomList)에
     * postMessage로 위임해 iframe 내부에서 전체 페이지 이동이 일어나지 않도록 합니다.
     */
    const isEmbedded = pageData.dataset.embedded === "true"
        || window.self !== window.top;

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

    const READER_REFRESH_INTERVAL_MS = 4000;

    /**
     * 숫자형 식별값을 안전하게 양의 정수로 변환합니다.
     * 값이 없거나 유효하지 않으면 null을 반환하여 잘못된 fallback 비교를 막습니다.
     *
     * @param {*} value 변환할 값
     * @returns {number|null} 양의 정수 또는 null
     */
    function toPositiveNumber(value) {

        const numberValue = Number(value);

        return Number.isFinite(numberValue) && numberValue > 0
            ? numberValue
            : null;
    }

    /**
     * 역할 문자열을 비교 가능한 대문자 값으로 정규화합니다.
     *
     * @param {*} value 역할 값
     * @returns {string} 정규화된 역할
     */
    function normalizeRole(value) {
        return String(value || "").trim().toUpperCase();
    }

    /**
     * 현재 로그인 사용자가 작성한 메시지인지 판별합니다.
     *
     * 신규 메시지는 senderMemberNo를 최우선으로 사용합니다.
     * 과거 메시지처럼 senderMemberNo가 없는 경우에만 senderBusinessNo를
     * 호환용으로 사용합니다. 다만 공지방 메시지는 관리자만 작성할 수 있으므로
     * 사업자 화면에서는 BUSINESS_NO가 우연히 같아도 절대 내 메시지로 처리하지 않습니다.
     *
     * @param {Object} message 확인할 메시지
     * @returns {boolean} 현재 사용자가 작성한 메시지 여부
     */
    function isCurrentUserMessage(message) {

        if (!message || message.type === "SYSTEM") {
            return false;
        }

        const senderMemberNo = toPositiveNumber(message.senderMemberNo);
        const currentMemberNo = toPositiveNumber(memberNo);

        if (senderMemberNo !== null) {
            return currentMemberNo !== null
                && senderMemberNo === currentMemberNo;
        }

        const senderRole = normalizeRole(message.senderRole);
        const noticeMessage = roomType === "NOTICE"
            || message.type === "NOTICE";

        if (noticeMessage) {
            if (!isAdmin) {
                return false;
            }

            return !senderRole || senderRole === "ADMIN";
        }

        if (senderRole === "ADMIN") {
            return isAdmin;
        }

        const senderBusinessNo = toPositiveNumber(
            message.senderBusinessNo
        );
        const currentBusinessNo = toPositiveNumber(businessNo);

        return senderBusinessNo !== null
            && currentBusinessNo !== null
            && senderBusinessNo === currentBusinessNo;
    }

    /**
     * 연속 메시지 묶음 비교에 사용할 발신자 식별키를 만듭니다.
     * 회원번호가 존재하면 회원번호를 사용하고, 과거 메시지는 역할과
     * 사업자번호를 조합하여 관리자와 사업자가 같은 번호로 오인되지 않게 합니다.
     *
     * @param {Object} message 메시지
     * @returns {string} 발신자 식별키
     */
    function getMessageSenderIdentity(message) {

        if (!message) {
            return "";
        }

        const senderMemberNo = toPositiveNumber(message.senderMemberNo);

        if (senderMemberNo !== null) {
            return "MEMBER:" + senderMemberNo;
        }

        const senderRole = normalizeRole(message.senderRole);
        const noticeMessage = roomType === "NOTICE"
            || message.type === "NOTICE";

        if (noticeMessage || senderRole === "ADMIN") {
            return "ROLE:ADMIN";
        }

        const senderBusinessNo = toPositiveNumber(
            message.senderBusinessNo
        );

        if (senderBusinessNo !== null) {
            return "BUSINESS:" + senderBusinessNo;
        }

        return "NAME:" + String(message.senderName || "");
    }

    const AVATAR_COLOR_COUNT = 6;

    /**
     * 상대 메시지 묶음 앞에 표시할 원형 프로필의 이니셜 글자를 만듭니다.
     * 공지 메시지는 발신자 표기가 "[관리자] 이름" 형태이므로 실제 이름만 사용합니다.
     *
     * @param {Object} message 메시지
     * @returns {string} 아바타에 표시할 한 글자
     */
    function getAvatarInitial(message) {

        const name = String((message && message.senderName) || "").trim();

        return name ? name.charAt(0).toUpperCase() : "?";
    }

    /**
     * 발신자 식별키를 해시하여 항상 같은 사람이 같은 색 아바타를 갖도록 합니다.
     *
     * @param {Object} message 메시지
     * @returns {string} avatar-color-N 클래스명
     */
    function getAvatarColorClass(message) {

        const identity = getMessageSenderIdentity(message) || "unknown";

        let hash = 0;

        for (let index = 0; index < identity.length; index += 1) {
            hash = (hash * 31 + identity.charCodeAt(index)) >>> 0;
        }

        return "avatar-color-" + (hash % AVATAR_COLOR_COUNT);
    }

    let latestMessageList = [];
    let participantReadList = [];
    let lastSavedReadKey = "";
    let readStateRequestInFlight = false;
    let readerRefreshTimer = null;

    /*
     * 안읽음 인원수 배지가 나중에 추가/제거되면서 메시지 영역 높이가
     * 바뀌어도, 사용자가 과거 메시지를 읽으려고 위로 스크롤해둔 상태라면
     * 그 위치를 강제로 끌어내리지 않기 위한 임계값입니다.
     */
    const NEAR_BOTTOM_THRESHOLD_PX = 80;

    /**
     * 메시지 영역이 이미 맨 아래(또는 그 근처)에 있는지 확인합니다.
     *
     * @returns {boolean} 맨 아래 근처 여부
     */
    function isMessageAreaNearBottom() {

        if (!messageArea) {
            return false;
        }

        const distanceFromBottom =
            messageArea.scrollHeight
            - messageArea.scrollTop
            - messageArea.clientHeight;

        return distanceFromBottom <= NEAR_BOTTOM_THRESHOLD_PX;
    }

    /**
     * 메시지 영역을 맨 아래로 스크롤합니다.
     */
    function scrollMessageAreaToBottom() {

        if (!messageArea) {
            return;
        }

        messageArea.scrollTop = messageArea.scrollHeight;
    }

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
            memberNo,
            businessNo,
            businessName,
            message,
            roomType,
            role,
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
            await showAlert("자유방 메시지만 수정할 수 있습니다.", "warning");
            return;
        }

        if (!message || !message.id) {
            await showAlert("수정할 메시지 정보를 확인할 수 없습니다.", "warning");
            return;
        }

        if (!isCurrentUserMessage(message)
                || message.type !== "CHAT") {
            await showAlert("본인이 작성한 메시지만 수정할 수 있습니다.", "warning");
            return;
        }

        if (!isWithinEditLimit(message)) {
            await showAlert("메시지는 전송 후 5분 이내에만 수정할 수 있습니다.", "warning");
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
            await showAlert("메시지 내용을 입력해주세요.", "warning");
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
            await showAlert("자유방 메시지만 삭제할 수 있습니다.", "warning");
            return;
        }

        if (!message || !message.id) {
            await showAlert("삭제할 메시지 정보를 확인할 수 없습니다.", "warning");
            return;
        }

        if (!isCurrentUserMessage(message)
                || message.type !== "CHAT") {
            await showAlert("본인이 작성한 메시지만 삭제할 수 있습니다.", "warning");
            return;
        }

        const deleteConfirmed = await showConfirm("이 메시지를 삭제하시겠습니까?", "warning");

        if (!deleteConfirmed) {
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
     * 날짜 구분선을 표시할 때 사용할 연월일 비교값을 만듭니다.
     * 같은 날짜의 메시지에는 구분선을 한 번만 표시합니다.
     *
     * @param {Object} timestamp Firestore Timestamp
     * @returns {string|null} yyyy-MM-dd 형태의 비교값
     */
    function getDateKey(timestamp) {

        const minuteKey = getMinuteKey(timestamp);

        return minuteKey ? minuteKey.substring(0, 10) : null;
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

        return getMessageSenderIdentity(firstMessage)
                === getMessageSenderIdentity(secondMessage)
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
            && isCurrentUserMessage(message)
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
     * 현재 화면에 수신된 메시지 중 전송 시각이 확정된 마지막 메시지를 찾습니다.
     */
    function getLatestReadableMessage() {

        for (let index = latestMessageList.length - 1; index >= 0; index -= 1) {
            const message = latestMessageList[index];

            if (message && message.id && getTimestampMillis(message.sendTime) > 0) {
                return message;
            }
        }

        return null;
    }

    /**
     * 사용자가 채팅방을 실제로 보고 있을 때 마지막 읽음 위치를 Oracle에 저장합니다.
     */
    async function saveLatestReadState() {

        if (document.visibilityState !== "visible"
                || !Number.isFinite(memberNo)
                || memberNo <= 0
                || readStateRequestInFlight) {
            return;
        }

        const latestMessage = getLatestReadableMessage();

        if (!latestMessage) {
            return;
        }

        const lastReadEpochMs = getTimestampMillis(latestMessage.sendTime);
        const readKey = latestMessage.id + ":" + lastReadEpochMs;

        if (readKey === lastSavedReadKey) {
            return;
        }

        readStateRequestInFlight = true;

        try {

            const body = new URLSearchParams();
            body.set("roomId", roomId);
            body.set("lastReadMessageId", latestMessage.id);
            body.set("lastReadEpochMs", String(lastReadEpochMs));

            const response = await fetch(contextPath + "/chat/api/read", {
                method: "POST",
                headers: {
                    "Content-Type": "application/x-www-form-urlencoded; charset=UTF-8",
                    "Accept": "application/json"
                },
                body: body.toString()
            });

            if (!response.ok) {
                throw new Error("채팅 읽음 처리 요청 실패: " + response.status);
            }

            const result = await response.json();

            if (!result.success) {
                throw new Error(result.message || "채팅 읽음 처리에 실패했습니다.");
            }

            lastSavedReadKey = readKey;

            window.dispatchEvent(
                new CustomEvent("oditji:chat-read-updated", {
                    detail: {
                        roomId,
                        lastReadMessageId: latestMessage.id,
                        lastReadEpochMs
                    }
                })
            );

            await refreshParticipantReadList();

        } catch (error) {
            console.error("채팅 읽음 위치 저장 실패:", error);
        } finally {
            readStateRequestInFlight = false;

            const newestMessage = getLatestReadableMessage();

            if (newestMessage) {
                const newestKey = newestMessage.id
                    + ":"
                    + getTimestampMillis(newestMessage.sendTime);

                if (newestKey !== lastSavedReadKey) {
                    saveLatestReadState();
                }
            }
        }
    }

    /**
     * 메시지별 미열람 인원 계산을 위해 참여자별 마지막 읽음 위치를 조회합니다.
     */
    async function refreshParticipantReadList() {

        if (!roomId.trim()) {
            return;
        }

        try {

            const response = await fetch(
                contextPath
                    + "/chat/api/rooms/"
                    + encodeURIComponent(roomId)
                    + "/readers",
                {
                    method: "GET",
                    headers: {
                        "Accept": "application/json"
                    },
                    cache: "no-store"
                }
            );

            if (!response.ok) {
                throw new Error("참여자 읽음 정보 요청 실패: " + response.status);
            }

            const data = await response.json();
            participantReadList = Array.isArray(data) ? data : [];
            updateUnreadParticipantIndicators();

        } catch (error) {
            console.error("참여자 읽음 정보 조회 실패:", error);
        }
    }

    function createMessageIndexMap() {

        const indexMap = new Map();

        latestMessageList.forEach(function(message, index) {
            if (message && message.id) {
                indexMap.set(message.id, index);
            }
        });

        return indexMap;
    }

    function isParticipantSender(participant, message) {

        const senderMemberNo = toPositiveNumber(message.senderMemberNo);
        const participantMemberNo = toPositiveNumber(participant.memberNo);

        if (senderMemberNo !== null) {
            return participantMemberNo !== null
                && senderMemberNo === participantMemberNo;
        }

        const senderRole = normalizeRole(message.senderRole);
        const noticeMessage = roomType === "NOTICE"
            || message.type === "NOTICE";

        /*
         * 공지방 참여자 조회 결과에는 승인된 사업자만 포함되고
         * 공지 작성자인 관리자는 포함되지 않습니다. 따라서 과거 공지 메시지의
         * senderBusinessNo가 특정 사업자 번호와 같더라도 발신자로 제외하면 안 됩니다.
         */
        if (noticeMessage || senderRole === "ADMIN") {
            return false;
        }

        const senderBusinessNo = toPositiveNumber(
            message.senderBusinessNo
        );
        const participantBusinessNo = toPositiveNumber(
            participant.businessNo
        );

        return senderBusinessNo !== null
            && participantBusinessNo !== null
            && senderBusinessNo === participantBusinessNo;
    }

    function hasParticipantReadMessage(
        participant,
        message,
        messageIndex,
        messageIndexMap
    ) {

        const lastReadMessageId = String(
            participant.lastReadMessageId || ""
        );

        if (lastReadMessageId && messageIndexMap.has(lastReadMessageId)) {
            return messageIndexMap.get(lastReadMessageId) >= messageIndex;
        }

        const lastReadEpochMs = Number(participant.lastReadEpochMs) || 0;
        const messageEpochMs = getTimestampMillis(message.sendTime);

        return lastReadEpochMs >= messageEpochMs;
    }

    /**
     * 메시지를 아직 읽지 않은 현재 채팅방 참여자 수를 계산합니다.
     */
    function calculateUnreadParticipantCount(message, messageIndex) {

        if (!message
                || message.type === "SYSTEM"
                || !Array.isArray(participantReadList)
                || participantReadList.length === 0) {
            return 0;
        }

        const messageEpochMs = getTimestampMillis(message.sendTime);

        if (messageEpochMs <= 0) {
            return 0;
        }

        const messageIndexMap = createMessageIndexMap();
        let unreadCount = 0;

        participantReadList.forEach(function(participant) {

            if (!participant || isParticipantSender(participant, message)) {
                return;
            }

            const joinedAtEpochMs = Number(participant.joinedAtEpochMs) || 0;

            if (joinedAtEpochMs > messageEpochMs) {
                return;
            }

            if (!hasParticipantReadMessage(
                    participant,
                    message,
                    messageIndex,
                    messageIndexMap)) {

                unreadCount += 1;
            }
        });

        return unreadCount;
    }

    /**
     * 참여자 읽음 상태가 변경되면 말풍선을 다시 만들지 않고 숫자만 갱신합니다.
     */
    function updateUnreadParticipantIndicators() {

        if (!messageArea || latestMessageList.length === 0) {
            return;
        }

        /*
         * 안읽음 배지가 새로 붙거나 떨어지면서 메시지 줄 높이가 바뀌기 전에
         * 현재 스크롤이 맨 아래 근처였는지 먼저 기억해둡니다.
         * 배지 반영 후 이 상태였을 때만 다시 맨 아래로 맞춰서,
         * 메시지 전송 직후 배지가 붙으며 살짝 가려지는 문제를 막습니다.
         * (과거 메시지를 읽으려고 위로 스크롤해둔 경우에는 끌어내리지 않습니다.)
         */
        const wasNearBottom = isMessageAreaNearBottom();

        const messageById = new Map();
        const messageIndexById = new Map();

        latestMessageList.forEach(function(message, index) {
            if (message && message.id) {
                messageById.set(message.id, message);
                messageIndexById.set(message.id, index);
            }
        });

        messageArea
            .querySelectorAll(".message-line[data-message-id]")
            .forEach(function(messageLine) {

                const messageId = messageLine.dataset.messageId;
                const message = messageById.get(messageId);
                const messageIndex = messageIndexById.get(messageId);

                if (!message || messageIndex === undefined) {
                    return;
                }

                const unreadCount = calculateUnreadParticipantCount(
                    message,
                    messageIndex
                );

                let messageMeta = messageLine.querySelector(".message-meta");
                let indicator = messageLine.querySelector(
                    ".unread-participant-count"
                );

                if (unreadCount <= 0) {
                    if (indicator) {
                        indicator.remove();
                    }

                    if (messageMeta && messageMeta.childElementCount === 0) {
                        messageMeta.remove();
                    }

                    return;
                }

                if (!messageMeta) {
                    messageMeta = document.createElement("div");
                    messageMeta.className = "message-meta";

                    const actionArea = messageLine.querySelector(
                        ".message-actions"
                    );

                    if (actionArea) {
                        messageLine.insertBefore(messageMeta, actionArea);
                    } else {
                        messageLine.appendChild(messageMeta);
                    }
                }

                if (!indicator) {
                    indicator = document.createElement("span");
                    indicator.className = "unread-participant-count";
                    messageMeta.insertBefore(
                        indicator,
                        messageMeta.firstChild
                    );
                }

                indicator.textContent = String(unreadCount);
                indicator.title = unreadCount
                    + "명이 아직 이 메시지를 읽지 않았습니다.";
                indicator.setAttribute(
                    "aria-label",
                    unreadCount + "명이 아직 읽지 않음"
                );
            });

        if (wasNearBottom) {
            scrollMessageAreaToBottom();
        }
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

        let lastRenderedDateKey = null;

        messageList.forEach(function(message, index) {

            const currentDateKey = getDateKey(message.sendTime);

            if (currentDateKey
                    && currentDateKey !== lastRenderedDateKey) {

                const dateDivider = document.createElement("div");
                dateDivider.className = "date-divider";
                dateDivider.textContent = formatDate(message.sendTime);
                messageArea.appendChild(dateDivider);
                lastRenderedDateKey = currentDateKey;
            }

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

            const mine = isCurrentUserMessage(message);
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
            let sender = null;

            if (!sameAsPrevious) {
                sender = document.createElement("div");
                sender.className = "sender";
                sender.textContent = noticeMessage
                    ? "[관리자] " + message.senderName
                    : message.senderName;
            }

            const messageLine = document.createElement("div");
            messageLine.className = "message-line";
            messageLine.dataset.messageId = message.id || "";

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

            const unreadParticipantCount =
                calculateUnreadParticipantCount(message, index);

            if (unreadParticipantCount > 0) {
                const unreadIndicator = document.createElement("span");
                unreadIndicator.className = "unread-participant-count";
                unreadIndicator.textContent = String(unreadParticipantCount);
                unreadIndicator.title = unreadParticipantCount
                    + "명이 아직 이 메시지를 읽지 않았습니다.";
                unreadIndicator.setAttribute(
                    "aria-label",
                    unreadParticipantCount + "명이 아직 읽지 않음"
                );
                messageMeta.appendChild(unreadIndicator);
            }

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

            if (mine) {
                /* 내 메시지: 발신자 표기가 없으므로 기존과 동일하게 바로 붙입니다. */
                row.appendChild(messageLine);
            } else {
                /*
                 * 상대 메시지: 카카오톡처럼 좌측에 원형 프로필을 두고,
                 * 이름 + 말풍선을 세로로 쌓은 콘텐츠 컬럼을 오른쪽에 배치합니다.
                 * 묶음 중간/끝 메시지도 정렬을 맞추기 위해 아바타 자리는 항상 만들되
                 * CSS(group-middle/group-end)에서 시각적으로만 숨깁니다.
                 */
                const avatarSlot = document.createElement("div");
                avatarSlot.className = "avatar-slot";

                if (!sameAsPrevious) {
                    const avatarCircle = document.createElement("div");
                    avatarCircle.className =
                        "avatar-circle " + getAvatarColorClass(message);
                    avatarCircle.textContent = getAvatarInitial(message);
                    avatarSlot.appendChild(avatarCircle);
                }

                const contentCol = document.createElement("div");
                contentCol.className = "content-col";

                if (sender) {
                    contentCol.appendChild(sender);
                }

                contentCol.appendChild(messageLine);

                row.appendChild(avatarSlot);
                row.appendChild(contentCol);
            }

            messageArea.appendChild(row);
        });

        scrollMessageAreaToBottom();
    }

    /**
     * 자유방 참가 기록을 삭제하고 채팅방 목록으로 이동합니다.
     * 사업자 번호는 서버 세션에서 확인하므로 요청 본문에는 roomId만 전달합니다.
     */
    async function leaveRoom() {

        const leaveConfirmed = await showConfirm("채팅방에서 나가시겠습니까?", "warning");

        if (!leaveConfirmed) {
            return;
        }

        if (!roomId.trim()) {
            await showAlert("채팅방 정보를 확인할 수 없습니다.", "warning");
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
        .then(async function(data) {

            await showAlert(data.message || "채팅방 나가기 처리가 완료되었습니다.", data.success ? "success" : "info");

            if (data.success) {
                goToRoomList(
                    roomType === "PUBLIC" ? { left: true, roomId: roomId } : null
                );
            }
        })
        .catch(function(error) {
            console.error("채팅방 나가기 중 오류:", error);
            showAlert("채팅방 나가기 중 오류가 발생했습니다.", "error");
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
            goToRoomList();
        });
    }

    /**
     * 채팅방 목록으로 이동합니다.
     * iframe으로 임베드된 상태(데스크톱 중앙 패널/모바일)에서는 iframe 자체를
     * 이동시키지 않고 부모 대시보드(roomList)에 위임하여 좌측 목록 화면으로 돌아갑니다.
     *
     * @param {Object} [extraData] close-room 메시지에 함께 실어 보낼 추가 데이터.
     *        나가기(leaveRoom) 성공 시 roomList.js가 좌측 목록의 참가 상태(참가/입장 ↔ 입장)를
     *        새로고침 없이 즉시 되돌릴 수 있도록 { left: true, roomId } 형태로 전달합니다.
     */
    function goToRoomList(extraData) {

        if (isEmbedded && window.parent) {
            window.parent.postMessage(
                Object.assign(
                    { source: "oditji-chat-room", action: "close-room" },
                    extraData || {}
                ),
                window.location.origin
            );
            return;
        }

        location.href = contextPath + "/chat/list";
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

    const unsubscribeMessages = listenMessages(
        roomId,
        async function(messageList) {
            latestMessageList = Array.isArray(messageList)
                ? messageList
                : [];
            renderMessages(latestMessageList);
            await saveLatestReadState();
            await refreshParticipantReadList();
        }
    );

    refreshParticipantReadList();

    readerRefreshTimer = window.setInterval(
        refreshParticipantReadList,
        READER_REFRESH_INTERVAL_MS
    );

    document.addEventListener("visibilitychange", function() {
        if (document.visibilityState === "visible") {
            saveLatestReadState();
        }
    });

    window.addEventListener("focus", function() {
        saveLatestReadState();
    });

    window.addEventListener("beforeunload", function() {

        if (readerRefreshTimer !== null) {
            window.clearInterval(readerRefreshTimer);
        }

        if (typeof unsubscribeMessages === "function") {
            unsubscribeMessages();
        }
    });
});
