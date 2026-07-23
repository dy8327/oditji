<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<c:set var="activeMenu" value="monitoring"/>

<!DOCTYPE html>
<html lang="ko">
<head>
<meta charset="UTF-8">
<title>ODITJI | 모니터링</title>
<link rel="stylesheet" href="${pageContext.request.contextPath}/css/admin.css">
<script src="https://cdn.jsdelivr.net/npm/chart.js@4/dist/chart.umd.min.js"></script>
<script src="${pageContext.request.contextPath}/js/admin.js"></script>
</head>

<body>

<jsp:include page="/WEB-INF/views/common/header.jsp"/>

<div class="admin-wrap">

    <jsp:include page="/WEB-INF/views/common/adminSidebar.jsp"/>

    <main class="main-content">

        <div class="admin-page-header">

            <a href="${pageContext.request.contextPath}/admin/main" class="back-link">
                ← 뒤로가기
            </a>

            <h1 class="admin-page-title">모니터링</h1>

            <p class="admin-page-desc">
                회원별 접속 현황과 상품 클릭 통계를 차트와 표로 확인할 수 있습니다.
            </p>

        </div>

        <section class="admin-content-box">

            <div class="chart-row">

                <div class="chart-box <c:if test='${empty visitorTrend}'>empty</c:if>">
                    <c:choose>
                        <c:when test="${not empty visitorTrend}">
                            <h3 class="chart-box-title">최근 7일 방문자 추이</h3>
                            <canvas id="visitorTrendChart" data-chart='<c:out value="${visitorTrendJson}"/>'></canvas>
                        </c:when>
                        <c:otherwise>
                            최근 7일간 방문자 데이터가 없습니다.
                        </c:otherwise>
                    </c:choose>
                </div>

                <div class="chart-box <c:if test='${empty popularClicks}'>empty</c:if>">
                    <c:choose>
                        <c:when test="${not empty popularClicks}">
                            <h3 class="chart-box-title">상품 클릭 TOP 5</h3>
                            <canvas id="popularClickChart" data-chart='<c:out value="${popularClicksJson}"/>'></canvas>
                        </c:when>
                        <c:otherwise>
                            상품 클릭 데이터가 없습니다.
                        </c:otherwise>
                    </c:choose>
                </div>

            </div>

            <table class="data-table">

                <thead>
                    <tr>
                        <th>회원</th>
                        <th>최근 접속일</th>
                        <th>상품 클릭 수</th>
                        <th>접속 IP</th>
                    </tr>
                </thead>

                <tbody>

                    <c:choose>

                        <c:when test="${not empty monitoringList}">

                            <c:forEach var="row" items="${monitoringList}">
                                <tr>
                                    <td>${row.nickname}</td>
                                    <td><fmt:formatDate value="${row.lastAccessAt}" pattern="yyyy-MM-dd"/></td>
                                    <td>${row.productClickCount}</td>
                                    <td>${row.accessIp}</td>
                                </tr>
                            </c:forEach>

                        </c:when>

                        <c:otherwise>
                            <tr><td colspan="4">모니터링 데이터가 없습니다.</td></tr>
                        </c:otherwise>

                    </c:choose>

                </tbody>

            </table>

        </section>

    </main>

</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp"/>

</body>
</html>
