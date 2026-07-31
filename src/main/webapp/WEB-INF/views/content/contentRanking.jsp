<%@ page language="java"
contentType="text/html; charset=UTF-8"
pageEncoding="UTF-8"%>

<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<%
String contextPath = request.getContextPath();
%>

<!DOCTYPE html>

<html lang="ko">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">

<title>인기 랭킹 | ODITJI</title>

<link rel="stylesheet"
      href="<%=contextPath%>/css/oditji.css">

</head>

<body>

<%@ include file="/WEB-INF/views/common/header.jsp" %>

<main id="mainContent" class="ranking-page">

<section class="ranking-header">
    <h1>한국 OTT 콘텐츠 인기 랭킹</h1>

    <p>
        한국에서 정액제로 제공되는 영화와 TV 콘텐츠를
        JSONL에 저장된 인기도와 평점 기준으로 정렬한 랭킹입니다.
    </p>
</section>

<nav class="ranking-tabs"
     aria-label="OTT 인기 랭킹 선택">

    <button type="button"
            class="ranking-tab-button active"
            data-ranking-tab="overall">
        전체
    </button>

    <button type="button"
            class="ranking-tab-button"
            data-ranking-tab="netflix">
        Netflix
    </button>

    <button type="button"
            class="ranking-tab-button"
            data-ranking-tab="tving">
        TVING
    </button>

    <button type="button"
            class="ranking-tab-button"
            data-ranking-tab="wavve">
        wavve
    </button>

    <button type="button"
            class="ranking-tab-button"
            data-ranking-tab="disney">
        Disney+
    </button>

    <button type="button"
            class="ranking-tab-button"
            data-ranking-tab="watcha">
        Watcha
    </button>

    <button type="button"
            class="ranking-tab-button"
            data-ranking-tab="coupang">
        Coupang Play
    </button>

</nav>

<!-- 전체 인기 랭킹 -->
<section class="ranking-panel active"
         id="ranking-panel-overall">

    <div class="ranking-panel-header">
        <div>
            <h2 class="ranking-panel-title">
                전체 인기 콘텐츠
            </h2>

            <p class="ranking-panel-description">
                지원 OTT에서 제공되는 영화와 TV 통합 인기순입니다.
            </p>
        </div>
    </div>

    <c:choose>
        <c:when test="${not empty overallRanking}">

            <ol class="ranking-list">

                <c:forEach var="content"
                           items="${overallRanking}"
                           varStatus="status">

                    <li class="ranking-card">

                        <c:url var="detailUrl"
                               value="/content/prepare">
                            <c:param name="tmdbId"
                                     value="${content.tmdbId}" />
                            <c:param name="contentType"
                                     value="${content.contentType}" />
                        </c:url>

                        <a href="${detailUrl}"
                           class="ranking-link">

                            <div class="ranking-poster-wrap">

                                <c:choose>
                                    <c:when test="${not empty content.posterPath}">

                                        <img class="ranking-poster"
                                             src="https://image.tmdb.org/t/p/w500${content.posterPath}"
                                             alt="<c:out value='${content.title}' /> 포스터"
                                             loading="lazy">

                                    </c:when>

                                    <c:otherwise>

                                        <div class="ranking-no-poster">
                                            포스터 이미지가 없습니다.
                                        </div>

                                    </c:otherwise>
                                </c:choose>

                                <span class="ranking-number
                                    ${status.count le 3 ? 'top-rank' : ''}">
                                    ${status.count}
                                </span>

                            </div>

                            <div class="ranking-info">

                                <h3 class="ranking-content-title">
                                    <c:out value="${content.title}" />
                                </h3>

                                <div class="ranking-meta">

                                    <span class="ranking-type">
                                        <c:choose>
                                            <c:when test="${content.contentType eq 'MOVIE'}">
                                                영화
                                            </c:when>

                                            <c:when test="${content.contentType eq 'TV'}">
                                                TV
                                            </c:when>

                                            <c:otherwise>
                                                <c:out value="${content.contentType}" />
                                            </c:otherwise>
                                        </c:choose>
                                    </span>

                                    <c:if test="${not empty content.releaseDate}">
                                        <span>
                                            <c:out value="${content.releaseDate}" />
                                        </span>
                                    </c:if>

                                    <c:if test="${not empty content.tmdbScore}">
                                        <span class="ranking-score">
                                            ★
                                            <c:out value="${content.tmdbScore}" />
                                        </span>
                                    </c:if>

                                </div>

                            </div>

                        </a>

                    </li>

                </c:forEach>

            </ol>

        </c:when>

        <c:otherwise>
            <div class="ranking-empty">
                전체 인기 랭킹을 불러오지 못했습니다.
            </div>
        </c:otherwise>
    </c:choose>

