<%@ page language="java"
    contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>

<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!DOCTYPE html>
<html lang="ko">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>
    <c:choose>
        <c:when test="${isAdmin}">관리자 공지 채팅방</c:when>
        <c:otherwise>사업자 채팅방</c:otherwise>
    </c:choose>
</title>
<link rel="stylesheet"
      href="${pageContext.request.contextPath}/css/chat-common.css?v=1">
<link rel="stylesheet"
      href="${pageContext.request.contextPath}/css/chat-room-list.css?v=3">
</head>
<body class="chat-dashboard-page chat-room-list-page">

<jsp:include page="/WEB-INF/views/common/header.jsp"/>

<%--
    3단 대시보드 셸.
    데스크톱(>1024px): Left(방 목록) | Middle(채팅방) | Right(방 생성) splitview.
    모바일(<=768px): panel-middle / panel-right 는 CSS에서 숨겨지고,
    방 목록 자체가 카카오톡 앱 홈 화면처럼 전체 화면을 채운다.
--%>
<div id="mainContent" class="chat-dashboard">

    <section class="dashboard-panel panel-left">
        <div class="room-list-root">

            <div class="room-list-header">
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
                <button type="button" class="create-btn" id="createRoomBtn">
                    <c:choose>
                        <c:when test="${isAdmin}">공지방 생성</c:when>
                        <c:otherwise>자유방 생성</c:otherwise>
                    </c:choose>
                </button>
            </div>

            <div class="room-list-scroll">

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

                <%-- 관리자는 공지방만 확인하므로 자유방 영역 자체를 출력하지 않는다. --%>
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
                                                <c:out value="${room.memberCount}" />/<c:out value="${room.maxMember}" />명
                                            </span>
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
        </div>
    </section>

    <section class="dashboard-panel panel-middle" id="middlePanel">
        <div class="panel-placeholder" id="roomPlaceholder">
            <div class="placeholder-icon">💬</div>
            <p>왼쪽 목록에서 채팅방을 선택하세요.</p>
        </div>
        <iframe class="panel-frame"
                id="roomFrame"
                title="채팅방"
                style="display:none;"></iframe>
    </section>

    <section class="dashboard-panel panel-right" id="rightPanel">
        <iframe class="panel-frame"
                id="createFrameDesktop"
                title="채팅방 생성"
                data-src="${pageContext.request.contextPath}/chat/create?embed=1"></iframe>
    </section>

</div>

<%-- 모바일 전용: FAB(+) 로 방 생성 모달을 연다. --%>
<button type="button" class="mobile-fab" id="mobileCreateBtn" aria-label="채팅방 생성">+</button>

<div class="mobile-modal-overlay" id="mobileCreateModal">
    <div class="mobile-modal-sheet">
        <div class="mobile-modal-head">
            <span>
                <c:choose>
                    <c:when test="${isAdmin}">공지방 생성</c:when>
                    <c:otherwise>자유방 생성</c:otherwise>
                </c:choose>
            </span>
            <button type="button" class="mobile-modal-close" id="mobileCreateClose" aria-label="닫기">×</button>
        </div>
        <iframe class="panel-frame"
                id="createFrameMobile"
                title="채팅방 생성"
                data-src="${pageContext.request.contextPath}/chat/create?embed=1"></iframe>
    </div>
</div>

<input type="hidden"
       id="contextPath"
       value="${pageContext.request.contextPath}">

<script type="module"
        src="${pageContext.request.contextPath}/js/roomList.js?v=3"></script>

</body>
</html>
