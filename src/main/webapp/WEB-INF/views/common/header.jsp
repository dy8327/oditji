<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<c:set var="headerMemberNo"
       value="${not empty sessionScope.memberNo ? sessionScope.memberNo : sessionScope.loginMember.memberNo}" />
<c:set var="headerRole"
       value="${not empty sessionScope.role ? sessionScope.role : sessionScope.loginMember.role}" />
<c:set var="headerChatEnabled"
       value="${headerRole eq 'ADMIN' or (headerRole eq 'BUSINESS' and not empty sessionScope.businessNo)}" />

<link rel="stylesheet" href="${pageContext.request.contextPath}/css/layout.css">
<link rel="stylesheet" href="${pageContext.request.contextPath}/css/common.css?v=8">

<script src="https://cdn.jsdelivr.net/npm/sweetalert2@11"></script>
<script defer src="${pageContext.request.contextPath}/js/common.js?v=8"></script>
<meta name="viewport" content="width=device-width, initial-scale=1.0">

<a href="#mainContent" class="skip-link">본문 바로가기</a>

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

                        <%--
                            [수정] 카테고리별 상품은 자체 목록 페이지로 이동하지 않고
                            마우스를 올리거나 클릭했을 때 세부 카테고리를 표시합니다.
                            클릭하면 메뉴가 고정되고 ESC 또는 메뉴 바깥 클릭 시 닫힙니다.
                        --%>
                        <div class="header-category-menu" data-category-menu>
                            <button type="button"
                                    class="header-category-trigger"
                                    aria-expanded="false"
                                    aria-haspopup="true">
                                <span>카테고리별 상품</span>
                                <span class="header-category-arrow" aria-hidden="true">›</span>
                            </button>

                            <div class="header-category-submenu" role="menu" aria-label="상품 세부 카테고리">
                                <a href="${pageContext.request.contextPath}/goods/list?type=category&amp;productTypes=BOOK" role="menuitem">도서</a>
                                <a href="${pageContext.request.contextPath}/goods/list?type=category&amp;productTypes=CLOTHES" role="menuitem">의상</a>
                                <a href="${pageContext.request.contextPath}/goods/list?type=category&amp;productTypes=SHOES" role="menuitem">신발</a>
                                <a href="${pageContext.request.contextPath}/goods/list?type=category&amp;productTypes=OST" role="menuitem">OST</a>
                                <a href="${pageContext.request.contextPath}/goods/list?type=category&amp;productTypes=PROP" role="menuitem">소품</a>
                                <a href="${pageContext.request.contextPath}/goods/list?type=category&amp;productTypes=FIGURE" role="menuitem">피규어</a>
                                <a href="${pageContext.request.contextPath}/goods/list?type=category&amp;productTypes=POSTER" role="menuitem">포스터</a>
                                <a href="${pageContext.request.contextPath}/goods/list?type=category&amp;productTypes=GOODS" role="menuitem">굿즈</a>
                                <a href="${pageContext.request.contextPath}/goods/list?type=category&amp;productTypes=ETC" role="menuitem">기타</a>
                            </div>
                        </div>
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

        <%--
            768px 이하 모바일에서는 이 검색창이 기본적으로 숨겨져 있다가,
            header-search-toggle 버튼을 누르면 헤더 아래로 펼쳐지는 형태로 노출된다.
            (js/common.js의 initMobileSearchToggle 참고)
        --%>
        <div class="header-search" id="headerSearch">
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
            <%--
                모바일 전용 검색 토글 버튼.
                768px 이하에서만 아이콘으로 노출되며, 클릭 시 header-search(#headerSearch)를
                헤더 바로 아래에 펼쳐서 보여준다. 데스크톱에서는 CSS로 숨김 처리된다.
            --%>
            <button type="button"
                    class="icon-btn header-search-toggle"
                    id="headerSearchToggle"
                    aria-controls="headerSearch"
                    aria-expanded="false"
                    aria-label="검색 열기">
                <svg width="20" height="20" viewBox="0 0 24 24" fill="none">
                    <path d="M21 21L16.65 16.65" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                    <circle cx="11" cy="11" r="6" stroke="currentColor" stroke-width="2"/>
                </svg>
            </button>

            <c:choose>
                <c:when test="${empty sessionScope.loginMember}">
                    <a href="${pageContext.request.contextPath}/member/join" class="join-btn">회원가입</a>
                    <a href="${pageContext.request.contextPath}/member/login" class="login-btn">로그인</a>
                </c:when>

                <c:otherwise>
                    <%--
                        로그인 회원 모두에게 통합 알림 영역을 표시합니다.
                        일반 회원은 Oracle 업무 알림만 사용하고,
                        관리자와 사업자는 업무 알림과 채팅 알림을 함께 사용합니다.
                    --%>
                    <c:if test="${not empty headerMemberNo}">
                        <div class="notification-menu"
                         id="notificationMenu"
                         data-context-path="${pageContext.request.contextPath}"
                         data-member-no="${headerMemberNo}"
                         data-business-no="${sessionScope.businessNo}"
                         data-role="${headerRole}"
                         data-chat-enabled="${headerChatEnabled}">

                        <button type="button"
                                class="icon-btn notification-btn"
                                id="notificationBtn"
                                aria-label="알림 열기"
                                aria-controls="notificationDropdown"
                                aria-expanded="false">
                            <img src="${pageContext.request.contextPath}/images/alarm.svg"
                                 alt=""
                                 class="notification-icon"
                                 aria-hidden="true">
                            <span class="notification-badge"
                                  id="notificationBadge"
                                  aria-label="새 알림 0개"
                                  hidden></span>
                        </button>

                        <div class="notification-dropdown"
                             id="notificationDropdown"
                             aria-label="알림 상세">

                            <div class="notification-dropdown-header">
                                <strong>알림</strong>
                                <span id="notificationSummary">새 알림이 없습니다.</span>
                            </div>

                            <div class="notification-list"
                                 id="notificationList"
                                 aria-live="polite">
                                <div class="notification-empty">
                                    새로운 알림이 없습니다.
                                </div>
                            </div>

                            <%--
                                모두 읽음은 채팅의 마지막 읽음 위치와
                                업무 알림의 읽음 상태만 갱신하며 원본 데이터는 삭제하지 않습니다.
                            --%>
                            <div class="notification-dropdown-footer">
                                <button type="button"
                                        class="notification-clear-all-btn"
                                        id="notificationClearAllBtn"
                                        hidden>
                                    알림 모두 읽음
                                </button>
                            </div>
                        </div>
                    </div>
                    </c:if>

                    <%-- 일반 사용자 장바구니 --%>
                    <c:if test="${sessionScope.loginMember.role ne 'ADMIN' and empty sessionScope.businessNo}">
                        <a href="${pageContext.request.contextPath}/cart"
                           class="icon-btn"
                           title="장바구니"
                           aria-label="장바구니">🛒</a>
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

<%--
    맨 위로 이동 버튼.
    스크롤을 내리다가 헤더가 자동으로 숨겨지는 시점(js/common.js의 initHeaderScroll)에 맞춰
    우측 하단에 노출되며, 클릭 시 페이지 맨 위로 부드럽게 스크롤한다.
--%>
<button type="button"
        class="back-to-top-btn"
        id="backToTopBtn"
        aria-label="맨 위로 이동">
    <svg width="20" height="20" viewBox="0 0 24 24" fill="none">
        <path d="M12 19V5M12 5L5 12M12 5L19 12"
              stroke="currentColor"
              stroke-width="2"
              stroke-linecap="round"
              stroke-linejoin="round"/>
    </svg>
</button>

<c:if test="${not empty sessionScope.loginMember}">
    <script type="module"
            src="${pageContext.request.contextPath}/js/header-notification.js?v=5"></script>
</c:if>