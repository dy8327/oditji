<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

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
        <a href="${pageContext.request.contextPath}/business/main" class="back-link">← 뒤로가기</a>
        <h1 class="page-title">주문 현황</h1>

        <section class="content-panel">
            <table class="data-table">
                <thead>
                    <tr>
                        <th>주문번호</th>
                        <th>주문일</th>
                        <th>상품</th>
                        <th>수량</th>
                        <th>판매금액</th>
                        <th>주문상태</th>
                        <th>관리</th>
                    </tr>
                </thead>

                <tbody>
                    <c:choose>
                        <c:when test="${not empty orderList}">
                            <c:forEach var="order" items="${orderList}">
                                <c:set var="totalQuantity" value="0"/>
                                <c:forEach var="item" items="${order.items}">
                                    <c:set var="totalQuantity" value="${totalQuantity + item.quantity}"/>
                                </c:forEach>

                                <tr>
                                    <td><c:out value="${order.orderNo}"/></td>
                                    <td><fmt:formatDate value="${order.createdAt}" pattern="yyyy-MM-dd HH:mm"/></td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${not empty order.items}">
                                                <c:out value="${order.items[0].productName}"/>
                                                <c:if test="${fn:length(order.items) > 1}">
                                                    외 ${fn:length(order.items) - 1}건
                                                </c:if>
                                            </c:when>
                                            <c:otherwise>-</c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td><c:out value="${totalQuantity}"/>개</td>
                                    <td><fmt:formatNumber value="${order.totalAmount}" pattern="#,###"/>원</td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${order.orderStatus eq 'ORDERED'}">
                                                <span class="status waiting">주문 완료</span>
                                            </c:when>
                                            <c:when test="${order.orderStatus eq 'PAID'}">
                                                <span class="status ok">결제 완료</span>
                                            </c:when>
                                            <c:when test="${order.orderStatus eq 'PREPARING'}">
                                                <span class="status waiting">상품 준비 중</span>
                                            </c:when>
                                            <c:when test="${order.orderStatus eq 'SHIPPING'}">
                                                <span class="status waiting">배송 중</span>
                                            </c:when>
                                            <c:when test="${order.orderStatus eq 'DELIVERED'}">
                                                <span class="status ok">배송 완료</span>
                                            </c:when>
                                            <c:when test="${order.orderStatus eq 'CANCELED'}">
                                                <span class="status">주문 취소</span>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="status"><c:out value="${order.orderStatus}"/></span>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td>
                                        <a class="btn btn-dark"
                                           href="${pageContext.request.contextPath}/business/order/detail?orderNo=${order.orderNo}">
                                            상세보기
                                        </a>
                                    </td>
                                </tr>
                            </c:forEach>
                        </c:when>
                        <c:otherwise>
                            <tr>
                                <td colspan="7">주문 내역이 없습니다.</td>
                            </tr>
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