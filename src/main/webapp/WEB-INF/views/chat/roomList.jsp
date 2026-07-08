<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>

<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!DOCTYPE html>

<html>

<head>

<meta charset="UTF-8">

<title>채팅방 목록</title>

<style>

* {
    box-sizing: border-box;
    font-family: 맑은 고딕;
}

body {
    margin: 0;
    background: #f5f5f5;
}

.container {
    width: 1000px;
    margin: 40px auto;
}

.page-title {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
}

.page-title h2 {
    margin: 0;
    color: #263238;
}

.create-btn {
    padding: 10px 16px;
    border: none;
    background: #1565C0;
    color: white;
    border-radius: 6px;
    cursor: pointer;
    font-weight: bold;
}

.room-list {
    display: flex;
    flex-direction: column;
    gap: 14px;
}

.room-card {
    background: white;
    border-radius: 10px;
    padding: 18px 20px;
    box-shadow: 0 2px 8px rgba(0,0,0,0.12);
}

.room-top {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
}

.room-title {
    margin: 0;
    font-size: 20px;
    color: #263238;
}

.room-type {
    display: inline-block;
    margin-left: 8px;
    padding: 3px 8px;
    border-radius: 12px;
    font-size: 12px;
    background: #eceff1;
    color: #455a64;
}

.default-badge {
    display: inline-block;
    margin-left: 6px;
    padding: 3px 8px;
    border-radius: 12px;
    font-size: 12px;
    background: #fff3e0;
    color: #ef6c00;
}

.room-desc {
    margin: 8px 0 12px 0;
    color: #666;
    line-height: 1.5;
}

.room-info {
    font-size: 13px;
    color: #607d8b;
    display: flex;
    gap: 16px;
    flex-wrap: wrap;
}

.last-message-box {
    margin-top: 14px;
    padding: 12px;
    background: #f7f9fa;
    border-radius: 8px;
    color: #455a64;
    display: flex;
    justify-content: space-between;
    gap: 20px;
}

.last-message {
    flex: 1;
    font-size: 14px;
    color: #37474f;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
}

.last-time {
    font-size: 13px;
    color: #78909c;
    white-space: nowrap;
}

.room-buttons {
    display: flex;
    gap: 8px;
}

.room-buttons button {
    padding: 8px 12px;
    border: none;
    border-radius: 6px;
    color: white;
    cursor: pointer;
}

.enter-btn {
    background: #455a64;
}

.join-btn {
    background: #2e7d32;
}

.empty-room {
    background: white;
    border-radius: 10px;
    padding: 40px;
    text-align: center;
    color: #777;
    box-shadow: 0 2px 8px rgba(0,0,0,0.12);
}

</style>

</head>

<body>

<div class="container">

    <div class="page-title">

        <h2>사업자 채팅방</h2>

        <button type="button"
                class="create-btn"
                onclick="location.href='${pageContext.request.contextPath}/chat/create'">
            채팅방 생성
        </button>

    </div>

    <div class="room-list">

        <c:choose>

            <c:when test="${empty roomList}">

                <div class="empty-room">
                    생성된 채팅방이 없습니다.
                </div>

            </c:when>

            <c:otherwise>

                <c:forEach var="room" items="${roomList}">

                    <div class="room-card"
                         data-room-id="${room.roomId}">

                        <div class="room-top">

                            <div>

                                <h3 class="room-title">

                                    <c:out value="${room.roomName}" />

                                    <span class="room-type">
                                        <c:out value="${room.roomType}" />
                                    </span>

                                    <c:if test="${room.isDefault eq 'Y'}">
                                        <span class="default-badge">
                                            기본방
                                        </span>
                                    </c:if>

                                </h3>

                            </div>

                            <div class="room-buttons">

                                <button type="button"
                                        class="join-btn"
                                        data-room-id="${room.roomId}">
                                    참가
                                </button>

                                <button type="button"
                                        class="enter-btn"
                                        data-room-id="${room.roomId}">
                                    입장
                                </button>

                            </div>

                        </div>

                        <p class="room-desc">
                            <c:choose>
                                <c:when test="${empty room.roomDescription}">
                                    채팅방 설명이 없습니다.
                                </c:when>
                                <c:otherwise>
                                    <c:out value="${room.roomDescription}" />
                                </c:otherwise>
                            </c:choose>
                        </p>

                        <div class="room-info">

                            <span>
                                생성자 :
                                <c:out value="${room.creatorName}" />
                            </span>

                            <span>
                                참여 인원 :
                                <c:out value="${room.memberCount}" />
                                /
                                <c:out value="${room.maxMember}" />
                            </span>

                            <span>
                                ROOM ID :
                                <c:out value="${room.roomId}" />
                            </span>

                        </div>

                        <div class="last-message-box">

                            <div class="last-message"
                                 id="lastMessage-${room.roomId}">
                                마지막 메시지가 없습니다.
                            </div>

                            <div class="last-time"
                                 id="lastTime-${room.roomId}">
                            </div>

                        </div>

                    </div>

                </c:forEach>

            </c:otherwise>

        </c:choose>

    </div>

</div>

<input type="hidden"
       id="contextPath"
       value="${pageContext.request.contextPath}">

<input type="hidden"
       id="businessNo"
       value="${businessNo}">

<input type="hidden"
       id="businessName"
       value="${businessName}">

<script type="module"
        src="${pageContext.request.contextPath}/js/roomList.js">
</script>

</body>

</html>