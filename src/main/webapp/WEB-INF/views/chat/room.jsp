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
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>${room.roomName}</title>
<link rel="stylesheet"
      href="${pageContext.request.contextPath}/css/chat-common.css?v=1">
<link rel="stylesheet"
      href="${pageContext.request.contextPath}/css/chat-room.css?v=3">

<%-- embed 모드에서 header.jsp(내비게이션 바)는 생략하더라도
     CSRF 토큰과 common.js(CSRF 자동 첨부, showAlert)는
     /chat/api/read, /chat/api/leave fetch 요청에 필수이므로 항상 로드합니다. --%>
<c:if test="${isEmbedded}">
    <jsp:include page="/WEB-INF/views/common/head-assets.jsp"/>
</c:if>
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
                          maxlength="2000"
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
        src="${pageContext.request.contextPath}/js/room.js?v=5"></script>

</body>
</html>
