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

<div class="auth-container">

    <div class="auth-box">

        <h2>로그인</h2>

        <!-- 로그인 폼 -->
        <form action="${pageContext.request.contextPath}/member/login" method="post">

            <div class="form-group">
                <label>아이디</label>
                <input type="text" name="memberId" required>
            </div>

            <div class="form-group">
                <label>비밀번호</label>
                <input type="password" name="memberPw" required>
            </div>

            <c:if test="${not empty error}">
                <div class="error-text">${error}</div>
            </c:if>

            <c:if test="${not empty errorMessage}">
                <script>
                    alert("${errorMessage}");
                </script>
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

<jsp:include page="/WEB-INF/views/common/footer.jsp" />

</body>
</html>