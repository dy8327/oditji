<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isELIgnored="false" %>
<% String contextPath = request.getContextPath(); %>
<!DOCTYPE html>
<html lang="ko">
<head>
<meta charset="UTF-8">
<title>ODITJI | 이벤트 등록 요청</title>
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
            <li><a class="active" href="<%= contextPath %>/business/event/list">이벤트 관리</a></li>
            <li><a class="" href="<%= contextPath %>/business/approval/product">승인 관리</a></li>
            <li><a class="" href="<%= contextPath %>/business/chat/list">실시간 채팅</a></li>
        </ul>
    </aside>
    <main class="main-content">
        <a href="<%= contextPath %>/business/main" class="back-link">← 뒤로가기</a>
        <section class="form-panel">
            <h1 class="form-title">이벤트 등록 요청</h1>
            <form action="#" method="post" enctype="multipart/form-data">
                <div class="form-group"><label class="form-label">이벤트명</label><input class="form-input" type="text" name="eventTitle"></div>
                <div class="form-group"><label class="form-label">이벤트 설명</label><textarea class="form-textarea" name="eventContent"></textarea></div>
                <div class="form-group"><label class="form-label">이벤트 기간</label><input class="form-input" type="text" name="eventPeriod" placeholder="2026-07-01 ~ 2026-07-31"></div>
                <div class="form-group"><label class="form-label">연결 상품</label><div class="input-with-btn"><input class="form-input" type="text" name="productName" readonly><button class="btn btn-dark" type="button">상품 검색</button></div></div>
                <div class="form-group"><label class="form-label">상태</label><div class="btn-row"><label><input type="checkbox" name="status" value="WAITING"> 예정</label><label><input type="checkbox" name="status" value="ACTIVE"> 진행중</label><label><input type="checkbox" name="status" value="ENDED"> 종료</label></div></div>
                <div class="form-group"><label class="form-label">이벤트 이미지</label><div class="file-box"><input type="file" name="eventImage"><span>선택된 파일 없음</span></div></div>
                <div class="submit-stack"><button class="btn btn-primary" type="submit">이벤트 등록 요청</button><a class="btn btn-dark" href="<%= contextPath %>/business/event/list">취소</a></div>
            </form>
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
