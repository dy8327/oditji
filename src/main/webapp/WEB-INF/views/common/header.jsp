<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<link rel="stylesheet" href="${pageContext.request.contextPath}/css/layout.css">
<link rel="stylesheet" href="${pageContext.request.contextPath}/css/common.css?v=6">
<script defer src="${pageContext.request.contextPath}/js/common.js?v=6"></script>

<header class="header">
    <div class="header-container">
        <div class="header-left">
            <a href="${pageContext.request.contextPath}/" class="logo">
                <span class="logo-main">ODITJI</span>
            </a>
        </div>

        <button type="button"
                class="header-nav-toggle"
                id="headerNavToggle"
                aria-controls="headerNav"
                aria-expanded="false"
                aria-label="전체 메뉴 열기">
            <span></span>
            <span></span>
            <span></span>
        </button>

        <nav class="header-nav" id="headerNav" aria-label="주요 메뉴">
            <ul class="header-nav-list">
                <li class="header-nav-item header-nav-dropdown" data-nav-dropdown>
                    <button type="button"
                            class="header-nav-link header-nav-trigger"
                            aria-expanded="false">
                        콘텐츠
                        <span class="header-nav-arrow" aria-hidden="true">⌄</span>
                    </button>

                    <div class="header-submenu" role="menu">
                        <div class="header-submenu-heading">콘텐츠 탐색</div>
                        <a href="${pageContext.request.contextPath}/content/list?type=all" role="menuitem">영화·시리즈</a>
                        <a href="${pageContext.request.contextPath}/content/list?type=popular" role="menuitem">인기</a>
                        <a href="${pageContext.request.contextPath}/content/list?type=new" role="menuitem">신규</a>
                        <a href="${pageContext.request.contextPath}/ranking" role="menuitem">랭킹</a>
                        <a href="${pageContext.request.contextPath}/content/today" role="menuitem">오늘의 콘텐츠</a>
                        <a href="${pageContext.request.contextPath}/recommend" role="menuitem">추천 콘텐츠</a>
                    </div>
                </li>

                <li class="header-nav-item header-nav-dropdown" data-nav-dropdown>
                    <button type="button"
                            class="header-nav-link header-nav-trigger"
                            aria-expanded="false">
                        상품
                        <span class="header-nav-arrow" aria-hidden="true">⌄</span>
                    </button>

                    <div class="header-submenu" role="menu">
                        <div class="header-submenu-heading">상품 탐색</div>
                        <a href="${pageContext.request.contextPath}/goods/list?type=all" role="menuitem">전체 상품</a>
                        <a href="${pageContext.request.contextPath}/goods/list?type=popular" role="menuitem">인기 상품</a>
                        <a href="${pageContext.request.contextPath}/goods/list?type=category#goods-category-filter" role="menuitem">카테고리별 상품</a>
                    </div>
                </li>

                <li class="header-nav-item header-nav-dropdown" data-nav-dropdown>
                    <button type="button"
                            class="header-nav-link header-nav-trigger"
                            aria-expanded="false">
                        이벤트
                        <span class="header-nav-arrow" aria-hidden="true">⌄</span>
                    </button>

                    <div class="header-submenu" role="menu">
                        <div class="header-submenu-heading">할인 이벤트</div>
                        <a href="${pageContext.request.contextPath}/event/list?period=ongoing" role="menuitem">진행 중</a>
                        <a href="${pageContext.request.contextPath}/event/list?period=upcoming" role="menuitem">예정</a>
                        <a href="${pageContext.request.contextPath}/event/list?period=ended" role="menuitem">종료</a>
                    </div>
                </li>
            </ul>
        </nav>

        <div class="header-search">
            <form id="headerSearchForm" action="${pageContext.request.contextPath}/search" method="get">
                <%--
                    공통 검색창에 고유 id와 label을 연결하여
                    검색 입력 목적을 보조 기술에 제공한다.
                --%>
                <label for="headerSearchKeyword"
                       style="position:absolute;
                              width:1px;
                              height:1px;
                              padding:0;
                              margin:-1px;
                              overflow:hidden;
                              clip:rect(0, 0, 0, 0);
                              white-space:nowrap;
                              border:0;">
                    통합 검색어
                </label>

                <input type="text"
                       id="headerSearchKeyword"
                       name="keyword"
                       value="<c:out value='${keyword}'/>"
                       placeholder="작품, 배우, 감독, 상품 검색"
                       autocomplete="off">
                <button type="submit" aria-label="검색">
                    <svg width="18" height="18" viewBox="0 0 24 24" fill="none">
                        <path d="M21 21L16.65 16.65" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                        <circle cx="11" cy="11" r="6" stroke="currentColor" stroke-width="2"/>
                    </svg>
                </button>
            </form>
        </div>
        <div class="header-right">
            <c:choose>
                <c:when test="${empty sessionScope.loginMember}">
                    <a href="${pageContext.request.contextPath}/member/join" class="join-btn">회원가입</a>
                    <a href="${pageContext.request.contextPath}/member/login" class="login-btn">로그인</a>
                </c:when>
                <c:otherwise>
                    <%-- 일반 사용자 장바구니 --%>
                    <c:if test="${sessionScope.loginMember.role ne 'ADMIN' and empty sessionScope.businessNo}">
                        <a href="${pageContext.request.contextPath}/cart" class="icon-btn" title="장바구니" aria-label="장바구니">🛒</a>
                    </c:if>

                    <div class="profile-menu">
                        <button class="profile-btn" id="profileBtn" type="button" aria-expanded="false">
                            👤 <span>${sessionScope.loginDisplayName}</span>
                        </button>

                        <div class="profile-dropdown">
                            <c:choose>
                                <%-- 관리자 메뉴 --%>
                                <c:when test="${sessionScope.loginMember.role eq 'ADMIN'}">
                                    <a href="${pageContext.request.contextPath}/admin/main">사이트 관리</a>
                                </c:when>

                                <%-- 사업자 메뉴 --%>
                                <c:when test="${not empty sessionScope.businessNo}">
                                    <a href="${pageContext.request.contextPath}/business/main">대시보드</a>
                                    <a href="${pageContext.request.contextPath}/business/product/list">상품관리</a>
                                    <a href="${pageContext.request.contextPath}/business/order/list">주문관리</a>
                                </c:when>

                                <%-- 일반 사용자 메뉴 --%>
                                <c:otherwise>
                                    <a href="${pageContext.request.contextPath}/member/mypage">마이페이지</a>
                                    <a href="${pageContext.request.contextPath}/favorite/list">찜 목록</a>
                                    <a href="${pageContext.request.contextPath}/cart">장바구니</a>
                                    <a href="${pageContext.request.contextPath}/order/list">주문 내역</a>
                                    <a href="${pageContext.request.contextPath}/review/myReviewList">내 리뷰</a>
                                </c:otherwise>
                            </c:choose>

                            <hr>
                            <a href="${pageContext.request.contextPath}/member/logout" class="logout-link">로그아웃</a>
                        </div>
                    </div>
                </c:otherwise>
            </c:choose>
        </div>
        
    </div>
</header>
