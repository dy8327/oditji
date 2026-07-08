<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<% String contextPath = request.getContextPath(); %>
<!DOCTYPE html>
<html lang="ko">
<head>
<meta charset="UTF-8">
<title>ODITJI | 상품 관리</title>
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
                <a href="<%= contextPath %>/business/product/list" class="active">상품 관리</a>
            </li>
            <li>
                <a href="<%= contextPath %>/business/settlement/main">정산 관리</a>
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

        <div class="business-page-header">
            <a href="<%= contextPath %>/business/main" class="back-link">← 뒤로가기</a>
            <h1 class="business-page-title">상품 관리</h1>
            <p class="business-page-desc">등록한 상품을 조회하고 등록, 수정, 삭제, 판매 현황, 승인 상태를 확인할 수 있습니다.</p>
        </div>

        <section class="business-content-box">

            <div class="product-control-row">

                <div></div>

                <form action="<%= contextPath %>/business/product/list" method="get" class="product-search-form">
                    <input type="text" name="keyword" placeholder="상품명, 관련 콘텐츠, 배우명 검색">
                    <button type="submit">검색</button>
                </form>

                <button type="button" class="product-register-btn"
                    onclick="location.href='<%= contextPath %>/business/product/register'">
                    등록 요청
                </button>

            </div>

            <div class="product-list-frame">

                <div class="product-item">

                    <div class="product-thumb">
                        상품
                    </div>

                    <div class="product-info">
                        <div class="product-type">의상</div>
                        <div class="product-name">영화 속 공식 후드티</div>

                        <div class="product-meta">
                            <span>관련 콘텐츠: 콘텐츠명</span>
                            <span>수량: 35개</span>
                            <span>가격: 49,000원</span>
                        </div>

                        <div class="product-status-text">
                            승인 상태:
                            <span class="status-ok">승인 완료</span>
                        </div>
                    </div>

                    <div class="product-actions">
                        <button type="button" class="product-action-btn"
                            onclick="location.href='<%= contextPath %>/business/product/update'">
                            수정 요청
                        </button>

                        <button type="button" class="product-action-btn delete">
                            삭제 요청
                        </button>

                        <button type="button" class="product-action-btn"
                            onclick="location.href='<%= contextPath %>/business/sales/status'">
                            판매 현황
                        </button>

                        <button type="button" class="product-action-btn"
                            onclick="location.href='<%= contextPath %>/business/approval/product/detail'">
                            승인 상태
                        </button>
                    </div>

                </div>

                <div class="product-item">

                    <div class="product-thumb">
                        상품
                    </div>

                    <div class="product-info">
                        <div class="product-type">굿즈</div>
                        <div class="product-name">드라마 굿즈 키링</div>

                        <div class="product-meta">
                            <span>관련 콘텐츠: 콘텐츠명</span>
                            <span>수량: 18개</span>
                            <span>가격: 12,000원</span>
                        </div>

                        <div class="product-status-text">
                            승인 상태:
                            <span class="status-waiting">승인 대기</span>
                        </div>
                    </div>

                    <div class="product-actions">
                        <button type="button" class="product-action-btn"
                            onclick="location.href='<%= contextPath %>/business/product/update'">
                            수정 요청
                        </button>

                        <button type="button" class="product-action-btn delete">
                            삭제 요청
                        </button>

                        <button type="button" class="product-action-btn"
                            onclick="location.href='<%= contextPath %>/business/sales/status'">
                            판매 현황
                        </button>

                        <button type="button" class="product-action-btn"
                            onclick="location.href='<%= contextPath %>/business/approval/product/detail'">
                            승인 상태
                        </button>
                    </div>

                </div>

                <div class="pagination">
                    <a href="#">&lt;</a>
                    <a href="#" class="active">1</a>
                    <a href="#">2</a>
                    <a href="#">3</a>
                    <a href="#">&gt;</a>
                    <a href="#">&gt;&gt;</a>
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
