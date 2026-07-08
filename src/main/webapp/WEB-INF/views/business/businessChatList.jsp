<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<% String contextPath = request.getContextPath(); %>
<!DOCTYPE html>
<html lang="ko">
<head>
<meta charset="UTF-8">
<title>ODITJI | 실시간 채팅</title>
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
            <li><a class="" href="<%= contextPath %>/business/approval/product">승인 관리</a></li>
            <li><a class="active" href="<%= contextPath %>/business/chat/list">실시간 채팅</a></li>
        </ul>
    </aside>
    <main class="main-content">
        <a href="<%= contextPath %>/business/main" class="back-link">← 뒤로가기</a><h1 class="page-title">실시간 채팅</h1>
        <section class="chat-layout"><aside class="chat-list"><h2 class="page-title" style="font-size:20px;">대화 목록</h2><div class="chat-user active">사업자 A</div><div class="chat-user">사업자 B</div></aside><div class="chat-room"><div class="chat-message-area"><div class="bubble">안녕하세요. 계약 종료 작품 공유드립니다.</div><div class="bubble me">확인했습니다. 관련 상품은 판매 중지 처리하겠습니다.</div></div><form class="chat-input"><input type="text" placeholder="메시지를 입력하세요"><button class="btn btn-primary" type="submit">전송</button></form></div></section>
    </main>
</div>
<footer class="footer">
    <span>ODITJI</span>
    <span>대전광역시 서구 대덕구로 182 오라클 빌딩 3층, 10층</span>
    <span>© ODITJI. All Rights Reserved.</span>
</footer>
</body>
</html>
