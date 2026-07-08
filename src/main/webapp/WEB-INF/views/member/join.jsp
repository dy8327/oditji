<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>

<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>ODITJI - 회원가입</title>

<link rel="stylesheet"
      href="${pageContext.request.contextPath}/css/member.css">

<script defer src="${pageContext.request.contextPath}/js/member.js"></script>
</head>

<body data-context-path="${pageContext.request.contextPath}">

<jsp:include page="/WEB-INF/views/common/header.jsp" />

<div class="auth-container">
    <div class="auth-box large">

        <h2>회원가입</h2>

        <form id="joinForm"
              action="${pageContext.request.contextPath}/member/join"
              method="post"
              enctype="multipart/form-data">

            <!-- 기본 정보 -->
            <div class="form-group">
                <label>이름</label>
                <input type="text"
                       name="memberName"
                       id="memberName"
                       required>
            </div>

            <div class="form-group">
                <label>아이디</label>
                <div class="row">
                    <input type="text"
                           name="memberId"
                           id="memberId"
                           placeholder="소문자/숫자 5~12자"
                           required>
                    <button type="button" onclick="checkId()">중복확인</button>
                </div>
            </div>

            <div class="form-group">
                <label>비밀번호</label>
                <input type="password"
                       name="memberPw"
                       id="memberPw"
                       placeholder="영문, 숫자, 특수문자 포함 8~20자"
                       required>
            </div>

            <div class="form-group">
                <label>비밀번호 확인</label>
                <input type="password"
                       id="memberPwCheck"
                       required>
            </div>

            <!-- 추가 정보 -->
            <div class="form-group">
                <label>닉네임</label>
                <div class="row">
                    <input type="text"
                           name="nickname"
                           id="nickname"
                           placeholder="한글/영문/숫자 2~10자"
                           required>
                    <button type="button" onclick="checkNickname()">중복확인</button>
                </div>
            </div>

            <div class="form-group">
                <label>이메일</label>
                <input type="email"
                       name="email"
                       id="email"
                       placeholder="example@email.com"
                       required>
            </div>

            <div class="form-group">
                <label>전화번호</label>
                <input type="text"
                       name="phone"
                       id="phone"
                       placeholder="010-1234-5678"
                       maxlength="13">
            </div>

            <!-- 프로필 이미지 -->
            <div class="form-group">
                <label>프로필 이미지</label>

                <div class="file-box">
                    <input type="file"
                           name="profileImageFile"
                           id="profileImageFile"
                           accept="image/*">

                    <label for="profileImageFile" class="file-label">
                        파일 선택
                    </label>

                    <span class="file-name">선택된 파일 없음</span>
                </div>
            </div>

            <!-- OTT 선택 -->
            <div class="form-group">
                <label>사용 중인 OTT</label>

                <div class="ott-box">
                    <label><input type="checkbox" name="ottList" value="Netflix"> 넷플릭스</label>
                    <label><input type="checkbox" name="ottList" value="Disney Plus"> 디즈니+</label>
                    <label><input type="checkbox" name="ottList" value="Tving"> 티빙</label>
                    <label><input type="checkbox" name="ottList" value="Wavve"> 웨이브</label>
                    <label><input type="checkbox" name="ottList" value="Watcha"> 왓챠</label>
                    <label><input type="checkbox" name="ottList" value="Coupangplay"> 쿠팡플레이</label>
                </div>
            </div>

            <!-- 에러 메시지 -->
            <c:if test="${not empty errorMessage}">
                <div class="error-text">
                    ${errorMessage}
                </div>
            </c:if>

            <button type="submit" class="btn-primary">
                회원가입
            </button>

        </form>

        <!-- 로그인 링크 -->
        <div class="auth-cta">
            <div class="cta-item">
                <span>이미 회원이신가요?</span>
            </div>

            <a href="${pageContext.request.contextPath}/member/login"
               class="cta-btn">
                로그인
            </a>
        </div>

    </div>
</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />

</body>
</html>