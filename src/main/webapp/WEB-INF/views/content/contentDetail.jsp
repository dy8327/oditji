<%@ page language="java"
         contentType="text/html; charset=UTF-8"%>
<%@ taglib prefix="c"
           uri="jakarta.tags.core" %>

<%@ taglib prefix="fmt"
           uri="jakarta.tags.fmt" %>

<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ taglib prefix="dt" uri="http://oditji.com/functions/datetime" %>
<%@ taglib prefix="common" tagdir="/WEB-INF/tags/common" %>

<!DOCTYPE html>
<html lang="ko">

<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>ODITJI | 콘텐츠 상세</title>

<link rel="stylesheet"
      href="${pageContext.request.contextPath}/css/content.css?v=20260804-1">

  <link rel="stylesheet"
      href="${pageContext.request.contextPath}/css/component.css">

<link rel="stylesheet"
      href="${pageContext.request.contextPath}/css/report.css">

<script defer
        src="${pageContext.request.contextPath}/js/content.js"></script>

<script defer
        src="${pageContext.request.contextPath}/js/report.js"></script>

<%-- [추가] 스포일러 리뷰 블라인드 및 확인 모달 --%>
<script defer src="${pageContext.request.contextPath}/js/spoiler-review.js"></script>

<%-- [수정] 콘텐츠 리뷰 중복 안내, 인라인 수정, 삭제 확인 기능 --%>
<%--<script defer src="${pageContext.request.contextPath}/js/content-review.js"></script>--%>
</head>

<body>

<jsp:include page="/WEB-INF/views/common/header.jsp"/>

<div id="contentPageData"
     data-context-path="${pageContext.request.contextPath}"
     hidden>
</div>

<main id="mainContent" class="detail-container">

<div class="back-area">

    <%--
        기존에는 콘텐츠 목록으로 고정 이동했지만,
        현재는 사용자가 실제로 보고 있던 이전 화면으로 돌아갑니다.

        JavaScript가 브라우저 방문 기록을 확인해 history.back()을 실행하고,
        이전 기록이 없는 직접 접근 상황에서는 콘텐츠 목록을 예비 경로로 사용합니다.
    --%>
    <button type="button"
            id="detailBackButton"
            class="back-btn"
            data-fallback-url="${pageContext.request.contextPath}/content/list">
        ← 뒤로가기
    </button>

</div>

<section class="content-hero">

    <%--
        실제 OTT(넷플릭스/웨이브/티빙 등) 상세페이지처럼 상단에
        콘텐츠 포스터를 흐리게 확대한 배경(백드롭)을 깔아 입체감을 준다.
        별도의 백드롭 이미지 컬럼이 없으므로 이미 갖고 있는 포스터
        이미지를 재사용하며, 실제 카드 내용에는 영향을 주지 않는
        순수 장식용 레이어라 스크린리더에는 노출하지 않는다.
    --%>
    <c:if test="${not empty content.posterPath}">
        <div class="content-hero-backdrop"
             style="background-image:url('https://image.tmdb.org/t/p/w1280${content.posterPath}');"
             aria-hidden="true">
        </div>
    </c:if>

    <div class="content-hero-scrim" aria-hidden="true"></div>

