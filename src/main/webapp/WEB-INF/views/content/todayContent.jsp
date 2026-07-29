<%@ page language="java"
         contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8" %>

<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!DOCTYPE html>
<html lang="ko">

<head>
<meta charset="UTF-8">
<meta name="viewport"
      content="width=device-width, initial-scale=1.0">

<title>오늘의 콘텐츠 | ODITJI</title>

<link rel="stylesheet"
      href="${pageContext.request.contextPath}/css/content-more.css?v=2">
</head>

<body>

<jsp:include page="/WEB-INF/views/common/header.jsp"/>

<main class="content-more-page">

    <section class="content-more-hero">

        <div>

            <p class="content-more-kicker">
                TODAY
            </p>

            <h1>
                오늘의 콘텐츠
            </h1>

            <p class="content-more-description">
                최근 30일 이내 공개된 인기 영화와 TV 콘텐츠를 확인해보세요.
            </p>

        </div>

        <div class="content-more-count">
            총 ${contentCount}개
        </div>

    </section>

    <section class="content-more-grid-section">

        <c:choose>

            <c:when test="${not empty todayContentList}">

                <div class="content-more-grid">

                    <c:forEach var="content"
                               items="${todayContentList}">

                        <%-- 콘텐츠 연령등급 배지 표시용 값 설정 --%>
                        <c:set var="ageBadgeLabel" value="?"/>
                        <c:set var="ageBadgeClass" value="unknown"/>
                        <c:set var="ageBadgeTitle" value="등급 정보 없음"/>

                        <c:choose>
                            <c:when test="${content.ageRating eq '전체 관람가'}">
                                <c:set var="ageBadgeLabel" value="ALL"/>
                                <c:set var="ageBadgeClass" value="all"/>
                                <c:set var="ageBadgeTitle" value="전체 관람가"/>
                            </c:when>
                            <c:when test="${content.ageRating eq '7세 이상 관람가'}">
                                <c:set var="ageBadgeLabel" value="7"/>
                                <c:set var="ageBadgeClass" value="age7"/>
                                <c:set var="ageBadgeTitle" value="7세 이상 관람가"/>
                            </c:when>
                            <c:when test="${content.ageRating eq '12세 이상 관람가'}">
                                <c:set var="ageBadgeLabel" value="12"/>
                                <c:set var="ageBadgeClass" value="age12"/>
                                <c:set var="ageBadgeTitle" value="12세 이상 관람가"/>
                            </c:when>
                            <c:when test="${content.ageRating eq '15세 이상 관람가'}">
                                <c:set var="ageBadgeLabel" value="15"/>
                                <c:set var="ageBadgeClass" value="age15"/>
                                <c:set var="ageBadgeTitle" value="15세 이상 관람가"/>
                            </c:when>
                            <c:when test="${content.ageRating eq '청소년 관람불가'}">
                                <c:set var="ageBadgeLabel" value="19"/>
                                <c:set var="ageBadgeClass" value="adult"/>
                                <c:set var="ageBadgeTitle" value="청소년 관람불가"/>
                            </c:when>
                        </c:choose>

                        <a href="${pageContext.request.contextPath}/content/prepare?tmdbId=${content.tmdbId}&contentType=${content.contentType}"
                           class="content-more-card">

                            <div class="content-more-thumb">

                                <c:choose>

                                    <c:when test="${not empty content.posterPath}">
                                        <img src="https://image.tmdb.org/t/p/w500${content.posterPath}"
                                             alt="${content.title}"
                                             loading="lazy">
                                    </c:when>

                                    <c:otherwise>
                                        <div class="content-more-no-image">
                                            NO IMAGE
                                        </div>
                                    </c:otherwise>

                                </c:choose>

                                <span class="content-more-type">

                                    <c:choose>

                                        <c:when test="${content.contentType eq 'MOVIE'}">
                                            영화
                                        </c:when>

                                        <c:otherwise>
                                            TV
                                        </c:otherwise>

                                    </c:choose>

                                </span>

                                <span class="content-more-age-rating"
                                      title="${ageBadgeTitle}">
                                    <span class="content-age-rating-badge is-${ageBadgeClass}"
                                          aria-label="${ageBadgeTitle}">
                                        ${ageBadgeLabel}
                                    </span>
                                </span>

                            </div>

                            <div class="content-more-info">

                                <h2>
                                    ${content.title}
                                </h2>

                                <div class="content-more-meta">

                                    <span>
                                        <c:choose>

                                            <c:when test="${not empty content.releaseDate}">
                                                ${content.releaseDate}
                                            </c:when>

                                            <c:otherwise>
                                                공개일 미정
                                            </c:otherwise>

                                        </c:choose>
                                    </span>

                                    <c:if test="${not empty content.tmdbScore}">
                                        <span class="content-more-score">
                                            ⭐ ${content.tmdbScore}
                                        </span>
                                    </c:if>

                                </div>

                                <c:if test="${not empty content.platformList}">

                                    <div class="content-more-platforms">

                                        <c:forEach var="platform"
                                                   items="${content.platformList}"
                                                   begin="0"
                                                   end="3">

                                            <img src="${platform.logoImage}"
                                                 alt="${platform.platformName}"
                                                 title="${platform.platformName}"
                                                 loading="lazy">

                                        </c:forEach>

                                        <c:if test="${content.platformList.size() > 4}">
                                            <span class="content-more-platform-more">
                                                +${content.platformList.size() - 4}
                                            </span>
                                        </c:if>

                                    </div>

                                </c:if>

                            </div>

                        </a>

                    </c:forEach>

                </div>

            </c:when>

            <c:otherwise>

                <div class="content-more-empty">
                    조건에 맞는 오늘의 콘텐츠가 없습니다.
                </div>

            </c:otherwise>

        </c:choose>

    </section>

</main>

<jsp:include page="/WEB-INF/views/common/footer.jsp"/>

</body>
</html>