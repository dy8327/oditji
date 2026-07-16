<%@ page language="java"
    contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>

<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<!DOCTYPE html>
<html lang="ko">

<head>

    <meta charset="UTF-8">
    <title>ODITJI | 주문 완료</title>

    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/css/order.css">

</head>

<body>

<jsp:include page="/WEB-INF/views/common/header.jsp"/>

<main class="order-complete-container">

    <section class="complete-box">

        <h1>주문 완료 🎉</h1>

        <p>주문이 정상적으로 처리되었습니다.</p>

        <!-- ORDER INFO -->
        <div class="complete-info">

            <div>
                <span>주문번호</span>
                <strong>${order.orderNo}</strong>
            </div>

            <div>
                <span>받는 사람</span>
                <strong>${order.receiverName}</strong>
            </div>

            <div>
                <span>배송지</span>
                <strong>${order.address}</strong>
            </div>

            <div>
                <span>결제금액</span>
                <strong>
                    ₩ <fmt:formatNumber value="${order.totalAmount}" pattern="#,###"/>
                </strong>
            </div>

        </div>

        <div class="ticket-divider" aria-hidden="true"></div>

        <!-- ORDER ITEMS -->
        <div class="complete-items">

            <c:forEach var="item" items="${order.items}">

                <div class="complete-item">

                    <c:choose>

                        <c:when test="${empty item.mainImage}">
                            <div class="no-image">NO IMAGE</div>
                        </c:when>

                        <c:otherwise>
                            <img src="${item.mainImage}" alt="${item.productName}">
                        </c:otherwise>

                    </c:choose>

                    <div class="complete-item-info">
                        <div>${item.productName}</div>
                        <div>수량: ${item.quantity}</div>
                        <div>
                            ₩ <fmt:formatNumber value="${item.itemTotalPrice}" pattern="#,###"/>
                        </div>
                    </div>

                </div>

            </c:forEach>

        </div>

        <!-- ACTION -->
        <div class="complete-actions">

            <a href="${pageContext.request.contextPath}/order/list">
                주문 내역 보기
            </a>

            <a href="${pageContext.request.contextPath}/goods/list">
                쇼핑 계속하기
            </a>

        </div>

    </section>

</main>

<jsp:include page="/WEB-INF/views/common/footer.jsp"/>

</body>

</html>
