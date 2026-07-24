<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>

<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!DOCTYPE html>
<html lang="ko">
<head>
<meta charset="UTF-8">
<title>ODITJI - 비밀번호 찾기</title>

<link rel="stylesheet"
      href="${pageContext.request.contextPath}/css/member.css">

<script defer
        src="${pageContext.request.contextPath}/js/member.js">
</script>

</head>

<body data-context-path="${pageContext.request.contextPath}">

<jsp:include page="/WEB-INF/views/common/header.jsp" />

<div class="auth-container">

    <div class="auth-box large">

        <h2>비밀번호 찾기</h2>

        <form action="${pageContext.request.contextPath}/member/findPw"
              method="post">

            <div class="form-group">

                <%--
                    아이디 입력창의 id와 label의 for를 연결한다.
                --%>
                <label for="findPwMemberId">
                    아이디
                </label>

                <input type="text"
                       id="findPwMemberId"
                       name="memberId"
                       required>

            </div>

            <div class="form-group">

                <%--
                    이름 입력창의 id와 label의 for를 연결한다.
                --%>
                <label for="findPwMemberName">
                    이름
                </label>

                <input type="text"
                       id="findPwMemberName"
                       name="memberName"
                       required>

            </div>

            <div class="form-group">

                <%--
                    이메일 입력창의 id와 label의 for를 연결한다.
                --%>
                <label for="findPwEmail">
                    이메일
                </label>

                <input type="email"
                       id="findPwEmail"
                       name="email"
                       placeholder="example@email.com"
                       required>

            </div>

            <button type="submit"
                    class="btn-primary">
                비밀번호 찾기
            </button>

        </form>

        <c:if test="${not empty findPwResult}">

            <div class="result-box">

                <h3>조회 결과</h3>

                <p>
                    회원님의 비밀번호는
                    <strong>${findPwResult}</strong>
                    입니다.
                </p>

            </div>

        </c:if>

        <c:if test="${not empty errorMessage}">

            <div class="error-text">
                ${errorMessage}
            </div>

        </c:if>

        <div class="auth-links">

            <a href="${pageContext.request.contextPath}/member/login">
                로그인
            </a>

            <span>|</span>

            <a href="${pageContext.request.contextPath}/member/findId">
                아이디 찾기
            </a>

        </div>

    </div>

</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />

</body>
</html>