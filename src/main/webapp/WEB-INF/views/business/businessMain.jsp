<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%
    // contextPath 변수를 선언하고 현재 프로젝트의 컨텍스트 경로를 가리킴
    String contextPath = request.getContextPath(); 
%>

<!DOCTYPE html>
<html lang="ko">
  <head>
    <meta charset="UTF-8" />
    <title>ODITJI | 사업자 페이지</title>
    <link rel="stylesheet" href="<%= contextPath %>/css/business.css">
  </head>

  <body>
    <!-- Header -->
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
        <input type="text" placeholder="작품, 배우, 굿즈 검색" />
        <span class="search-icon">⌕</span>
      </div>

      <div class="header-right">
        <span>사업자님</span>
        <a href="<%= contextPath %>/member/logout" class="logout-btn">로그아웃</a>
      </div>
    </header>

    <div class="business-wrap">
      <!-- Sidebar -->
      <aside class="sidebar">
        <h2 class="sidebar-title">사업자 메뉴</h2>

        <ul class="side-menu">
          <li>
            <a href="<%= contextPath %>/business/main" class="active">대시보드</a>
          </li>
          <li>
            <a href="<%= contextPath %>/business/product/list">상품 관리</a>
          </li>
          <li>
            <a href="<%= contextPath %>/business/settlement/list">정산 관리</a>
          </li>
          <li>
            <a href="<%= contextPath %>/business/event/list">이벤트 관리</a>
          </li>
          <li>
            <a href="<%= contextPath %>/business/approval/list">승인 관리</a>
          </li>
          <li>
            <a href="<%= contextPath %>/business/chat/list">실시간 채팅</a>
          </li>
        </ul>
      </aside>

      <!-- Main Content -->
      <main class="main-content">
        <section class="page-top">
          <div class="page-title-box">
            <p>ODITJI 사업자 전용 페이지</p>
            <h1>상품과 주문을<br />한눈에 관리하세요</h1>
          </div>

          <div class="quick-info">
            <div class="quick-card">
              <span>오늘 주문</span>
              <strong>12건</strong>
            </div>
            <div class="quick-card">
              <span>승인 대기</span>
              <strong>4건</strong>
            </div>
            <div class="quick-card">
              <span>이번 달 매출</span>
              <strong>1,240,000원</strong>
            </div>
          </div>
        </section>

        <section class="dashboard-box">
          <div class="dashboard-head">
            <div>
              <h2>사업자 대시보드</h2>
              <p>상품, 주문, 정산, 리뷰, 이벤트, 승인 현황을 확인할 수 있습니다.</p>
            </div>
          </div>

          <div class="stat-grid">
            <a href="<%= contextPath %>/business/product/list" class="stat-card">
              <div class="stat-card-title">전체 상품 수</div>
              <div class="stat-card-value">38</div>
              <div class="stat-card-desc">등록된 전체 상품</div>
            </a>

            <a href="<%= contextPath %>/business/order/list" class="stat-card">
              <div class="stat-card-title">주문 수</div>
              <div class="stat-card-value">126</div>
              <div class="stat-card-desc">누적 주문 건수</div>
            </a>

            <a href="<%= contextPath %>/business/sales/list" class="stat-card">
              <div class="stat-card-title">매출 내역</div>
              <div class="stat-card-value">₩</div>
              <div class="stat-card-desc">매출 및 판매 내역 확인</div>
            </a>

            <a href="<%= contextPath %>/business/review/list" class="stat-card">
              <div class="stat-card-title">리뷰 수</div>
              <div class="stat-card-value">84</div>
              <div class="stat-card-desc">상품 리뷰 관리</div>
            </a>

            <a href="<%= contextPath %>/business/event/list" class="stat-card">
              <div class="stat-card-title">이벤트 내역</div>
              <div class="stat-card-value">3</div>
              <div class="stat-card-desc">진행 중인 이벤트</div>
            </a>

            <a href="<%= contextPath %>/business/approval/list" class="stat-card">
              <div class="stat-card-title">승인 내역</div>
              <div class="stat-card-value">7</div>
              <div class="stat-card-desc">상품 승인 상태 확인</div>
            </a>
          </div>
        </section>

        <section class="bottom-section">
          <div class="panel">
            <h3>최근 주문 내역</h3>
            <ul class="mini-list">
              <li>
                <span>드라마 굿즈 키링</span>
                <strong class="status-ok">결제 완료</strong>
              </li>
              <li>
                <span>영화 속 후드티</span>
                <strong class="status-waiting">배송 준비중</strong>
              </li>
              <li>
                <span>배우 착용 모자</span>
                <strong class="status-ok">배송중</strong>
              </li>
              <li>
                <span>공식 포스터 세트</span>
                <strong class="status-danger">취소 요청</strong>
              </li>
            </ul>
          </div>

          <div class="panel">
            <h3>승인 상태</h3>
            <ul class="mini-list">
              <li>
                <span>승인 완료 상품</span>
                <strong class="status-ok">31건</strong>
              </li>
              <li>
                <span>승인 대기 상품</span>
                <strong class="status-waiting">4건</strong>
              </li>
              <li>
                <span>반려 상품</span>
                <strong class="status-danger">3건</strong>
              </li>
              <li>
                <span>판매 중지 상품</span>
                <strong>0건</strong>
              </li>
            </ul>
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
