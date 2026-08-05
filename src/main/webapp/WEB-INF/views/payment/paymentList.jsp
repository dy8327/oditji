<%@ page language="java"
    contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>

<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="dt" uri="http://oditji.com/functions/datetime" %>

<!DOCTYPE html>
<html lang="ko">

<head>

    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>ODITJI | 결제 내역</title>

    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/payment.css">

</head>

<body>

<div class="container">

    <h2>결제 내역</h2>

    <a class="action-link"
       href="${pageContext.request.contextPath}/goods/list">
        상품 보러 가기
    </a>

    <c:choose>

        <c:when test="${empty paymentList}">

            <div class="empty">
                저장된 결제 내역이 없습니다.
            </div>

        </c:when>

        <c:otherwise>

            <table>

                <thead>

                    <tr>
                        <th>결제번호</th>
                        <th>주문번호</th>
                        <th>결제 ID</th>
                        <th>주문명</th>
                        <th>금액</th>
                        <th>상태</th>
                        <th>결제수단</th>
                        <th>PG</th>
                        <th>PG 거래번호</th>
                        <th>결제일시</th>
                        <th>저장일시</th>
                    </tr>

                </thead>

                <tbody>

                    <c:forEach var="payment"
                               items="${paymentList}">

                        <tr>

                            <td>
                                ${payment.paymentNo}
                            </td>

                            <td>
                                ${payment.orderNo}
                            </td>

                            <td class="long-text">
                                ${payment.paymentId}
                            </td>

                            <td>
                                ${payment.orderName}
                            </td>

                            <td>
                                ₩
                                <fmt:formatNumber
                                    value="${payment.paymentAmount}"
                                    pattern="#,###"/>
                            </td>

                            <td>

                                <span class="${payment.paymentStatus eq 'PAID'
                                    ? 'status-paid'
                                    : 'status-canceled'}">

                                    ${payment.paymentStatus}

                                </span>

                            </td>

                            <td>
                                ${payment.payMethod}
                            </td>

                            <td>
                                ${payment.pgProvider}
                            </td>

                            <td class="long-text">

                                <c:choose>

                                    <c:when test="${empty payment.pgTxId}">
                                        -
                                    </c:when>

                                    <c:otherwise>
                                        ${payment.pgTxId}
                                    </c:otherwise>

                                </c:choose>

                            </td>

                            <td>

                                <c:choose>

                                    <c:when test="${empty payment.paidAt}">
                                        -
                                    </c:when>

                                    <c:otherwise>
                                        ${payment.paidAt}
                                    </c:otherwise>

                                </c:choose>

                            </td>

                            <td>

                                ${dt:format(payment.createdAt, 'yyyy-MM-dd HH:mm:ss')}

                            </td>

                        </tr>

                    </c:forEach>

                </tbody>

            </table>

        </c:otherwise>

    </c:choose>

</div>

</body>

</html>