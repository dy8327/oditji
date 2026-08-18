import {
    db,
    ensureFirebaseChatAuth
} from "./firebase-config.js?v=2";

import {
    collection,
    query,
    orderBy,
    limit,
    onSnapshot
} from "https://www.gstatic.com/firebasejs/12.15.0/firebase-firestore.js";

/**
 * 채팅방 목록의 입장 버튼과 최근 메시지 구독을 연결합니다.
 *
 * 데스크톱(>768px)에서는 3단 대시보드의 중앙 패널 iframe에 채팅방을 띄우고,
 * 모바일(<=768px)에서는 카카오톡 앱처럼 기존 방식대로 전체 화면 이동을 합니다.
 */
/*
 * joinAndEnterRoom()은 DOMContentLoaded 클로저 밖에서도 호출되므로,
 * 데스크톱 중앙 패널 iframe 전환 로직(openRoom)을 모듈 스코프 변수에 담아 공유합니다.
 */
let openRoomRef = null;

document.addEventListener("DOMContentLoaded", function() {

    const contextPathElement = document.getElementById("contextPath");

    if (!contextPathElement) {
        return;
    }

    const contextPath = contextPathElement.value;

    const roomFrame = document.getElementById("roomFrame");
    const roomPlaceholder = document.getElementById("roomPlaceholder");
    const createFrameDesktop = document.getElementById("createFrameDesktop");
    const createFrameMobile = document.getElementById("createFrameMobile");

    const createRoomBtn = document.getElementById("createRoomBtn");
    const mobileCreateBtn = document.getElementById("mobileCreateBtn");
    const mobileCreateModal = document.getElementById("mobileCreateModal");
    const mobileCreateClose = document.getElementById("mobileCreateClose");

    const mainContent = document.getElementById("mainContent");
    const rightPanelToggle = document.getElementById("rightPanelToggle");

    const actionButtons = document.querySelectorAll(".room-action-btn");
    const roomCards = document.querySelectorAll(".room-card");

    const MOBILE_QUERY = "(max-width: 768px)";
    const mobileMediaQuery = window.matchMedia(MOBILE_QUERY);

    let desktopCreateFrameLoaded = false;
    let mobileCreateFrameLoaded = false;
    let activeRoomId = null;

    /**
     * 현재 화면 폭이 모바일 레이아웃(카카오톡 앱처럼 단일 화면 전환)인지 확인합니다.
     */
    function isMobileLayout() {
        return mobileMediaQuery.matches;
    }

    /**
     * 방 생성 iframe이 /chat/create 를 벗어나면(폼 제출 후 서버 redirect)
     * 생성이 완료된 것으로 보고 대시보드를 새로고침해 새 방이 목록에 보이게 합니다.
     * createRoom.js는 일반 form POST(fetch 아님)로 동작하고 서버 응답 형식을
     * 알 수 없으므로, createRoom.js를 건드리지 않고 iframe의 이동만 관찰합니다.
     *
     * @param {HTMLIFrameElement|null} frame 감시할 iframe
     */
    function watchCreateFrameForSubmit(frame) {

        if (!frame) {
            return;
        }

        frame.addEventListener("load", function() {

            const currentSrc = frame.getAttribute("src");

            if (!currentSrc || currentSrc === "about:blank") {
                return;
            }

            let currentPath = null;

            try {
                currentPath = frame.contentWindow.location.pathname;
            } catch (error) {
                // 동일 앱 내부 리다이렉트만 발생하므로 정상적으로는 도달하지 않습니다.
                return;
            }

            const createPath = contextPath + "/chat/create";

            if (currentPath === createPath) {
                // 생성 폼 페이지 자체를 (다시) 불러온 것이므로 아무 것도 하지 않습니다.
                return;
            }

            // 서버가 다른 경로(목록/상세 등)로 리다이렉트했다는 뜻이므로 생성 완료로 간주합니다.
            window.location.reload();
        });
    }

    /**
     * 우측 방 생성 패널(desktop) iframe은 항상 열려 있으므로
     * 불필요한 모바일 네트워크 요청을 막기 위해 데스크톱일 때만 최초 1회 로드합니다.
     */
    function ensureDesktopCreateFrameLoaded() {

        if (!createFrameDesktop || desktopCreateFrameLoaded || isMobileLayout()) {
            return;
        }

        const src = createFrameDesktop.dataset.src;

        if (src) {
            createFrameDesktop.src = src;
            desktopCreateFrameLoaded = true;
            watchCreateFrameForSubmit(createFrameDesktop);
        }
    }

    /**
     * 모바일 방 생성 모달의 iframe은 최초로 모달을 열 때만 로드합니다.
     */
    function ensureMobileCreateFrameLoaded() {

        if (!createFrameMobile || mobileCreateFrameLoaded) {
            return;
        }

        const src = createFrameMobile.dataset.src;

        if (src) {
            createFrameMobile.src = src;
            mobileCreateFrameLoaded = true;
            watchCreateFrameForSubmit(createFrameMobile);
        }
    }

    function openMobileCreateModal() {

        if (!mobileCreateModal) {
            return;
        }

        ensureMobileCreateFrameLoaded();
        mobileCreateModal.classList.add("open");
    }

    function closeMobileCreateModal() {

        if (!mobileCreateModal) {
            return;
        }

        mobileCreateModal.classList.remove("open");
    }

    /**
     * 방 목록 카드의 선택 상태(active) 표시를 갱신합니다.
     */
    function setActiveRoomCard(roomId) {

        activeRoomId = roomId;

        roomCards.forEach(function(card) {
            card.classList.toggle(
                "active",
                card.dataset.roomId === roomId
            );
        });
    }

    /**
     * 중앙 패널을 다시 안내 문구 상태로 되돌립니다.
     * embed된 room.jsp에서 "목록으로"를 눌렀을 때 postMessage로 호출됩니다.
     */
    function resetMiddlePanel() {

        if (roomFrame) {
            roomFrame.src = "about:blank";
            roomFrame.style.display = "none";
        }

        if (roomPlaceholder) {
            roomPlaceholder.style.display = "";
        }

        setActiveRoomCard(null);
    }

    /**
     * 채팅방을 엽니다.
     * 데스크톱: 중앙 패널 iframe에 embed=1 로 로드합니다.
     * 모바일: 기존과 동일하게 전체 화면으로 이동합니다.
     */
    function openRoom(roomId) {

        if (isMobileLayout() || !roomFrame || !roomPlaceholder) {
            enterRoom(contextPath, roomId);
            return;
        }

        roomPlaceholder.style.display = "none";
        roomFrame.style.display = "";
        roomFrame.src = contextPath
            + "/chat/room/"
            + encodeURIComponent(roomId)
            + "?embed=1";

        setActiveRoomCard(roomId);
    }

    openRoomRef = openRoom;

    if (mobileCreateBtn) {
        mobileCreateBtn.addEventListener("click", openMobileCreateModal);
    }

    if (mobileCreateClose) {
        mobileCreateClose.addEventListener("click", closeMobileCreateModal);
    }

    if (mobileCreateModal) {
        mobileCreateModal.addEventListener("click", function(event) {
            if (event.target === mobileCreateModal) {
                closeMobileCreateModal();
            }
        });
    }

    /*
     * 우측 "채팅방 생성" 패널을 탭처럼 접었다 펼친다.
     * 접힌 상태에서도 탭 버튼은 항상 보이므로 언제든 다시 펼칠 수 있다.
     */
    if (rightPanelToggle && mainContent) {
        rightPanelToggle.addEventListener("click", function() {

            const collapsed = mainContent.classList.toggle("right-collapsed");

            rightPanelToggle.setAttribute("aria-expanded", String(!collapsed));

            if (!collapsed) {
                ensureDesktopCreateFrameLoaded();
            }
        });
    }

    if (createRoomBtn) {
        createRoomBtn.addEventListener("click", function() {

            if (isMobileLayout()) {
                openMobileCreateModal();
                return;
            }

            if (createFrameDesktop) {
                ensureDesktopCreateFrameLoaded();
                createFrameDesktop.scrollIntoView({ behavior: "smooth", block: "nearest" });
            }
        });
    }

    actionButtons.forEach(function(button) {

        button.addEventListener("click", function(event) {

            event.stopPropagation();

            const roomId = button.dataset.roomId;
            const roomType = button.dataset.roomType;

            if (roomType === "NOTICE") {
                openRoom(roomId);
                return;
            }

            const alreadyJoined = button.dataset.joined === "true";

            joinAndEnterRoom(contextPath, roomId, button, alreadyJoined);
        });
    });

    /*
     * 카드 전체를 클릭해도 입장/참가 버튼과 같은 동작을 하도록 합니다.
     * (버튼 클릭은 위에서 stopPropagation 하므로 중복 호출되지 않습니다.)
     */
    const readableRoomCards = [];

    roomCards.forEach(function(card) {

        card.addEventListener("click", function() {

            const button = card.querySelector(".room-action-btn");

            if (button && !button.disabled) {
                button.click();
            }
        });

        if (card.dataset.firestoreReadable === "true") {
            readableRoomCards.push(card);
        }
    });

    if (readableRoomCards.length > 0) {
        ensureFirebaseChatAuth(contextPath)
            .then(function() {
                readableRoomCards.forEach(function(card) {
                    listenLastMessage(card.dataset.roomId);
                });
            })
            .catch(function(error) {
                console.error("채팅방 최근 메시지 인증 실패:", error);
            });
    }

    /*
     * room.jsp(embed) 에서 "목록으로"를 눌렀을 때 중앙 패널을 초기화합니다.
     * 같은 오리진의 iframe에서만 오는 메시지를 신뢰합니다.
     */
    window.addEventListener("message", function(event) {

        if (event.origin !== window.location.origin) {
            return;
        }

        const data = event.data;

        if (!data || data.source !== "oditji-chat-room") {
            return;
        }

        if (data.action === "close-room") {
            resetMiddlePanel();

            /*
             * 나가기 성공 후 전달된 경우에만 roomId가 함께 온다.
             * 새로고침 없이도 좌측 목록의 참가 상태를 다시 "참가"로 되돌려
             * 다음에 같은 방에 들어갈 때 참여 확인 문구가 다시 뜨도록 한다.
             */
            if (data.left && data.roomId) {
                markRoomAsLeft(data.roomId);
            }
        }
    });

    /*
     * 데스크톱 <-> 모바일 전환 시 우측 생성 패널 iframe 로드 상태를 맞춥니다.
     */
    function handleLayoutChange() {
        ensureDesktopCreateFrameLoaded();
    }

    if (typeof mobileMediaQuery.addEventListener === "function") {
        mobileMediaQuery.addEventListener("change", handleLayoutChange);
    } else if (typeof mobileMediaQuery.addListener === "function") {
        // Safari 등 구형 브라우저 호환
        mobileMediaQuery.addListener(handleLayoutChange);
    }

    handleLayoutChange();
});

