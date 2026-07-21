<%@ page language="java"
         contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8" %>

<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<!DOCTYPE html>
<html lang="ko">

<head>
    <meta charset="UTF-8">
    <meta name="viewport"
          content="width=device-width, initial-scale=1.0">

    <title>ODITJI | ${pageTitle}</title>

    <link rel="stylesheet"
      <link rel="stylesheet"
      href="${pageContext.request.contextPath}/css/component.css">
      href="${pageContext.request.contextPath}/css/content.css?v=22">

    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/css/contentTopTabs.css?v=1">

<script>
    const contextPath = "${pageContext.request.contextPath}";
</script>

<script defer
        src="${pageContext.request.contextPath}/js/content.js"></script>

<script defer
        src="${pageContext.request.contextPath}/js/favorite.js"></script>
    <script defer
            src="${pageContext.request.contextPath}/js/contentList.js?v=3">
    </script>
</head>

<body>

<jsp:include page="/WEB-INF/views/common/header.jsp"/>

<main class="content-list-page">

    <jsp:include page="/WEB-INF/views/common/contentTopTabs.jsp">
        <jsp:param name="activeTab" value="${type}"/>
    </jsp:include>

    <div class="content-list-layout">

        <aside class="content-list-left-sidebar">
            <jsp:include page="/WEB-INF/views/common/contentLeftSidebar.jsp"/>
        </aside>

        <section class="content-list-main">

            <header class="content-list-header">

                <div>
                    <h1>${pageTitle}</h1>

                    <p>
                        한국에서 정액제로 시청 가능한 콘텐츠만 표시합니다.
                    </p>
                </div>

                <span class="content-list-result-count">
                    약 ${totalCount}건
                </span>

            </header>

            <c:choose>

                <c:when test="${empty contentList}">

                    <section class="empty-state">
                        <p>조건에 맞는 콘텐츠가 없습니다.</p>
                    </section>

                </c:when>

                <c:otherwise>

                    <section class="content-list-card-grid">

                        <c:forEach var="content"
                                   items="${contentList}">

                            <article class="content-list-card">

                                <c:url var="detailUrl"
                                       value="/content/prepare">

                                    <c:param name="tmdbId"
                                             value="${content.tmdbId}"/>

                                    <c:param name="contentType"
                                             value="${content.contentType}"/>

                                </c:url>

                                <button type="button"
                                        class="content-list-favorite-btn"
                                        data-content-list-favorite
                                        data-tmdb-id="${content.tmdbId}"
                                        data-content-type="${content.contentType}"
                                        aria-pressed="false"
                                        aria-label="<c:out value='${content.title}'/> 찜하기"
                                        title="찜하기">
                                    <span aria-hidden="true">♡</span>
                                </button>

                                <a class="content-list-card-link"
                                   href="${detailUrl}">

                                    <div class="content-list-card-poster">

                                        <c:choose>

                                            <c:when test="${not empty content.posterPath}">

                                                <img src="https://image.tmdb.org/t/p/w500${content.posterPath}"
                                                     alt="<c:out value='${content.title}'/>"
                                                     loading="lazy">

                                            </c:when>

                                            <c:otherwise>

                                                <div class="no-img">
                                                    NO IMAGE
                                                </div>

                                            </c:otherwise>

                                        </c:choose>

                                        <span class="content-list-type-badge">

                                            <c:choose>

                                                <c:when test="${content.contentType eq 'MOVIE'}">
                                                    영화
                                                </c:when>

                                                <c:otherwise>
                                                    시리즈
                                                </c:otherwise>

                                            </c:choose>

                                        </span>

                                    </div>

                                    <div class="content-list-card-info">

                                        <h2>
                                            <c:out value="${content.title}"/>
                                        </h2>

                                        <div class="content-list-card-meta">

                                            <span>
                                                <c:choose>
                                                    <c:when test="${not empty content.releaseDate}">
                                                        ${fn:substring(content.releaseDate, 0, 4)}
                                                    </c:when>
                                                    <c:otherwise>
                                                        공개일 미정
                                                    </c:otherwise>
                                                </c:choose>
                                            </span>

                                            <c:if test="${not empty content.tmdbScore}">
                                                <span>⭐ ${content.tmdbScore}</span>
                                            </c:if>

                                        </div>

                                        <c:if test="${not empty content.genreText}">
                                            <p class="content-list-card-genre">
                                                <c:out value="${content.genreText}"/>
                                            </p>
                                        </c:if>

                                    </div>

                                </a>

                            </article>

                        </c:forEach>

                    </section>

                </c:otherwise>

            </c:choose>

            <c:if test="${totalPage > 1}">

                <nav class="content-list-pagination"
                     aria-label="콘텐츠 목록 페이지">

                    <c:set var="startPage"
                           value="${page - 2 > 1 ? page - 2 : 1}"/>

                    <c:set var="endPage"
                           value="${page + 2 < totalPage ? page + 2 : totalPage}"/>

                    <c:if test="${page > 1}">

                        <c:url var="previousPageUrl"
                               value="/content/list">

                            <c:param name="type"
                                     value="${type}"/>

                            <c:param name="page"
                                     value="${page - 1}"/>

                            <c:forEach var="category"
                                       items="${contentCategories}">
                                <c:param name="contentCategories"
                                         value="${category}"/>
                            </c:forEach>

                            <c:forEach var="genre"
                                       items="${genreCodes}">
                                <c:param name="genreCodes"
                                         value="${genre}"/>
                            </c:forEach>

                            <c:forEach var="provider"
                                       items="${providerIds}">
                                <c:param name="providerIds"
                                         value="${provider}"/>
                            </c:forEach>

                        </c:url>

                        <a class="page-btn"
                           href="${previousPageUrl}">
                            ‹
                        </a>

                    </c:if>

                    <c:forEach var="pageNumber"
                               begin="${startPage}"
                               end="${endPage}">

                        <c:url var="pageUrl"
                               value="/content/list">

                            <c:param name="type"
                                     value="${type}"/>

                            <c:param name="page"
                                     value="${pageNumber}"/>

                            <c:forEach var="category"
                                       items="${contentCategories}">
                                <c:param name="contentCategories"
                                         value="${category}"/>
                            </c:forEach>

                            <c:forEach var="genre"
                                       items="${genreCodes}">
                                <c:param name="genreCodes"
                                         value="${genre}"/>
                            </c:forEach>

                            <c:forEach var="provider"
                                       items="${providerIds}">
                                <c:param name="providerIds"
                                         value="${provider}"/>
                            </c:forEach>

                        </c:url>

                        <c:choose>

                            <c:when test="${pageNumber == page}">
                                <span class="page-now">
                                    ${pageNumber}
                                </span>
                            </c:when>

                            <c:otherwise>
                                <a class="page-btn"
                                   href="${pageUrl}">
                                    ${pageNumber}
                                </a>
                            </c:otherwise>

                        </c:choose>

                    </c:forEach>

                    <c:if test="${page < totalPage}">

                        <c:url var="nextPageUrl"
                               value="/content/list">

                            <c:param name="type"
                                     value="${type}"/>

                            <c:param name="page"
                                     value="${page + 1}"/>

                            <c:forEach var="category"
                                       items="${contentCategories}">
                                <c:param name="contentCategories"
                                         value="${category}"/>
                            </c:forEach>

                            <c:forEach var="genre"
                                       items="${genreCodes}">
                                <c:param name="genreCodes"
                                         value="${genre}"/>
                            </c:forEach>

            <button
                type="button"
                class="fav-btn"
                data-type="content"
                data-content-no="${c.contentNo}">
                ♡
            </button>
                            <c:forEach var="provider"
                                       items="${providerIds}">
                                <c:param name="providerIds"
                                         value="${provider}"/>
                            </c:forEach>

                        </c:url>

                        <a class="page-btn"
                           href="${nextPageUrl}">
                            ›
                        </a>

                    </c:if>

                </nav>

            </c:if>

        </section>

        <aside class="content-list-right-sidebar">
            <jsp:include page="/WEB-INF/views/common/contentRightSidebar.jsp"/>
        </aside>

    </div>

</main>

<jsp:include page="/WEB-INF/views/common/footer.jsp"/>

</body>
</html>