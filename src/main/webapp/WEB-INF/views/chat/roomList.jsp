<%@ page language="java"
    contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>

<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!DOCTYPE html>
<html lang="ko">
<head>
<meta charset="UTF-8">
<title>사업자 채팅방</title>
<link rel="stylesheet"
      href="${pageContext.request.contextPath}/css/chat-room-list.css">
</head>
<body>

<div class="container">

    <div class="page-title">
        <div>
            <h2>
                <c:choose>
                    <c:when test="${isAdmin}">관리자 공지 채팅방</c:when>
                    <c:otherwise>사업자 채팅방</c:otherwise>
                </c:choose>
            </h2>

            <p>
                <c:choose>
                    <c:when test="${isAdmin}">
                        공지방을 확인하고 사업자에게 전달할 공지를 작성할 수 있습니다.
                    </c:when>
                    <c:otherwise>
                        공지사항을 확인하고 자유방에서 다른 사업자와 대화할 수 있습니다.
                    </c:otherwise>
                </c:choose>
            </p>
        </div>

        <button type="button"
                class="create-btn"
                id="createRoomBtn">
            <c:choose>
                <c:when test="${isAdmin}">
                    공지방 생성
                </c:when>
                <c:otherwise>
                    자유방 생성
                </c:otherwise>
            </c:choose>
        </button>
    </div>

    <section class="room-section notice-section">

        <div class="section-title-area">
            <h3>공지방</h3>
            <span>관리자 작성 · 사업자 전체 열람</span>
        </div>

        <div class="room-list">

            <c:set var="noticeCount" value="0" />

            <c:forEach var="room" items="${roomList}">
                <c:if test="${room.roomType eq 'NOTICE'}">

                    <c:set var="noticeCount" value="${noticeCount + 1}" />

                    <div class="room-card notice-card"
                         data-room-id="${room.roomId}">

                        <div class="room-top">
                            <div>
                                <h4 class="room-title">
                                    <c:out value="${room.roomName}" />
                                    <span class="room-type notice-badge">공지방</span>
                                </h4>
                            </div>

                            <div class="room-buttons">
                                <button type="button"
                                        class="room-action-btn notice-enter-btn"
                                        data-room-id="${room.roomId}"
                                        data-room-type="NOTICE">
                                    <c:choose>
                                        <c:when test="${isAdmin}">공지 작성</c:when>
                                        <c:otherwise>공지 보기</c:otherwise>
                                    </c:choose>
                                </button>
                            </div>
                        </div>

                        <p class="room-desc">
                            <c:choose>
                                <c:when test="${empty room.roomDescription}">
                                    관리자 공지 전용 채팅방입니다.
                                </c:when>
                                <c:otherwise>
                                    <c:out value="${room.roomDescription}" />
                                </c:otherwise>
                            </c:choose>
                        </p>

                        <div class="room-info">
                            <span>작성자: <c:out value="${room.creatorName}" /></span>
                            <span>ROOM ID: <c:out value="${room.roomId}" /></span>
                        </div>

                        <div class="last-message-box notice-last-message-box">
                            <div class="last-message"
                                 id="lastMessage-${room.roomId}">
                                마지막 공지가 없습니다.
                            </div>
                            <div class="last-time"
                                 id="lastTime-${room.roomId}"></div>
                        </div>

                    </div>

                </c:if>
            </c:forEach>

            <c:if test="${noticeCount eq 0}">
                <div class="empty-room">
                    등록된 공지방이 없습니다.
                </div>
            </c:if>

        </div>

    </section>

    <%-- 관리자는 공지방만 확인하므로 자유방 영역 자체를 출력하지 않습니다. --%>
    <c:if test="${not isAdmin}">

        <section class="room-section public-section">

            <div class="section-title-area">
                <h3>자유방</h3>
                <span>사업자 생성 · 참가자 자유 채팅</span>
            </div>

            <div class="room-list">

                <c:set var="publicCount" value="0" />

                <c:forEach var="room" items="${roomList}">
                    <c:if test="${room.roomType eq 'PUBLIC'}">

                        <c:set var="publicCount" value="${publicCount + 1}" />

                        <div class="room-card public-card"
                             data-room-id="${room.roomId}">

                            <div class="room-top">
                                <div>
                                    <h4 class="room-title">
                                        <c:out value="${room.roomName}" />
                                        <span class="room-type public-badge">자유방</span>
                                    </h4>
                                </div>

                                <div class="room-buttons">
                                    <button type="button"
                                            class="room-action-btn public-enter-btn"
                                            data-room-id="${room.roomId}"
                                            data-room-type="PUBLIC">
                                        참가/입장
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
                                <span>생성자: <c:out value="${room.creatorName}" /></span>
                                <span>
                                    참여 인원:
                                    <c:out value="${room.memberCount}" />
                                    /
                                    <c:out value="${room.maxMember}" />
                                </span>
                                <span>ROOM ID: <c:out value="${room.roomId}" /></span>
                            </div>

                            <div class="last-message-box">
                                <div class="last-message"
                                     id="lastMessage-${room.roomId}">
                                    마지막 메시지가 없습니다.
                                </div>
                                <div class="last-time"
                                     id="lastTime-${room.roomId}"></div>
                            </div>

                        </div>

                    </c:if>
                </c:forEach>

                <c:if test="${publicCount eq 0}">
                    <div class="empty-room">
                        생성된 자유방이 없습니다.
                    </div>
                </c:if>

            </div>

        </section>

    </c:if>

</div>

<input type="hidden"
       id="contextPath"
       value="${pageContext.request.contextPath}">

<script type="module"
        src="${pageContext.request.contextPath}/js/roomList.js"></script>

</body>
</html>