<section class="detail-header content-detail-card">

    <div class="detail-poster">

        <c:choose>

            <c:when test="${not empty content.posterPath}">

                <img src="https://image.tmdb.org/t/p/w500${content.posterPath}"
                     alt="${content.title}">

            </c:when>

            <c:otherwise>

                <div class="no-img">
                    NO IMAGE
                </div>

            </c:otherwise>

        </c:choose>

    </div>

    <div class="detail-info">

        <div class="info-box">

            <%--
                연령등급 뱃지 (콘텐츠 목록 카드의 .content-age-rating-badge와
                동일한 컴포넌트/클래스를 재사용해 등급 표기를 통일한다.
                recommendCard.jsp의 뱃지 분기 로직과 동일)
            --%>
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

            <div class="title-row">

                <h1 class="title">
                    ${content.title}
                </h1>

                <span class="content-age-rating-badge is-${ageBadgeClass}"
                      title="${ageBadgeTitle}"
                      aria-label="${ageBadgeTitle}">
                    ${ageBadgeLabel}
                </span>

                <%--
                    [수정] 조회수 뱃지를 평점 영역(score-box)에서
                    타이틀 줄의 연령등급 뱃지 옆으로 이동. 표시하는
                    값(content.viewCount)은 완전히 동일하게 유지.
                --%>
                <span class="content-view-count"
                      aria-label="조회수 ${content.viewCount}회">
                    <span aria-hidden="true">👁</span>
                    ${content.viewCount}
                </span>

            </div>

            <%--
                [수정] 넷플릭스/웨이브 등 실제 OTT 상세페이지처럼
                콘텐츠 유형·길이·공개일 정보를 하나의 줄글 대신
                구분되는 칩(pill) 형태로 보여준다.
                정보 자체와 표시 조건은 기존과 완전히 동일하게 유지한다.
            --%>
            <div class="meta-chip-list">

                <span class="meta-chip">
                    ${content.contentType}
                </span>

                <c:choose>

                    <c:when test="${content.contentType eq 'TV'
                                  and not empty content.episodeCount}">
                        <span class="meta-chip">
                            총 ${content.episodeCount}화
                        </span>
                    </c:when>

                    <c:when test="${content.contentType eq 'MOVIE'
                                  and not empty content.runtime}">
                        <span class="meta-chip">
                            총 ${content.runtime}분
                        </span>
                    </c:when>

                </c:choose>

                <span class="meta-chip meta-chip--date">
                    ${content.releaseDate}
                </span>

            </div>

            <p class="genre">
                <span class="genre-label">장르</span>
                ${content.genreText}
            </p>

        </div>

        <div class="score-box">

            <span class="score-badge score-badge--tmdb">
                <span class="score-badge-icon" aria-hidden="true">⭐</span>
                <span class="score-badge-text">
                    <em>글로벌 평점</em>
                    <strong>${content.tmdbScore}</strong>
                </span>
            </span>

            <span class="score-badge score-badge--user">
                <span class="score-badge-icon" aria-hidden="true">⭐</span>
                <span class="score-badge-text">
                    <em>오딧지 평점</em>

                    <c:choose>

                        <c:when test="${not empty avgRating}">
                            <span class="score-badge-value">
                                <strong>
                                    <fmt:formatNumber
                                        value="${avgRating}"
                                        pattern="0.0"/>
                                </strong>
                                <small>(${reviewCount}건)</small>
                            </span>
                        </c:when>

                        <c:otherwise>
                            <strong>-</strong>
                        </c:otherwise>

                    </c:choose>

                </span>
            </span>

        </div>

        <div class="action-box">

            <button type="button"
                    id="detailFavoriteBtn"
                    class="btn detail-favorite-btn${favoriteActive ? ' is-active' : ''}"
                    data-content-no="${content.contentNo}"
                    data-active="${favoriteActive}"
                    data-login-required="${loginRequired}"
                    aria-pressed="${favoriteActive}">

                <c:choose>

                    <c:when test="${favoriteActive}">
                        ♥ 찜 완료
                    </c:when>

                    <c:otherwise>
                        ♡ 찜하기
                    </c:otherwise>

                </c:choose>

            </button>

            <%--
                별도 리뷰 목록 페이지로 이동하던 '리뷰 보기' 버튼은 제거합니다.
                리뷰 작성 버튼은 현재 상세 페이지의 리뷰 작성 영역으로
                부드럽게 이동하도록 JavaScript가 처리합니다.
            --%>
            <button type="button"
                    id="scrollReviewWriteBtn"
                    class="btn">
                리뷰 작성
            </button>

        </div>

        <section class="ott-section">

            <h2>시청 가능한 OTT</h2>

            <c:choose>

                <c:when test="${not empty ottList}">

                    <div class="ott-platform-list">

                        <c:forEach var="ott"
                                   items="${ottList}">

                            <c:url var="ottSearchUrl"
                                   value="/content/ott-search">

                                <c:param name="platformName"
                                         value="${ott.platformName}"/>

                                <c:param name="title"
                                         value="${content.title}"/>

                            </c:url>

                            <a href="${ottSearchUrl}"
                               class="ott-platform-item"
                               target="_blank"
                               rel="noopener noreferrer"
                               aria-label="${ott.platformName}에서 ${content.title} 검색">

                                <span class="ott-platform-logo-wrap">

                                    <c:choose>

                                        <c:when test="${not empty ott.logoImage}">

                                            <img src="${ott.logoImage}"
                                                 alt="${ott.platformName}"
                                                 class="ott-platform-logo">

                                        </c:when>

                                        <c:otherwise>

                                            <span class="ott-platform-no-logo">
                                                OTT
                                            </span>

                                        </c:otherwise>

                                    </c:choose>

                                </span>

                                <span class="ott-platform-name">
                                    ${ott.platformName}
                                </span>

                                <span class="ott-platform-link-icon"
                                      aria-hidden="true">
                                    ↗
                                </span>

                            </a>

                        </c:forEach>

                    </div>

                </c:when>

                <c:otherwise>

                    <p class="ott-platform-empty">
                        현재 확인된 시청 가능 OTT가 없습니다.
                    </p>

                </c:otherwise>

            </c:choose>

        </section>

    </div>

