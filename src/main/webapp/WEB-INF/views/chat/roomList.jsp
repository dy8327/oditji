<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>

<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!DOCTYPE html>

<html>

<head>

<meta charset="UTF-8">

<title>채팅방 목록</title>

<link rel="stylesheet" href="${pageContext.request.contextPath}/css/chat-room-list.css">
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