<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<% String contextPath = request.getContextPath(); %>
<!DOCTYPE html>
<html lang="ko">
<head>
<meta charset="UTF-8">
<title>ODITJI | 정산 관리</title>
<link rel="stylesheet" href="<%= contextPath %>/css/business.css">
</head>
<body>

<header class="header">
    <div class="logo">
        <a href="<%= contextPath %>/">ODITJI</a>
    </div>

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
            <li>
                <a href="<%= contextPath %>/business/product/list">상품 관리</a>
            </li>
            <li>
                <a class="active" href="<%= contextPath %>/business/settlement/main">정산 관리</a>
            </li>
            <li>
                <a href="<%= contextPath %>/business/event/list">이벤트 관리</a>
            </li>
            <li>
                <a href="<%= contextPath %>/business/approval/product">승인 관리</a>
            </li>
            <li>
                <a href="<%= contextPath %>/business/chat/list">실시간 채팅</a>
            </li>
        </ul>
    </aside>

    <main class="main-content">

        <a href="<%= contextPath %>/business/main" class="back-link">← 뒤로가기</a>

        <h1 class="page-title">정산 관리</h1>

        <section class="content-panel">

            <nav class="tab-menu">
                <a class="active" href="<%= contextPath %>/business/settlement/main">정산 예정 금액</a>
                <a href="<%= contextPath %>/business/settlement/complete">정산 완료 금액</a>
                <a href="<%= contextPath %>/business/settlement/account">계좌 정보 관리</a>
            </nav>

            <div class="summary-box">
                <p>
                    <span class="summary-label">이번달 매출 :</span>
                    <span class="summary-value">1,240,000원</span>
                </p>

                <p>
                    <span class="summary-label">수수료 :</span>
                    <span class="summary-value fee">124,000원</span>
                </p>

                <p>
                    <span class="summary-label">정산 예정 금액 :</span>
                    <span class="summary-value point">1,116,000원</span>
                </p>

                <p>
                    <span class="summary-label">정산 예정일 :</span>
                    <span class="summary-value">2026-07-05</span>
                </p>
            </div>

            <div class="btn-row">
                <button class="btn btn-primary" type="button">정산 요청</button>
            </div>

            <div class="settlement-summary-grid">
                <div class="settlement-card">
                    <span>정산 상태</span>
                    <strong>요청 가능</strong>
                </div>

                <div class="settlement-card">
                    <span>이번달 주문 수</span>
                    <strong>126건</strong>
                </div>

                <div class="settlement-card">
                    <span>수수료율</span>
                    <strong>10%</strong>
                </div>
            </div>

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
