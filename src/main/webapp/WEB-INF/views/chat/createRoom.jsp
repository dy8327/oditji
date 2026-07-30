<%@ page language="java"
    contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>

<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!DOCTYPE html>
<html lang="ko">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>채팅방 생성</title>
<link rel="stylesheet"
      href="${pageContext.request.contextPath}/css/chat-create-room.css?v=2">
</head>
<body class="chat-create-page">

<jsp:include page="/WEB-INF/views/common/header.jsp"/>

<div class="create-room-container">

    <div class="create-room-header">
        <h2>
            <c:choose>
                <c:when test="${isAdmin}">공지방 생성</c:when>
                <c:otherwise>자유방 생성</c:otherwise>
            </c:choose>
        </h2>

        <p>
            <c:choose>
                <c:when test="${isAdmin}">
                    관리자는 사업자 전체가 열람할 공지방만 생성할 수 있습니다.
                </c:when>
                <c:otherwise>
                    사업자는 다른 사업자와 대화할 자유방만 생성할 수 있습니다.
                </c:otherwise>
            </c:choose>
        </p>
    </div>

    <form action="${pageContext.request.contextPath}/chat/create"
          method="post"
          class="create-room-form"
          id="createRoomForm">

        <div class="form-group">
            <label for="roomName">채팅방 이름</label>
            <input type="text"
                   id="roomName"
                   name="roomName"
                   maxlength="100"
                   required>
        </div>

        <div class="form-group">
            <label for="roomDescription">방 설명</label>
            <textarea id="roomDescription"
                      name="roomDescription"
                      maxlength="500"
                      placeholder="채팅방의 목적을 입력하세요."></textarea>
        </div>

        <div class="form-group">
            <label for="roomType">채팅방 종류</label>

            <select id="roomType"
                    name="roomType">

                <c:choose>
                    <c:when test="${isAdmin}">
                        <option value="NOTICE">
                            공지방
                        </option>
                    </c:when>
                    <c:otherwise>
                        <option value="PUBLIC">
                            자유방
                        </option>
                    </c:otherwise>
                </c:choose>

            </select>

            <p id="roomTypeHelp"
               class="form-help">
                <c:choose>
                    <c:when test="${isAdmin}">
                        공지방은 관리자만 메시지를 작성하고 모든 사업자가 열람합니다.
                    </c:when>
                    <c:otherwise>
                        자유방은 사업자들이 참가하여 자유롭게 대화하는 공간입니다.
                    </c:otherwise>
                </c:choose>
            </p>
        </div>

        <div class="form-group"
             id="maxMemberGroup">
            <label for="maxMember">최대 참여 인원</label>
            <input type="number"
                   id="maxMember"
                   name="maxMember"
                   value="100"
                   min="2"
                   max="9999">
        </div>

        <div class="button-area">
            <button type="submit"
                    class="submit-btn">
                생성
            </button>

            <button type="button"
                    class="cancel-btn"
                    id="cancelBtn">
                취소
            </button>
        </div>

    </form>

</div>

<script type="module"
        src="${pageContext.request.contextPath}/js/createRoom.js"></script>

</body>
</html>
