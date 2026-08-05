<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core"%>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt"%>
<%@ taglib prefix="dt" uri="http://oditji.com/functions/datetime" %>
<c:set var="activeMenu" value="settlement" />
<!DOCTYPE html>
<html lang="ko">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>ODITJI | 정산 내역</title>
<link rel="stylesheet" href="${pageContext.request.contextPath}/css/business.css">
</head>
<body>
<jsp:include page="/WEB-INF/views/common/header.jsp" />
<div class="business-wrap">
    <jsp:include page="/WEB-INF/views/common/businessSidebar.jsp" />
    <main id="mainContent" class="main-content">
        <a href="${pageContext.request.contextPath}/business/main" class="back-link">← 뒤로가기</a>
        <h1 class="page-title">정산 관리</h1>
        <section class="content-panel">
            <nav class="tab-menu">
                <a href="${pageContext.request.contextPath}/business/settlement/main">정산 요청</a>
                <a class="active" href="${pageContext.request.contextPath}/business/settlement/complete">정산 내역</a>
                <a href="${pageContext.request.contextPath}/business/settlement/account">계좌 정보 관리</a>
            </nav>

            <div class="admin-account-box">
                <h3>정산 요청 내역</h3>
                <table class="data-table">
                    <thead>
                        <tr>
                            <th>정산 월</th>
                            <th>요청일</th>
                            <th>판매금액</th>
                            <th>수수료</th>
                            <th>정산금</th>
                            <th>주문 수</th>
                            <th>상태</th>
                            <th>처리일</th>
                        </tr>
                    </thead>
                    <tbody>
                    <c:choose>
                        <c:when test="${not empty settlementHistory}">
                            <c:forEach var="item" items="${settlementHistory}">
                                <tr>
                                    <td>${item.settlementMonth}</td>
                                    <td>
                                        ${dt:format(item.requestedAt, 'yyyy-MM-dd')}
                                    </td>
                                    <td>
                                        <fmt:formatNumber value="${item.totalAmount}" pattern="#,##0"/>원
                                    </td>
                                    <td>
                                        <fmt:formatNumber value="${item.feeAmount}" pattern="#,##0"/>원
                                    </td>
                                    <td>
                                        <fmt:formatNumber value="${item.settledAmount}" pattern="#,##0"/>원
                                    </td>
                                    <td>${item.orderCount}건</td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${item.status eq 'REQUESTED'}">처리 중</c:when>
                                            <c:when test="${item.status eq 'DONE'}">지급 완료</c:when>
                                            <c:when test="${item.status eq 'REJECTED'}">
                                                반려
                                                <c:if test="${not empty item.rejectReason}">
                                                    <br><small>${item.rejectReason}</small>
                                                </c:if>
                                            </c:when>
                                            <c:otherwise>${item.status}</c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${not empty item.processedAt}">
                                                ${dt:format(item.processedAt, 'yyyy-MM-dd')}
                                            </c:when>
                                            <c:otherwise>-</c:otherwise>
                                        </c:choose>
                                    </td>
                                </tr>
                            </c:forEach>
                        </c:when>
                        <c:otherwise>
                            <tr>
                                <td colspan="8">정산 요청 내역이 없습니다.</td>
                            </tr>
                        </c:otherwise>
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