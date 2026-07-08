<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>

<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<link rel="stylesheet"
      href="${pageContext.request.contextPath}/css/layout.css">

<link rel="stylesheet"
      href="${pageContext.request.contextPath}/css/common.css">

<script defer src="${pageContext.request.contextPath}/js/common.js"></script>

<header class="header">

    <div class="header-container">

        <!-- Logo -->
        <div class="header-left">
            <a href="${pageContext.request.contextPath}/" class="logo">
                <span class="logo-main">ODITJI</span>
            </a>
        </div>

        <!-- Navigation -->
        <nav class="header-nav">
            <ul>
                <li><a href="${pageContext.request.contextPath}/">홈</a></li>
                <li><a href="${pageContext.request.contextPath}/content/list?type=all">영화 · 시리즈</a></li>
                <li><a href="${pageContext.request.contextPath}/content/list?type=popular">인기</a></li>
                <li><a href="${pageContext.request.contextPath}/content/list?type=new">신규</a></li>
                <li><a href="${pageContext.request.contextPath}/goods/list">상품</a></li>
            </ul>
        </nav>

        <!-- Search -->
        <div class="header-search">

            <form action="${pageContext.request.contextPath}/search" method="get">

                <input
                    type="text"
                    name="keyword"
                    placeholder="작품, 배우, 굿즈 검색"
                    autocomplete="off">

                <button type="submit">

                    <svg width="18"
                         height="18"
                         viewBox="0 0 24 24"
                         fill="none">

                        <path
                            d="M21 21L16.65 16.65"
                            stroke="currentColor"
                            stroke-width="2"
                            stroke-linecap="round"/>

                        <circle
                            cx="11"
                            cy="11"
                            r="6"
                            stroke="currentColor"
                            stroke-width="2"/>

                    </svg>

                </button>

            </form>

        </div>

        <div class="header-right">

            <c:choose>

                <c:when test="${empty sessionScope.loginMember}">

                    <a href="${pageContext.request.contextPath}/member/join"
                       class="join-btn">
                        회원가입
                    </a>

                    <a href="${pageContext.request.contextPath}/member/login"
                       class="login-btn">
                        로그인
                    </a>

                </c:when>

                <c:otherwise>

                    <a href="${pageContext.request.contextPath}/cart"
                       class="icon-btn"
                       title="장바구니">
                        🛒
                    </a>

                    <c:if test="${sessionScope.loginMember.role eq 'ADMIN'}">

                        <a href="${pageContext.request.contextPath}/admin/main"
                           class="icon-btn"
                           title="관리자">
                            ⚙
                        </a>

                    </c:if>

                    <div class="profile-menu">

                        <button class="profile-btn" id="profileBtn">

                            👤

                            <span>
                                ${sessionScope.loginMember.nickname}
                            </span>

                        </button>

                        <div class="profile-dropdown">

                            <a href="${pageContext.request.contextPath}/member/mypage">
                                마이페이지
                            </a>

                            <a href="${pageContext.request.contextPath}/favorite/list">
                                찜 목록
                            </a>

                            <a href="${pageContext.request.contextPath}/cart">
                                장바구니
                            </a>

                            <a href="${pageContext.request.contextPath}/order/list">
                                주문 내역
                            </a>

                            <a href="${pageContext.request.contextPath}/review/my">
                                내 리뷰
                            </a>

                            <hr>

                            <a href="${pageContext.request.contextPath}/member/logout"
                               class="logout-link">
                                로그아웃
                            </a>

                        </div>

                    </div>

                </c:otherwise>

            </c:choose>

        </div>

    </div>

</header>