</section>

</section>

<section class="detail-section">

    <h2>줄거리</h2>

    <p class="overview">
        ${content.overview}
    </p>

</section>

<section class="detail-section people-section">

    <h2>감독 / 출연</h2>

    <div class="people-group">

        <h3>감독 및 제작진</h3>

        <c:choose>

            <c:when test="${not empty directorList}">

                <div class="people-list">

                    <c:forEach var="director"
                               items="${directorList}">

                        <c:url var="directorFilmographyUrl"
                               value="/content/person/${director.tmdbDirectorId}">

                            <c:param name="role"
                                     value="${director.directorType eq 'CREATOR' ? 'CREATOR' : 'DIRECTOR'}"/>

                        </c:url>

                        <a href="${directorFilmographyUrl}"
                           class="person-card">

                            <div class="person-profile">

                                <c:choose>

                                    <c:when test="${not empty director.profilePath}">

                                        <img src="https://image.tmdb.org/t/p/w185${director.profilePath}"
                                             alt="${director.directorName}">

                                    </c:when>

                                    <c:otherwise>

                                        <div class="person-no-image">
                                            NO IMAGE
                                        </div>

                                    </c:otherwise>

                                </c:choose>

                            </div>

                            <div class="person-info">

                                <strong>
                                    ${director.directorName}
                                </strong>

                                <span>

                                    <c:choose>

                                        <c:when test="${director.directorType eq 'CREATOR'}">
                                            크리에이터
                                        </c:when>

                                        <c:otherwise>
                                            감독
                                        </c:otherwise>

                                    </c:choose>

                                </span>

                            </div>

                        </a>

                    </c:forEach>

                </div>

            </c:when>

            <c:otherwise>

                <p class="people-empty">
                    등록된 감독 정보가 없습니다.
                </p>

            </c:otherwise>

        </c:choose>

    </div>

    <div class="people-group">

        <h3>출연진</h3>

        <c:choose>

            <c:when test="${not empty actorList}">

                <div class="people-list">

                    <c:forEach var="actor"
                               items="${actorList}">

                        <c:url var="actorFilmographyUrl"
                               value="/content/person/${actor.tmdbActorId}">

                            <c:param name="role"
                                     value="ACTOR"/>

                        </c:url>

                        <a href="${actorFilmographyUrl}"
                           class="person-card">

                            <div class="person-profile">

                                <c:choose>

                                    <c:when test="${not empty actor.profilePath}">

                                        <img src="https://image.tmdb.org/t/p/w185${actor.profilePath}"
                                             alt="${actor.actorName}">

                                    </c:when>

                                    <c:otherwise>

                                        <div class="person-no-image">
                                            NO IMAGE
                                        </div>

                                    </c:otherwise>

                                </c:choose>

                            </div>

                            <div class="person-info">

                                <strong>
                                    ${actor.actorName}
                                </strong>

                                <span>

                                    <c:choose>

                                        <c:when test="${not empty actor.characterName}">
                                            ${actor.characterName} 역
                                        </c:when>

                                        <c:otherwise>
                                            출연
                                        </c:otherwise>

                                    </c:choose>

                                </span>

                            </div>

                        </a>

                    </c:forEach>

                </div>

            </c:when>

            <c:otherwise>

                <p class="people-empty">
                    등록된 출연진 정보가 없습니다.
                </p>

            </c:otherwise>

        </c:choose>

    </div>

