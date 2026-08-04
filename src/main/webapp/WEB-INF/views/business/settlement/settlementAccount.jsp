<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core"%>
<c:set var="activeMenu" value="settlement" />
<!DOCTYPE html>
<html lang="ko">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>ODITJI | 계좌 정보 관리</title>
<link rel="stylesheet" href="${pageContext.request.contextPath}/css/business.css">
</head>
<body>
<jsp:include page="/WEB-INF/views/common/header.jsp" />
<div class="business-wrap">
    <jsp:include page="/WEB-INF/views/common/businessSidebar.jsp" />

    <main id="mainContent" class="main-content">
        <a href="${pageContext.request.contextPath}/business/main" class="back-link">← 뒤로가기</a>
        <h1 class="page-title">수수료 관리</h1>

        <section class="content-panel settlement-page-panel">
            <%-- [수정] 기존 수수료 관리 화면의 탭 구조와 전체 레이아웃을 그대로 유지한다. --%>
            <nav class="tab-menu">
                <a href="${pageContext.request.contextPath}/business/settlement/main">이번달 수수료</a>
                <a href="${pageContext.request.contextPath}/business/settlement/complete">납부 내역</a>
                <a class="active" href="${pageContext.request.contextPath}/business/settlement/account">계좌 정보 관리</a>
            </nav>

            <c:if test="${not empty successMessage}">
                <p class="alert alert-success">${successMessage}</p>
            </c:if>
            <c:if test="${not empty errorMessage}">
                <p class="alert alert-danger">${errorMessage}</p>
            </c:if>

            <%-- [수정] 입력 폼을 안내 영역과 분리하고, 기존 색상과 카드 레이아웃 안에서 정돈한다. --%>
            <div class="settlement-account-layout">
                <div class="settlement-section-card settlement-account-card">
                    <div class="settlement-section-head">
                        <div>
                            <p class="settlement-section-kicker">SETTLEMENT ACCOUNT</p>
                            <h3>판매 대금 정산 계좌</h3>
                            <p class="settlement-section-description">
                                관리자가 정산 요청을 검토할 때 표시되는 사업자 계좌 정보입니다.
                            </p>
                        </div>
                    </div>

                    <form action="${pageContext.request.contextPath}/business/settlement/account"
                          method="post"
                          class="settlement-account-form">
                        <%-- [수정] BUSINESS 테이블의 계좌 정보를 로그인 사업자 기준으로 수정한다. --%>
                        <div class="settlement-form-field">
                            <label for="bankName">은행명 <span class="required-mark">*</span></label>
                            <input type="text"
                                   id="bankName"
                                   name="bankName"
                                   value="${settlementAccount.bankName}"
                                   maxlength="50"
                                   placeholder="예: 국민은행"
                                   required>
                        </div>

                        <div class="settlement-form-field">
                            <label for="accountNumber">계좌번호 <span class="required-mark">*</span></label>
                            <input type="text"
                                   id="accountNumber"
                                   name="accountNumber"
                                   value="${settlementAccount.accountNumber}"
                                   maxlength="50"
                                   inputmode="numeric"
                                   placeholder="숫자와 하이픈(-)만 입력"
                                   pattern="[0-9-]+"
                                   required>
                            <span class="settlement-field-hint">공백 없이 숫자 또는 하이픈(-)으로 입력해주세요.</span>
                        </div>

                        <div class="settlement-form-field">
                            <label for="accountHolder">예금주 <span class="required-mark">*</span></label>
                            <input type="text"
                                   id="accountHolder"
                                   name="accountHolder"
                                   value="${settlementAccount.accountHolder}"
                                   maxlength="50"
                                   placeholder="사업자 또는 대표자명"
                                   required>
                        </div>

                        <div class="settlement-account-actions">
                            <p>저장한 계좌 정보는 다음 정산 요청부터 관리자 화면에 표시됩니다.</p>
                            <button type="submit" class="btn btn-primary settlement-save-button">계좌 정보 저장</button>
                        </div>
                    </form>
                </div>

                <%-- [수정] 계좌 입력 시 주의사항을 별도 안내 카드로 구성한다. --%>
                <aside class="settlement-account-guide">
                    <h4>계좌 등록 안내</h4>
                    <ul>
                        <li>사업자 본인 또는 대표자 명의 계좌를 입력해주세요.</li>
                        <li>입력한 예금주와 실제 계좌 예금주가 일치해야 합니다.</li>
                        <li>계좌 변경 후 진행 중인 요청이 있다면 관리자 확인이 필요할 수 있습니다.</li>
                    </ul>
                    <div class="settlement-account-preview">
                        <span>현재 등록 계좌</span>
                        <strong>${empty settlementAccount.bankName ? '미등록' : settlementAccount.bankName}</strong>
                        <p>${empty settlementAccount.accountNumber ? '계좌번호를 등록해주세요.' : settlementAccount.accountNumber}</p>
                    </div>
                </aside>
            </div>
        </section>
    </main>
</div>
<jsp:include page="/WEB-INF/views/common/footer.jsp" />
</body>
</html>