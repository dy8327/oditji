<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<c:set var="activeMenu" value="order"/>

<!DOCTYPE html>
<html lang="ko">
<head>
<meta charset="UTF-8">
<title>ODITJI | 주문 상세</title>
<link rel="stylesheet" href="${pageContext.request.contextPath}/css/business.css">
</head>
<body>

<jsp:include page="/WEB-INF/views/common/header.jsp"/>

<div class="business-wrap">
    <jsp:include page="/WEB-INF/views/common/businessSidebar.jsp"/>

    <main class="main-content">
        <div class="business-page-header">
            <a href="${pageContext.request.contextPath}/business/order/list" class="back-link">← 목록으로</a>
            <h1 class="business-page-title">주문 상세 정보</h1>
        </div>

        <!-- 주문 기본 정보 -->
        <section class="content-panel">
            <article class="item-card">
                <div class="item-info">
                    <h3>주문번호 : ${order.orderNo}</h3>
                    <div class="meta">
                        <span>주문일 : <fmt:formatDate value="${order.createdAt}" pattern="yyyy-MM-dd HH:mm"/></span>
                    </div>
                </div>
            </article>

            <!-- 주문 상품 -->
            <table class="data-table">
                <thead>
                    <tr>
                        <th>상품명</th>
                        <th>상품번호</th>
                        <th>수량</th>
                        <th>판매단가</th>
                        <th>판매금액</th>
                        <th>상태</th>
                    </tr>
                </thead>
                <tbody>
                    <c:forEach var="item" items="${order.items}">
                        <tr>
                            <td><c:out value="${item.productName}"/></td>
                            <td>${item.productNo}</td>
                            <td>${item.quantity}개</td>
                            <td><fmt:formatNumber value="${item.productPrice}" pattern="#,###"/>원</td>
                            <td><fmt:formatNumber value="${item.productPrice * item.quantity}" pattern="#,###"/>원</td>
                            <td>
                                <c:choose>
                                    <c:when test="${item.status eq 'PAID'}">
                                        <span class="status waiting">결제 완료</span>
                                    </c:when>
                                    <c:when test="${item.status eq 'PREPARING'}">
                                        <span class="status waiting">상품 준비 중</span>
                                    </c:when>
                                    <c:when test="${item.status eq 'SHIPPING'}">
                                        <span class="status waiting">배송 중</span>
                                    </c:when>
                                    <c:when test="${item.status eq 'DELIVERED'}">
                                        <span class="status ok">배송 완료</span>
                                    </c:when>
                                    <c:when test="${item.status eq 'CANCELED'}">
                                        <span class="status reject">취소</span>
                                    </c:when>
                                    <c:otherwise>
                                        <span class="status">${item.status}</span>
                                    </c:otherwise>
                                </c:choose>
                            </td>
                        </tr>
                    </c:forEach>
                </tbody>
            </table>

            <!-- 배송지 정보 -->
            <article class="item-card">
                <div class="item-info">
                    <h3>배송지 정보</h3>
                    <div class="meta">
                        <span>수령인 : <c:out value="${order.receiverName}"/></span>
                        <span>연락처 : <c:out value="${order.receiverPhone}"/></span>
                        <span>주소 : <c:out value="${order.address}"/></span>
                    </div>
                </div>
            </article>

            <!-- 판매 금액 -->
            <div class="summary-box">
                <p>
                    <span>내 상품 판매금액</span>
                    <strong><fmt:formatNumber value="${order.totalAmount}" pattern="#,###"/>원</strong>
                </p>
            </div>
        </section>
    </main>
</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp"/>

</body>
</html>