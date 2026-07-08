<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<c:set var="activeMenu" value="event"/>

<!DOCTYPE html>
<html lang="ko">
<head>
<meta charset="UTF-8">
<title>ODITJI | 이벤트 관리</title>
<link rel="stylesheet" href="${pageContext.request.contextPath}/css/business.css">
</head>

<body>

<jsp:include page="/WEB-INF/views/common/header.jsp"/>

<div class="business-wrap">

    <jsp:include page="/WEB-INF/views/common/businessSidebar.jsp"/>

    <main class="main-content">

        <a href="${pageContext.request.contextPath}/business/main"
           class="back-link">
            ← 뒤로가기
        </a>

        <h1 class="page-title">
            이벤트 관리
        </h1>

        <section class="content-panel">
        
            <div class="product-control-row">
                
                <div></div>

                <form action="${pageContext.request.contextPath}/business/event/list" 
                    method="get" 
                    class="product-search-form">

                    <input type="text"
                        name="keyword"
                        value="${param.keyword}"
                        placeholder="이벤트명, 상태 검색">

                    <button type="submit">
                        검색
                    </button>
                </form>

                <button type="button"
                        class="product-register-btn"
                        onclick="location.href='${pageContext.request.contextPath}/business/event/register'">
                    등록 요청
                </button>

            </div>

            <table class="data-table">

                <thead>

                    <tr>
                        <th>번호</th>
                        <th>이벤트명</th>
                        <th>이벤트 기간</th>
                        <th>상태</th>
                        <th>등록일</th>
                        <th>관리</th>
                    </tr>

                </thead>

                <tbody>

                    <c:choose>

                        <c:when test="${not empty eventList}">

                            <c:forEach var="event" items="${eventList}">

                                <tr>

                                    <td>${event.eventNo}</td>

                                    <td>${event.title}</td>

                                    <td>
                                        ${event.startDate} ~ ${event.endDate}
                                    </td>

                                    <td>

                                        <c:choose>

                                            <c:when test="${event.status == 'ACTIVE'}">

                                                <span class="status ok">
                                                    진행중
                                                </span>

                                            </c:when>

                                            <c:when test="${event.status == 'WAITING'}">

                                                <span class="status waiting">
                                                    승인 대기
                                                </span>

                                            </c:when>

                                            <c:otherwise>

                                                <span class="status">
                                                    종료
                                                </span>

                                            </c:otherwise>

                                        </c:choose>

                                    </td>

                                    <td>${event.createdAt}</td>

                                    <td>

                                        <c:choose>

                                            <c:when test="${event.status == 'WAITING'}">

                                                <a class="btn btn-dark"
                                                   href="${pageContext.request.contextPath}/business/approval/event-detail?eventNo=${event.eventNo}">
                                                    승인 상태
                                                </a>

                                            </c:when>

                                            <c:otherwise>

                                                <a class="btn btn-dark"
                                                   href="${pageContext.request.contextPath}/business/event/update?eventNo=${event.eventNo}">
                                                    수정 요청
                                                </a>

                                            </c:otherwise>

                                        </c:choose>

                                    </td>

                                </tr>

                            </c:forEach>

                        </c:when>

                        <c:otherwise>

                            <tr>
                                <td colspan="6">
                                    등록된 이벤트가 없습니다.
                                </td>
                            </tr>

                        </c:otherwise>

                    </c:choose>

                </tbody>

            </table>


            <div class="pagination">

                <a href="#">
                    ‹
                </a>

                <c:forEach var="p"
                           begin="1"
                           end="${empty pagination.totalPages ? 1 : pagination.totalPages}">

                    <a href="?page=${p}"
                       class="${pagination.currentPage == p ? 'active' : ''}">
                        ${p}
                    </a>

                </c:forEach>

                <a href="#">
                    ›
                </a>

            </div>

        </section>

    </main>

</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp"/>

</body>
</html>