</section>

<!-- Netflix 랭킹 -->
<section class="ranking-panel"
         id="ranking-panel-netflix">

    <div class="ranking-panel-header">
        <div>
            <h2 class="ranking-panel-title">
                Netflix 인기 콘텐츠
            </h2>

            <p class="ranking-panel-description">
                한국 Netflix 정액제 제공 콘텐츠 기준입니다.
            </p>
        </div>
    </div>

    <c:set var="netflixRanking"
           value="${platformRankings['Netflix']}" />

    <c:choose>
        <c:when test="${not empty netflixRanking}">

            <ol class="ranking-list">

                <c:forEach var="content"
                           items="${netflixRanking}"
                           varStatus="status">

                    <li class="ranking-card">

                        <c:url var="detailUrl"
                               value="/content/prepare">
                            <c:param name="tmdbId"
                                     value="${content.tmdbId}" />
                            <c:param name="contentType"
                                     value="${content.contentType}" />
                        </c:url>

                        <a href="${detailUrl}"
                           class="ranking-link">

                            <div class="ranking-poster-wrap">

                                <c:choose>
                                    <c:when test="${not empty content.posterPath}">
                                        <img class="ranking-poster"
                                             src="https://image.tmdb.org/t/p/w500${content.posterPath}"
                                             alt="<c:out value='${content.title}' /> 포스터"
                                             loading="lazy">
                                    </c:when>

                                    <c:otherwise>
                                        <div class="ranking-no-poster">
                                            포스터 이미지가 없습니다.
                                        </div>
                                    </c:otherwise>
                                </c:choose>

                                <span class="ranking-number
                                    ${status.count le 3 ? 'top-rank' : ''}">
                                    ${status.count}
                                </span>

                            </div>

                            <div class="ranking-info">

                                <h3 class="ranking-content-title">
                                    <c:out value="${content.title}" />
                                </h3>

                                <div class="ranking-meta">

                                    <span class="ranking-type">
                                        ${content.contentType eq 'MOVIE' ? '영화' : 'TV'}
                                    </span>

                                    <c:if test="${not empty content.releaseDate}">
                                        <span>
                                            <c:out value="${content.releaseDate}" />
                                        </span>
                                    </c:if>

                                    <c:if test="${not empty content.tmdbScore}">
                                        <span class="ranking-score">
                                            ★
                                            <c:out value="${content.tmdbScore}" />
                                        </span>
                                    </c:if>

                                </div>

                            </div>

                        </a>

                    </li>

                </c:forEach>

            </ol>

        </c:when>

        <c:otherwise>
            <div class="ranking-empty">
                Netflix 인기 랭킹을 불러오지 못했습니다.
            </div>
        </c:otherwise>
    </c:choose>

</section>

