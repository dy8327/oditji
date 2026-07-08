<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core"%>

<c:set var="activeMenu" value="settlement" />

<!DOCTYPE html>
<html lang="ko">

<head>

<meta charset="UTF-8">

<title>ODITJI | 수수료 관리</title>

<link rel="stylesheet"
    href="${pageContext.request.contextPath}/css/business.css">

</head>

<body>

    <jsp:include page="/WEB-INF/views/common/header.jsp" />

    <div class="business-wrap">

        <jsp:include page="/WEB-INF/views/common/businessSidebar.jsp" />

        <main class="main-content">

            <a href="${pageContext.request.contextPath}/business/main"
                class="back-link">
                ← 뒤로가기
            </a>

            <h1 class="page-title">수수료 관리</h1>

            <section class="content-panel">

                <!-- 탭 -->
                <nav class="tab-menu">

                    <a class="active"
                        href="${pageContext.request.contextPath}/business/settlement/main">
                        이번달 수수료
                    </a>

                    <a
                        href="${pageContext.request.contextPath}/business/settlement/complete">
                        납부 내역
                    </a>

                    <a
                        href="${pageContext.request.contextPath}/business/settlement/account">
                        계좌 정보 관리
                    </a>

                </nav>

                <!-- 수수료 요약 -->
                <div class="summary-box">

                    <p>

                        <span class="summary-label">
                            이번달 매출
                        </span>

                        <span class="summary-value">
                            ${settlementSummary.monthSales}원
                        </span>

                    </p>

                    <p>

                        <span class="summary-label">
                            플랫폼 수수료
                        </span>

                        <span class="summary-value fee">
                            ${settlementSummary.feeAmount}원
                        </span>

                    </p>

                    <p>

                        <span class="summary-label">
                            수수료율
                        </span>

                        <span class="summary-value">
                            ${settlementSummary.feeRate}%
                        </span>

                    </p>

                    <p>

                        <span class="summary-label">
                            납부 기한
                        </span>

                        <span class="summary-value point">
                            ${settlementSummary.expectedDate}
                        </span>

                    </p>

                </div>

                <!-- 관리자 계좌 -->
                <div class="admin-account-box">

                    <h3>관리자 수수료 입금 계좌</h3>

                    <div class="admin-account-info">

                        <div class="account-row">

                            <span>은행명</span>

                            <strong>국민은행</strong>

                        </div>

                        <div class="account-row">

                            <span>예금주</span>

                            <strong>ODITJI 운영팀</strong>

                        </div>

                        <div class="account-row">

                            <span>계좌번호</span>

                            <div class="admin-account-copy">

                                <strong class="account-number">
                                    123-456-789012
                                </strong>

                                <button type="button"
                                        class="btn-copy"
                                        onclick="copyAccountNumber()">
                                    복사
                                </button>

                            </div>

                        </div>

                    </div>

                    <div class="admin-account-notice">

                        <p>
                            ※ 상품 판매 대금은 사업자에게 지급됩니다.
                        </p>

                        <p>
                            ※ 플랫폼 이용 수수료만 위 계좌로 입금해주세요.
                        </p>

                        <p>
                            ※ 입금 후 아래 "입금 확인 요청" 버튼을 눌러주세요.
                        </p>

                    </div>

                </div>

                <!-- 버튼 -->
                <div class="btn-row">

                    <form
                        action="${pageContext.request.contextPath}/business/settlement/request"
                        method="post">

                        <button
                            class="btn btn-primary"
                            type="submit">

                            입금 확인 요청

                        </button>

                    </form>

                </div>

                <!-- 현황 -->
                <div class="settlement-summary-grid">

                    <div class="settlement-card">

                        <span>수수료 납부 상태</span>

                        <strong>

                            <c:choose>

                                <c:when test="${settlementSummary.status eq 'WAITING'}">
                                    납부 가능
                                </c:when>

                                <c:when test="${settlementSummary.status eq 'REQUESTED'}">
                                    관리자 확인중
                                </c:when>

                                <c:when test="${settlementSummary.status eq 'APPROVED'}">
                                    납부 완료
                                </c:when>

                                <c:when test="${settlementSummary.status eq 'REJECTED'}">
                                    반려
                                </c:when>

                                <c:otherwise>
                                    납부 가능
                                </c:otherwise>

                            </c:choose>

                        </strong>

                    </div>

                    <div class="settlement-card">

                        <span>이번달 주문 수</span>

                        <strong>

                            ${settlementSummary.monthOrderCount}건

                        </strong>

                    </div>

                    <div class="settlement-card">

                        <span>수수료 금액</span>

                        <strong>

                            ${settlementSummary.feeAmount}원

                        </strong>

                    </div>

                </div>

            </section>

        </main>

    </div>

    <jsp:include page="/WEB-INF/views/common/footer.jsp" />

</body>

</html>