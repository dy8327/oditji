<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<% String contextPath = request.getContextPath(); %>
<!DOCTYPE html>
<html lang="ko">
<head>
<meta charset="UTF-8">
<title>ODITJI | 이벤트 승인 상태</title>
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
            <li><a class="" href="<%= contextPath %>/business/settlement/main">정산 관리</a></li>
            <li><a class="" href="<%= contextPath %>/business/event/list">이벤트 관리</a></li>
            <li><a class="active" href="<%= contextPath %>/business/approval/product">승인 관리</a></li>
            <li><a class="" href="<%= contextPath %>/business/chat/list">실시간 채팅</a></li>
        </ul>
    </aside>
    <main class="main-content">
        <a href="<%= contextPath %>/business/main" class="back-link">← 뒤로가기</a><h1 class="page-title">승인 상태</h1>
        <section class="content-panel"><nav class="tab-menu"><a href="<%= contextPath %>/business/approval/product">상품</a><a class="active" href="<%= contextPath %>/business/approval/event">이벤트</a></nav><div class="toolbar"><input class="page-search" type="text" placeholder="검색"><span class="page-subtitle">요청 유형</span></div><table class="data-table"><thead><tr><th>번호</th><th>이벤트명</th><th>이벤트 기간</th><th>상태</th><th>등록일</th><th>요청 유형</th><th>관리</th></tr></thead><tbody><tr><td>1</td><td>여름 굿즈 기획전</td><td>2026-07-01 ~ 2026-07-31</td><td><span class="status waiting">승인 대기</span></td><td>2026-06-20</td><td>등록 요청</td><td><a class="btn btn-dark" href="<%= contextPath %>/business/approval/event-detail">상세보기</a></td></tr></tbody></table><div class="pagination"><a href="#">‹</a><a class="active" href="#">1</a><a href="#">2</a><a href="#">3</a><a href="#">›</a></div></section>
    </main>
</div>
<footer class="footer">
    <span>ODITJI</span>
    <span>대전광역시 서구 대덕구로 182 오라클 빌딩 3층, 10층</span>
    <span>© ODITJI. All Rights Reserved.</span>
</footer>
</body>
</html>
