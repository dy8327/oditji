<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core"%>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt"%>
<c:set var="activeMenu" value="settlement" />
<!DOCTYPE html>
<html lang="ko">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>ODITJI | 납부 내역</title>
<link rel="stylesheet" href="${pageContext.request.contextPath}/css/business.css">
</head>
<body>
<jsp:include page="/WEB-INF/views/common/header.jsp" />
<div class="business-wrap">
    <jsp:include page="/WEB-INF/views/common/businessSidebar.jsp" />
    <main class="main-content">
        <a href="${pageContext.request.contextPath}/business/main" class="back-link">← 뒤로가기</a>
        <h1 class="page-title">수수료 관리</h1>
        <section class="content-panel">
            <%-- [수정] 기존 수수료 관리 화면과 동일한 탭 레이아웃 유지 --%>
            <nav class="tab-menu">
                <a href="${pageContext.request.contextPath}/business/settlement/main">이번달 수수료</a>
                <a class="active" href="${pageContext.request.contextPath}/business/settlement/complete">납부 내역</a>
                <a href="${pageContext.request.contextPath}/business/settlement/account">계좌 정보 관리</a>
            </nav>
            <div class="admin-account-box">
                <h3>월별 수수료 납부 내역</h3>
                <table class="data-table">
                    <thead>
                        <tr><th>정산 월</th><th>월 매출</th><th>주문 수</th><th>수수료율</th><th>수수료 금액</th><th>상태</th><th>처리일</th></tr>
                    </thead>
                    <tbody>
                    <c:choose>
                        <c:when test="${not empty settlementHistory}">
                            <c:forEach var="item" items="${settlementHistory}">
                                <tr>
                                    <td>${item.settlementMonth}</td>
                                    <td><fmt:formatNumber value="${item.monthSales}" pattern="#,##0"/>원</td>
                                    <td>${item.monthOrderCount}건</td>
                                    <td>${item.feeRate}%</td>
                                    <td><fmt:formatNumber value="${item.feeAmount}" pattern="#,##0"/>원</td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${item.status eq 'APPROVED'}">납부 완료</c:when>
                                            <c:when test="${item.status eq 'REQUESTED'}">관리자 확인중</c:when>
                                            <c:when test="${item.status eq 'REJECTED'}">반려</c:when>
                                            <c:otherwise>납부 가능</c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td>${empty item.settledAt ? '-' : item.settledAt}</td>
                                </tr>
                            </c:forEach>
                        </c:when>
                        <c:otherwise><tr><td colspan="7">수수료 납부 내역이 없습니다.</td></tr></c:otherwise>
                    </c:choose>
                    </tbody>
                </table>
            </div>
        </section>
    </main>
</div>
<jsp:include page="/WEB-INF/views/common/footer.jsp" />
</body>
</html>