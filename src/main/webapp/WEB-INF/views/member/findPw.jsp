<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core"%>

<!DOCTYPE html>
<html lang="ko">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>ODITJI - 비밀번호 찾기</title>
<link rel="stylesheet" href="${pageContext.request.contextPath}/css/member.css">
<script defer src="${pageContext.request.contextPath}/js/member.js"></script>
</head>

<body data-context-path="${pageContext.request.contextPath}">
<jsp:include page="/WEB-INF/views/common/header.jsp"/>

<div class="auth-container">
    <div class="auth-box large">
        <h2>비밀번호 찾기</h2>

        <c:choose>
            <%-- 회원정보 입력 단계 --%>
            <c:when test="${not verificationStep}">
                <form action="${pageContext.request.contextPath}/member/findPw" method="post">
                    <div class="form-group">
                        <label for="findPwMemberId">아이디</label>
                        <input type="text" id="findPwMemberId" name="memberId" required>
                    </div>

                    <div class="form-group">
                        <label for="findPwMemberName">이름</label>
                        <input type="text" id="findPwMemberName" name="memberName" required>
                    </div>

                    <div class="form-group">
                        <label for="findPwEmail">이메일</label>
                        <input type="email" id="findPwEmail" name="email"
                               placeholder="example@email.com" required>
                    </div>

                    <button type="submit" class="btn-primary">인증번호 받기</button>
                </form>
            </c:when>

            <%-- 이메일 인증번호 입력 단계 --%>
            <c:otherwise>
                <p>
                    <strong><c:out value="${maskedEmail}"/></strong>로<br>
                    인증번호를 발송했습니다.
                </p>

                <form action="${pageContext.request.contextPath}/member/verifyPwCode"
                      method="post">
                    <div class="form-group">
                        <label for="authCode">인증번호</label>
                        <input type="text" id="authCode" name="authCode"
                               placeholder="6자리 인증번호"
                               inputmode="numeric"
                               autocomplete="one-time-code"
                               maxlength="6"
                               pattern="[0-9]{6}"
                               oninput="this.value=this.value.replace(/[^0-9]/g,'')"
                               required>
                    </div>

                    <button type="submit" class="btn-primary">인증번호 확인</button>
                </form>

                <div class="auth-links">
                    <a href="${pageContext.request.contextPath}/member/findPw">
                        회원정보 다시 입력
                    </a>
                </div>
            </c:otherwise>
        </c:choose>

        <c:if test="${not empty message}">
            <p><c:out value="${message}"/></p>
        </c:if>

        <c:if test="${not empty errorMessage}">
            <div class="error-text">
                <c:out value="${errorMessage}"/>
            </div>
        </c:if>

        <div class="auth-links">
            <a href="${pageContext.request.contextPath}/member/login">로그인</a>
            <span>|</span>
            <a href="${pageContext.request.contextPath}/member/findId">아이디 찾기</a>
        </div>
    </div>
</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp"/>
</body>
</html>