<%@ page language="java"
         contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8" %>

<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<!DOCTYPE html>
<html lang="ko">

<head>
<meta charset="UTF-8">

<title>출시 알림 캘린더 | ODITJI</title>

<link rel="stylesheet"
      href="${pageContext.request.contextPath}/css/content-more.css?v=2">
<link rel="stylesheet"
      href="${pageContext.request.contextPath}/css/release-calendar.css?v=4">

<%-- 연·월 이동 폼 자동 제출 + 데스크톱 "더보기" 팝오버 / 모바일 날짜 탭 모달 --%>
<script defer
        src="${pageContext.request.contextPath}/js/release-calendar.js?v=3"></script>

<jsp:include page="/WEB-INF/views/common/head-assets.jsp"/>
</head>

<body>

<jsp:include page="/WEB-INF/views/common/header.jsp"/>

<main id="mainContent" class="content-more-page">

    <section class="content-more-hero">

        <div>

            <p class="content-more-kicker">
                CALENDAR
            </p>

            <h1>
                출시 알림 캘린더
            </h1>

            <p class="content-more-description">
                이 달에 개봉·공개하는 작품을 한눈에 확인하세요.
                찜한 작품은 공개일이 다가오면 알림으로 알려드려요.
            </p>

        </div>

    </section>

    <section class="release-calendar-section">

        <div class="release-calendar-toolbar">

            <div class="release-calendar-toolbar-nav">

                <a class="release-calendar-today-btn${isCurrentMonth ? ' is-disabled' : ''}"
                   href="${pageContext.request.contextPath}/content/release-calendar"
                   aria-disabled="${isCurrentMonth}">
                    오늘
                </a>

                <div class="release-calendar-arrows">

                    <a class="release-calendar-arrow-btn"
                       href="${pageContext.request.contextPath}/content/release-calendar?year=${prevYear}&amp;month=${prevMonth}"
                       aria-label="이전 달">
                        <svg viewBox="0 0 24 24" width="18" height="18" aria-hidden="true">
                            <path d="M15 5l-7 7 7 7" fill="none" stroke="currentColor"
                                  stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                        </svg>
                    </a>

                    <a class="release-calendar-arrow-btn"
                       href="${pageContext.request.contextPath}/content/release-calendar?year=${nextYear}&amp;month=${nextMonth}"
                       aria-label="다음 달">
                        <svg viewBox="0 0 24 24" width="18" height="18" aria-hidden="true">
                            <path d="M9 5l7 7-7 7" fill="none" stroke="currentColor"
                                  stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                        </svg>
                    </a>

                </div>

                <h2 class="release-calendar-title">
                    ${targetYear}년 ${targetMonth}월
                </h2>

            </div>

            <%-- 연도·월을 직접 선택해 원하는 달로 바로 이동합니다.
                 JS 미동작 환경에서도 "이동" 버튼으로 동작하도록 폼으로 구성합니다. --%>
            <form class="release-calendar-jump"
                  method="get"
                  action="${pageContext.request.contextPath}/content/release-calendar"
                  data-calendar-jump-form>

                <span class="release-calendar-sr-only" id="releaseCalendarJumpLabel">
                    연도와 월을 선택해 이동
                </span>

                <select id="releaseCalendarYearSelect"
                        name="year"
                        class="release-calendar-select"
                        aria-label="연도 선택">

                    <c:forEach var="y" begin="${targetYear - 6}" end="${targetYear + 6}">
                        <option value="${y}" ${y == targetYear ? 'selected' : ''}>${y}년</option>
                    </c:forEach>

                </select>

                <select id="releaseCalendarMonthSelect"
                        name="month"
                        class="release-calendar-select"
                        aria-label="월 선택">

                    <c:forEach var="m" begin="1" end="12">
                        <option value="${m}" ${m == targetMonth ? 'selected' : ''}>${m}월</option>
                    </c:forEach>

                </select>

                <button type="submit" class="release-calendar-jump-btn" data-calendar-jump-submit>
                    이동
                </button>

            </form>

        </div>

        <div class="release-calendar-notice">

            <p class="release-calendar-notice-item">
                <svg viewBox="0 0 24 24" width="16" height="16" aria-hidden="true">
                    <path d="M9 9h6M9 13h4M7 4h10a2 2 0 0 1 2 2v9a2 2 0 0 1-2 2H10l-4 3v-3H7a2 2 0 0 1-2-2V6a2 2 0 0 1 2-2z"
                          fill="none" stroke="currentColor" stroke-width="1.6"
                          stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
                날짜를 탭하면 그 날 출시된 작품을 모아볼 수 있어요.
            </p>

            <p class="release-calendar-notice-item">
                <svg viewBox="0 0 24 24" width="16" height="16" aria-hidden="true">
                    <path d="M12 3a5 5 0 0 0-5 5v3.2c0 .6-.24 1.18-.66 1.6L5 14.2c-.53.53-.16 1.44.6 1.44h12.8c.76 0 1.13-.9.6-1.44l-1.34-1.4a2.27 2.27 0 0 1-.66-1.6V8a5 5 0 0 0-5-5z"
                          fill="none" stroke="currentColor" stroke-width="1.6"
                          stroke-linecap="round" stroke-linejoin="round"/>
                    <path d="M9.5 18a2.5 2.5 0 0 0 5 0" fill="none" stroke="currentColor"
                          stroke-width="1.6" stroke-linecap="round"/>
                </svg>
                찜한 작품은 공개일이 다가오면 알림으로 알려드려요.
            </p>

        </div>

        <div class="release-calendar-grid">

            <div class="release-calendar-weekday release-calendar-weekday--sun">일</div>
            <div class="release-calendar-weekday">월</div>
            <div class="release-calendar-weekday">화</div>
            <div class="release-calendar-weekday">수</div>
            <div class="release-calendar-weekday">목</div>
            <div class="release-calendar-weekday">금</div>
            <div class="release-calendar-weekday release-calendar-weekday--sat">토</div>

            <c:forEach begin="1" end="${firstDayOfWeek}">
                <div class="release-calendar-cell release-calendar-cell--empty"></div>
            </c:forEach>

            <c:forEach begin="1" end="${daysInMonth}" var="day">

                <c:set var="dayContentList" value="${releaseByDay[day]}" />
                <c:set var="dayContentCount" value="${fn:length(dayContentList)}" />
                <c:set var="cellWeekday" value="${(firstDayOfWeek + day - 1) % 7}" />
                <c:set var="isToday" value="${isCurrentMonth and day == todayDay}" />

                <div class="release-calendar-cell${empty dayContentList ? '' : ' release-calendar-cell--has-content'}${cellWeekday == 0 ? ' release-calendar-cell--sun' : ''}${cellWeekday == 6 ? ' release-calendar-cell--sat' : ''}${isToday ? ' release-calendar-cell--today' : ''}"
                     data-calendar-cell-popover="releaseCalendarPopover-${day}">

                    <span class="release-calendar-date">
                        <c:if test="${isToday}">
                            <span class="release-calendar-date-badge">${day}</span>
                        </c:if>
                        <c:if test="${not isToday}">
                            ${day}
                        </c:if>
                    </span>

                    <c:if test="${not empty dayContentList}">
                        <%-- 모바일에서는 포스터 목록 대신 이 배지만 보이고,
                             셀 전체를 탭하면 release-calendar-popover가 모달로 열립니다. --%>
                        <span class="release-calendar-cell-count">
                            ${dayContentCount}개
                        </span>
                    </c:if>

                    <c:if test="${not empty dayContentList}">

                        <%-- 셀 높이를 항목 수와 무관하게 항상 동일하게 유지하기 위해
                             미리보기에는 최대 2개까지만 표시합니다. --%>
                        <ul class="release-calendar-content-list">

                            <c:forEach var="content"
                                       items="${dayContentList}"
                                       varStatus="status"
                                       begin="0"
                                       end="${dayContentCount > 2 ? 1 : dayContentCount - 1}">

                                <li class="release-calendar-content-item">

                                    <a href="${pageContext.request.contextPath}/content/prepare?tmdbId=${content.tmdbId}&amp;contentType=${content.contentType}"
                                       title="${content.title}">

                                        <c:choose>

                                            <c:when test="${not empty content.posterPath}">
                                                <img src="https://image.tmdb.org/t/p/w92${content.posterPath}"
                                                     alt="${content.title}">
                                            </c:when>

                                            <c:otherwise>
                                                <span class="release-calendar-content-noimg"></span>
                                            </c:otherwise>

                                        </c:choose>

                                        <span class="release-calendar-content-title">
                                            ${content.title}
                                        </span>

                                    </a>

                                </li>

                            </c:forEach>

                        </ul>

                        <c:if test="${dayContentCount > 2}">

                            <%-- 데스크톱 전용 "더보기" 버튼입니다. 클릭 시 아래
                                 release-calendar-popover를 버튼 근처에 띄웁니다. --%>
                            <button type="button"
                                    class="release-calendar-more"
                                    data-calendar-more-toggle
                                    aria-haspopup="true"
                                    aria-expanded="false"
                                    aria-controls="releaseCalendarPopover-${day}">
                                +${dayContentCount - 2}개 더보기
                            </button>

                        </c:if>

                        <%-- 그 날의 전체 출시 목록입니다. 데스크톱에서는 위 "더보기" 버튼이,
                             모바일에서는 날짜 셀 자체를 탭하는 것이 이 패널을 엽니다
                             (release-calendar.js). 모바일에서는 CSS로 화면 중앙 모달처럼
                             보이도록 스타일이 바뀝니다. --%>
                        <%-- [SonarQube 접근성] role="dialog"을 부여한 div 대신
                             브라우저 기본 대화상자 의미를 제공하는 native dialog 요소를 사용합니다. --%>
                        <dialog class="release-calendar-popover"
                                id="releaseCalendarPopover-${day}"
                                aria-labelledby="releaseCalendarPopoverTitle-${day}"
                                data-calendar-popover>

                            <div class="release-calendar-popover-header">

                                <span class="release-calendar-popover-title"
                                      id="releaseCalendarPopoverTitle-${day}">
                                    ${targetMonth}월 ${day}일 출시 · ${dayContentCount}건
                                </span>

                                <button type="button"
                                        class="release-calendar-popover-close"
                                        data-calendar-popover-close
                                        aria-label="닫기">
                                    &times;
                                </button>

                            </div>

                            <ul class="release-calendar-popover-list">

                                <c:forEach var="content" items="${dayContentList}">

                                    <li class="release-calendar-popover-item">

                                        <a href="${pageContext.request.contextPath}/content/prepare?tmdbId=${content.tmdbId}&amp;contentType=${content.contentType}">

                                            <c:choose>

                                                <c:when test="${not empty content.posterPath}">
                                                    <img src="https://image.tmdb.org/t/p/w92${content.posterPath}"
                                                         alt="${content.title}">
                                                </c:when>

                                                <c:otherwise>
                                                    <span class="release-calendar-content-noimg"></span>
                                                </c:otherwise>

                                            </c:choose>

                                            <span class="release-calendar-popover-item-title">
                                                ${content.title}
                                            </span>

                                        </a>

                                    </li>

                                </c:forEach>

                            </ul>

                        </dialog>

                    </c:if>

                </div>

            </c:forEach>

        </div>

        <%-- 모바일에서 팝오버가 모달로 열릴 때 뒤에 깔리는 배경입니다.
             배경을 탭하면 닫힙니다 (release-calendar.js). --%>
        <div class="release-calendar-backdrop" data-calendar-backdrop></div>

    </section>

</main>

<jsp:include page="/WEB-INF/views/common/footer.jsp"/>

</body>
</html>
