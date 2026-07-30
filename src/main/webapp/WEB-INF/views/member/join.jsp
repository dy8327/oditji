<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>

<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!DOCTYPE html>
<html lang="ko">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>ODITJI - 회원가입</title>

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

        <h2>회원가입</h2>

        <!-- 회원가입 유형 탭 -->
        <div class="join-tab-menu">

            <button type="button"
                    class="join-tab-btn active"
                    data-type="USER"
                    onclick="switchJoinType('USER')">
                일반 회원가입
            </button>

            <button type="button"
                    class="join-tab-btn"
                    data-type="BUSINESS"
                    onclick="switchJoinType('BUSINESS')">
                사업자 회원가입
            </button>

        </div>

        <form id="joinForm"
              action="${pageContext.request.contextPath}/member/join"
              method="post"
              enctype="multipart/form-data">

            <input type="hidden"
                   name="joinType"
                   id="joinType"
                   value="USER">

            <!-- 공통 기본 정보 -->
            <div class="join-section-title no-border">
                기본 정보
            </div>

            <%--
                SonarQube 접근성 규칙을 충족하도록 각 입력 요소의 id와
                화면에 표시되는 label의 for 속성을 동일한 값으로 연결한다.

                파일 입력은 제목 label과 파일 선택 label을 모두
                같은 input에 연결한다.
            --%>

            <div class="form-group">

                <label for="memberId">
                    아이디
                    <span class="required-mark">*</span>
                </label>

                <div class="row">

                    <input type="text"
                           name="memberId"
                           id="memberId"
                           placeholder="소문자/숫자 5~12자"
                           required>

                    <button type="button"
                            onclick="checkId()">
                        중복확인
                    </button>

                </div>

            </div>

            <div class="form-group">

                <label for="memberPw">
                    비밀번호
                    <span class="required-mark">*</span>
                </label>

                <%--
                    =========================================================
                    비밀번호 표시/숨김 버튼
                    =========================================================
                --%>
                <div class="password-input-wrap">

                    <input type="password"
                           name="memberPw"
                           id="memberPw"
                           placeholder="영문, 숫자, 특수문자 포함 8~20자"
                           required>

                    <button type="button"
                            class="password-toggle-btn"
                            data-target="memberPw"
                            aria-label="비밀번호 표시"
                            aria-pressed="false">
                        보기
                    </button>

                </div>

            </div>

            <div class="form-group">

                <label for="memberPwCheck">
                    비밀번호 확인
                    <span class="required-mark">*</span>
                </label>

                <%--
                    =========================================================
                    비밀번호 확인 표시/숨김 버튼
                    =========================================================
                --%>
                <div class="password-input-wrap">

                    <input type="password"
                           id="memberPwCheck"
                           required>

                    <button type="button"
                            class="password-toggle-btn"
                            data-target="memberPwCheck"
                            aria-label="비밀번호 확인 표시"
                            aria-pressed="false">
                        보기
                    </button>

                </div>

            </div>

            <div class="form-group">

                <label for="email">
                    이메일
                    <span class="required-mark">*</span>
                </label>

                <%--
                    =========================================================
                    이메일 중복확인

                    아이디 및 닉네임 중복확인 영역과 동일한
                    레이아웃과 버튼 스타일을 적용
                    =========================================================
                --%>
                <div class="row">

                    <input type="email"
                           name="email"
                           id="email"
                           placeholder="example@email.com"
                           required>

                    <button type="button"
                            onclick="checkEmail()">
                        중복확인
                    </button>

                </div>

            </div>

            <div class="form-group">

                <label for="phone">
                    전화번호
                    <span class="required-mark">*</span>
                </label>

                <input type="text"
                       name="phone"
                       id="phone"
                       placeholder="010-1234-5678"
                       maxlength="13"
                       required>

            </div>

            <!-- 일반회원 전용 기본 정보 -->
            <div class="user-only-fields active"
                 id="userBasicFields">

                <div class="form-group">

                    <label for="memberName">
                        이름
                        <span class="required-mark">*</span>
                    </label>

                    <input type="text"
                           name="memberName"
                           id="memberName"
                           required>

                </div>

                <div class="form-group">

                    <label for="nickname">
                        닉네임
                        <span class="required-mark">*</span>
                    </label>

                    <div class="row">

                        <input type="text"
                               name="nickname"
                               id="nickname"
                               placeholder="한글/영문/숫자 2~10자"
                               required>

                        <button type="button"
                                onclick="checkNickname()">
                            중복확인
                        </button>

                    </div>

                </div>

            </div>

            <!-- 프로필 이미지 -->
            <div class="form-group">

                <%--
                    프로필 이미지 제목 label과 실제 파일 선택 버튼 역할의
                    label을 모두 profileImageFile 입력창에 연결한다.
                --%>
                <label for="profileImageFile">
                    프로필 이미지
                    <span class="optional-mark">(선택)</span>
                </label>

                <div class="file-box">

                    <input type="file"
                           name="profileImageFile"
                           id="profileImageFile"
                           accept="image/*">

                    <label for="profileImageFile"
                           class="file-label">
                        파일 선택
                    </label>

                    <span class="file-name">
                        선택된 파일 없음
                    </span>

                </div>

            </div>

            <!-- 일반회원 전용: OTT 선택 -->
            <div class="user-only-fields active"
                 id="userOnlyFields">

                <div class="join-section-title">
                    사용 중인 OTT
                    <span class="required-mark">*</span>
                </div>

                <div class="form-group">

                    <div class="ott-box">

                        <c:forEach var="platform" items="${platformList}">
                            <label>
                                <input type="checkbox" name="ottList" value="${platform.platformName}">

                                <img class="ott-box-logo"
                                     src="${platform.logoImage}"
                                     alt="${platform.platformName}">

                                <span>
                                    <c:choose>
                                        <c:when test="${platform.platformName eq 'Netflix'}">넷플릭스</c:when>
                                        <c:when test="${platform.platformName eq 'Disney Plus'}">디즈니+</c:when>
                                        <c:when test="${platform.platformName eq 'Disney+'}">디즈니+</c:when>
                                        <c:when test="${platform.platformName eq 'Tving'}">티빙</c:when>
                                        <c:when test="${platform.platformName eq 'TVING'}">티빙</c:when>
                                        <c:when test="${platform.platformName eq 'Wavve'}">웨이브</c:when>
                                        <c:when test="${platform.platformName eq 'Watcha'}">왓챠</c:when>
                                        <c:when test="${platform.platformName eq 'Coupangplay'}">쿠팡플레이</c:when>
                                        <c:when test="${platform.platformName eq 'Coupang Play'}">쿠팡플레이</c:when>
                                        <c:otherwise>${platform.platformName}</c:otherwise>
                                    </c:choose>
                                </span>
                            </label>
                        </c:forEach>

                        <%--
                            =========================================================
                            일반회원 OTT 미사용 선택

                            OTT 없음 선택 시 MEMBER_PLATFORM에는
                            별도 데이터를 저장하지 않는다.
                            =========================================================
                        --%>
                        <label>
                            <input type="checkbox"
                                   name="noOtt"
                                   id="noOtt"
                                   value="Y">
                            OTT 없음
                        </label>

                    </div>

                </div>

            </div>

            <!-- 사업자 전용: 사업자 및 정산 정보 -->
            <div class="business-only-fields"
                 id="businessOnlyFields">

                <div class="join-section-title">
                    사업자 정보
                </div>

                <div class="form-group">

                    <label for="businessName">
                        상호명
                    </label>

                    <input type="text"
                           name="businessName"
                           id="businessName"
                           placeholder="사업체명을 입력하세요">

                </div>

                <div class="form-group">

                    <label for="representativeName">
                        대표자명
                    </label>

                    <input type="text"
                           name="representativeName"
                           id="representativeName"
                           placeholder="사업자등록증의 대표자명을 입력하세요">

                </div>

                <div class="form-group">

                    <label for="openDate">
                        개업일
                    </label>

                    <input type="text"
                           name="openDate"
                           id="openDate"
                           placeholder="YYYYMMDD (예: 20200101)"
                           maxlength="8"
                           inputmode="numeric">

                </div>

                <div class="form-group">
                    <label for="businessNumber">사업자등록번호</label>

                    <div class="row business-number-row">
                        <input type="text"
                            name="businessNumber"
                            id="businessNumber"
                            placeholder="000-00-00000"
                            maxlength="12">

                        <button type="button"
                                onclick="checkBusinessNumber()">
                            중복확인
                        </button>

                        <button type="button"
                                id="verifyBusinessBtn"
                                onclick="verifyBusiness()">
                            사업자 정보 인증
                        </button>
                    </div>

                    <div id="businessVerifyMessage"
                        style="margin-top: 8px;">
                    </div>
                </div>

                <div class="form-group">
                    <%--
                        사업자등록증 제목과 파일 선택 버튼을
                        모두 licenseFile 입력창에 연결한다.
                    --%>
                    <label for="licenseFile">
                        사업자등록증
                    </label>

                    <div class="file-box">

                        <input type="file"
                               name="licenseFile"
                               id="licenseFile"
                               accept=".pdf,.jpg,.jpeg,.png">

                        <label for="licenseFile"
                               class="file-label">
                            파일 선택
                        </label>

                        <span class="file-name"
                              id="licenseFileName">
                            선택된 파일 없음
                        </span>

                    </div>

                    <div class="form-hint">
                        PDF, JPG, JPEG, PNG 파일을 등록해주세요.
                    </div>

                </div>

                <div class="join-section-title">
                    정산 계좌 정보
                </div>

                <div class="form-group">

                    <label for="bankName">
                        은행명
                    </label>

                    <input type="text"
                           name="bankName"
                           id="bankName"
                           placeholder="예: 국민은행">

                </div>

                <div class="form-group">

                    <label for="accountNumber">
                        계좌번호
                    </label>

                    <input type="text"
                           name="accountNumber"
                           id="accountNumber"
                           placeholder="- 없이 숫자만 입력">

                </div>

                <div class="form-group">

                    <label for="accountHolder">
                        예금주
                    </label>

                    <input type="text"
                           name="accountHolder"
                           id="accountHolder"
                           placeholder="예금주명을 입력하세요">

                </div>

                <div class="form-hint">
                    사업자 회원가입은 관리자 승인 후 사업자 페이지 이용이 가능합니다.
                </div>

            </div>

            <!-- 에러 메시지 -->
            <c:if test="${not empty errorMessage}">

                <div class="error-text">
                    ${errorMessage}
                </div>

            </c:if>

            <button type="submit"
                    class="btn-primary">
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