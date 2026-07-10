<%@ page language="java"
         contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8" %>

<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<!DOCTYPE html>
<html lang="ko">

<head>

    <meta charset="UTF-8">

    <meta name="viewport"
          content="width=device-width, initial-scale=1.0">

    <title>ODITJI | 검색 결과</title>

    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/css/search.css">

    <script defer
            src="${pageContext.request.contextPath}/js/search.js?v=6">
    </script>

</head>

<body>

<jsp:include page="/WEB-INF/views/common/header.jsp"/>

<main class="search-page-container">

    <div class="search-layout">

        <%-- =================================================
             왼쪽 검색 필터
        ================================================= --%>
        <aside class="search-left-sidebar">

            <jsp:include page="/WEB-INF/views/common/leftSidebar.jsp"/>

        </aside>


        <%-- =================================================
             검색 결과 영역
        ================================================= --%>
        <section class="search-result-area"
                 data-search-root
                 data-initial-tab="${empty searchTab ? 'ALL' : searchTab}">


            <%-- =================================================
                 검색 결과 헤더
            ================================================= --%>
            <section class="search-result-header">

                <div>

                    <h1>
                        <c:out value="${searchTitle}"/>
                    </h1>

                </div>

                <div class="search-result-count">

                    전체
                    <c:out value="${combinedTotalCount}"/>건

                </div>

            </section>


            <%-- =================================================
                 검색 결과 탭
            ================================================= --%>
            <nav class="search-result-tabs"
                 aria-label="검색 결과 유형">

                <button type="button"
                        class="search-result-tab"
                        data-search-tab="ALL"
                        aria-selected="false">

                    전체

                    <span>
                        <c:out value="${combinedTotalCount}"/>
                    </span>

                </button>

                <button type="button"
                        class="search-result-tab"
                        data-search-tab="CONTENT"
                        aria-selected="false">

                    콘텐츠

                    <span>
                        <c:out value="${contentTotalCount}"/>
                    </span>

                </button>

                <button type="button"
                        class="search-result-tab"
                        data-search-tab="GOODS"
                        aria-selected="false">

                    상품

                    <span>
                        <c:out value="${goodsTotalCount}"/>
                    </span>

                </button>

            </nav>


            <%-- =================================================
                 전체 탭
            ================================================= --%>
            <section class="search-tab-panel"
                     data-search-panel="ALL">


                <%-- =================================================
                     전체 탭: 항상 1페이지 콘텐츠 상위 5개
                ================================================= --%>
                <section class="search-all-section">

                    <div class="search-section-heading">

                        <h2>콘텐츠</h2>

                        <button type="button"
                                class="search-more-button"
                                data-search-move-tab="CONTENT">

                            더보기

                        </button>

                    </div>


                    <c:choose>

                        <c:when test="${empty allContentResults}">

                            <section class="empty-state search-all-empty">

                                <p>검색된 콘텐츠가 없습니다.</p>

                            </section>

                        </c:when>

                        <c:otherwise>

                            <section class="search-content-list">

                                <c:forEach var="content"
                                           items="${allContentResults}">

                                    <article class="search-content-item">

                                        <c:url var="allContentDetailUrl"
                                               value="/content/prepare">

                                            <c:param name="tmdbId"
                                                     value="${content.tmdbId}"/>

                                            <c:param name="contentType"
                                                     value="${content.contentType}"/>

                                        </c:url>


                                        <a class="search-content-link"
                                           href="${allContentDetailUrl}">

                                            <div class="search-content-poster">

                                                <c:choose>

                                                    <c:when test="${not empty content.posterPath}">

                                                        <img src="https://image.tmdb.org/t/p/w300${content.posterPath}"
                                                             alt="<c:out value='${content.title}'/>"
                                                             loading="lazy">

                                                    </c:when>

                                                    <c:otherwise>

                                                        <div class="search-content-no-image">
                                                            NO IMAGE
                                                        </div>

                                                    </c:otherwise>

                                                </c:choose>

                                            </div>


                                            <div class="search-content-info">

                                                <h3 class="search-content-title">

                                                    <c:out value="${content.title}"/>

                                                </h3>


                                                <c:if test="${content.matchType eq 'PERSON'}">

                                                    <p class="search-person-match">

                                                        <c:out value="${content.matchedPersonRole}"/>
                                                        검색 결과 ·

                                                        <c:out value="${content.matchedPersonName}"/>

                                                        <c:choose>

                                                            <c:when test="${content.matchedPersonRole eq '감독'}">
                                                                연출
                                                            </c:when>

                                                            <c:otherwise>
                                                                출연
                                                            </c:otherwise>

                                                        </c:choose>

                                                    </p>

                                                </c:if>


                                                <div class="search-content-meta">

                                                    <span class="search-content-type">

                                                        <c:choose>

                                                            <c:when test="${content.contentType eq 'MOVIE'}">
                                                                영화
                                                            </c:when>

                                                            <c:otherwise>
                                                                TV
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


                                                <c:if test="${not empty content.platformList}">

                                                    <div class="search-content-platform-list">

                                                        <c:forEach var="platform"
                                                                   items="${content.platformList}">

                                                            <img class="search-content-platform-logo"
                                                                 src="${platform.logoImage}"
                                                                 alt="<c:out value='${platform.platformName}'/>"
                                                                 title="<c:out value='${platform.platformName}'/>">

                                                        </c:forEach>

                                                    </div>

                                                </c:if>

                                            </div>


                                            <c:if test="${not empty content.tmdbScore}">

                                                <div class="search-content-score">

                                                    ★

                                                    <fmt:formatNumber value="${content.tmdbScore}"
                                                                      pattern="0.0"/>

                                                </div>

                                            </c:if>

                                        </a>

                                    </article>

                                </c:forEach>

                            </section>

                        </c:otherwise>

                    </c:choose>

                </section>


                <%-- =================================================
                     전체 탭: 상품 상위 5개
                ================================================= --%>
                <section class="search-all-section">

                    <div class="search-section-heading">

                        <h2>상품</h2>

                        <button type="button"
                                class="search-more-button"
                                data-search-move-tab="GOODS">

                            더보기

                        </button>

                    </div>


                    <c:choose>

                        <c:when test="${empty allGoodsResults}">

                            <section class="empty-state search-all-empty">

                                <p>검색된 상품이 없습니다.</p>

                            </section>

                        </c:when>

                        <c:otherwise>

                            <section class="search-goods-grid search-all-goods-grid">

                                <c:forEach var="goods"
                                           items="${allGoodsResults}">

                                    <article class="search-goods-card
                                            ${goods.stock <= 0 ? 'is-soldout' : ''}">

                                        <a href="${pageContext.request.contextPath}/goods/goodsDetail/${goods.productNo}">

                                            <div class="search-goods-image">

                                                <c:choose>

                                                    <c:when test="${not empty goods.mainImage}">

                                                        <img src="${goods.mainImage}"
                                                             alt="<c:out value='${goods.productName}'/>"
                                                             loading="lazy">

                                                    </c:when>

                                                    <c:otherwise>

                                                        <div class="search-goods-no-image">
                                                            NO IMAGE
                                                        </div>

                                                    </c:otherwise>

                                                </c:choose>


                                                <c:if test="${goods.stock <= 0}">

                                                    <span class="search-goods-soldout">
                                                        SOLD OUT
                                                    </span>

                                                </c:if>

                                            </div>


                                            <div class="search-goods-info">

                                                <p class="search-goods-business">

                                                    <c:out value="${goods.businessName}"/>

                                                </p>

                                                <h3>

                                                    <c:out value="${goods.productName}"/>

                                                </h3>


                                                <c:choose>

                                                    <c:when test="${goods.discountRate > 0}">

                                                        <p class="search-goods-price">

                                                            <span class="search-goods-rate">

                                                                <c:out value="${goods.discountRate}"/>%

                                                            </span>

                                                            ₩

                                                            <fmt:formatNumber value="${goods.discountPrice}"
                                                                              pattern="#,###"/>

                                                        </p>

                                                        <p class="search-goods-original">

                                                            ₩

                                                            <fmt:formatNumber value="${goods.price}"
                                                                              pattern="#,###"/>

                                                        </p>

                                                    </c:when>

                                                    <c:otherwise>

                                                        <p class="search-goods-price">

                                                            ₩

                                                            <fmt:formatNumber value="${goods.price}"
                                                                              pattern="#,###"/>

                                                        </p>

                                                    </c:otherwise>

                                                </c:choose>

                                            </div>

                                        </a>

                                    </article>

                                </c:forEach>

                            </section>

                        </c:otherwise>

                    </c:choose>

                </section>

            </section>


            <%-- =================================================
                 콘텐츠 탭
            ================================================= --%>
            <section class="search-tab-panel"
                     data-search-panel="CONTENT"
                     hidden>

                <div class="search-section-heading">

                    <h2>콘텐츠</h2>

                    <span>
                        <c:out value="${contentTotalCount}"/>건
                    </span>

                </div>


                <c:choose>

                    <c:when test="${empty contentResults}">

                        <section class="empty-state">

                            <p>검색된 콘텐츠가 없습니다.</p>

                        </section>

                    </c:when>

                    <c:otherwise>

                        <section class="search-content-list">

                            <c:forEach var="content"
                                       items="${contentResults}">

                                <article class="search-content-item">

                                    <c:url var="contentDetailUrl"
                                           value="/content/prepare">

                                        <c:param name="tmdbId"
                                                 value="${content.tmdbId}"/>

                                        <c:param name="contentType"
                                                 value="${content.contentType}"/>

                                    </c:url>


                                    <a class="search-content-link"
                                       href="${contentDetailUrl}">

                                        <div class="search-content-poster">

                                            <c:choose>

                                                <c:when test="${not empty content.posterPath}">

                                                    <img src="https://image.tmdb.org/t/p/w300${content.posterPath}"
                                                         alt="<c:out value='${content.title}'/>"
                                                         loading="lazy">

                                                </c:when>

                                                <c:otherwise>

                                                    <div class="search-content-no-image">
                                                        NO IMAGE
                                                    </div>

                                                </c:otherwise>

                                            </c:choose>

                                        </div>


                                        <div class="search-content-info">

                                            <h3 class="search-content-title">

                                                <c:out value="${content.title}"/>

                                            </h3>


                                            <c:if test="${content.matchType eq 'PERSON'}">

                                                <p class="search-person-match">

                                                    <c:out value="${content.matchedPersonRole}"/>
                                                    검색 결과 ·

                                                    <c:out value="${content.matchedPersonName}"/>

                                                    <c:choose>

                                                        <c:when test="${content.matchedPersonRole eq '감독'}">
                                                            연출
                                                        </c:when>

                                                        <c:otherwise>
                                                            출연
                                                        </c:otherwise>

                                                    </c:choose>

                                                </p>

                                            </c:if>


                                            <div class="search-content-meta">

                                                <span class="search-content-type">

                                                    <c:choose>

                                                        <c:when test="${content.contentType eq 'MOVIE'}">
                                                            영화
                                                        </c:when>

                                                        <c:otherwise>
                                                            TV
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


                                            <c:if test="${not empty content.platformList}">

                                                <div class="search-content-platform-list">

                                                    <c:forEach var="platform"
                                                               items="${content.platformList}">

                                                        <img class="search-content-platform-logo"
                                                             src="${platform.logoImage}"
                                                             alt="<c:out value='${platform.platformName}'/>"
                                                             title="<c:out value='${platform.platformName}'/>">

                                                    </c:forEach>

                                                </div>

                                            </c:if>

                                        </div>


                                        <c:if test="${not empty content.tmdbScore}">

                                            <div class="search-content-score">

                                                ★

                                                <fmt:formatNumber value="${content.tmdbScore}"
                                                                  pattern="0.0"/>

                                            </div>

                                        </c:if>

                                    </a>

                                </article>

                            </c:forEach>

                        </section>


                        <%-- =================================================
                             페이지 범위 계산

                             현재 페이지 -2 ~ 현재 페이지 +2
                        ================================================= --%>
                        <c:set var="startPage"
                               value="${currentPage - 2}"/>

                        <c:if test="${startPage < 1}">

                            <c:set var="startPage"
                                   value="1"/>

                        </c:if>


                        <c:set var="endPage"
                               value="${currentPage + 2}"/>

                        <c:if test="${endPage > pageVO.totalPages}">

                            <c:set var="endPage"
                                   value="${pageVO.totalPages}"/>

                        </c:if>


                        <c:if test="${pageVO.totalPages > 1}">

                            <section class="pagination search-pagination">


                                <%-- 이전 페이지 --%>
                                <c:if test="${currentPage > 1}">

                                    <c:url var="prevUrl"
                                           value="/search">

                                        <c:param name="keyword"
                                                 value="${keyword}"/>

                                        <c:param name="page"
                                                 value="${currentPage - 1}"/>

                                        <c:param name="searchTab"
                                                 value="CONTENT"/>

                                        <c:forEach var="contentType"
                                                   items="${contentTypes}">

                                            <c:param name="contentTypes"
                                                     value="${contentType}"/>

                                        </c:forEach>

                                        <c:forEach var="genreCode"
                                                   items="${genreCodes}">

                                            <c:param name="genreCodes"
                                                     value="${genreCode}"/>

                                        </c:forEach>

                                        <c:forEach var="providerId"
                                                   items="${providerIds}">

                                            <c:param name="providerIds"
                                                     value="${providerId}"/>

                                        </c:forEach>

                                    </c:url>

                                    <a class="page-btn page-arrow"
                                       href="${prevUrl}"
                                       aria-label="이전 페이지">

                                        ‹

                                    </a>

                                </c:if>


                                <%-- 첫 페이지 --%>
                                <c:if test="${startPage > 1}">

                                    <c:url var="firstPageUrl"
                                           value="/search">

                                        <c:param name="keyword"
                                                 value="${keyword}"/>

                                        <c:param name="page"
                                                 value="1"/>

                                        <c:param name="searchTab"
                                                 value="CONTENT"/>

                                        <c:forEach var="contentType"
                                                   items="${contentTypes}">

                                            <c:param name="contentTypes"
                                                     value="${contentType}"/>

                                        </c:forEach>

                                        <c:forEach var="genreCode"
                                                   items="${genreCodes}">

                                            <c:param name="genreCodes"
                                                     value="${genreCode}"/>

                                        </c:forEach>

                                        <c:forEach var="providerId"
                                                   items="${providerIds}">

                                            <c:param name="providerIds"
                                                     value="${providerId}"/>

                                        </c:forEach>

                                    </c:url>

                                    <a class="page-btn"
                                       href="${firstPageUrl}">

                                        1

                                    </a>

                                </c:if>


                                <%-- 첫 페이지와 범위 사이 생략 --%>
                                <c:if test="${startPage > 2}">

                                    <span class="page-ellipsis">
                                        ...
                                    </span>

                                </c:if>


                                <%-- 현재 페이지 기준 -2 ~ +2 --%>
                                <c:forEach var="pageNumber"
                                           begin="${startPage}"
                                           end="${endPage}">

                                    <c:choose>

                                        <c:when test="${pageNumber == currentPage}">

                                            <span class="page-now">

                                                <c:out value="${pageNumber}"/>

                                            </span>

                                        </c:when>

                                        <c:otherwise>

                                            <c:url var="pageUrl"
                                                   value="/search">

                                                <c:param name="keyword"
                                                         value="${keyword}"/>

                                                <c:param name="page"
                                                         value="${pageNumber}"/>

                                                <c:param name="searchTab"
                                                         value="CONTENT"/>

                                                <c:forEach var="contentType"
                                                           items="${contentTypes}">

                                                    <c:param name="contentTypes"
                                                             value="${contentType}"/>

                                                </c:forEach>

                                                <c:forEach var="genreCode"
                                                           items="${genreCodes}">

                                                    <c:param name="genreCodes"
                                                             value="${genreCode}"/>

                                                </c:forEach>

                                                <c:forEach var="providerId"
                                                           items="${providerIds}">

                                                    <c:param name="providerIds"
                                                             value="${providerId}"/>

                                                </c:forEach>

                                            </c:url>

                                            <a class="page-btn"
                                               href="${pageUrl}">

                                                <c:out value="${pageNumber}"/>

                                            </a>

                                        </c:otherwise>

                                    </c:choose>

                                </c:forEach>


                                <%-- 범위와 마지막 페이지 사이 생략 --%>
                                <c:if test="${endPage < pageVO.totalPages - 1}">

                                    <span class="page-ellipsis">
                                        ...
                                    </span>

                                </c:if>


                                <%-- 마지막 페이지 --%>
                                <c:if test="${endPage < pageVO.totalPages}">

                                    <c:url var="lastPageUrl"
                                           value="/search">

                                        <c:param name="keyword"
                                                 value="${keyword}"/>

                                        <c:param name="page"
                                                 value="${pageVO.totalPages}"/>

                                        <c:param name="searchTab"
                                                 value="CONTENT"/>

                                        <c:forEach var="contentType"
                                                   items="${contentTypes}">

                                            <c:param name="contentTypes"
                                                     value="${contentType}"/>

                                        </c:forEach>

                                        <c:forEach var="genreCode"
                                                   items="${genreCodes}">

                                            <c:param name="genreCodes"
                                                     value="${genreCode}"/>

                                        </c:forEach>

                                        <c:forEach var="providerId"
                                                   items="${providerIds}">

                                            <c:param name="providerIds"
                                                     value="${providerId}"/>

                                        </c:forEach>

                                    </c:url>

                                    <a class="page-btn"
                                       href="${lastPageUrl}">

                                        <c:out value="${pageVO.totalPages}"/>

                                    </a>

                                </c:if>


                                <%-- 다음 페이지 --%>
                                <c:if test="${currentPage < pageVO.totalPages}">

                                    <c:url var="nextUrl"
                                           value="/search">

                                        <c:param name="keyword"
                                                 value="${keyword}"/>

                                        <c:param name="page"
                                                 value="${currentPage + 1}"/>

                                        <c:param name="searchTab"
                                                 value="CONTENT"/>

                                        <c:forEach var="contentType"
                                                   items="${contentTypes}">

                                            <c:param name="contentTypes"
                                                     value="${contentType}"/>

                                        </c:forEach>

                                        <c:forEach var="genreCode"
                                                   items="${genreCodes}">

                                            <c:param name="genreCodes"
                                                     value="${genreCode}"/>

                                        </c:forEach>

                                        <c:forEach var="providerId"
                                                   items="${providerIds}">

                                            <c:param name="providerIds"
                                                     value="${providerId}"/>

                                        </c:forEach>

                                    </c:url>

                                    <a class="page-btn page-arrow"
                                       href="${nextUrl}"
                                       aria-label="다음 페이지">

                                        ›

                                    </a>

                                </c:if>

                            </section>

                        </c:if>

                    </c:otherwise>

                </c:choose>

            </section>


            <%-- =================================================
                 상품 탭
            ================================================= --%>
            <section class="search-tab-panel"
                     data-search-panel="GOODS"
                     hidden>

                <div class="search-section-heading">

                    <h2>상품</h2>

                    <span>
                        <c:out value="${goodsTotalCount}"/>건
                    </span>

                </div>


                <c:choose>

                    <c:when test="${empty goodsResults}">

                        <section class="empty-state">

                            <p>검색된 상품이 없습니다.</p>

                        </section>

                    </c:when>

                    <c:otherwise>

                        <section class="search-goods-grid">

                            <c:forEach var="goods"
                                       items="${goodsResults}">

                                <article class="search-goods-card
                                        ${goods.stock <= 0 ? 'is-soldout' : ''}">

                                    <a href="${pageContext.request.contextPath}/goods/goodsDetail/${goods.productNo}">

                                        <div class="search-goods-image">

                                            <c:choose>

                                                <c:when test="${not empty goods.mainImage}">

                                                    <img src="${goods.mainImage}"
                                                         alt="<c:out value='${goods.productName}'/>"
                                                         loading="lazy">

                                                </c:when>

                                                <c:otherwise>

                                                    <div class="search-goods-no-image">
                                                        NO IMAGE
                                                    </div>

                                                </c:otherwise>

                                            </c:choose>


                                            <c:if test="${goods.stock <= 0}">

                                                <span class="search-goods-soldout">
                                                    SOLD OUT
                                                </span>

                                            </c:if>

                                        </div>


                                        <div class="search-goods-info">

                                            <p class="search-goods-business">

                                                <c:out value="${goods.businessName}"/>

                                            </p>

                                            <h3>

                                                <c:out value="${goods.productName}"/>

                                            </h3>


                                            <c:choose>

                                                <c:when test="${goods.discountRate > 0}">

                                                    <p class="search-goods-price">

                                                        <span class="search-goods-rate">

                                                            <c:out value="${goods.discountRate}"/>%

                                                        </span>

                                                        ₩

                                                        <fmt:formatNumber value="${goods.discountPrice}"
                                                                          pattern="#,###"/>

                                                    </p>

                                                    <p class="search-goods-original">

                                                        ₩

                                                        <fmt:formatNumber value="${goods.price}"
                                                                          pattern="#,###"/>

                                                    </p>

                                                </c:when>

                                                <c:otherwise>

                                                    <p class="search-goods-price">

                                                        ₩

                                                        <fmt:formatNumber value="${goods.price}"
                                                                          pattern="#,###"/>

                                                    </p>

                                                </c:otherwise>

                                            </c:choose>

                                        </div>

                                    </a>

                                </article>

                            </c:forEach>

                        </section>

                    </c:otherwise>

                </c:choose>

            </section>

        </section>

    </div>

</main>

<jsp:include page="/WEB-INF/views/common/footer.jsp"/>

</body>
</html>