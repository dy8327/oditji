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

<title>ODITJI MAIN</title>

<link rel="stylesheet"
      href="${pageContext.request.contextPath}/css/main.css?v=2">

<script defer
        src="${pageContext.request.contextPath}/js/main.js"></script>
</head>

<body>

<jsp:include page="/WEB-INF/views/common/header.jsp"/>

<main id="mainContent" class="main">

    <section class="hero">

        <div class="hero-left">

            <div class="hero-sub">
                OTT 통합 검색 서비스
            </div>

            <h1 class="hero-title">
                세상 모든 OTT<br>
                오딧지?!
            </h1>

            <p class="hero-desc">
                영화, 드라마, 예능, 그리고 관련 굿즈까지<br>
                OTT 정보를 한 번에 확인하세요.
            </p>

        </div>

        <div class="hero-right">

            <div class="hero-box">

                <div class="box-title">
                    인기 콘텐츠
                </div>

                <div class="box-list">

                    <c:choose>

                        <c:when test="${not empty popularContentList}">

                            <c:forEach var="content"
                                       items="${popularContentList}"
                                       begin="0"
                                       end="4"
                                       varStatus="status">

                                <a href="${pageContext.request.contextPath}/content/prepare?tmdbId=${content.tmdbId}&contentType=${content.contentType}"
                                   class="box-item">

                                    <span class="box-rank">
                                        ${status.count}
                                    </span>

                                    <span class="box-name">
                                        ${content.title}
                                    </span>

                                    <c:if test="${not empty content.tmdbScore}">
                                        <span class="box-score">
                                            ⭐ ${content.tmdbScore}
                                        </span>
                                    </c:if>

                                </a>

                            </c:forEach>

                        </c:when>

                        <c:otherwise>

                            <div class="box-empty">
                                인기 콘텐츠를 불러오지 못했습니다.
                            </div>

                        </c:otherwise>

                    </c:choose>

                </div>

            </div>

        </div>

    </section>

    <section class="slider-section">

        <div class="section-header">

            <div>

                <h2 class="section-title">
                    오늘의 콘텐츠
                </h2>

                <p class="section-description">
                    오늘 주목받는 영화와 TV 콘텐츠
                </p>

            </div>

            <a href="${pageContext.request.contextPath}/content/today"
               class="section-more">
                더보기
            </a>

        </div>

        <div class="slider">

            <button class="slider-btn"
                    type="button"
                    aria-label="오늘의 콘텐츠 이전 목록"
                    onclick="moveSlider('today','left')">
                ‹
            </button>

            <div class="track"
                 id="todaySlider">

                <c:choose>

                    <c:when test="${not empty todayContentList}">

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
                               class="card">

                                <div class="thumb">

                                    <c:choose>

                                        <c:when test="${not empty content.posterPath}">

                                            <img src="https://image.tmdb.org/t/p/w500${content.posterPath}"
                                                 alt="${content.title}"
                                                 loading="lazy">

                                        </c:when>

                                        <c:otherwise>

                                            <div class="no-img">
                                                NO IMAGE
                                            </div>

                                        </c:otherwise>

                                    </c:choose>

                                    <span class="content-type-badge">

                                        <c:choose>

                                            <c:when test="${content.contentType eq 'MOVIE'}">
                                                영화
                                            </c:when>

                                            <c:otherwise>
                                                TV
                                            </c:otherwise>

                                        </c:choose>

                                    </span>

                                    <span class="content-age-rating"
                                          title="${ageBadgeTitle}">
                                        <span class="content-age-rating-badge is-${ageBadgeClass}"
                                              aria-label="${ageBadgeTitle}">
                                            ${ageBadgeLabel}
                                        </span>
                                    </span>

                                </div>

                                <div class="card-info">

                                    <h3 class="card-title">
                                        ${content.title}
                                    </h3>

                                    <div class="card-meta">

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

                                            <span class="card-score">
                                                ⭐ ${content.tmdbScore}
                                            </span>

                                        </c:if>

                                    </div>

                                    <c:if test="${not empty content.platformList}">

                                        <div class="card-platform-list"
                                             aria-label="시청 가능한 OTT">

                                            <c:forEach var="platform"
                                                       items="${content.platformList}"
                                                       begin="0"
                                                       end="2">

                                                <img class="card-platform-logo"
                                                     src="${platform.logoImage}"
                                                     alt="${platform.platformName}"
                                                     title="${platform.platformName}"
                                                     loading="lazy">

                                            </c:forEach>

                                            <c:if test="${content.platformList.size() > 3}">

                                                <span class="card-platform-more">
                                                    +${content.platformList.size() - 3}
                                                </span>

                                            </c:if>

                                        </div>

                                    </c:if>

                                </div>

                            </a>

                        </c:forEach>

                    </c:when>

                    <c:otherwise>

                        <div class="slider-empty">
                            오늘의 콘텐츠가 없습니다.
                        </div>

                    </c:otherwise>

                </c:choose>

            </div>

            <button class="slider-btn"
                    type="button"
                    aria-label="오늘의 콘텐츠 다음 목록"
                    onclick="moveSlider('today','right')">
                ›
            </button>

        </div>

    </section>

    <section class="slider-section">

        <div class="section-header">

            <div>

                <h2 class="section-title">
                    추천 콘텐츠
                </h2>

                <p class="section-description">

                    <c:choose>

                        <c:when test="${personalizedRecommendation}">
                            회원님이 이용하는 OTT에서 시청 가능한 콘텐츠
                        </c:when>

                        <c:otherwise>
                            전체 OTT에서 시청 가능한 인기·평점 기반 콘텐츠
                        </c:otherwise>

                    </c:choose>

                </p>

            </div>

            <a href="${pageContext.request.contextPath}/recommend"
               class="section-more">
                더보기
            </a>

        </div>

        <div class="slider">

            <button class="slider-btn"
                    type="button"
                    aria-label="추천 콘텐츠 이전 목록"
                    onclick="moveSlider('rec','left')">
                ‹
            </button>

            <div class="track"
                 id="recSlider">

                <c:choose>

                    <c:when test="${not empty recommendedContentList}">

                        <c:forEach var="content"
                                   items="${recommendedContentList}">

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
                               class="card">

                                <div class="thumb">

                                    <c:choose>

                                        <c:when test="${not empty content.posterPath}">

                                            <img src="https://image.tmdb.org/t/p/w500${content.posterPath}"
                                                 alt="${content.title}"
                                                 loading="lazy">

                                        </c:when>

                                        <c:otherwise>

                                            <div class="no-img">
                                                NO IMAGE
                                            </div>

                                        </c:otherwise>

                                    </c:choose>

                                    <span class="content-type-badge">

                                        <c:choose>

                                            <c:when test="${content.contentType eq 'MOVIE'}">
                                                영화
                                            </c:when>

                                            <c:otherwise>
                                                TV
                                            </c:otherwise>

                                        </c:choose>

                                    </span>

                                    <span class="content-age-rating"
                                          title="${ageBadgeTitle}">
                                        <span class="content-age-rating-badge is-${ageBadgeClass}"
                                              aria-label="${ageBadgeTitle}">
                                            ${ageBadgeLabel}
                                        </span>
                                    </span>

                                </div>

                                <div class="card-info">

                                    <h3 class="card-title">
                                        ${content.title}
                                    </h3>

                                    <div class="card-meta">

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

                                            <span class="card-score">
                                                ⭐ ${content.tmdbScore}
                                            </span>

                                        </c:if>

                                    </div>

                                    <c:if test="${not empty content.platformList}">

                                        <div class="card-platform-list"
                                             aria-label="시청 가능한 OTT">

                                            <c:forEach var="platform"
                                                       items="${content.platformList}"
                                                       begin="0"
                                                       end="2">

                                                <img class="card-platform-logo"
                                                     src="${platform.logoImage}"
                                                     alt="${platform.platformName}"
                                                     title="${platform.platformName}"
                                                     loading="lazy">

                                            </c:forEach>

                                            <c:if test="${content.platformList.size() > 3}">

                                                <span class="card-platform-more">
                                                    +${content.platformList.size() - 3}
                                                </span>

                                            </c:if>

                                        </div>

                                    </c:if>

                                </div>

                            </a>

                        </c:forEach>

                    </c:when>

                    <c:otherwise>

                        <div class="slider-empty">

                            <c:choose>

                                <c:when test="${personalizedRecommendation}">
                                    선택한 OTT에서 추천할 콘텐츠를 찾지 못했습니다.
                                </c:when>

                                <c:otherwise>
                                    추천 콘텐츠가 없습니다.
                                </c:otherwise>

                            </c:choose>

                        </div>

                    </c:otherwise>

                </c:choose>

            </div>

            <button class="slider-btn"
                    type="button"
                    aria-label="추천 콘텐츠 다음 목록"
                    onclick="moveSlider('rec','right')">
                ›
            </button>

        </div>

    </section>

</main>

<jsp:include page="/WEB-INF/views/common/footer.jsp"/>

</body>
</html>