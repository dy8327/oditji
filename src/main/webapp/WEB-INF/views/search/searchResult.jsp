<%@ page language="java"
         contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8" %>

<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!DOCTYPE html>
<html lang="ko">

<head>

    <meta charset="UTF-8">
    <title>ODITJI | 검색 결과</title>

    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/css/content.css">

    <script defer
            src="${pageContext.request.contextPath}/js/content.js"></script>

</head>

<body>

<jsp:include page="/WEB-INF/views/common/header.jsp"/>

<main class="content-container">

    <section class="content-header">

        <h1>
            검색 결과
            <span style="font-size:14px; color:#aaa;">
                "${keyword}"
            </span>
        </h1>

    </section>

    <c:if test="${empty resultList}">
        <section class="empty-state">
            <p>검색 결과가 없습니다.</p>
        </section>
    </c:if>

    <c:if test="${not empty resultList}">

        <section class="content-grid">

            <c:forEach var="content"
                       items="${resultList}">

                <article class="content-card">

                    <c:url var="detailUrl"
                           value="/content/prepare">

                        <c:param name="tmdbId"
                                 value="${content.tmdbId}"/>

                        <c:param name="contentType"
                                 value="${content.contentType}"/>

                    </c:url>

                    <a class="content-card__link"
                       href="${detailUrl}">

                        <div class="content-card__poster">

                            <c:choose>

                                <c:when test="${not empty content.posterPath}">

                                    <img src="https://image.tmdb.org/t/p/w500${content.posterPath}"
                                         alt="${content.title}"
                                         loading="lazy"/>

                                </c:when>

                                <c:otherwise>

                                    <div class="no-img">
                                        NO IMAGE
                                    </div>

                                </c:otherwise>

                            </c:choose>

                        </div>

                        <div class="content-card__info">

                            <h3 class="content-card__title">
                                ${content.title}
                            </h3>

                            <div class="content-card__meta">

                                <span class="content-type">

                                    <c:choose>

                                        <c:when test="${content.contentType eq 'MOVIE'}">
                                            영화
                                        </c:when>

                                        <c:when test="${content.contentType eq 'TV'}">
                                            TV
                                        </c:when>

                                        <c:otherwise>
                                            콘텐츠
                                        </c:otherwise>

                                    </c:choose>

                                </span>

                                <c:if test="${not empty content.tmdbScore}">

                                    <span class="content-rating">
                                        ⭐ ${content.tmdbScore}
                                    </span>

                                </c:if>

                            </div>

                            <div class="content-card__sub">

                                <c:choose>

                                    <c:when test="${not empty content.genreText}">
                                        ${content.genreText}
                                    </c:when>

                                    <c:when test="${not empty content.releaseDate}">
                                        ${content.releaseDate}
                                    </c:when>

                                    <c:otherwise>
                                        콘텐츠 정보 없음
                                    </c:otherwise>

                                </c:choose>

                            </div>

                        </div>

                    </a>

                    <c:if test="${not empty content.contentNo}">

                        <button type="button"
                                class="content-card__fav-btn"
                                data-content-no="${content.contentNo}">
                            ♡
                        </button>

                    </c:if>

                </article>

            </c:forEach>

        </section>

    </c:if>

</main>

<jsp:include page="/WEB-INF/views/common/footer.jsp"/>

</body>

</html>