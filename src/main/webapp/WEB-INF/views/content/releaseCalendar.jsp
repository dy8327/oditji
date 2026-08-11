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
      href="${pageContext.request.contextPath}/css/release-calendar.css?v=1">
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

        <div class="release-calendar-nav">

            <a class="release-calendar-nav-btn"
               href="${pageContext.request.contextPath}/content/release-calendar?year=${prevYear}&amp;month=${prevMonth}">
                ‹ 이전 달
            </a>

            <h2 class="release-calendar-title">
                ${targetYear}년 ${targetMonth}월
            </h2>

            <a class="release-calendar-nav-btn"
               href="${pageContext.request.contextPath}/content/release-calendar?year=${nextYear}&amp;month=${nextMonth}">
                다음 달 ›
            </a>

        </div>

        <div class="release-calendar-grid">

            <div class="release-calendar-weekday">일</div>
            <div class="release-calendar-weekday">월</div>
            <div class="release-calendar-weekday">화</div>
            <div class="release-calendar-weekday">수</div>
            <div class="release-calendar-weekday">목</div>
            <div class="release-calendar-weekday">금</div>
            <div class="release-calendar-weekday">토</div>

            <c:forEach begin="1" end="${firstDayOfWeek}">
                <div class="release-calendar-cell release-calendar-cell--empty"></div>
            </c:forEach>

            <c:forEach begin="1" end="${daysInMonth}" var="day">

                <c:set var="dayContentList" value="${releaseByDay[day]}" />

                <div class="release-calendar-cell${empty dayContentList ? '' : ' release-calendar-cell--has-content'}">

                    <span class="release-calendar-date">
                        ${day}
                    </span>

                    <c:if test="${not empty dayContentList}">

                        <ul class="release-calendar-content-list">

                            <c:forEach var="content"
                                       items="${dayContentList}"
                                       varStatus="status"
                                       begin="0"
                                       end="${fn:length(dayContentList) > 3 ? 2 : fn:length(dayContentList) - 1}">

                                <li class="release-calendar-content-item">

                                    <a href="${pageContext.request.contextPath}/content/prepare?tmdbId=${content.tmdbId}&amp;contentType=${content.contentType}"
                                       title="${content.title}">

                                        <c:choose>

                                            <c:when test="${not empty content.posterPath}">
                                                <img src="https://image.tmdb.org/t/p/w92${content.posterPath}"
                                                     alt="${content.title}">
                                            </c:when>

                                            <c:otherwise>
                                                <span class="release-calendar-content-noimg">
                                                    ${content.title}
                                                </span>
                                            </c:otherwise>

                                        </c:choose>

                                        <span class="release-calendar-content-title">
                                            ${content.title}
                                        </span>

                                    </a>

                                </li>

                            </c:forEach>

                        </ul>

                        <c:if test="${fn:length(dayContentList) > 3}">
                            <span class="release-calendar-more">
                                +${fn:length(dayContentList) - 3}개 더보기
                            </span>
                        </c:if>

                    </c:if>

                </div>

            </c:forEach>

        </div>

    </section>

</main>

<jsp:include page="/WEB-INF/views/common/footer.jsp"/>

</body>
</html>
