<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<c:set var="headerMemberNo"
       value="${not empty sessionScope.memberNo ? sessionScope.memberNo : sessionScope.loginMember.memberNo}" />
<c:set var="headerRole"
       value="${not empty sessionScope.role ? sessionScope.role : sessionScope.loginMember.role}" />
<c:set var="headerChatEnabled"
       value="${headerRole eq 'ADMIN' or (headerRole eq 'BUSINESS' and not empty sessionScope.businessNo)}" />

<%-- CSS/공통 스크립트/CSRF meta/viewport는 head-assets.jsp로 분리되어 있습니다.
     웹표준(HTML) 준수를 위해 이 조각은 <head> 안에서만 include해야 하므로,
     header.jsp(<body> 안에서 include됨)에서는 더 이상 include하지 않습니다.
     header.jsp를 include하는 모든 JSP는 반드시 자신의 <head> 안에서
     head-assets.jsp를 먼저 include해야 하며, 그렇지 않으면 공통 CSS/JS와
     CSRF meta가 로드되지 않아 화면/기능이 깨집니다.
     (embed 모드 페이지에서 header.jsp 전체를 생략하는 경우도 head-assets.jsp만은
     반드시 <head>에서 별도로 include해야 합니다.) --%>

<a href="#mainContent" class="skip-link">본문 바로가기</a>

