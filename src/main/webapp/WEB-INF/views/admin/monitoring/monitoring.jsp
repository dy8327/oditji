<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<c:set var="activeMenu" value="monitoring"/>

<!DOCTYPE html>
<html lang="ko">
<head>
<meta charset="UTF-8">
<title>ODITJI | 모니터링</title>
<link rel="stylesheet" href="${pageContext.request.contextPath}/css/admin.css">
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
                회원별 콘텐츠 이용 현황을 차트와 표로 확인할 수 있습니다.
            </p>

        </div>

        <section class="admin-content-box">

            <div class="chart-box">
                사용자 별 콘텐츠 이용 차트, 표<br>
                (콘텐츠 조회수 / 상품 클릭수 / 방문자 추이 등 통계 영역)
            </div>

            <table class="data-table">

                <thead>
                    <tr>
                        <th>회원</th>
                        <th>최근 접속일</th>
                        <th>콘텐츠 이용 수</th>
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
                                    <td>${row.lastAccessAt}</td>
                                    <td>${row.contentUseCount}</td>
                                    <td>${row.productClickCount}</td>
                                    <td>${row.accessIp}</td>
                                </tr>
                            </c:forEach>

                        </c:when>

                        <c:otherwise>
                            <tr><td colspan="5">모니터링 데이터가 없습니다.</td></tr>
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
