<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ taglib prefix="dt" uri="http://oditji.com/functions/datetime" %>

<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>ODITJI | ${periodTitle}</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/event.css?v=2">

    <script>
        const contextPath = "${pageContext.request.contextPath}";
    </script>
    <script defer src="${pageContext.request.contextPath}/js/event.js?v=2"></script>
</head>
<body>

<jsp:include page="/WEB-INF/views/common/header.jsp"/>

<main id="mainContent" class="main">
    <section class="event-section">
        <div class="event-list-heading event-list-heading--${period}">
            <div>
                <p class="event-eyebrow">ODITJI EVENT</p>
                <h1 class="section-title">${periodTitle}</h1>
                <p class="section-description">${periodDescription}</p>
                <p class="event-count">
                    총 <strong><c:out value="${fn:length(eventList)}"/></strong>개의 이벤트
                </p>
            </div>

            <nav class="event-period-links" aria-label="이벤트 기간 분류">
                <a href="${pageContext.request.contextPath}/event/list?period=ongoing"
                   class="event-period-link--ongoing ${period eq 'ongoing' ? 'active' : ''}">진행 중</a>
                <a href="${pageContext.request.contextPath}/event/list?period=upcoming"
                   class="event-period-link--upcoming ${period eq 'upcoming' ? 'active' : ''}">예정</a>
                <a href="${pageContext.request.contextPath}/event/list?period=ended"
                   class="event-period-link--ended ${period eq 'ended' ? 'active' : ''}">종료</a>
            </nav>
        </div>

        <%--
            [수정] 스포트라이트 = 현재 선택된 탭(진행 중/예정)의 이벤트 전체를
            메인화면(index.jsp) 히어로처럼 자동으로 넘어가는 슬라이더로 보여준다.
            "진행 중" 탭이면 진행 중 이벤트 전부, "예정" 탭이면 예정 이벤트
            전부가 슬라이드로 들어간다 - 새 컨트롤러 속성 없이 이미 탭별로
            내려오는 eventList를 그대로 재사용한다. 종료 탭은 이미 끝난
            이벤트에 카운트다운/CTA를 보여주는 게 어색해서 스포트라이트 없이
            그리드만 보여준다. 아래 그리드는 스포트라이트와 별개로 항상
            eventList 전체를 처음부터 보여준다("전체 이벤트 목록").
        --%>
        <c:set var="showSpotlight" value="${not empty eventList and (period eq 'ongoing' or period eq 'upcoming')}"/>

        <c:if test="${showSpotlight}">
            <section class="event-spotlight"
                      id="eventSpotlight"
                      aria-roledescription="carousel"
                      aria-label="${periodTitle}">

                <div class="event-spotlight-slides" id="eventSpotlightSlides">
                    <c:forEach var="spotlightEvent" items="${eventList}" varStatus="spotStatus">
                        <article class="event-spotlight-slide event-spotlight-slide--${period} ${spotStatus.first ? 'is-active' : ''}"
                                 data-slide-index="${spotStatus.index}"
                                 data-event-timer
                                 data-period="${period}"
                                 data-start="${dt:format(spotlightEvent.startDate, 'yyyy-MM-dd')}"
                                 data-end="${dt:format(spotlightEvent.endDate, 'yyyy-MM-dd')}"
                                 aria-hidden="${spotStatus.first ? 'false' : 'true'}">

                            <a class="event-spotlight-link" href="${pageContext.request.contextPath}/event/detail/${spotlightEvent.eventNo}">
                                <div class="event-spotlight-media">
                                    <c:choose>
                                        <c:when test="${not empty spotlightEvent.bannerImage}">
                                            <img src="${pageContext.request.contextPath}${spotlightEvent.bannerImage}"
                                                 alt="<c:out value='${spotlightEvent.title}'/>"
                                                 loading="${spotStatus.first ? 'eager' : 'lazy'}">
                                        </c:when>
                                        <c:otherwise>
                                            <div class="no-img no-img--spotlight">NO IMAGE</div>
                                        </c:otherwise>
                                    </c:choose>
                                    <div class="event-spotlight-scrim"></div>
                                </div>

                                <div class="event-spotlight-content">
                                    <span class="event-spotlight-eyebrow">
                                        <span class="event-spotlight-dot" aria-hidden="true"></span>
                                        ${periodBadge} · PICK
                                    </span>

                                    <h2 class="event-spotlight-title"><c:out value="${spotlightEvent.title}"/></h2>

                                    <p class="event-spotlight-date">
                                        ${dt:format(spotlightEvent.startDate, 'yyyy.MM.dd')}
                                        ~
                                        ${dt:format(spotlightEvent.endDate, 'yyyy.MM.dd')}
                                    </p>

                                    <c:if test="${not empty spotlightEvent.description}">
                                        <p class="event-spotlight-desc"><c:out value="${spotlightEvent.description}"/></p>
                                    </c:if>

                                    <div class="event-spotlight-footer">
                                        <div class="event-countdown" data-big-countdown aria-live="polite"></div>
                                        <span class="event-spotlight-cta">
                                            자세히 보기
                                            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" aria-hidden="true">
                                                <path d="M9 6l6 6-6 6" stroke="currentColor" stroke-width="2"
                                                      stroke-linecap="round" stroke-linejoin="round"/>
                                            </svg>
                                        </span>
                                    </div>
                                </div>
                            </a>
                        </article>
                    </c:forEach>
                </div>

                <%-- 슬라이드가 2개 이상일 때만 화살표/점 네비게이션을 보여준다 --%>
                <c:if test="${fn:length(eventList) > 1}">
                    <button type="button"
                            class="event-spotlight-nav event-spotlight-nav-prev"
                            aria-label="이전 이벤트 보기"
                            onclick="moveEventSpotlight(-1)">
                        &lsaquo;
                    </button>

                    <button type="button"
                            class="event-spotlight-nav event-spotlight-nav-next"
                            aria-label="다음 이벤트 보기"
                            onclick="moveEventSpotlight(1)">
                        &rsaquo;
                    </button>

                    <div class="event-spotlight-dots" role="tablist" aria-label="이벤트 선택">
                        <c:forEach var="dotEvent" items="${eventList}" varStatus="dotStatus">
                            <button type="button"
                                    class="event-spotlight-dot ${dotStatus.first ? 'is-active' : ''}"
                                    role="tab"
                                    aria-selected="${dotStatus.first ? 'true' : 'false'}"
                                    aria-label="<c:out value='${dotEvent.title}'/>(으)로 이동"
                                    onclick="goEventSpotlight(${dotStatus.index})"></button>
                        </c:forEach>
                    </div>
                </c:if>
            </section>
        </c:if>

        <div class="event-grid event-grid--${period}">
            <c:choose>
                <c:when test="${not empty eventList}">
                    <c:forEach var="event" items="${eventList}" varStatus="loopStatus">
                        <article class="event-card"
                                 data-event-timer
                                 data-period="${period}"
                                 data-start="${dt:format(event.startDate, 'yyyy-MM-dd')}"
                                 data-end="${dt:format(event.endDate, 'yyyy-MM-dd')}">
                            <a href="${pageContext.request.contextPath}/event/detail/${event.eventNo}">
                                <div class="event-image">
                                    <c:choose>
                                        <c:when test="${not empty event.bannerImage}">
                                            <img src="${pageContext.request.contextPath}${event.bannerImage}"
                                                 alt="<c:out value='${event.title}'/>"
                                                 loading="lazy">
                                        </c:when>
                                        <c:otherwise>
                                            <div class="no-img">NO IMAGE</div>
                                        </c:otherwise>
                                    </c:choose>

                                    <span class="event-badge">${periodBadge}</span>
                                    <span class="event-dday-badge" data-dday-text aria-hidden="true"></span>

                                    <c:if test="${period eq 'ongoing' or period eq 'upcoming'}">
                                        <span class="event-progress" data-progress-bar aria-hidden="true"></span>
                                    </c:if>
                                </div>

                                <div class="event-info">
                                    <h2><c:out value="${event.title}"/></h2>
                                    <p class="event-date">
                                        ${dt:format(event.startDate, 'yyyy.MM.dd')}
                                        ~
                                        ${dt:format(event.endDate, 'yyyy.MM.dd')}
                                    </p>
                                    <c:if test="${not empty event.description}">
                                        <p class="event-card-desc">
                                            <c:out value="${event.description}"/>
                                        </p>
                                    </c:if>
                                    <span class="event-card-cta">
                                        자세히 보기
                                        <span aria-hidden="true">&rarr;</span>
                                    </span>
                                </div>
                            </a>
                        </article>
                    </c:forEach>
                </c:when>
                <c:otherwise>
                    <div class="empty">
                        <svg width="40" height="40" viewBox="0 0 24 24" fill="none" aria-hidden="true">
                            <rect x="3" y="6" width="18" height="14" rx="2" stroke="currentColor" stroke-width="1.6"/>
                            <path d="M3 10h18" stroke="currentColor" stroke-width="1.6"/>
                            <path d="M8 3v4M16 3v4" stroke="currentColor" stroke-width="1.6" stroke-linecap="round"/>
                        </svg>
                        <p>${emptyMessage}</p>
                    </div>
                </c:otherwise>
            </c:choose>
        </div>
    </section>
</main>

<jsp:include page="/WEB-INF/views/common/footer.jsp"/>

</body>
</html>
