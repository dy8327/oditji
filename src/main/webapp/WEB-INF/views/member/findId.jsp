<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>

<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!DOCTYPE html>
<html lang="ko">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>ODITJI - 아이디 찾기</title>

<link rel="stylesheet"
      href="${pageContext.request.contextPath}/css/member.css">

<script defer
        src="${pageContext.request.contextPath}/js/member.js">
</script>

</head>

<body data-context-path="${pageContext.request.contextPath}">

<jsp:include page="/WEB-INF/views/common/header.jsp" />

<div id="mainContent" class="auth-container">

    <div class="auth-box large">

        <h2>아이디 찾기</h2>

        <form action="${pageContext.request.contextPath}/member/findId"
              method="post">

            <div class="form-group">

                <%--
                    이름 입력창의 id와 label의 for를 연결하여
                    보조 기술이 입력 목적을 정확히 인식하도록 한다.
                --%>
                <label for="findIdMemberName">
                    이름
                </label>

                <input type="text"
                       id="findIdMemberName"
                       name="memberName"
                       required>

            </div>

            <div class="form-group">

                <%--
                    이메일 입력창에도 고유 id를 부여하고
                    label의 for 속성과 연결한다.
                --%>
                <label for="findIdEmail">
                    이메일
                </label>

                <input type="email"
                       id="findIdEmail"
                       name="email"
                       placeholder="example@email.com"
                       required>

            </div>

            <button type="submit"
                    class="btn-primary">
                아이디 찾기
            </button>

        </form>

        <c:if test="${not empty findIdResult}">

            <%-- [수정] 아이디 조회 결과를 비밀번호 찾기 안내 영역과 동일한 디자인 계열로 구성 --%>
            <div class="find-id-result" role="status" aria-live="polite">

                <div class="find-id-result-icon" aria-hidden="true">✓</div>

                <div class="find-id-result-content">

                    <span class="find-id-result-title">조회 결과</span>

                    <p class="find-id-result-description">
                        회원님의 아이디를 찾았습니다.
                    </p>

                    <%-- [수정] 조회된 아이디를 별도 영역으로 강조하고 EL 출력 시 이스케이프 처리 --%>
                    <strong class="find-id-result-id">
                        <c:out value="${findIdResult}"/>
                    </strong>

                    <span class="find-id-result-help">
                        로그인 후 ODITJI 서비스를 이용해 보세요.
                    </span>

                </div>

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

            <a href="${pageContext.request.contextPath}/member/findPw">
                비밀번호 찾기
            </a>

        </div>

    </div>

</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />

</body>
</html>