<!-- TVING 랭킹 -->
<section class="ranking-panel"
         id="ranking-panel-tving">

    <div class="ranking-panel-header">
        <div>
            <h2 class="ranking-panel-title">
                TVING 인기 콘텐츠
            </h2>

            <p class="ranking-panel-description">
                한국 TVING 정액제 제공 콘텐츠 기준입니다.
            </p>
        </div>
    </div>

    <c:set var="tvingRanking"
           value="${platformRankings['TVING']}" />

    <c:choose>
        <c:when test="${not empty tvingRanking}">

            <ol class="ranking-list">

                <c:forEach var="content"
                           items="${tvingRanking}"
                           varStatus="status">

                    <li class="ranking-card">

                        <c:url var="detailUrl"
                               value="/content/prepare">
                            <c:param name="tmdbId"
                                     value="${content.tmdbId}" />
                            <c:param name="contentType"
                                     value="${content.contentType}" />
                        </c:url>

                        <a href="${detailUrl}"
                           class="ranking-link">

                            <div class="ranking-poster-wrap">

                                <c:choose>
                                    <c:when test="${not empty content.posterPath}">
                                        <img class="ranking-poster"
                                             src="https://image.tmdb.org/t/p/w500${content.posterPath}"
                                             alt="<c:out value='${content.title}' /> 포스터"
                                             loading="lazy">
                                    </c:when>

                                    <c:otherwise>
                                        <div class="ranking-no-poster">
                                            포스터 이미지가 없습니다.
                                        </div>
                                    </c:otherwise>
                                </c:choose>

                                <span class="ranking-number
                                    ${status.count le 3 ? 'top-rank' : ''}">
                                    ${status.count}
                                </span>

                            </div>

                            <div class="ranking-info">

                                <h3 class="ranking-content-title">
                                    <c:out value="${content.title}" />
                                </h3>

                                <div class="ranking-meta">

                                    <span class="ranking-type">
                                        ${content.contentType eq 'MOVIE' ? '영화' : 'TV'}
                                    </span>

                                    <c:if test="${not empty content.releaseDate}">
                                        <span>
                                            <c:out value="${content.releaseDate}" />
                                        </span>
                                    </c:if>

                                    <c:if test="${not empty content.tmdbScore}">
                                        <span class="ranking-score">
                                            ★
                                            <c:out value="${content.tmdbScore}" />
                                        </span>
                                    </c:if>

                                </div>

                            </div>

                        </a>

                    </li>

                </c:forEach>

            </ol>

        </c:when>

        <c:otherwise>
            <div class="ranking-empty">
                TVING 인기 랭킹을 불러오지 못했습니다.
            </div>
        </c:otherwise>
    </c:choose>

</section>

<!-- wavve 랭킹 -->
<section class="ranking-panel"
         id="ranking-panel-wavve">

    <div class="ranking-panel-header">
        <div>
            <h2 class="ranking-panel-title">
                wavve 인기 콘텐츠
            </h2>

            <p class="ranking-panel-description">
                한국 wavve 정액제 제공 콘텐츠 기준입니다.
            </p>
        </div>
    </div>

    <c:set var="wavveRanking"
           value="${platformRankings['wavve']}" />

    <c:choose>
        <c:when test="${not empty wavveRanking}">

            <ol class="ranking-list">

                <c:forEach var="content"
                           items="${wavveRanking}"
                           varStatus="status">

                    <li class="ranking-card">

                        <c:url var="detailUrl"
                               value="/content/prepare">
                            <c:param name="tmdbId"
                                     value="${content.tmdbId}" />
                            <c:param name="contentType"
                                     value="${content.contentType}" />
                        </c:url>

                        <a href="${detailUrl}"
                           class="ranking-link">

                            <div class="ranking-poster-wrap">

                                <c:choose>
                                    <c:when test="${not empty content.posterPath}">
                                        <img class="ranking-poster"
                                             src="https://image.tmdb.org/t/p/w500${content.posterPath}"
                                             alt="<c:out value='${content.title}' /> 포스터"
                                             loading="lazy">
                                    </c:when>

                                    <c:otherwise>
                                        <div class="ranking-no-poster">
                                            포스터 이미지가 없습니다.
                                        </div>
                                    </c:otherwise>
                                </c:choose>

                                <span class="ranking-number
                                    ${status.count le 3 ? 'top-rank' : ''}">
                                    ${status.count}
                                </span>

                            </div>

                            <div class="ranking-info">

                                <h3 class="ranking-content-title">
                                    <c:out value="${content.title}" />
                                </h3>

                                <div class="ranking-meta">

                                    <span class="ranking-type">
                                        ${content.contentType eq 'MOVIE' ? '영화' : 'TV'}
                                    </span>

                                    <c:if test="${not empty content.releaseDate}">
                                        <span>
                                            <c:out value="${content.releaseDate}" />
                                        </span>
                                    </c:if>

                                    <c:if test="${not empty content.tmdbScore}">
                                        <span class="ranking-score">
                                            ★
                                            <c:out value="${content.tmdbScore}" />
                                        </span>
                                    </c:if>

                                </div>

                            </div>

                        </a>

                    </li>

                </c:forEach>

            </ol>

        </c:when>

        <c:otherwise>
            <div class="ranking-empty">
                wavve 인기 랭킹을 불러오지 못했습니다.
            </div>
        </c:otherwise>
    </c:choose>

