<%@ page language="java"
    contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>

<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!DOCTYPE html>
<html lang="ko">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>${room.roomName}</title>
<link rel="stylesheet"
      href="${pageContext.request.contextPath}/css/chat-room.css?v=2">
</head>
<body class="chat-page">

<jsp:include page="/WEB-INF/views/common/header.jsp"/>

<div id="chatPageData"
     data-context-path="${pageContext.request.contextPath}"
     data-room-id="${room.roomId}"
     data-room-type="${room.roomType}"
     data-member-no="${memberNo}"
     data-business-no="${businessNo}"
     data-business-name="${businessName}"
     data-role="${role}"
     data-admin="${isAdmin}">
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

        <div class="top-btn-area">
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

    <c:choose>

        <c:when test="${isNoticeRoom and not isAdmin}">
            <div class="readonly-footer">
                관리자만 공지 메시지를 작성할 수 있습니다.
            </div>
        </c:when>

        <c:otherwise>
            <div class="input-area">

                <%--
                    메시지 입력창에 명시적인 label을 연결한다.
                    화면 디자인에는 영향을 주지 않으면서 스크린 리더에는
                    입력 목적이 전달되도록 label을 시각적으로만 숨긴다.
                --%>
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
                    채팅 메시지 또는 공지 내용 입력
                </label>

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
        src="${pageContext.request.contextPath}/js/room.js?v=3"></script>

</body>
</html>
