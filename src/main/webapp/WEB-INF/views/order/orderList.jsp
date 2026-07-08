<%@ page language="java" contentType="text/html; charset=UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

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
                    <span>${o.orderId}</span>
                </div>

                <div>
                    <span>${o.createdAt}</span>
                </div>

                <div class="order-status">
                    ${o.status}
                </div>

            </div>

            <!-- ORDER ITEMS -->
            <div class="order-items">

                <c:forEach var="i" items="${o.items}">

                    <div class="order-item">

                        <img src="${i.image}" />

                        <div class="order-item-info">

                            <div>${i.goodsName}</div>

                            <div>
                                수량: ${i.quantity}
                            </div>

                            <div>
                                ₩ ${i.price}
                            </div>

                        </div>

                    </div>

                </c:forEach>

            </div>

            <!-- TOTAL -->
            <div class="order-total">
                총 결제금액: ₩ ${o.totalPrice}
            </div>

        </section>

    </c:forEach>

</main>

<jsp:include page="/WEB-INF/views/common/footer.jsp"/>

</body>

</html>