</section>

<section class="detail-section related-content-section">

    <div class="related-content-header">

        <div>

            <h2>관련 콘텐츠 추천</h2>

            <p>
                JSONL에 저장된 콘텐츠 중 주 장르와 제작진 정보가 비슷한 작품을 추천했어요.
            </p>

        </div>

    </div>

    <c:choose>

        <c:when test="${not empty relatedContentList}">

            <div class="related-content-grid">

                <c:forEach var="related"
                           items="${relatedContentList}">

                    <c:url var="relatedDetailUrl"
                           value="/content/prepare">

                        <c:param name="tmdbId"
                                 value="${related.tmdbId}"/>

                        <c:param name="contentType"
                                 value="${related.contentType}"/>

                    </c:url>

                    <a href="${relatedDetailUrl}"
                       class="related-content-card">

                        <div class="related-content-poster">

                            <c:choose>

                                <c:when test="${not empty related.posterPath}">

                                    <img src="https://image.tmdb.org/t/p/w500${related.posterPath}"
                                         alt="${related.title}">

                                </c:when>

                                <c:otherwise>

                                    <div class="related-content-no-image">
                                        NO IMAGE
                                    </div>

                                </c:otherwise>

                            </c:choose>

                            <span class="related-content-type">

                                <c:choose>

                                    <c:when test="${related.contentType eq 'MOVIE'}">
                                        영화
                                    </c:when>

                                    <c:otherwise>
                                        시리즈
                                    </c:otherwise>

                                </c:choose>

                            </span>

                            <%--
                                [강화] 실제 OTT 서비스처럼 포스터 위에도
                                평점을 오버레이 뱃지로 얹는다. 아래
                                related-content-meta의 평점 텍스트는
                                그대로 유지해 정보 중복 표시로 안전하게
                                처리한다(평점 없을 때는 뱃지 자체를 숨김).
                            --%>
                            <c:if test="${not empty related.tmdbScore}">
                                <span class="related-content-score-badge">
                                    <span aria-hidden="true">⭐</span>
                                    ${related.tmdbScore}
                                </span>
                            </c:if>

                        </div>

                        <div class="related-content-info">

                            <h3>${related.title}</h3>

                            <div class="related-content-meta">

                                <span>

                                    <c:choose>

                                        <c:when test="${not empty related.releaseDate}">
                                            ${related.releaseDate}
                                        </c:when>

                                        <c:otherwise>
                                            공개일 미정
                                        </c:otherwise>

                                    </c:choose>

                                </span>

                                <span>

                                    <c:choose>

                                        <c:when test="${not empty related.tmdbScore}">
                                            ⭐ ${related.tmdbScore}
                                        </c:when>

                                        <c:otherwise>
                                            평점 없음
                                        </c:otherwise>

                                    </c:choose>

                                </span>

                            </div>

                            <p class="related-content-genre">

                                <c:choose>

                                    <c:when test="${not empty related.genreText}">
                                        ${related.genreText}
                                    </c:when>

                                    <c:otherwise>
                                        장르 정보 없음
                                    </c:otherwise>

                                </c:choose>

                            </p>

                            <c:if test="${not empty related.recommendationReason}">

                                <%--
                                    추천 정렬에 사용한 장르·감독·출연진 일치 근거를
                                    카드 하단에 짧은 문장으로 표시합니다.
                                --%>
                                <%--
                                    추천 이유의 대표 유형에 따라 색상 클래스를 적용합니다.
                                    EL에서 문자열을 직접 CSS 클래스로 변환하지 않고
                                    c:choose로 제한된 클래스만 선택하여 안전하게 출력합니다.
                                --%>
                                <c:set var="reasonColorClass"
                                       value="reason-category" />

                                <c:choose>

                                    <c:when test="${related.recommendationReasonType eq 'DIRECTOR'}">
                                        <c:set var="reasonColorClass"
                                               value="reason-director" />
                                    </c:when>

                                    <c:when test="${related.recommendationReasonType eq 'CAST'}">
                                        <c:set var="reasonColorClass"
                                               value="reason-cast" />
                                    </c:when>

                                    <c:when test="${related.recommendationReasonType eq 'MAIN_GENRE'}">
                                        <c:set var="reasonColorClass"
                                               value="reason-main-genre" />
                                    </c:when>

                                    <c:when test="${related.recommendationReasonType eq 'GENRE'}">
                                        <c:set var="reasonColorClass"
                                               value="reason-genre" />
                                    </c:when>

                                </c:choose>

                                <div class="related-content-reason ${reasonColorClass}">

                                    <span class="related-content-reason-label">
                                        추천 이유
                                    </span>

                                    <p>
                                        ${related.recommendationReason}
                                    </p>

                                </div>

                            </c:if>

                        </div>

                    </a>

                </c:forEach>

            </div>

        </c:when>

        <c:otherwise>

            <div class="related-content-empty">
                추천할 수 있는 관련 콘텐츠가 아직 없습니다.
            </div>

        </c:otherwise>

    </c:choose>

