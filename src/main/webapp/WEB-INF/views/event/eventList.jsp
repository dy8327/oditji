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
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/event.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/pagination-common.css?v=1">
    <script defer src="${pageContext.request.contextPath}/js/pagination.js?v=1"></script>
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
                    총 <strong><c:out value="${eventTotalCount}"/></strong>개의 이벤트
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

                <div class="event-grid event-grid--${period}">
                <c:choose>
                    <c:when test="${not empty eventList}">
                        <c:forEach var="event" items="${eventList}">
                            <article class="event-card">
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
                        <div class="empty">${emptyMessage}</div>
                    </c:otherwise>
                </c:choose>
            </div>

            <%-- =====================================================
                [추가] 이벤트 목록 페이지네이션
                진행 중/예정/종료 조건을 유지하면서 페이지를 이동합니다.
            ===================================================== --%>
            <c:if test="${eventTotalCount > 0}">
                <nav class="oditji-pagination"
                    data-pagination
                    data-current-page="${pageVO.currentPage}"
                    data-total-page="${pageVO.totalPage}"
                    data-page-param="page"
                    data-fixed-param-name="period"
                    data-fixed-param-value="${period}"
                    aria-label="이벤트 목록 페이지 이동">
                </nav>
            </c:if>

        </section>
    </main>

<jsp:include page="/WEB-INF/views/common/footer.jsp"/>

</body>
</html>
