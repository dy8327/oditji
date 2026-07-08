<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<% String contextPath = request.getContextPath(); %>
<!DOCTYPE html>
<html lang="ko">
<head>
<meta charset="UTF-8">
<title>ODITJI | 정산 예정 금액</title>
<link rel="stylesheet" href="<%= contextPath %>/css/business.css">
</head>
<body>
<header class="header">
    <div class="logo"><a href="<%= contextPath %>/">ODITJI</a></div>
    <nav class="nav-menu">
        <a href="<%= contextPath %>/">홈</a>
        <a href="<%= contextPath %>/content/list">영화 · 시리즈</a>
        <a href="<%= contextPath %>/ranking">인기</a>
        <a href="<%= contextPath %>/new">신규</a>
        <a href="<%= contextPath %>/product/list">상품</a>
    </nav>
    <div class="search-box">
        <input type="text" placeholder="작품, 배우, 굿즈 검색">
        <span class="search-icon">⌕</span>
    </div>
    <div class="header-right">
        <span>사업자님</span>
        <a href="<%= contextPath %>/member/logout" class="logout-btn">로그아웃</a>
    </div>
</header>
<div class="business-wrap">
    <aside class="sidebar">
        <h2 class="sidebar-title">사업자 메뉴</h2>
        <ul class="side-menu">
            <li><a class="" href="<%= contextPath %>/business/product/list">상품 관리</a></li>
            <li><a class="active" href="<%= contextPath %>/business/settlement/main">정산 관리</a></li>
            <li><a class="" href="<%= contextPath %>/business/event/list">이벤트 관리</a></li>
            <li><a class="" href="<%= contextPath %>/business/approval/product">승인 관리</a></li>
            <li><a class="" href="<%= contextPath %>/business/chat/list">실시간 채팅</a></li>
        </ul>
    </aside>
    <main class="main-content">
        <a href="<%= contextPath %>/business/main" class="back-link">← 뒤로가기</a>
        <h1 class="page-title">정산 관리</h1>
        <section class="content-panel">
            <nav class="tab-menu"><a class="active" href="<%= contextPath %>/business/settlement/expected">정산 예정 금액</a><a href="<%= contextPath %>/business/settlement/complete">정산 완료 금액</a><a href="<%= contextPath %>/business/settlement/account">계좌 정보 관리</a></nav>
            <p class="page-subtitle">기간 선택 2026-06-01 ~ 2026-06-30 [조회]</p>
            <div class="summary-box"><p>누적 정산 금액 : 2,480,000원</p><p>선택 기간 정산 금액 : 1,116,000원</p></div>
            <table class="data-table"><thead><tr><th>정산일</th><th>정산 금액</th><th>수수료</th><th>상태</th></tr></thead><tbody><tr><td>2026-07-05</td><td>1,116,000원</td><td>124,000원</td><td><span class="status waiting">예정</span></td></tr><tr><td>2026-06-05</td><td>860,000원</td><td>95,000원</td><td><span class="status ok">완료</span></td></tr></tbody></table>
        </section>
    </main>
</div>
<footer class="footer">
    <span>ODITJI</span>
    <span>대전광역시 서구 대덕구로 182 오라클 빌딩 3층, 10층</span>
    <span>© ODITJI. All Rights Reserved.</span>
</footer>
</body>
</html>
