import { db } from "./firebase-config.js";

import {
    collection,
    query,
    orderBy,
    onSnapshot
} from "https://www.gstatic.com/firebasejs/12.15.0/firebase-firestore.js";

/**
 * ODITJI 공통 헤더 알림 센터입니다.
 *
 * 현재는 채팅 알림을 연결합니다.
 * 이후 환불 요청, 배송 변경 등의 기능은 아래 전역 API에 별도 source를 등록하면
 * 같은 벨과 상세 목록을 그대로 사용할 수 있습니다.
 *
 * window.oditjiNotificationCenter.setSource("refund", {
 *     badgeCount: 1,
 *     items: [{ id, title, message, href, typeLabel }]
 * });
 */
function initializeHeaderNotification() {

    const menu = document.getElementById("notificationMenu");
    const button = document.getElementById("notificationBtn");
    const dropdown = document.getElementById("notificationDropdown");
    const badge = document.getElementById("notificationBadge");
    const summary = document.getElementById("notificationSummary");
    const list = document.getElementById("notificationList");
    const clearAllButton = document.getElementById(
        "notificationClearAllBtn"
    );

    if (!menu
            || !button
            || !dropdown
            || !badge
            || !summary
            || !list
            || !clearAllButton) {
        return;
    }

    const contextPath = menu.dataset.contextPath || "";
    const chatEnabled = menu.dataset.chatEnabled === "true";
    const notificationSources = new Map();
    const roomSubscriptions = new Map();
    let chatContext = null;
    let contextRefreshTimer = null;

    /**
     * 알림 source를 등록하거나 갱신합니다.
     * 채팅은 badgeCount에 미읽은 채팅방 수를 전달합니다.
     */
    function setSource(sourceName, sourceData) {

        if (!sourceName || !sourceData) {
            return;
        }

        const badgeCount = Math.max(
            0,
            Number(sourceData.badgeCount) || 0
        );

        const items = Array.isArray(sourceData.items)
            ? sourceData.items
            : [];

        notificationSources.set(sourceName, {
            badgeCount,
            items,
            clearAll: typeof sourceData.clearAll === "function"
                ? sourceData.clearAll
                : null
        });

        renderNotificationCenter();
    }

    function removeSource(sourceName) {
        notificationSources.delete(sourceName);
        renderNotificationCenter();
    }

    /**
     * 다른 알림 기능이 동일한 헤더 벨을 재사용할 수 있도록 공개 API를 제공합니다.
     */
    window.oditjiNotificationCenter = {
        setSource,
        removeSource,
        refresh: renderNotificationCenter
    };

    function renderNotificationCenter() {

        const allItems = [];
        let totalBadgeCount = 0;

        notificationSources.forEach(function(sourceData, sourceName) {

            totalBadgeCount += sourceData.badgeCount;

            sourceData.items.forEach(function(item) {
                allItems.push({
                    ...item,
                    sourceName
                });
            });
        });

        if (totalBadgeCount > 0) {
            badge.hidden = false;
            clearAllButton.hidden = false;
            clearAllButton.disabled = false;
            badge.textContent = totalBadgeCount > 99
                ? "99+"
                : String(totalBadgeCount);
            badge.setAttribute(
                "aria-label",
                "새 알림 " + totalBadgeCount + "개"
            );
            summary.textContent = "새 알림 " + totalBadgeCount + "개";
        } else {
            badge.hidden = true;
            clearAllButton.hidden = true;
            clearAllButton.disabled = false;
            badge.textContent = "";
            badge.setAttribute("aria-label", "새 알림 0개");
            summary.textContent = "새 알림이 없습니다.";
        }

        list.innerHTML = "";

        if (allItems.length === 0) {
            const empty = document.createElement("div");
            empty.className = "notification-empty";
            empty.textContent = "새로운 알림이 없습니다.";
            list.appendChild(empty);
            return;
        }

        allItems.forEach(function(item) {
            list.appendChild(createNotificationItem(item));
        });
    }

    /**
     * 등록된 모든 알림 source의 일괄 읽음 처리를 실행합니다.
     * 실제 메시지나 주문 데이터를 삭제하지 않고 각 source의 읽음 상태만 갱신합니다.
     */
    async function clearAllNotifications() {

        const clearHandlers = [];

        notificationSources.forEach(function(sourceData) {
            if (sourceData.badgeCount > 0
                    && typeof sourceData.clearAll === "function") {
                clearHandlers.push(sourceData.clearAll);
            }
        });

        if (clearHandlers.length === 0) {
            return;
        }

        clearAllButton.disabled = true;
        clearAllButton.textContent = "처리 중...";

        try {
            await Promise.all(
                clearHandlers.map(clearHandler => clearHandler())
            );
            closeDropdown();
        } catch (error) {
            console.error("알림 전체 읽음 처리 실패:", error);
            window.alert(
                "알림 전체 읽음 처리 중 오류가 발생했습니다."
            );
        } finally {
            clearAllButton.disabled = false;
            clearAllButton.textContent = "알림 모두 읽음";
        }
    }

    clearAllButton.addEventListener("click", function(event) {
        event.preventDefault();
        event.stopPropagation();
        clearAllNotifications();
    });

    function createNotificationItem(item) {

        const link = document.createElement("a");
        link.className = "notification-item";
        link.href = item.href || "#";

        const top = document.createElement("div");
        top.className = "notification-item-top";

        const title = document.createElement("strong");
        title.className = "notification-item-title";
        title.textContent = item.title || "알림";

        const type = document.createElement("span");
        type.className = "notification-item-type";
        type.textContent = item.typeLabel || "알림";

        const message = document.createElement("p");
        message.className = "notification-item-message";
        message.textContent = item.message || "새로운 알림이 있습니다.";

        top.appendChild(title);
        top.appendChild(type);
        link.appendChild(top);
        link.appendChild(message);

        return link;
    }

    function openDropdown() {

        dropdown.classList.add("open");
        button.setAttribute("aria-expanded", "true");

        const profileDropdown = document.querySelector(".profile-dropdown");
        const profileButton = document.getElementById("profileBtn");

        if (profileDropdown) {
            profileDropdown.classList.remove("open");
        }

        if (profileButton) {
            profileButton.setAttribute("aria-expanded", "false");
        }
    }

    function closeDropdown() {
        dropdown.classList.remove("open");
        button.setAttribute("aria-expanded", "false");
    }

    button.addEventListener("click", function(event) {

        event.stopPropagation();

        if (dropdown.classList.contains("open")) {
            closeDropdown();
            return;
        }

        openDropdown();
    });

    dropdown.addEventListener("click", function(event) {
        event.stopPropagation();
    });

    document.addEventListener("click", closeDropdown);

    document.addEventListener("keydown", function(event) {
        if (event.key === "Escape") {
            closeDropdown();
        }
    });

    /**
     * Firestore Timestamp를 epoch millisecond로 변환합니다.
     */
    function getMessageEpochMs(message) {

        if (!message
                || !message.sendTime
                || typeof message.sendTime.toMillis !== "function") {
            return 0;
        }

        return message.sendTime.toMillis();
    }

    function isOwnMessage(message) {

        if (!chatContext || !message) {
            return false;
        }

        const currentMemberNo = Number(chatContext.memberNo);
        const currentBusinessNo = Number(chatContext.businessNo);
        const senderMemberNo = Number(message.senderMemberNo);
        const senderBusinessNo = Number(message.senderBusinessNo);

        if (Number.isFinite(senderMemberNo)
                && senderMemberNo > 0
                && Number.isFinite(currentMemberNo)) {

            return senderMemberNo === currentMemberNo;
        }

        return Number.isFinite(senderBusinessNo)
            && Number.isFinite(currentBusinessNo)
            && senderBusinessNo === currentBusinessNo;
    }

    /**
     * 특정 방에서 현재 사용자가 읽지 않은 실제 메시지 수를 계산합니다.
     *
     * LAST_READ_MESSAGE_ID가 현재 스냅샷에 남아 있으면 정확한 배열 위치를 사용하고,
     * 메시지가 삭제되어 ID를 찾지 못하면 LAST_READ_EPOCH_MS를 보조 기준으로 사용합니다.
     */
    function calculateUnreadMessageCount(room, messages) {

        if (!room || !Array.isArray(messages) || messages.length === 0) {
            return 0;
        }

        const accessStartEpochMs = Number(room.accessStartEpochMs) || 0;
        const lastReadEpochMs = Number(room.lastReadEpochMs) || 0;
        const lastReadMessageId = String(room.lastReadMessageId || "");
        const lastReadIndex = lastReadMessageId
            ? messages.findIndex(message => message.id === lastReadMessageId)
            : -1;

        let unreadCount = 0;

        messages.forEach(function(message, index) {

            if (!message || message.type === "SYSTEM" || isOwnMessage(message)) {
                return;
            }

            const messageEpochMs = getMessageEpochMs(message);

            if (messageEpochMs <= 0 || messageEpochMs < accessStartEpochMs) {
                return;
            }

            const unread = lastReadIndex >= 0
                ? index > lastReadIndex
                : messageEpochMs > lastReadEpochMs;

            if (unread) {
                unreadCount += 1;
            }
        });

        return unreadCount;
    }

    /**
     * 현재 미읽음이 존재하는 모든 채팅방을 최신 메시지 위치까지 읽음 처리합니다.
     * Firestore 메시지는 삭제하지 않으며 Oracle의 마지막 읽음 위치만 갱신합니다.
     */
    async function clearAllChatNotifications() {

        if (!chatContext || !Array.isArray(chatContext.roomList)) {
            return;
        }

        const readTargets = [];

        chatContext.roomList.forEach(function(room) {

            const subscription = roomSubscriptions.get(room.roomId);
            const messages = subscription ? subscription.messages : [];
            const unreadMessageCount = calculateUnreadMessageCount(
                room,
                messages
            );

            if (unreadMessageCount <= 0 || messages.length === 0) {
                return;
            }

            const latestMessage = [...messages]
                .reverse()
                .find(message => getMessageEpochMs(message) > 0);

            if (!latestMessage) {
                return;
            }

            readTargets.push({
                room,
                messageId: latestMessage.id,
                epochMs: getMessageEpochMs(latestMessage)
            });
        });

        if (readTargets.length === 0) {
            return;
        }

        const results = await Promise.all(
            readTargets.map(async function(target) {

                const formData = new URLSearchParams();
                formData.set("roomId", target.room.roomId);
                formData.set("lastReadMessageId", target.messageId);
                formData.set("lastReadEpochMs", String(target.epochMs));

                const response = await fetch(
                    contextPath + "/chat/api/read",
                    {
                        method: "POST",
                        headers: {
                            "Content-Type": "application/x-www-form-urlencoded; charset=UTF-8",
                            "Accept": "application/json"
                        },
                        body: formData.toString()
                    }
                );

                if (!response.ok) {
                    throw new Error(
                        "채팅 읽음 처리 요청 실패: " + response.status
                    );
                }

                const result = await response.json();

                if (!result.success) {
                    throw new Error(
                        result.message || "채팅 읽음 처리에 실패했습니다."
                    );
                }

                target.room.lastReadMessageId = target.messageId;
                target.room.lastReadEpochMs = target.epochMs;

                return result;
            })
        );

        if (results.length > 0) {
            updateChatSource();
            await loadChatContext();

            window.dispatchEvent(
                new CustomEvent("oditji:chat-read-updated")
            );
        }
    }

    /**
     * 채팅 source는 미읽은 메시지가 존재하는 방만 상세에 표시합니다.
     * badgeCount는 방별 실제 메시지 수의 합이 아니라 해당 방의 개수입니다.
     */
    function updateChatSource() {

        if (!chatContext || !Array.isArray(chatContext.roomList)) {
            setSource("chat", {
                badgeCount: 0,
                items: [],
                clearAll: clearAllChatNotifications
            });
            return;
        }

        const items = [];

        chatContext.roomList.forEach(function(room) {

            const subscription = roomSubscriptions.get(room.roomId);
            const messages = subscription ? subscription.messages : [];
            const unreadMessageCount = calculateUnreadMessageCount(
                room,
                messages
            );

            if (unreadMessageCount <= 0) {
                return;
            }

            items.push({
                id: "chat-" + room.roomId,
                title: room.roomName,
                typeLabel: room.roomType === "NOTICE" ? "공지방" : "자유방",
                message: "읽지 않은 메시지가 "
                    + unreadMessageCount
                    + "개 있습니다.",
                href: contextPath
                    + "/chat/room/"
                    + encodeURIComponent(room.roomId)
            });
        });

        setSource("chat", {
            badgeCount: items.length,
            items,
            clearAll: clearAllChatNotifications
        });
    }

    function subscribeRoom(room) {

        const messageQuery = query(
            collection(db, "chatRooms", room.roomId, "messages"),
            orderBy("sendTime", "asc")
        );

        const subscription = {
            room,
            messages: [],
            unsubscribe: null
        };

        subscription.unsubscribe = onSnapshot(
            messageQuery,
            function(snapshot) {

                subscription.messages = snapshot.docs.map(function(documentSnapshot) {
                    return {
                        id: documentSnapshot.id,
                        ...documentSnapshot.data()
                    };
                });

                updateChatSource();
            },
            function(error) {
                console.error(
                    "헤더 채팅 알림 메시지 조회 실패:",
                    room.roomId,
                    error
                );
            }
        );

        roomSubscriptions.set(room.roomId, subscription);
    }

    /**
     * 서버에서 접근 가능한 방과 읽음 위치를 다시 받아
     * Firestore 구독 목록을 동기화합니다.
     */
    function synchronizeRoomSubscriptions() {

        const roomList = chatContext && Array.isArray(chatContext.roomList)
            ? chatContext.roomList
            : [];

        const nextRoomIds = new Set(
            roomList.map(room => room.roomId)
        );

        roomSubscriptions.forEach(function(subscription, roomId) {

            if (nextRoomIds.has(roomId)) {
                return;
            }

            if (typeof subscription.unsubscribe === "function") {
                subscription.unsubscribe();
            }

            roomSubscriptions.delete(roomId);
        });

        roomList.forEach(function(room) {

            const existing = roomSubscriptions.get(room.roomId);

            if (existing) {
                existing.room = room;
                return;
            }

            subscribeRoom(room);
        });

        updateChatSource();
    }

    async function loadChatContext() {

        if (!chatEnabled) {
            setSource("chat", {
                badgeCount: 0,
                items: [],
                clearAll: clearAllChatNotifications
            });
            return;
        }

        try {

            const response = await fetch(
                contextPath + "/chat/api/notifications/context",
                {
                    method: "GET",
                    headers: {
                        "Accept": "application/json"
                    },
                    cache: "no-store"
                }
            );

            if (!response.ok) {
                throw new Error(
                    "채팅 알림 컨텍스트 요청 실패: " + response.status
                );
            }

            chatContext = await response.json();
            synchronizeRoomSubscriptions();

        } catch (error) {
            console.error("헤더 채팅 알림 초기화 실패:", error);
        }
    }

    setSource("chat", {
        badgeCount: 0,
        items: []
    });

    loadChatContext();

    if (chatEnabled) {
        contextRefreshTimer = window.setInterval(
            loadChatContext,
            5000
        );
    }

    window.addEventListener("oditji:chat-read-updated", function() {
        loadChatContext();
    });

    window.addEventListener("beforeunload", function() {

        if (contextRefreshTimer !== null) {
            window.clearInterval(contextRefreshTimer);
        }

        roomSubscriptions.forEach(function(subscription) {
            if (typeof subscription.unsubscribe === "function") {
                subscription.unsubscribe();
            }
        });
    });
}

if (document.readyState === "loading") {
    document.addEventListener(
        "DOMContentLoaded",
        initializeHeaderNotification
    );
} else {
    initializeHeaderNotification();
}
