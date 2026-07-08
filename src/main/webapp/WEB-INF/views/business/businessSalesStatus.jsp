<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<% String contextPath = request.getContextPath(); %>
<!DOCTYPE html>
<html lang="ko">
<head>
<meta charset="UTF-8">
<title>ODITJI | 판매 현황</title>
<link rel="stylesheet" href="<%= contextPath %>/css/business.css">
</head>
<body>
<div class="modal-page">
    <section class="modal-box">
        <h1 class="modal-title">판매 현황</h1>
        <div class="info-list">
            <div class="info-row"><strong>일일 매출 :</strong><span>320,000원</span></div>
            <div class="info-row"><strong>판매 상품 :</strong><span>영화 속 공식 후드티</span></div>
            <div class="info-row"><strong>주문 건수 :</strong><span>12건</span></div>
        </div>
            <p class="page-subtitle">기간 선택 2026-06-01 ~ 2026-06-30 [조회]</p>
            <h2 class="page-title" style="font-size:20px;">상품 판매 내역</h2>
            <table class="data-table">
                <thead>
                    <tr><th>날짜</th><th>판매 수량</th><th>매출액</th></tr>
                </thead>
                <tbody>
                    <tr><td>2026-06-01</td><td>4</td><td>196,000원</td></tr>
                    <tr><td>2026-06-02</td><td>2</td><td>98,000원</td></tr>
                </tbody>
                </table>
                    <div class="submit-stack">
                        <button class="btn btn-dark" type="button" onclick="history.back();">닫기</button>
                    </div>
    </section>
</div>
</body>
</html>