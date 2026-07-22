<%@ page language="java"
         contentType="text/html; charset=UTF-8"%>
<%@ taglib prefix="c"
           uri="jakarta.tags.core" %>

<%@ taglib prefix="fmt"
           uri="jakarta.tags.fmt" %>

<!DOCTYPE html>
<html lang="ko">

<head>
<meta charset="UTF-8">
<title>ODITJI | 콘텐츠 상세</title>

<link rel="stylesheet"
      href="${pageContext.request.contextPath}/css/content.css">

  <link rel="stylesheet"
      href="${pageContext.request.contextPath}/css/component.css">

<link rel="stylesheet"
      href="${pageContext.request.contextPath}/css/report.css">

<script defer
        src="${pageContext.request.contextPath}/js/content.js"></script>

<script defer
        src="${pageContext.request.contextPath}/js/report.js"></script>
</head>

<body>

<jsp:include page="/WEB-INF/views/common/header.jsp"/>

<div id="contentPageData"
     data-context-path="${pageContext.request.contextPath}"
     hidden>
</div>

<main class="detail-container">

<div class="back-area">

    <a href="${pageContext.request.contextPath}/content/list"
       class="back-btn">
        ← 뒤로가기
    </a>

</div>

<section class="detail-header">

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

            <h1 class="title">
                ${content.title}
            </h1>

            <p class="meta">
                ${content.contentType}
                |
                ${content.releaseDate}
                |
                ${content.ageRating}
            </p>

            <p class="genre">
                장르 : ${content.genreText}
            </p>

        </div>

        <div class="score-box">

            <span class="score-tmdb">
                글로벌 평점 ⭐ ${content.tmdbScore}
            </span>

            <span class="score-divider">
                |
            </span>

            <span class="score-user">

                <c:choose>

                    <c:when test="${not empty avgRating}">
                        오딧지 평점 ⭐
                        <fmt:formatNumber
                            value="${avgRating}"
                            pattern="0.0"/>
                        (${reviewCount}건)
                    </c:when>

                    <c:otherwise>
                        오딧지 평점 ⭐ 평점 없음
                    </c:otherwise>

                </c:choose>

            </span>

            <span>
                👁 ${content.viewCount}
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

            <a href="${pageContext.request.contextPath}/review/list?contentNo=${content.contentNo}"
               class="btn">
                리뷰 보기
            </a>

            <a href="${pageContext.request.contextPath}/review/write?contentNo=${content.contentNo}"
               class="btn">
                리뷰 작성
            </a>

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

    <div class="goods-grid">

        <c:if test="${not empty goodsList}">

            <c:forEach var="g"
                       items="${goodsList}">

                <a href="${pageContext.request.contextPath}/goods/detail?goodsNo=${g.goodsNo}"
                   class="goods-card">

                    <img src="${g.image}"
                         alt="${g.name}">

                    <div class="goods-info">

                        <p class="name">
                            ${g.name}
                        </p>

                        <p class="price">
                            ₩ ${g.price}
                        </p>

                    </div>

                </a>

            </c:forEach>

        </c:if>

    </div>

    <a href="${pageContext.request.contextPath}/goods/list"
       class="more-btn">
        상품 둘러보기
    </a>

</section>

<section id="reviewSection"
         class="detail-section">

    <h2>리뷰</h2>

    <div class="review-write-box">

        <c:choose>

            <c:when test="${not empty myReview}">

                <form action="${pageContext.request.contextPath}/review/update"
                      method="post"
                      class="review-form">

                    <input type="hidden"
                           name="reviewNo"
                           value="${myReview.reviewNo}">

                    <input type="hidden"
                           name="contentNo"
                           value="${content.contentNo}">

                    <div class="rating-box">

                        <label>평점</label>

                        <input type="number"
                               name="rating"
                               min="0"
                               max="5"
                               step="0.5"
                               value="${myReview.rating}">

                    </div>

                    <textarea name="reviewText"
                              required>${myReview.reviewText}</textarea>

                    <button type="submit"
                            class="btn">
                        수정하기
                    </button>

                </form>

            </c:when>

            <c:otherwise>

                <form action="${pageContext.request.contextPath}/review/write"
                      method="post"
                      class="review-form">

                    <input type="hidden"
                           name="contentNo"
                           value="${content.contentNo}">

                    <div class="rating-box">

                        <label>
                            평점 (0 ~ 5)
                        </label>

                        <input type="number"
                               name="rating"
                               min="0"
                               max="5"
                               step="0.5"
                               required>

                    </div>

                    <textarea name="reviewText"
                              placeholder="이 작품에 대한 리뷰를 작성하세요"
                              required></textarea>

                    <button type="submit"
                            class="btn">
                        등록
                    </button>

                </form>

            </c:otherwise>

        </c:choose>

    </div>

    <div class="review-list">

        <c:choose>

            <c:when test="${not empty reviewList}">

                <c:forEach var="r"
                           items="${reviewList}">

                    <c:if test="${r.status eq 'ACTIVE'}">

                        <div class="review-item">

                            <div class="review-meta">

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
                                    <fmt:formatDate
                                        value="${r.createdAt}"
                                        pattern="yyyy-MM-dd"/>
                                </span>

                                <c:choose>

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
                                                data-review-no="${r.reviewNo}"
                                                aria-label="${r.writer}님의 리뷰 신고">
                                            🚨 신고
                                        </button>

                                    </c:otherwise>

                                </c:choose>

                            </div>

                            <p class="review-content"><c:out value="${r.reviewText}"/></p>

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

                <option value="도배">
                    도배
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