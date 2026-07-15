<%@ page language="java"
         contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8" %>

<%@ taglib prefix="c" uri="jakarta.tags.core" %>
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
            src="${pageContext.request.contextPath}/js/search.js?v=12">
    </script>

</head>

<body>

<jsp:include page="/WEB-INF/views/common/header.jsp"/>

<main class="search-page-container">

    <div class="search-layout">

        <aside class="search-left-sidebar">

            <jsp:include page="/WEB-INF/views/common/leftSidebar.jsp"/>

        </aside>


        <section class="search-result-area"
                 data-search-root
                 data-initial-tab="${empty searchTab ? 'ALL' : searchTab}">


            <section class="search-result-header">

                <h1>
                    <c:out value="${searchTitle}"/>
                </h1>

                <div class="search-result-count">

                    전체
                    <c:out value="${combinedTotalCount}"/>건

                </div>

            </section>


            <nav class="search-result-tabs"
                 aria-label="검색 결과 유형">

                <button type="button"
                        class="search-result-tab"
                        data-search-tab="ALL">

                    전체

                    <span>
                        <c:out value="${combinedTotalCount}"/>
                    </span>

                </button>

                <button type="button"
                        class="search-result-tab"
                        data-search-tab="CONTENT">

                    콘텐츠

                    <span>
                        <c:out value="${contentTotalCount}"/>
                    </span>

                </button>

                <button type="button"
                        class="search-result-tab"
                        data-search-tab="GOODS">

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

                                                    <span>

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

                                    <article class="search-goods-card">

                                        <a href="${pageContext.request.contextPath}/goods/goodsDetail/${goods.productNo}">

                                            <div class="search-goods-image">

                                                <c:choose>

                                                    <c:when test="${not empty goods.mainImage}">

                                                        <img src="${pageContext.request.contextPath}${goods.mainImage}"
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

                                                <span>

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


                <c:if test="${contentTotalPages > 1}">

                    <c:set var="contentStartPage"
                           value="${contentCurrentPage - 2}"/>

                    <c:if test="${contentStartPage < 1}">

                        <c:set var="contentStartPage"
                               value="1"/>

                    </c:if>

                    <c:set var="contentEndPage"
                           value="${contentCurrentPage + 2}"/>

                    <c:if test="${contentEndPage > contentTotalPages}">

                        <c:set var="contentEndPage"
                               value="${contentTotalPages}"/>

                    </c:if>


                    <section class="pagination search-pagination">


                        <c:if test="${contentCurrentPage > 1}">

                            <c:url var="contentPrevUrl"
                                   value="/search">

                                <c:param name="keyword"
                                         value="${keyword}"/>

                                <c:param name="contentPage"
                                         value="${contentCurrentPage - 1}"/>

                                <c:param name="goodsPage"
                                         value="${goodsCurrentPage}"/>

                                <c:param name="searchTab"
                                         value="CONTENT"/>

                                <c:forEach var="v"
                                           items="${contentTypes}">

                                    <c:param name="contentTypes"
                                             value="${v}"/>

                                </c:forEach>

                                <c:forEach var="v"
                                           items="${genreCodes}">

                                    <c:param name="genreCodes"
                                             value="${v}"/>

                                </c:forEach>

                                <c:forEach var="v"
                                           items="${providerIds}">

                                    <c:param name="providerIds"
                                             value="${v}"/>

                                </c:forEach>

                            </c:url>

                            <a class="page-btn page-arrow"
                               href="${contentPrevUrl}">

                                ‹

                            </a>

                        </c:if>


                        <c:if test="${contentStartPage > 1}">

                            <c:url var="contentFirstUrl"
                                   value="/search">

                                <c:param name="keyword"
                                         value="${keyword}"/>

                                <c:param name="contentPage"
                                         value="1"/>

                                <c:param name="goodsPage"
                                         value="${goodsCurrentPage}"/>

                                <c:param name="searchTab"
                                         value="CONTENT"/>

                                <c:forEach var="v"
                                           items="${contentTypes}">

                                    <c:param name="contentTypes"
                                             value="${v}"/>

                                </c:forEach>

                                <c:forEach var="v"
                                           items="${genreCodes}">

                                    <c:param name="genreCodes"
                                             value="${v}"/>

                                </c:forEach>

                                <c:forEach var="v"
                                           items="${providerIds}">

                                    <c:param name="providerIds"
                                             value="${v}"/>

                                </c:forEach>

                            </c:url>

                            <a class="page-btn"
                               href="${contentFirstUrl}">

                                1

                            </a>

                        </c:if>


                        <c:if test="${contentStartPage > 2}">

                            <span class="page-ellipsis">
                                ...
                            </span>

                        </c:if>


                        <c:forEach var="pageNumber"
                                   begin="${contentStartPage}"
                                   end="${contentEndPage}">

                            <c:choose>

                                <c:when test="${pageNumber == contentCurrentPage}">

                                    <span class="page-now">

                                        <c:out value="${pageNumber}"/>

                                    </span>

                                </c:when>

                                <c:otherwise>

                                    <c:url var="contentPageUrl"
                                           value="/search">

                                        <c:param name="keyword"
                                                 value="${keyword}"/>

                                        <c:param name="contentPage"
                                                 value="${pageNumber}"/>

                                        <c:param name="goodsPage"
                                                 value="${goodsCurrentPage}"/>

                                        <c:param name="searchTab"
                                                 value="CONTENT"/>

                                        <c:forEach var="v"
                                                   items="${contentTypes}">

                                            <c:param name="contentTypes"
                                                     value="${v}"/>

                                        </c:forEach>

                                        <c:forEach var="v"
                                                   items="${genreCodes}">

                                            <c:param name="genreCodes"
                                                     value="${v}"/>

                                        </c:forEach>

                                        <c:forEach var="v"
                                                   items="${providerIds}">

                                            <c:param name="providerIds"
                                                     value="${v}"/>

                                        </c:forEach>

                                    </c:url>

                                    <a class="page-btn"
                                       href="${contentPageUrl}">

                                        <c:out value="${pageNumber}"/>

                                    </a>

                                </c:otherwise>

                            </c:choose>

                        </c:forEach>


                        <c:if test="${contentEndPage < contentTotalPages - 1}">

                            <span class="page-ellipsis">
                                ...
                            </span>

                        </c:if>


                        <c:if test="${contentEndPage < contentTotalPages}">

                            <c:url var="contentLastUrl"
                                   value="/search">

                                <c:param name="keyword"
                                         value="${keyword}"/>

                                <c:param name="contentPage"
                                         value="${contentTotalPages}"/>

                                <c:param name="goodsPage"
                                         value="${goodsCurrentPage}"/>

                                <c:param name="searchTab"
                                         value="CONTENT"/>

                                <c:forEach var="v"
                                           items="${contentTypes}">

                                    <c:param name="contentTypes"
                                             value="${v}"/>

                                </c:forEach>

                                <c:forEach var="v"
                                           items="${genreCodes}">

                                    <c:param name="genreCodes"
                                             value="${v}"/>

                                </c:forEach>

                                <c:forEach var="v"
                                           items="${providerIds}">

                                    <c:param name="providerIds"
                                             value="${v}"/>

                                </c:forEach>

                            </c:url>

                            <a class="page-btn"
                               href="${contentLastUrl}">

                                <c:out value="${contentTotalPages}"/>

                            </a>

                        </c:if>


                        <c:if test="${contentCurrentPage < contentTotalPages}">

                            <c:url var="contentNextUrl"
                                   value="/search">

                                <c:param name="keyword"
                                         value="${keyword}"/>

                                <c:param name="contentPage"
                                         value="${contentCurrentPage + 1}"/>

                                <c:param name="goodsPage"
                                         value="${goodsCurrentPage}"/>

                                <c:param name="searchTab"
                                         value="CONTENT"/>

                                <c:forEach var="v"
                                           items="${contentTypes}">

                                    <c:param name="contentTypes"
                                             value="${v}"/>

                                </c:forEach>

                                <c:forEach var="v"
                                           items="${genreCodes}">

                                    <c:param name="genreCodes"
                                             value="${v}"/>

                                </c:forEach>

                                <c:forEach var="v"
                                           items="${providerIds}">

                                    <c:param name="providerIds"
                                             value="${v}"/>

                                </c:forEach>

                            </c:url>

                            <a class="page-btn page-arrow"
                               href="${contentNextUrl}">

                                ›

                            </a>

                        </c:if>

                    </section>

                </c:if>

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

                                <article class="search-goods-card">

                                    <a href="${pageContext.request.contextPath}/goods/goodsDetail/${goods.productNo}">

                                        <div class="search-goods-image">

                                            <c:choose>

                                                <c:when test="${not empty goods.mainImage}">

                                                    <img src="${pageContext.request.contextPath}${goods.mainImage}"
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


                <c:if test="${goodsTotalPages > 1}">

                    <c:set var="goodsStartPage"
                           value="${goodsCurrentPage - 2}"/>

                    <c:if test="${goodsStartPage < 1}">

                        <c:set var="goodsStartPage"
                               value="1"/>

                    </c:if>

                    <c:set var="goodsEndPage"
                           value="${goodsCurrentPage + 2}"/>

                    <c:if test="${goodsEndPage > goodsTotalPages}">

                        <c:set var="goodsEndPage"
                               value="${goodsTotalPages}"/>

                    </c:if>


                    <section class="pagination search-pagination">


                        <c:if test="${goodsCurrentPage > 1}">

                            <c:url var="goodsPrevUrl"
                                   value="/search">

                                <c:param name="keyword"
                                         value="${keyword}"/>

                                <c:param name="contentPage"
                                         value="${contentCurrentPage}"/>

                                <c:param name="goodsPage"
                                         value="${goodsCurrentPage - 1}"/>

                                <c:param name="searchTab"
                                         value="GOODS"/>

                                <c:forEach var="v"
                                           items="${contentTypes}">

                                    <c:param name="contentTypes"
                                             value="${v}"/>

                                </c:forEach>

                                <c:forEach var="v"
                                           items="${genreCodes}">

                                    <c:param name="genreCodes"
                                             value="${v}"/>

                                </c:forEach>

                                <c:forEach var="v"
                                           items="${providerIds}">

                                    <c:param name="providerIds"
                                             value="${v}"/>

                                </c:forEach>

                            </c:url>

                            <a class="page-btn page-arrow"
                               href="${goodsPrevUrl}">

                                ‹

                            </a>

                        </c:if>


                        <c:if test="${goodsStartPage > 1}">

                            <c:url var="goodsFirstUrl"
                                   value="/search">

                                <c:param name="keyword"
                                         value="${keyword}"/>

                                <c:param name="contentPage"
                                         value="${contentCurrentPage}"/>

                                <c:param name="goodsPage"
                                         value="1"/>

                                <c:param name="searchTab"
                                         value="GOODS"/>

                                <c:forEach var="v"
                                           items="${contentTypes}">

                                    <c:param name="contentTypes"
                                             value="${v}"/>

                                </c:forEach>

                                <c:forEach var="v"
                                           items="${genreCodes}">

                                    <c:param name="genreCodes"
                                             value="${v}"/>

                                </c:forEach>

                                <c:forEach var="v"
                                           items="${providerIds}">

                                    <c:param name="providerIds"
                                             value="${v}"/>

                                </c:forEach>

                            </c:url>

                            <a class="page-btn"
                               href="${goodsFirstUrl}">

                                1

                            </a>

                        </c:if>


                        <c:if test="${goodsStartPage > 2}">

                            <span class="page-ellipsis">
                                ...
                            </span>

                        </c:if>


                        <c:forEach var="pageNumber"
                                   begin="${goodsStartPage}"
                                   end="${goodsEndPage}">

                            <c:choose>

                                <c:when test="${pageNumber == goodsCurrentPage}">

                                    <span class="page-now">

                                        <c:out value="${pageNumber}"/>

                                    </span>

                                </c:when>

                                <c:otherwise>

                                    <c:url var="goodsPageUrl"
                                           value="/search">

                                        <c:param name="keyword"
                                                 value="${keyword}"/>

                                        <c:param name="contentPage"
                                                 value="${contentCurrentPage}"/>

                                        <c:param name="goodsPage"
                                                 value="${pageNumber}"/>

                                        <c:param name="searchTab"
                                                 value="GOODS"/>

                                        <c:forEach var="v"
                                                   items="${contentTypes}">

                                            <c:param name="contentTypes"
                                                     value="${v}"/>

                                        </c:forEach>

                                        <c:forEach var="v"
                                                   items="${genreCodes}">

                                            <c:param name="genreCodes"
                                                     value="${v}"/>

                                        </c:forEach>

                                        <c:forEach var="v"
                                                   items="${providerIds}">

                                            <c:param name="providerIds"
                                                     value="${v}"/>

                                        </c:forEach>

                                    </c:url>

                                    <a class="page-btn"
                                       href="${goodsPageUrl}">

                                        <c:out value="${pageNumber}"/>

                                    </a>

                                </c:otherwise>

                            </c:choose>

                        </c:forEach>


                        <c:if test="${goodsEndPage < goodsTotalPages - 1}">

                            <span class="page-ellipsis">
                                ...
                            </span>

                        </c:if>


                        <c:if test="${goodsEndPage < goodsTotalPages}">

                            <c:url var="goodsLastUrl"
                                   value="/search">

                                <c:param name="keyword"
                                         value="${keyword}"/>

                                <c:param name="contentPage"
                                         value="${contentCurrentPage}"/>

                                <c:param name="goodsPage"
                                         value="${goodsTotalPages}"/>

                                <c:param name="searchTab"
                                         value="GOODS"/>

                                <c:forEach var="v"
                                           items="${contentTypes}">

                                    <c:param name="contentTypes"
                                             value="${v}"/>

                                </c:forEach>

                                <c:forEach var="v"
                                           items="${genreCodes}">

                                    <c:param name="genreCodes"
                                             value="${v}"/>

                                </c:forEach>

                                <c:forEach var="v"
                                           items="${providerIds}">

                                    <c:param name="providerIds"
                                             value="${v}"/>

                                </c:forEach>

                            </c:url>

                            <a class="page-btn"
                               href="${goodsLastUrl}">

                                <c:out value="${goodsTotalPages}"/>

                            </a>

                        </c:if>


                        <c:if test="${goodsCurrentPage < goodsTotalPages}">

                            <c:url var="goodsNextUrl"
                                   value="/search">

                                <c:param name="keyword"
                                         value="${keyword}"/>

                                <c:param name="contentPage"
                                         value="${contentCurrentPage}"/>

                                <c:param name="goodsPage"
                                         value="${goodsCurrentPage + 1}"/>

                                <c:param name="searchTab"
                                         value="GOODS"/>

                                <c:forEach var="v"
                                           items="${contentTypes}">

                                    <c:param name="contentTypes"
                                             value="${v}"/>

                                </c:forEach>

                                <c:forEach var="v"
                                           items="${genreCodes}">

                                    <c:param name="genreCodes"
                                             value="${v}"/>

                                </c:forEach>

                                <c:forEach var="v"
                                           items="${providerIds}">

                                    <c:param name="providerIds"
                                             value="${v}"/>

                                </c:forEach>

                            </c:url>

                            <a class="page-btn page-arrow"
                               href="${goodsNextUrl}">

                                ›

                            </a>

                        </c:if>

                    </section>

                </c:if>

            </section>

        </section>

    </div>

</main>

<jsp:include page="/WEB-INF/views/common/footer.jsp"/>

</body>
</html>