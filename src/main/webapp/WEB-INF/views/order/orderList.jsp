<%@ page language="java"
    contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>

<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<!DOCTYPE html>
<html lang="ko">

<head>

    <meta charset="UTF-8">
    <title>ODITJI | 주문 내역</title>

    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/css/order.css">

</head>

<body>

<jsp:include page="/WEB-INF/views/common/header.jsp"/>

<main class="order-list-container">

    <h1>주문 내역</h1>

    <!-- EMPTY -->
    <c:if test="${empty orderList}">
        <div class="empty-box">
            주문 내역이 없습니다.
        </div>
    </c:if>

    <!-- LIST -->
    <c:forEach var="o" items="${orderList}">

        <section class="order-card">

            <!-- ORDER HEADER -->
            <div class="order-card-header">

                <div>
                    <strong>주문번호</strong>
                    <span>${o.orderNo}</span>
                </div>

                <div>
                    <fmt:formatDate value="${o.createdAt}" pattern="yyyy.MM.dd HH:mm"/>
                </div>

                <div class="order-status">
                    ${o.orderStatus}
                </div>

            </div>

            <!-- ORDER ITEMS -->
            <div class="order-items">

                <c:forEach var="i" items="${o.items}">

                    <a class="order-item"
                       href="${pageContext.request.contextPath}/goods/goodsDetail/${i.productNo}">

                        <c:choose>

                            <c:when test="${empty i.mainImage}">
                                <div class="no-image">NO IMAGE</div>
                            </c:when>

                            <c:otherwise>
                                <img src="${i.mainImage}" alt="${i.productName}">
                            </c:otherwise>

                        </c:choose>

                        <div class="order-item-info">

                            <div>${i.productName}</div>

                            <div>
                                수량: ${i.quantity}
                            </div>

                            <div>
                                ₩ <fmt:formatNumber value="${i.itemTotalPrice}" pattern="#,###"/>
                            </div>

                        </div>

                    </a>

                </c:forEach>

            </div>

            <div class="ticket-divider" aria-hidden="true"></div>

            <!-- TOTAL -->
            <div class="order-total">
                총 결제금액: ₩ <fmt:formatNumber value="${o.totalAmount}" pattern="#,###"/>
            </div>

        </section>

    </c:forEach>

</main>

<jsp:include page="/WEB-INF/views/common/footer.jsp"/>

</body>

</html>
