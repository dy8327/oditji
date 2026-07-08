<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<% String contextPath = request.getContextPath(); %>
<!DOCTYPE html>
<html lang="ko">
<head>
<meta charset="UTF-8">
<title>ODITJI | 상품 승인 상태</title>
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
        <section class="content-panel"><nav class="tab-menu"><a class="active" href="<%= contextPath %>/business/approval/product">상품</a><a href="<%= contextPath %>/business/approval/event">이벤트</a></nav><div class="toolbar"><input class="page-search" type="text" placeholder="검색"><span class="page-subtitle">요청 유형</span></div><div class="card-list"><article class="item-card"><div class="thumb">상품</div><div class="item-info"><h3>영화 속 공식 후드티</h3><div class="meta"><span>관련 콘텐츠: 콘텐츠명</span><span>수량: 35개</span><span>가격: 49,000원</span><span class="status waiting">요청 유형: 등록 요청</span></div></div><div class="btn-row item-actions"><a class="btn btn-dark" href="<%= contextPath %>/business/approval/product-detail">상세보기</a><button class="btn btn-danger" type="button">요청 취소</button><button class="btn btn-outline" type="button">승인 상태</button></div></article></div><div class="pagination"><a href="#">‹</a><a class="active" href="#">1</a><a href="#">2</a><a href="#">3</a><a href="#">›</a></div></section>
    </main>
</div>
<footer class="footer">
    <span>ODITJI</span>
    <span>대전광역시 서구 대덕구로 182 오라클 빌딩 3층, 10층</span>
    <span>© ODITJI. All Rights Reserved.</span>
</footer>
</body>
</html>
