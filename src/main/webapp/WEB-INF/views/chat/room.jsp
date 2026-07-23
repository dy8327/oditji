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

<div id="chatPageData"
     data-context-path="${pageContext.request.contextPath}"
     data-room-id="${room.roomId}"
     data-room-type="${room.roomType}"
     data-business-no="${businessNo}"
     data-business-name="${businessName}"
     data-admin="${isAdmin}">
</div>

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
                    id="roomListBtn">
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

<script type="module"
        src="${pageContext.request.contextPath}/js/room.js"></script>

</body>
</html>
