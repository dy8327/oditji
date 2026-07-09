<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>ODITJI MAIN</title>

<link rel="stylesheet" href="${pageContext.request.contextPath}/css/main.css">
<script src="${pageContext.request.contextPath}/js/main.js" defer></script>

</head>

<body>

<jsp:include page="/WEB-INF/views/common/header.jsp"/>

<main class="main">

<section class="hero">

    <div class="hero-left">
        <div class="hero-sub">OTT 통합 검색 서비스</div>

        <h1 class="hero-title">
            세상 모든 OTT<br/>
            오딧지?!
        </h1>

        <p class="hero-desc">
            영화, 드라마, 예능, 그리고 관련 굿즈까지<br/>
            OTT 정보를 한 번에 확인하세요.
        </p>
    </div>

    <div class="hero-right">

        <div class="hero-box">
            <div class="box-title">인기 콘텐츠</div>

            <div class="box-list">

                <c:choose>
                    <c:when test="${not empty popularContentList}">
                        <c:forEach var="content"
                                   items="${popularContentList}"
                                   begin="0"
                                   end="3">

                            <c:url var="popularDetailUrl" value="/content/prepare">
                                <c:param name="tmdbId" value="${content.tmdbId}"/>
                                <c:param name="contentType" value="${content.contentType}"/>
                            </c:url>

                            <a href="${popularDetailUrl}"
                               class="box-item"
                               style="display:block; color:inherit; text-decoration:none; margin-bottom:10px;">

                                <strong>${content.title}</strong><br/>

                                <span style="font-size:11px; color:#999;">
                                    ${content.contentType}
                                    · 평점
                                    <fmt:formatNumber value="${content.tmdbScore}"
                                                      pattern="0.0"/>
                                    · 인기도
                                    <fmt:formatNumber value="${content.popularity}"
                                                      pattern="0.0"/>
                                    · ${empty content.releaseDate ? '공개일 미정' : content.releaseDate}
                                </span>

                            </a>
                        </c:forEach>
                    </c:when>

                    <c:otherwise>
                        <div class="box-item">데이터 없음</div>
                    </c:otherwise>
                </c:choose>

            </div>

        </div>

    </div>

</section>

<section class="slider-section">

    <h2 class="section-title">오늘의 콘텐츠</h2>

    <div class="slider">

        <button class="btn" type="button" onclick="moveSlider('today','left')">‹</button>

        <div class="track" id="todaySlider">

            <c:choose>
                <c:when test="${not empty todayContentList}">
                    <c:forEach var="content" items="${todayContentList}">

                        <c:url var="todayDetailUrl" value="/content/prepare">
                            <c:param name="tmdbId" value="${content.tmdbId}"/>
                            <c:param name="contentType" value="${content.contentType}"/>
                        </c:url>

                        <a href="${todayDetailUrl}"
                           class="card"
                           style="height:auto; color:inherit; text-decoration:none;">

                            <div class="thumb" style="height:220px;">

                                <c:choose>
                                    <c:when test="${not empty content.posterPath}">
                                        <img src="https://image.tmdb.org/t/p/w500${content.posterPath}"
                                             alt="${content.title}"
                                             style="width:100%; height:100%; object-fit:cover; border-radius:6px;">
                                    </c:when>
                                    <c:otherwise>
                                        <div class="no-img"></div>
                                    </c:otherwise>
                                </c:choose>

                            </div>

                            <div style="padding:8px; font-size:11px; line-height:1.5;">
                                <strong style="display:block; font-size:12px;">
                                    ${content.title}
                                </strong>

                                <span>
                                    ${content.contentType}
                                    · 평점
                                    <fmt:formatNumber value="${content.tmdbScore}"
                                                      pattern="0.0"/>
                                </span><br/>

                                <span>
                                    인기도
                                    <fmt:formatNumber value="${content.popularity}"
                                                      pattern="0.0"/>
                                </span><br/>

                                <span>
                                    ${empty content.releaseDate ? '공개일 미정' : content.releaseDate}
                                </span>

                                <span style="display:none;">
                                    TMDB ID: ${content.tmdbId}
                                </span>
                            </div>

                        </a>

                    </c:forEach>
                </c:when>

                <c:otherwise>
                    <div class="card">데이터 없음</div>
                </c:otherwise>
            </c:choose>

        </div>

        <button class="btn" type="button" onclick="moveSlider('today','right')">›</button>

    </div>

</section>

<section class="slider-section">

    <h2 class="section-title">추천 콘텐츠</h2>

    <div class="slider">

        <button class="btn" type="button" onclick="moveSlider('rec','left')">‹</button>

        <div class="track" id="recSlider">

            <c:choose>
                <c:when test="${not empty recommendedContentList}">
                    <c:forEach var="content" items="${recommendedContentList}">

                        <c:url var="recommendedDetailUrl" value="/content/prepare">
                            <c:param name="tmdbId" value="${content.tmdbId}"/>
                            <c:param name="contentType" value="${content.contentType}"/>
                        </c:url>

                        <a href="${recommendedDetailUrl}"
                           class="card"
                           style="height:auto; color:inherit; text-decoration:none;">

                            <div class="thumb" style="height:220px;">

                                <c:choose>
                                    <c:when test="${not empty content.posterPath}">
                                        <img src="https://image.tmdb.org/t/p/w500${content.posterPath}"
                                             alt="${content.title}"
                                             style="width:100%; height:100%; object-fit:cover; border-radius:6px;">
                                    </c:when>
                                    <c:otherwise>
                                        <div class="no-img"></div>
                                    </c:otherwise>
                                </c:choose>

                            </div>

                            <div style="padding:8px; font-size:11px; line-height:1.5;">
                                <strong style="display:block; font-size:12px;">
                                    ${content.title}
                                </strong>

                                <span>
                                    ${content.contentType}
                                    · 평점
                                    <fmt:formatNumber value="${content.tmdbScore}"
                                                      pattern="0.0"/>
                                </span><br/>

                                <span>
                                    인기도
                                    <fmt:formatNumber value="${content.popularity}"
                                                      pattern="0.0"/>
                                </span><br/>

                                <span>
                                    ${empty content.releaseDate ? '공개일 미정' : content.releaseDate}
                                </span>

                                <span style="display:none;">
                                    TMDB ID: ${content.tmdbId}
                                </span>
                            </div>

                        </a>

                    </c:forEach>
                </c:when>

                <c:otherwise>
                    <div class="card">데이터 없음</div>
                </c:otherwise>
            </c:choose>

        </div>

        <button class="btn" type="button" onclick="moveSlider('rec','right')">›</button>

    </div>

</section>

</main>

<jsp:include page="/WEB-INF/views/common/footer.jsp"/>

</body>
</html>
