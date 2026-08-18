<%@ page language="java"
    contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>

<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<%--
    embed=1 로 접근하면 roomList.jsp 의 중앙 패널(iframe) 안에서 렌더링된다.
    이 페이지 자체는 독립 URL(/chat/room/{roomId})로도 그대로 동작해야 하므로
    embed 여부는 요청 파라미터로만 판단하고 컨트롤러/서비스는 건드리지 않는다.
--%>
<c:set var="isEmbedded" value="${param.embed eq '1'}" />

<!DOCTYPE html>
<html lang="ko">
<head>
<meta charset="UTF-8">
<title>${room.roomName}</title>
<link rel="stylesheet"
      href="${pageContext.request.contextPath}/css/chat-common.css?v=1">
<link rel="stylesheet"
      href="${pageContext.request.contextPath}/css/chat-room.css?v=6">

<%-- head-assets.jsp(CSS/공통 스크립트/CSRF meta/viewport)는 header.jsp가 body 안에서
     include하는 대신, embed 여부와 무관하게 항상 head 레벨에서 로드합니다.
     embed 모드에서는 header.jsp(내비게이션 바) 자체를 생략하더라도
     CSRF 토큰과 common.js(CSRF 자동 첨부, showAlert)는
     /chat/api/read, /chat/api/leave fetch 요청에 필수이므로 항상 필요합니다. --%>
<jsp:include page="/WEB-INF/views/common/head-assets.jsp"/>
</head>
<body class="chat-page ${isEmbedded ? 'embedded' : ''}">

<c:if test="${not isEmbedded}">
    <jsp:include page="/WEB-INF/views/common/header.jsp"/>
</c:if>

<div id="chatPageData"
     data-context-path="${pageContext.request.contextPath}"
     data-room-id="${room.roomId}"
     data-room-type="${room.roomType}"
     data-member-no="${memberNo}"
     data-business-no="${businessNo}"
     data-business-name="${businessName}"
     data-role="${role}"
     data-admin="${isAdmin}"
     data-embedded="${isEmbedded}">
</div>

<div id="mainContent" class="chat-container ${isNoticeRoom ? 'notice-room-container' : ''}">

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

        <%-- embed 모드(데스크톱 중앙 패널)에서는 좌측 목록이 항상 보이므로
             이 버튼은 CSS(body.chat-page.embedded #roomListBtn)에서 숨긴다.
             모바일 전체화면 진입 시에는 그대로 노출되어 뒤로 가기 역할을 한다. --%>
        <div class="top-btn-area">
            <button type="button"
                    id="participantListBtn"
                    aria-haspopup="dialog"
                    aria-controls="participantModal">
                참여자 목록 <span id="participantCountText"></span>
            </button>

            <button type="button"
                    id="roomListBtn">
                목록으로
            </button>
        </div>

    </div>

    <div class="chat-info">
        <span>접속 사용자: <c:out value="${businessName}" /></span>
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

    <%--
        자유방(PUBLIC)은 사업자 메시지 입력 영역을 항상 출력합니다.
        공지방(NOTICE)은 관리자에게만 입력 영역을 제공하고, 사업자는 읽기 전용입니다.
        자유방 입력창이 공지방 조건 분기에 함께 묶여 사라지는 일을 막기 위해
        자유방 조건을 가장 먼저 명시적으로 분리합니다.
    --%>
    <c:choose>

        <c:when test="${not isNoticeRoom}">
            <div class="input-area chat-input-area">

                <label for="messageInput"
                       style="position:absolute;
                              width:1px;
                              height:1px;
                              padding:0;
                              margin:-1px;
                              overflow:hidden;
                              clip:rect(0, 0, 0, 0);
                              white-space:nowrap;
                              border:0;">
                    채팅 메시지 입력
                </label>

                <textarea id="messageInput"
                          maxlength="2000"
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
        </c:when>

        <c:when test="${isAdmin}">
            <div class="input-area chat-input-area">

                <label for="messageInput"
                       style="position:absolute;
                              width:1px;
                              height:1px;
                              padding:0;
                              margin:-1px;
                              overflow:hidden;
                              clip:rect(0, 0, 0, 0);
                              white-space:nowrap;
                              border:0;">
                    공지 내용 입력
                </label>

                <textarea id="messageInput"
                          maxlength="2000"
                          placeholder="공지 내용을 입력하세요."></textarea>

                <button type="button"
                        class="send-btn"
                        id="sendBtn">
                    전송
                </button>

            </div>
        </c:when>

        <c:otherwise>
            <div class="readonly-footer">
                관리자만 공지 메시지를 작성할 수 있습니다.
            </div>
        </c:otherwise>

    </c:choose>

</div>

<%--
    현재 채팅방의 참여자 목록입니다.
    자유방은 CHAT_ROOM_MEMBER 현재 참가자, 공지방은 서버에서 반환하는 논리적 참여자 목록을 사용합니다.
--%>
<div id="participantModal"
     class="participant-modal"
     hidden>
    <dialog class="participant-modal-panel"
        open
        aria-modal="true"
        aria-labelledby="participantModalTitle">

        <div class="participant-modal-header">
            <div>
                <h3 id="participantModalTitle">참여자 목록</h3>
                <p id="participantSummary">참여자 정보를 불러오는 중입니다.</p>
            </div>

            <button type="button"
                    id="participantModalClose"
                    class="participant-modal-close"
                    aria-label="참여자 목록 닫기">
                ×
            </button>
        </div>

        <ul id="participantList"
            class="participant-list"
            aria-live="polite"></ul>
    </dialog>
</div>

<script type="module"
        src="${pageContext.request.contextPath}/js/room.js?v=8"></script>

</body>
</html>
