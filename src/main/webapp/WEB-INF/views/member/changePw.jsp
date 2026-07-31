<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core"%>

<!DOCTYPE html>
<html lang="ko">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>ODITJI - 비밀번호 변경</title>
        <link rel="stylesheet"
        href="${pageContext.request.contextPath}/css/member.css">

    </head>

    <body>
        <jsp:include page="/WEB-INF/views/common/header.jsp" />

        <div id="mainContent" class="auth-container">
            <div class="auth-box large">
                <h2>비밀번호 변경</h2>

                <c:if test="${not empty errorMessage}">
                    <div class="error-text">
                        <c:out value="${errorMessage}"/>
                    </div>
                </c:if>

                <form id="changePwForm" action="${pageContext.request.contextPath}/member/changePw" method="post">

                <div class="form-group">
                    <label for="newPassword">
                        새 비밀번호
                    </label>

                    <!-- 비밀번호 입력창과 토글 버튼을 감싸는 wrapper 추가 -->
                    <div class="password-input-wrap">
                        <input type="password" id="newPassword" name="newPassword" placeholder="영문, 숫자, 특수문자 포함 8~20자"
                            autocomplete="new-password" required>
                        <button type="button" class="password-toggle-btn" data-target="newPassword" aria-label="새 비밀번호 표시">
                            보기
                        </button>
                    </div>
                </div>

                <div class="form-group">
                    <label for="confirmPassword">
                        새 비밀번호 확인
                    </label>
                    <div class="password-input-wrap">
                        <input type="password" id="confirmPassword" name="confirmPassword" placeholder="비밀번호를 한번 더 입력해 주세요"
                            autocomplete="new-password" required>
                        <button type="button" class="password-toggle-btn" data-target="confirmPassword" aria-label="새 비밀번호 확인 표시">
                            보기
                        </button>
                    </div>
                </div>
                <button type="submit" class="btn-primary">
                비밀번호 변경
            </button>
        </form>
    </div>
</div>


<jsp:include page="/WEB-INF/views/common/footer.jsp" />

 <!--수정:기존 member.js의 비밀번호 표시/숨김 기능을 사용한다.-->
<script src="${pageContext.request.contextPath}/js/member.js"></script>

</body>
</html>