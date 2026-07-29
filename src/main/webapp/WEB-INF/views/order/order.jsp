<%@ page language="java"
    contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>

<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<!DOCTYPE html>
<html lang="ko">

<head>

    <meta charset="UTF-8">
    <title>ODITJI | 주문서</title>

    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/css/order.css">

</head>

<body>

<jsp:include page="/WEB-INF/views/common/header.jsp"/>

<main class="order-container">

    <h1>주문서</h1>

    <!-- ================= ORDER ITEMS ================= -->
    <section class="order-items">

        <c:forEach var="item" items="${orderItems}">

            <article class="order-item">

                <div class="order-img">

                    <c:choose>

                        <c:when test="${empty item.mainImage}">
                            <div class="no-image">
                                NO IMAGE
                            </div>
                        </c:when>

                        <c:otherwise>
                            <img src="${pageContext.request.contextPath}${item.mainImage}"
                                 alt="${item.productName}">
                        </c:otherwise>

                    </c:choose>

                </div>

                <div class="order-info">

                    <h3>
                        ${item.productName}
                    </h3>

                    <p class="order-item-business">
                        ${item.businessName}
                    </p>

                    <p>
                        수량: ${item.quantity}
                    </p>

                    <p>
                        단가: ₩
                        <fmt:formatNumber
                            value="${item.discountPrice}"
                            pattern="#,###"/>
                    </p>

                </div>

                <div class="order-subtotal">

                    ₩
                    <fmt:formatNumber
                        value="${item.itemTotalPrice}"
                        pattern="#,###"/>

                </div>

            </article>

        </c:forEach>

    </section>

    <!-- ================= ORDER FORM ================= -->
    <section class="order-form">

        <h2>배송 정보</h2>

        <%--
            받는 사람 입력창의 id와 label의 for를 연결하여
            입력 목적을 스크린 리더가 인식할 수 있도록 한다.
        --%>
        <label for="receiver"
               class="order-accessibility-label">
            받는 사람
        </label>

        <input type="text"
               id="receiver"
               placeholder="받는 사람"
               value="${defaultReceiverName}"
               maxlength="50"
              >

        <label for="phone"
               class="order-accessibility-label">
            연락처
        </label>

        <input type="text"
               id="phone"
               placeholder="연락처"
               value="${defaultReceiverPhone}"
               maxlength="20"
               >

        <div class="address-search-row">

            <label for="zipcode"
                   class="order-accessibility-label">
                우편번호
            </label>

            <input type="text"
                   id="zipcode"
                   placeholder="우편번호"
                   readonly>

            <button type="button"
                    id="addressSearchBtn"
                    class="address-search-btn">
                주소 검색
            </button>

        </div>

        <label for="address1"
               class="order-accessibility-label">
            기본 주소
        </label>

        <input type="text"
               id="address1"
               placeholder="기본 주소"
               readonly>

        <label for="address2"
               class="order-accessibility-label">
            상세 주소
        </label>

        <input type="text"
               id="address2"
               placeholder="상세 주소를 입력해주세요"
               maxlength="100"
               autocomplete="off">

    </section>

    <!-- ================= SUMMARY ================= -->
    <section class="order-summary">

        <div>

            <span>총 상품 금액</span>

            <span id="totalPrice">

                <fmt:formatNumber
                    value="${totalPrice}"
                    pattern="#,###"/>

            </span>

        </div>

        <div>

            <span>배송비</span>
            <span>0</span>

        </div>

        <div class="final">

            <span>최종 결제 금액</span>

            <span id="finalPrice">

                <fmt:formatNumber
                    value="${totalPrice}"
                    pattern="#,###"/>

            </span>

        </div>

        <p class="order-error"
           id="orderError"
           style="display:none;">
        </p>

        <button id="orderBtn"
                type="button">
            결제하기
        </button>

        <button id="cancelBtn"
                type="button">
            취소하기
        </button>

    </section>

</main>

<jsp:include page="/WEB-INF/views/common/footer.jsp"/>

<!-- 카카오 우편번호 서비스 -->
<script src="https://t1.kakaocdn.net/mapjsapi/bundle/postcode/prod/postcode.v2.js"></script>

<!-- PortOne V2 브라우저 SDK -->
<script src="https://cdn.portone.io/v2/browser-sdk.js"></script>

<script>

var contextPath =
    "${pageContext.request.contextPath}";

var customerEmail =
    "${defaultReceiverEmail}";

</script>

<script src="${pageContext.request.contextPath}/js/orderCheckout.js"></script>

</body>

</html>