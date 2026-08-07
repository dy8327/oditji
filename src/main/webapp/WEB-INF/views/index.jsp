<%@ page language="java"
         contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8" %>

<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ taglib prefix="oditji" tagdir="/WEB-INF/tags/content" %>
<%@ taglib prefix="common" tagdir="/WEB-INF/tags/common" %>

<!DOCTYPE html>
<html lang="ko">

<head>
<meta charset="UTF-8">
<meta name="viewport"
      content="width=device-width, initial-scale=1.0">

<title>ODITJI MAIN</title>

<link rel="stylesheet"
      href="${pageContext.request.contextPath}/css/main.css?v=4">

<script defer
        src="${pageContext.request.contextPath}/js/main.js"></script>
</head>

<body>

<jsp:include page="/WEB-INF/views/common/header.jsp"/>

<main id="mainContent" class="main">

    <%--
        실제 OTT(넷플릭스/웨이브/티빙) 메인처럼 화면 폭 전체를 채우는
        시네마틱 배너로 교체합니다. "오늘의 콘텐츠"의 백드롭 이미지를
        재사용해 자동으로 넘어가는 슬라이드를 구성하고, 데이터가
        없을 때는 "추천 콘텐츠" → 브랜드 소개 문구 순으로 대체합니다.
    --%>
    <c:choose>
        <c:when test="${not empty todayContentList}">
            <c:set var="heroContentList" value="${todayContentList}" />
            <c:set var="heroEyebrow" value="오늘의 콘텐츠" />
        </c:when>
        <c:when test="${not empty recommendedContentList}">
            <c:set var="heroContentList" value="${recommendedContentList}" />
            <c:set var="heroEyebrow" value="추천 콘텐츠" />
        </c:when>
        <c:otherwise>
            <c:set var="heroContentList" value="${null}" />
        </c:otherwise>
    </c:choose>

    <section class="hero"
             id="mainHero"
             aria-roledescription="carousel"
             aria-label="오딧지 추천 배너">

        <div class="hero-slides" id="heroSlides">

            <%-- 브랜드 소개 슬라이드: 콘텐츠 유무와 상관없이 항상 첫 번째로 노출 --%>
            <article class="hero-slide is-active hero-slide-fallback"
                     data-slide-index="0"
                     aria-hidden="false">

                <div class="hero-slide-bg hero-slide-bg-empty" aria-hidden="true"></div>
                <div class="hero-slide-scrim" aria-hidden="true"></div>

                <div class="hero-slide-body">

                    <span class="hero-eyebrow">
                        OTT 통합 검색 서비스
                    </span>

                    <h1 class="hero-slide-title">
                        세상 모든 OTT<br>
                        오딧지?!
                    </h1>

                    <p class="hero-slide-desc">
                        영화, 드라마, 예능, 그리고 관련 굿즈까지<br>
                        OTT 정보를 한 번에 확인하세요.
                    </p>

                </div>

            </article>

            <c:if test="${not empty heroContentList}">

                    <c:forEach var="content"
                               items="${heroContentList}"
                               begin="0"
                               end="4"
                               varStatus="heroStatus">

                        <article class="hero-slide"
                                 data-slide-index="${heroStatus.index + 1}"
                                 aria-hidden="true">

                            <c:choose>
                                <c:when test="${not empty content.backdropPath}">
                                    <div class="hero-slide-bg"
                                         style="background-image:url('https://image.tmdb.org/t/p/original${content.backdropPath}');"
                                         aria-hidden="true"></div>
                                </c:when>
                                <c:when test="${not empty content.posterPath}">
                                    <div class="hero-slide-bg hero-slide-bg-poster"
                                         style="background-image:url('https://image.tmdb.org/t/p/original${content.posterPath}');"
                                         aria-hidden="true"></div>
                                    <img class="hero-slide-poster-img"
                                         src="https://image.tmdb.org/t/p/w500${content.posterPath}"
                                         alt=""
                                         aria-hidden="true"
                                         loading="lazy">
                                </c:when>
                                <c:otherwise>
                                    <div class="hero-slide-bg hero-slide-bg-empty" aria-hidden="true"></div>
                                </c:otherwise>
                            </c:choose>

                            <div class="hero-slide-scrim" aria-hidden="true"></div>

                            <div class="hero-slide-body">

                                <span class="hero-eyebrow">
                                    오딧지 PICK · <c:out value="${heroEyebrow}"/>
                                </span>

                                <h1 class="hero-slide-title">
                                    <c:out value="${content.title}"/>
                                </h1>

                                <div class="hero-slide-meta">

                                    <common:ageRatingBadge ageRating="${content.ageRating}"
                                                           outerClass="content-age-rating-badge"
                                                           mode="flat"
                                                           showAriaLabel="false" />

                                    <span class="hero-meta-chip">
                                        ${content.contentType eq 'MOVIE' ? '영화' : '시리즈'}
                                    </span>

                                    <c:if test="${not empty content.tmdbScore and content.tmdbScore > 0}">
                                        <span class="hero-meta-score">
                                            ★ ${content.tmdbScore}
                                        </span>
                                    </c:if>

                                    <c:if test="${not empty content.genreText}">
                                        <span class="hero-meta-genre">
                                            <c:out value="${content.genreText}"/>
                                        </span>
                                    </c:if>

                                </div>

                                <c:if test="${not empty content.overview}">
                                    <p class="hero-slide-desc">
                                        <c:out value="${content.overview}"/>
                                    </p>
                                </c:if>

                                <div class="hero-slide-actions">

                                    <a class="hero-btn hero-btn-primary"
                                       href="${pageContext.request.contextPath}/content/prepare?tmdbId=${content.tmdbId}&contentType=${content.contentType}">
                                        <span aria-hidden="true">▶</span> 자세히 보기
                                    </a>

                                    <a class="hero-btn hero-btn-ghost"
                                       href="#todaySection">
                                        더 많은 콘텐츠
                                    </a>

                                </div>

                            </div>

                        </article>

                    </c:forEach>

            </c:if>

        </div>

        <%-- 실시간 인기 콘텐츠는 배너 위 오버레이가 아니라
             넷플릭스식 'TOP 10' 가로 스크롤 섹션으로 배너 아래에 별도 노출 --%>

        <%-- 빠른 탐색 칩: 히어로 하단 좌측의 여백을 채우면서 콘텐츠 탐색 동선을 강화 --%>
        <nav class="hero-quick-links" aria-label="빠른 콘텐츠 탐색">

            <a href="${pageContext.request.contextPath}/content/list?type=popular"
               class="hero-quick-link">
                🔥 인기 콘텐츠
            </a>

            <a href="${pageContext.request.contextPath}/content/list?type=new"
               class="hero-quick-link">
                🆕 신규 콘텐츠
            </a>

            <a href="${pageContext.request.contextPath}/ranking"
               class="hero-quick-link">
                🏆 랭킹
            </a>

            <a href="${pageContext.request.contextPath}/recommend"
               class="hero-quick-link">
                ✨ 맞춤 추천
            </a>

            <a href="${pageContext.request.contextPath}/goods/list?type=popular"
               class="hero-quick-link">
                🛍️ 인기 굿즈
            </a>

        </nav>

        <c:if test="${not empty heroContentList}">

            <button type="button"
                    class="hero-nav hero-nav-prev"
                    id="heroPrevBtn"
                    aria-label="이전 배너 보기"
                    onclick="moveHero(-1)">
                ‹
            </button>

            <button type="button"
                    class="hero-nav hero-nav-next"
                    id="heroNextBtn"
                    aria-label="다음 배너 보기"
                    onclick="moveHero(1)">
                ›
            </button>

            <div class="hero-dots"
                 id="heroDots"
                 role="tablist"
                 aria-label="배너 선택">

                <button type="button"
                        class="hero-dot is-active"
                        role="tab"
                        aria-selected="true"
                        aria-label="오딧지 소개 배너로 이동"
                        onclick="goHero(0)"></button>

                <c:forEach var="content"
                           items="${heroContentList}"
                           begin="0"
                           end="4"
                           varStatus="dotStatus">

                    <button type="button"
                            class="hero-dot"
                            role="tab"
                            aria-selected="false"
                            aria-label="<c:out value='${content.title}'/> 배너로 이동"
                            onclick="goHero(${dotStatus.index + 1})"></button>

                </c:forEach>

            </div>

        </c:if>

    </section>

    <%-- 실시간 인기 콘텐츠: 넷플릭스/웨이브 스타일의 랭킹 넘버 + 카드 가로 스크롤 --%>
    <section class="slider-section rank-slider-section" id="popularRankSection">

        <div class="section-header">

            <div>

                <h2 class="section-title">
                    <span class="section-eyebrow section-eyebrow--rank">LIVE</span>
                    실시간 인기 콘텐츠
                </h2>

                <p class="section-description">
                    지금 오딧지에서 가장 많이 찾는 콘텐츠
                </p>

            </div>

            <a href="${pageContext.request.contextPath}/ranking"
               class="section-more">
                더보기 <span aria-hidden="true">›</span>
            </a>

        </div>

        <div class="slider">

            <button class="slider-btn"
                    type="button"
                    aria-label="실시간 인기 콘텐츠 이전 목록"
                    onclick="moveSlider('rank','left')">
                ‹
            </button>

            <div class="track rank-track"
                 id="rankSlider">

                <c:choose>

                    <c:when test="${not empty popularContentList}">

                        <c:forEach var="content"
                                   items="${popularContentList}"
                                   begin="0"
                                   end="9"
                                   varStatus="status">

                            <div class="rank-item">

                                <span class="rank-number"
                                      aria-hidden="true">
                                    ${status.count}
                                </span>

                                <oditji:contentCard content="${content}" variant="main" />

                            </div>

                        </c:forEach>

                    </c:when>

                    <c:otherwise>

                        <div class="slider-empty">
                            인기 콘텐츠를 불러오지 못했습니다.
                        </div>

                    </c:otherwise>

                </c:choose>

            </div>

            <button class="slider-btn"
                    type="button"
                    aria-label="실시간 인기 콘텐츠 다음 목록"
                    onclick="moveSlider('rank','right')">
                ›
            </button>

        </div>

    </section>

    <%-- 신규 콘텐츠: 최근 등록/공개된 콘텐츠를 별도로 노출 --%>
    <section class="slider-section" id="newSection">

        <div class="section-header">

            <div>

                <h2 class="section-title">
                    <span class="section-eyebrow section-eyebrow--new">NEW</span>
                    신규 콘텐츠
                </h2>

                <p class="section-description">
                    오딧지에 새로 올라온 콘텐츠
                </p>

            </div>

            <a href="${pageContext.request.contextPath}/content/list?type=new"
               class="section-more">
                더보기 <span aria-hidden="true">›</span>
            </a>

        </div>

        <div class="slider">

            <button class="slider-btn"
                    type="button"
                    aria-label="신규 콘텐츠 이전 목록"
                    onclick="moveSlider('new','left')">
                ‹
            </button>

            <div class="track"
                 id="newSlider">

                <c:choose>

                    <c:when test="${not empty newContentList}">

                        <c:forEach var="content"
                                   items="${newContentList}">

                            <oditji:contentCard content="${content}" variant="main" />

                        </c:forEach>

                    </c:when>

                    <c:otherwise>

                        <div class="slider-empty">
                            신규 콘텐츠가 없습니다.
                        </div>

                    </c:otherwise>

                </c:choose>

            </div>

            <button class="slider-btn"
                    type="button"
                    aria-label="신규 콘텐츠 다음 목록"
                    onclick="moveSlider('new','right')">
                ›
            </button>

        </div>

    </section>

    <%-- 인기 콘텐츠: 실시간 랭킹과 별개로, 꾸준히 인기 있는 콘텐츠를 일반 카드 형태로 노출 --%>
    <section class="slider-section" id="popularSection">

        <div class="section-header">

            <div>

                <h2 class="section-title">
                    <span class="section-eyebrow section-eyebrow--popular">POPULAR</span>
                    인기 콘텐츠
                </h2>

                <p class="section-description">
                    오딧지 이용자들이 꾸준히 찾는 인기 콘텐츠
                </p>

            </div>

            <a href="${pageContext.request.contextPath}/content/list?type=popular"
               class="section-more">
                더보기 <span aria-hidden="true">›</span>
            </a>

        </div>

        <div class="slider">

            <button class="slider-btn"
                    type="button"
                    aria-label="인기 콘텐츠 이전 목록"
                    onclick="moveSlider('popular','left')">
                ‹
            </button>

            <div class="track"
                 id="popularSlider">

                <c:choose>

                    <c:when test="${not empty popularSectionContentList}">

                        <c:forEach var="content"
                                   items="${popularSectionContentList}">

                            <oditji:contentCard content="${content}" variant="main" />

                        </c:forEach>

                    </c:when>

                    <c:otherwise>

                        <div class="slider-empty">
                            인기 콘텐츠를 불러오지 못했습니다.
                        </div>

                    </c:otherwise>

                </c:choose>

            </div>

            <button class="slider-btn"
                    type="button"
                    aria-label="인기 콘텐츠 다음 목록"
                    onclick="moveSlider('popular','right')">
                ›
            </button>

        </div>

    </section>

    <section class="slider-section" id="todaySection">

        <div class="section-header">

            <div>

                <h2 class="section-title">
                    <span class="section-eyebrow section-eyebrow--today">TODAY</span>
                    오늘의 콘텐츠
                </h2>

                <p class="section-description">
                    오늘 주목받는 영화와 TV 콘텐츠
                </p>

            </div>

            <a href="${pageContext.request.contextPath}/content/today"
               class="section-more">
                더보기 <span aria-hidden="true">›</span>
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

                            <oditji:contentCard content="${content}" variant="main" />

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
                    <span class="section-eyebrow section-eyebrow--recommend">PICK</span>
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
                더보기 <span aria-hidden="true">›</span>
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

                            <oditji:contentCard content="${content}" variant="main" />

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
