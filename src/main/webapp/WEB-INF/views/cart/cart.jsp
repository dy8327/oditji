<%@ page language="java" contentType="text/html; charset=UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!DOCTYPE html>
<html lang="ko">

<head>

    <meta charset="UTF-8">
    <title>ODITJI | 장바구니</title>

    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/css/cart.css">

    <script defer
            src="${pageContext.request.contextPath}/js/cart.js"></script>

</head>

<body>

<jsp:include page="/WEB-INF/views/common/header.jsp"/>

<main class="cart-container">

    <!-- ================= HEADER ================= -->
    <section class="cart-header">
        <h1>장바구니</h1>
    </section>

    <!-- ================= EMPTY ================= -->
    <c:if test="${empty cartList}">
        <div class="empty-cart">
            장바구니가 비어있습니다.
        </div>
    </c:if>

    <!-- ================= CART LIST ================= -->
    <c:if test="${not empty cartList}">

        <section class="cart-list">

            <c:forEach var="c" items="${cartList}">

                <article class="cart-item">

                    <!-- IMAGE -->
                    <div class="cart-item__img">
                        <img src="${c.image}" alt="${c.goodsName}">
                    </div>

                    <!-- INFO -->
                    <div class="cart-item__info">

                        <h3>${c.goodsName}</h3>

                        <p class="cart-price">
                            ₩ <span class="unit-price">${c.price}</span>
                        </p>

                        <!-- QUANTITY -->
                        <div class="cart-qty">

                            <button class="qty-btn minus"
                                    data-cart-id="${c.cartId}"
                                    data-price="${c.price}">
                                -
                            </button>

                            <input type="number"
                                   class="qty-input"
                                   value="${c.quantity}"
                                   min="1"
                                   data-cart-id="${c.cartId}"
                                   data-price="${c.price}"/>

                            <button class="qty-btn plus"
                                    data-cart-id="${c.cartId}"
                                    data-price="${c.price}">
                                +
                            </button>

                        </div>

                    </div>

                    <!-- SUBTOTAL -->
                    <div class="cart-item__total">
                        ₩ <span class="subtotal">
                            ${c.price * c.quantity}
                        </span>
                    </div>

                    <!-- DELETE -->
                    <button class="cart-delete-btn"
                            data-cart-id="${c.cartId}">
                        삭제
                    </button>

                </article>

            </c:forEach>

        </section>

        <!-- ================= TOTAL ================= -->
        <section class="cart-summary">

            <div class="summary-box">

                <div class="summary-row">
                    <span>상품 합계</span>
                    <span id="totalPrice">0</span>
                </div>

                <div class="summary-row">
                    <span>배송비</span>
                    <span>0</span>
                </div>

                <div class="summary-row total">
                    <span>총 결제금액</span>
                    <span id="finalPrice">0</span>
                </div>

                <button class="checkout-btn">
                    주문하기
                </button>

            </div>

        </section>

    </c:if>

</main>

<jsp:include page="/WEB-INF/views/common/footer.jsp"/>

</body>

</html>