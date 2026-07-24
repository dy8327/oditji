<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>

<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>ODITJI - 로그인</title>

<link rel="stylesheet"
      href="${pageContext.request.contextPath}/css/member.css">

</head>

<body>

<jsp:include page="/WEB-INF/views/common/header.jsp" />

<div class="auth-container"
     data-message="<c:out value="${message}"/>"
     data-error-message="<c:out value="${errorMessage}"/>"
     data-restored-message="<c:out value="${restoredMessage}"/>"
     data-blocked-message="<c:out value="${blockedMessage}"/>"
     data-withdrawn-message="<c:out value="${withdrawnMessage}"/>">

    <div class="auth-box">

        <h2>로그인</h2>

        <!-- 로그인 폼 -->
        <form action="${pageContext.request.contextPath}/member/login" method="post">

            <div class="form-group">
                <label>아이디</label>
                <input type="text" name="memberId" value="<c:out value='${loginMemberId}'/>" autocomplete="username" required>
            </div>

            <div class="form-group">
                <label>비밀번호</label>
                <input type="password" name="memberPw" required>
            </div>

            <c:if test="${not empty error}">
                <div class="error-text">${error}</div>
            </c:if>

            <button type="submit" class="btn-primary">로그인</button>

        </form>

        <!-- SNS 로그인 (하나만 유지) -->
        <div class="sns-login-box">

            <div class="sns-title">SNS 로그인</div>

            <div class="sns-icon-row">

                <a href="${pageContext.request.contextPath}/member/kakao/login" class="sns-icon kakao"></a>
                <a href="${pageContext.request.contextPath}/member/naver/login" class="sns-icon naver"></a>
                <a href="${pageContext.request.contextPath}/member/google/login" class="sns-icon google"></a>

            </div>

        </div>

        <!-- 아이디 / 비밀번호 찾기 -->
        <div class="auth-links">

            <a href="${pageContext.request.contextPath}/member/findId">
                아이디 찾기
            </a>

            <span>|</span>

            <a href="${pageContext.request.contextPath}/member/findPw">
                비밀번호 찾기
            </a>

        </div>

        <!-- CTA -->
        <div class="auth-cta">

            <div class="cta-item">
                <span>아직 회원이 아니신가요?</span>
            </div>

            <a href="${pageContext.request.contextPath}/member/join" class="cta-btn">회원가입</a>

        </div>

    </div>

</div>

<!-- 탈퇴 회원 복구 모달 -->
<div class="oditji-modal-overlay" id="restoreModal">
    <div class="oditji-modal-box">

        <h3>계정 복구</h3>

        <p>
            탈퇴한 계정입니다.<br>
            아래 입력창에 <strong>복구</strong>라고 입력하시면<br>
            계정을 다시 활성화할 수 있습니다.
        </p>

        <form action="${pageContext.request.contextPath}/member/restore" method="post">

            <input type="text" id="restoreConfirmInput" placeholder="복구" autocomplete="off">

            <div class="oditji-modal-btn-row">
                <button type="button" class="oditji-btn-cancel" onclick="closeRestoreModal()">취소</button>
                <button type="submit" class="oditji-btn-restore" id="restoreSubmitBtn" disabled>복구하기</button>
            </div>

        </form>

    </div>
</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />

<script src="${pageContext.request.contextPath}/js/member.js"></script>

</body>
</html>
