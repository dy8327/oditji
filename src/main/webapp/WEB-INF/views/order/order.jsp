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
                            <div class="no-image">NO IMAGE</div>
                        </c:when>

                        <c:otherwise>
                            <img src="${item.mainImage}" alt="${item.productName}">
                        </c:otherwise>

                    </c:choose>

                </div>

                <div class="order-info">

                    <h3>${item.productName}</h3>

                    <p class="order-item-business">${item.businessName}</p>

                    <p>수량: ${item.quantity}</p>

                    <p>
                        단가: ₩
                        <fmt:formatNumber value="${item.discountPrice}" pattern="#,###"/>
                    </p>

                </div>

                <div class="order-subtotal">
                    ₩ <fmt:formatNumber value="${item.itemTotalPrice}" pattern="#,###"/>
                </div>

            </article>

        </c:forEach>

    </section>

    <!-- ================= ORDER FORM ================= -->
    <section class="order-form">

        <h2>배송 정보</h2>

        <input type="text" id="receiver" placeholder="받는 사람"
               value="${defaultReceiverName}">

        <input type="text" id="phone" placeholder="연락처"
               value="${defaultReceiverPhone}">

        <input type="text" id="address" placeholder="배송지 주소">

    </section>

    <!-- ================= SUMMARY ================= -->
    <section class="order-summary">

        <div>
            <span>총 상품 금액</span>
            <span id="totalPrice">
                <fmt:formatNumber value="${totalPrice}" pattern="#,###"/>
            </span>
        </div>

        <div>
            <span>배송비</span>
            <span>0</span>
        </div>

        <div class="final">
            <span>최종 결제 금액</span>
            <span id="finalPrice">
                <fmt:formatNumber value="${totalPrice}" pattern="#,###"/>
            </span>
        </div>

        <p class="order-error" id="orderError" style="display:none;"></p>

        <button id="orderBtn" type="button">
            결제하기
        </button>

    </section>

</main>

<jsp:include page="/WEB-INF/views/common/footer.jsp"/>

<!--
    order.js가 아직 없어 주문 제출 로직을 이 화면에 인라인으로 작성했습니다.
    별도의 /js/order.js 파일로 분리하고 싶다면 아래 스크립트를 그대로 옮기면 됩니다.
-->
<script>
(function () {

    var contextPath = "${pageContext.request.contextPath}";
    var orderBtn = document.getElementById("orderBtn");
    var errorBox = document.getElementById("orderError");

    function showError(message) {
        errorBox.textContent = message;
        errorBox.style.display = "block";
    }

    orderBtn.addEventListener("click", function () {

        var receiverName = document.getElementById("receiver").value.trim();
        var receiverPhone = document.getElementById("phone").value.trim();
        var address = document.getElementById("address").value.trim();

        if (!receiverName || !receiverPhone || !address) {
            showError("받는 사람, 연락처, 배송지 주소를 모두 입력해주세요.");
            return;
        }

        orderBtn.disabled = true;

        fetch(contextPath + "/order/submit", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({
                receiverName: receiverName,
                receiverPhone: receiverPhone,
                address: address
            })
        })
            .then(function (response) { return response.json(); })
            .then(function (data) {

                if (data.success) {
                    location.href = contextPath + data.redirectUrl;
                    return;
                }

                showError(data.message || "주문 처리 중 오류가 발생했습니다.");
                orderBtn.disabled = false;
            })
            .catch(function () {
                showError("네트워크 오류가 발생했습니다.");
                orderBtn.disabled = false;
            });
    });

}());
</script>

</body>

</html>
