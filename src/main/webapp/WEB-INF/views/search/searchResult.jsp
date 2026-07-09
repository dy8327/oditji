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

    <title>ODITJI | 검색 결과</title>

    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/css/content.css">

    <script defer
            src="${pageContext.request.contextPath}/js/content.js"></script>

    <script defer
            src="${pageContext.request.contextPath}/js/search.js"></script>

</head>

<body>

<jsp:include page="/WEB-INF/views/common/header.jsp"/>

<main class="content-container search-page-container">

    <div class="search-layout">

        <!-- =====================================
             LEFT SIDEBAR
        ====================================== -->
        <aside class="search-left-sidebar">

            <jsp:include page="/WEB-INF/views/common/leftSidebar.jsp"/>

        </aside>

        <!-- =====================================
             SEARCH RESULT AREA
        ====================================== -->
        <section class="search-result-area">

            <!-- =====================================
                 SEARCH RESULT HEADER
            ====================================== -->
            <section class="content-header search-result-header">

                <div>

                    <c:choose>

                        <c:when test="${not empty searchTitle}">

                            <h1>
                                <c:out value="${searchTitle}"/>
                            </h1>

                        </c:when>

                        <c:when test="${not empty keyword}">

                            <h1>
                                "<c:out value="${keyword}"/>" 검색 결과
                            </h1>

                        </c:when>

                        <c:otherwise>

                            <h1>
                                지금 인기 있는 콘텐츠
                            </h1>

                        </c:otherwise>

                    </c:choose>

                </div>

                <div class="search-result-count">

                    <c:choose>

                        <c:when test="${not empty pageVO}">
                            전체
                            <c:out value="${pageVO.totalResults}"/>
                            건
                        </c:when>

                        <c:otherwise>
                            전체
                            <c:out value="${fn:length(resultList)}"/>
                            건
                        </c:otherwise>

                    </c:choose>

                </div>

            </section>

            <!-- =====================================
                 EMPTY RESULT
            ====================================== -->
            <c:if test="${empty resultList}">

                <section class="empty-state">

                    <p>
                        검색 결과가 없습니다.
                    </p>

                </section>

            </c:if>

            <!-- =====================================
                 RESULT LIST
            ====================================== -->
            <c:if test="${not empty resultList}">

                <section class="search-content-list">

                    <c:forEach var="content"
                            items="${resultList}">

                        <article class="search-content-item">

                            <c:url var="detailUrl"
                                value="/content/prepare">

                                <c:param name="tmdbId"
                                        value="${content.tmdbId}"/>

                                <c:param name="contentType"
                                        value="${content.contentType}"/>

                            </c:url>

                            <a class="search-content-link"
                            href="${detailUrl}">

                                <!-- 포스터 -->
                                <div class="search-content-poster">

                                    <c:choose>

                                        <c:when test="${not empty content.posterPath}">

                                            <img src="https://image.tmdb.org/t/p/w300${content.posterPath}"
                                                alt="<c:out value='${content.title}'/>"
                                                loading="lazy"/>

                                        </c:when>

                                        <c:otherwise>

                                            <div class="search-content-no-image">
                                                NO IMAGE
                                            </div>

                                        </c:otherwise>

                                    </c:choose>

                                </div>

                                <!-- 작품 정보 -->
                                <div class="search-content-info">

                                    <h3 class="search-content-title">
                                        <c:out value="${content.title}"/>
                                    </h3>

                                    <div class="search-content-meta">

                                        <span>

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

                                        <c:if test="${not empty content.releaseDate}">

                                            <span>
                                                <c:out value="${content.releaseDate}"/>
                                            </span>

                                        </c:if>

                                        <c:if test="${not empty content.genreText}">

                                            <span>
                                                <c:out value="${content.genreText}"/>
                                            </span>

                                        </c:if>

                                    </div>

                                </div>

                                <!-- 평점 -->
                                <c:if test="${not empty content.tmdbScore}">

                                    <div class="search-content-score">
                                        ★
                                        <c:out value="${content.tmdbScore}"/>
                                    </div>

                                </c:if>

                            </a>

                            <!-- 찜 버튼 -->
                            <c:if test="${not empty content.contentNo}">

                                <button type="button"
                                        class="search-content-favorite"
                                        data-content-no="${content.contentNo}"
                                        aria-label="<c:out value='${content.title}'/> 찜하기">
                                    ♡
                                </button>

                            </c:if>

                        </article>

                    </c:forEach>

                </section>

                <!-- =====================================
                     PAGINATION
                ====================================== -->
                <c:if test="${not empty pageVO
                              and pageVO.totalPages > 1}">

                    <!--
                        TMDB 검색 API는 일반적으로 최대 500페이지까지만
                        정상 접근하도록 제한하는 것이 안전하다.
                    -->
                    <c:set var="availableTotalPages"
                           value="${pageVO.totalPages > 500
                                    ? 500
                                    : pageVO.totalPages}"/>

                    <!-- 현재 페이지 기준 앞쪽 2개 -->
                    <c:set var="startPage"
                           value="${pageVO.page - 2}"/>

                    <!-- 현재 페이지 기준 뒤쪽 2개 -->
                    <c:set var="endPage"
                           value="${pageVO.page + 2}"/>

                    <!-- 시작 페이지가 1보다 작으면 1로 보정 -->
                    <c:if test="${startPage < 1}">

                        <c:set var="startPage"
                               value="1"/>

                    </c:if>

                    <!-- 끝 페이지가 전체 페이지보다 크면 보정 -->
                    <c:if test="${endPage > availableTotalPages}">

                        <c:set var="endPage"
                               value="${availableTotalPages}"/>

                    </c:if>

                    <!-- 마지막 부분에서도 최대 5개가 보이도록 시작 위치 보정 -->
                    <c:if test="${endPage - startPage < 4
                                  and endPage == availableTotalPages}">

                        <c:set var="startPage"
                               value="${endPage - 4}"/>

                        <c:if test="${startPage < 1}">

                            <c:set var="startPage"
                                   value="1"/>

                        </c:if>

                    </c:if>

                    <!-- 첫 부분에서도 최대 5개가 보이도록 끝 위치 보정 -->
                    <c:if test="${endPage - startPage < 4
                                  and startPage == 1}">

                        <c:set var="endPage"
                               value="${startPage + 4}"/>

                        <c:if test="${endPage > availableTotalPages}">

                            <c:set var="endPage"
                                   value="${availableTotalPages}"/>

                        </c:if>

                    </c:if>

                    <nav class="pagination"
                         aria-label="검색 결과 페이지">

                        <!-- 이전 페이지 -->
                        <c:if test="${pageVO.page > 1}">

                            <c:url var="previousPageUrl"
                                   value="/search">

                                <c:param name="keyword"
                                         value="${keyword}"/>

                                <c:param name="page"
                                         value="${pageVO.page - 1}"/>

                                <c:forEach var="type"
                                           items="${contentTypes}">

                                    <c:param name="contentTypes"
                                             value="${type}"/>

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

                            <a class="page-btn page-btn--previous"
                               href="${previousPageUrl}">
                                이전
                            </a>

                        </c:if>

                        <!-- 첫 페이지 바로가기 -->
                        <c:if test="${startPage > 1}">

                            <c:url var="firstPageUrl"
                                   value="/search">

                                <c:param name="keyword"
                                         value="${keyword}"/>

                                <c:param name="page"
                                         value="1"/>

                                <c:forEach var="type"
                                           items="${contentTypes}">

                                    <c:param name="contentTypes"
                                             value="${type}"/>

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
                               href="${firstPageUrl}">
                                1
                            </a>

                            <c:if test="${startPage > 2}">

                                <span class="page-ellipsis">
                                    ...
                                </span>

                            </c:if>

                        </c:if>

                        <!-- 현재 페이지 주변 번호 -->
                        <c:forEach var="pageNumber"
                                   begin="${startPage}"
                                   end="${endPage}">

                            <c:choose>

                                <c:when test="${pageNumber eq pageVO.page}">

                                    <span class="page-now"
                                          aria-current="page">
                                        ${pageNumber}
                                    </span>

                                </c:when>

                                <c:otherwise>

                                    <c:url var="pageUrl"
                                           value="/search">

                                        <c:param name="keyword"
                                                 value="${keyword}"/>

                                        <c:param name="page"
                                                 value="${pageNumber}"/>

                                        <c:forEach var="type"
                                                   items="${contentTypes}">

                                            <c:param name="contentTypes"
                                                     value="${type}"/>

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
                                       href="${pageUrl}">
                                        ${pageNumber}
                                    </a>

                                </c:otherwise>

                            </c:choose>

                        </c:forEach>

                        <!-- 마지막 페이지 바로가기 -->
                        <c:if test="${endPage < availableTotalPages}">

                            <c:if test="${endPage < availableTotalPages - 1}">

                                <span class="page-ellipsis">
                                    ...
                                </span>

                            </c:if>

                            <c:url var="lastPageUrl"
                                   value="/search">

                                <c:param name="keyword"
                                         value="${keyword}"/>

                                <c:param name="page"
                                         value="${availableTotalPages}"/>

                                <c:forEach var="type"
                                           items="${contentTypes}">

                                    <c:param name="contentTypes"
                                             value="${type}"/>

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
                               href="${lastPageUrl}">
                                ${availableTotalPages}
                            </a>

                        </c:if>

                        <!-- 다음 페이지 -->
                        <c:if test="${pageVO.page < availableTotalPages}">

                            <c:url var="nextPageUrl"
                                   value="/search">

                                <c:param name="keyword"
                                         value="${keyword}"/>

                                <c:param name="page"
                                         value="${pageVO.page + 1}"/>

                                <c:forEach var="type"
                                           items="${contentTypes}">

                                    <c:param name="contentTypes"
                                             value="${type}"/>

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

                            <a class="page-btn page-btn--next"
                               href="${nextPageUrl}">
                                다음
                            </a>

                        </c:if>

                    </nav>

                </c:if>

            </c:if>

        </section>

        <!-- =====================================
             RIGHT SIDEBAR
        ====================================== -->
        <aside class="search-right-sidebar">

            <jsp:include page="/WEB-INF/views/common/rightSidebar.jsp"/>

        </aside>

    </div>

</main>

<jsp:include page="/WEB-INF/views/common/footer.jsp"/>

</body>

</html>