</section>

<!-- Disney Plus 랭킹 -->
<section class="ranking-panel"
         id="ranking-panel-disney">

    <div class="ranking-panel-header">
        <div>
            <h2 class="ranking-panel-title">
                Disney+ 인기 콘텐츠
            </h2>

            <p class="ranking-panel-description">
                한국 Disney+ 정액제 제공 콘텐츠 기준입니다.
            </p>
        </div>
    </div>

    <c:set var="disneyRanking"
           value="${platformRankings['Disney Plus']}" />

    <c:choose>
        <c:when test="${not empty disneyRanking}">

            <ol class="ranking-list">

                <c:forEach var="content"
                           items="${disneyRanking}"
                           varStatus="status">

                    <li class="ranking-card">

                        <c:url var="detailUrl"
                               value="/content/prepare">
                            <c:param name="tmdbId"
                                     value="${content.tmdbId}" />
                            <c:param name="contentType"
                                     value="${content.contentType}" />
                        </c:url>

                        <a href="${detailUrl}"
                           class="ranking-link">

                            <div class="ranking-poster-wrap">

                                <c:choose>
                                    <c:when test="${not empty content.posterPath}">
                                        <img class="ranking-poster"
                                             src="https://image.tmdb.org/t/p/w500${content.posterPath}"
                                             alt="<c:out value='${content.title}' /> 포스터"
                                             loading="lazy">
                                    </c:when>

                                    <c:otherwise>
                                        <div class="ranking-no-poster">
                                            포스터 이미지가 없습니다.
                                        </div>
                                    </c:otherwise>
                                </c:choose>

                                <span class="ranking-number
                                    ${status.count le 3 ? 'top-rank' : ''}">
                                    ${status.count}
                                </span>

                            </div>

                            <div class="ranking-info">

                                <h3 class="ranking-content-title">
                                    <c:out value="${content.title}" />
                                </h3>

                                <div class="ranking-meta">

                                    <span class="ranking-type">
                                        ${content.contentType eq 'MOVIE' ? '영화' : 'TV'}
                                    </span>

                                    <c:if test="${not empty content.releaseDate}">
                                        <span>
                                            <c:out value="${content.releaseDate}" />
                                        </span>
                                    </c:if>

                                    <c:if test="${not empty content.tmdbScore}">
                                        <span class="ranking-score">
                                            ★
                                            <c:out value="${content.tmdbScore}" />
                                        </span>
                                    </c:if>

                                </div>

                            </div>

                        </a>

                    </li>

                </c:forEach>

            </ol>

        </c:when>

        <c:otherwise>
            <div class="ranking-empty">
                Disney+ 인기 랭킹을 불러오지 못했습니다.
            </div>
        </c:otherwise>
    </c:choose>

</section>

