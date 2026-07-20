<%@ page language="java" contentType="text/html; charset=UTF-8"
pageEncoding="UTF-8"%>

<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!DOCTYPE html>
<html>

    <head>

        <meta charset="UTF-8">

        <title>ODITJI EVENT</title>

        <link rel="stylesheet"
        href="${pageContext.request.contextPath}/css/event.css">

    </head>

    <body>

        <jsp:include page="/WEB-INF/views/common/header.jsp"/>


        <main class="main">

            <section class="event-section">

                <h1 class="section-title">
                    이벤트
                </h1>


                <div class="event-grid">

                    <c:choose>

                        <c:when test="${not empty eventList}">

                            <c:forEach var="event" items="${eventList}">

                                <c:if test="${event.status eq 'APPROVED'}">

                                    <div class="event-card">

                                        <a href="${pageContext.request.contextPath}/event/detail/${event.eventNo}">

                                            <div class="event-image">

                                                <c:choose>

                                                    <c:when test="${not empty event.bannerImage}">

                                                        <img src="${pageContext.request.contextPath}${event.bannerImage}"
                                                        alt="${event.title}">

                                                    </c:when>

                                                    <c:otherwise>

                                                        <div class="no-img">
                                                            NO IMAGE
                                                        </div>

                                                    </c:otherwise>

                                                </c:choose>

                                            </div>


                                            <div class="event-info">

                                                <h2>
                                                    ${event.title}
                                                </h2>

                                                <p>
                                                    ${event.startDate}
                                                    ~
                                                    ${event.endDate}
                                                </p>

                                                <span>
                                                    진행중
                                                </span>

                                            </div>

                                        </a>

                                    </div>

                                </c:if>

                            </c:forEach>

                        </c:when>


                        <c:otherwise>

                            <div class="empty">
                                진행중인 이벤트가 없습니다.
                            </div>

                        </c:otherwise>

                    </c:choose>

                </div>

            </section>

        </main>


        <jsp:include page="/WEB-INF/views/common/footer.jsp"/>

    </body>

</html>