/**
 * 자유방 참가 API를 호출한 후 참가 성공 또는 기존 참가 상태이면 입장합니다.
 * 이미 참가한 방(alreadyJoined)은 확인 문구 없이 바로 입장하고,
 * 아직 참가하지 않은 방만 나가기와 마찬가지로 참여 의사를 먼저 확인합니다.
 * 사업자 번호는 서버 세션에서 확인하므로 요청 본문으로 보내지 않습니다.
 */
async function joinAndEnterRoom(contextPath, roomId, button, alreadyJoined) {

    if (!alreadyJoined) {

        const joinConfirmed = await showConfirm("자유방에 참여하시겠습니까?", "question");

        if (!joinConfirmed) {
            return;
        }
    }

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

            /*
             * 새로고침 없이 같은 방을 다시 열 때 참여 확인 문구가 또 뜨지 않도록,
             * 서버가 내려준 참가 상태를 클라이언트에서도 즉시 반영해 둔다.
             */
            button.dataset.joined = "true";
            button.textContent = "입장";

            if (typeof openRoomRef === "function") {
                openRoomRef(roomId);
            } else {
                enterRoom(contextPath, roomId);
            }
            return;
        }

        showAlert(data.message || "채팅방 참가에 실패했습니다.", "error");
    })
    .catch(function(error) {
        console.error("채팅방 참가 중 오류:", error);
        showAlert("채팅방 참가 중 오류가 발생했습니다.", "error");
    })
    .finally(function() {
        button.disabled = false;
    });
}

/**
 * 나가기 성공으로 좌측 목록에 있는 해당 자유방의 참가 상태를 되돌립니다.
 * roomList.js는 embed된 room.jsp와 다른 문서이므로, 나가기 자체는 room.js가
 * 처리하고 그 결과만 postMessage로 전달받아 여기서 버튼 상태만 동기화합니다.
 */
function markRoomAsLeft(roomId) {

    const selector = '.room-action-btn[data-room-type="PUBLIC"][data-room-id="'
        + (window.CSS && CSS.escape ? CSS.escape(roomId) : roomId)
        + '"]';

    const button = document.querySelector(selector);

    if (!button) {
        return;
    }

    button.dataset.joined = "false";
    button.textContent = "참가";
}

/**
 * 채팅방 상세 화면으로 전체 페이지 이동합니다. (모바일 / iframe 미지원 환경)
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