<!-- Watcha 랭킹 -->
<section class="ranking-panel"
         id="ranking-panel-watcha">

    <div class="ranking-panel-header">
        <div>
            <h2 class="ranking-panel-title">
                Watcha 인기 콘텐츠
            </h2>

            <p class="ranking-panel-description">
                한국 Watcha 정액제 제공 콘텐츠 기준입니다.
            </p>
        </div>
    </div>

    <c:set var="watchaRanking"
           value="${platformRankings['Watcha']}" />

    <c:choose>
        <c:when test="${not empty watchaRanking}">

            <ol class="ranking-list">

                <c:forEach var="content"
                           items="${watchaRanking}"
                           varStatus="status">

                    <li class="ranking-card">

                        <c:url var="detailUrl"
                               value="/content/prepare">
                            <c:param name="tmdbId"
                                     value="${content.tmdbId}" />
                            <c:param name="contentType"
                                     value="${content.contentType}" />
                        </c:url>

                        <a href="${detailUrl}"
                           class="ranking-link">

                            <div class="ranking-poster-wrap">

                                <c:choose>
                                    <c:when test="${not empty content.posterPath}">
                                        <img class="ranking-poster"
                                             src="https://image.tmdb.org/t/p/w500${content.posterPath}"
                                             alt="<c:out value='${content.title}' /> 포스터"
                                             loading="lazy">
                                    </c:when>

                                    <c:otherwise>
                                        <div class="ranking-no-poster">
                                            포스터 이미지가 없습니다.
                                        </div>
                                    </c:otherwise>
                                </c:choose>

                                <span class="ranking-number
                                    ${status.count le 3 ? 'top-rank' : ''}">
                                    ${status.count}
                                </span>

                            </div>

                            <div class="ranking-info">

                                <h3 class="ranking-content-title">
                                    <c:out value="${content.title}" />
                                </h3>

                                <div class="ranking-meta">

                                    <span class="ranking-type">
                                        ${content.contentType eq 'MOVIE' ? '영화' : 'TV'}
                                    </span>

                                    <c:if test="${not empty content.releaseDate}">
                                        <span>
                                            <c:out value="${content.releaseDate}" />
                                        </span>
                                    </c:if>

                                    <c:if test="${not empty content.tmdbScore}">
                                        <span class="ranking-score">
                                            ★
                                            <c:out value="${content.tmdbScore}" />
                                        </span>
                                    </c:if>

                                </div>

                            </div>

                        </a>

                    </li>

                </c:forEach>

            </ol>

        </c:when>

        <c:otherwise>
            <div class="ranking-empty">
                Watcha 인기 랭킹을 불러오지 못했습니다.
            </div>
        </c:otherwise>
    </c:choose>

</section>

<!-- Coupang Play 랭킹 -->
<section class="ranking-panel"
         id="ranking-panel-coupang">

    <div class="ranking-panel-header">
        <div>
            <h2 class="ranking-panel-title">
                Coupang Play 인기 콘텐츠
            </h2>

            <p class="ranking-panel-description">
                한국 Coupang Play 정액제 제공 콘텐츠 기준입니다.
            </p>
        </div>
    </div>

    <c:set var="coupangRanking"
           value="${platformRankings['Coupangplay']}" />

    <c:choose>
        <c:when test="${not empty coupangRanking}">

            <ol class="ranking-list">

                <c:forEach var="content"
                           items="${coupangRanking}"
                           varStatus="status">

                    <li class="ranking-card">

                        <c:url var="detailUrl"
                               value="/content/prepare">
                            <c:param name="tmdbId"
                                     value="${content.tmdbId}" />
                            <c:param name="contentType"
                                     value="${content.contentType}" />
                        </c:url>

                        <a href="${detailUrl}"
                           class="ranking-link">

                            <div class="ranking-poster-wrap">

                                <c:choose>
                                    <c:when test="${not empty content.posterPath}">
                                        <img class="ranking-poster"
                                             src="https://image.tmdb.org/t/p/w500${content.posterPath}"
                                             alt="<c:out value='${content.title}' /> 포스터"
                                             loading="lazy">
                                    </c:when>

                                    <c:otherwise>
                                        <div class="ranking-no-poster">
                                            포스터 이미지가 없습니다.
                                        </div>
                                    </c:otherwise>
                                </c:choose>

                                <span class="ranking-number
                                    ${status.count le 3 ? 'top-rank' : ''}">
                                    ${status.count}
                                </span>

                            </div>

                            <div class="ranking-info">

                                <h3 class="ranking-content-title">
                                    <c:out value="${content.title}" />
                                </h3>

                                <div class="ranking-meta">

                                    <span class="ranking-type">
                                        ${content.contentType eq 'MOVIE' ? '영화' : 'TV'}
                                    </span>

                                    <c:if test="${not empty content.releaseDate}">
                                        <span>
                                            <c:out value="${content.releaseDate}" />
                                        </span>
                                    </c:if>

                                    <c:if test="${not empty content.tmdbScore}">
                                        <span class="ranking-score">
                                            ★
                                            <c:out value="${content.tmdbScore}" />
                                        </span>
                                    </c:if>

                                </div>

                            </div>

                        </a>

                    </li>

                </c:forEach>

            </ol>

        </c:when>

        <c:otherwise>
            <div class="ranking-empty">
                Coupang Play 인기 랭킹을 불러오지 못했습니다.
            </div>
        </c:otherwise>
    </c:choose>

</section>

</main>

<script defer
        src="<%=contextPath%>/js/contentRanking.js"></script>

</body>
</html>