</section>

<section class="detail-section">

    <h2>관련 상품</h2>

    <c:choose>

        <c:when test="${not empty goodsList}">

            <div class="goods-grid">

                <c:forEach var="g"
                           items="${goodsList}">

                    <a href="${pageContext.request.contextPath}/goods/goodsDetail/${g.productNo}"
                       class="goods-card">

                        <img src="${pageContext.request.contextPath}${g.mainImage}"
                             alt="${g.productName}">

                        <div class="goods-info">

                            <p class="name">
                                ${g.productName}
                            </p>

                            <p class="price">
                                ₩ ${g.discountPrice}
                            </p>

                        </div>

                    </a>

                </c:forEach>

            </div>

        </c:when>

        <%--
            [수정] 관련 상품이 없을 때 goods-grid가 빈 채로 렌더링되면서
            아래 "상품 둘러보기" 링크만 덩그러니 남아 어색해 보였다.
            관련 콘텐츠 섹션(related-content-empty)과 동일한 빈 상태
            안내 문구를 재사용해 통일감을 준다.
        --%>
        <c:otherwise>

            <div class="related-content-empty">
                등록된 관련 상품이 아직 없습니다.
            </div>

        </c:otherwise>

    </c:choose>

    <a href="${pageContext.request.contextPath}/goods/list"
       class="more-btn">
        상품 둘러보기
    </a>

</section>

