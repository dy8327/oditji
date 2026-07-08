<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="com.project.oditji.payment.vo.PaymentTestVO" %>

<%
    List<PaymentTestVO> paymentList =
            (List<PaymentTestVO>) request.getAttribute("paymentList");
%>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>결제 테스트 내역</title>

    <style>
        body {
            font-family: Arial, sans-serif;
            background: #f5f6f8;
            margin: 0;
            padding: 40px;
        }

        .container {
            max-width: 1250px;
            margin: 0 auto;
            background: #fff;
            border-radius: 16px;
            padding: 30px;
            box-shadow: 0 8px 24px rgba(0, 0, 0, 0.08);
        }

        h2 {
            margin-top: 0;
        }

        table {
            width: 100%;
            border-collapse: collapse;
            margin-top: 24px;
            font-size: 13px;
        }

        th, td {
            border-bottom: 1px solid #eee;
            padding: 10px;
            text-align: left;
            vertical-align: middle;
        }

        th {
            background: #fafafa;
        }

        .status-paid {
            font-weight: bold;
            color: #0a7d32;
        }

        .status-canceled {
            font-weight: bold;
            color: #c0392b;
        }

        .empty {
            padding: 40px;
            text-align: center;
            color: #777;
        }

        a {
            display: inline-block;
            margin-top: 20px;
            padding: 10px 14px;
            border-radius: 8px;
            background: #111;
            color: #fff;
            text-decoration: none;
        }

        .cancel-form {
            margin: 0;
        }

        .cancel-btn {
            padding: 7px 10px;
            border: none;
            border-radius: 6px;
            background: #c0392b;
            color: #fff;
            cursor: pointer;
            font-size: 12px;
        }

        .cancel-btn:hover {
            background: #a93226;
        }

        .disabled-text {
            color: #999;
            font-size: 12px;
        }

        .long-text {
            max-width: 180px;
            word-break: break-all;
        }
    </style>

    <script>
        function confirmCancel() {
            return confirm("이 결제를 취소하시겠습니까?");
        }
    </script>
</head>
<body>

<div class="container">
    <h2>결제 테스트 내역</h2>

    <a href="<%= request.getContextPath() %>/payment/test">결제 테스트하기</a>

    <%
        if (paymentList == null || paymentList.isEmpty()) {
    %>
        <div class="empty">
            저장된 결제 내역이 없습니다.
        </div>
    <%
        } else {
    %>
        <table>
            <thead>
            <tr>
                <th>번호</th>
                <th>결제 ID</th>
                <th>상품명</th>
                <th>금액</th>
                <th>상태</th>
                <th>결제수단</th>
                <th>PG</th>
                <th>PG 거래번호</th>
                <th>결제일시</th>
                <th>취소일시</th>
                <th>취소사유</th>
                <th>저장일시</th>
                <th>관리</th>
            </tr>
            </thead>
            <tbody>
            <%
                for (PaymentTestVO payment : paymentList) {
                    String status = payment.getPaymentStatus();
                    String statusClass = "PAID".equals(status) ? "status-paid" : "status-canceled";
            %>
                <tr>
                    <td><%= payment.getPaymentNo() %></td>
                    <td class="long-text"><%= payment.getPaymentId() %></td>
                    <td><%= payment.getOrderName() %></td>
                    <td><%= payment.getPaymentAmount() %>원</td>
                    <td class="<%= statusClass %>"><%= payment.getPaymentStatus() %></td>
                    <td><%= payment.getPayMethod() %></td>
                    <td><%= payment.getPgProvider() %></td>
                    <td class="long-text"><%= payment.getPgTxId() %></td>
                    <td><%= payment.getPaidAt() %></td>
                    <td><%= payment.getCanceledAt() == null ? "-" : payment.getCanceledAt() %></td>
                    <td><%= payment.getCancelReason() == null ? "-" : payment.getCancelReason() %></td>
                    <td><%= payment.getCreatedAt() %></td>
                    <td>
                        <%
                            if ("PAID".equals(payment.getPaymentStatus())) {
                        %>
                            <form class="cancel-form"
                                  action="<%= request.getContextPath() %>/payment/cancel"
                                  method="post"
                                  onsubmit="return confirmCancel();">
                                <input type="hidden" name="paymentId" value="<%= payment.getPaymentId() %>">
                                <input type="hidden" name="reason" value="테스트 결제 취소">
                                <button type="submit" class="cancel-btn">취소</button>
                            </form>
                        <%
                            } else {
                        %>
                            <span class="disabled-text">처리완료</span>
                        <%
                            }
                        %>
                    </td>
                </tr>
            <%
                }
            %>
            </tbody>
        </table>
    <%
        }
    %>
</div>

</body>
</html>