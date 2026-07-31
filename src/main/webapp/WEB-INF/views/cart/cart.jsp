<%@ page language="java"
    contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>

<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<!DOCTYPE html>
<html lang="ko">

<head>

    <meta charset="UTF-8">

    <meta name="viewport"
          content="width=device-width, initial-scale=1.0">

    <title>ODITJI | 장바구니</title>

    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/css/cart.css">

    <script defer
            src="${pageContext.request.contextPath}/js/cart.js">
    </script>

</head>

<body data-context-path="${pageContext.request.contextPath}">

<jsp:include page="/WEB-INF/views/common/header.jsp"/>

<main id="mainContent" class="cart-container">

    <section class="cart-header">

        <div>

            <p class="cart-header-label">
                MY SHOPPING
            </p>

            <h1>장바구니</h1>

            <p class="cart-header-description">
                구매할 상품과 수량을 확인해주세요.
            </p>

        </div>

        <a href="${pageContext.request.contextPath}/goods/list"
           class="continue-shopping-btn">

            상품 더 보기

        </a>

    </section>

    <c:choose>

        <c:when test="${empty cartItemList}">

            <section class="empty-cart">

                <div class="empty-cart-icon">
                    🛒
                </div>

                <h2>
                    장바구니가 비어 있습니다.
                </h2>

                <p>
                    마음에 드는 상품을 장바구니에 담아보세요.
                </p>

                <a href="${pageContext.request.contextPath}/goods/list"
                   class="empty-cart-link">

                    상품 보러가기

                </a>

            </section>

        </c:when>

        <c:otherwise>

            <section class="cart-toolbar">

                <label class="cart-select-all">

                    <input type="checkbox"
                           id="selectAll"
                           checked>

                    <span>전체 선택</span>

                </label>

                <button type="button"
                        id="deleteSelectedBtn"
                        class="delete-selected-btn">

                    선택 삭제

                </button>

            </section>

            <div class="cart-layout">

                <section class="cart-list">

                    <c:forEach var="item"
                               items="${cartItemList}">

                        <article class="cart-item
                            ${!item.available ? 'is-unavailable' : ''}"
                            data-cart-item-no="${item.cartItemNo}"
                            data-unit-price="${item.discountPrice}"
                            data-stock="${item.stock}"
                            data-available="${item.available}">

                            <div class="cart-item-checkbox">

                                <%--
                                    반복되는 상품 선택 체크박스마다 고유 id를 만들고
                                    상품명을 포함한 label과 연결한다. label은 화면에는
                                    보이지 않지만 스크린 리더에는 선택 대상을 알려준다.
                                --%>
                                <label for="cartItemCheck-${item.cartItemNo}"
                                       style="position:absolute;
                                              width:1px;
                                              height:1px;
                                              padding:0;
                                              margin:-1px;
                                              overflow:hidden;
                                              clip:rect(0, 0, 0, 0);
                                              white-space:nowrap;
                                              border:0;">
                                    <c:out value="${item.productName}"/> 상품 선택
                                </label>

                                <input type="checkbox"
                                       id="cartItemCheck-${item.cartItemNo}"
                                       class="cart-item-check"
                                       value="${item.cartItemNo}"
                                       ${item.available ? 'checked' : 'disabled'}>

                            </div>

                            <a class="cart-item-image"
                               href="${pageContext.request.contextPath}/goods/goodsDetail/${item.productNo}">

                                <c:choose>

                                    <c:when test="${empty item.mainImage}">

                                        <div class="cart-no-image">
                                            NO IMAGE
                                        </div>

                                    </c:when>

                                    <c:when test="${fn:startsWith(item.mainImage, 'http://')
                                                    or fn:startsWith(item.mainImage, 'https://')}">

                                        <img src="${item.mainImage}"
                                             alt="${item.productName}">

                                    </c:when>

                                    <c:otherwise>

                                        <img src="${pageContext.request.contextPath}${item.mainImage}"
                                             alt="${item.productName}">

                                    </c:otherwise>

                                </c:choose>

                            </a>

                            <div class="cart-item-info">

                                <p class="cart-item-business">
                                    ${item.businessName}
                                </p>

                                <a class="cart-item-name"
                                   href="${pageContext.request.contextPath}/goods/goodsDetail/${item.productNo}">

                                    ${item.productName}

                                </a>

                                <%--
                                    =========================================================
                                    [상품 옵션 정보 표시 추가]

                                    사용자가 선택한 상품 옵션이 있는 경우에만
                                    색상과 사이즈 정보를 장바구니 화면에 표시한다.
                                    =========================================================
                                --%>
                                <c:if test="${not empty item.optionNo}">
                                    <p class="cart-item-option">

                                        <span class="option-label">옵션</span>

                                        <c:if test="${not empty item.colorName}">
                                            <span>
                                                색상:
                                                <c:out value="${item.colorName}"/>
                                            </span>
                                        </c:if>

                                        <c:if test="${not empty item.sizeName}">
                                            <span>
                                                사이즈:
                                                <c:out value="${item.sizeName}"/>
                                            </span>
                                        </c:if>

                                    </p>
                                </c:if>

                                <p class="cart-item-type">
                                    ${item.productType}
                                </p>

                                <c:if test="${item.discountRate > 0}">

                                    <div class="cart-original-price">

                                        <fmt:formatNumber
                                            value="${item.price}"
                                            pattern="#,###"/>원

                                    </div>

                                </c:if>

                                <div class="cart-sale-price">

                                    <c:if test="${item.discountRate > 0}">

                                        <span class="cart-discount-rate">
                                            ${item.discountRate}%
                                        </span>

                                    </c:if>

                                    <strong>

                                        <fmt:formatNumber
                                            value="${item.discountPrice}"
                                            pattern="#,###"/>원

                                    </strong>

                                </div>

                                <c:choose>

                                    <c:when test="${item.stock <= 0}">

                                        <p class="cart-status-message error">
                                            품절된 상품입니다.
                                        </p>

                                    </c:when>

                                    <c:when test="${item.status ne 'APPROVED'}">

                                        <p class="cart-status-message error">
                                            현재 판매 중인 상품이 아닙니다.
                                        </p>

                                    </c:when>

                                    <c:when test="${item.quantity > item.stock}">

                                        <p class="cart-status-message error">
                                            재고가 부족합니다. 현재 재고 ${item.stock}개
                                        </p>

                                    </c:when>

                                    <c:when test="${item.stock <= 5}">

                                        <p class="cart-status-message warning">
                                            재고 ${item.stock}개 남음
                                        </p>

                                    </c:when>

                                </c:choose>

                            </div>

                            <div class="cart-item-actions">

                                <div class="cart-quantity">

                                    <button type="button"
                                            class="quantity-btn minus"
                                            data-cart-item-no="${item.cartItemNo}"
                                            ${!item.available ? 'disabled' : ''}>

                                        −

                                    </button>

                                    <%--
                                        수량 입력창도 상품별 고유 id와 label을 연결한다.
                                        수량 조절 버튼 사이의 기존 레이아웃은 유지한다.
                                    --%>
                                    <label for="quantity-${item.cartItemNo}"
                                           style="position:absolute;
                                                  width:1px;
                                                  height:1px;
                                                  padding:0;
                                                  margin:-1px;
                                                  overflow:hidden;
                                                  clip:rect(0, 0, 0, 0);
                                                  white-space:nowrap;
                                                  border:0;">
                                        <c:out value="${item.productName}"/> 상품 수량
                                    </label>

                                    <input type="number"
                                           id="quantity-${item.cartItemNo}"
                                           class="quantity-input"
                                           value="${item.quantity}"
                                           min="1"
                                           max="${item.stock}"
                                           data-cart-item-no="${item.cartItemNo}"
                                           data-previous-quantity="${item.quantity}"
                                           ${!item.available ? 'disabled' : ''}>

                                    <button type="button"
                                            class="quantity-btn plus"
                                            data-cart-item-no="${item.cartItemNo}"
                                            ${!item.available ? 'disabled' : ''}>

                                        ＋

                                    </button>

                                </div>

                                <div class="cart-item-total">

                                    <span>상품 금액</span>

                                    <strong class="item-total-price">

                                        <fmt:formatNumber
                                            value="${item.itemTotalPrice}"
                                            pattern="#,###"/>원

                                    </strong>

                                </div>

                                <button type="button"
                                        class="cart-delete-btn"
                                        data-cart-item-no="${item.cartItemNo}">

                                    삭제

                                </button>

                            </div>

                        </article>

                    </c:forEach>

                </section>

                <aside class="cart-summary">

                    <div class="summary-box">

                        <h2>
                            결제 예정 금액
                        </h2>

                        <div class="summary-row">

                            <span>선택 상품 금액</span>

                            <strong id="selectedProductPrice">
                                0원
                            </strong>

                        </div>

                        <div class="summary-row">

                            <span>배송비</span>

                            <strong>
                                0원
                            </strong>

                        </div>

                        <div class="summary-divider">
                        </div>

                        <div class="summary-row total">

                            <span>총 결제 금액</span>

                            <strong id="finalPrice">
                                0원
                            </strong>

                        </div>

                        <p class="summary-notice">
                            상품 가격과 재고는 주문 시점에 변경될 수 있습니다.
                        </p>

                        <button type="button"
                                id="checkoutBtn"
                                class="checkout-btn">

                            선택 상품 주문하기

                        </button>

                    </div>

                </aside>

            </div>

        </c:otherwise>

    </c:choose>

</main>

<jsp:include page="/WEB-INF/views/common/footer.jsp"/>

</body>

</html>