<section id="reviewSection"
         class="detail-section">

    <h2>리뷰</h2>

    <%-- [수정] 서버에서 전달한 중복 리뷰 안내 메시지를 JavaScript 알림으로 출력한다. --%>
    <c:if test="${not empty reviewAlertMessage}">
        <div id="reviewAlertMessage"
             data-message="<c:out value='${reviewAlertMessage}'/>"
             hidden></div>
    </c:if>

    <%--
    [수정] 로그인한 사용자가 현재 콘텐츠에 작성한 리뷰가 없는 경우에만
    신규 리뷰 등록 영역을 표시한다.

    본인 리뷰가 존재하면 등록 영역을 숨기고,
    리뷰 삭제 후에는 myReview가 조회되지 않으므로 등록 영역이 다시 표시된다.
    리뷰 수정 시에는 기존 리뷰가 유지되므로 등록 영역은 표시되지 않는다.
    --%>
    <c:if test="${empty myReview}">

        <div id="reviewWriteBox"
             class="review-write-box"
             data-has-my-review="false">

            <%--
                [수정] 다시 리뷰 등록을 시도하면 JavaScript와 서버 양쪽에서 중복 작성을 차단한다.
            --%>
            <form action="${pageContext.request.contextPath}/review/write"
                method="post"
                class="review-form"
                id="contentReviewWriteForm">

                <input type="hidden"
                    name="contentNo"
                    value="${content.contentNo}">

                <div class="rating-box">

                    <label for="writeReviewRating">
                        평점 (0 ~ 5)
                    </label>

                    <input type="number"
                        id="writeReviewRating"
                        name="rating"
                        min="0"
                        max="5"
                        step="0.5"
                        required>

                </div>

                <%--
                    신규 리뷰 textarea에 id와 label을 연결한다.
                    label은 화면 배치를 유지하기 위해 시각적으로만 숨긴다.
                --%>
                <label for="writeReviewText"
                    style="position:absolute;
                            width:1px;
                            height:1px;
                            padding:0;
                            margin:-1px;
                            overflow:hidden;
                            clip:rect(0, 0, 0, 0);
                            white-space:nowrap;
                            border:0;">
                    리뷰 작성 내용
                </label>

                <textarea id="writeReviewText"
                        name="reviewText"
                        placeholder="이 작품에 대한 리뷰를 작성하세요"
                        required></textarea>

                <%-- [추가] 사용자가 직접 스포일러 포함 여부 선택 --%>
                <label class="spoiler-check-box" for="writeSpoilerYn">
                    <input type="checkbox" id="writeSpoilerYn" name="spoilerYn" value="Y">
                    <span>스포일러가 포함되어 있습니다.</span>
                </label>

                <button type="submit"
                        class="btn">
                    등록
                </button>

            </form>

        </div>
    </c:if>

    <div class="review-list">

        <c:choose>

            <c:when test="${not empty reviewList}">

                <c:forEach var="r" items="${reviewList}">

                    <c:if test="${r.status eq 'ACTIVE'}">

                        <%-- [수정] 본인 리뷰인지 구분하여 신고 버튼과 스포일러 표시 방식을 다르게 처리한다. --%>
                        <c:set var="isMyContentReview"
                               value="${not empty sessionScope.loginMember and sessionScope.loginMember.memberNo eq r.memberNo}"/>

                        <div class="review-item"
                             data-review-item
                             data-review-no="${r.reviewNo}">

                            <div class="review-meta">

                                <span class="review-avatar" aria-hidden="true">
                                    <c:if test="${not empty r.writer}">
                                        ${fn:substring(r.writer, 0, 1)}
                                    </c:if>
                                </span>

                                <span class="writer">
                                    ${r.writer}
                                </span>

                                <span class="rating">
                                    ⭐
                                    <fmt:formatNumber
                                        value="${r.rating}"
                                        pattern="0.0"/>
                                </span>

                                <span class="date">
                                    ${dt:format(r.createdAt, 'yyyy-MM-dd')}
                                </span>

                                <c:choose>

                                    <c:when test="${isMyContentReview}">

                                        <%-- [수정] 본인 리뷰에는 신고 대신 수정·삭제 버튼을 표시한다. --%>
                                        <div class="my-review-actions">
                                            <button type="button"
                                                    class="review-btn review-edit-open-btn"
                                                    data-review-edit-open>
                                                수정
                                            </button>

                                            <form action="${pageContext.request.contextPath}/review/deleteContentReview"
                                                  method="post"
                                                  class="review-delete-form">
                                                <input type="hidden" name="reviewNo" value="${r.reviewNo}">
                                                <input type="hidden" name="contentNo" value="${content.contentNo}">
                                                <button type="submit" class="review-delete-btn">
                                                    삭제
                                                </button>
                                            </form>
                                        </div>

                                    </c:when>

                                    <c:when test="${reportedReviewSet.contains(r.reviewNo)}">

                                        <span class="report-btn reported"
                                              aria-disabled="true">
                                            🚨 신고완료
                                        </span>

                                    </c:when>

                                    <c:otherwise>

                                        <button type="button"
                                                class="report-btn"
                                                data-review-type="CONTENT"
                                                data-review-no="${r.reviewNo}">
                                            🚨 신고
                                        </button>

                                    </c:otherwise>

                                </c:choose>

                            </div>

                            <div class="review-display-area" data-review-display>
                                <%-- [수정] 본인 리뷰는 스포일러 포함 여부와 관계없이 내용을 즉시 표시한다. --%>
                                <c:choose>
                                    <c:when test="${r.spoilerYn eq 'Y' and not isMyContentReview}">
                                        <div class="spoiler-review" data-spoiler-review>
                                            <p class="review-content spoiler-review-content"><c:out value="${r.reviewText}"/></p>
                                            <button type="button" class="spoiler-review-overlay" data-spoiler-open>
                                                <strong>스포일러가 포함되어 있습니다.</strong>
                                                <span>내용을 보려면 클릭하세요.</span>
                                            </button>
                                        </div>
                                    </c:when>
                                    <c:otherwise>
                                        <p class="review-content"><c:out value="${r.reviewText}"/></p>
                                    </c:otherwise>
                                </c:choose>
                            </div>

                            <c:if test="${isMyContentReview}">
                                <%-- [수정] 수정 버튼을 누르면 현재 리뷰 카드 내부에서 수정 폼을 표시한다. --%>
                                <form action="${pageContext.request.contextPath}/review/update"
                                      method="post"
                                      class="review-form inline-review-edit-form"
                                      data-review-edit-form
                                      hidden>

                                    <input type="hidden" name="reviewNo" value="${r.reviewNo}">
                                    <input type="hidden" name="contentNo" value="${content.contentNo}">

                                    <div class="rating-box">
                                        <label for="editRating${r.reviewNo}">평점</label>
                                        <input type="number"
                                               id="editRating${r.reviewNo}"
                                               name="rating"
                                               min="0"
                                               max="5"
                                               step="0.5"
                                               value="${r.rating}"
                                               required>
                                    </div>

                                    <label for="editReviewText${r.reviewNo}" class="visually-hidden-label">
                                        리뷰 수정 내용
                                    </label>

                                    <textarea id="editReviewText${r.reviewNo}"
                                              name="reviewText"
                                              required><c:out value="${r.reviewText}"/></textarea>

                                    <label class="spoiler-check-box" for="editSpoilerYn${r.reviewNo}">
                                        <input type="checkbox"
                                               id="editSpoilerYn${r.reviewNo}"
                                               name="spoilerYn"
                                               value="Y"
                                               <c:if test="${r.spoilerYn eq 'Y'}">checked</c:if>>
                                        <span>스포일러가 포함되어 있습니다.</span>
                                    </label>

                                    <div class="inline-review-edit-actions">
                                        <button type="submit" class="btn">수정 완료</button>
                                        <button type="button" class="btn" data-review-edit-cancel>취소</button>
                                    </div>

                                </form>
                            </c:if>

                        </div>

                    </c:if>

                </c:forEach>

            </c:when>

            <c:otherwise>

                <div class="empty-state">
                    아직 등록된 리뷰가 없습니다
                </div>

            </c:otherwise>

        </c:choose>

    </div>

