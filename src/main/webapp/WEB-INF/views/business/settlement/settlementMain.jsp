<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core"%>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt"%>

<c:set var="activeMenu" value="settlement" />

<!DOCTYPE html>
<html lang="ko">

<head>

<meta charset="UTF-8">

<title>정산 관리</title>

<link rel="stylesheet"
    href="${pageContext.request.contextPath}/css/business.css">

<jsp:include page="/WEB-INF/views/common/head-assets.jsp"/>
</head>

<body>

    <jsp:include page="/WEB-INF/views/common/header.jsp" />

    <div class="business-wrap">

        <jsp:include page="/WEB-INF/views/common/businessSidebar.jsp" />

        <main id="mainContent" class="main-content">

            <a href="${pageContext.request.contextPath}/business/main"
                class="back-link">
                ← 뒤로가기
            </a>

            <h1 class="page-title">수수료 관리</h1>

            <section class="content-panel">

                <%-- [수정] 정산 요청 결과 메시지 표시 --%>
                <c:if test="${not empty successMessage}">
                    <p class="alert alert-success">${successMessage}</p>
                </c:if>
                <c:if test="${not empty errorMessage}">
                    <p class="alert alert-danger">${errorMessage}</p>
                </c:if>

                <!-- 탭 -->
                <nav class="tab-menu">
                    <a class="active" href="${pageContext.request.contextPath}/business/settlement/main">정산 요청</a>
                    <a href="${pageContext.request.contextPath}/business/settlement/complete">정산 내역</a>
                    <a href="${pageContext.request.contextPath}/business/settlement/account">계좌 정보 관리</a>
                </nav>

                <!-- 정산 요약 -->
                <div class="summary-box">
                    <p>
                        <span class="summary-label">정산 대상 판매금액</span>
                        <span class="summary-value">
                            <fmt:formatNumber value="${settlementSummary.monthSales}" pattern="#,##0"/>원
                        </span>
                    </p>
                    <p>
                        <span class="summary-label">플랫폼 수수료</span>
                        <span class="summary-value fee">
                            <fmt:formatNumber value="${settlementSummary.feeAmount}" pattern="#,##0"/>원
                        </span>
                    </p>
                    <p>
                        <span class="summary-label">정산 예정금</span>
                        <span class="summary-value point">
                            <fmt:formatNumber value="${settlementSummary.settledAmount}" pattern="#,##0"/>원
                        </span>
                    </p>
                    <p>
                        <span class="summary-label">적용 수수료율</span>
                        <span class="summary-value">${settlementSummary.feeRate}%</span>
                    </p>
                </div>    

                <!-- 사업자 정산 계좌 -->
                <div class="admin-account-box">
                    <h3>정산 지급 계좌</h3>

                    <div class="admin-account-info">
                        <div class="account-row">
                            <span>은행명</span>
                            <strong>
                                <c:choose>
                                    <c:when test="${not empty settlementAccount.bankName}">
                                        ${settlementAccount.bankName}
                                    </c:when>
                                    <c:otherwise>미등록</c:otherwise>
                                </c:choose>
                            </strong>
                        </div>

                        <div class="account-row">
                            <span>예금주</span>
                            <strong>
                                <c:choose>
                                    <c:when test="${not empty settlementAccount.accountHolder}">
                                        ${settlementAccount.accountHolder}
                                    </c:when>
                                    <c:otherwise>미등록</c:otherwise>
                                </c:choose>
                            </strong>
                        </div>

                        <div class="account-row">
                            <span>계좌번호</span>
                            <strong class="account-number">
                                <c:choose>
                                    <c:when test="${not empty settlementAccount.accountNumber}">
                                        ${settlementAccount.accountNumber}
                                    </c:when>
                                    <c:otherwise>미등록</c:otherwise>
                                </c:choose>
                            </strong>
                        </div>
                    </div>

                    <div class="admin-account-notice">
                        <p>※ 배송 완료된 주문만 정산 요청에 포함됩니다.</p>
                        <p>※ 플랫폼 수수료를 제외한 정산금이 위 계좌로 지급됩니다.</p>
                        <p>※ 정산 요청 당시의 계좌 정보가 정산 내역에 저장됩니다.</p>
                    </div>
                </div>

                <!-- 정산 요청 버튼 -->
                <div class="btn-row">
                    <c:choose>
                        <c:when test="${empty settlementAccount.bankName
                                or empty settlementAccount.accountNumber
                                or empty settlementAccount.accountHolder}">
                            <a class="btn btn-primary"
                            href="${pageContext.request.contextPath}/business/settlement/account">
                                정산 계좌 등록
                            </a>
                        </c:when>

                        <c:otherwise>
                            <form action="${pageContext.request.contextPath}/business/settlement/request"
                                method="post">
                                <c:choose>
                                    <c:when test="${settlementSummary.status eq 'REQUESTED'}">
                                        <button class="btn btn-primary" type="button" disabled>정산 요청 처리 중</button>
                                    </c:when>

                                    <c:when test="${settlementSummary.status eq 'REJECTED'}">
                                        <button class="btn btn-primary" type="button" disabled>정산 요청 반려</button>
                                    </c:when>

                                    <c:when test="${settlementSummary.status eq 'APPROVED'}">
                                        <button class="btn btn-primary" type="button" disabled>정산 완료</button>
                                    </c:when>

                                    <c:when test="${settlementSummary.settledAmount le 0}">
                                        <button class="btn btn-primary" type="button" disabled>정산 가능 내역 없음</button>
                                    </c:when>

                                    <c:otherwise>
                                        <button class="btn btn-primary" type="submit">정산 요청</button>
                                    </c:otherwise>
                                </c:choose>
                            </form>
                        </c:otherwise>
                    </c:choose>
                </div>

                <!-- 정산 현황 -->
                <div class="settlement-summary-grid">
                    <div class="settlement-card">
                        <span>정산 상태</span>
                        <strong>
                            <c:choose>
                                <c:when test="${settlementSummary.status eq 'WAITING'}">정산 요청 가능</c:when>
                                <c:when test="${settlementSummary.status eq 'REQUESTED'}">관리자 처리 중</c:when>
                                <c:when test="${settlementSummary.status eq 'APPROVED'}">정산 완료</c:when>
                                <c:when test="${settlementSummary.status eq 'REJECTED'}">정산 반려</c:when>
                                <c:otherwise>정산 가능 내역 없음</c:otherwise>
                            </c:choose>
                        </strong>
                    </div>

                    <div class="settlement-card">
                        <span>정산 대상 주문 수</span>
                        <strong>${settlementSummary.monthOrderCount}건</strong>
                    </div>

                    <div class="settlement-card">
                        <span>정산 예정금</span>
                        <strong>
                            <fmt:formatNumber value="${settlementSummary.settledAmount}" pattern="#,##0"/>원
                        </strong>
                    </div>
                </div>

            </section>

        </main>

    </div>

    <jsp:include page="/WEB-INF/views/common/footer.jsp" />

</body>

</html>