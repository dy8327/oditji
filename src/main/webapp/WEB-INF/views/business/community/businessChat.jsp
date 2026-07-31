<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<c:set var="activeMenu" value="chat"/>

<!DOCTYPE html>
<html lang="ko">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>ODITJI | 실시간 채팅</title>
<link rel="stylesheet" href="${pageContext.request.contextPath}/css/business.css">
</head>

<body>

<jsp:include page="/WEB-INF/views/common/header.jsp"/>

<div class="business-wrap">

    <jsp:include page="/WEB-INF/views/common/businessSidebar.jsp"/>

    <main id="mainContent" class="main-content">

        <a href="${pageContext.request.contextPath}/business/main"
           class="back-link">
            ← 뒤로가기
        </a>

        <h1 class="page-title">
            실시간 채팅
        </h1>

        <section class="chat-layout">

            <aside class="chat-list">

                <h2 class="chat-title">
                    대화 목록
                </h2>

                <c:choose>

                    <c:when test="${not empty chatRoomList}">

                        <c:forEach var="room" items="${chatRoomList}">

                            <a href="${pageContext.request.contextPath}/business/chat/list?roomId=${room.roomId}"
                               class="chat-user ${room.roomId eq activeRoom.roomId ? 'active' : ''}">
                                ${room.roomName}
                            </a>

                        </c:forEach>

                    </c:when>

                    <c:otherwise>

                        <div class="chat-user">
                            참여중인 채팅방이 없습니다.
                        </div>

                    </c:otherwise>

                </c:choose>

            </aside>


            <div class="chat-room">

                <div class="chat-message-area">

                    <c:choose>

                        <c:when test="${not empty chatMessageList}">

                            <c:forEach var="msg" items="${chatMessageList}">

                                <div class="bubble ${msg.mine ? 'me' : ''}">
                                    <c:out value="${msg.content}"/>
                                </div>

                            </c:forEach>

                        </c:when>

                        <c:otherwise>

                            <div class="bubble">
                                대화 내용이 없습니다.
                            </div>

                        </c:otherwise>

                    </c:choose>

                </div>


                <form class="chat-input"
                      action="${pageContext.request.contextPath}/business/chat/send"
                      method="post">

                    <input type="hidden"
                           name="roomId"
                           value="${activeRoom.roomId}">

                    <label for="chatMessage" class="sr-only">메시지 입력</label>
                    <input type="text"
                           id="chatMessage"
                           name="message"
                           placeholder="메시지를 입력하세요">

                    <button class="btn btn-primary"
                            type="submit">
                        전송
                    </button>

                </form>

            </div>

        </section>

    </main>

</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp"/>

</body>
</html>