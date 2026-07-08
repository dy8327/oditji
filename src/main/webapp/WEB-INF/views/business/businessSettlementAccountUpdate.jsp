<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<% String contextPath = request.getContextPath(); %>
<!DOCTYPE html>
<html lang="ko">
<head>
<meta charset="UTF-8">
<title>ODITJI | 계좌 정보 변경</title>
<link rel="stylesheet" href="<%= contextPath %>/css/business.css">
</head>
<body>
<div class="modal-page">
<section class="modal-box">
    <h1 class="modal-title">계좌 정보 변경</h1>
    <form action="#" method="post">
        <div class="form-group">
            <label class="form-label">은행명</label>
            <input class="form-input" type="text" name="bankName">
        </div>
        <div class="form-group">
            <label class="form-label">계좌 번호</label>
            <input class="form-input" type="text" name="accountNumber">
        </div>
        <div class="form-group">
            <label class="form-label">예금주</label>
            <input class="form-input" type="text" name="accountHolder">
        </div>
        <div class="btn-row" style="justify-content:center;margin-top:26px;">
            <button class="btn btn-primary" type="submit">수정하기</button>
            <button class="btn btn-dark" type="button" onclick="history.back();">닫기</button>
        </div>
    </form>
</section>
</div>
</body>
</html>