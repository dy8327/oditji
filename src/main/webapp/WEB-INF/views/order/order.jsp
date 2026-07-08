<%@ page language="java" contentType="text/html; charset=UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!DOCTYPE html>
<html lang="ko">

<head>

    <meta charset="UTF-8">
    <title>ODITJI | 주문서</title>

    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/css/order.css">

    <script defer
            src="${pageContext.request.contextPath}/js/order.js"></script>

</head>

<body>

<jsp:include page="/WEB-INF/views/common/header.jsp"/>

<main class="order-container">

    <h1>주문서</h1>

    <!-- ================= CART ITEMS ================= -->
    <section class="order-items">

        <c:forEach var="c" items="${cartList}">

            <article class="order-item">

                <div class="order-img">
                    <img src="${c.image}" alt="${c.goodsName}">
                </div>

                <div class="order-info">

                    <h3>${c.goodsName}</h3>

                    <p>수량: ${c.quantity}</p>

                    <p>단가: ₩ ${c.price}</p>

                </div>

                <div class="order-subtotal">
                    ₩ ${c.price * c.quantity}
                </div>

            </article>

        </c:forEach>

    </section>

    <!-- ================= ORDER FORM ================= -->
    <section class="order-form">

        <h2>배송 정보</h2>

        <input type="text" id="receiver" placeholder="받는 사람">
        <input type="text" id="address" placeholder="주소">
        <input type="text" id="phone" placeholder="연락처">

    </section>

    <!-- ================= SUMMARY ================= -->
    <section class="order-summary">

        <div>
            <span>총 상품 금액</span>
            <span id="totalPrice">${totalPrice}</span>
        </div>

        <div>
            <span>배송비</span>
            <span>0</span>
        </div>

        <div class="final">
            <span>최종 결제 금액</span>
            <span id="finalPrice">${totalPrice}</span>
        </div>

        <button id="orderBtn">
            결제하기
        </button>

    </section>

</main>

<jsp:include page="/WEB-INF/views/common/footer.jsp"/>

</body>

</html>