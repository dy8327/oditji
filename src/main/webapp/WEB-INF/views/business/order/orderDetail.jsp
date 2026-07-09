<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

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

                    <a href="${pageContext.request.contextPath}/business/order/list" class="back-link">
                        ← 목록으로
                    </a>

                    <h1 class="business-page-title">주문 상세 정보</h1>

                </div>

                <section class="business-content-box">

                    <article class="item-card">
                        <div class="item-info">
                            <h3>주문 및 배송 정보</h3>
                            <div class="meta">
                                <span>수령인 : ${order.receiverName}</span>
                                <span>연락처 : ${order.receiverPhone}</span>
                                <span>주소 : ${order.address}</span>
                                <span>결제금액 : ${payment.paymentAmount}원</span>
                            </div>
                        </div>
                    </article>

                    <form action="${pageContext.request.contextPath}/business/delivery/update" method="post" class="toolbar">
                        <input type="hidden" name="orderItemNo" value="${item.orderItemNo}">
                        
                        <select name="status" class="page-search">
                            <option value="PREPARING" ${delivery.status == 'PREPARING' ? 'selected' : ''}>상품 준비 중</option>
                            <option value="SHIPPING" ${delivery.status == 'SHIPPING' ? 'selected' : ''}>배송 중</option>
                            <option value="DELIVERED" ${delivery.status == 'DELIVERED' ? 'selected' : ''}>배송 완료</option>
                        </select>

                        <input type="text" class="page-search" name="trackingNumber" 
                               placeholder="운송장 번호 입력" value="${delivery.trackingNumber}">
                        
                        <button type="submit" class="btn btn-dark">배송 정보 저장</button>
                    </form>

                </section>

            </main>

        </div>

        <jsp:include page="/WEB-INF/views/common/footer.jsp"/>

    </body>
</html>