<header class="header">
    <div class="header-container">
        <div class="header-left">
            <%-- [로고 이미지 적용] 기존 메인 이동 링크 로직은 그대로 유지하고,
                 테마에 따라 글자색이 다른 투명 PNG 로고만 교체해서 보여줍니다.
                 실제 테마 전환은 기존 theme-toggle.js의 html[data-theme] 값을 그대로 사용합니다. --%>
            <a href="${pageContext.request.contextPath}/"
               class="logo"
               aria-label="ODITJI 메인 페이지로 이동">
                <img src="${pageContext.request.contextPath}/images/oditji-logo-dark.png"
                     class="logo-image logo-image--dark"
                     alt=""
                     aria-hidden="true">
                <img src="${pageContext.request.contextPath}/images/oditji-logo-light.png"
                     class="logo-image logo-image--light"
                     alt=""
                     aria-hidden="true">
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

        <%--
            라이트/다크 모드 토글 버튼.
            기본은 다크 모드이며, 클릭 시 js/theme-toggle.js가
            <html data-theme="..."> 값을 바꾸고 localStorage에 저장합니다.
            버튼 안에는 달/해 아이콘을 모두 넣어두고 light-mode.css가
            현재 테마에 맞는 아이콘만 보여줍니다.
            [수정] header-container의 직계 자식으로 옮겨, 모바일 반응형에서
            header-nav-toggle(햄버거 메뉴 버튼) 옆에 order로 붙일 수 있게 했다.
            데스크톱(992px 초과)에서는 기존처럼 header-right 옆(오른쪽 끝)에
            그대로 보이도록 common.css에서 order를 맞춘다.
        --%>
        <button type="button"
                class="icon-btn theme-toggle-btn"
                id="themeToggleBtn"
                aria-pressed="false"
                aria-label="라이트 모드로 전환"
                title="라이트 모드로 전환">
            <svg class="theme-toggle-icon theme-toggle-icon-dark" width="20" height="20" viewBox="0 0 24 24" fill="none" aria-hidden="true">
                <path d="M21 12.8A9 9 0 1 1 11.2 3a7 7 0 0 0 9.8 9.8Z"
                      stroke="currentColor"
                      stroke-width="2"
                      stroke-linecap="round"
                      stroke-linejoin="round"/>
            </svg>
            <svg class="theme-toggle-icon theme-toggle-icon-light" width="20" height="20" viewBox="0 0 24 24" fill="none" aria-hidden="true">
                <circle cx="12" cy="12" r="4.5" stroke="currentColor" stroke-width="2"/>
                <path d="M12 2.5v2.4M12 19.1v2.4M4.2 4.2l1.7 1.7M18.1 18.1l1.7 1.7M2.5 12h2.4M19.1 12h2.4M4.2 19.8l1.7-1.7M18.1 5.9l1.7-1.7"
                      stroke="currentColor"
                      stroke-width="2"
                      stroke-linecap="round"/>
            </svg>
        </button>

        <nav class="header-nav" id="headerNav" aria-label="주요 메뉴">
            <ul class="header-nav-list">
                <li class="header-nav-item header-nav-dropdown" data-nav-dropdown>
                    <button type="button"
                            class="header-nav-link header-nav-trigger"
                            aria-expanded="false">
                        콘텐츠
                        <%-- [헤더 수정] 글자와 화살표가 따로 노는 느낌을 줄이기 위해
                             폰트 기호(⌄) 대신 크기/정렬을 일정하게 제어할 수 있는 SVG chevron을 사용합니다. --%>
                        <span class="header-nav-arrow" aria-hidden="true">
                            <svg viewBox="0 0 12 8" focusable="false">
                                <path d="M1 1.5 6 6.5 11 1.5" />
                            </svg>
                        </span>
                    </button>

                    <div class="header-submenu" role="menu">
                        <div class="header-submenu-heading">콘텐츠 탐색</div>
                        <a href="${pageContext.request.contextPath}/content/list?type=popular" role="menuitem">인기</a>
                        <a href="${pageContext.request.contextPath}/content/list?type=new" role="menuitem">신규 <span class="header-submenu-badge">NEW</span></a>
                        <a href="${pageContext.request.contextPath}/ranking" role="menuitem">랭킹 <span class="header-submenu-badge header-submenu-badge--hot">HOT</span></a>
                        <a href="${pageContext.request.contextPath}/content/today" role="menuitem">오늘의 콘텐츠</a>
                        <a href="${pageContext.request.contextPath}/recommend" role="menuitem">추천 콘텐츠</a>
                        <a href="${pageContext.request.contextPath}/content/release-calendar" role="menuitem">출시 캘린더</a>
                    </div>
                </li>

                <li class="header-nav-item header-nav-dropdown" data-nav-dropdown>
                    <button type="button"
                            class="header-nav-link header-nav-trigger"
                            aria-expanded="false">
                        상품
                        <%-- [헤더 수정] 콘텐츠 메뉴와 동일한 SVG chevron을 사용해
                             세 메뉴의 화살표 크기와 기준선을 통일합니다. --%>
                        <span class="header-nav-arrow" aria-hidden="true">
                            <svg viewBox="0 0 12 8" focusable="false">
                                <path d="M1 1.5 6 6.5 11 1.5" />
                            </svg>
                        </span>
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
                        <%-- [헤더 수정] 이벤트 메뉴도 동일한 SVG chevron을 사용해
                             메뉴명과 화살표가 한 덩어리처럼 자연스럽게 보이도록 합니다. --%>
                        <span class="header-nav-arrow" aria-hidden="true">
                            <svg viewBox="0 0 12 8" focusable="false">
                                <path d="M1 1.5 6 6.5 11 1.5" />
                            </svg>
                        </span>
                    </button>

                    <div class="header-submenu" role="menu">
                        <div class="header-submenu-heading">할인 이벤트</div>
                        <a href="${pageContext.request.contextPath}/event/list?period=ongoing" role="menuitem">진행 중</a>
                        <a href="${pageContext.request.contextPath}/event/list?period=upcoming" role="menuitem">예정</a>
                        <a href="${pageContext.request.contextPath}/event/list?period=ended" role="menuitem">종료</a>
                        <a href="${pageContext.request.contextPath}/discount/ott" role="menuitem">OTT 할인 정보</a>
                        <a href="${pageContext.request.contextPath}/subscription/calculator" role="menuitem">구독 조합 계산기</a>
                    </div>
                </li>
            </ul>
        </nav>

        <%--
            768px 이하 모바일에서는 이 검색창이 기본적으로 숨겨져 있다가,
            header-search-toggle 버튼을 누르면 헤더 아래로 펼쳐지는 형태로 노출된다.
            (js/common.js의 initMobileSearchToggle 참고)
        --%>
        <%--
            data-context-path/data-member-no는 js/common.js의
            검색 자동완성 + 최근 검색어 드롭다운(initHeaderSearchSuggestions)에서 사용한다.
            비로그인 상태(data-member-no 없음)에서는 최근 검색어를
            서버(SEARCH_KEYWORD_HISTORY) 대신 브라우저 저장소에 보관한다.
        --%>
        <div class="header-search"
             id="headerSearch"
             data-context-path="${pageContext.request.contextPath}"
             data-member-no="${headerMemberNo}">
            <form id="headerSearchForm" action="${pageContext.request.contextPath}/search" method="get" autocomplete="off">
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
                       autocomplete="off"
                       role="combobox"
                       aria-expanded="false"
                       aria-controls="headerSearchDropdown"
                       aria-autocomplete="list">
                <button type="submit" aria-label="검색">
                    <svg width="18" height="18" viewBox="0 0 24 24" fill="none">
                        <path d="M21 21L16.65 16.65" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                        <circle cx="11" cy="11" r="6" stroke="currentColor" stroke-width="2"/>
                    </svg>
                </button>
            </form>

            <%--
                타이핑 중 콘텐츠/배우 미리보기와 포커스 시 최근 검색어를
                함께 보여주는 드롭다운. 기본은 숨김이며 js/common.js가
                "open" 클래스를 토글해서 펼친다.
            --%>
            <section class="header-search-dropdown"
                     id="headerSearchDropdown"
                     aria-label="검색 추천"
                     aria-live="polite"></section>
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
                    <%-- 관리자/승인된 사업자만 사용하는 사업자 채팅(공지방·자유방) 진입 아이콘.
                         일반 회원에게는 노출하지 않는다. --%>
                    <c:if test="${headerChatEnabled}">
                        <div class="chat-menu" id="chatMenu">
                            <a href="${pageContext.request.contextPath}/chat/list"
                               class="icon-btn chat-btn"
                               id="chatEntryBtn"
                               title="채팅"
                               aria-label="채팅방 목록으로 이동"
                               aria-haspopup="true"
                               aria-expanded="false"
                               aria-controls="chatUnreadDropdown">
                                <svg width="20" height="20" viewBox="0 0 24 24" fill="none">
                                    <path d="M12 3C6.9 3 3 6.4 3 10.6c0 2.5 1.4 4.7 3.6 6.1-.1.9-.4 2-1.1 3.1a.5.5 0 0 0 .6.7c1.7-.5 3-1.2 3.8-1.8.7.2 1.4.3 2.1.3 5.1 0 9-3.4 9-7.4S17.1 3 12 3Z"
                                          stroke="currentColor"
                                          stroke-width="2"
                                          stroke-linecap="round"
                                          stroke-linejoin="round"/>
                                    <circle cx="8.3" cy="10.6" r="1.1" fill="currentColor"/>
                                    <circle cx="12" cy="10.6" r="1.1" fill="currentColor"/>
                                    <circle cx="15.7" cy="10.6" r="1.1" fill="currentColor"/>
                                </svg>
                                <%--
                                    채팅 미읽음 배지. 값 갱신/실시간 구독은 header-notification.js가
                                    이미 알림벨용으로 붙여둔 Firestore 리스너(chat source)를 그대로 재사용해
                                    여기서는 표시만 담당한다(#notificationBadge와 동일한 스타일 클래스 재사용).
                                --%>
                                <span class="notification-badge chat-badge"
                                      id="chatUnreadBadge"
                                      aria-label="읽지 않은 채팅방 0개"
                                      hidden></span>
                            </a>

                            <%--
                                배지에 숫자가 떠 있을 때만(안 읽은 메시지가 있을 때만) 아이콘 클릭 시
                                이 드롭다운이 열리고, 목록 항목을 누르면 해당 채팅방으로 바로 이동한다.
                                배지가 없을 때는 header-notification.js가 preventDefault를 걸지 않으므로
                                <a href="...chat/list">의 기본 동작(채팅방 목록 이동)이 그대로 유지된다.
                            --%>
                            <div class="notification-dropdown chat-unread-dropdown"
                                 id="chatUnreadDropdown"
                                 aria-label="안 읽은 채팅방 목록">

                                <div class="notification-dropdown-header">
                                    <strong>안 읽은 채팅방</strong>
                                </div>

                                <div class="notification-list"
                                     id="chatUnreadList"
                                     aria-live="polite">
                                    <div class="notification-empty">
                                        안 읽은 채팅방이 없습니다.
                                    </div>
                                </div>
                            </div>
                        </div>
                    </c:if>

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

                    <%-- 일반 사용자 장바구니
                         (모바일 반응형에서는 common.css의 .cart-btn 규칙으로 숨김 처리) --%>
                    <c:if test="${sessionScope.loginMember.role ne 'ADMIN' and empty sessionScope.businessNo}">
                        <a href="${pageContext.request.contextPath}/cart"
                           class="icon-btn cart-btn"
                           title="장바구니"
                           aria-label="장바구니">
                            <svg width="20" height="20" viewBox="0 0 24 24" fill="none">
                                <path d="M3 4h2l2.4 12.2a2 2 0 0 0 2 1.6h7.6a2 2 0 0 0 2-1.6L21 8H6.2"
                                      stroke="currentColor"
                                      stroke-width="2"
                                      stroke-linecap="round"
                                      stroke-linejoin="round"/>
                                <circle cx="10" cy="20" r="1.4" fill="currentColor"/>
                                <circle cx="17" cy="20" r="1.4" fill="currentColor"/>
                            </svg>
                        </a>
                    </c:if>

                    <div class="profile-menu">
                        <button class="profile-btn" id="profileBtn" type="button" aria-expanded="false">
                            <c:choose>
                                <c:when test="${empty sessionScope.loginMember.profileImage}">
                                    <img class="profile-btn-avatar"
                                         src="${pageContext.request.contextPath}/images/profile_image.jpg"
                                         alt="기본 프로필">
                                </c:when>
                                <c:when test="${fn:startsWith(sessionScope.loginMember.profileImage, 'http')}">
                                    <img class="profile-btn-avatar"
                                         src="${sessionScope.loginMember.profileImage}"
                                         alt="프로필 이미지">
                                </c:when>
                                <c:otherwise>
                                    <img class="profile-btn-avatar"
                                         src="${pageContext.request.contextPath}/uploads/profile/${sessionScope.loginMember.profileImage}"
                                         alt="프로필 이미지">
                                </c:otherwise>
                            </c:choose>
                            <span>${sessionScope.loginDisplayName}</span>
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

                            <%--
                                [추가] 모바일 반응형 전용 라이트/다크 모드 전환 항목.
                                관리자/사업자/일반 회원 메뉴 분기 밖으로 빼서 세 역할 모두에
                                공통으로 노출한다. header-right 안의 아이콘 버튼(#themeToggleBtn)과
                                같은 js/theme-toggle.js가 .theme-toggle-btn 클래스를 가진 모든
                                요소를 위임 클릭으로 처리하므로, id 없이 클래스만 추가하면
                                별도 JS 없이 동작한다(중복 id 방지). 데스크톱에서는 이미
                                헤더에 아이콘 토글이 있으므로 992px 이하에서만 보인다.
                            --%>
                            <button type="button"
                                    class="theme-toggle-btn profile-dropdown-theme-toggle"
                                    aria-pressed="false">
                                <span class="theme-toggle-dropdown-label theme-toggle-dropdown-label-dark">라이트 모드로 전환</span>
                                <span class="theme-toggle-dropdown-label theme-toggle-dropdown-label-light">다크 모드로 전환</span>
                            </button>

                            <hr>
                            <a href="${pageContext.request.contextPath}/member/logout" class="logout-link">로그아웃</a>
                        </div>
                    </div>
                </c:otherwise>
            </c:choose>
        </div>
    </div>
</header>

<%-- [추가] 모바일 전체 메뉴/검색이 펼쳐졌을 때 뒤 배경을 어둡게 눌러주는 오버레이.
     common.js의 syncHeaderOverlay()가 상태에 맞춰 open 클래스를 토글한다. --%>
<div class="header-overlay" id="headerOverlay" aria-hidden="true"></div>

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
            src="${pageContext.request.contextPath}/js/header-notification.js?v=9"></script>
</c:if>