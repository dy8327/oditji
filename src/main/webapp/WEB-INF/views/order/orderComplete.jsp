<%@ page language="java" contentType="text/html; charset=UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

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
                <strong>${order.orderId}</strong>
            </div>

            <div>
                <span>받는 사람</span>
                <strong>${order.receiver}</strong>
            </div>

            <div>
                <span>배송지</span>
                <strong>${order.address}</strong>
            </div>

            <div>
                <span>결제금액</span>
                <strong>₩ ${order.totalPrice}</strong>
            </div>

        </div>

        <!-- ACTION -->
        <div class="complete-actions">

            <a href="${pageContext.request.contextPath}/order/list">
                주문 내역 보기
            </a>

            <a href="${pageContext.request.contextPath}/content/list">
                쇼핑 계속하기
            </a>

        </div>

    </section>

</main>

<jsp:include page="/WEB-INF/views/common/footer.jsp"/>

</body>

</html>