</section>

<%-- [추가] 예/아니오 버튼을 표시하는 스포일러 확인 모달 --%>
<dialog id="spoilerConfirmModal" class="spoiler-confirm-modal" open hidden
        aria-modal="true" aria-labelledby="spoilerConfirmTitle">
    <div class="spoiler-confirm-dialog">
        <h3 id="spoilerConfirmTitle">스포일러 안내</h3>
        <p>스포일러가 포함되어있습니다.<br>계속 보시겠습니까?</p>
        <div class="spoiler-confirm-actions">
            <button type="button" id="spoilerConfirmNo" class="btn">아니오</button>
            <button type="button" id="spoilerConfirmYes" class="btn">예</button>
        </div>
    </div>
</dialog>

<div id="reportModal"
     class="modal-overlay"
     hidden>

    <div class="modal-box">

        <h3>리뷰 신고</h3>

        <input type="hidden"
               id="reportReviewType"
               value="">

        <input type="hidden"
               id="reportReviewNo"
               value="">

        <div class="modal-field">

            <label for="reportReason">
                신고 사유
            </label>

            <select id="reportReason">

                <option value="욕설/비방">
                    욕설/비방
                </option>

                <option value="스팸/광고">
                    스팸/광고
                </option>

                <option value="음란물/불법정보">
                    음란물/불법정보
                </option>

                <option value="기타">
                    기타
                </option>

            </select>

        </div>

        <div class="modal-field">

            <label for="reportDetail">
                상세 내용
            </label>

            <textarea id="reportDetail"
                      maxlength="1000"
                      placeholder="신고 사유를 자세히 적어주세요"></textarea>

        </div>

        <div class="modal-actions">

            <button type="button"
                    id="reportCancelBtn"
                    class="btn">
                취소
            </button>

            <button type="button"
                    id="reportSubmitBtn"
                    class="btn btn-danger">
                신고하기
            </button>

        </div>

    </div>

</div>

</main>

<jsp:include page="/WEB-INF/views/common/footer.jsp"/>

</body>
</html>