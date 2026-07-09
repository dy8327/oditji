<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<c:set var="activeMenu" value="order"/>

<!DOCTYPE html>
<html lang="ko">

    <head>
        <meta charset="UTF-8">
        <title>ODITJI | 주문 현황</title>
        <link rel="stylesheet" href="${pageContext.request.contextPath}/css/business.css">
    </head>

    <body>

        <jsp:include page="/WEB-INF/views/common/header.jsp"/>

        <div class="business-wrap">

            <jsp:include page="/WEB-INF/views/common/businessSidebar.jsp"/>

            <main class="main-content">

                <div class="business-page-header">
                    <h1 class="business-page-title">주문 현황</h1>
                    <p class="business-page-desc">
                        접수된 주문 내역을 확인하고 관리하세요.
                    </p>
                </div>

                <section class="business-content-box">

                    <%-- 1. 상태별 탭 메뉴 --%>
                    <nav class="tab-menu" style="margin-bottom: 24px;">
                        <a href="?status=" class="${empty param.status ? 'active' : ''}">전체</a>
                        <a href="?status=ORDERED" class="${param.status == 'ORDERED' ? 'active' : ''}">주문 완료</a>
                        <a href="?status=SHIPPING" class="${param.status == 'SHIPPING' ? 'active' : ''}">배송 중</a>
                        <a href="?status=DELIVERED" class="${param.status == 'DELIVERED' ? 'active' : ''}">배송 완료</a>
                    </nav>

                    <%-- 2. 기간 검색 및 키워드 검색 --%>
                    <div class="toolbar">
                        <form method="get" action="${pageContext.request.contextPath}/business/order/list">
                            <input type="date" name="startDate" value="${param.startDate}" class="page-search">
                            <span style="color: var(--biz-text-dim);">~</span>
                            <input type="date" name="endDate" value="${param.endDate}" class="page-search">
                            <input type="text" class="page-search" name="keyword" value="${param.keyword}" placeholder="상품명/수령인 검색">
                            <button type="submit" class="btn btn-dark">조회</button>
                        </form>
                    </div>

                    <%-- 3. 일괄 처리 폼 --%>
                    <form action="${pageContext.request.contextPath}/business/order/bulk-update" method="post">
                        <div class="card-list">
                            <c:choose>
                                <c:when test="${not empty orderList}">
                                    <c:forEach var="order" items="${orderList}">
                                        <article class="item-card">
                                            <input type="checkbox" name="orderNos" value="${order.orderNo}" style="margin-right: 15px;">
                                            <div class="item-info">
                                                <h3>${order.productName}</h3>
                                                <div class="meta">
                                                    <span>${order.createdAt} | ${order.orderNo}</span>
                                                    <%-- 4. 실시간 상태 배지 --%>
                                                    <span class="badge ${order.orderStatus == 'SHIPPING' ? 'badge-blue' : 'badge-gray'}">
                                                        ${order.orderStatus}
                                                    </span>
                                                </div>
                                            </div>
                                            <div class="item-actions">
                                                <a class="btn btn-dark" href="${pageContext.request.contextPath}/business/order/detail?orderNo=${order.orderNo}">상세</a>
                                            </div>
                                        </article>
                                    </c:forEach>
                                </c:when>
                                <c:otherwise>
                                    <article class="item-card empty">주문 내역이 없습니다.</article>
                                </c:otherwise>
                            </c:choose>
                        </div>
                        
                        <div style="margin-top: 24px;">
                            <button type="submit" class="btn btn-dark">선택 주문 배송 시작</button>
                        </div>
                    </form>

                </section>

            </main>

        </div>

        <jsp:include page="/WEB-INF/views/common/footer.jsp"/>

    </body>
</html>