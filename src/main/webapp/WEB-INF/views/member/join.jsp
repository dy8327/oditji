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

            <input type="hidden" name="joinType" id="joinType" value="USER">

            <!-- 공통 기본 정보 -->
            <div class="join-section-title no-border">기본 정보</div>

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

            <!-- 일반회원 전용: OTT 선택 -->
            <div class="user-only-fields active" id="userOnlyFields">
                <div class="join-section-title">사용 중인 OTT</div>

                <div class="form-group">
                    <div class="ott-box">
                        <label><input type="checkbox" name="ottList" value="Netflix"> 넷플릭스</label>
                        <label><input type="checkbox" name="ottList" value="Disney Plus"> 디즈니+</label>
                        <label><input type="checkbox" name="ottList" value="Tving"> 티빙</label>
                        <label><input type="checkbox" name="ottList" value="Wavve"> 웨이브</label>
                        <label><input type="checkbox" name="ottList" value="Watcha"> 왓챠</label>
                        <label><input type="checkbox" name="ottList" value="Coupangplay"> 쿠팡플레이</label>
                    </div>
                </div>
            </div>

            <!-- 사업자 전용: 사업자/정산 정보 -->
            <div class="business-only-fields" id="businessOnlyFields">
                <div class="join-section-title">사업자 정보</div>

                <div class="form-group">
                    <label>상호명</label>
                    <input type="text"
                           name="businessName"
                           id="businessName"
                           placeholder="사업체명을 입력하세요">
                </div>

                <div class="form-group">
                    <label>사업자등록번호</label>
                    <div class="row">
                        <input type="text"
                               name="businessNumber"
                               id="businessNumber"
                               placeholder="000-00-00000"
                               maxlength="12">
                        <button type="button" onclick="checkBusinessNumber()">중복확인</button>
                    </div>
                </div>

                <div class="join-section-title">정산 계좌 정보</div>

                <div class="form-group">
                    <label>은행명</label>
                    <input type="text"
                           name="bankName"
                           id="bankName"
                           placeholder="예: 국민은행">
                </div>

                <div class="form-group">
                    <label>계좌번호</label>
                    <input type="text"
                           name="accountNumber"
                           id="accountNumber"
                           placeholder="- 없이 숫자만 입력">
                </div>

                <div class="form-group">
                    <label>예금주</label>
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

<script>
    // 회원가입 유형(일반/사업자) 탭 전환
    function switchJoinType(type) {
        document.getElementById('joinType').value = type;

        document.querySelectorAll('.join-tab-btn').forEach(function (btn) {
            btn.classList.toggle('active', btn.dataset.type === type);
        });

        var userFields = document.getElementById('userOnlyFields');
        var businessFields = document.getElementById('businessOnlyFields');
        var businessName = document.getElementById('businessName');
        var businessNumber = document.getElementById('businessNumber');
        var bankName = document.getElementById('bankName');
        var accountNumber = document.getElementById('accountNumber');
        var accountHolder = document.getElementById('accountHolder');

        if (type === 'BUSINESS') {
            userFields.classList.remove('active');
            businessFields.classList.add('active');

            // 숨겨진 일반회원 전용 필드는 제출 시 막히지 않도록 required 해제할 것 없음(체크박스라 required 아님)

            // 사업자 전용 필드는 필수값으로 전환
            businessName.required = true;
            businessNumber.required = true;
            bankName.required = true;
            accountNumber.required = true;
            accountHolder.required = true;
        } else {
            businessFields.classList.remove('active');
            userFields.classList.add('active');

            // 일반회원 전환 시 사업자 필드 required 해제 (숨겨진 상태라 제출 막힘 방지)
            businessName.required = false;
            businessNumber.required = false;
            bankName.required = false;
            accountNumber.required = false;
            accountHolder.required = false;
        }
    }

    // 유효성 검사 실패로 폼이 다시 렌더링된 경우, 이전에 선택했던 탭을 복원
    (function restoreJoinType() {
        var previousJoinType = "${joinType}";

        if (previousJoinType === 'BUSINESS') {
            switchJoinType('BUSINESS');
        }
    })();
</script>

</body>
</html>
