<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<c:set var="activeMenu" value="order"/>

<!DOCTYPE html>
<html lang="ko">

    <head>
        <meta charset="UTF-8">
        <title>ODITJI | 취소/환불 관리</title>
        <link rel="stylesheet" href="${pageContext.request.contextPath}/css/business.css">
    </head>

    <body>

        <jsp:include page="/WEB-INF/views/common/header.jsp"/>

        <div class="business-wrap">

            <jsp:include page="/WEB-INF/views/common/businessSidebar.jsp"/>

            <main class="main-content">

                <div class="business-page-header">
                    <h1 class="business-page-title">취소/환불 관리</h1>
                    <p class="business-page-desc">고객의 요청을 확인하고 승인 또는 반려 처리하세요.</p>
                </div>

                <section class="business-content-box">

                    <%-- 상태별 필터 탭 --%>
                    <nav class="tab-menu" style="margin-bottom: 24px;">
                        <a href="?status=ALL" class="${empty param.status || param.status == 'ALL' ? 'active' : ''}">전체</a>
                        <a href="?status=WAITING" class="${param.status == 'WAITING' ? 'active' : ''}">처리 대기</a>
                        <a href="?status=APPROVED" class="${param.status == 'APPROVED' ? 'active' : ''}">승인 완료</a>
                        <a href="?status=REJECTED" class="${param.status == 'REJECTED' ? 'active' : ''}">반려</a>
                    </nav>

                    <div class="card-list">
                        <c:choose>
                            <c:when test="${not empty cancelList}">
                                <c:forEach var="item" items="${cancelList}">
                                    <article class="item-card">
                                        <div class="item-info">
                                            <h3>${item.productName}</h3>
                                            <div class="meta">
                                                <span>주문번호 : ${item.orderNo}</span>
                                                <span>사유 : ${item.reason}</span>
                                                <span class="badge ${item.status == 'WAITING' ? 'badge-yellow' : 'badge-gray'}">
                                                    ${item.status}
                                                </span>
                                            </div>
                                        </div>
                                        <div class="item-actions">
                                            <c:if test="${item.status == 'WAITING'}">
                                                <form action="${pageContext.request.contextPath}/business/cancel/approve" method="post" style="display:inline-block;">
                                                    <input type="hidden" name="cancelNo" value="${item.cancelNo}">
                                                    <button type="submit" class="btn btn-dark">승인</button>
                                                </form>
                                                <form action="${pageContext.request.contextPath}/business/cancel/reject" method="post" style="display:inline-block; margin-left: 8px;">
                                                    <input type="hidden" name="cancelNo" value="${item.cancelNo}">
                                                    <button type="submit" class="btn btn-danger">반려</button>
                                                </form>
                                            </c:if>
                                        </div>
                                    </article>
                                </c:forEach>
                            </c:when>
                            <c:otherwise>
                                <article class="item-card empty">취소/환불 요청 내역이 없습니다.</article>
                            </c:otherwise>
                        </c:choose>
                    </div>

                </section>

            </main>

        </div>

        <jsp:include page="/WEB-INF/views/common/footer.jsp"/>

    </body>
</html>